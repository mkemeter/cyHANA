package org.kemeter.cytoscape.internal.tasks;

import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.kemeter.cytoscape.internal.hdb.HanaConnectionManager;

public class CyRefreshTask extends AbstractTask {

    @ContainsTunables
    public CyRefreshTaskTunables tunables;

    private final CyNetworkFactory networkFactory;
    private final CyNetworkManager networkManager;
    private final HanaConnectionManager connectionManager;

    public CyRefreshTask(
            CyNetworkFactory networkFactory,
            CyNetworkManager networkManager,
            HanaConnectionManager connectionManager
    ) {
        this.networkFactory = networkFactory;
        this.networkManager = networkManager;
        this.connectionManager = connectionManager;

        CyConnectTask.tryConnect(this.connectionManager);

        this.tunables = new CyRefreshTaskTunables();
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {

        taskMonitor.setTitle("SAP HANA: Refreshing Cytoscape Graph");
        taskMonitor.setProgress(0d);

        taskMonitor.setStatusMessage("Starting to refresh Cytoscape graph");

        if(!this.connectionManager.isConnected()){
            // user has not yet executed the connect task
            taskMonitor.showMessage(
                    TaskMonitor.Level.ERROR,
                    "Connection to SAP HANA has not been established. Please connect first."
            );
            taskMonitor.setProgress(1d);
            return;
        }


        // TODO


        taskMonitor.setProgress(1d);
        taskMonitor.setStatusMessage("Finished refreshing Cytoscape graph from SAP HANA");
    }
}
