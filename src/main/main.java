package main;

import query.SQLselect;
import query.SQLExpression;
import query.SQLTableRef;
import query.SQLJoin;
import query.SQLCondition;
import query.SQLOrderItem;
import query.SQLParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        String path = args.length > 0 ? args[0] : "query_examples/select_ex.sql";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(new File(path)))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append(" ");
            }
            String sql = sb.toString().trim();
            SQLselect query = new SQLselect(sql);

            System.out.println("\n=== QUERY ANALYSIS ===\n");

            // SELECT clause
            System.out.println(">>> SELECT CLAUSE");
            if (query.isDistinct()) {
                System.out.println("  DISTINCT");
            }
            if (query.getTopN() != null) {
                String pct = query.isTopPercent() ? " PERCENT" : "";
                System.out.println("  TOP " + query.getTopN() + pct);
            }
            System.out.println("  Columns:");
            List<SQLExpression> cols = query.getColumns();
            for (int i = 0; i < cols.size(); i++) {
                SQLExpression col = cols.get(i);
                System.out.printf("    %2d. %s%n", i + 1, col.toString());
            }

            // FROM clause
            System.out.println("\n>>> FROM CLAUSE");
            List<SQLTableRef> tables = query.getTables();
            for (int i = 0; i < tables.size(); i++) {
                SQLTableRef tbl = tables.get(i);
                System.out.printf("    %2d. %s%n", i + 1, tbl.toString());
            }

            // JOINs
            List<SQLJoin> joins = query.getJoins();
            if (!joins.isEmpty()) {
                System.out.println("\n>>> JOINS");
                for (SQLJoin join : joins) {
                    String joinDesc = join.getJoinType() + " JOIN " + join.getTableRef().toString();
                    if (join.getCondition() != null) {
                        joinDesc += " ON " + join.getCondition().toString();
                    } else if (!join.getUsingColumns().isEmpty()) {
                        String colsUsing = join.getUsingColumns().stream().collect(Collectors.joining(", "));
                        joinDesc += " USING (" + colsUsing + ")";
                    }
                    System.out.println("    - " + joinDesc);
                }
            }

            // WHERE
            SQLCondition where = query.getWhereCondition();
            if (where != null) {
                System.out.println("\n>>> WHERE");
                System.out.println("    " + where.toString());
            }

            // GROUP BY
            List<SQLExpression> groupBy = query.getGroupBy();
            if (!groupBy.isEmpty()) {
                System.out.println("\n>>> GROUP BY");
                for (SQLExpression expr : groupBy) {
                    System.out.println("    - " + expr.toString());
                }
            }

            // HAVING
            SQLCondition having = query.getHavingCondition();
            if (having != null) {
                System.out.println("\n>>> HAVING");
                System.out.println("    " + having.toString());
            }

            // QUALIFY
            SQLCondition qualify = query.getQualifyCondition();
            if (qualify != null) {
                System.out.println("\n>>> QUALIFY");
                System.out.println("    " + qualify.toString());
            }

            // ORDER BY
            List<SQLOrderItem> orderBy = query.getOrderBy();
            if (!orderBy.isEmpty()) {
                System.out.println("\n>>> ORDER BY");
                for (SQLOrderItem item : orderBy) {
                    System.out.println("    - " + item.toString());
                }
            }

            System.out.println("\n=== END OF ANALYSIS ===\n");

        } catch (SQLParseException spe) {
            System.err.println("Error parsing SQL:");
            spe.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error:");
            e.printStackTrace();
        }
    }
}
