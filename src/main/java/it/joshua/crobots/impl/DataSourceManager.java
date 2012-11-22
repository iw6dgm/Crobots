/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.joshua.crobots.impl;

import it.joshua.crobots.DataSourceManagerInterface;
import it.joshua.crobots.SharedVariables;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.dbcp.BasicDataSource;

/**
 *
 * @author joshua
 */
public class DataSourceManager implements DataSourceManagerInterface {

    private static SharedVariables sharedVariables = SharedVariables.getInstance();
    private static Logger logger;
    private static BasicDataSource localDataSource, remoteDataSource;

    private DataSourceManager() {
        logger = Logger.getLogger(DataSourceManager.class.getName());
    }

    private static class Container {

        private static final DataSourceManager DATA_SOURCE_MANAGER = new DataSourceManager();
    }
    
    @Override
    public void initialize() {
        try {
            remoteDataSource = getConnection(sharedVariables.getRemoteJdbc(), sharedVariables.getRemoteDriver(), sharedVariables.getRemoteUser(), sharedVariables.getRemotePwd());
            if (sharedVariables.isLocalDb()) {
                localDataSource = getConnection(sharedVariables.getLocalJdbc(), sharedVariables.getLocalDriver(), sharedVariables.getLocalUser(), sharedVariables.getLocalPwd());
            } else {
                localDataSource = remoteDataSource;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "DataSourceManager initialize {0}", e);
        }
    }

    public static DataSourceManager getDataSourceManager() {
        return Container.DATA_SOURCE_MANAGER;
    }

    @Override
    public BasicDataSource getDataSource(boolean local) {
        if (local) {
            return localDataSource;
        }
        return remoteDataSource;
    }

    @Override
    public BasicDataSource getLocalDataSource() {
        return localDataSource;
    }

    @Override
    public BasicDataSource getRemoteDataSource() {
        return remoteDataSource;
    }

    @Override
    public BasicDataSource getConnection(String jdbc, String driver, String user, String password) throws Exception {
        BasicDataSource ds = new BasicDataSource();
        try {
            ds.setDriverClassName(driver);
            ds.setUsername(user);
            ds.setPassword(password);
            ds.setUrl(jdbc);
            if (sharedVariables.isLocalDb()) {
                ds.setDefaultAutoCommit(sharedVariables.isLocalAutocommit());
            } else {
                ds.setDefaultAutoCommit(sharedVariables.isRemoteAutocommit());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "DataSourceManager getConnection {0}", e);
            throw e;
        }
        return ds;
    }
}
