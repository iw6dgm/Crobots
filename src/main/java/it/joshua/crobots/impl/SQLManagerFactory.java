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
        if (sharedVariables.isLocalDb() && sharedVariables.getLocalDriver().contains("Oracle")) {
                return SQLManagerOracle.getInstance(tableName);
            } else {
                return SQLManager.getInstance(tableName);
            }
    }
}
