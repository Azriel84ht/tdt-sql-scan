package com.tdtsqlscan.select;

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

        assertEquals(3, q.getColumns().size());
        assertTrue(q.getColumns().contains("a"));
        assertTrue(q.getColumns().contains("b"));
        assertTrue(q.getColumns().contains("c"));

        assertEquals(2, q.getTables().size());
        assertTrue(q.getTables().contains("T1"));
        assertTrue(q.getTables().contains("T2"));
    }
}
