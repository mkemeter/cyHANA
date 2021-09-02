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
    public HanaDataType dataType;

    /**
     *
     */
    public boolean primaryKey;

    /**
     *
     */
    public boolean notNull;

    public HanaColumnInfo(String schema, String table, String name, int sqlType) {
        this(schema, table, name, sqlType, false);
    }

    public HanaColumnInfo(String schema, String table, String name, int sqlType, boolean primaryKey){
        this(schema, table, name, sqlType, primaryKey, false);
    }

    public HanaColumnInfo(String schema, String table, String name, int sqlType, boolean primaryKey, boolean notNull){
        this.schema = schema;
        this.table = table;
        this.name = name;
        this.dataType = new HanaDataType(sqlType);
        this.primaryKey = primaryKey;
        this.notNull = notNull;
    }
}
