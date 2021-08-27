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
    @Tunable(description="Schema/Name", groups = {"Graph Workspace"}, required = true, params="lookup=contains")
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

    /**
     * Loads all edges and nodes from the select graph workspace on SAP HANA
     *
     * @param taskMonitor   TaskMonitor to report progress
     * @throws Exception    In case of errors
     */
    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {

        taskMonitor.setTitle("SAP HANA Network Creation");
        taskMonitor.setProgress(0d);

        if(!this.connectionManager.isConnected()){
            // user has not yet executed the connect task
            // TODO should we execute it on behalf of the user? is that even possible?
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
        String[] nodeAttributeNames = graphWorkspace.getNodeAttributeNames();
        for(String attName : nodeAttributeNames){
            newNetwork.getDefaultNodeTable().createColumn("HANA_" + attName, String.class, false);
        }

        // create edge attributes
        String[] edgeAttributeNames = graphWorkspace.getEdgeAttributeNames();
        for(String attName : edgeAttributeNames){
            newNetwork.getDefaultEdgeTable().createColumn("HANA_" + attName, String.class, false);
        }

        // measure progress based on number of nodes and edges
        int nGraphObjects = graphWorkspace.edgeTable.size() + graphWorkspace.nodeTable.size();
        int progress = 0;

        taskMonitor.setStatusMessage("Creating nodes");

        // create nodes
        HashMap<String, CyNode> nodesByHanaKey = new HashMap<>();
        for(HanaNodeTableRow row : graphWorkspace.nodeTable){
            CyNode newNode = newNetwork.addNode();
            CyRow newRow = newNetwork.getDefaultNodeTable().getRow(newNode.getSUID());
            newRow.set("name", row.key);
            for(String attName : nodeAttributeNames){
                newRow.set("HANA_" + attName, row.getStringAttribute(attName));
            }
            nodesByHanaKey.put(row.key, newNode);

            taskMonitor.setProgress(progress++ / (double)nGraphObjects);
        }

        taskMonitor.setStatusMessage("Creating edges");

        // create edges
        for(HanaEdgeTableRow row: graphWorkspace.edgeTable){
            CyNode sourceNode = nodesByHanaKey.get(row.source);
            String sourceNodeName = newNetwork.getDefaultNodeTable().getRow(sourceNode.getSUID()).get("name", String.class);
            CyNode targetNode = nodesByHanaKey.get(row.target);
            String targetNodeName = newNetwork.getDefaultNodeTable().getRow(targetNode.getSUID()).get("name", String.class);

            CyEdge newEdge = newNetwork.addEdge(sourceNode, targetNode, true);
            CyRow newRow = newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID());

            newRow.set("name", sourceNodeName + " -> " + targetNodeName);
            for(String attName : edgeAttributeNames){
                newRow.set("HANA_" + attName, row.getStringAttribute(attName));
            }

            taskMonitor.setProgress(progress++ / (double)nGraphObjects);
        }

        networkManager.addNetwork(newNetwork);

        taskMonitor.setProgress(1d);
        taskMonitor.setStatusMessage("Finished creating network from Graph Workspace in SAP HANA");
    }
}
