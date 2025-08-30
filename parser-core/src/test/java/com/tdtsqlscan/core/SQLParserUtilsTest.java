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

    @Test
    public void extractBetweenKeywords_handlesNestedParens() {
        String sql = "CREATE TABLE my_table (col1 VARCHAR(50), col2 DECIMAL(10, 2))";
        String result = SQLParserUtils.extractBetweenKeywords(sql, "(", ")");
        assertEquals("col1 VARCHAR(50), col2 DECIMAL(10, 2)", result);
    }

    @Test
    public void extractBetweenKeywords_withMultiCharKeywords() {
        String sql = "SELECT * FROM my_table WHERE id = 1";
        String result = SQLParserUtils.extractBetweenKeywords(sql, "FROM", "WHERE");
        assertEquals("my_table", result.trim());
    }
}
