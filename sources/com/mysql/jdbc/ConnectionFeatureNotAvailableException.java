package com.mysql.jdbc;

public class ConnectionFeatureNotAvailableException extends CommunicationsException {
    static final long serialVersionUID = -5065030488729238287L;

    public ConnectionFeatureNotAvailableException(MySQLConnection conn, long lastPacketSentTimeMs, Exception underlyingException) {
        super(conn, lastPacketSentTimeMs, 0, underlyingException);
    }

    public String getMessage() {
        return "Feature not available in this distribution of Connector/J";
    }

    public String getSQLState() {
        return SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE;
    }
}
