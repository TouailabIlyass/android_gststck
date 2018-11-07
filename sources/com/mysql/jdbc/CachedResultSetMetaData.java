package com.mysql.jdbc;

import java.sql.ResultSetMetaData;
import java.util.Map;

public class CachedResultSetMetaData {
    Map<String, Integer> columnNameToIndex = null;
    Field[] fields;
    Map<String, Integer> fullColumnNameToIndex = null;
    ResultSetMetaData metadata;

    public Map<String, Integer> getColumnNameToIndex() {
        return this.columnNameToIndex;
    }

    public Field[] getFields() {
        return this.fields;
    }

    public Map<String, Integer> getFullColumnNameToIndex() {
        return this.fullColumnNameToIndex;
    }

    public ResultSetMetaData getMetadata() {
        return this.metadata;
    }
}
