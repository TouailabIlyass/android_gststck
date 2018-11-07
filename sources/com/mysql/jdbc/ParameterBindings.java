package com.mysql.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Ref;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

public interface ParameterBindings {
    Array getArray(int i) throws SQLException;

    InputStream getAsciiStream(int i) throws SQLException;

    BigDecimal getBigDecimal(int i) throws SQLException;

    InputStream getBinaryStream(int i) throws SQLException;

    Blob getBlob(int i) throws SQLException;

    boolean getBoolean(int i) throws SQLException;

    byte getByte(int i) throws SQLException;

    byte[] getBytes(int i) throws SQLException;

    Reader getCharacterStream(int i) throws SQLException;

    Clob getClob(int i) throws SQLException;

    Date getDate(int i) throws SQLException;

    double getDouble(int i) throws SQLException;

    float getFloat(int i) throws SQLException;

    int getInt(int i) throws SQLException;

    long getLong(int i) throws SQLException;

    Reader getNCharacterStream(int i) throws SQLException;

    Reader getNClob(int i) throws SQLException;

    Object getObject(int i) throws SQLException;

    Ref getRef(int i) throws SQLException;

    short getShort(int i) throws SQLException;

    String getString(int i) throws SQLException;

    Time getTime(int i) throws SQLException;

    Timestamp getTimestamp(int i) throws SQLException;

    URL getURL(int i) throws SQLException;

    boolean isNull(int i) throws SQLException;
}
