package it.joshua.crobots.http.obsolete;

import it.joshua.crobots.SharedVariables;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Deprecated
public class RunHTTPQueryManager implements Runnable {

    private static final Logger logger = Logger.getLogger(RunHTTPQueryManager.class.getName());
    private static SharedVariables sharedVariables = SharedVariables.getInstance();

    @Override
    public void run() {
        String url;
        String query;
        boolean bufferNotEmpty = sharedVariables.getHttpURLs().size() > 0;
        logger.info("Starting thread RunHTTPQueryManager");
        HTTPClient httpc = new HTTPClient(sharedVariables.getUserName(), sharedVariables.getPassWord());
        sharedVariables.setRunnable(true);
        while (sharedVariables.isRunnable()) {
            if (sharedVariables.isKill() && sharedVariables.getKillfile().exists()) {
                logger.warning("Kill reached! " + sharedVariables.getKillFile() + " found!");
                //bufferNotEmpty=false;
                sharedVariables.setRunnable(false);
                //break;
            } else {
                bufferNotEmpty = sharedVariables.getHttpURLs().size() > 0;
            }

            while (bufferNotEmpty) {
                synchronized (sharedVariables.getHttpURLs()) {
                    url = sharedVariables.getHttpURLs().get(0);
                    sharedVariables.getHttpURLs().remove(0);
                }
                query = null;
                try {
                    query = httpc.doQuery(url);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "RunHTTPQueryManager {0}", e);
                }

                if (!"ok".equals(query)) {
                    logger.severe(query);
                    logger.severe("SKIP: " + url);
                    synchronized (sharedVariables.getHttpURLs()) {
                        sharedVariables.getHttpURLs().add(url);
                    }
                }

                if (sharedVariables.isKill() && sharedVariables.getKillfile().exists()) {
                    logger.warning("Kill reached! " + sharedVariables.getKillFile() + " found!");
                    bufferNotEmpty = false;
                    sharedVariables.setRunnable(false);
                    break;
                } else {
                    bufferNotEmpty = sharedVariables.getHttpURLs().size() > 0;
                }
            }

            if (sharedVariables.getActiveThreads() == 0 && sharedVariables.getHttpURLs().size() == 0) {
                sharedVariables.setRunnable(false);
            }

            if (sharedVariables.isRunnable() && sharedVariables.getHttpURLs().size() == 0) {
                try {
                    Thread.sleep(sharedVariables.getMainSleepInterval());
                } catch (InterruptedException e) {
                }
            }
        }
        logger.info("Shutdown thread RunHTTPQueryManager");

    }
}
