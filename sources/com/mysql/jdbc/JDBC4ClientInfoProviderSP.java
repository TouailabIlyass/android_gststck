package com.mysql.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;

public class JDBC4ClientInfoProviderSP implements JDBC4ClientInfoProvider {
    PreparedStatement getClientInfoBulkSp;
    PreparedStatement getClientInfoSp;
    PreparedStatement setClientInfoSp;

    public synchronized void initialize(Connection conn, Properties configurationProps) throws SQLException {
        String identifierQuote = conn.getMetaData().getIdentifierQuoteString();
        String setClientInfoSpName = configurationProps.getProperty("clientInfoSetSPName", "setClientInfo");
        String getClientInfoSpName = configurationProps.getProperty("clientInfoGetSPName", "getClientInfo");
        String getClientInfoBulkSpName = configurationProps.getProperty("clientInfoGetBulkSPName", "getClientInfoBulk");
        String clientInfoCatalog = configurationProps.getProperty("clientInfoCatalog", "");
        String catalog = "".equals(clientInfoCatalog) ? conn.getCatalog() : clientInfoCatalog;
        Connection connection = (Connection) conn;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("CALL ");
        stringBuilder.append(identifierQuote);
        stringBuilder.append(catalog);
        stringBuilder.append(identifierQuote);
        stringBuilder.append(".");
        stringBuilder.append(identifierQuote);
        stringBuilder.append(setClientInfoSpName);
        stringBuilder.append(identifierQuote);
        stringBuilder.append("(?, ?)");
        this.setClientInfoSp = connection.clientPrepareStatement(stringBuilder.toString());
        connection = (Connection) conn;
        stringBuilder = new StringBuilder();
        stringBuilder.append("CALL");
        stringBuilder.append(identifierQuote);
        stringBuilder.append(catalog);
        stringBuilder.append(identifierQuote);
        stringBuilder.append(".");
        stringBuilder.append(identifierQuote);
        stringBuilder.append(getClientInfoSpName);
        stringBuilder.append(identifierQuote);
        stringBuilder.append("(?)");
        this.getClientInfoSp = connection.clientPrepareStatement(stringBuilder.toString());
        connection = (Connection) conn;
        stringBuilder = new StringBuilder();
        stringBuilder.append("CALL ");
        stringBuilder.append(identifierQuote);
        stringBuilder.append(catalog);
        stringBuilder.append(identifierQuote);
        stringBuilder.append(".");
        stringBuilder.append(identifierQuote);
        stringBuilder.append(getClientInfoBulkSpName);
        stringBuilder.append(identifierQuote);
        stringBuilder.append("()");
        this.getClientInfoBulkSp = connection.clientPrepareStatement(stringBuilder.toString());
    }

    public synchronized void destroy() throws SQLException {
        if (this.setClientInfoSp != null) {
            this.setClientInfoSp.close();
            this.setClientInfoSp = null;
        }
        if (this.getClientInfoSp != null) {
            this.getClientInfoSp.close();
            this.getClientInfoSp = null;
        }
        if (this.getClientInfoBulkSp != null) {
            this.getClientInfoBulkSp.close();
            this.getClientInfoBulkSp = null;
        }
    }

    public synchronized Properties getClientInfo(Connection conn) throws SQLException {
        Properties props;
        ResultSet rs = null;
        props = new Properties();
        try {
            this.getClientInfoBulkSp.execute();
            rs = this.getClientInfoBulkSp.getResultSet();
            while (rs.next()) {
                props.setProperty(rs.getString(1), rs.getString(2));
            }
            if (rs != null) {
                rs.close();
            }
        } catch (Throwable th) {
            if (rs != null) {
                rs.close();
            }
        }
        return props;
    }

    public synchronized String getClientInfo(Connection conn, String name) throws SQLException {
        String clientInfo;
        ResultSet rs = null;
        clientInfo = null;
        try {
            this.getClientInfoSp.setString(1, name);
            this.getClientInfoSp.execute();
            rs = this.getClientInfoSp.getResultSet();
            if (rs.next()) {
                clientInfo = rs.getString(1);
            }
            if (rs != null) {
                rs.close();
            }
        } catch (Throwable th) {
            if (rs != null) {
                rs.close();
            }
        }
        return clientInfo;
    }

    public synchronized void setClientInfo(Connection conn, Properties properties) throws SQLClientInfoException {
        try {
            Enumeration<?> propNames = properties.propertyNames();
            while (propNames.hasMoreElements()) {
                String name = (String) propNames.nextElement();
                setClientInfo(conn, name, properties.getProperty(name));
            }
        } catch (SQLException sqlEx) {
            SQLClientInfoException clientInfoEx = new SQLClientInfoException();
            clientInfoEx.initCause(sqlEx);
            throw clientInfoEx;
        }
    }

    public synchronized void setClientInfo(Connection conn, String name, String value) throws SQLClientInfoException {
        try {
            this.setClientInfoSp.setString(1, name);
            this.setClientInfoSp.setString(2, value);
            this.setClientInfoSp.execute();
        } catch (SQLException sqlEx) {
            SQLClientInfoException clientInfoEx = new SQLClientInfoException();
            clientInfoEx.initCause(sqlEx);
            throw clientInfoEx;
        }
    }
}
