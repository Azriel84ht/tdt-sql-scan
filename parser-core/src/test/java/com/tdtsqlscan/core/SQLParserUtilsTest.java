package com.tdtsqlscan.core;

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

/**
 * Tests para SQLParserUtils.
 */
public class SQLParserUtilsTest {
    @Test
    public void splitTopLevel_onCommas() {
        List<String> parts = SQLParserUtils.splitTopLevel("a, b, func(c, d), e", ",");
        assertEquals(4, parts.size());
        assertEquals("a", parts.get(0).trim());
        assertEquals("func(c, d)", parts.get(2).trim());
    }

    @Test
    public void findTopLevelKeyword_caseInsensitive() {
        int idx = SQLParserUtils.findTopLevelKeyword(
                "SELECT * FROM A iNNeR jOiN B ON A.id=B.id", "INNER JOIN", 0);
        assertTrue(idx > 0);
    }
}
