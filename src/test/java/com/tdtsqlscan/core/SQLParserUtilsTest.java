package com.tdtsqlscan.core;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import main.java.com.tdtsqlscan.core.SQLParserUtils;

/**
 * Tests unitarios para las utilidades de parsing SQL.
 */
public class SQLParserUtilsTest {

    @Test
    public void splitTopLevel_onCommas() {
        String s = "a, b, func(c, d), e";
        List<String> parts = SQLParserUtils.splitTopLevel(s, ",");
        assertEquals(4, parts.size());
        assertEquals("a", parts.get(0).trim());
        assertEquals("b", parts.get(1).trim());
        assertEquals("func(c, d)", parts.get(2).trim());
        assertEquals("e", parts.get(3).trim());
    }

    @Test
    public void splitTopLevel_handlesNestedParens() {
        String s = "x, y(z, w(q, r)), t";
        List<String> parts = SQLParserUtils.splitTopLevel(s, ",");
        assertEquals(3, parts.size());
        assertEquals("x", parts.get(0).trim());
        assertEquals("y(z, w(q, r))", parts.get(1).trim());
        assertEquals("t", parts.get(2).trim());
    }

    @Test
    public void findTopLevelKeyword_innerJoin() {
        String s = "SELECT * FROM A INNER JOIN B ON A.id = B.id";
        int idx = SQLParserUtils.findTopLevelKeyword(s, "INNER JOIN", 0);
        assertTrue("Debe encontrar 'INNER JOIN' a nivel top", idx > 0);
    }

    @Test
    public void findTopLevelKeyword_caseInsensitive() {
        String s = "select * from A iNNeR jOiN B on A.id=B.id";
        int idx = SQLParserUtils.findTopLevelKeyword(s, "INNER JOIN", 0);
        assertTrue("La búsqueda debe ser case-insensitive", idx > 0);
    }
}
