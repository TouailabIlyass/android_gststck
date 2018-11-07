package com.mysql.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.NClob;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;

public class JDBC4UpdatableResultSet extends UpdatableResultSet {
    public JDBC4UpdatableResultSet(String catalog, Field[] fields, RowData tuples, MySQLConnection conn, StatementImpl creatorStmt) throws SQLException {
        super(catalog, fields, tuples, conn, creatorStmt);
    }

    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        updateNCharacterStream(columnIndex, x, (int) length);
    }

    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        throw new NotUpdatable();
    }

    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
        updateAsciiStream(findColumn(columnLabel), x);
    }

    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
        updateAsciiStream(findColumn(columnLabel), x, length);
    }

    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
        updateBinaryStream(findColumn(columnLabel), x);
    }

    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
        updateBinaryStream(findColumn(columnLabel), x, length);
    }

    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
        updateBlob(findColumn(columnLabel), inputStream);
    }

    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
        updateBlob(findColumn(columnLabel), inputStream, length);
    }

    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
        updateCharacterStream(findColumn(columnLabel), reader);
    }

    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        updateCharacterStream(findColumn(columnLabel), reader, length);
    }

    public void updateClob(String columnLabel, Reader reader) throws SQLException {
        updateClob(findColumn(columnLabel), reader);
    }

    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
        updateClob(findColumn(columnLabel), reader, length);
    }

    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
        updateNCharacterStream(findColumn(columnLabel), reader);
    }

    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        updateNCharacterStream(findColumn(columnLabel), reader, length);
    }

    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
        updateNClob(findColumn(columnLabel), reader);
    }

    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
        updateNClob(findColumn(columnLabel), reader, length);
    }

    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
        updateSQLXML(findColumn(columnLabel), xmlObject);
    }

    public void updateNCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            String fieldEncoding = this.fields[columnIndex - 1].getEncoding();
            if (fieldEncoding != null) {
                if (fieldEncoding.equals("UTF-8")) {
                    if (this.onInsertRow) {
                        ((JDBC4PreparedStatement) this.inserter).setNCharacterStream(columnIndex, x, (long) length);
                        if (x == null) {
                            this.thisRow.setColumnValue(columnIndex - 1, null);
                        } else {
                            this.thisRow.setColumnValue(columnIndex - 1, STREAM_DATA_MARKER);
                        }
                    } else {
                        if (!this.doingUpdates) {
                            this.doingUpdates = true;
                            syncUpdate();
                        }
                        ((JDBC4PreparedStatement) this.updater).setNCharacterStream(columnIndex, x, (long) length);
                    }
                }
            }
            throw new SQLException("Can not call updateNCharacterStream() when field's character set isn't UTF-8");
        }
    }

    public void updateNCharacterStream(String columnName, Reader reader, int length) throws SQLException {
        updateNCharacterStream(findColumn(columnName), reader, length);
    }

    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            String fieldEncoding = this.fields[columnIndex - 1].getEncoding();
            if (fieldEncoding != null) {
                if (fieldEncoding.equals("UTF-8")) {
                    if (nClob == null) {
                        updateNull(columnIndex);
                    } else {
                        updateNCharacterStream(columnIndex, nClob.getCharacterStream(), (int) nClob.length());
                    }
                }
            }
            throw new SQLException("Can not call updateNClob() when field's character set isn't UTF-8");
        }
    }

    public void updateNClob(String columnName, NClob nClob) throws SQLException {
        updateNClob(findColumn(columnName), nClob);
    }

    public void updateNString(int columnIndex, String x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            String fieldEncoding = this.fields[columnIndex - 1].getEncoding();
            if (fieldEncoding != null) {
                if (fieldEncoding.equals("UTF-8")) {
                    if (this.onInsertRow) {
                        ((JDBC4PreparedStatement) this.inserter).setNString(columnIndex, x);
                        if (x == null) {
                            this.thisRow.setColumnValue(columnIndex - 1, null);
                        } else {
                            this.thisRow.setColumnValue(columnIndex - 1, StringUtils.getBytes(x, this.charConverter, fieldEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), getExceptionInterceptor()));
                        }
                    } else {
                        if (!this.doingUpdates) {
                            this.doingUpdates = true;
                            syncUpdate();
                        }
                        ((JDBC4PreparedStatement) this.updater).setNString(columnIndex, x);
                    }
                }
            }
            throw new SQLException("Can not call updateNString() when field's character set isn't UTF-8");
        }
    }

    public void updateNString(String columnName, String x) throws SQLException {
        updateNString(findColumn(columnName), x);
    }

    public int getHoldability() throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    protected NClob getNativeNClob(int columnIndex) throws SQLException {
        String stringVal = getStringForNClob(columnIndex);
        if (stringVal == null) {
            return null;
        }
        return getNClobFromString(stringVal, columnIndex);
    }

    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        String fieldEncoding = this.fields[columnIndex - 1].getEncoding();
        if (fieldEncoding != null) {
            if (fieldEncoding.equals("UTF-8")) {
                return getCharacterStream(columnIndex);
            }
        }
        throw new SQLException("Can not call getNCharacterStream() when field's charset isn't UTF-8");
    }

    public Reader getNCharacterStream(String columnName) throws SQLException {
        return getNCharacterStream(findColumn(columnName));
    }

    public NClob getNClob(int columnIndex) throws SQLException {
        String fieldEncoding = this.fields[columnIndex - 1].getEncoding();
        if (fieldEncoding != null) {
            if (fieldEncoding.equals("UTF-8")) {
                if (this.isBinaryEncoded) {
                    return getNativeNClob(columnIndex);
                }
                String asString = getStringForNClob(columnIndex);
                if (asString == null) {
                    return null;
                }
                return new JDBC4NClob(asString, getExceptionInterceptor());
            }
        }
        throw new SQLException("Can not call getNClob() when field's charset isn't UTF-8");
    }

    public NClob getNClob(String columnName) throws SQLException {
        return getNClob(findColumn(columnName));
    }

    private final NClob getNClobFromString(String stringVal, int columnIndex) throws SQLException {
        return new JDBC4NClob(stringVal, getExceptionInterceptor());
    }

    public String getNString(int columnIndex) throws SQLException {
        String fieldEncoding = this.fields[columnIndex - 1].getEncoding();
        if (fieldEncoding != null) {
            if (fieldEncoding.equals("UTF-8")) {
                return getString(columnIndex);
            }
        }
        throw new SQLException("Can not call getNString() when field's charset isn't UTF-8");
    }

    public String getNString(String columnName) throws SQLException {
        return getNString(findColumn(columnName));
    }

    public RowId getRowId(int columnIndex) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    public RowId getRowId(String columnLabel) throws SQLException {
        return getRowId(findColumn(columnLabel));
    }

    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        return new JDBC4MysqlSQLXML(this, columnIndex, getExceptionInterceptor());
    }

    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        return getSQLXML(findColumn(columnLabel));
    }

    private String getStringForNClob(int columnIndex) throws SQLException {
        String asString = null;
        String forcedEncoding = "UTF-8";
        try {
            byte[] asBytes;
            if (this.isBinaryEncoded) {
                asBytes = getNativeBytes(columnIndex, true);
            } else {
                asBytes = getBytes(columnIndex);
            }
            if (asBytes != null) {
                asString = new String(asBytes, forcedEncoding);
            }
            return asString;
        } catch (UnsupportedEncodingException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Unsupported character encoding ");
            stringBuilder.append(forcedEncoding);
            throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
    }

    public boolean isClosed() throws SQLException {
        return this.isClosed;
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        checkClosed();
        return iface.isInstance(this);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        try {
            return iface.cast(this);
        } catch (ClassCastException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Unable to unwrap to ");
            stringBuilder.append(iface.toString());
            throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
    }
}
