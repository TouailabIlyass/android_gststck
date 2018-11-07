package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.ConnectionPropertiesImpl;
import com.mysql.jdbc.Messages;
import com.mysql.jdbc.NonRegisteringDriver;
import com.mysql.jdbc.SQLError;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Properties;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import javax.sql.DataSource;

public class MysqlDataSource extends ConnectionPropertiesImpl implements DataSource, Referenceable, Serializable {
    protected static final NonRegisteringDriver mysqlDriver;
    static final long serialVersionUID = -5515846944416881264L;
    protected String databaseName = null;
    protected String encoding = null;
    protected boolean explicitUrl = false;
    protected String hostName = null;
    protected transient PrintWriter logWriter = null;
    protected String password = null;
    protected int port = 3306;
    protected String profileSql = "false";
    protected String url = null;
    protected String user = null;

    static {
        try {
            mysqlDriver = new NonRegisteringDriver();
        } catch (Exception e) {
            throw new RuntimeException("Can not load Driver class com.mysql.jdbc.Driver");
        }
    }

    public Connection getConnection() throws SQLException {
        return getConnection(this.user, this.password);
    }

    public Connection getConnection(String userID, String pass) throws SQLException {
        Properties props = new Properties();
        if (userID != null) {
            props.setProperty(NonRegisteringDriver.USER_PROPERTY_KEY, userID);
        }
        if (pass != null) {
            props.setProperty(NonRegisteringDriver.PASSWORD_PROPERTY_KEY, pass);
        }
        exposeAsProperties(props);
        return getConnection(props);
    }

    public void setDatabaseName(String dbName) {
        this.databaseName = dbName;
    }

    public String getDatabaseName() {
        return this.databaseName != null ? this.databaseName : "";
    }

    public void setLogWriter(PrintWriter output) throws SQLException {
        this.logWriter = output;
    }

    public PrintWriter getLogWriter() {
        return this.logWriter;
    }

    public void setLoginTimeout(int seconds) throws SQLException {
    }

    public int getLoginTimeout() {
        return 0;
    }

    public void setPassword(String pass) {
        this.password = pass;
    }

    public void setPort(int p) {
        this.port = p;
    }

    public int getPort() {
        return this.port;
    }

    public void setPortNumber(int p) {
        setPort(p);
    }

    public int getPortNumber() {
        return getPort();
    }

    public void setPropertiesViaRef(Reference ref) throws SQLException {
        super.initializeFromRef(ref);
    }

    public Reference getReference() throws NamingException {
        Reference ref = new Reference(getClass().getName(), "com.mysql.jdbc.jdbc2.optional.MysqlDataSourceFactory", null);
        ref.add(new StringRefAddr(NonRegisteringDriver.USER_PROPERTY_KEY, getUser()));
        ref.add(new StringRefAddr(NonRegisteringDriver.PASSWORD_PROPERTY_KEY, this.password));
        ref.add(new StringRefAddr("serverName", getServerName()));
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("");
        stringBuilder.append(getPort());
        ref.add(new StringRefAddr("port", stringBuilder.toString()));
        ref.add(new StringRefAddr("databaseName", getDatabaseName()));
        ref.add(new StringRefAddr("url", getUrl()));
        ref.add(new StringRefAddr("explicitUrl", String.valueOf(this.explicitUrl)));
        try {
            storeToRef(ref);
            return ref;
        } catch (SQLException sqlEx) {
            throw new NamingException(sqlEx.getMessage());
        }
    }

    public void setServerName(String serverName) {
        this.hostName = serverName;
    }

    public String getServerName() {
        return this.hostName != null ? this.hostName : "";
    }

    public void setURL(String url) {
        setUrl(url);
    }

    public String getURL() {
        return getUrl();
    }

    public void setUrl(String url) {
        this.url = url;
        this.explicitUrl = true;
    }

    public String getUrl() {
        if (this.explicitUrl) {
            return this.url;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("jdbc:mysql://");
        stringBuilder.append(getServerName());
        stringBuilder.append(":");
        stringBuilder.append(getPort());
        stringBuilder.append("/");
        stringBuilder.append(getDatabaseName());
        return stringBuilder.toString();
    }

    public void setUser(String userID) {
        this.user = userID;
    }

    public String getUser() {
        return this.user;
    }

    protected Connection getConnection(Properties props) throws SQLException {
        String jdbcUrlToUse;
        if (this.explicitUrl) {
            jdbcUrlToUse = this.url;
        } else {
            StringBuilder jdbcUrl = new StringBuilder("jdbc:mysql://");
            if (this.hostName != null) {
                jdbcUrl.append(this.hostName);
            }
            jdbcUrl.append(":");
            jdbcUrl.append(this.port);
            jdbcUrl.append("/");
            if (this.databaseName != null) {
                jdbcUrl.append(this.databaseName);
            }
            jdbcUrlToUse = jdbcUrl.toString();
        }
        Properties urlProps = mysqlDriver.parseURL(jdbcUrlToUse, null);
        if (urlProps == null) {
            throw SQLError.createSQLException(Messages.getString("MysqlDataSource.BadUrl", new Object[]{jdbcUrlToUse}), SQLError.SQL_STATE_CONNECTION_FAILURE, null);
        }
        urlProps.remove(NonRegisteringDriver.DBNAME_PROPERTY_KEY);
        urlProps.remove(NonRegisteringDriver.HOST_PROPERTY_KEY);
        urlProps.remove(NonRegisteringDriver.PORT_PROPERTY_KEY);
        Iterator<Object> keys = urlProps.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            props.setProperty(key, urlProps.getProperty(key));
        }
        return mysqlDriver.connect(jdbcUrlToUse, props);
    }
}
