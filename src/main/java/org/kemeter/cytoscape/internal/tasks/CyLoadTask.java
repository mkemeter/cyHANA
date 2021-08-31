package org.kemeter.cytoscape.internal.tasks;

import org.cytoscape.model.*;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
import org.kemeter.cytoscape.internal.hdb.*;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * This task loads all the nodes and edges of a given graph workspace on SAP HANA
 */
public class CyLoadTask extends AbstractTask {

    /**
     *
     * @return Title of the input parameter dialog
     */
    @ProvidesTitle
    public String getTitle() { return "Select Graph Workspace"; }

    /**
     * Dropdown box for selection of graph workspace. Will be pre-populated on the constructor.
     */
    @Tunable(description="Schema/Name", groups = {"Graph Workspace"}, required = true, params="lookup=contains", gravity=1)
    public ListSingleSelection<String> workspaceSelection;

    /**
     * Maps graph workspaces by their name in the tunable dropdown box
     */
    private HashMap<String, HanaDbObject> graphWorkspaces;

    private final CyNetworkFactory networkFactory;
    private final CyNetworkManager networkManager;
    private final HanaConnectionManager connectionManager;

    /**
     * Constructor uses the connectionManager to initially retrieve the list of available graph
     * workspaces on the system.
     *
     * @param networkFactory    Creation of networks
     * @param networkManager    Registering networks in the client
     * @param connectionManager Manage connection to SAP HANA
     */
    public CyLoadTask(
            CyNetworkFactory networkFactory,
            CyNetworkManager networkManager,
            HanaConnectionManager connectionManager
    ) {
        this.networkFactory = networkFactory;
        this.networkManager = networkManager;
        this.connectionManager = connectionManager;

        if(this.connectionManager.isConnected()){

            try {
                List<HanaDbObject> workspaceList = this.connectionManager.listGraphWorkspaces();
                graphWorkspaces = new HashMap<>();

                for (HanaDbObject ws : workspaceList) {
                    String namedItem = ws.schema + "." + ws.name;
                    graphWorkspaces.put(namedItem, ws);
                }
                // pre-populate available workspaces
                String[] wsArray = graphWorkspaces.keySet().toArray(new String[0]);
                Arrays.sort(wsArray);
                this.workspaceSelection = new ListSingleSelection(wsArray);
            } catch (SQLException e){
                this.workspaceSelection = new ListSingleSelection<>();
            }

        }else{
            // since there is no option, Cytoscape will skip the dialog and start the run method.
            this.workspaceSelection = new ListSingleSelection<>();
        }
    }

    public static void enhanceCyTableWithAttributes(CyTable cyTable, List<HanaColumnInfo> fieldList){
        for(HanaColumnInfo hanaCol : fieldList){
            CyColumn col = cyTable.getColumn(hanaCol.name);
            if(col == null) {
                // try to re-use columns, that are already existing. This might cause clashes with the Cytoscape
                // data model, but makes loading of networks, that have been created with Cytoscape, easier.
                cyTable.createColumn(hanaCol.name, String.class, false);
            }
        }
    }

    /**
     * Loads all edges and nodes from the select graph workspace on SAP HANA
     *
     * @param taskMonitor   TaskMonitor to report progress
     * @throws Exception    In case of errors
     */
    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {

        taskMonitor.setTitle("SAP HANA: Load Graph Workspace");
        taskMonitor.setProgress(0d);

        taskMonitor.setStatusMessage("Starting to load Graph Workspace");

        if(!this.connectionManager.isConnected()){
            // user has not yet executed the connect task
            taskMonitor.showMessage(
                    TaskMonitor.Level.ERROR,
                    "Connection to SAP HANA has not been established. Please connect first."
            );
            taskMonitor.setProgress(1d);
            return;
        }

        // retrieve selected graph workspace
        String selectedWorkspaceKey = workspaceSelection.getSelectedValue();
        HanaDbObject selectedWorkspace = graphWorkspaces.get(selectedWorkspaceKey);

        taskMonitor.setStatusMessage("Downloading data from Graph Workspace " + selectedWorkspaceKey + " in SAP HANA");

        // load data from SAP HANA
        HanaGraphWorkspace graphWorkspace =
                connectionManager.loadGraphWorkspace(selectedWorkspace);

        // start network creation in Cytoscape
        CyNetwork newNetwork = this.networkFactory.createNetwork();

        // visible name of the network in the client
        newNetwork.getDefaultNetworkTable().getRow(newNetwork.getSUID()).set("name", selectedWorkspaceKey);

        // create node attributes
        enhanceCyTableWithAttributes(newNetwork.getDefaultNodeTable(), graphWorkspace.getNodeFieldList());

        // create edge attributes
        enhanceCyTableWithAttributes(newNetwork.getDefaultEdgeTable(), graphWorkspace.getEdgeFieldList());

        // measure progress based on number of nodes and edges
        int nGraphObjects = graphWorkspace.getEdgeTable().size() + graphWorkspace.getNodeTable().size();
        int progress = 0;

        taskMonitor.setStatusMessage("Creating nodes");

        // create nodes
        HashMap<String, CyNode> nodesByHanaKey = new HashMap<>();
        for(HanaNodeTableRow row : graphWorkspace.getNodeTable()){
            CyNode newNode = newNetwork.addNode();
            CyRow newRow = newNetwork.getDefaultNodeTable().getRow(newNode.getSUID());
            newRow.set("name", row.getKeyValue());
            for(HanaColumnInfo field : graphWorkspace.getNodeFieldList()){
                newRow.set(field.name, row.getFieldValueToString(field.name));
            }
            nodesByHanaKey.put(row.getKeyValue(), newNode);

            taskMonitor.setProgress(progress++ / (double)nGraphObjects);
        }

        taskMonitor.setStatusMessage("Creating edges");

        // create edges
        for(HanaEdgeTableRow row: graphWorkspace.getEdgeTable()){
            CyNode sourceNode = nodesByHanaKey.get(row.getSourceValue());
            String sourceNodeName = newNetwork.getDefaultNodeTable().getRow(sourceNode.getSUID()).get("name", String.class);
            CyNode targetNode = nodesByHanaKey.get(row.getTargetValue());
            String targetNodeName = newNetwork.getDefaultNodeTable().getRow(targetNode.getSUID()).get("name", String.class);

            CyEdge newEdge = newNetwork.addEdge(sourceNode, targetNode, true);
            CyRow newRow = newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID());

            newRow.set("name", sourceNodeName + " -> " + targetNodeName);
            for(HanaColumnInfo field : graphWorkspace.getEdgeFieldList()){
                newRow.set(field.name, row.getFieldValueToString(field.name));
            }

            taskMonitor.setProgress(progress++ / (double)nGraphObjects);
        }

        networkManager.addNetwork(newNetwork);

        taskMonitor.setProgress(1d);
        taskMonitor.setStatusMessage("Finished creating network from Graph Workspace in SAP HANA");
    }
}
