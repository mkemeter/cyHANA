import org.junit.Assert;
import org.junit.Test;
import org.kemeter.cytoscape.internal.hdb.HanaNodeTableRow;

public class OtherHdbTest {
    @Test
    public void testNodeSuidCast(){
        HanaNodeTableRow nodeTableRow = new HanaNodeTableRow();
        nodeTableRow.setKeyFieldName("SUID");
        // put number as a string
        nodeTableRow.getFieldValues().put("SUID", "1234");

        long test = nodeTableRow.getKeyValue(Long.class);

        Assert.assertEquals(1234, test);
    }
}
