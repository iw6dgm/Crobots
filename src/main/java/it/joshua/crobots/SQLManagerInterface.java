/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.joshua.crobots;

import it.joshua.crobots.bean.GamesBean;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author joshua
 */
public interface SQLManagerInterface {

    public Connection getConnection(boolean localDb) throws SQLException;

    public void initializeRobots(Vector<String> robots);

    public void setupTable();

    public void setupResults();

    public void setupRobots(Vector<String> robots, boolean localDb);

    public void setupParameters(int param);

    public boolean test(boolean localDb);

    public boolean initializeUpdates();

    public void releaseUpdates();

    public void recoveryTable(GamesBean bean);

    public boolean updateResults(GamesBean bean);

    public List<GamesBean> getGames();
}
