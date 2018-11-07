package com.mysql.jdbc;

import java.sql.SQLException;
import java.util.List;

public class RowDataStatic implements RowData {
    private int index = -1;
    private Field[] metadata;
    ResultSetImpl owner;
    private List<ResultSetRow> rows;

    public RowDataStatic(List<ResultSetRow> rows) {
        this.rows = rows;
    }

    public void addRow(ResultSetRow row) {
        this.rows.add(row);
    }

    public void afterLast() {
        if (this.rows.size() > 0) {
            this.index = this.rows.size();
        }
    }

    public void beforeFirst() {
        if (this.rows.size() > 0) {
            this.index = -1;
        }
    }

    public void beforeLast() {
        if (this.rows.size() > 0) {
            this.index = this.rows.size() - 2;
        }
    }

    public void close() {
    }

    public ResultSetRow getAt(int atIndex) throws SQLException {
        if (atIndex >= 0) {
            if (atIndex < this.rows.size()) {
                return ((ResultSetRow) this.rows.get(atIndex)).setMetadata(this.metadata);
            }
        }
        return null;
    }

    public int getCurrentRowNumber() {
        return this.index;
    }

    public ResultSetInternalMethods getOwner() {
        return this.owner;
    }

    public boolean hasNext() {
        boolean z = true;
        if (this.index + 1 >= this.rows.size()) {
            z = false;
        }
        return z;
    }

    public boolean isAfterLast() {
        return this.index >= this.rows.size() && this.rows.size() != 0;
    }

    public boolean isBeforeFirst() {
        return this.index == -1 && this.rows.size() != 0;
    }

    public boolean isDynamic() {
        return false;
    }

    public boolean isEmpty() {
        return this.rows.size() == 0;
    }

    public boolean isFirst() {
        return this.index == 0;
    }

    public boolean isLast() {
        boolean z = false;
        if (this.rows.size() == 0) {
            return false;
        }
        if (this.index == this.rows.size() - 1) {
            z = true;
        }
        return z;
    }

    public void moveRowRelative(int rowsToMove) {
        if (this.rows.size() > 0) {
            this.index += rowsToMove;
            if (this.index < -1) {
                beforeFirst();
            } else if (this.index > this.rows.size()) {
                afterLast();
            }
        }
    }

    public ResultSetRow next() throws SQLException {
        this.index++;
        if (this.index > this.rows.size()) {
            afterLast();
        } else if (this.index < this.rows.size()) {
            return ((ResultSetRow) this.rows.get(this.index)).setMetadata(this.metadata);
        }
        return null;
    }

    public void removeRow(int atIndex) {
        this.rows.remove(atIndex);
    }

    public void setCurrentRow(int newIndex) {
        this.index = newIndex;
    }

    public void setOwner(ResultSetImpl rs) {
        this.owner = rs;
    }

    public int size() {
        return this.rows.size();
    }

    public boolean wasEmpty() {
        return this.rows != null && this.rows.size() == 0;
    }

    public void setMetadata(Field[] metadata) {
        this.metadata = metadata;
    }
}
