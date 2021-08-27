import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kemeter.cytoscape.internal.hdb.HanaConnectionCredentials;
import org.kemeter.cytoscape.internal.hdb.HanaConnectionManager;
import org.kemeter.cytoscape.internal.hdb.HanaDbObject;
import org.kemeter.cytoscape.internal.hdb.HanaGraphWorkspace;
import org.kemeter.cytoscape.internal.utils.IOUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class HanaConnectionManagerTest {

    private static HanaConnectionManager connectionManager;
    private static Properties sqlStringsTest;

    /**
     *
     * @return
     */
    private static HanaConnectionCredentials getTestCredentials(){
        try{
            Properties connectProps = IOUtils.loadResourceProperties("testcredentials.properties");

            HanaConnectionCredentials testCred = new HanaConnectionCredentials(
                    connectProps.getProperty("host"),
                    connectProps.getProperty("port"),
                    connectProps.getProperty("username"),
                    connectProps.getProperty("password")
            );

            return testCred;
        }catch (Exception e){
            System.err.println("Cannot load connection details for test instance");
            return null;
        }


    }

    /**
     *
     * @return
     */
    private static HanaConnectionManager connectToTestInstance() throws SQLException, IOException {
        HanaConnectionManager connectionManager = new HanaConnectionManager();
        connectionManager.connect(getTestCredentials());
        return connectionManager;
    }

    private static void createSspGraph() throws SQLException {
        connectionManager.executeNoException(sqlStringsTest.getProperty("DROP_SSP_WORKSPACE"));
        connectionManager.executeNoException(sqlStringsTest.getProperty("DROP_SSP_TABLES"));
        connectionManager.execute((sqlStringsTest.getProperty("CREATE_SSP_TABLES")));
        connectionManager.execute((sqlStringsTest.getProperty("CREATE_SSP_WORKSPACE")));
    }

    private static void createFlightsGraph() throws SQLException {
        connectionManager.executeNoException(sqlStringsTest.getProperty("DROP_FLIGHTS_WORKSPACE"));
        connectionManager.executeNoException(sqlStringsTest.getProperty("DROP_FLIGHTS_TABLES"));
        connectionManager.execute((sqlStringsTest.getProperty("CREATE_FLIGHTS_TABLES")));
        connectionManager.execute((sqlStringsTest.getProperty("INSERT_FLIGHTS_TABLES_VALUES")));
        connectionManager.execute((sqlStringsTest.getProperty("CREATE_FLIGHTS_WORKSPACE")));
    }

    @BeforeClass
    public static void setUp() throws IOException, SQLException {
        connectionManager = connectToTestInstance();
        sqlStringsTest = IOUtils.loadResourceProperties("SqlStringsTest.sql");

        createSspGraph();
        createFlightsGraph();
    }

    @Test
    public void testInitialSetup(){
        Assert.assertNotNull(connectionManager);
        Assert.assertTrue(connectionManager.isConnected());
    }

    @Test
    public void testGetCurrentSchema(){
        String currentSchema = null;

        try{
            currentSchema = connectionManager.getCurrentSchema();
        } catch (SQLException e){
            Assert.fail();
        }

        Assert.assertNotNull(currentSchema);
        Assert.assertTrue(currentSchema.length() > 0);
    }

    @Test
    public void someQuery(){

        List<Object[]> resultList = null;
        try {
            resultList = connectionManager.executeQueryList(
                    sqlStringsTest.getProperty("SOME_QUERY"),
                    null
            );
        } catch (SQLException e){
            Assert.fail();
        }

        if(resultList == null || resultList.size() != 2) Assert.fail();

        Object[] firstRow = resultList.get(0);
        if(!firstRow[0].equals("A")) Assert.fail();
        if(!firstRow[1].equals("B")) Assert.fail();

        Object[] secondRow = resultList.get(1);
        if(!secondRow[0].equals("C")) Assert.fail();
        if(!secondRow[1].equals("D")) Assert.fail();
    }

    @Test
    public void loadGraphWorkspace(){
        HanaGraphWorkspace sspWorkspace = null;
        try {
            sspWorkspace = connectionManager.loadGraphWorkspace(connectionManager.getCurrentSchema(), "SSP");
        } catch (SQLException e){
            Assert.fail();
        }

        Assert.assertNotNull(sspWorkspace);
        Assert.assertEquals(6, sspWorkspace.edgeTable.size());
        Assert.assertEquals(4, sspWorkspace.nodeTable.size());
    }

    @Test
    public void loadGraphWorkspaceWithGeometries(){
        HanaGraphWorkspace flightsWorkspace = null;
        try {
            flightsWorkspace = connectionManager.loadGraphWorkspace(this.connectionManager.getCurrentSchema(), "FLIGHTS");
        } catch (SQLException e){
            Assert.fail();
        }

        Assert.assertNotNull(flightsWorkspace);
        Assert.assertEquals(31, flightsWorkspace.edgeTable.size());
        Assert.assertEquals(8, flightsWorkspace.nodeTable.size());
    }

    @Test
    public void listGraphWorkspace(){
        List<HanaDbObject> workspaceList = null;
        String currentSchema = null;

        try {
            workspaceList = connectionManager.listGraphWorkspaces();
            currentSchema = connectionManager.getCurrentSchema();
        } catch (SQLException e){
            Assert.fail();
        }

        boolean foundTestWorkspace = false;
        for(HanaDbObject ws : workspaceList){
            if(ws.schema.equals(currentSchema) && ws.name.equals("SSP")){
                foundTestWorkspace = true;
            }
        }

        if(!foundTestWorkspace) Assert.fail();
    }

    @Test
    public void testSchemaExists(){
        try {
            Assert.assertTrue(connectionManager.schemaExists("SYS"));
            Assert.assertFalse(connectionManager.schemaExists("THIS_SCHEMA_DOES_NOT_EXIST"));
        } catch (SQLException e){
            Assert.fail();
        }
    }
}
