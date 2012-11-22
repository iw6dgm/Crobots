package it.joshua.crobots.http.obsolete;

import it.joshua.crobots.SharedVariables;
import it.joshua.crobots.data.TableName;
import it.joshua.crobots.impl.Manager;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

@Deprecated
public class RunHTTP implements Runnable {

    private static final Logger logger = Logger.getLogger(RunHTTP.class.getName());
    private String threadName;
    private TableName tableName;
    private Integer numOfMatch;
    private Manager manager;
    private SharedVariables sharedVariables = SharedVariables.getInstance();

    public RunHTTP(String threadName, TableName tableName) {
        super();
        this.threadName = threadName;
        this.tableName = tableName;
        manager = new Manager.Builder(tableName.getNumOfOpponents()).build();
    }

    private void selfRecovering(String id, String tableName, String checkSum) {
        String url = sharedVariables.getHostName() + "/engine.php?action=recovery&table=" + tableName + "&id=" + id + "&checksum=" + checkSum;
        logger.warning("Trying to recovery Table=" + tableName + "; Id=" + id);
        HTTPClient httpc = new HTTPClient(sharedVariables.getUserName(), sharedVariables.getPassWord());
        try {
            logger.fine(httpc.doQuery(url));
        } catch (IOException e) {
            logger.log(Level.SEVERE,"RunHTTP {0}", e);
        }

    }

    @Override
    public void run() {

        try {
            String[] buffer = new String[sharedVariables.getBigBuffer()];
            String[] outCmd = new String[tableName.getNumOfOpponents()];
            logger.info("Starting thread : " + threadName);
            logger.info("Retrieving " + tableName.getTableName().toUpperCase() + " parameters...");
            String url = sharedVariables.getHostName() + "/engine.php?action=getparam&table=" + tableName + "&checksum=" + manager.encrypt("getparam" + tableName);

            String query = null;

            HTTPClient httpc = new HTTPClient(sharedVariables.getUserName(), sharedVariables.getPassWord());

            try {
                query = httpc.doQuery(url);
            } catch (IOException e) {
                logger.log(Level.SEVERE,"RunHTTP {0}", e);
            }

            if (query != null) {
                try {
                    numOfMatch = Integer.parseInt(query);
                } catch (Exception e) {
                    logger.severe(e.getMessage());
                    logger.severe(query);
                    logger.severe("Error retrieving " + tableName.getTableName().toUpperCase() + " parameters!");
                    throw new InterruptedException("Thread " + threadName + " interrupted!");
                }
            } else {
                logger.severe("Error retrieving " + tableName.getTableName().toUpperCase() + " parameters!");
                throw new InterruptedException("Thread " + threadName + " interrupted!");
            }

            logger.info("Running a series of " + numOfMatch + " matches...");

            logger.info("Starting " + tableName + " ...");
            boolean rows = true;
            //int counter = 0;
            int global_calculated = 0;
            StringBuilder sBuilder;

            do {
                if (sharedVariables.isKill() && sharedVariables.getKillfile().exists()) {
                    logger.warning("Kill reached! " + sharedVariables.getKillFile() + " found!");
                    rows = false;
                } else {
                    url = sharedVariables.getHostName() + "/engine.php?action=select_" + tableName + "&offset=" + sharedVariables.getBufferMinSize() + "&checksum=" + manager.encrypt("select" + tableName);
                    buffer = httpc.doQueryStream(url);
                    if (buffer[0] == null) {
                        logger.fine("No " + tableName + " retreived");
                    }
                    if ((buffer[0] != null) && !(buffer[0].contains("Server Error"))) {
                        int n = 0;
                        while (buffer[n] != null) {
                            StringTokenizer st = new StringTokenizer(buffer[n++], ",");
                            String[] elements = new String[st.countTokens()];
                            int i = 0;
                            while (st.hasMoreElements()) {
                                elements[i++] = (String) st.nextElement();
                            }
                            String id = elements[0];

                            StringBuilder in = new StringBuilder(sharedVariables.getCmdScript());

                            in.append(" ")
                                    .append(threadName)
                                    .append(" ")
                                    .append(numOfMatch)
                                    .append(" ");

                            for (int r = 1; r <= tableName.getNumOfOpponents(); r++) {
                                in.append(sharedVariables.getPath())
                                        .append(sharedVariables.getDelimit())
                                        .append(elements[r]);

                                if (r < tableName.getNumOfOpponents()) {
                                    in.append(" ");
                                }
                            }
                            outCmd = null;
                            try {
                                outCmd = manager.cmdExec(in.toString());
                            } catch (Exception e) {
                                logger.log(Level.SEVERE,"RunHTTP {0}", e);
                            }

                            if (outCmd != null
                                    && outCmd.length == tableName.getNumOfOpponents()) {

                                global_calculated += numOfMatch;
                                String robot = "";
                                String games = "";
                                String won = "";
                                String tie = "";
                                String point = "";

                                sBuilder = new StringBuilder(sharedVariables.getHostName() + "/engine.php?action=update&table=" + tableName + "&id=" + id);
                                String cmdString = "";
                                boolean ok = false;

                                for (int k = 0; k < outCmd.length; k++) {

                                    cmdString = outCmd[k];

                                    if ((cmdString != null) && (cmdString.length() > 60)) {
                                        robot = cmdString.substring(4, 17).trim();
                                        games = cmdString.substring(18, 27).trim();
                                        won = cmdString.substring(28, 37).trim();
                                        tie = cmdString.substring(38, 47).trim();
                                        point = cmdString.substring(58, 68).trim();

                                        logger.fine("Update Robot " + robot
                                                + " games=" + games
                                                + " wins=" + won
                                                + " tie=" + tie
                                                + " point=" + point);
                                        sBuilder.append("&r").append(k).append("=").append(robot).
                                                append("&g").append(k).append("=").append(games).
                                                append("&w").append(k).append("=").append(won).
                                                append("&t").append(k).append("=").append(tie).
                                                append("&p").append(k).append("=").append(point);

                                        ok = true;

                                    } else {
                                        break;
                                    }
                                }

                                if (ok) {
                                    sBuilder.append("&checksum=" + manager.encrypt(id + tableName));
                                    url = sBuilder.toString();
                                    query = null;
                                    try {
                                        query = httpc.doQuery(url);
                                    } catch (IOException e) {
                                        logger.log(Level.SEVERE,"RunHTTP {0}", e);
                                    }

                                    if (!(query != null && query.equals("ok"))) {
                                        logger.severe(query);
                                        logger.severe("SKIP: " + url);
                                        selfRecovering(id, tableName.getTableName(), manager.encrypt("recovery" + tableName + id));
                                    }
                                    //logger.fine(query);
                                    //if ((++counter % 100) == 0) logger.info("Reached " + counter + " updates count on thread " + threadName + " ...");						  		
                                } else {
                                    StringBuilder errorString = new StringBuilder("Calculating match for ");

                                    for (int r = 1; r <= tableName.getNumOfOpponents(); r++) {
                                        errorString.append(" ")
                                                .append(elements[r]);
                                    }
                                    logger.severe(errorString.toString());
                                    selfRecovering(id, tableName.getTableName(), manager.encrypt("recovery" + tableName + id));
                                }
                            } else {
                                StringBuilder errorString = new StringBuilder("Calculating match for ");

                                for (int r = 1; r <= tableName.getNumOfOpponents(); r++) {
                                    errorString.append(" ")
                                            .append(elements[r]);
                                }
                                logger.severe(errorString.toString());
                                selfRecovering(id, tableName.getTableName(), manager.encrypt("recovery" + tableName + id));
                            }
                        }
                    } else {
                        int n = 0;
                        while (buffer[n] != null) {
                            logger.severe(buffer[n++]);
                        }
                        rows = false;
                    }
                }
            } while (rows);
            logger.info(tableName.getTableName().toUpperCase() + " completed...");
            logger.info("Total match(es) calculated : " + global_calculated);
            logger.info("HTTP URLs buffer size : " + sharedVariables.getHttpURLs().size());
            logger.info("Shutdown thread : " + threadName);
            sharedVariables.setActiveThreads((Integer) (sharedVariables.getActiveThreads() - 1));

        } catch (InterruptedException exception) {
            logger.log(Level.SEVERE,"RunHTTP {0}", exception);
            sharedVariables.setActiveThreads((Integer) (sharedVariables.getActiveThreads() - 1));
        }

    }
}
