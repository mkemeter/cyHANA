package org.kemeter.cytoscape.internal.hdb;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a graph workspace in SAP HANA
 */
public class HanaGraphWorkspace{
    /**
     * Content of the edge table
     */
    public List<HanaEdgeTableRow> edgeTable;

    /**
     * Content of the node table
     */
    public List<HanaNodeTableRow> nodeTable;

    /**
     * Schema and name of the graph workspace itself
     */
    public HanaDbObject workspaceDbObject;

    /**
     * Key column of the node table
     */
    public HanaColumnInfo nodeKeyCol;

    /**
     * Key column of the edge table
     */
    public HanaColumnInfo edgeKeyCol;

    /**
     * Source column of the edge table
     */
    public HanaColumnInfo edgeSourceCol;

    /**
     * Target column of the edge table
     */
    public HanaColumnInfo edgeTargetCol;

    /**
     * Attribute columns of the edge table
     */
    public ArrayList<HanaColumnInfo> edgeAttributeCols;

    /**
     * Attribute columns of the node table
     */
    public ArrayList<HanaColumnInfo> nodeAttributeCols;

    /**
     * Constructs empty HanaGraphWorkspace
     */
    public HanaGraphWorkspace(){
        this.edgeAttributeCols = new ArrayList<>();
        this.nodeAttributeCols = new ArrayList<>();
    }

    /**
     * Checks if metadata is complete
     * (i.e. table contents can be loaded given the existing metadata)
     *
     * @return true, if workspace content can be loaded given the existing metadata
     */
    public boolean isMetadataComplete(){

        if(workspaceDbObject == null) return false;
        if(workspaceDbObject.schema == null || workspaceDbObject.schema.length() == 0) return false;
        if(workspaceDbObject.name == null || workspaceDbObject.name.length() == 0) return false;
        if(nodeKeyCol == null) return false;
        if(edgeKeyCol == null) return false;
        if(edgeSourceCol == null) return false;
        if(edgeTargetCol == null) return false;

        return true;
    }

    /**
     *
     * @return Names of the node attribute columns
     */
    public String[] getNodeAttributeNames(){
        String[] attributeNames = new String[this.nodeAttributeCols.size()];
        for(int i=0; i< attributeNames.length; i++){
            attributeNames[i] = nodeAttributeCols.get(i).name;
        }
        return attributeNames;
    }

    /**
     *
     * @return Names of the edge attribute columns
     */
    public String[] getEdgeAttributeNames(){
        String[] attributeNames = new String[this.edgeAttributeCols.size()];
        for(int i=0; i< attributeNames.length; i++){
            attributeNames[i] = edgeAttributeCols.get(i).name;
        }
        return attributeNames;
    }
}
