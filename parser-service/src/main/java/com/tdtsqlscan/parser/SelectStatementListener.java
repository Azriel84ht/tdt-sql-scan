package com.tdtsqlscan.parser;

import com.tdtsqlscan.parser.antlr.BteqBaseListener;
import com.tdtsqlscan.parser.antlr.BteqParser;
import com.tdtsqlscan.parser.dto.ColumnDto;
import com.tdtsqlscan.parser.dto.JoinDto;
import com.tdtsqlscan.parser.dto.SelectStatementDto;
import com.tdtsqlscan.parser.dto.TableDto;
import com.tdtsqlscan.parser.dto.WhereClauseDto;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.misc.Interval;


public class SelectStatementListener extends BteqBaseListener {

    private final SelectStatementDto selectStatementDto = new SelectStatementDto();
    private TableDto currentTable;

    public SelectStatementDto getSelectStatementDto() {
        return selectStatementDto;
    }

    @Override
    public void enterColumn_element(BteqParser.Column_elementContext ctx) {
        String columnName = ctx.expression().getText();
        String alias = (ctx.alias() != null) ? ctx.alias().getText() : null;
        selectStatementDto.addColumn(new ColumnDto(columnName, alias));
    }

    @Override
    public void enterTable_reference(BteqParser.Table_referenceContext ctx) {
        String tableName = ctx.table_name().getText();
        String alias = (ctx.alias() != null) ? ctx.alias().getText() : null;

        // This listener can be entered from a from_clause or a join_clause.
        // We store the table being processed in a member variable.
        currentTable = new TableDto(tableName, alias);

        // If it's the main from clause's table, add it to the tables list.
        if (ctx.getParent() instanceof BteqParser.From_clauseContext) {
            selectStatementDto.addTable(currentTable);
        }
    }

    @Override
    public void enterJoin_clause(BteqParser.Join_clauseContext ctx) {
        // The table for the join is captured by the enterTable_reference method,
        // so 'currentTable' is already populated when we get here.

        String joinType = "INNER"; // Default
        if (ctx.join_operator() != null) {
            joinType = ctx.join_operator().getText().toUpperCase();
        }

        String onCondition = null;
        if (ctx.expression() != null) {
            // Capture the full text of the ON condition expression
            int startIndex = ctx.expression().start.getStartIndex();
            int stopIndex = ctx.expression().stop.getStopIndex();
            Interval interval = new Interval(startIndex, stopIndex);
            onCondition = ctx.start.getInputStream().getText(interval);
        }

        selectStatementDto.addJoin(new JoinDto(joinType, currentTable, onCondition));
    }

    @Override
    public void enterWhere_clause(BteqParser.Where_clauseContext ctx) {
        int startIndex = ctx.start.getStartIndex();
        int stopIndex = ctx.stop.getStopIndex();
        Interval interval = new Interval(startIndex, stopIndex);
        String condition = ctx.start.getInputStream().getText(interval);
        selectStatementDto.setWhereClause(new WhereClauseDto(condition));
    }

    @Override
    public void enterGroup_by_clause(BteqParser.Group_by_clauseContext ctx) {
        for (BteqParser.ExpressionContext exprCtx : ctx.expression()) {
            selectStatementDto.addGroupByColumn(exprCtx.getText());
        }
    }
}
