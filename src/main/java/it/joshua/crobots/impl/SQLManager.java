package it.joshua.crobots.impl;

/*
 * Created on 13-lug-2006
 *
 * @author mcamangi
 *
 */
import it.joshua.crobots.SQLManagerInterface;
import it.joshua.crobots.SharedVariables;
import it.joshua.crobots.bean.GamesBean;
import it.joshua.crobots.bean.RobotGameBean;
import it.joshua.crobots.data.CONST;
import it.joshua.crobots.data.TableName;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.AbstractQueue;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLManager implements SQLManagerInterface {

    private static final Logger logger = Logger.getLogger(SQLManager.class.getName());
    protected DataSourceManager dataSourceManager;
    //private CallableStatement cs2  = null;
    protected CallableStatement callableStatement = null;
    protected Connection remoteC = null;
    protected StringBuilder sqlRecovery, sqlUpdateResults;
    protected String sqlGetFromDB;
    protected TableName tableName;
    protected static SharedVariables sharedVariables = SharedVariables.getInstance();

    protected SQLManager(TableName tableName) {
        this.tableName = tableName;
        sqlRecovery = new StringBuilder("{CALL pRecovery" + tableName.getTableName() + "(");
        sqlUpdateResults = new StringBuilder("{CALL pUpdate" + tableName.getTableName() + "Results(?, ");
        sqlGetFromDB = "{CALL pSelect" + tableName.getTableName() + "(?)}";
        for (int i = 0; i < tableName.getNumOfOpponents(); i++) {
            if (i > 0) {
                sqlRecovery.append(", ");
                sqlUpdateResults.append(", ");
            }
            sqlRecovery.append("?");
            sqlUpdateResults.append("?, ?, ?, ?");
        }
        sqlRecovery.append(")}");
        sqlUpdateResults.append(")}");
    }

    @Override
    public void setDataSourceManager(DataSourceManager dataSourceManager) {
        this.dataSourceManager = dataSourceManager;
    }

    @Override
    public boolean initializeUpdates() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void releaseUpdates() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected static class Container {

        private static final SQLManager f2fSQLManager = new SQLManager(TableName.F2F);
        private static final SQLManager vs3SQLManager = new SQLManager(TableName.VS3);
        private static final SQLManager vs4SQLManager = new SQLManager(TableName.VS4);
    }

    public static SQLManager getInstance(TableName tableName) {
        switch (tableName) {
            case F2F:
                return Container.f2fSQLManager;
            case VS3:
                return Container.vs3SQLManager;
            case VS4:
                return Container.vs4SQLManager;
        }
        return null;
    }

    @Override
    public Connection getConnection(boolean localDb) throws SQLException {
        if (localDb) {
            return dataSourceManager.getLocalDataSource().getConnection();
        } else {
            return dataSourceManager.getRemoteDataSource().getConnection();
        }
    }

    @Override
    public void initializeRobots(List<String> robots) {
        String sql = "INSERT INTO robots(name) VALUES(?)";
        int ok = 0, failure = 0;
        logger.info("Initialize locally robots ...");
        try (Connection c = getConnection(true)) {
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                for (String robot : robots) {
                    ps.clearParameters();
                    ps.setString(1, robot);
                    try {
                        ps.executeUpdate();
                        ok++;
                    } catch (SQLException e) {
                        logger.log(Level.WARNING, "Name = {0} - {1}", new Object[]{robot, e.getMessage()});
                        failure++;
                    }
                    if (!dataSourceManager.getLocalDataSource().getDefaultAutoCommit()) {
                        c.commit();
                    }
                }
}
        } catch (Exception e) {
            logger.log(Level.SEVERE, "SQLManager {0}", e);
        }
        logger.log(Level.INFO, "Initialize OK(s)={0}; Failure(s)={1}", new Object[]{ok, failure});
    }

    @Override
    public void setupTable() {
        logger.log(Level.INFO, "Setting Up table {0} ...", tableName);
        try (Connection c = getConnection(sharedVariables.isLocalDb())) {
            
            try (CallableStatement cs = c.prepareCall("{CALL pSetup" + tableName.getTableName().toUpperCase() + "()}")) {
                cs.execute();
                if (!sharedVariables.isLocalDb() && !sharedVariables.isRemoteAutocommit()) {
                    c.commit();
                }                
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "SQLManager {0}", e);
        }
    }

    @Override
    public void setupResults() {
        logger.log(Level.INFO, "Setting up results {0} ...", tableName);
        try (Connection c = getConnection(false)) {
            
            try (CallableStatement cs = c.prepareCall("{CALL pSetupResults" + tableName.getTableName().toUpperCase() + "()}")) {
                cs.execute();
                if (!sharedVariables.isRemoteAutocommit()) {
                    c.commit();
                }                
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "SQLManager {0}", e);
        }
    }

    @Override
    public void setupRobots(List<String> robots, boolean localDb) {
        boolean autoCommit;
        logger.log(Level.INFO, "Initialize robots {0} ...", ((localDb) ? "locally" : "remotely"));
        try (Connection c = getConnection(localDb)) {
            if (localDb) {
                autoCommit = sharedVariables.isLocalAutocommit();
            } else {
                autoCommit = sharedVariables.isRemoteAutocommit();
            }
            c.setAutoCommit(autoCommit);
            
            try (CallableStatement cs = c.prepareCall("{CALL pCleanUpRobots()}")) {
                cs.execute();
            }

            try (CallableStatement cs = c.prepareCall("{CALL pInitializeRobot(?)}")) {
                for (String robot : robots) {
                    cs.clearParameters();
                    cs.setString(1, robot);
                    cs.execute();
                }
            }
            
            if (!autoCommit) {
                c.commit();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "SQLManager {0}", e);
        }
    }

    @Override
    public void setupParameters(int param) {
        String sql = "UPDATE parameters SET " + tableName.getTableName().toLowerCase() + "=? WHERE id=1";
        logger.log(Level.INFO, "Setting Up {0} parameter to {1} ...", new Object[]{tableName, param});
        try (Connection c = getConnection(false)) {
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, param);
                ps.executeUpdate();
                if (!dataSourceManager.getRemoteDataSource().getDefaultAutoCommit()) {
                    c.commit();
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "SQLManager {0}", e);
        }
    }

    @Override
    public boolean test(boolean localDb) {
        boolean result = true;

        String sql = "{CALL pTest(?)}";
        try (Connection c = getConnection(localDb)) {
            try (CallableStatement cs = c.prepareCall(sql)) {
                cs.registerOutParameter(1, Types.TIMESTAMP);
                cs.execute();
                logger.log(Level.INFO, "Test at {0}", cs.getTimestamp(1));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "SQLManager {0}", e);
            result = false;
        }
        return result;
    }

    protected void close(CallableStatement cs) {
        try {
            if (cs != null) {
                cs.close();
            }
        } catch (Exception e) {
        } finally {
            cs = null;
        }
    }

    protected void close(Statement cs) {
        try {
            if (cs != null) {
                cs.close();
            }
        } catch (Exception e) {
        } finally {
            cs = null;
        }
    }

    protected void close(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (Exception e) {
        } finally {
            rs = null;
        }
    }

    protected void close(Connection c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (Exception e2) {
        } finally {
            c = null;
        }
    }

    @Override
    public void recoveryTable(GamesBean bean) {
        boolean autoCommit;
        try (Connection c = getConnection(sharedVariables.isLocalDb())) {
            if (sharedVariables.isLocalDb()) {
                autoCommit = sharedVariables.isLocalAutocommit();
            } else {
                autoCommit = sharedVariables.isRemoteAutocommit();
            }
            c.setAutoCommit(autoCommit);

            try (CallableStatement cs4 = c.prepareCall(sqlRecovery.toString())) {
                List<RobotGameBean> robots = bean.getRobots();
                for (int i = 1; i <= tableName.getNumOfOpponents(); i++) {
                    cs4.setString(i, robots.get(i - 1).getRobot());
                }
                cs4.executeUpdate();
                if (!autoCommit) {
                    c.commit();
                }
            }

        } catch (SQLException se) {
            logger.log(Level.SEVERE, "SQLManager {0}", se);
            sharedVariables.setRunnable(false);
            sharedVariables.setUnrecoverableError(true);
        }
    }

    @Override
    public boolean updateResults(AbstractQueue<GamesBean> beans) {
        GamesBean bean = null;
        try (Connection c = getConnection(false)) {
            c.setAutoCommit(sharedVariables.isRemoteAutocommit());

            try (CallableStatement cs = c.prepareCall(sqlUpdateResults.toString())) {
                while (!beans.isEmpty()) {
                    try {
                        bean = beans.remove();
                        if (CONST._UPDATE_.equals(bean.getAction()) && tableName.equals(bean.getTableName())) {
                            int robotIndex = 1;
                            cs.clearParameters();
                            cs.setInt(1, bean.getGames());
                            for (RobotGameBean b : bean.getRobots()) {
                                cs.setString(1 + robotIndex, b.getRobot());
                                cs.setInt(2 + robotIndex, b.getWin());
                                cs.setInt(3 + robotIndex, b.getTie());
                                cs.setInt(4 + robotIndex, b.getPoints());
                                robotIndex += 4;
                            }
                            cs.executeUpdate();
                            if (!sharedVariables.isRemoteAutocommit()) {
                                c.commit();
                            }
                        }
                    } catch (NoSuchElementException e) {
                        logger.log(Level.WARNING, "updateResults {0}", e);
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "updateResults {0}", e);
            if (bean != null) {
                bean.setAction(CONST._RECOVERY_);
                recoveryTable(bean);
            }
            return false;
        }
        return true;
    }

    @Override
    public AbstractQueue<GamesBean> getGamesFromDB() {

        AbstractQueue<GamesBean> result = new ConcurrentLinkedQueue<>();

        GamesBean game;
        boolean autoCommit;

        try (Connection c = getConnection(sharedVariables.isLocalDb())) {
            if (sharedVariables.isLocalDb()) {
                autoCommit = sharedVariables.isLocalAutocommit();
            } else {
                autoCommit = sharedVariables.isRemoteAutocommit();
            }
            c.setAutoCommit(autoCommit);
            try (CallableStatement cs = c.prepareCall(sqlGetFromDB)) {
                cs.setInt(1, sharedVariables.getBufferMinSize());
                cs.execute();
                if (!autoCommit) {
                    c.commit();
                }

                try (ResultSet rs = cs.getResultSet()) {
                    while (rs.next()) {
                        game = new GamesBean.Builder(rs.getInt(1), tableName, sharedVariables.getNumOfMatch(tableName), CONST._MATCH_).build();
                        int n = tableName.getNumOfOpponents() + 1;
                        for (int i = 2; i <= n; i++) {
                            game.getRobots().add(new RobotGameBean.Builder(rs.getString(i)).build());
                        }
                        result.add(game);
                    }
                    if (result.isEmpty()) {
                        logger.log(Level.INFO, "{0} table empty ...", tableName);
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "getGamesFromDB {0}", e);
            sharedVariables.setRunnable(false);
            sharedVariables.setUnrecoverableError(true);
        }
        return result;
    }
}
