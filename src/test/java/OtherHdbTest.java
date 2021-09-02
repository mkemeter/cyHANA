import org.junit.Assert;
import org.junit.Test;
import org.kemeter.cytoscape.internal.hdb.HanaDataType;
import org.kemeter.cytoscape.internal.hdb.HanaNodeTableRow;

import java.sql.Types;
import java.util.ArrayList;

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

    @Test
    public void testHanaTypeConversion(){
        Assert.assertEquals(Types.NVARCHAR, HanaDataType.convertJavaToSqlType(String.class));
        Assert.assertEquals(Types.INTEGER, HanaDataType.convertJavaToSqlType(Integer.class));
        Assert.assertEquals(Types.DOUBLE, HanaDataType.convertJavaToSqlType(Double.class));
        Assert.assertEquals(Types.BIGINT, HanaDataType.convertJavaToSqlType(Long.class));
        Assert.assertEquals(Types.OTHER, HanaDataType.convertJavaToSqlType(new ArrayList()));
    }
}
