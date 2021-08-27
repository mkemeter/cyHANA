package org.kemeter.cytoscape.internal.hdb;

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

    /**
     *
     */
    public boolean primaryKey;

    public HanaColumnInfo(String schema, String table, String name) {
        this(schema, table, name, false);
    }

    public HanaColumnInfo(String schema, String table, String name, boolean primaryKey){
        this.schema = schema;
        this.table = table;
        this.name = name;
        this.primaryKey = primaryKey;
    }
}
