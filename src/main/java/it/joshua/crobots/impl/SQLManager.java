package it.joshua.crobots.impl;

/*
 * Created on 13-lug-2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
/**
 * @author mcamangi
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
import it.joshua.crobots.SQLManagerInterface;
import it.joshua.crobots.SharedVariables;
import it.joshua.crobots.bean.GamesBean;
import it.joshua.crobots.bean.RobotGameBean;
import it.joshua.crobots.data.TableName;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;

public class SQLManager implements SQLManagerInterface {

    private static final Logger logger = Logger.getLogger(SQLManager.class.getName());
    protected static DataSourceManager dataSourceManager = DataSourceManager.getDataSourceManager();
    //private CallableStatement cs2  = null;
    protected CallableStatement callableStatement = null;
    protected Connection remoteC = null;
    protected StringBuilder sqlRecovery, sqlUpdateResults;
    protected TableName tableName;
    protected static SharedVariables sharedVariables = SharedVariables.getInstance();

    protected SQLManager(TableName tableName) {
        this.tableName = tableName;
        sqlRecovery = new StringBuilder("{CALL pRecovery" + tableName.getTableName() + "(");
        sqlUpdateResults = new StringBuilder("{CALL pUpdate" + tableName.getTableName() + "Results(?, ");
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

    public static void initialize() {
        dataSourceManager.initialize();
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

    private static void closeConnection(DataSource ds) throws Exception {
        BasicDataSource bds = (BasicDataSource) ds;
        bds.close();
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
    public void initializeRobots(Vector<String> robots) {
        PreparedStatement ps = null;
        Connection c = null;
        String sql = "INSERT INTO robots(name) VALUES(?)";
        int ok = 0, failure = 0;
        logger.info("Initialize locally robots ...");
        try {
            c = getConnection(true);
            ps = c.prepareStatement(sql);
            for (String robot : robots) {
                ps.clearParameters();
                ps.setString(1, robot);
                try {
                    ps.executeUpdate();
                    ok++;
                } catch (SQLException e) {
                    logger.warning("Name = " + robot + " - " + e.getMessage());
                    failure++;
                }
                if (!dataSourceManager.getLocalDataSource().getDefaultAutoCommit()) {
                    c.commit();
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "SQLManager {0}", e);
        } finally {
            close(ps);
            close(c);
        }
        logger.info("Initialize OK(s)=" + ok + "; Failure(s)=" + failure);
    }

    @Override
    public void setupTable() {
        CallableStatement cs = null;
        Connection c = null;
        logger.info("Setting Up table " + tableName + " ...");
        try {
            c = getConnection(sharedVariables.isLocalDb());
            cs = c.prepareCall("{CALL pSetup" + tableName.getTableName().toUpperCase() + "()}");
            cs.execute();
            if (!sharedVariables.isLocalDb() && !sharedVariables.isRemoteAutocommit()) {
                c.commit();
            }
        } catch (Exception e) {
            if (!sharedVariables.isLocalDb() && !sharedVariables.isRemoteAutocommit()) {
                try {
                    c.rollback();
                } catch (SQLException se) {
                }
            }
            logger.log(Level.SEVERE,"SQLManager {0}", e);
        } finally {
            close(cs);
            close(c);
        }
    }

    @Override
    public void setupResults() {
        CallableStatement cs = null;
        Connection c = null;
        logger.info("Setting up results " + tableName + " ...");
        try {
            c = getConnection(false);
            cs = c.prepareCall("{CALL pSetupResults" + tableName.getTableName().toUpperCase() + "()}");
            cs.execute();
        } catch (Exception e) {
            logger.log(Level.SEVERE,"SQLManager {0}", e);
        } finally {
            close(cs);
            close(c);
        }
    }

    @Override
    public void setupRobots(Vector<String> robots, boolean localDb) {
        CallableStatement cs = null;
        Connection c = null;
        boolean autoCommit = false;
        logger.info("Initialize robots " + ((localDb) ? "locally" : "remotely") + " ...");
        try {
            c = getConnection(localDb);
            if (localDb) {
                autoCommit = sharedVariables.isLocalAutocommit();
            } else {
                autoCommit = sharedVariables.isRemoteAutocommit();
            }
            c.setAutoCommit(autoCommit);
            cs = c.prepareCall("{CALL pCleanUpRobots()}");
            cs.execute();

            cs = c.prepareCall("{CALL pInitializeRobot(?)}");
            for (String robot : robots) {
                cs.clearParameters();
                cs.setString(1, robot);
                cs.execute();
            }
            if (!autoCommit) {
                c.commit();
            }
        } catch (Exception e) {
            if (!autoCommit) {
                try {
                    c.rollback();
                } catch (SQLException se) {
                }
            }
            logger.log(Level.SEVERE,"SQLManager {0}", e);
        } finally {
            close(cs);
            close(c);
        }
    }

    @Override
    public void setupParameters(int param) {
        PreparedStatement ps = null;
        Connection c = null;
        String sql = "UPDATE parameters SET " + tableName + "=? WHERE id=1";
        logger.info("Setting Up " + tableName + " parameter to " + param + " ...");
        try {
            c = getConnection(false);
            ps = c.prepareStatement(sql);
            ps.setInt(1, param);
            ps.executeUpdate();
            if (!dataSourceManager.getRemoteDataSource().getDefaultAutoCommit()) {
                c.commit();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE,"SQLManager {0}", e);
        } finally {
            close(ps);
            close(c);
        }
    }

    @Override
    public boolean test(boolean localDb) {
        boolean result = true;

        String sql = "{CALL pTest(?)}";
        CallableStatement cs = null;
        Connection c = null;
        try {
            c = getConnection(localDb);
            cs = c.prepareCall(sql);
            cs.registerOutParameter(1, Types.TIMESTAMP);
            cs.execute();
            logger.info("Test at " + cs.getTimestamp(1));
        } catch (Exception e) {
            logger.log(Level.SEVERE,"SQLManager {0}", e);
            result = false;
        } finally {
            close(cs);
            close(c);
        }
        return result;
    }

    @Override
    public boolean initializeUpdates() {
        boolean result = true;
        logger.fine("Start initialize Callables...");
        try {
            remoteC = getConnection(false);
            remoteC.setAutoCommit(sharedVariables.isRemoteAutocommit());
            callableStatement = remoteC.prepareCall(sqlUpdateResults.toString());
        } catch (Exception e) {
            logger.log(Level.SEVERE,"SQLManager {0}", e);
            result = false;
        }
        logger.fine("Initialize completed...");
        return result;
    }

    @Override
    public void releaseUpdates() {
        //close(cs2);
        close(callableStatement);
        //close(localC);
        close(remoteC);
        logger.fine("Release completed...");
    }

    public static void closeAll() {
        try {
            closeConnection(dataSourceManager.getLocalDataSource());
            closeConnection(dataSourceManager.getRemoteDataSource());
        } catch (Exception e) {
            //logger.log(Level.SEVERE,"SQLManager {0}",e); botta silente
        }
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
        CallableStatement cs4 = null;
        Connection c = null;
        boolean autoCommit = false;
        try {
            c = getConnection(sharedVariables.isLocalDb());
            if (sharedVariables.isLocalDb()) {
                autoCommit = sharedVariables.isLocalAutocommit();
            } else {
                autoCommit = sharedVariables.isRemoteAutocommit();
            }
            c.setAutoCommit(autoCommit);
            cs4 = c.prepareCall(sqlRecovery.toString());
            List<RobotGameBean> robots = bean.getRobots();
            for (int i = 1; i <= tableName.getNumOfOpponents(); i++) {
                cs4.setString(i, robots.get(i - 1).getRobot());
            }
            cs4.executeUpdate();
            if (!autoCommit) {
                c.commit();
            }
        } catch (Exception e) {
            if (!autoCommit) {
                try {
                    c.rollback();
                } catch (SQLException se) {
                }
            }
            logger.log(Level.SEVERE,"SQLManager {0}", e);
        } finally {
            close(cs4);
            close(c);
        }
    }

    @Override
    public boolean updateResults(GamesBean bean) {
        boolean result = true;
        try {
            int robotIndex = 1;

            if (callableStatement == null || callableStatement.isClosed()) {
                initializeUpdates();
            }

            callableStatement.clearParameters();
            callableStatement.setInt(1, bean.getGames());
            for (RobotGameBean b : bean.getRobots()) {
                callableStatement.setString(1 + robotIndex, b.getRobot());
                callableStatement.setInt(2 + robotIndex, b.getWin());
                callableStatement.setInt(3 + robotIndex, b.getTie());
                callableStatement.setInt(4 + robotIndex, b.getPoints());
                robotIndex += 4;
            }
            callableStatement.executeUpdate();
            if (!sharedVariables.isRemoteAutocommit()) {
                remoteC.commit();
            }
        } catch (Exception e) {
            if (!sharedVariables.isRemoteAutocommit()) {
                try {
                    remoteC.rollback();
                } catch (SQLException se) {
                }
            }
            logger.log(Level.SEVERE,"SQLManager {0}", e);
            result = false;
        }
        return result;
    }

    @Override
    public List<GamesBean> getGames() {
        Connection c = null;
        CallableStatement cs = null;
        List<GamesBean> result = new ArrayList<>();
        String sql = "{CALL pSelect" + tableName + "(?)}";
        ResultSet rs = null;
        GamesBean game;
        boolean autoCommit = false;
        try {
            c = getConnection(sharedVariables.isLocalDb());
            if (sharedVariables.isLocalDb()) {
                autoCommit = sharedVariables.isLocalAutocommit();
            } else {
                autoCommit = sharedVariables.isRemoteAutocommit();
            }
            c.setAutoCommit(autoCommit);
            cs = c.prepareCall(sql);
            cs.setInt(1, sharedVariables.getBufferMinSize());
            cs.execute();
            if (!autoCommit) {
                c.commit();
            }
            rs = cs.getResultSet();
            while (rs.next()) {
                game = new GamesBean.Builder(rs.getInt(1), tableName, sharedVariables.getNumOfMatch(tableName), "match").build();
                int n = tableName.getNumOfOpponents() + 1;
                for (int i = 2; i <= n; i++) {
                    game.getRobots().add(RobotGameBean.create(rs.getString(i)));
                }
                result.add(game);
            }
            if (result.isEmpty()) {
                logger.info(tableName + " table empty ...");
            }
        } catch (Exception e) {
            if (!autoCommit) {
                try {
                    c.rollback();
                } catch (SQLException se) {
                }
            }
            logger.log(Level.SEVERE,"SQLManager {0}", e);
        } finally {
            close(rs);
            close(cs);
            close(c);
        }
        return result;
    }
}
