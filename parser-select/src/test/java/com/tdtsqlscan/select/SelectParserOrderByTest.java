package com.tdtsqlscan.select;

import com.tdtsqlscan.core.SQLOrderItem;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class SelectParserOrderByTest {

    @Test
    public void parse_orderBy_multiple() {
        String sql = "SELECT a FROM T1 ORDER BY x ASC, y DESC, z;";
        SelectParser p = new SelectParser();
        SelectQuery q = p.parse(sql);

        List<SQLOrderItem> ob = q.getOrderBy();
        assertEquals(3, ob.size());
        assertEquals("x", ob.get(0).getExpression());
        assertEquals(SQLOrderItem.Direction.ASC,  ob.get(0).getDirection());
        assertEquals("y", ob.get(1).getExpression());
        assertEquals(SQLOrderItem.Direction.DESC, ob.get(1).getDirection());
        assertEquals("z", ob.get(2).getExpression());
        assertEquals(SQLOrderItem.Direction.ASC,  ob.get(2).getDirection()); // default ASC
    }
}
