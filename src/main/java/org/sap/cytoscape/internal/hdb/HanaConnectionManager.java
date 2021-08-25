package org.sap.cytoscape.internal.hdb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles communication with the database and SAP HANA specific stuff
 */
public class HanaConnectionManager {

    /**
     *  Helper function to handle the String representation of DB null values
     *
     * @param obj   Value of a database record
     * @return      String representation of the value; "DB_NULL" if value has been null
     */
    private static String toStrNull(Object obj){
        return obj == null ? "DB_NULL" : obj.toString();
    }

    /**
     * Internal connection object
     */
    private Connection connection;

    /**
     * Default constructor
     */
    public HanaConnectionManager() {
        this.connection = null;
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

            System.out.println("Connected to HANA database: ");
            System.out.println(host);
        } catch (SQLException e) {
            System.err.println("Error connecting to HANA instance:");
            System.err.println(e);
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
    public boolean execute(String statement){
        try{
            Statement stmt = this.connection.createStatement();
            stmt.execute(statement);
            return true;
        } catch (SQLException e){
            System.err.println("Could not execute statement");
            System.err.println(statement);
            System.err.println(e);
            return false;
        }
    }

    /**
     * Executes a query statement on the database
     *
     * @param statement The statement to execute
     * @param params    SQL parameters
     * @return          The ResultSet of the query; Null in case of errors
     */
    public ResultSet executeQuery(String statement, HanaSqlParameter[] params){
        try{
            PreparedStatement stmt = this.connection.prepareStatement(statement);

            if(params != null) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i+1, params[i].parameterValue, params[i].parameterType);
                }
            }

            return stmt.executeQuery();
        } catch (SQLException e){
            System.err.println("Could not execute statement");
            System.err.println(statement);
            System.err.println(e);
            return null;
        }
    }

    /**
     * Executes a query statement on the database
     *
     * @param statement The statement to execute
     * @return          The result of the query as a list; Null in case of errors
     */
    public List<Object[]> executeQueryList(String statement){
        return this.executeQueryList(statement, null);
    }

    /**
     * Executes a query statement on the database
     *
     * @param statement The statement to execute
     * @param params    SQL parameters
     * @return          The result of the query as a list; Null in case of errors
     */
    public List<Object[]> executeQueryList(String statement, HanaSqlParameter[] params){
        ResultSet resultSet = this.executeQuery(statement, params);

        if(resultSet == null) return null;

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
            System.err.println("Could not fetch data");
            System.err.println(statement);
            System.err.println(e);
            return null;
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
    public <T> T executeQuerySingleValue(String statement, HanaSqlParameter[] params, Class<T> type){
        ResultSet resultSet = this.executeQuery(statement, params);

        if(resultSet == null) return null;

        try {
            resultSet.next();
            return resultSet.getObject(1, type);
        } catch (SQLException e) {
            System.err.println("Could not fetch data");
            System.err.println(statement);
            System.err.println(e);
            return null;
        }
    }

    /**
     * Retrieves the current schema from the database
     *
     * @return  Name of the currently active schema
     */
    public String getCurrentSchema(){
        return this.executeQuerySingleValue(
                "SELECT CURRENT_SCHEMA FROM DUMMY", null, String.class
        );
    }

    /**
     * Retrieves a list of all graph workspaces on the SAP HANA instance
     *
     * @return  List of all available graph workspaces
     */
    public List<HanaDbObject> listGraphWorkspaces(){
        List<Object[]> resultList = this.executeQueryList(
                "SELECT SCHEMA_NAME, WORKSPACE_NAME FROM GRAPH_WORKSPACES WHERE IS_VALID = 'TRUE'"
        );

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
    private boolean loadWorkspaceMetadata(HanaGraphWorkspace graphWorkspace){
        List<Object[]> metadata = this.executeQueryList(
                "SELECT ENTITY_TYPE, ENTITY_ROLE, ENTITY_SCHEMA_NAME, ENTITY_TABLE_NAME, ENTITY_COLUMN_NAME " +
                        "FROM GRAPH_WORKSPACE_COLUMNS WHERE SCHEMA_NAME = ? AND WORKSPACE_NAME = ?",
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
            System.err.println("Incomplete graph workspace definition in GRAPH_WORKSPACE_COLUMNS");
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
    private boolean loadNetworkNodes(HanaGraphWorkspace graphWorkspace){
        String attCols = "";
        for(HanaColumnInfo col : graphWorkspace.nodeAttributeCols){
            attCols += ",\"" + col.name + "\"";
        }

        List<Object[]> nodeTable = this.executeQueryList(String.format(
                "SELECT \"%s\" %s FROM \"%s\".\"%s\"",
                graphWorkspace.nodeKeyCol.name,
                attCols,
                graphWorkspace.nodeKeyCol.schema,
                graphWorkspace.nodeKeyCol.table
        ), null);

        if(nodeTable == null) return false;

        graphWorkspace.nodeTable = new ArrayList<>();
        for(Object[] row : nodeTable){
            HanaNodeTableRow newRow = new HanaNodeTableRow(toStrNull(row[0]));
            for(int i=1; i<row.length; i++){
                newRow.attributeValues.put(graphWorkspace.nodeAttributeCols.get(i-1).name, row[i]);
            }
            graphWorkspace.nodeTable.add(newRow);
        }
        return true;
    }

    /**
     * Loads the content of the edge table for a HanaGraphWorkspace object with complete metadata
     *
     * @param graphWorkspace    HANA Graph Workspace with complete metadata
     * @return                  True, if edge table content has been loaded
     */
    private boolean loadNetworkEdges(HanaGraphWorkspace graphWorkspace){

        String attCols = "";
        for(HanaColumnInfo col : graphWorkspace.edgeAttributeCols){
            attCols += ",\"" + col.name + "\"";
        }

        List<Object[]> edgeTable = this.executeQueryList(String.format(
                "SELECT \"%s\", \"%s\", \"%s\" %s FROM \"%s\".\"%s\"",
                graphWorkspace.edgeKeyCol.name,
                graphWorkspace.edgeSourceCol.name,
                graphWorkspace.edgeTargetCol.name,
                attCols,
                graphWorkspace.edgeKeyCol.schema,
                graphWorkspace.edgeKeyCol.table
        ), null);

        if(edgeTable == null) return false;

        graphWorkspace.edgeTable = new ArrayList<>();
        for(Object[] row : edgeTable){
            HanaEdgeTableRow newRow = new HanaEdgeTableRow(toStrNull(row[0]), toStrNull(row[1]), toStrNull(row[2]));
            for(int i=3; i<row.length; i++){
                newRow.attributeValues.put(graphWorkspace.edgeAttributeCols.get(i-3).name, row[i]);
            }
            graphWorkspace.edgeTable.add(newRow);
        }
        return true;
    }

    /**
     * Loads the complete graph workspace (i.e. metadata, nodes, edges)
     * into a new instance of HanaGraphWorkspace
     *
     * @param schema                Schema of the workspace to be loaded
     * @param graphWorkspaceName    Name of the workspace to be loaded
     * @return                      HanaGraphWorkspace Object
     */
    public HanaGraphWorkspace loadGraphWorkspace(String schema, String graphWorkspaceName){
        HanaGraphWorkspace graphWorkspace = new HanaGraphWorkspace();
        graphWorkspace.workspaceDbObject = new HanaDbObject(schema, graphWorkspaceName);

        if(!loadWorkspaceMetadata(graphWorkspace)){
            return null;
        }
        if(!loadNetworkNodes(graphWorkspace)){
            return null;
        }
        if(!loadNetworkEdges(graphWorkspace)){
            return null;
        }

        return graphWorkspace;
    }

    /**
     * Loads the complete graph workspace (i.e. metadata, nodes, edges)
     * into a new instance of HanaGraphWorkspace
     *
     * @param graphWorkspace    Schema and Name of the workspace to be loaded
     * @return                  HanaGraphWorkspace Object
     */
    public HanaGraphWorkspace loadGraphWorkspace(HanaDbObject graphWorkspace){
        return loadGraphWorkspace(graphWorkspace.schema, graphWorkspace.name);
    }


}
