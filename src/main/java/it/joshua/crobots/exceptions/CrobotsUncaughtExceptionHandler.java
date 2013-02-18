package it.joshua.crobots.exceptions;

import it.joshua.crobots.SharedVariables;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joshua
 */
public class CrobotsUncaughtExceptionHandler implements
        Thread.UncaughtExceptionHandler {

    private static final Logger logger = Logger.getLogger(CrobotsUncaughtExceptionHandler.class.getName());
    private static final SharedVariables sharedVariables = SharedVariables.getInstance();
    
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        logger.log(Level.WARNING, "caught {0}", e);
        sharedVariables.setRunnable(false);
        sharedVariables.setUnrecoverableError(true);
    }
}
