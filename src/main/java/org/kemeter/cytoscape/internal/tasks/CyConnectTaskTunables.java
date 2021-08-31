package org.kemeter.cytoscape.internal.tasks;

import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.Tunable;
import org.kemeter.cytoscape.internal.hdb.HanaConnectionCredentials;
import org.kemeter.cytoscape.internal.hdb.HanaConnectionManager;
import org.kemeter.cytoscape.internal.tunables.PasswordString;
import org.kemeter.cytoscape.internal.utils.IOUtils;

import java.io.IOException;

public class CyConnectTaskTunables {
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
     */
    @Tunable(description="Password", groups={"User Credentials"}, required = true, gravity = 4)
    public PasswordString password;

    /**
     * Checkbox if password shall be stored in an unsecure way
     */
    @Tunable(description="Save Password (plain text)", gravity = 5)
    public boolean savePassword;

    /**
     *
     */
    public CyConnectTaskTunables(){
        try{
            HanaConnectionCredentials cachedCredentials = IOUtils.loadCredentials();

            this.host = cachedCredentials.host;
            this.port = cachedCredentials.port;
            this.username = cachedCredentials.username;
            this.password = new PasswordString(cachedCredentials.password);

            // assume that the user still wants to store the password, if this
            // has been done before
            this.savePassword = this.password.getPassword().length() > 0;

        }catch (IOException e){
            // file was probably not yet existing
        }
    }

    /**
     *
     * @return
     */
    public HanaConnectionCredentials getHanaConnectionCredentials(){
        return new HanaConnectionCredentials(
                this.host, this.port, this.username, this.password.getPassword()
        );
    }

    /**
     *
     * @throws IOException
     */
    public void saveToCacheFile() throws IOException {
        IOUtils.cacheCredentials(this.getHanaConnectionCredentials(), this.savePassword);
    }
}
