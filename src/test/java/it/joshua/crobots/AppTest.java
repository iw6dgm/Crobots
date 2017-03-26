package it.joshua.crobots;

import it.joshua.crobots.data.TableName;
import it.joshua.crobots.impl.DataSourceManager;
import it.joshua.crobots.impl.SQLManagerFactory;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.SQLException;


public class AppTest {
    
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

    @Test
    @Ignore("fix me")
    public void testApp() throws SQLException {
        SharedVariables sharedVariables = SharedVariables.getInstance();

        assert (sharedVariables != null);
        
        testConnection(sharedVariables);
    }
}
