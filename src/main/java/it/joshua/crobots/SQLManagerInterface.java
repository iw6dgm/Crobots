/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.joshua.crobots;

import it.joshua.crobots.bean.GamesBean;
import it.joshua.crobots.impl.DataSourceManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.AbstractQueue;
import java.util.List;

/**
 *
 * @author joshua
 */
public interface SQLManagerInterface {
    
    /**
     *
     * @param dataSourceManager Inject the database data source
     */
    public void setDataSourceManager(DataSourceManager dataSourceManager);

    /**
     *
     * @param localDb True if the database is local, false if remote
     * @return a connection from the datasource manager
     * @throws SQLException
     */
    public Connection getConnection(boolean localDb) throws SQLException;

    /**
     * Initialize the local (if available) and remote temporary tables
     * @param robots List of the robot names
     */
    public void initializeRobots(List<String> robots);

    /**
     * Setup the F2F, 3VS3 and 4VS4 tables
     */
    public void setupTable();

    /**
     * Setup the Results (locale and remote) tables
     */
    public void setupResults();

    /**
     *
     * @param robots List of the robot names
     * @param localDb True if the database is local, false if remote
     */
    public void setupRobots(List<String> robots, boolean localDb);

    /**
     * Setup the F2F, 3VS3 or 4VS4 match repetition parameters
     * @param param
     */
    public void setupParameters(int param);

    /**
     * Test the database connection, calling a stored procedure
     * @param localDb True if the database is local, false if remote
     * @return True if the connection is OK, false otherwise
     */
    public boolean test(boolean localDb);

    /**
     * Initialize the callable statement with the corret parameters
     * @return True if no error occured, false otherwise
     */
    public boolean initializeUpdates();

    /**
     * Release the callable statement
     */
    public void releaseUpdates();

    /**
     * Try to calculate again a game, if some error has occurred
     * @param bean Game info to re-match
     */
    public void recoveryTable(GamesBean bean);

    /**
     * Update the results table
     * @param bean Game info of the calculate match
     * @return True if no error occured, false otherwise
     */
    public boolean updateResults(AbstractQueue<GamesBean> bean);

    /**
     * Retreive the list of a bunch of uncalculated games from the DB
     * @return List of uncalculated games
     */
    public AbstractQueue<GamesBean> getGamesFromDB();    
}
