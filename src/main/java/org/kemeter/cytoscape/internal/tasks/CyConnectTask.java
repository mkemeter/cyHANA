package org.kemeter.cytoscape.internal.tasks;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.kemeter.cytoscape.internal.hdb.HanaConnectionManager;
import org.kemeter.cytoscape.internal.utils.IOUtils;
import org.kemeter.cytoscape.internal.hdb.HanaConnectionCredentials;

import java.io.*;
import java.sql.SQLException;

/**
 * The task establishes the connection to an SAP HANA instance
 */
public class CyConnectTask extends AbstractTask {

    /**
     *
     * @return Title of the input parameter dialog
     */
    @ProvidesTitle
    public String getTitle() { return "Connect to SAP HANA instance"; }

    /**
     * host address
     */
    @Tunable(description="Host", groups={"SAP HANA Database"}, required = true, gravity = 1)
    public String host;

    /**
     * Port number (e.g. 443 for SAP HANA Cloud)
     */
    @Tunable(description="Port", groups={"SAP HANA Database"}, required = true, gravity = 2)
    public String port;

    /**
     * Database username
     */
    @Tunable(description="Username", groups={"User Credentials"}, required = true, gravity = 3)
    public String username;

    /**
     * User password
     *
     * TODO can we have a masked input field?
     */
    @Tunable(description="Password", groups={"User Credentials"}, required = true, gravity = 4)
    public String password;

    /**
     * Checkbox if password shall be stored in an unsecure way
     */
    @Tunable(description="Save Password (plain text)", gravity = 5)
    public boolean savePassword;

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
        try{
            HanaConnectionCredentials cachedCredentials = IOUtils.loadCredentials();

            this.host = cachedCredentials.host;
            this.port = cachedCredentials.port;
            this.username = cachedCredentials.username;
            this.password = cachedCredentials.password;

            // assume that the user still wants to store the password, if this
            // has been done before
            this.savePassword = this.password.length() > 0;

        }catch (IOException e){
            // file was probably not yet existing
        }
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

        taskMonitor.setTitle("SAP HANA Connection");
        taskMonitor.setProgress(0d);

        HanaConnectionCredentials cred = new HanaConnectionCredentials(
                this.host, this.port, this.username, this.password
        );

        // save credentials to properties file
        try{
            IOUtils.cacheCredentials(cred, this.savePassword);
        }catch(IOException e){
            taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Unable to cache login credentials");
            taskMonitor.showMessage(TaskMonitor.Level.ERROR, e.toString());
        }

        // establish connection
        try{
            connectionManager.connect(cred);
            taskMonitor.showMessage(TaskMonitor.Level.INFO, "Successfully connected to " + this.host);
        } catch (SQLException e){
            taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Could not establish connection to " + this.host);
            taskMonitor.showMessage(TaskMonitor.Level.ERROR, e.toString());
        }

        taskMonitor.setProgress(1d);
    }

}
