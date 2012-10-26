/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.joshua.crobots.impl;

import it.joshua.crobots.DataSourceManagerInterface;
import it.joshua.crobots.SharedVariables;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

/**
 *
 * @author joshua
 */
public class DataSourceManager implements DataSourceManagerInterface {

    private static SharedVariables sharedVariables = SharedVariables.getInstance();
    private static Logger logger = Logger.getLogger(DataSourceManager.class);
    private static BasicDataSource localDataSource, remoteDataSource;

    private DataSourceManager() {    
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
            logger.fatal(e);
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
            logger.error("", e);
            throw e;
        }
        return ds;
    }
}
