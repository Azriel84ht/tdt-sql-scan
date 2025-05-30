package com.tdtsqlscan.ddl;

import com.tdtsqlscan.core.SQLQuery;
import java.util.List;

/**
 * Representa la consulta CREATE TABLE.
 */
public class CreateTableQuery extends SQLQuery {

    private final String tableName;
    private final List<ColumnDefinition> columns;

    /**
     * @param sql       texto completo de la consulta (sin ';')
     * @param tableName nombre de la tabla a crear
     * @param columns   lista de definiciones de columna
     */
    public CreateTableQuery(String sql, String tableName, List<ColumnDefinition> columns) {
        super(sql);
        this.tableName = tableName;
        this.columns = columns;
    }

    @Override
    public String getType() {
        return "CREATE_TABLE";
    }

    public String getTableName() {
        return tableName;
    }

    public List<ColumnDefinition> getColumns() {
        return columns;
    }
}
