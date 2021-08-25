package org.sap.cytoscape.internal.hdb;

import java.util.HashMap;

/**
 * Represents a record in an edge table on SAP HANA
 */
public class HanaEdgeTableRow{

    /**
     * Key value
     */
    public String key;

    /**
     * Source value
     */
    public String source;

    /**
     * Target value
     */
    public String target;

    /**
     * Attribute values mapped by column name
     */
    public HashMap<String, Object> attributeValues;

    /**
     * Default Constructor
     *
     * @param key       Initial key
     * @param source    Initial source
     * @param target    Initial target
     */
    public HanaEdgeTableRow(String key, String source, String target){
        this.key = key;
        this.source = source;
        this.target = target;
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
