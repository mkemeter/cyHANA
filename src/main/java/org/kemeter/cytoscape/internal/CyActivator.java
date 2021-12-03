package org.kemeter.cytoscape.internal;

import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.GUITunableHandlerFactory;
import org.cytoscape.work.swing.SimpleGUITunableHandlerFactory;
import org.kemeter.cytoscape.internal.tasks.CyCreateWorkspaceTaskFactory;
import org.kemeter.cytoscape.internal.tasks.CyLoadTaskFactory;
import org.kemeter.cytoscape.internal.tasks.CyRefreshTaskFactory;
import org.kemeter.cytoscape.internal.tunables.PasswordString;
import org.kemeter.cytoscape.internal.tunables.PasswordStringGUIHandler;
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

        // register custom password tunable
        SimpleGUITunableHandlerFactory<PasswordStringGUIHandler> passwordHandlerFactory = new SimpleGUITunableHandlerFactory<>(
                PasswordStringGUIHandler.class, PasswordString.class);
        registerService(bc, passwordHandlerFactory, GUITunableHandlerFactory.class);

        try {
            HanaConnectionManager connectionManager = new HanaConnectionManager();

            // connect
            CyConnectTaskFactory connectFactory = new CyConnectTaskFactory(connectionManager);
            Properties connectProps = new Properties();
            connectProps.setProperty(ServiceProperties.PREFERRED_MENU, "Apps.SAP HANA");
            connectProps.setProperty(ServiceProperties.TITLE, "Connect to Database");
            connectProps.setProperty(ServiceProperties.MENU_GRAVITY, "1.0");
            registerService(bc, connectFactory, TaskFactory.class, connectProps);

            // create graph workspace from network
            CyCreateWorkspaceTaskFactory createFactory = new CyCreateWorkspaceTaskFactory(networkManager, connectionManager);
            Properties createProps = new Properties();
            createProps.setProperty(ServiceProperties.PREFERRED_MENU, "Apps.SAP HANA");
            createProps.setProperty(ServiceProperties.TITLE, "Create Graph Workspace from Current Network");
            createProps.setProperty(ServiceProperties.MENU_GRAVITY, "2.0");
            registerService(bc, createFactory, TaskFactory.class, createProps);

            // load graph workspace
            CyLoadTaskFactory loadFactory = new CyLoadTaskFactory(networkFactory, networkManager, connectionManager);
            Properties loadProps = new Properties();
            loadProps.setProperty(ServiceProperties.PREFERRED_MENU, "Apps.SAP HANA");
            loadProps.setProperty(ServiceProperties.TITLE, "Load Graph Workspace from Database");
            loadProps.setProperty(ServiceProperties.MENU_GRAVITY, "3.0");
            registerService(bc, loadFactory, TaskFactory.class, loadProps);

            // refresh current network from SAP HANA
            /*
            CyRefreshTaskFactory refreshFactory = new CyRefreshTaskFactory(networkFactory, networkManager, connectionManager);
            Properties refreshProps = new Properties();
            refreshProps.setProperty(ServiceProperties.PREFERRED_MENU, "Apps.SAP HANA");
            refreshProps.setProperty(ServiceProperties.TITLE, "Refresh Current Network from Database");
            refreshProps.setProperty(ServiceProperties.MENU_GRAVITY, "4.0");
            registerService(bc, refreshFactory, TaskFactory.class, refreshProps);
            */

            // load result of openCypher query

            // load single node (for later exploration via context menu)

            // TODO handle heterogeneous graphs
        } catch (Exception e){
            System.err.println("Failed to activate cyHANA plugin");
            System.err.println(e);
        }
    }

}
