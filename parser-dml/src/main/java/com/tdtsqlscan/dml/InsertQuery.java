package com.tdtsqlscan.dml;

import com.tdtsqlscan.core.SQLQuery;
import com.tdtsqlscan.select.SelectQuery;
import java.util.List;

/**
 * Representa una sentencia INSERT.
 * Puede ser un INSERT INTO ... VALUES ... o un INSERT INTO ... SELECT ...
 */
public class InsertQuery extends SQLQuery {

    private final String tableName;
    private final List<String> columns;
    private final List<List<String>> values;
    private final List<String> sourceTables;
    private final SelectQuery selectQuery;

    /**
     * Constructor para la clase InsertQuery.
     *
     * @param sql La sentencia SQL completa.
     * @param tableName La tabla de destino del INSERT.
     * @param columns La lista de columnas (para INSERT...VALUES).
     * @param values La lista de valores (para INSERT...VALUES).
     * @param sourceTables La lista de tablas de origen (para INSERT...SELECT).
     * @param selectQuery El objeto SelectQuery parseado (para INSERT...SELECT).
     */
    public InsertQuery(String sql, String tableName, List<String> columns, List<List<String>> values, List<String> sourceTables, SelectQuery selectQuery) {
        super(sql);
        this.tableName = tableName;
        this.columns = columns;
        this.values = values;
        this.sourceTables = sourceTables;
        this.selectQuery = selectQuery;
    }

    @Override
    public Type getType() {
        return Type.INSERT;
    }

    public String getTableName() {
        return tableName;
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<List<String>> getValues() {
        return values;
    }

    public List<String> getSourceTables() {
        return sourceTables;
    }

    public SelectQuery getSelectQuery() {
        return selectQuery;
    }

    public boolean isSelect() {
        return selectQuery != null;
    }
}
