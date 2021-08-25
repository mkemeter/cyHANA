package org.kemeter.cytoscape.internal.hdb;

import java.util.HashMap;

/**
 * Represents a record in a node table on SAP HANA
 */
public class HanaNodeTableRow{
    /**
     * Key value
     */
    public String key;

    /**
     * Attribute values mapped by column name
     */
    public HashMap<String, Object> attributeValues;

    /**
     *  Default constructor
     *
     * @param key   Initial key
     */
    public HanaNodeTableRow(String key){
        this.key = key;
        this.attributeValues = new HashMap<>();
    }

    /**
     * Retrieves the attribute value as a String
     *
     * @param attributeName Name of the attribute
     * @return Attribute value as a String; Null if conversion to String is not possible
     */
    public String getStringAttribute(String attributeName){
        if(!this.attributeValues.containsKey(attributeName)){
            return null;
        }

        Object value = this.attributeValues.get(attributeName);

        try{
            return value.toString();
        }catch (Exception e){
            return null;
        }
    }
}
