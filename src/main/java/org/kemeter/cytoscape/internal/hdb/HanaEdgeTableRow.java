package org.kemeter.cytoscape.internal.hdb;

import java.util.HashMap;

/**
 * Represents a record in an edge table on SAP HANA
 */
public class HanaEdgeTableRow extends AbstractHanaGraphTableRow{

    private String keyFieldName;

    private String sourceFieldName;

    private String targetFieldName;

    public HanaEdgeTableRow(){
        super();
    }

    public void setKeyFieldName(String keyFieldName) {
        this.keyFieldName = keyFieldName;
    }

    public void setSourceFieldName(String sourceFieldName) {
        this.sourceFieldName = sourceFieldName;
    }

    public void setTargetFieldName(String targetFieldName) {
        this.targetFieldName = targetFieldName;
    }

    public String getKeyValue(){
        return this.getFieldValueToString(this.keyFieldName);
    }

    public String getSourceValue(){
        return this.getFieldValueToString(this.sourceFieldName);
    }

    public String getTargetValue(){
        return this.getFieldValueToString(this.targetFieldName);
    }
}
