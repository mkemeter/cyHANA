package org.kemeter.cytoscape.internal.utils;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.kemeter.cytoscape.internal.hdb.HanaColumnInfo;

import java.util.List;

public class CyUtils {

    /**
     *
     * @param cyTable
     * @param fieldList
     */
    public static void enhanceCyTableWithAttributes(CyTable cyTable, List<HanaColumnInfo> fieldList){
        for(HanaColumnInfo hanaCol : fieldList){
            CyColumn col = cyTable.getColumn(hanaCol.name);

            if(col == null) {
                // try to re-use columns, that are already existing. This might cause clashes with the Cytoscape
                // data model, but makes loading of networks, that have been created with Cytoscape, easier.
                cyTable.createColumn(hanaCol.name, hanaCol.dataType.getJavaCytoDataType(), false);
            }
        }
    }

    public static void enhanceCyNetworkWithDatabaseLinkInformation(CyTable cyNetworkTable, Long networkSuid, String sapHanaInstance, String sapHanaWorkspace){

        // add instance information
        CyColumn instanceCol = cyNetworkTable.getColumn("sap_hana_instance");
        if(instanceCol == null) {
            cyNetworkTable.createColumn("sap_hana_instance", String.class, false);
        }
        cyNetworkTable.getRow(networkSuid).set("sap_hana_instance", sapHanaInstance);

        // add workspace information
        CyColumn workspaceCol = cyNetworkTable.getColumn("sap_hana_workspace");
        if(workspaceCol == null) {
            cyNetworkTable.createColumn("sap_hana_workspace", String.class, false);
        }
        cyNetworkTable.getRow(networkSuid).set("sap_hana_workspace", sapHanaWorkspace);
    }
}
