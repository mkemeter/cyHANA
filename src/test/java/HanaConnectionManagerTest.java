import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kemeter.cytoscape.internal.hdb.HanaConnectionManager;
import org.kemeter.cytoscape.internal.hdb.HanaDbObject;
import org.kemeter.cytoscape.internal.hdb.HanaGraphWorkspace;

import java.io.IOException;
import java.util.List;

public class HanaConnectionManagerTest {

    private static HanaConnectionManager connectionManager;

    @BeforeClass
    public static void setUp() throws IOException {
        connectionManager = HanaConnectionManagerTestUtils.connectToTestInstance();
        HanaConnectionManagerTestUtils.createSspGraph(connectionManager);
        HanaConnectionManagerTestUtils.createFlightsGraph(connectionManager);
    }

    @Test
    public void testInitialSetup(){
        Assert.assertNotNull(connectionManager);
        Assert.assertTrue(connectionManager.isConnected());
    }

    @Test
    public void testGetCurrentSchema(){
        String currentSchema = connectionManager.getCurrentSchema();

        Assert.assertNotNull(currentSchema);
        Assert.assertTrue(currentSchema.length() > 0);
    }

    @Test
    public void someQuery(){

        List<Object[]> resultList = connectionManager.executeQueryList(
                "SELECT 'A', 'B' FROM DUMMY UNION SELECT 'C', 'D' FROM DUMMY",
                null
        );

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
        HanaGraphWorkspace sspWorkspace = connectionManager.loadGraphWorkspace(
                connectionManager.getCurrentSchema(), "SSP");

        Assert.assertNotNull(sspWorkspace);
        Assert.assertEquals(6, sspWorkspace.edgeTable.size());
        Assert.assertEquals(4, sspWorkspace.nodeTable.size());
    }

    @Test
    public void loadGraphWorkspaceWithGeometries(){
        HanaGraphWorkspace flightsWorkspace = connectionManager.loadGraphWorkspace(
                this.connectionManager.getCurrentSchema(), "FLIGHTS");

        Assert.assertNotNull(flightsWorkspace);
        Assert.assertEquals(31, flightsWorkspace.edgeTable.size());
        Assert.assertEquals(8, flightsWorkspace.nodeTable.size());
    }

    @Test
    public void listGraphWorkspace(){

        List<HanaDbObject> workspaceList = connectionManager.listGraphWorkspaces();
        String currentSchema = connectionManager.getCurrentSchema();

        boolean foundTestWorkspace = false;
        for(HanaDbObject ws : workspaceList){
            if(ws.schema.equals(currentSchema) && ws.name.equals("SSP")){
                foundTestWorkspace = true;
            }
        }

        if(!foundTestWorkspace) Assert.fail();
    }
}
