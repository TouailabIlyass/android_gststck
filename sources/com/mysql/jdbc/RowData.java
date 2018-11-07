package com.mysql.jdbc;

import java.sql.SQLException;

public interface RowData {
    public static final int RESULT_SET_SIZE_UNKNOWN = -1;

    void addRow(ResultSetRow resultSetRow) throws SQLException;

    void afterLast() throws SQLException;

    void beforeFirst() throws SQLException;

    void beforeLast() throws SQLException;

    void close() throws SQLException;

    ResultSetRow getAt(int i) throws SQLException;

    int getCurrentRowNumber() throws SQLException;

    ResultSetInternalMethods getOwner();

    boolean hasNext() throws SQLException;

    boolean isAfterLast() throws SQLException;

    boolean isBeforeFirst() throws SQLException;

    boolean isDynamic() throws SQLException;

    boolean isEmpty() throws SQLException;

    boolean isFirst() throws SQLException;

    boolean isLast() throws SQLException;

    void moveRowRelative(int i) throws SQLException;

    ResultSetRow next() throws SQLException;

    void removeRow(int i) throws SQLException;

    void setCurrentRow(int i) throws SQLException;

    void setMetadata(Field[] fieldArr);

    void setOwner(ResultSetImpl resultSetImpl);

    int size() throws SQLException;

    boolean wasEmpty();
}
