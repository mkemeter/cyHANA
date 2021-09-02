package org.kemeter.cytoscape.internal.hdb;

import java.sql.Types;

/**
 * Resource for type conversion:
 * https://help.sap.com/doc/saphelp_nw73ehp1/7.31.19/en-US/4b/05d4bc4bb82592e10000000a42189b/content.htm?no_cache=true
 *
 */
public class HanaDataType {

    private int sqlDataType;

    public HanaDataType(int sqlDataType){
        this.sqlDataType = sqlDataType;
    }

    public<T> HanaDataType(T classType){
        this.sqlDataType = convertJavaToSqlType(classType);
    }

    public int getSqlDataType(){
        return this.sqlDataType;
    }

    public Class getJavaCytoDataType(){
        return convertSqlToJavaCytoType(this.sqlDataType);
    }

    public Class convertSqlToJavaCytoType(int sqlType){
        // data types supported in cytoscape tables:
        // Boolean, String, Integer, Long, Double
        // List<Boolean>, List<String>, List<Integer>, List<Long>, List<Double>

        switch (sqlType){
            case Types.BOOLEAN:
                return Boolean.class;
            case Types.SMALLINT:
            case Types.INTEGER:
                return Integer.class;
            case Types.BIGINT:
                return Long.class;
            case Types.REAL:
            case Types.DOUBLE:
                return Double.class;
            default:
                return String.class;
        }
    }

    public static<T> int convertJavaToSqlType(T classType) {

        if (classType.equals(String.class)) {
            return Types.NVARCHAR;
        }
        if (classType.equals(Integer.class)) {
            return Types.INTEGER;
        }
        if (classType.equals(Long.class)) {
            return Types.BIGINT;
        }
        if (classType.equals(Double.class)) {
            return Types.DOUBLE;
        }
        if(classType.equals(Boolean.class)) {
            return Types.BOOLEAN;
        }

        return Types.OTHER;

    }
}
