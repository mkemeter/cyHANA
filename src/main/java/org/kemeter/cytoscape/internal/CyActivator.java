package org.kemeter.cytoscape.internal;

import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.kemeter.cytoscape.internal.tasks.CyLoadTaskFactory;
import org.osgi.framework.BundleContext;
import org.kemeter.cytoscape.internal.hdb.HanaConnectionManager;
import org.kemeter.cytoscape.internal.tasks.CyConnectTaskFactory;

import java.util.Properties;

public class CyActivator extends AbstractCyActivator {

    public CyActivator(){
        super();
    }

    public void start(BundleContext bc) {
        // fetch api stuff
        CyNetworkFactory networkFactory = getService(bc, CyNetworkFactory.class);
        CyNetworkManager networkManager = getService(bc, CyNetworkManager.class);

        HanaConnectionManager connectionManager = new HanaConnectionManager();

        // connect
        CyConnectTaskFactory connectFactory = new CyConnectTaskFactory(connectionManager);
        Properties connectProps = new Properties();
        connectProps.setProperty(ServiceProperties.PREFERRED_MENU, "Apps.SAP HANA");
        connectProps.setProperty(ServiceProperties.TITLE, "Connect to Database");
        connectProps.setProperty(ServiceProperties.MENU_GRAVITY, "1.0");
        registerService(bc, connectFactory, TaskFactory.class, connectProps);

        // load graph workspace
        CyLoadTaskFactory loadFactory = new CyLoadTaskFactory(networkFactory, networkManager, connectionManager);
        Properties loadProps = new Properties();
        loadProps.setProperty(ServiceProperties.PREFERRED_MENU, "Apps.SAP HANA");
        loadProps.setProperty(ServiceProperties.TITLE, "Load Graph Workspace");
        loadProps.setProperty(ServiceProperties.MENU_GRAVITY, "2.0");
        registerService(bc, loadFactory, TaskFactory.class, loadProps);

        // load result of OpenCypher query

        // load single node (for later exploration via context menu)

        // TODO handle heterogeneous graphs
    }

}
