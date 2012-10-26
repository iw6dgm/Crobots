package it.joshua.crobots.impl;

import it.joshua.crobots.SharedVariables;
import it.joshua.crobots.bean.GamesBean;
import it.joshua.crobots.data.TableName;
import java.util.List;
import org.apache.log4j.Logger;

public class RunnableCrobotsManager implements Runnable {

    private Logger logger = Logger.getLogger(RunnableCrobotsManager.class);
    private TableName tableName;
    private static SQLManager mySQLManager;
    private static SharedVariables sharedVariables = SharedVariables.getInstance();

    public RunnableCrobotsManager(TableName tableName) {
        super();
        this.tableName = tableName;
        sharedVariables.setRunnable(true);
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        long elapsed = 0;
        boolean isCompleted = false;
        int idles = 0;
        int calls = 0;
        int updates = 0;
        List<GamesBean> i;
        logger.info("Starting thread");
        try {
            if (sharedVariables.isLocalDb() && sharedVariables.getLocalDriver().contains("Oracle")) {
                mySQLManager = SQLManagerOracle.getInstance(tableName);
            } else {
                mySQLManager = SQLManager.getInstance(tableName);
            }

            SQLManager.initialize();

            GamesBean bean;
            if (sharedVariables.isTimeLimit()) {
                elapsed = System.currentTimeMillis();
                if (((elapsed - sharedVariables.getGlobalStartTime()) / 60000) >= sharedVariables.getTimeLimitMinutes()) {
                    logger.warn("Time limit " + sharedVariables.getTimeLimitMinutes() + " minute(s) reached. Stopping application.");
                    sharedVariables.setRunnable(false);
                    isCompleted = true;
                }
            }
            while (sharedVariables.isRunnable()) {
                if (sharedVariables.isKill() && sharedVariables.getKillfile().exists()) {
                    logger.warn("Kill reached! " + sharedVariables.getKillFile() + " found!");
                    sharedVariables.setRunnable(false);
                    isCompleted = true;
                }

                if (!isCompleted && sharedVariables.getBufferSize() <= sharedVariables.getBufferMinSize()) {
                    i = mySQLManager.getGames();
                    if (i != null && i.size() > 0) {
                        logger.debug("Append " + i.size() + " match(es) to buffer...");
                        sharedVariables.addAllToGames(i);
                    } else {
                        isCompleted = true;
                    }
                }

                logger.debug("isGameBufferEmpty " + sharedVariables.isGameBufferEmpty() + " size " + sharedVariables.getGamesSize());
                logger.debug("isInputBufferEmpty " + sharedVariables.isInputBufferEmpty() + " size " + sharedVariables.getBufferSize());

                if (!sharedVariables.isGameBufferEmpty()) {
                    boolean ok = mySQLManager.initializeUpdates();
                    if (ok) {
                        calls++;
                        while (!sharedVariables.isGameBufferEmpty()) {
                            bean = sharedVariables.getAndRemoveBean();
                            if ("update".equals(bean.getAction()) && tableName.equals(bean.getTableName())) {

                                if (!mySQLManager.updateResults(bean)) {
                                    logger.error("Can't update results of " + bean.toString());
                                    logger.warn("Retry " + tableName + " id=" + bean.getId());
                                    sharedVariables.addToGames(bean);
                                } else {
                                    updates++;
                                }
                            } else if ("recovery".equals(bean.getAction()) && tableName.equals(bean.getTableName())) {
                                logger.warn("Recovery " + bean.toString());
                                mySQLManager.recoveryTable(bean);
                            }
                        }
                    } else {
                        logger.error("Can't initialize stored");
                    }

                    mySQLManager.releaseUpdates();
                } else if (isCompleted && sharedVariables.isInputBufferEmpty()) {
                    sharedVariables.setRunnable(false);
                    logger.info("Everything is done here...");
                } else if (sharedVariables.isRunnable()
                        && ((!isCompleted && (sharedVariables.getBufferSize() >= sharedVariables.getBufferMinSize()))
                        || (isCompleted && !sharedVariables.isInputBufferEmpty()))) {
                    logger.debug("Im going to sleep for " + sharedVariables.getSleepInterval(tableName) + " ms...");
                    try {
                        Thread.sleep(sharedVariables.getSleepInterval(tableName));
                    } catch (InterruptedException ie) {
                    }
                    idles++;
                }

                if (sharedVariables.isTimeLimit()) {
                    elapsed = System.currentTimeMillis();
                    if (((elapsed - sharedVariables.getGlobalStartTime()) / 60000) >= sharedVariables.getTimeLimitMinutes()) {
                        logger.warn("Time limit " + sharedVariables.getTimeLimitMinutes() + " minute(s) reached. Stopping application.");
                        sharedVariables.setRunnable(false);
                        isCompleted = true;
                    }
                }
            }
            long endTime = System.currentTimeMillis();
            float seconds = (endTime - startTime) / 1000F;

            if (calls > 0 && seconds > 0) {
                logger.info("Calls : "
                        + calls
                        + "; Updates : "
                        + updates
                        + " in "
                        + Float.toString(seconds)
                        + " seconds. Rate : "
                        + Float.toString(updates / calls)
                        + " update/call; "
                        + Float.toString(updates / seconds)
                        + " update/s. Idles : "
                        + idles);
            }

            logger.info("Shutdown thread");
        } catch (Exception exception) {
            logger.error("", exception);
        } finally {
            SQLManager.closeAll();
        }
    }
}
