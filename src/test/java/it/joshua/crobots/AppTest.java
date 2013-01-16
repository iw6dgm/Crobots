package it.joshua.crobots;

import it.joshua.crobots.data.TableName;
import it.joshua.crobots.impl.DataSourceManager;
import it.joshua.crobots.impl.Manager;
import it.joshua.crobots.impl.SQLManagerFactory;
import java.sql.SQLException;
import java.util.logging.Level;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest
        extends TestCase {

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }
    
    private void testConnection(SharedVariables sharedVariables) throws SQLException {
        SQLManagerInterface mySQLManager = SQLManagerFactory.getInstance(TableName.F2F);
        DataSourceManager dataSourceManager = DataSourceManager.getDataSourceManager();
        mySQLManager.setDataSourceManager(dataSourceManager);
        dataSourceManager.initialize();
        assert (mySQLManager.test(false));

        if (sharedVariables.isLocalDb()) {
            assert (mySQLManager.test(true));
        }
        dataSourceManager.closeAll();
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() throws SQLException {
        SharedVariables sharedVariables = SharedVariables.getInstance();

        assert (sharedVariables != null);
        
        testConnection(sharedVariables);
    }
}
