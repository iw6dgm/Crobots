/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.joshua.crobots.data;

/**
 *
 * @author joshua
 */
public enum TableName {

    F2F("f2f", 2), VS3("3vs3", 3), VS4("4vs4", 4);
    private String tableName;
    private int numOfOpponents;

    private TableName(String tablename, int numOfOpponents) {
        this.tableName = tablename;
        this.numOfOpponents = numOfOpponents;
    }

    public String getTableName() {
        return tableName;
    }

    public int getNumOfOpponents() {
        return numOfOpponents;
    }
}
