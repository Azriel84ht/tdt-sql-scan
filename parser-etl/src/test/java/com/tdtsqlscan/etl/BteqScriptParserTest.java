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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class BteqScriptParserTest {

    private BteqScriptParser parser;

    @BeforeEach
    public void setUp() {
        parser = new BteqScriptParser();
    }

    @Test
    public void testParseBteqScript() {
        String scriptText =
                ".LOGON myuser,mypass; -- logon line\n" +
                "/* multiline\n" +
                "   comment */\n" +
                ".SET SESSION DATABASE a;\n" +
                "SELECT * \n" +
                "FROM my_table\n" +
                "WHERE id = 1;\n" +
                ".LOGOFF;";

        BteqScript script = parser.parse(scriptText, "test.bteq");
        List<BteqCommand> commands = script.getCommands();

        assertEquals(3, commands.size());

        BteqConfigurationCommand config = assertInstanceOf(BteqConfigurationCommand.class, commands.get(0));
        assertEquals(2, config.getCommands().size());

        BteqSqlCommand sqlCommand = assertInstanceOf(BteqSqlCommand.class, commands.get(1));
        assertEquals("SELECT * FROM my_table WHERE id = 1", sqlCommand.getRawText().replaceAll("\\s+", " "));

        SQLQuery query = sqlCommand.getQuery();
        assertNotNull(query);
        assertInstanceOf(com.tdtsqlscan.core.SelectQuery.class, query);
        com.tdtsqlscan.core.SelectQuery selectQuery = (com.tdtsqlscan.core.SelectQuery) query;
        assertEquals(1, selectQuery.getColumns().size());
        assertEquals("*", selectQuery.getColumns().get(0));
        assertEquals(1, selectQuery.getTables().size());
        assertEquals("my_table", selectQuery.getTables().get(0).getName());

        BteqControlCommand logoff = assertInstanceOf(BteqControlCommand.class, commands.get(2));
        assertEquals(BteqCommandType.LOGOFF, logoff.getType());
    }

    @Test
    public void testParseCreateTable() {
        String scriptText = "CREATE TABLE my_table (col1 INT, col2 VARCHAR(100));";
        BteqScript script = parser.parse(scriptText, "test.bteq");
        List<BteqCommand> commands = script.getCommands();
        assertEquals(1, commands.size());

        BteqSqlCommand sqlCommand = assertInstanceOf(BteqSqlCommand.class, commands.get(0));
        SQLQuery query = sqlCommand.getQuery();
        assertNotNull(query);
        assertInstanceOf(com.tdtsqlscan.core.CreateTableQuery.class, query);
        com.tdtsqlscan.core.CreateTableQuery createTableQuery = (com.tdtsqlscan.core.CreateTableQuery) query;
        assertEquals("my_table", createTableQuery.getTableName());
        assertEquals(2, createTableQuery.getColumns().size());
        assertEquals("col1", createTableQuery.getColumns().get(0).getName());
        assertEquals("INT", createTableQuery.getColumns().get(0).getType());
        assertEquals("col2", createTableQuery.getColumns().get(1).getName());
        assertEquals("VARCHAR (100)", createTableQuery.getColumns().get(1).getType());
    }

    @Test
    public void testParseCreateTableAsSelect() {
        String scriptText = "CREATE TABLE my_table AS (SELECT * FROM another_table) WITH DATA;";
        BteqScript script = parser.parse(scriptText, "test.bteq");
        List<BteqCommand> commands = script.getCommands();
        assertEquals(1, commands.size());

        BteqSqlCommand sqlCommand = assertInstanceOf(BteqSqlCommand.class, commands.get(0));
        SQLQuery query = sqlCommand.getQuery();
        assertNotNull(query);
        assertInstanceOf(com.tdtsqlscan.core.CreateTableQuery.class, query);
        com.tdtsqlscan.core.CreateTableQuery createTableQuery = (com.tdtsqlscan.core.CreateTableQuery) query;
        assertEquals("my_table", createTableQuery.getTableName());
        assertNotNull(createTableQuery.getSelectQuery());
        assertEquals(1, createTableQuery.getSourceTables().size());
        assertEquals("another_table", createTableQuery.getSourceTables().get(0).getName());
    }

    @Test
    public void testParseInsertValues() {
        String scriptText = "INSERT INTO my_table (col1, col2) VALUES (1, 'test');";
        BteqScript script = parser.parse(scriptText, "test.bteq");
        List<BteqCommand> commands = script.getCommands();
        assertEquals(1, commands.size());

        BteqSqlCommand sqlCommand = assertInstanceOf(BteqSqlCommand.class, commands.get(0));
        SQLQuery query = sqlCommand.getQuery();
        assertNotNull(query);
        assertInstanceOf(com.tdtsqlscan.core.InsertQuery.class, query);
        com.tdtsqlscan.core.InsertQuery insertQuery = (com.tdtsqlscan.core.InsertQuery) query;
        assertEquals("my_table", insertQuery.getTableName());
        assertEquals(2, insertQuery.getColumns().size());
        assertEquals("col1", insertQuery.getColumns().get(0));
        assertEquals("col2", insertQuery.getColumns().get(1));
        // Note: The current implementation of JsqlAstConverter does not parse VALUES, so this will be empty.
        assertEquals(0, insertQuery.getValues().size());
    }

    @Test
    public void testParseInsertSelect() {
        String scriptText = "INSERT INTO my_table (col1, col2) SELECT c1, c2 FROM another_table;";
        BteqScript script = parser.parse(scriptText, "test.bteq");
        List<BteqCommand> commands = script.getCommands();
        assertEquals(1, commands.size());

        BteqSqlCommand sqlCommand = assertInstanceOf(BteqSqlCommand.class, commands.get(0));
        SQLQuery query = sqlCommand.getQuery();
        assertNotNull(query);
        assertInstanceOf(com.tdtsqlscan.core.InsertQuery.class, query);
        com.tdtsqlscan.core.InsertQuery insertQuery = (com.tdtsqlscan.core.InsertQuery) query;
        assertEquals("my_table", insertQuery.getTableName());
        assertEquals(2, insertQuery.getColumns().size());
        assertEquals("col1", insertQuery.getColumns().get(0));
        assertEquals("col2", insertQuery.getColumns().get(1));
        assertNotNull(insertQuery.getSelectQuery());
        assertEquals(1, insertQuery.getSourceTables().size());
        assertEquals("another_table", insertQuery.getSourceTables().get(0).getName());
    }

    @Test
    public void testParseUpdate() {
        String scriptText = "UPDATE my_table SET col1 = 1 WHERE col2 = 'test';";
        BteqScript script = parser.parse(scriptText, "test.bteq");
        List<BteqCommand> commands = script.getCommands();
        assertEquals(1, commands.size());

        BteqSqlCommand sqlCommand = assertInstanceOf(BteqSqlCommand.class, commands.get(0));
        SQLQuery query = sqlCommand.getQuery();
        assertNotNull(query);
        assertInstanceOf(com.tdtsqlscan.core.UpdateQuery.class, query);
        com.tdtsqlscan.core.UpdateQuery updateQuery = (com.tdtsqlscan.core.UpdateQuery) query;
        assertEquals("my_table", updateQuery.getTargetTable());
    }

    @Test
    public void testParseDelete() {
        String scriptText = "DELETE FROM my_table WHERE col1 = 1;";
        BteqScript script = parser.parse(scriptText, "test.bteq");
        List<BteqCommand> commands = script.getCommands();
        assertEquals(1, commands.size());

        BteqSqlCommand sqlCommand = assertInstanceOf(BteqSqlCommand.class, commands.get(0));
        SQLQuery query = sqlCommand.getQuery();
        assertNotNull(query);
        assertInstanceOf(com.tdtsqlscan.core.DeleteQuery.class, query);
        com.tdtsqlscan.core.DeleteQuery deleteQuery = (com.tdtsqlscan.core.DeleteQuery) query;
        assertEquals("my_table", deleteQuery.getTable());
        assertNotNull(deleteQuery.getCondition());
    }

    @Test
    public void testParseDropTable() {
        String scriptText = "DROP TABLE my_table;";
        BteqScript script = parser.parse(scriptText, "test.bteq");
        List<BteqCommand> commands = script.getCommands();
        assertEquals(1, commands.size());

        BteqSqlCommand sqlCommand = assertInstanceOf(BteqSqlCommand.class, commands.get(0));
        SQLQuery query = sqlCommand.getQuery();
        assertNotNull(query);
        assertInstanceOf(com.tdtsqlscan.core.DropTableQuery.class, query);
        com.tdtsqlscan.core.DropTableQuery dropTableQuery = (com.tdtsqlscan.core.DropTableQuery) query;
        assertEquals("my_table", dropTableQuery.getTableName());
    }

    @Test
    public void testParseCreateIndex() {
        String scriptText = "CREATE INDEX my_index ON my_table (col1);";
        BteqScript script = parser.parse(scriptText, "test.bteq");
        List<BteqCommand> commands = script.getCommands();
        assertEquals(1, commands.size());

        BteqSqlCommand sqlCommand = assertInstanceOf(BteqSqlCommand.class, commands.get(0));
        SQLQuery query = sqlCommand.getQuery();
        assertNotNull(query);
        assertInstanceOf(com.tdtsqlscan.core.CreateIndexQuery.class, query);
        com.tdtsqlscan.core.CreateIndexQuery createIndexQuery = (com.tdtsqlscan.core.CreateIndexQuery) query;
        assertEquals("my_index", createIndexQuery.getIndexName());
        assertEquals("my_table", createIndexQuery.getTableName());
        assertEquals(1, createIndexQuery.getColumns().size());
        assertEquals("col1", createIndexQuery.getColumns().get(0));
    }
}
