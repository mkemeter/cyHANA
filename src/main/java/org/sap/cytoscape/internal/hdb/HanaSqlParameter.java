package org.sap.cytoscape.internal.hdb;

/**
 * Describes a generic SQL parameter
 */
public class HanaSqlParameter{
    /**
     * Value of the parameter
     */
    public Object parameterValue;
    /**
     * SQL Type of the parameter
     * (see java.sql.Types)
     */
    public int parameterType;

    public HanaSqlParameter(Object parameterValue, int parameterType){
        this.parameterValue = parameterValue;
        this.parameterType = parameterType;
    }
}
