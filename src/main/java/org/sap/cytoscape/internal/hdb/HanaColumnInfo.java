package org.sap.cytoscape.internal.hdb;

/**
 * Describes a column of an SAP HANA database
 */
public class HanaColumnInfo{
    /**
     * Schema name
     */
    public String schema;

    /**
     * Table name
     */
    public String table;

    /**
     * Column name
     */
    public String name;

    public HanaColumnInfo(String schema, String table, String name){
        this.schema = schema;
        this.table = table;
        this.name = name;
    }
}
