package com.mysql.jdbc;

import java.sql.SQLException;

public class CommunicationsException extends SQLException implements StreamingNotifiable {
    static final long serialVersionUID = 3193864990663398317L;
    private String exceptionMessage = null;

    public CommunicationsException(MySQLConnection conn, long lastPacketSentTimeMs, long lastPacketReceivedTimeMs, Exception underlyingException) {
        this.exceptionMessage = SQLError.createLinkFailureMessageBasedOnHeuristics(conn, lastPacketSentTimeMs, lastPacketReceivedTimeMs, underlyingException);
        if (underlyingException != null) {
            initCause(underlyingException);
        }
    }

    public String getMessage() {
        return this.exceptionMessage;
    }

    public String getSQLState() {
        return SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE;
    }

    public void setWasStreamingResults() {
        this.exceptionMessage = Messages.getString("CommunicationsException.ClientWasStreaming");
    }
}
