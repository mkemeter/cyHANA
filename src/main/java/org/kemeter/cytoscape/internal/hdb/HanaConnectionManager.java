package org.kemeter.cytoscape.internal.hdb;

import org.kemeter.cytoscape.internal.utils.IOUtils;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Handles communication with the database and SAP HANA specific stuff
 */
public class HanaConnectionManager {

    private static Logger logger = LoggerFactory.getLogger("org.cytoscape.application.userlog");
    public static void debug(String msg){ logger.debug(" HANA "+msg); }
    public static void info(String msg){  logger.info( " HANA "+msg); }
    public static void warn(String msg){  logger.warn( " HANA "+msg); }
    public static void err(String msg){   logger.error(" HANA "+msg); }

    /**
     *  Helper function to handle the String representation of DB null values
     *
     * @param obj   Value of a database record
     * @return      String representation of the value; "DB_NULL" if value has been null
     */
    private static String toStrNull(Object obj){
        return obj == null ? "DB_NULL" : obj.toString();
    }


    private static String quoteIdentifier(String id){
        return '"'+id+'"';
    }
    /**
     * Method to parse a build string
     */
    public static boolean isCloudEdition(String buildStr){
        if (buildStr==null){
            return false;
        }
        return buildStr.contains("/CE");
    }

    /**
     * Internal connection object
     */
    private Connection connection;

    /**
     * Holding all SQL statement that are required
     */
    private Properties sqlStrings;

    /**
     * HANA version and edition (Cloud, On prem)
     * For instance HANA Cloud: fa/CE2021.18
     * HANA on prem: fa/hana2sp05
     */
    protected String buildVersion;

    /**
     * Default constructor
     */
    public HanaConnectionManager() throws IOException {
        this.connection = null;
        this.sqlStrings = IOUtils.loadResourceProperties("SqlStrings.sql");
    }

    /**
     * Establish connection to a HANA database
     *
     * @param host      Host address
     * @param port      Port number
     * @param username  Db Username
     * @param password  Password
     */
    public void connect(String host, String port, String username, String password) throws SQLException {
        this.connection = null;
        try {
            this.connection = DriverManager.getConnection(
                    "jdbc:sap://" + host + ":" + port + "/?autocommit=true",
                    username, password);

            if (this.connection.isValid(1500)){
                this.buildVersion = this.executeQuerySingleValue(this.sqlStrings.getProperty("GET_BUILD"), null, String.class);
            }

            info("Connected to HANA database: "+host+" ("+this.buildVersion+")");
        } catch (SQLException e) {
            err("Error connecting to HANA instance:"+host);
            err(e.toString());
            throw e;
        }
    }

    /**
     * Establish connection to a HANA database
     *
     * @param cred  Connection credentials
     */
    public void connect(HanaConnectionCredentials cred) throws SQLException {
        this.connect(cred.host, cred.port, cred.username, cred.password);
    }

    /**
     * Checks if this instance of HanaConnectionManager is connected to
     * SAP HANA database
     *
     * @return True, if connection has been established
     */
    public boolean isConnected(){
        if(this.connection == null){
            return false;
        }else{
            try{
                return !this.connection.isClosed();
            }catch (Exception e){
                return false;
            }
        }
    }

    /**
     * Executes a statement on the database
     *
     * @param statement The statement to execute
     * @return          True, if statement has been executed successfully
     */
    public boolean execute(String statement) throws SQLException {
        try{
            Statement stmt = this.connection.createStatement();
            stmt.execute(statement);
            return true;
        } catch (SQLException e){
            err("Could not execute statement: " + statement);
            err(e.toString());
            throw e;
        }
    }
    /**
     * Executes a statement on the database and doesn't return any error nor throw exception
     *
     * @param statement The statement to execute
     */
    public void executeNoException(String statement) {
        try{
            Statement stmt = this.connection.createStatement();
            stmt.execute(statement);
        } catch (SQLException e){}
    }    

    /**
     * Executes a query statement on the database
     *
     * @param statement The statement to execute
     * @param params    SQL parameters
     * @return          The ResultSet of the query; Null in case of errors
     */
    public ResultSet executeQuery(String statement, HanaSqlParameter[] params) throws SQLException {
        try{
            PreparedStatement stmt = this.connection.prepareStatement(statement);

            if(params != null) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i+1, params[i].parameterValue, params[i].parameterType);
                }
            }

            return stmt.executeQuery();
        } catch (SQLException e){
            err("Could not execute statement: " + statement );
            err(e.getMessage());
            throw e;
        }
    }

    /**
     * Executes a query statement on the database
     *
     * @param statement The statement to execute
     * @return          The result of the query as a list; Null in case of errors
     */
    public List<Object[]> executeQueryList(String statement) throws SQLException {
        return this.executeQueryList(statement, null);
    }

    /**
     * Executes a query statement on the database
     *
     * @param statement The statement to execute
     * @param params    SQL parameters
     * @return          The result of the query as a list; Null in case of errors
     */
    public List<Object[]> executeQueryList(String statement, HanaSqlParameter[] params) throws SQLException {
        ResultSet resultSet = this.executeQuery(statement, params);

        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            List<Object[]> resultList = new ArrayList<>();

            while (resultSet.next()) {
                Object[] newRow = new Object[metaData.getColumnCount()];
                for (int col = 1; col <= metaData.getColumnCount(); col++) {
                    newRow[col - 1] = resultSet.getObject(col);
                }
                resultList.add(newRow);
            }

            return resultList;
        } catch (SQLException e) {
            err("Could not fetch data. " + statement);
            err(e.toString());
            throw e;
        }
    }

    /**
     * Executes a query statement on the database that return a single value
     *
     * @param statement The statement to execute
     * @param params    SQL parameters
     * @param type      Class type of the single value
     * @param <T>       Template type inferred from class type
     * @return          Single value returned by the query; Null in case of errors
     */
    public <T> T executeQuerySingleValue(String statement, HanaSqlParameter[] params, Class<T> type) throws SQLException {
        ResultSet resultSet = this.executeQuery(statement, params);

        try {
            resultSet.next();
            return resultSet.getObject(1, type);
        } catch (SQLException e) {
            err("Could not fetch data. " + statement);
            err(e.toString());
            throw e;
        }
    }

    /**
     * Retrieves the current schema from the database
     *
     * @return  Name of the currently active schema
     */
    public String getCurrentSchema() throws SQLException {
        return this.executeQuerySingleValue(this.sqlStrings.getProperty("SELECT_CURRENT_SCHEMA"), null, String.class);
    }

    /**
     * Retrieves the current version of the database
     *
     * @return  Name of the currently active schema
     */
    public String getHANABuild(){
        return this.buildVersion;
    }

    /**
     * Retrieves a list of all graph workspaces on the SAP HANA instance
     *
     * @return  List of all available graph workspaces
     */
    public List<HanaDbObject> listGraphWorkspaces() throws SQLException {
        List<Object[]> resultList = this.executeQueryList(this.sqlStrings.getProperty("LIST_GRAPH_WORKSPACES"));

        List<HanaDbObject> workspaceList = new ArrayList<>();
        for(Object[] row : resultList){
            workspaceList.add(new HanaDbObject(toStrNull(row[0]), toStrNull(row[1])));
        }

        return workspaceList;
    }

    /**
     * Loads the metadata of a given graph workspace object with pre-populated
     * workspaceDbObject (i.e. schema and name are already given)
     *
     * @param graphWorkspace    HanaGraphWorkspace with pre-populated workspaceDbObject
     * @return                  True, if metadata has been completely loaded
     */
    private boolean loadWorkspaceMetadata(HanaGraphWorkspace graphWorkspace) throws SQLException {
        List<Object[]> metadata = null;
        String propName="LOAD_WORKSPACE_METADATA_HANA_";
        propName+=(HanaConnectionManager.isCloudEdition(this.buildVersion))? "CLOUD":"ONPREM";
        debug("Reading graph metadata with "+propName);
        metadata = this.executeQueryList(
                    this.sqlStrings.getProperty(propName),
                    new HanaSqlParameter[]{
                            new HanaSqlParameter(graphWorkspace.workspaceDbObject.schema, Types.VARCHAR),
                            new HanaSqlParameter(graphWorkspace.workspaceDbObject.name, Types.VARCHAR)
                    }
            );
        
        for(Object[] row : metadata){
            HanaColumnInfo newColInfo = new HanaColumnInfo(toStrNull(row[2]), toStrNull(row[3]), toStrNull(row[4]));
            switch(toStrNull(row[0])){
                case "EDGE":
                    switch(toStrNull(row[1])){
                        case "KEY":
                            graphWorkspace.edgeKeyCol = newColInfo;
                            break;
                        case "SOURCE":
                            graphWorkspace.edgeSourceCol = newColInfo;
                            break;
                        case "TARGET":
                            graphWorkspace.edgeTargetCol = newColInfo;
                            break;
                        default:
                            graphWorkspace.edgeAttributeCols.add(newColInfo);
                    }
                    break;
                case "VERTEX":
                    switch (toStrNull(row[1])){
                        case "KEY":
                            graphWorkspace.nodeKeyCol = newColInfo;
                            break;
                        default:
                            graphWorkspace.nodeAttributeCols.add(newColInfo);
                    }
                    break;
            }
        }

        if(!graphWorkspace.isMetadataComplete()){
            err("Incomplete graph workspace definition in GRAPH_WORKSPACE_COLUMNS");
            return false;
        }
        return true;
    }

    /**
     * Loads the content of the node table for a HanaGraphWorkspace object with complete metadata
     *
     * @param graphWorkspace    HANA Graph Workspace with complete metadata
     * @return                  True, if node table content has been loaded
     */
    private void loadNetworkNodes(HanaGraphWorkspace graphWorkspace) throws SQLException {
        info("Loading network nodes of "+graphWorkspace.workspaceDbObject.toString());
        String attCols = "";
        for(HanaColumnInfo col : graphWorkspace.nodeAttributeCols){
            attCols += ", " + quoteIdentifier(col.name);
        }

        List<Object[]> nodeTable = this.executeQueryList(String.format(
                this.sqlStrings.getProperty("LOAD_NETWORK_NODES"),
                graphWorkspace.nodeKeyCol.name,
                attCols,
                graphWorkspace.nodeKeyCol.schema,
                graphWorkspace.nodeKeyCol.table
        ), null);

        graphWorkspace.nodeTable = new ArrayList<>();
        for(Object[] row : nodeTable){
            HanaNodeTableRow newRow = new HanaNodeTableRow(toStrNull(row[0]));
            for(int i=1; i<row.length; i++){
                newRow.attributeValues.put(graphWorkspace.nodeAttributeCols.get(i-1).name, row[i]);
            }
            graphWorkspace.nodeTable.add(newRow);
        }
    }

    /**
     * Loads the content of the edge table for a HanaGraphWorkspace object with complete metadata
     *
     * @param graphWorkspace    HANA Graph Workspace with complete metadata
     * @return                  True, if edge table content has been loaded
     */
    private void loadNetworkEdges(HanaGraphWorkspace graphWorkspace) throws SQLException {
        info("Loading network edges of "+graphWorkspace.workspaceDbObject.toString());
        String attCols = "";
        for(HanaColumnInfo col : graphWorkspace.edgeAttributeCols){
            attCols += ", " + quoteIdentifier(col.name) ;
        }

        List<Object[]> edgeTable = this.executeQueryList(String.format(
                this.sqlStrings.getProperty("LOAD_NETWORK_EDGES"),
                graphWorkspace.edgeKeyCol.name,
                graphWorkspace.edgeSourceCol.name,
                graphWorkspace.edgeTargetCol.name,
                attCols,
                graphWorkspace.edgeKeyCol.schema,
                graphWorkspace.edgeKeyCol.table
        ), null);

        graphWorkspace.edgeTable = new ArrayList<>();
        for(Object[] row : edgeTable){
            HanaEdgeTableRow newRow = new HanaEdgeTableRow(toStrNull(row[0]), toStrNull(row[1]), toStrNull(row[2]));
            for(int i=3; i<row.length; i++){
                newRow.attributeValues.put(graphWorkspace.edgeAttributeCols.get(i-3).name, row[i]);
            }
            graphWorkspace.edgeTable.add(newRow);
        }
    }

    /**
     * Loads the complete graph workspace (i.e. metadata, nodes, edges)
     * into a new instance of HanaGraphWorkspace
     *
     * @param schema                Schema of the workspace to be loaded
     * @param graphWorkspaceName    Name of the workspace to be loaded
     * @return                      HanaGraphWorkspace Object
     */
    public HanaGraphWorkspace loadGraphWorkspace(String schema, String graphWorkspaceName) throws SQLException {
        HanaGraphWorkspace graphWorkspace = new HanaGraphWorkspace();
        graphWorkspace.workspaceDbObject = new HanaDbObject(schema, graphWorkspaceName);

        loadWorkspaceMetadata(graphWorkspace);
        loadNetworkNodes(graphWorkspace);
        loadNetworkEdges(graphWorkspace);

        return graphWorkspace;
    }

    /**
     * Loads the complete graph workspace (i.e. metadata, nodes, edges)
     * into a new instance of HanaGraphWorkspace
     *
     * @param graphWorkspace    Schema and Name of the workspace to be loaded
     * @return                  HanaGraphWorkspace Object
     */
    public HanaGraphWorkspace loadGraphWorkspace(HanaDbObject graphWorkspace) throws SQLException {
        return loadGraphWorkspace(graphWorkspace.schema, graphWorkspace.name);
    }


}
