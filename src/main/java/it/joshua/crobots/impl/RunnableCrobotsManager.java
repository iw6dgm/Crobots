package it.joshua.crobots.impl;

import it.joshua.crobots.SQLManagerInterface;
import it.joshua.crobots.SharedVariables;
import it.joshua.crobots.bean.GamesBean;
import it.joshua.crobots.data.TableName;
import java.util.AbstractQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RunnableCrobotsManager implements Runnable {

    private static final Logger logger = Logger.getLogger(RunnableCrobotsManager.class.getName());
    private TableName tableName;
    private SQLManagerInterface mySQLManager;
    private SharedVariables sharedVariables = SharedVariables.getInstance();
    private DataSourceManager dataSourceManager = DataSourceManager.getDataSourceManager();

    public RunnableCrobotsManager(TableName tableName) {
        super();
        this.tableName = tableName;
        sharedVariables.setRunnable(true);
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        long elapsed;
        boolean isCompleted = false;
        int idles = 0;
        int calls = 0;
        int updates = 0;
        AbstractQueue<GamesBean> i;
        logger.info("Starting thread");
        try {
            mySQLManager = SQLManagerFactory.getInstance(tableName);
            mySQLManager.setDataSourceManager(dataSourceManager);
            dataSourceManager.initialize();

            /* If time limit is reached stop the run */
            if (sharedVariables.isTimeLimit()) {
                elapsed = System.currentTimeMillis();
                if (((elapsed - sharedVariables.getGlobalStartTime()) / 60000) >= sharedVariables.getTimeLimitMinutes()) {
                    logger.log(Level.WARNING, "Time limit {0} minute(s) reached. Stopping application.", sharedVariables.getTimeLimitMinutes());
                    sharedVariables.setRunnable(false);
                    isCompleted = true;
                }
            }
            /* Perform until running */
            while (sharedVariables.isRunnable()) {
                if (sharedVariables.isKill() && sharedVariables.getKillfile().exists()) {
                    logger.log(Level.WARNING, "Kill reached! {0} found!", sharedVariables.getKillFile());
                    sharedVariables.setRunnable(false);
                    isCompleted = true;
                } else {
                    /* Retreive non-calculated match */
                    if (!isCompleted && sharedVariables.getBufferSize() < sharedVariables.getBufferMinSize()) {
                        i = mySQLManager.getGamesFromDB();
                        if (i != null && i.size() > 0) {
                            logger.log(Level.FINE, "Append {0} match(es) to buffer...", i.size());
                            sharedVariables.addAllToBuffer(i);
                        } else {
                            isCompleted = true;
                        }
                    }

                    logger.log(Level.FINE, "isGameBufferEmpty {0} size {1}", new Object[]{sharedVariables.isGamesEmpty(), sharedVariables.getGamesSize()});
                    logger.log(Level.FINE, "isInputBufferEmpty {0} size {1}", new Object[]{sharedVariables.isBufferEmpty(), sharedVariables.getBufferSize()});

                    /* Perform the update */
                    if (!sharedVariables.isGamesEmpty()) {
                        if (mySQLManager.updateResults(sharedVariables.getGames())) {
                            calls++;
                        } else {
                            logger.severe("Error updating results");
                            sharedVariables.setRunnable(false);
                            isCompleted = true;
                        }
                    } else if ((!isCompleted && (sharedVariables.getBufferSize() >= sharedVariables.getBufferMinSize()))
                            || (isCompleted && !sharedVariables.isBufferEmpty())) {
                        logger.log(Level.FINE, "Im going to sleep for {0} ms...", sharedVariables.getSleepInterval(tableName));
                        try {
                            Thread.sleep(sharedVariables.getSleepInterval(tableName));
                        } catch (InterruptedException ie) {
                        }
                        idles++;
                    } else if (isCompleted && sharedVariables.isBufferEmpty()) {
                        sharedVariables.setRunnable(false);
                        logger.info("Everything is done here...");
                    }

                    /* If time limit is reached stop the run */
                    if (sharedVariables.isTimeLimit()) {
                        elapsed = System.currentTimeMillis();
                        if (((elapsed - sharedVariables.getGlobalStartTime()) / 60000) >= sharedVariables.getTimeLimitMinutes()) {
                            logger.log(Level.WARNING, "Time limit {0} minute(s) reached. Stopping application.", sharedVariables.getTimeLimitMinutes());
                            sharedVariables.setRunnable(false);
                            isCompleted = true;
                        }
                    }                    
                }
            }
            long endTime = System.currentTimeMillis();
            float seconds = (endTime - startTime) / 1000F;

            if (calls > 0 && seconds > 0) {
                logger.log(Level.INFO, "Calls : {0}; Updates : {1} in {2} seconds. Rate : {3} update/call; {4} update/s. Idles : {5}", new Object[]{calls, updates, Float.toString(seconds), Float.toString(updates / calls), Float.toString(updates / seconds), idles});
            }

            logger.info("Shutdown thread");
        } catch (Exception exception) {
            logger.log(Level.SEVERE, "RunnableCrobotsManager {0}", exception);
        } finally {
            dataSourceManager.closeAll();
        }
    }
}
