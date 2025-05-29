package com.tdtsqlscan.core;

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

/**
 * Tests unitarios para SQLParserUtils.
 */
public class SQLParserUtilsTest {

    @Test
    public void splitTopLevel_onCommas() {
        String input = "a, b, func(c, d), e";
        List<String> parts = SQLParserUtils.splitTopLevel(input, ",");
        assertEquals(4, parts.size());
        assertEquals("a", parts.get(0).trim());
        assertEquals("b", parts.get(1).trim());
        assertEquals("func(c, d)", parts.get(2).trim());
        assertEquals("e", parts.get(3).trim());
    }

    @Test
    public void splitTopLevel_handlesNestedParens() {
        String input = "x, y(z, w(q, r)), t";
        List<String> parts = SQLParserUtils.splitTopLevel(input, ",");
        assertEquals(3, parts.size());
        assertEquals("x", parts.get(0).trim());
        assertEquals("y(z, w(q, r))", parts.get(1).trim());
        assertEquals("t", parts.get(2).trim());
    }

    @Test
    public void findTopLevelKeyword_caseInsensitive() {
        String input = "SELECT * FROM A iNNeR jOiN B ON A.id=B.id";
        int idx = SQLParserUtils.findTopLevelKeyword(input, "INNER JOIN", 0);
        assertTrue("Debe encontrar 'INNER JOIN' ignorando mayúsculas/minúsculas", idx > 0);
    }

    @Test
    public void findTopLevelKeyword_notFound() {
        String input = "SELECT * FROM A LEFT OUTER JOIN B ON A.id=B.id";
        int idx = SQLParserUtils.findTopLevelKeyword(input, "INNER JOIN", 0);
        assertEquals(-1, idx);
    }
}
