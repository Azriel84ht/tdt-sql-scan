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
        script.addCommand(new BteqSqlCommand("SELECT * FROM my_table;", new com.tdtsqlscan.select.SelectQuery("SELECT * FROM my_table;")));
        script.addCommand(new BteqControlCommand(BteqCommandType.LOGOFF, ".LOGOFF;"));

        BteqScriptGraphConverter converter = new BteqScriptGraphConverter();
        Graph graph = converter.convert(script);

        assertEquals(3, graph.getNodes().size());
        assertEquals(2, graph.getEdges().size());

        Node node0 = graph.getNodes().get(0);
        assertEquals("node-0", node0.getId());
        assertEquals(".LOGON", node0.getLabel());
        assertEquals("LOGON", node0.getProperties().get("commandType"));
        assertEquals(".LOGON myuser,mypass;", node0.getProperties().get("fullText"));

        Node node1 = graph.getNodes().get(1);
        assertEquals("node-1", node1.getId());
        assertEquals("SELECT", node1.getLabel());
        assertEquals("SELECT", node1.getProperties().get("commandType"));
        assertEquals("SELECT * FROM my_table;", node1.getProperties().get("fullText"));

        Node node2 = graph.getNodes().get(2);
        assertEquals("node-2", node2.getId());
        assertEquals(".LOGOFF", node2.getLabel());
        assertEquals("LOGOFF", node2.getProperties().get("commandType"));
        assertEquals(".LOGOFF;", node2.getProperties().get("fullText"));

        assertEquals("node-0", graph.getEdges().get(0).getSource());
        assertEquals("node-1", graph.getEdges().get(0).getTarget());

        assertEquals("node-1", graph.getEdges().get(1).getSource());
        assertEquals("node-2", graph.getEdges().get(1).getTarget());
    }
}
