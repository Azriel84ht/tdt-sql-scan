package com.tdtsqlscan.select;

import com.tdtsqlscan.core.QueryParser;
import org.junit.Test;
import static org.junit.Assert.*;

public class SelectParserTest {

    @Test
    public void supports_recognizesSelect() {
        QueryParser p = new SelectParser();
        assertTrue(p.supports("SELECT * FROM A"));
        assertTrue(p.supports("  select col FROM tbl"));
        assertFalse(p.supports("INSERT INTO x"));
    }
}
