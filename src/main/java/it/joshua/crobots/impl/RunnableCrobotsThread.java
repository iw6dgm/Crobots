package it.joshua.crobots.impl;

import it.joshua.crobots.SharedVariables;
import it.joshua.crobots.bean.GamesBean;
import it.joshua.crobots.bean.RobotGameBean;
import it.joshua.crobots.data.TableName;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RunnableCrobotsThread implements Runnable {

    private static final Logger logger = Logger.getLogger(RunnableCrobotsThread.class.getName());
    private static SharedVariables sharedVariables = SharedVariables.getInstance();
    private String threadName;
    TableName tableName;
    private Integer numOfMatch;
    private Manager manager;
    //private SQLManager mySQLManager;

    public RunnableCrobotsThread(String threadName, TableName tableName) {
        super();
        this.threadName = threadName;
        this.tableName = tableName;
    }

    @Override
    public void run() {
        try {
            long startTime = System.currentTimeMillis();
            logger.info("Starting thread : " + threadName);
            String[] outCmd = new String[tableName.getNumOfOpponents()];
            manager = new Manager.Builder(tableName.getNumOfOpponents()).build();
            logger.info("Retrieving " + tableName.getTableName().toUpperCase() + " parameters...");
            numOfMatch = sharedVariables.getNumOfMatch(tableName);
            if (numOfMatch == 0) {
                logger.severe("Error retrieving " + tableName.getTableName().toUpperCase() + " parameters!");
                throw new InterruptedException("Thread " + threadName + " interrupted!");
            }
            logger.info("Running sets of " + numOfMatch + " matches...");
            logger.info("Starting " + tableName + " ...");
            int global_calculated = 0;
            int idles = 0;
            String cmdString;
            StringBuilder in;
            GamesBean bean, calculatedBean;
            List<RobotGameBean> robots;
            boolean ok;
            do {
                if (sharedVariables.isKill() && sharedVariables.getKillfile().exists()) {
                    logger.warning("Kill reached! " + sharedVariables.getKillFile() + " found!");
                    sharedVariables.setRunnable(false);
                } else if (sharedVariables.isRunnable()) {
                    if (!sharedVariables.isInputBufferEmpty()) {
                        bean = sharedVariables.getFromBuffer();
                        if (bean != null && "match".equals(bean.getAction())) {
                            robots = bean.getRobots();

                            if (robots != null && robots.size() > 0) {
                                in = new StringBuilder(sharedVariables.getCmdScript());
                                in.append(" ")
                                        .append(threadName)
                                        .append(" ")
                                        .append(numOfMatch)
                                        .append(" ");

                                Collections.shuffle(robots);
                                for (RobotGameBean bean2 : robots) {
                                    in.append(sharedVariables.getPath())
                                            .append(sharedVariables.getDelimit())
                                            .append(bean2.getRobot())
                                            .append(" ");
                                }
                                outCmd = null;
                                try {
                                    outCmd = manager.cmdExec(in.toString().trim());
                                } catch (Exception e) {
                                    logger.log(Level.SEVERE, "RunnableCrobotsThread {0}", e);
                                }
                                if (outCmd != null && outCmd.length == tableName.getNumOfOpponents()) {

                                    global_calculated++;
                                    ok = false;
                                    calculatedBean = new GamesBean.Builder(bean.getId(), bean.getTableName(), bean.getGames(), "update").build();

                                    for (int k = 0; k < outCmd.length; k++) {
                                        cmdString = outCmd[k];
                                        if ((cmdString != null) && (cmdString.length() > 60)) {
                                            try {
                                                calculatedBean.getRobots().add(
                                                        RobotGameBean.create(
                                                        cmdString.substring(4, 17).trim(),
                                                        //Integer.parseInt(cmdString.substring( 18, 27).trim()),
                                                        Integer.parseInt(cmdString.substring(28, 37).trim()),
                                                        Integer.parseInt(cmdString.substring(38, 47).trim()),
                                                        Integer.parseInt(cmdString.substring(58, 68).trim())));
                                                ok = true;
                                            } catch (Exception e) {
                                                logger.log(Level.SEVERE,"RunnableCrobotsThread {0}", e);
                                                ok = false;
                                                break;
                                            }
                                        } else {
                                            break;
                                        }
                                    }

                                    if (ok) {
                                        sharedVariables.addToGames(calculatedBean);
                                    } else {
                                        logger.severe("Calculating " + bean.toString());
                                        logger.warning("Retry " + tableName + " id=" + bean.getId());
                                        bean.setAction("match");
                                        sharedVariables.addToBuffer(bean);
                                    }
                                } else {
                                    logger.severe("Calculating " + bean.toString());
                                    logger.warning("Retry " + tableName + " id=" + bean.getId());
                                    bean.setAction("match");
                                    sharedVariables.addToBuffer(bean);
                                }
                            } else {
                                logger.severe("Malformed bean " + bean.toString());
                            }
                        }
                    } else {
                        logger.fine("Im going to sleep for " + sharedVariables.getMainSleepInterval()
                                + " ms...");
                        try {
                            Thread.sleep(sharedVariables.getMainSleepInterval());
                        } catch (InterruptedException ie) {
                        }
                        idles++;
                    }
                }
            } while (sharedVariables.isRunnable());

            long endTime = System.currentTimeMillis();
            float seconds = (endTime - startTime) / 1000F;

            logger.info(tableName.getTableName().toUpperCase() + " completed...");

            if (seconds > 0) {
                logger.info("Total match(es) calculated : "
                        + global_calculated
                        + " in "
                        + Float.toString(seconds)
                        + " seconds. Rate : "
                        + Float.toString(global_calculated / seconds)
                        + " match/s. Idles : "
                        + idles);
            }

            logger.info("Shutdown thread : " + threadName);
            if (!sharedVariables.isGameBufferEmpty()) {
                logger.info("Games buffer size : " + sharedVariables.getGamesSize());
            }
            sharedVariables.setActiveThreads((Integer) (sharedVariables.getActiveThreads() - 1));
        } catch (InterruptedException exception) {
            logger.log(Level.SEVERE,"RunnableCrobotsThread {0}", exception);
            sharedVariables.setActiveThreads((Integer) (sharedVariables.getActiveThreads() - 1));
        }
    }
}
