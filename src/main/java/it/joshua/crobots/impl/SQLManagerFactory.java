/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.joshua.crobots.impl;

import it.joshua.crobots.SQLManagerInterface;
import it.joshua.crobots.SharedVariables;
import it.joshua.crobots.data.TableName;

/**
 *
 * @author joshua
 */
public class SQLManagerFactory {

    private static SharedVariables sharedVariables = SharedVariables.getInstance();

    public static SQLManagerInterface getInstance(TableName tableName) {
        if (sharedVariables.isLocalDb()) {
            if (sharedVariables.getLocalDriver().contains("OracleDriver")) {
                return SQLManagerOracle.getInstance(tableName);
            } else if (sharedVariables.getLocalDriver().contains("derby")) {
                return SQLManagerDerby.getInstance(tableName);
            }
        }
        return SQLManager.getInstance(tableName);
    }
}
