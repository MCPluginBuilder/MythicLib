package io.lumine.mythic.lib.data.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface SQLConsumer {

    public void accept(ResultSet value) throws SQLException;
}
