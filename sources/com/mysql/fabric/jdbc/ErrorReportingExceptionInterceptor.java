package com.mysql.fabric.jdbc;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ConnectionImpl;
import com.mysql.jdbc.ExceptionInterceptor;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.NonRegisteringDriver;
import com.mysql.jdbc.SQLError;
import java.sql.SQLException;
import java.util.Properties;

public class ErrorReportingExceptionInterceptor implements ExceptionInterceptor {
    private String fabricHaGroup;
    private String hostname;
    private String port;

    public SQLException interceptException(SQLException sqlEx, Connection conn) {
        MySQLConnection mysqlConn = (MySQLConnection) conn;
        if (ConnectionImpl.class.isAssignableFrom(mysqlConn.getMultiHostSafeProxy().getClass())) {
            return null;
        }
        try {
            return ((FabricMySQLConnectionProxy) mysqlConn.getMultiHostSafeProxy()).interceptException(sqlEx, conn, this.fabricHaGroup, this.hostname, this.port);
        } catch (Throwable ex) {
            return SQLError.createSQLException("Failed to report error to Fabric.", SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE, ex, null);
        }
    }

    public void init(Connection conn, Properties props) throws SQLException {
        this.hostname = props.getProperty(NonRegisteringDriver.HOST_PROPERTY_KEY);
        this.port = props.getProperty(NonRegisteringDriver.PORT_PROPERTY_KEY);
        this.fabricHaGroup = props.getProperty("connectionAttributes").replaceAll("^.*\\bfabricHaGroup:(.+)\\b.*$", "$1");
    }

    public void destroy() {
    }
}
