package org.kemeter.cytoscape.internal.tasks;

import org.cytoscape.model.*;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListChangeListener;
import org.cytoscape.work.util.ListSelection;
import org.cytoscape.work.util.ListSingleSelection;
import org.kemeter.cytoscape.internal.hdb.HanaConnectionManager;
import org.kemeter.cytoscape.internal.utils.CyNetworkKey;
import org.kemeter.cytoscape.internal.hdb.HanaGraphWorkspace;

import java.sql.SQLException;

public class CyCreateWorkspaceTask extends AbstractTask {

    /**
     * ChangeListener for proposing a workspace name based on the selected network
     */
    class ProposeNameListener implements  ListChangeListener<CyNetworkKey> {
        private CyCreateWorkspaceTask task;
        public ProposeNameListener(CyCreateWorkspaceTask task) {
            this.task = task;
        }
        @Override
        public void selectionChanged(ListSelection<CyNetworkKey> source) {
            ListChangeListener.super.selectionChanged(source);
            task.setWorkspaceName(((ListSingleSelection<CyNetworkKey>)source).getSelectedValue().getName());
        }
    }

    /**
     *
     * @return Title of the input parameter dialog
     */
    @ProvidesTitle
    public String getTitle() { return "Create Graph Workspace"; }

    @Tunable(description="Network", groups = {"Network Selection"}, required = true, gravity = 1)
    public ListSingleSelection<CyNetworkKey> networkSelection;

    @Tunable(description="Schema", groups={"New Graph Workspace"}, required = true, gravity = 2)
    public String schema;

    private String workspaceName;
    @Tunable(description="Name", groups={"New Graph Workspace"}, listenForChange="networkSelection", required = true, gravity = 3)
    public String getWorkspaceName(){ return this.workspaceName; }
    public void setWorkspaceName(String workspaceName){
        this.edgeTableName = workspaceName + "_EDGES";
        this.nodeTableName = workspaceName + "_NODES";
        this.workspaceName = workspaceName;
    }

    @Tunable(description="Create Schema", groups={"Options"}, gravity = 4)
    public boolean createSchema = true;

    @Tunable(description="Node Table", groups={"New Tables"}, gravity = 5, params="displayState=collapsed", listenForChange={"WorkspaceName","networkSelection"})
    public String nodeTableName;

    @Tunable(description="Edge Table", groups={"New Tables"}, gravity = 6, params="displayState=collapsed", listenForChange={"WorkspaceName","networkSelection"})
    public String edgeTableName;


    private final CyNetworkManager networkManager;
    private final HanaConnectionManager connectionManager;

    public CyCreateWorkspaceTask(CyNetworkManager networkManager, HanaConnectionManager connectionManager){
        this.networkManager = networkManager;
        this.connectionManager = connectionManager;

        try {
            this.schema = connectionManager.getCurrentSchema();
        } catch (SQLException e) {};

        CyNetworkKey[] networkKeys = new CyNetworkKey[this.networkManager.getNetworkSet().size()];
        int i=0;
        for(CyNetwork network : this.networkManager.getNetworkSet()){
            Long suid = network.getSUID();
            String name = network.getDefaultNetworkTable().getAllRows().get(0).get("name", String.class);
            networkKeys[i++] = new CyNetworkKey(suid, name);
        }

        this.networkSelection = new ListSingleSelection<>(networkKeys);
        this.networkSelection.addListener(new ProposeNameListener(this));
        this.setWorkspaceName(this.networkSelection.getSelectedValue().getName());
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        taskMonitor.setTitle("SAP HANA: Create Graph Workspace");
        taskMonitor.setProgress(0d);

        taskMonitor.setStatusMessage("Starting to create Graph Workspace " + this.schema + "." + this.workspaceName);

        if(!this.connectionManager.isConnected()){
            // user has not yet executed the connect task
            taskMonitor.showMessage(
                    TaskMonitor.Level.ERROR,
                    "Connection to SAP HANA has not been established. Please connect first."
            );
            taskMonitor.setProgress(1d);
            return;
        }

        // check/create schema
        if(!this.connectionManager.schemaExists(this.schema)){
            if(this.createSchema){
                this.connectionManager.createSchema(schema);
                taskMonitor.setStatusMessage("Schema " + this.schema + " has been created");
            }else{
                taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Schema " + this.schema + " does not exist.");
                taskMonitor.setProgress(1d);
                return;
            }
        }

        // retrieve selected network
        CyNetworkKey selectedNetworkKey = this.networkSelection.getSelectedValue();
        CyNetwork selectedNetwork = this.networkManager.getNetwork(selectedNetworkKey.getSUID());


        taskMonitor.setStatusMessage("Assembling Graph Workspace");
        HanaGraphWorkspace newWorkspace =
                new HanaGraphWorkspace(this.schema, this.workspaceName, this.nodeTableName, this.edgeTableName, selectedNetwork);

        if(!newWorkspace.isMetadataComplete()){
            taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Error assembling workspace. Metadata is not complete.");
            taskMonitor.setProgress(1d);
            return;
        }

        taskMonitor.setStatusMessage("Creating node table");
        // create nodes table
        this.connectionManager.createTable(
                newWorkspace.getNodeTableDbObject(),
                newWorkspace.getNodeFieldList()
        );

        taskMonitor.setStatusMessage("Uploading node records");
        //insert values
        this.connectionManager.bulkInsertData(
                newWorkspace.getNodeTableDbObject(),
                newWorkspace.getNodeFieldList(),
                newWorkspace.getNodeTableData()
        );

        taskMonitor.setStatusMessage("Creating edge table");
        // create edges table
        this.connectionManager.createTable(
                newWorkspace.getEdgeTableDbObject(),
                newWorkspace.getEdgeFieldList()
        );

        taskMonitor.setStatusMessage("Uploading edge records");
        //insert values
        this.connectionManager.bulkInsertData(
                newWorkspace.getEdgeTableDbObject(),
                newWorkspace.getEdgeFieldList(),
                newWorkspace.getEdgeTableData()
        );

        taskMonitor.setStatusMessage("Creating Graph Workspace");
        // create graph workspace
        this.connectionManager.createGraphWorkspace(newWorkspace);
    }
}
