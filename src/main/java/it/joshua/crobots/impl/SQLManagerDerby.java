/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.joshua.crobots.impl;

import it.joshua.crobots.bean.GamesBean;
import it.joshua.crobots.data.TableName;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author joshua
 */
public class SQLManagerDerby extends SQLManager {
    private static final Logger logger = Logger.getLogger(SQLManagerDerby.class.getName());
    
    private SQLManagerDerby(TableName tableName) {
        super(tableName);
        sqlUpdateResults = new StringBuilder("UPDATE results_" + tableName.getTableName() + "SET games=?,wins=?,ties=?,points=? WHERE robot=?");
        sqlGetFromDB = "SELECT * FROM " + tableName.getTableName() + " FETCH FIRST ? ROWS ONLY FOR UPDATE";
    }
    
    @Override
    public List<GamesBean> getGamesFromDB() {
        return null;
    }

    @Override
    public void setupTable() {
        
    }

    @Override
    public void setupResults() {
        
    }

    @Override
    public void setupRobots(List<String> robots, boolean localDb) {
        
    }

    @Override
    public boolean test(boolean localDb) {
        return super.test(localDb);
    }

    @Override
    public boolean initializeUpdates() {
        return super.initializeUpdates();
    }

    @Override
    public void releaseUpdates() {
        
    }

    @Override
    public void recoveryTable(GamesBean bean) {
        
    }

    @Override
    public boolean updateResults(GamesBean bean) {
        return super.updateResults(bean);
    }
    
    
}
