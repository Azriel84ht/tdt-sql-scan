package com.tdtsqlscan.etl;

import com.tdtsqlscan.core.QueryParser;
import com.tdtsqlscan.core.SQLQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class BteqScriptParserTest {

    private BteqScriptParser parser;
    private QueryParser sqlParser;

    @BeforeEach
    public void setUp() {
        sqlParser = Mockito.mock(QueryParser.class);
        parser = new BteqScriptParser(Collections.singletonList(sqlParser));
    }

    @Test
    public void testParseBteqScript() {
        String scriptText =
                ".LOGON myuser,mypass;\n" +
                "SELECT * \n" +
                "FROM my_table\n" +
                "WHERE id = 1;\n" +
                ".LOGOFF;";

        SQLQuery mockQuery = Mockito.mock(SQLQuery.class);
        when(sqlParser.supports(anyString())).thenReturn(true);
        when(sqlParser.parse(anyString())).thenReturn(mockQuery);

        BteqScript script = parser.parse(scriptText);
        List<BteqCommand> commands = script.getCommands();

        assertEquals(3, commands.size());

        BteqControlCommand logon = assertInstanceOf(BteqControlCommand.class, commands.get(0));
        assertEquals(BteqCommandType.LOGON, logon.getType());

        BteqSqlCommand sqlCommand = assertInstanceOf(BteqSqlCommand.class, commands.get(1));
        assertEquals("SELECT * FROM my_table WHERE id = 1", sqlCommand.getRawText().replaceAll("\\s+", " "));
        assertEquals(mockQuery, sqlCommand.getQuery());

        BteqControlCommand logoff = assertInstanceOf(BteqControlCommand.class, commands.get(2));
        assertEquals(BteqCommandType.LOGOFF, logoff.getType());
    }
}
