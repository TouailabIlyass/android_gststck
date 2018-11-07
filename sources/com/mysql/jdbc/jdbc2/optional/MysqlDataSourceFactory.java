package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.NonRegisteringDriver;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

public class MysqlDataSourceFactory implements ObjectFactory {
    protected static final String DATA_SOURCE_CLASS_NAME = "com.mysql.jdbc.jdbc2.optional.MysqlDataSource";
    protected static final String POOL_DATA_SOURCE_CLASS_NAME = "com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource";
    protected static final String XA_DATA_SOURCE_CLASS_NAME = "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource";

    public Object getObjectInstance(Object refObj, Name nm, Context ctx, Hashtable<?, ?> hashtable) throws Exception {
        Reference ref = (Reference) refObj;
        String className = ref.getClassName();
        if (className == null || (!className.equals(DATA_SOURCE_CLASS_NAME) && !className.equals(POOL_DATA_SOURCE_CLASS_NAME) && !className.equals(XA_DATA_SOURCE_CLASS_NAME))) {
            return null;
        }
        try {
            MysqlDataSource dataSource = (MysqlDataSource) Class.forName(className).newInstance();
            int portNumber = 3306;
            String portNumberAsString = nullSafeRefAddrStringGet("port", ref);
            if (portNumberAsString != null) {
                portNumber = Integer.parseInt(portNumberAsString);
            }
            dataSource.setPort(portNumber);
            String user = nullSafeRefAddrStringGet(NonRegisteringDriver.USER_PROPERTY_KEY, ref);
            if (user != null) {
                dataSource.setUser(user);
            }
            String password = nullSafeRefAddrStringGet(NonRegisteringDriver.PASSWORD_PROPERTY_KEY, ref);
            if (password != null) {
                dataSource.setPassword(password);
            }
            String serverName = nullSafeRefAddrStringGet("serverName", ref);
            if (serverName != null) {
                dataSource.setServerName(serverName);
            }
            String databaseName = nullSafeRefAddrStringGet("databaseName", ref);
            if (databaseName != null) {
                dataSource.setDatabaseName(databaseName);
            }
            String explicitUrlAsString = nullSafeRefAddrStringGet("explicitUrl", ref);
            if (explicitUrlAsString != null && Boolean.valueOf(explicitUrlAsString).booleanValue()) {
                dataSource.setUrl(nullSafeRefAddrStringGet("url", ref));
            }
            dataSource.setPropertiesViaRef(ref);
            return dataSource;
        } catch (Exception ex) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Unable to create DataSource of class '");
            stringBuilder.append(className);
            stringBuilder.append("', reason: ");
            stringBuilder.append(ex.toString());
            throw new RuntimeException(stringBuilder.toString());
        }
    }

    private String nullSafeRefAddrStringGet(String referenceName, Reference ref) {
        RefAddr refAddr = ref.get(referenceName);
        return refAddr != null ? (String) refAddr.getContent() : null;
    }
}
