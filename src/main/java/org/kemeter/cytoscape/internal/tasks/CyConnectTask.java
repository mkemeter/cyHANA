package org.kemeter.cytoscape.internal.tasks;

import org.cytoscape.work.*;
import org.kemeter.cytoscape.internal.hdb.HanaConnectionManager;

import java.io.*;
import java.sql.SQLException;

/**
 * The task establishes the connection to an SAP HANA instance
 */
public class CyConnectTask extends AbstractTask {

    @ContainsTunables
    public CyConnectTaskTunables tunables;

    /**
     * Connection manager for database communication
     */
    private final HanaConnectionManager connectionManager;

    /**
     * Constructor loads credentials from the file system that have been cached before.
     *
     * @param connectionManager HanaConnectionManager object
     */
    public CyConnectTask(
            HanaConnectionManager connectionManager
    ){
        this.connectionManager = connectionManager;
        this.tunables = new CyConnectTaskTunables();
    }

    /**
     * Establishes connection to an SAP HANA database. The credentials will be
     * cached on the file system.
     *
     * @param taskMonitor   TaskMonitor to report progress
     * @throws Exception    In case of errors
     */
    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {

        taskMonitor.setTitle("SAP HANA: Connect Database");
        taskMonitor.setProgress(0d);

        taskMonitor.setStatusMessage("Starting to establish connection to " + tunables.host);

        // save credentials to properties file
        try{
            tunables.saveToCacheFile();
        }catch(IOException e){
            taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Unable to cache login credentials");
            taskMonitor.showMessage(TaskMonitor.Level.ERROR, e.toString());
        }

        // establish connection
        try{
            connectionManager.connect(tunables.getHanaConnectionCredentials());
            taskMonitor.showMessage(TaskMonitor.Level.INFO, "Successfully connected to " + tunables.host);
        } catch (SQLException e){
            taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Could not establish connection to " + tunables.host);
            taskMonitor.showMessage(TaskMonitor.Level.ERROR, e.toString());
        }

        taskMonitor.setProgress(1d);
    }

}
