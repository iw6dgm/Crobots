/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.joshua.crobots;

import org.apache.commons.dbcp.BasicDataSource;

/**
 *
 * @author joshua
 */
public interface DataSourceManagerInterface {
    public BasicDataSource getConnection(String jdbc, String driver, String user, String password) throws Exception;
    public void initialize();
    public BasicDataSource getLocalDataSource();
    public BasicDataSource getRemoteDataSource();
    public BasicDataSource getDataSource(boolean local);
}
