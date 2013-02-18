/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.joshua.crobots.impl;

import it.joshua.crobots.bean.GamesBean;
import it.joshua.crobots.data.TableName;
import java.util.AbstractQueue;
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
    public AbstractQueue<GamesBean> getGamesFromDB() {
        return null;
    }

    @Override
    public void setupTable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setupResults() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setupRobots(List<String> robots, boolean localDb) {
        throw new UnsupportedOperationException("Not supported yet.");
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
       throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void recoveryTable(GamesBean bean) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean updateResults(AbstractQueue<GamesBean> bean) {
        return super.updateResults(bean);
    }
    
    
}
