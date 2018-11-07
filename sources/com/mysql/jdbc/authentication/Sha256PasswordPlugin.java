package com.mysql.jdbc.authentication;

import com.mysql.jdbc.AuthenticationPlugin;
import com.mysql.jdbc.Buffer;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ExportControlled;
import com.mysql.jdbc.Messages;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.Security;
import com.mysql.jdbc.StringUtils;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class Sha256PasswordPlugin implements AuthenticationPlugin {
    public static String PLUGIN_NAME = "sha256_password";
    private Connection connection;
    private String password = null;
    private boolean publicKeyRequested = false;
    private String publicKeyString = null;
    private String seed = null;

    public void init(Connection conn, Properties props) throws SQLException {
        this.connection = conn;
        String pkURL = this.connection.getServerRSAPublicKeyFile();
        if (pkURL != null) {
            this.publicKeyString = readRSAKey(this.connection, pkURL);
        }
    }

    public void destroy() {
        this.password = null;
        this.seed = null;
        this.publicKeyRequested = false;
    }

    public String getProtocolPluginName() {
        return PLUGIN_NAME;
    }

    public boolean requiresConfidentiality() {
        return false;
    }

    public boolean isReusable() {
        return true;
    }

    public void setAuthenticationParameters(String user, String password) {
        this.password = password;
    }

    public boolean nextAuthenticationStep(Buffer fromServer, List<Buffer> toServer) throws SQLException {
        toServer.clear();
        if (!(this.password == null || this.password.length() == 0)) {
            if (fromServer != null) {
                if (((MySQLConnection) this.connection).getIO().isSSLEstablished()) {
                    try {
                        Buffer bresp = new Buffer(StringUtils.getBytes(this.password, this.connection.getPasswordCharacterEncoding()));
                        bresp.setPosition(bresp.getBufLength());
                        int oldBufLength = bresp.getBufLength();
                        bresp.writeByte((byte) 0);
                        bresp.setBufLength(oldBufLength + 1);
                        bresp.setPosition(0);
                        toServer.add(bresp);
                    } catch (UnsupportedEncodingException e) {
                        throw SQLError.createSQLException(Messages.getString("Sha256PasswordPlugin.3", new Object[]{this.connection.getPasswordCharacterEncoding()}), SQLError.SQL_STATE_GENERAL_ERROR, null);
                    }
                } else if (this.connection.getServerRSAPublicKeyFile() != null) {
                    this.seed = fromServer.readString();
                    toServer.add(new Buffer(encryptPassword(this.password, this.seed, this.connection, this.publicKeyString)));
                } else if (!this.connection.getAllowPublicKeyRetrieval()) {
                    throw SQLError.createSQLException(Messages.getString("Sha256PasswordPlugin.2"), SQLError.SQL_STATE_UNABLE_TO_CONNECT_TO_DATASOURCE, this.connection.getExceptionInterceptor());
                } else if (!this.publicKeyRequested || fromServer.getBufLength() <= 20) {
                    this.seed = fromServer.readString();
                    toServer.add(new Buffer(new byte[]{(byte) 1}));
                    this.publicKeyRequested = true;
                } else {
                    toServer.add(new Buffer(encryptPassword(this.password, this.seed, this.connection, fromServer.readString())));
                    this.publicKeyRequested = false;
                }
                return true;
            }
        }
        toServer.add(new Buffer(new byte[]{(byte) 0}));
        return true;
    }

    private static byte[] encryptPassword(String password, String seed, Connection connection, String key) throws SQLException {
        UnsupportedEncodingException e;
        byte[] input = null;
        if (password != null) {
            try {
                e = StringUtils.getBytesNullTerminated(password, connection.getPasswordCharacterEncoding());
            } catch (UnsupportedEncodingException e2) {
                throw SQLError.createSQLException(Messages.getString("Sha256PasswordPlugin.3", new Object[]{connection.getPasswordCharacterEncoding()}), SQLError.SQL_STATE_GENERAL_ERROR, null);
            }
        }
        e = new byte[]{null};
        byte[] input2 = e;
        input = new byte[input2.length];
        Security.xorString(input2, input, seed.getBytes(), input2.length);
        return ExportControlled.encryptWithRSAPublicKey(input, ExportControlled.decodeRSAPublicKey(key, ((MySQLConnection) connection).getExceptionInterceptor()), ((MySQLConnection) connection).getExceptionInterceptor());
    }

    private static String readRSAKey(Connection connection, String pkPath) throws SQLException {
        StringBuilder sb;
        byte[] fileBuf = new byte[2048];
        BufferedInputStream fileIn = null;
        try {
            fileIn = new BufferedInputStream(new FileInputStream(new File(pkPath).getCanonicalPath()));
            sb = new StringBuilder();
            while (true) {
                int read = fileIn.read(fileBuf);
                int bytesRead = read;
                if (read == -1) {
                    break;
                }
                sb.append(StringUtils.toAsciiString(fileBuf, 0, bytesRead));
            }
            String res = sb.toString();
            if (fileIn != null) {
                try {
                    fileIn.close();
                } catch (Throwable ex) {
                    throw SQLError.createSQLException(Messages.getString("Sha256PasswordPlugin.1"), SQLError.SQL_STATE_GENERAL_ERROR, ex, connection.getExceptionInterceptor());
                }
            }
            return res;
        } catch (Throwable ioEx) {
            if (connection.getParanoid()) {
                throw SQLError.createSQLException(Messages.getString("Sha256PasswordPlugin.0", new Object[]{""}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, connection.getExceptionInterceptor());
            }
            Object[] objArr = new Object[1];
            sb = new StringBuilder();
            sb.append("'");
            sb.append(pkPath);
            sb.append("'");
            objArr[0] = sb.toString();
            throw SQLError.createSQLException(Messages.getString("Sha256PasswordPlugin.0", objArr), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, ioEx, connection.getExceptionInterceptor());
        } catch (Throwable th) {
            if (fileIn != null) {
                try {
                    fileIn.close();
                } catch (Throwable ex2) {
                    throw SQLError.createSQLException(Messages.getString("Sha256PasswordPlugin.1"), SQLError.SQL_STATE_GENERAL_ERROR, ex2, connection.getExceptionInterceptor());
                }
            }
        }
    }
}
