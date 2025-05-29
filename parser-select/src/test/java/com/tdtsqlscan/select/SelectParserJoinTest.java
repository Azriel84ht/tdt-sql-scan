package com.tdtsqlscan.select;

import com.tdtsqlscan.core.SQLJoin;
import org.junit.Test;
import static org.junit.Assert.*;

public class SelectParserJoinTest {

    @Test
    public void parse_selectWithJoinsAndWhere() {
        String sql = "SELECT c.id, o.amount "
                   + "FROM customers c "
                   + "INNER JOIN orders o ON c.id=o.cid "
                   + "LEFT JOIN payments p ON o.id=p.oid "
                   + "WHERE c.active = 'Y';";

        SelectParser parser = new SelectParser();
        SelectQuery q    = parser.parse(sql);

        // Columnas
        assertEquals(2, q.getColumns().size());
        assertTrue(q.getColumns().contains("c.id"));
        assertTrue(q.getColumns().contains("o.amount"));

        // Tablas base + joins (las tablas se añaden en el orden parseado)
        assertEquals(3, q.getTables().size());
        assertEquals("customers", q.getTables().get(0).getExpression());
        assertEquals("c",         q.getTables().get(0).getAlias());
        assertEquals("orders",    q.getTables().get(1).getExpression());
        assertEquals("o",         q.getTables().get(1).getAlias());
        assertEquals("payments",  q.getTables().get(2).getExpression());
        assertEquals("p",         q.getTables().get(2).getAlias());

        // Joins
        assertEquals(2, q.getJoins().size());
        assertEquals(SQLJoin.Type.INNER, q.getJoins().get(0).getType());
        assertEquals("c.id=o.cid",      q.getJoins().get(0).getCondition());
        assertEquals(SQLJoin.Type.LEFT,  q.getJoins().get(1).getType());
        assertEquals("o.id=p.oid",      q.getJoins().get(1).getCondition());

        // WHERE
        assertEquals(1, q.getWhereConditions().size());
        assertEquals("c.active = 'Y'", q.getWhereConditions().get(0).getExpression());
    }
}
