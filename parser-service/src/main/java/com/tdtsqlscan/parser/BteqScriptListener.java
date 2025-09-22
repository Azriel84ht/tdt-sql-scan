package com.tdtsqlscan.parser;

import com.tdtsqlscan.parser.antlr.BteqBaseListener;
import com.tdtsqlscan.parser.antlr.BteqParser;
import com.tdtsqlscan.parser.dto.ParseResultDto;
import com.tdtsqlscan.parser.dto.StatementDto;
import com.tdtsqlscan.parser.dto.StatementType;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.misc.Interval;

import java.util.HashMap;
import java.util.Map;

public class BteqScriptListener extends BteqBaseListener {

    private final ParseResultDto parseResult = new ParseResultDto();

    public ParseResultDto getParseResult() {
        return parseResult;
    }

    private String getOriginalText(ParserRuleContext ctx) {
        if (ctx == null || ctx.getStart() == null || ctx.getStop() == null) {
            return ""; // Should not happen in a valid parse
        }
        CharStream inputStream = ctx.getStart().getInputStream();
        int startIndex = ctx.getStart().getStartIndex();
        int stopIndex = ctx.getStop().getStopIndex();

        if (inputStream == null || startIndex > stopIndex) {
             // Fallback for safety, though this indicates a bigger problem
             return ctx.getText();
        }

        return inputStream.getText(new Interval(startIndex, stopIndex));
    }

    @Override
    public void enterLogon_command(BteqParser.Logon_commandContext ctx) {
        StatementDto stmt = new StatementDto();
        stmt.setType(StatementType.BTEQ_COMMAND);
        stmt.setCommandName("LOGON");
        stmt.setRawContent(getOriginalText(ctx.getParent()));
        stmt.setDetails(new HashMap<>());
        parseResult.addStatement(stmt);
    }

    @Override
    public void enterLogoff_command(BteqParser.Logoff_commandContext ctx) {
        StatementDto stmt = new StatementDto();
        stmt.setType(StatementType.BTEQ_COMMAND);
        stmt.setCommandName("LOGOFF");
        stmt.setRawContent(getOriginalText(ctx.getParent()));
        stmt.setDetails(new HashMap<>());
        parseResult.addStatement(stmt);
    }

    @Override
    public void enterExport_reset_command(BteqParser.Export_reset_commandContext ctx) {
        StatementDto stmt = new StatementDto();
        stmt.setType(StatementType.BTEQ_COMMAND);
        stmt.setCommandName("EXPORT RESET");
        stmt.setRawContent(getOriginalText(ctx.getParent()));
        stmt.setDetails(new HashMap<>());
        parseResult.addStatement(stmt);
    }

    @Override
    public void enterExport_file_command(BteqParser.Export_file_commandContext ctx) {
        StatementDto stmt = new StatementDto();
        stmt.setType(StatementType.BTEQ_COMMAND);
        stmt.setCommandName("EXPORT FILE");
        stmt.setRawContent(getOriginalText(ctx.getParent()));
        Map<String, String> details = new HashMap<>();
        String path = ctx.path.getText();
        details.put("filePath", path.substring(1, path.length() - 1));
        stmt.setDetails(details);
        parseResult.addStatement(stmt);
    }

    @Override
    public void enterSet_width_command(BteqParser.Set_width_commandContext ctx) {
        StatementDto stmt = new StatementDto();
        stmt.setType(StatementType.BTEQ_COMMAND);
        stmt.setCommandName("SET WIDTH");
        stmt.setRawContent(getOriginalText(ctx.getParent()));
        Map<String, String> details = new HashMap<>();
        details.put("width", ctx.width.getText());
        stmt.setDetails(details);
        parseResult.addStatement(stmt);
    }

    @Override
    public void enterIf_errorcode_goto_command(BteqParser.If_errorcode_goto_commandContext ctx) {
        StatementDto stmt = new StatementDto();
        stmt.setType(StatementType.BTEQ_COMMAND);
        stmt.setCommandName("IF ERRORCODE GOTO");
        stmt.setRawContent(getOriginalText(ctx.getParent()));
        Map<String, String> details = new HashMap<>();
        details.put("operator", ctx.operator.getText());
        details.put("code", ctx.code.getText());
        details.put("label", ctx.label.getText());
        stmt.setDetails(details);
        parseResult.addStatement(stmt);
    }

    @Override
    public void enterDml_statement(BteqParser.Dml_statementContext ctx) {
        StatementDto stmt = new StatementDto();
        stmt.setType(StatementType.SQL_DML);
        stmt.setCommandName(ctx.getChild(0).getText().toUpperCase());
        stmt.setRawContent(getOriginalText(ctx.getParent()));
        stmt.setDetails(new HashMap<>());
        parseResult.addStatement(stmt);
    }

    @Override
    public void enterDdl_statement(BteqParser.Ddl_statementContext ctx) {
        StatementDto stmt = new StatementDto();
        stmt.setType(StatementType.SQL_DDL);
        String command = ctx.getChild(0).getText().toUpperCase() + " " + ctx.getChild(1).getText().toUpperCase();
        stmt.setCommandName(command);
        stmt.setRawContent(getOriginalText(ctx.getParent()));
        stmt.setDetails(new HashMap<>());
        parseResult.addStatement(stmt);
    }
}
