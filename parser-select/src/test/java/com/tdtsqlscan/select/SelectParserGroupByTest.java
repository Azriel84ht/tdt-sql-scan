package com.tdtsqlscan.select;

import com.tdtsqlscan.core.SQLCondition;
import org.junit.Test;
import static org.junit.Assert.*;

public class SelectParserGroupByTest {

    @Test
    public void parse_groupByAndHaving_single() {
        String sql = "SELECT a, SUM(b) FROM T1 GROUP BY a HAVING SUM(b) > 10;";
        SelectParser p = new SelectParser();
        SelectQuery q = p.parse(sql);

        // Ahora esperamos 2 columnas: a y SUM(b)
        assertEquals(2, q.getColumns().size());
        assertTrue(q.getColumns().contains("a"));
        assertTrue(q.getColumns().contains("SUM(b)"));

        // Solo un elemento en GROUP BY
        assertEquals(1, q.getGroupBy().size());
        assertEquals("a", q.getGroupBy().get(0));

        // Solo una condición en HAVING
        assertEquals(1, q.getHavingConditions().size());
        SQLCondition h = q.getHavingConditions().get(0);
        assertEquals("SUM(b) > 10", h.getExpression());
    }

    @Test
    public void parse_groupBy_multipleNoHaving() {
        String sql = "SELECT x, y FROM T2 GROUP BY x, y;";
        SelectParser p = new SelectParser();
        SelectQuery q = p.parse(sql);

        assertEquals(2, q.getColumns().size());
        assertEquals(2, q.getGroupBy().size());
        assertEquals("x", q.getGroupBy().get(0));
        assertEquals("y", q.getGroupBy().get(1));
        assertTrue(q.getHavingConditions().isEmpty());
    }
}
