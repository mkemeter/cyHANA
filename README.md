# Cytoscape Plugin for SAP HANA
The Cytoscape Plugin for SAP HANA (cyHANA) is an app to connect Cytoscape to SAP HANA Graph. The current preliminary version has a rather reduced feature scope. If time allows, the scope and matureness of the plugin may improve in future.

> Please note, that although I am associated with SAP this is **not** an official plugin by SAP, but rather a spare time project.

The plugin is tested to work with [Cytoscape 3.8.2](https://cytoscape.org/), [SapMachine 11](https://sap.github.io/SapMachine/) and [NGDBC 2.9.16](https://tools.hana.ondemand.com/#hanatools).

## Functionalities
The current feature scope comprises:
- Establish connection to SAP HANA (Cloud)
- Load all nodes and edges of an existing (homogeneous) graph workspace

## Installation
After getting the latest JAR package, you can install the plugin using the [Cytoscape App Manager](http://manual.cytoscape.org/en/stable/App_Manager.html). 

The plugin makes use of the SAP HANA JDBC client, which additionally needs to be included on the classpath. The latest version can be retrieved from the [SAP Development Tools](https://tools.hana.ondemand.com/#hanatools) (look for ngdcb-latest.jar).

On my Mac, it did the job to copy `ngdbc-latest.jar` to `/Applications/Cytoscape_v3.8.2/framework/lib/openjfx/mac`. However, there may be more suitable ways to include the driver. As a Java amateur, I'd be happy to get to know them.

After installation, you should be able to find the plugin under `Apps` > `SAP HANA`.