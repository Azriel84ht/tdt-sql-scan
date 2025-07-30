package com.tdtsqlscan.graph;

import com.tdtsqlscan.etl.BteqCommand;
import com.tdtsqlscan.etl.BteqControlCommand;
import com.tdtsqlscan.etl.BteqCommandType;
import com.tdtsqlscan.etl.BteqScript;
import com.tdtsqlscan.etl.BteqSqlCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BteqScriptGraphConverterTest {

    @Test
    public void testConvert() {
        BteqScript script = new BteqScript();
        script.addCommand(new BteqControlCommand(BteqCommandType.LOGON, ".LOGON myuser,mypass;"));
        script.addCommand(new BteqSqlCommand("SELECT * FROM my_table;", null));
        script.addCommand(new BteqControlCommand(BteqCommandType.LOGOFF, ".LOGOFF;"));

        BteqScriptGraphConverter converter = new BteqScriptGraphConverter();
        Graph graph = converter.convert(script);

        assertEquals(3, graph.getNodes().size());
        assertEquals(2, graph.getEdges().size());

        assertEquals("node-0", graph.getNodes().get(0).getId());
        assertEquals(".LOGON myuser,mypass;", graph.getNodes().get(0).getLabel());

        assertEquals("node-1", graph.getNodes().get(1).getId());
        assertEquals("SELECT * FROM my_table;", graph.getNodes().get(1).getLabel());

        assertEquals("node-2", graph.getNodes().get(2).getId());
        assertEquals(".LOGOFF;", graph.getNodes().get(2).getLabel());

        assertEquals("node-0", graph.getEdges().get(0).getSource());
        assertEquals("node-1", graph.getEdges().get(0).getTarget());

        assertEquals("node-1", graph.getEdges().get(1).getSource());
        assertEquals("node-2", graph.getEdges().get(1).getTarget());
    }
}
