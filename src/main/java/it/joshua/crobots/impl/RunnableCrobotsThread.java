package it.joshua.crobots.impl;

import it.joshua.crobots.SharedVariables;
import it.joshua.crobots.bean.GamesBean;
import it.joshua.crobots.bean.RobotGameBean;
import it.joshua.crobots.data.CONST;
import it.joshua.crobots.data.TableName;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RunnableCrobotsThread implements Runnable {

    private static final Logger logger = Logger.getLogger(RunnableCrobotsThread.class.getName());
    private static SharedVariables sharedVariables = SharedVariables.getInstance();
    private String threadName;
    private TableName tableName;
    private Integer numOfMatch;
    private Manager manager;
    private String[] outCmd;
    //private SQLManager mySQLManager;

    public RunnableCrobotsThread(String threadName, TableName tableName) {
        super();
        this.threadName = threadName;
        this.tableName = tableName;
        this.outCmd = new String[tableName.getNumOfOpponents()];
    }

    private int runCrobotsCmd() {
        int calculated = 0;
        String cmdString;
        StringBuilder in;
        GamesBean bean = null, calculatedBean;
        List<RobotGameBean> robots;
        boolean ok;
        /* Retreive match from buffer*/
        try {
            bean = sharedVariables.getFromBuffer();            
        } catch(NoSuchElementException e) {
            logger.log(Level.WARNING, "runCrobotsCmd {0}", e);
        }
        if (bean != null && CONST._MATCH_.equals(bean.getAction())) {
            robots = bean.getRobots();
            /* Build command line */
            if (robots != null && robots.size() > 0) {
                in = new StringBuilder(sharedVariables.getCmdScript());
                in.append(" ")
                        .append(threadName)
                        .append(" ")
                        .append(numOfMatch)
                        .append(" ");
                /* Shuffle robot names order */
                Collections.shuffle(robots);
                for (RobotGameBean bean2 : robots) {
                    in.append(sharedVariables.getPath())
                            .append(sharedVariables.getDelimit())
                            .append(bean2.getRobot())
                            .append(" ");
                }
                outCmd = null;
                /* Execute command line */
                try {
                    outCmd = manager.cmdExec(in.toString().trim());
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "RunnableCrobotsThread {0}", e);
                }
                /* Check the command line output */
                if (outCmd != null && outCmd.length == tableName.getNumOfOpponents()) {

                    calculated++;
                    ok = false;
                    calculatedBean = new GamesBean.Builder(bean.getId(), bean.getTableName(), bean.getGames(), CONST._UPDATE_).build();
                    /* Build robot update information */
                    for (int k = 0; k < outCmd.length; k++) {
                        cmdString = outCmd[k];
                        if ((cmdString != null) && (cmdString.length() > 60)) {
                            try {
                                calculatedBean.getRobots().add(new RobotGameBean.Builder(cmdString.substring(4, 17).trim()).setWin(Integer.parseInt(cmdString.substring(28, 37).trim())).setTie(Integer.parseInt(cmdString.substring(38, 47).trim())).setPoints(Integer.parseInt(cmdString.substring(58, 68).trim())).build());
                                ok = true;
                            } catch (Exception e) {
                                logger.log(Level.SEVERE, "RunnableCrobotsThread {0}", e);
                                ok = false;
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                    /* Add update information to buffer */
                    if (ok) {
                        sharedVariables.addToGames(calculatedBean);
                    } else {
                        logger.log(Level.SEVERE, "Calculating {0}", bean.toString());
                        logger.log(Level.WARNING, "Retry {0} id={1}", new Object[]{tableName, bean.getId()});
                        bean.setAction(CONST._MATCH_);
                        sharedVariables.addToBuffer(bean);
                    }
                } else {
                    logger.log(Level.SEVERE, "Calculating {0}", bean.toString());
                    logger.log(Level.WARNING, "Retry {0} id={1}", new Object[]{tableName, bean.getId()});
                    bean.setAction(CONST._MATCH_);
                    sharedVariables.addToBuffer(bean);
                }
            } else {
                logger.log(Level.SEVERE, "Malformed bean {0}", bean.toString());
            }
        }
        return calculated;
    }

    @Override
    public void run() {
        try {
            int global_calculated = 0;
            int idles = 0;
            long startTime = System.currentTimeMillis();
            logger.log(Level.INFO, "Starting thread : {0}", threadName);
            manager = new Manager.Builder(tableName.getNumOfOpponents()).build();
            logger.log(Level.INFO, "Retrieving {0} parameters...", tableName.getTableName().toUpperCase());
            numOfMatch = sharedVariables.getNumOfMatch(tableName);
            if (numOfMatch == 0) {
                logger.log(Level.SEVERE, "Error retrieving {0} parameters!", tableName.getTableName().toUpperCase());
                throw new InterruptedException("Thread " + threadName + " interrupted!");
            }
            logger.log(Level.INFO, "Running sets of {0} matches...", numOfMatch);
            logger.log(Level.INFO, "Starting {0} ...", tableName);
            do {
                if (sharedVariables.isKill() && sharedVariables.getKillfile().exists()) {
                    logger.log(Level.WARNING, "Kill reached! {0} found!", sharedVariables.getKillFile());
                    sharedVariables.setRunnable(false);
                } else if (sharedVariables.isRunnable()) {
                    if (!sharedVariables.isBufferEmpty()) {
                        global_calculated += runCrobotsCmd();
                    } else {
                        logger.log(Level.FINE, "Im going to sleep for {0} ms...", sharedVariables.getMainSleepInterval());
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

            logger.log(Level.INFO, "{0} completed...", tableName.getTableName().toUpperCase());

            if (seconds > 0) {
                logger.log(Level.INFO, "Total match(es) calculated : {0} in {1} seconds. Rate : {2} match/s. Idles : {3}", new Object[]{global_calculated, Float.toString(seconds), Float.toString(global_calculated / seconds), idles});
            }

            logger.log(Level.INFO, "Shutdown thread : {0}", threadName);
            if (!sharedVariables.isGamesEmpty()) {
                logger.log(Level.INFO, "Games buffer size : {0}", sharedVariables.getGamesSize());
            }
            sharedVariables.setActiveThreads((Integer) (sharedVariables.getActiveThreads() - 1));
        } catch (InterruptedException exception) {
            logger.log(Level.SEVERE, "RunnableCrobotsThread {0}", exception);
            sharedVariables.setActiveThreads((Integer) (sharedVariables.getActiveThreads() - 1));
        }
    }
}
