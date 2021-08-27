import org.kemeter.cytoscape.internal.hdb.HanaConnectionCredentials;
import org.kemeter.cytoscape.internal.hdb.HanaConnectionManager;
import org.kemeter.cytoscape.internal.utils.IOUtils;

import java.sql.SQLException;
import java.util.Properties;

public class HanaConnectionManagerTestUtils {

    /**
     *
     * @return
     */
    public static HanaConnectionCredentials getTestCredentials(){


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
    public static HanaConnectionManager connectToTestInstance(){
        try{
            HanaConnectionManager connectionManager = new HanaConnectionManager();
            connectionManager.connect(getTestCredentials());
            return connectionManager;
        } catch (Exception e){
            return null;
        }
    }

    /**
     *
     * @param conMgr
     */
    public static void createSspGraph(HanaConnectionManager conMgr) throws SQLException {
        
        conMgr.executeNoException("DROP TABLE SSP_NODES");
        conMgr.executeNoException("DROP TABLE SSP_EDGES");
        conMgr.executeNoException("DROP GRAPH WORKSPACE SSP");

        if(conMgr.execute("CREATE TABLE SSP_NODES (ID INTEGER UNIQUE NOT NULL, NAME NVARCHAR(255))")){
            conMgr.execute("INSERT INTO SSP_NODES VALUES (1, 'Schere')");
            conMgr.execute("INSERT INTO SSP_NODES VALUES (2, 'Stein')");
            conMgr.execute("INSERT INTO SSP_NODES VALUES (3, 'Papier')");
            conMgr.execute("INSERT INTO SSP_NODES VALUES (4, 'Hulk')");
        }

        if(conMgr.execute("CREATE TABLE SSP_EDGES (ID INTEGER UNIQUE NOT NULL, SOURCE_ID INTEGER NOT NULL, SINK_ID INTEGER NOT NULL)")){
            conMgr.execute("INSERT INTO SSP_EDGES VALUES (1, 1, 3)");
            conMgr.execute("INSERT INTO SSP_EDGES VALUES (2, 2, 1)");
            conMgr.execute("INSERT INTO SSP_EDGES VALUES (3, 3, 2)");
            conMgr.execute("INSERT INTO SSP_EDGES VALUES (4, 4, 1)");
            conMgr.execute("INSERT INTO SSP_EDGES VALUES (5, 4, 2)");
            conMgr.execute("INSERT INTO SSP_EDGES VALUES (6, 4, 3)");
        }

        String SQL_CREATE_TEST_WORKSPACE = "CREATE GRAPH WORKSPACE SSP\n" +
        "\tEDGE TABLE SSP_EDGES \n"+
        "\tSOURCE COLUMN SOURCE_ID  \n"+
        "\tTARGET COLUMN SINK_ID  \n"+
        "\tKEY COLUMN ID \n"+
        "\tVERTEX TABLE SSP_NODES \n"+
        "\tKEY COLUMN ID";

        if (HanaConnectionManager.isCloudEdition(conMgr.getHANABuild())){
           SQL_CREATE_TEST_WORKSPACE+="\n     WITH DYNAMIC CACHE";
        }

        conMgr.execute(SQL_CREATE_TEST_WORKSPACE);
    }

    public static void createFlightsGraph(HanaConnectionManager conMgr) throws SQLException {
        conMgr.executeNoException("DROP GRAPH WORKSPACE FLIGHTS");
        conMgr.executeNoException("DROP TABLE FLIGHTS_EDGES");
        conMgr.executeNoException("DROP TABLE FLIGHTS_NODES");

        conMgr.execute("CREATE TABLE \"FLIGHTS_NODES\" (\n" +
                "    \"NODE_KEY\" NVARCHAR(100) PRIMARY KEY,\n" +
                "    \"TYPE\"     NVARCHAR(100),\n" +
                "    \"NAME\"     NVARCHAR(100),\n" +
                "    \"CITY\"     NVARCHAR(100),\n" +
                "    \"COUNTRY\"  NVARCHAR(100),\n" +
                "    \"LOCATION\" ST_GEOMETRY(4326)\n" +
                ")");


        conMgr.execute("CREATE TABLE \"FLIGHTS_EDGES\" (\n" +
                "    \"EDGE_KEY\" BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,\n" +
                "    \"SOURCE\" NVARCHAR(100) NOT NULL REFERENCES \"FLIGHTS_NODES\" (\"NODE_KEY\")\n" +
                "        ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                "    \"TARGET\" NVARCHAR(100) NOT NULL REFERENCES \"FLIGHTS_NODES\" (\"NODE_KEY\")\n" +
                "        ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                "    \"DIST_KM\" DOUBLE\n" +
                ")");

        conMgr.execute("CREATE GRAPH WORKSPACE \"FLIGHTS\"\n" +
                "    EDGE TABLE \"FLIGHTS_EDGES\"\n" +
                "        SOURCE COLUMN \"SOURCE\"\n" +
                "        TARGET COLUMN \"TARGET\"\n" +
                "        KEY COLUMN \"EDGE_KEY\"\n" +
                "    VERTEX TABLE \"FLIGHTS_NODES\"\n" +
                "        KEY COLUMN \"NODE_KEY\"");

        conMgr.execute("INSERT INTO \"FLIGHTS_NODES\" VALUES (\n" +
                "    'FRA', 'Airport', 'Frankfurt am Main Airport', 'Frankfurt am Main', 'Germany',\n" +
                "    ST_GEOMFROMTEXT('POINT(8.570556 50.033333)', 4326))");
        conMgr.execute("INSERT INTO \"FLIGHTS_NODES\" VALUES (\n" +
                "    'JFK', 'Airport', 'John F Kennedy International Airport', 'New York', 'United States',\n" +
                "    ST_GEOMFROMTEXT('POINT(-73.77890015 -73.77890015)', 4326))");
        conMgr.execute("INSERT INTO \"FLIGHTS_NODES\" VALUES (\n" +
                "    'DXB', 'Airport', 'Dubai International Airport', 'Dubai', 'United Arab Emirates',\n" +
                "    ST_GEOMFROMTEXT('POINT(55.3643989563 25.2527999878)', 4326))");
        conMgr.execute("INSERT INTO \"FLIGHTS_NODES\" VALUES (\n" +
                "    'AMS', 'Airport', 'Amsterdam Airport Schiphol', 'Amsterdam', 'Netherlands',\n" +
                "    ST_GEOMFROMTEXT('POINT(4.76389 52.308601)', 4326))");
        conMgr.execute("INSERT INTO \"FLIGHTS_NODES\" VALUES (\n" +
                "    'PEK', 'Airport', 'Beijing Capital International Airport', 'Beijing', 'China',\n" +
                "    ST_GEOMFROMTEXT('POINT(116.58499908447266 40.080101013183594)', 4326))");
        conMgr.execute("INSERT INTO \"FLIGHTS_NODES\" VALUES (\n" +
                "    'DEL', 'Airport', 'Indira Gandhi International Airport', 'Delhi', 'India',\n" +
                "    ST_GEOMFROMTEXT('POINT(77.103104 28.5665)', 4326))");
        conMgr.execute("INSERT INTO \"FLIGHTS_NODES\" VALUES (\n" +
                "    'PHL', 'Airport', 'Philadelphia International Airport', 'Philadelphia', 'United States',\n" +
                "    ST_GEOMFROMTEXT('POINT(-75.24109649658203 -75.24109649658203)', 4326))");
        conMgr.execute("INSERT INTO \"FLIGHTS_NODES\" VALUES (\n" +
                "    'TNA', 'Airport', 'Yaoqiang Airport', 'Jinan', 'China',\n" +
                "    ST_GEOMFROMTEXT('POINT(117.21600341796875 36.857200622558594)', 4326))");

        conMgr.execute("INSERT INTO \"FLIGHTS_EDGES\" (\"SOURCE\", \"TARGET\", \"DIST_KM\") VALUES ('FRA','PEK',7808.278)");
        conMgr.execute("INSERT INTO \"FLIGHTS_EDGES\" (\"SOURCE\", \"TARGET\", \"DIST_KM\") VALUES ('DEL','FRA',6131.81)");
        conMgr.execute("INSERT INTO \"FLIGHTS_EDGES\" (\"SOURCE\", \"TARGET\", \"DIST_KM\") VALUES ('JFK','FRA',6206.163)");
        conMgr.execute("INSERT INTO \"FLIGHTS_EDGES\" (\"SOURCE\", \"TARGET\", \"DIST_KM\") VALUES ('PEK','FRA',7808.278)");
        conMgr.execute("INSERT INTO \"FLIGHTS_EDGES\" (\"SOURCE\", \"TARGET\", \"DIST_KM\") VALUES ('AMS','FRA',367.268)");
        conMgr.execute("INSERT INTO \"FLIGHTS_EDGES\" (\"SOURCE\", \"TARGET\", \"DIST_KM\") VALUES ('DXB','FRA',4848.058)");
        conMgr.execute("INSERT INTO \"FLIGHTS_EDGES\" (\"SOURCE\", \"TARGET\", \"DIST_KM\") VALUES ('FRA','JFK',6206.163)");
        conMgr.execute("INSERT INTO \"FLIGHTS_EDGES\" (\"SOURCE\", \"TARGET\", \"DIST_KM\") VALUES ('JFK','AMS',5863.339)");
        conMgr.execute("INSERT INTO \"FLIGHTS_EDGES\" (\"SOURCE\", \"TARGET\", \"DIST_KM\") VALUES ('PEK','AMS',7848.339)");
        conMgr.execute("INSERT INTO \"FLIGHTS_EDGES\" (\"SOURCE\", \"TARGET\", \"DIST_KM\") VALUES ('DXB','AMS',5174.013)");
        conMgr.execute("INSERT INTO \"FLIGHTS_EDGES\" (\"SOURCE\", \"TARGET\", \"DIST_KM\") VALUES ('DEL','AMS',6375.078)");
        conMgr.execute("INSERT INTO \"FLIGHTS_EDGES\" (\"SOURCE\", \"TARGET\", \"DIST_KM\") VALUES ('FRA','AMS',367.268)");
        conMgr.execute("INSERT INTO \"FLIGHTS_EDGES\" (\"SOURCE\", \"TARGET\", \"DIST_KM\") VALUES ('FRA','DXB',4848.058)");
        conMgr.execute("INSERT INTO \"FLIGHTS_EDGES\" (\"SOURCE\", \"TARGET\", \"DIST_KM\") VALUES ('FRA','DEL',6131.81)");
        conMgr.execute("INSERT INTO \"FLIGHTS_EDGES\" (\"SOURCE\", \"TARGET\", \"DIST_KM\") VALUES ('DEL','PEK',3815.291)");
        conMgr.execute("INSERT INTO \"FLIGHTS_EDGES\" (\"SOURCE\", \"TARGET\", \"DIST_KM\") VALUES ('DXB','PEK',5855.097)");
        conMgr.execute("INSERT INTO \"FLIGHTS_EDGES\" (\"SOURCE\", \"TARGET\", \"DIST_KM\") VALUES ('AMS','PEK',7848.339)");
        conMgr.execute("INSERT INTO \"FLIGHTS_EDGES\" (\"SOURCE\", \"TARGET\", \"DIST_KM\") VALUES ('DEL','DXB',2187.214)");
        conMgr.execute("INSERT INTO \"FLIGHTS_EDGES\" (\"SOURCE\", \"TARGET\", \"DIST_KM\") VALUES ('PEK','DXB',5855.097)");
        conMgr.execute("INSERT INTO \"FLIGHTS_EDGES\" (\"SOURCE\", \"TARGET\", \"DIST_KM\") VALUES ('AMS','DXB',5174.013)");
        conMgr.execute("INSERT INTO \"FLIGHTS_EDGES\" (\"SOURCE\", \"TARGET\", \"DIST_KM\") VALUES ('JFK','DXB',11021.831)");
        conMgr.execute("INSERT INTO \"FLIGHTS_EDGES\" (\"SOURCE\", \"TARGET\", \"DIST_KM\") VALUES ('DEL','JFK',11776.798)");
        conMgr.execute("INSERT INTO \"FLIGHTS_EDGES\" (\"SOURCE\", \"TARGET\", \"DIST_KM\") VALUES ('AMS','JFK',5863.339)");
        conMgr.execute("INSERT INTO \"FLIGHTS_EDGES\" (\"SOURCE\", \"TARGET\", \"DIST_KM\") VALUES ('DXB','JFK',11021.831)");
        conMgr.execute("INSERT INTO \"FLIGHTS_EDGES\" (\"SOURCE\", \"TARGET\", \"DIST_KM\") VALUES ('AMS','DEL',6375.078)");
        conMgr.execute("INSERT INTO \"FLIGHTS_EDGES\" (\"SOURCE\", \"TARGET\", \"DIST_KM\") VALUES ('PEK','DEL',3815.291)");
        conMgr.execute("INSERT INTO \"FLIGHTS_EDGES\" (\"SOURCE\", \"TARGET\", \"DIST_KM\") VALUES ('DXB','DEL',2187.214)");
        conMgr.execute("INSERT INTO \"FLIGHTS_EDGES\" (\"SOURCE\", \"TARGET\", \"DIST_KM\") VALUES ('JFK','DEL',11776.798)");
        conMgr.execute("INSERT INTO \"FLIGHTS_EDGES\" (\"SOURCE\", \"TARGET\", \"DIST_KM\") VALUES ('PHL','JFK',150.81)");
        conMgr.execute("INSERT INTO \"FLIGHTS_EDGES\" (\"SOURCE\", \"TARGET\", \"DIST_KM\") VALUES ('JFK','PHL',150.81)");
        conMgr.execute("INSERT INTO \"FLIGHTS_EDGES\" (\"SOURCE\", \"TARGET\", \"DIST_KM\") VALUES ('PEK','TNA',361.969)");
    }
}
