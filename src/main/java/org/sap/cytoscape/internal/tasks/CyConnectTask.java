package org.sap.cytoscape.internal.tasks;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.sap.cytoscape.internal.hdb.HanaConnectionCredentials;
import org.sap.cytoscape.internal.hdb.HanaConnectionManager;

import java.io.*;
import java.sql.SQLException;
import java.util.Properties;

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
     * The filename where credentials will be stored between sessions
     */
    private final String credentialCacheFile = "cyhana_cache.properties";

    /**
     * Stores current credentials to a properties file. Password will
     * only be stored, if the respective checkbox has been selected.
     */
    private void saveCredentials() throws IOException {
        Properties credProps = new Properties();
        credProps.setProperty("hdb.host", this.host);
        credProps.setProperty("hdb.port", this.port);
        credProps.setProperty("hdb.username", this.username);

        if (this.savePassword) {
            credProps.setProperty("hdb.password", this.password);
        } else {
            // overwrite previously saved passwords
            credProps.setProperty("hdb.password", "");
        }

        try (OutputStream output = new FileOutputStream(this.credentialCacheFile)){
            credProps.store(output, null);
        } catch(IOException e){
            System.err.println("Cannot store connection credentials");
            System.err.println(e);
            throw e;
        }
    }

    /**
     * Loads previously cached credentials from a properties file. If a password
     * has been saved before, it assumes that the user wants to do the same again
     * and pre-selects the checkbox to store passwords in plain text.
     */
    private void loadCredentials(){
        try (InputStream input = new FileInputStream(this.credentialCacheFile)) {
            // load cached credentials
            Properties credProps = new Properties();
            credProps.load(input);

            this.host = credProps.getProperty("hdb.host");
            this.port = credProps.getProperty("hdb.port");
            this.username = credProps.getProperty("hdb.username");
            this.password = credProps.getProperty("hdb.password");
            // assume that the user still wants to store the password, if this
            // has been done before
            this.savePassword = this.password.length() > 0;

        } catch (IOException e) {
            // this will happen at least on the first start and is likely
            // not an issue
            System.err.println("Cannot load cached connection credentials");
        }
    }

    /**
     * Constructor loads credentials from the file system that have been cached before.
     *
     * @param connectionManager HanaConnectionManager object
     */
    public CyConnectTask(
            HanaConnectionManager connectionManager
    ){
        this.connectionManager = connectionManager;
        loadCredentials();
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

        // save credentials to properties file
        try{
            saveCredentials();
        }catch(IOException e){
            taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Unable to cache login credentials");
            taskMonitor.showMessage(TaskMonitor.Level.ERROR, e.toString());
        }

        HanaConnectionCredentials cred = new HanaConnectionCredentials(
                this.host, this.port, this.username, this.password
        );

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
