package org.kemeter.cytoscape.internal.cyhelper;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.kemeter.cytoscape.internal.hdb.HanaColumnInfo;
import org.kemeter.cytoscape.internal.hdb.HanaDbObject;

import java.util.ArrayList;
import java.util.List;

public class CyNetworkUtils {

    public static List<HanaColumnInfo> getHanaColumnInfo(HanaDbObject targetTable, CyTable cyTable){
        List<HanaColumnInfo> columnList = new ArrayList<>();
        for(CyColumn col : cyTable.getColumns()){
            columnList.add(new HanaColumnInfo(
                    targetTable.schema,
                    targetTable.name,
                    col.getName(),
                    col.isPrimaryKey()
            ));
        }
        return columnList;
    }

}
