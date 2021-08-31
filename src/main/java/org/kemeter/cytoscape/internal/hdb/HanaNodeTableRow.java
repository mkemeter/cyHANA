package org.kemeter.cytoscape.internal.hdb;

import java.util.HashMap;

/**
 * Represents a record in a node table on SAP HANA
 */
public class HanaNodeTableRow extends AbstractHanaGraphTableRow{

    private String keyFieldName;

    public HanaNodeTableRow(){
        super();
    }

    public void setKeyFieldName(String keyFieldName){
        this.keyFieldName = keyFieldName;
    }

    public String getKeyValue(){
        return this.getFieldValueToString(this.keyFieldName);
    }
}
