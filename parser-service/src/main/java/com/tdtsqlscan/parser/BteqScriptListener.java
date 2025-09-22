package com.tdtsqlscan.parser;

import com.tdtsqlscan.parser.antlr.BteqBaseListener;
import com.tdtsqlscan.parser.antlr.BteqParser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BteqScriptListener extends BteqBaseListener {

    private final List<String> commands = new ArrayList<>();

    public List<String> getCommands() {
        return commands;
    }

    @Override
    public void enterLogon_command(BteqParser.Logon_commandContext ctx) {
        String commandText = ctx.LOGON().getText();
        String content = ctx.content_up_to_semicolon().getText();
        commands.add("LOGON: " + commandText + content);
    }

    @Override
    public void enterLogoff_command(BteqParser.Logoff_commandContext ctx) {
        commands.add("LOGOFF: " + ctx.LOGOFF().getText());
    }

    @Override
    public void enterSql_statement(BteqParser.Sql_statementContext ctx) {
        // We need to reconstruct the SQL statement from the tokens
        String sql = ctx.content_up_to_semicolon().children.stream()
                .map(ParseTree::getText)
                .collect(Collectors.joining(" "));
        commands.add("SQL: " + sql.trim());
    }
}
