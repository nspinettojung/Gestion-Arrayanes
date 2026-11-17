package com.Gestion.Arrayanes.support;

import org.hibernate.resource.jdbc.spi.StatementInspector;

import java.util.ArrayList;
import java.util.List;

/** Inspector de SQL para comprobar que una consulta no se repite por fila. */
public class SqlStatementCollector implements StatementInspector {

    private static final ThreadLocal<List<String>> STATEMENTS =
            ThreadLocal.withInitial(ArrayList::new);

    @Override
    public String inspect(String sql) {
        STATEMENTS.get().add(sql);
        return sql;
    }

    public static void reset() {
        STATEMENTS.get().clear();
    }

    public static long selectCount() {
        return STATEMENTS.get().stream()
                .map(String::trim)
                .filter(sql -> sql.regionMatches(true, 0, "select", 0, 6))
                .count();
    }
}
