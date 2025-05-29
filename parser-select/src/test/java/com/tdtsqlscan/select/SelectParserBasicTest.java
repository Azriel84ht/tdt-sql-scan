package com.tdtsqlscan.select;

import com.tdtsqlscan.core.SQLTableRef;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Pruebas básicas para SelectParser y SelectQuery.
 */
public class SelectParserBasicTest {

    @Test
    public void parse_simpleSelect() {
        String sql = "SELECT a, b, c FROM T1, T2;";
        SelectParser p = new SelectParser();
        SelectQuery q = p.parse(sql);

        // columnas
        assertEquals(3, q.getColumns().size());
        assertTrue(q.getColumns().contains("a"));
        assertTrue(q.getColumns().contains("b"));
        assertTrue(q.getColumns().contains("c"));

        // tablas
        assertEquals(2, q.getTables().size());
        SQLTableRef t1 = q.getTables().get(0);
        SQLTableRef t2 = q.getTables().get(1);

        assertEquals("T1", t1.getExpression());
        assertNull("No debe haber alias en T1", t1.getAlias());
        assertEquals("T2", t2.getExpression());
        assertNull("No debe haber alias en T2", t2.getAlias());
    }
}
