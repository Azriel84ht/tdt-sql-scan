package com.tdtsqlscan.select;

import com.tdtsqlscan.core.SQLQuery;

/**
 * AST específico de una consulta SELECT.
 */
public class SelectQuery extends SQLQuery {

    public SelectQuery(String sqlText) {
        super(sqlText);
        // TODO: inicializar componentes de SELECT (columns, from, joins…)
    }

    @Override
    public String getType() {
        return "SELECT";
    }

    // TODO: añadir getters para columnas, tablas, joins, etc.
}
