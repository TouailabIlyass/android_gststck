package com.mysql.fabric.jdbc;

import com.mysql.jdbc.NonRegisteringDriver;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Properties;

public class FabricMySQLDataSource extends MysqlDataSource implements FabricMySQLConnectionProperties {
    private static final Driver driver;
    private static final long serialVersionUID = 1;
    private String fabricPassword;
    private String fabricProtocol = "http";
    private boolean fabricReportErrors = false;
    private String fabricServerGroup;
    private String fabricShardKey;
    private String fabricShardTable;
    private String fabricUsername;

    static {
        try {
            driver = new FabricMySQLDriver();
        } catch (Exception ex) {
            throw new RuntimeException("Can create driver", ex);
        }
    }

    protected Connection getConnection(Properties props) throws SQLException {
        String jdbcUrlToUse;
        if (this.explicitUrl) {
            jdbcUrlToUse = this.url;
        } else {
            StringBuilder jdbcUrl = new StringBuilder(FabricMySQLDriver.FABRIC_URL_PREFIX);
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
        Properties urlProps = ((FabricMySQLDriver) driver).parseFabricURL(jdbcUrlToUse, null);
        urlProps.remove(NonRegisteringDriver.DBNAME_PROPERTY_KEY);
        urlProps.remove(NonRegisteringDriver.HOST_PROPERTY_KEY);
        urlProps.remove(NonRegisteringDriver.PORT_PROPERTY_KEY);
        Iterator<Object> keys = urlProps.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            props.setProperty(key, urlProps.getProperty(key));
        }
        if (this.fabricShardKey != null) {
            props.setProperty(FabricMySQLDriver.FABRIC_SHARD_KEY_PROPERTY_KEY, this.fabricShardKey);
        }
        if (this.fabricShardTable != null) {
            props.setProperty(FabricMySQLDriver.FABRIC_SHARD_TABLE_PROPERTY_KEY, this.fabricShardTable);
        }
        if (this.fabricServerGroup != null) {
            props.setProperty(FabricMySQLDriver.FABRIC_SERVER_GROUP_PROPERTY_KEY, this.fabricServerGroup);
        }
        props.setProperty(FabricMySQLDriver.FABRIC_PROTOCOL_PROPERTY_KEY, this.fabricProtocol);
        if (this.fabricUsername != null) {
            props.setProperty(FabricMySQLDriver.FABRIC_USERNAME_PROPERTY_KEY, this.fabricUsername);
        }
        if (this.fabricPassword != null) {
            props.setProperty(FabricMySQLDriver.FABRIC_PASSWORD_PROPERTY_KEY, this.fabricPassword);
        }
        props.setProperty(FabricMySQLDriver.FABRIC_REPORT_ERRORS_PROPERTY_KEY, Boolean.toString(this.fabricReportErrors));
        return driver.connect(jdbcUrlToUse, props);
    }

    public void setFabricShardKey(String value) {
        this.fabricShardKey = value;
    }

    public String getFabricShardKey() {
        return this.fabricShardKey;
    }

    public void setFabricShardTable(String value) {
        this.fabricShardTable = value;
    }

    public String getFabricShardTable() {
        return this.fabricShardTable;
    }

    public void setFabricServerGroup(String value) {
        this.fabricServerGroup = value;
    }

    public String getFabricServerGroup() {
        return this.fabricServerGroup;
    }

    public void setFabricProtocol(String value) {
        this.fabricProtocol = value;
    }

    public String getFabricProtocol() {
        return this.fabricProtocol;
    }

    public void setFabricUsername(String value) {
        this.fabricUsername = value;
    }

    public String getFabricUsername() {
        return this.fabricUsername;
    }

    public void setFabricPassword(String value) {
        this.fabricPassword = value;
    }

    public String getFabricPassword() {
        return this.fabricPassword;
    }

    public void setFabricReportErrors(boolean value) {
        this.fabricReportErrors = value;
    }

    public boolean getFabricReportErrors() {
        return this.fabricReportErrors;
    }
}
