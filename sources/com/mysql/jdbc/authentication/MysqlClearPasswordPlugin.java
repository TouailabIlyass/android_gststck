package com.mysql.jdbc.authentication;

import com.mysql.jdbc.AuthenticationPlugin;
import com.mysql.jdbc.Buffer;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Messages;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.StringUtils;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class MysqlClearPasswordPlugin implements AuthenticationPlugin {
    private Connection connection;
    private String password = null;

    public void init(Connection conn, Properties props) throws SQLException {
        this.connection = conn;
    }

    public void destroy() {
        this.password = null;
    }

    public String getProtocolPluginName() {
        return "mysql_clear_password";
    }

    public boolean requiresConfidentiality() {
        return true;
    }

    public boolean isReusable() {
        return true;
    }

    public void setAuthenticationParameters(String user, String password) {
        this.password = password;
    }

    public boolean nextAuthenticationStep(Buffer fromServer, List<Buffer> toServer) throws SQLException {
        toServer.clear();
        try {
            Buffer bresp = new Buffer(StringUtils.getBytes(this.password != null ? this.password : "", this.connection.versionMeetsMinimum(5, 7, 6) ? this.connection.getPasswordCharacterEncoding() : "UTF-8"));
            bresp.setPosition(bresp.getBufLength());
            int oldBufLength = bresp.getBufLength();
            bresp.writeByte((byte) 0);
            bresp.setBufLength(oldBufLength + 1);
            bresp.setPosition(0);
            toServer.add(bresp);
            return true;
        } catch (UnsupportedEncodingException e) {
            throw SQLError.createSQLException(Messages.getString("MysqlClearPasswordPlugin.1", new Object[]{this.connection.getPasswordCharacterEncoding()}), SQLError.SQL_STATE_GENERAL_ERROR, null);
        }
    }
}
