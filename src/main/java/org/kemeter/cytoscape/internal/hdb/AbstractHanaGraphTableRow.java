package org.kemeter.cytoscape.internal.hdb;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractHanaGraphTableRow {

    private HashMap<String, Object> fieldValues;

    AbstractHanaGraphTableRow(){
        this.fieldValues = new HashMap<>();
    }

    public void addFieldValue(String columnName, Object value){
        this.fieldValues.put(columnName, value);
    }

    public void addFieldValues(Map<String, Object> newFieldValues){
        this.fieldValues.putAll(newFieldValues);
    }

    public Map<String, Object> getFieldValues(){
        return this.fieldValues;
    }

    public String getFieldValueToString(String fieldName){
        if(!this.fieldValues.containsKey(fieldName)){
            return null;
        }

        Object value = this.fieldValues.get(fieldName);

        try{
            return value.toString();
        }catch (Exception e){
            return null;
        }
    }

}
