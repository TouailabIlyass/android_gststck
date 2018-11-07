package com.mysql.jdbc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

public class NonRegisteringDriver implements Driver {
    private static final String ALLOWED_QUOTES = "\"'";
    public static final String DBNAME_PROPERTY_KEY = "DBNAME";
    public static final boolean DEBUG = false;
    public static final int HOST_NAME_INDEX = 0;
    public static final String HOST_PROPERTY_KEY = "HOST";
    public static final String LICENSE = "GPL";
    public static final String LOADBALANCE_URL_PREFIX = "jdbc:mysql:loadbalance://";
    private static final String MXJ_URL_PREFIX = "jdbc:mysql:mxj://";
    public static final String NAME = "MySQL Connector Java";
    public static final String NUM_HOSTS_PROPERTY_KEY = "NUM_HOSTS";
    public static final String OS = getOSName();
    public static final String PASSWORD_PROPERTY_KEY = "password";
    public static final String PATH_PROPERTY_KEY = "PATH";
    public static final String PLATFORM = getPlatform();
    public static final int PORT_NUMBER_INDEX = 1;
    public static final String PORT_PROPERTY_KEY = "PORT";
    public static final String PROPERTIES_TRANSFORM_KEY = "propertiesTransform";
    public static final String PROTOCOL_PROPERTY_KEY = "PROTOCOL";
    private static final String REPLICATION_URL_PREFIX = "jdbc:mysql:replication://";
    public static final String RUNTIME_VENDOR = System.getProperty("java.vendor");
    public static final String RUNTIME_VERSION = System.getProperty("java.version");
    public static final boolean TRACE = false;
    private static final String URL_PREFIX = "jdbc:mysql://";
    public static final String USER_PROPERTY_KEY = "user";
    public static final String USE_CONFIG_PROPERTY_KEY = "useConfigs";
    public static final String VERSION = "5.1.45";
    protected static final ConcurrentHashMap<ConnectionPhantomReference, ConnectionPhantomReference> connectionPhantomRefs = new ConcurrentHashMap();
    protected static final ReferenceQueue<ConnectionImpl> refQueue = new ReferenceQueue();

    static class ConnectionPhantomReference extends PhantomReference<ConnectionImpl> {
        private NetworkResources io;

        ConnectionPhantomReference(ConnectionImpl connectionImpl, ReferenceQueue<ConnectionImpl> q) {
            super(connectionImpl, q);
            try {
                this.io = connectionImpl.getIO().getNetworkResources();
            } catch (SQLException e) {
            }
        }

        void cleanup() {
            if (this.io != null) {
                try {
                    this.io.forceClose();
                } finally {
                    this.io = null;
                }
            }
        }
    }

    static {
        try {
            Class.forName(AbandonedConnectionCleanupThread.class.getName());
        } catch (ClassNotFoundException e) {
        }
    }

    public static String getOSName() {
        return System.getProperty("os.name");
    }

    public static String getPlatform() {
        return System.getProperty("os.arch");
    }

    static int getMajorVersionInternal() {
        return safeIntParse("5");
    }

    static int getMinorVersionInternal() {
        return safeIntParse("1");
    }

    protected static String[] parseHostPortPair(String hostPortPair) throws SQLException {
        String[] splitValues = new String[2];
        if (StringUtils.startsWithIgnoreCaseAndWs(hostPortPair, "address=")) {
            splitValues[0] = hostPortPair.trim();
            splitValues[1] = null;
            return splitValues;
        }
        int portIndex = hostPortPair.indexOf(":");
        if (portIndex == -1) {
            splitValues[0] = hostPortPair;
            splitValues[1] = null;
        } else if (portIndex + 1 < hostPortPair.length()) {
            String portAsString = hostPortPair.substring(portIndex + 1);
            splitValues[0] = hostPortPair.substring(0, portIndex);
            splitValues[1] = portAsString;
        } else {
            throw SQLError.createSQLException(Messages.getString("NonRegisteringDriver.37"), SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE, null);
        }
        return splitValues;
    }

    private static int safeIntParse(String intAsString) {
        try {
            return Integer.parseInt(intAsString);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean acceptsURL(String url) throws SQLException {
        if (url != null) {
            return parseURL(url, null) != null;
        } else {
            throw SQLError.createSQLException(Messages.getString("NonRegisteringDriver.1"), SQLError.SQL_STATE_UNABLE_TO_CONNECT_TO_DATASOURCE, null);
        }
    }

    public Connection connect(String url, Properties info) throws SQLException {
        SQLException sqlEx;
        if (url == null) {
            throw SQLError.createSQLException(Messages.getString("NonRegisteringDriver.1"), SQLError.SQL_STATE_UNABLE_TO_CONNECT_TO_DATASOURCE, null);
        } else if (StringUtils.startsWithIgnoreCase(url, LOADBALANCE_URL_PREFIX)) {
            return connectLoadBalanced(url, info);
        } else {
            if (StringUtils.startsWithIgnoreCase(url, REPLICATION_URL_PREFIX)) {
                return connectReplicationConnection(url, info);
            }
            Properties parseURL = parseURL(url, info);
            Properties props = parseURL;
            if (parseURL == null) {
                return null;
            }
            if (!"1".equals(props.getProperty(NUM_HOSTS_PROPERTY_KEY))) {
                return connectFailover(url, info);
            }
            try {
                return ConnectionImpl.getInstance(host(props), port(props), props, database(props), url);
            } catch (SQLException sqlEx2) {
                throw sqlEx2;
            } catch (Exception ex) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(Messages.getString("NonRegisteringDriver.17"));
                stringBuilder.append(ex.toString());
                stringBuilder.append(Messages.getString("NonRegisteringDriver.18"));
                sqlEx2 = SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_UNABLE_TO_CONNECT_TO_DATASOURCE, null);
                sqlEx2.initCause(ex);
                throw sqlEx2;
            }
        }
    }

    protected static void trackConnection(Connection newConn) {
        ConnectionPhantomReference phantomRef = new ConnectionPhantomReference((ConnectionImpl) newConn, refQueue);
        connectionPhantomRefs.put(phantomRef, phantomRef);
    }

    private Connection connectLoadBalanced(String url, Properties info) throws SQLException {
        Properties parsedProps = parseURL(url, info);
        if (parsedProps == null) {
            return null;
        }
        parsedProps.remove("roundRobinLoadBalance");
        int numHosts = Integer.parseInt(parsedProps.getProperty(NUM_HOSTS_PROPERTY_KEY));
        List<String> hostList = new ArrayList();
        for (int i = 0; i < numHosts; i++) {
            int index = i + 1;
            StringBuilder stringBuilder = new StringBuilder();
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("HOST.");
            stringBuilder2.append(index);
            stringBuilder.append(parsedProps.getProperty(stringBuilder2.toString()));
            stringBuilder.append(":");
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append("PORT.");
            stringBuilder2.append(index);
            stringBuilder.append(parsedProps.getProperty(stringBuilder2.toString()));
            hostList.add(stringBuilder.toString());
        }
        return LoadBalancedConnectionProxy.createProxyInstance(hostList, parsedProps);
    }

    private Connection connectFailover(String url, Properties info) throws SQLException {
        Properties parsedProps = parseURL(url, info);
        if (parsedProps == null) {
            return null;
        }
        parsedProps.remove("roundRobinLoadBalance");
        int numHosts = Integer.parseInt(parsedProps.getProperty(NUM_HOSTS_PROPERTY_KEY));
        List<String> hostList = new ArrayList();
        for (int i = 0; i < numHosts; i++) {
            int index = i + 1;
            StringBuilder stringBuilder = new StringBuilder();
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("HOST.");
            stringBuilder2.append(index);
            stringBuilder.append(parsedProps.getProperty(stringBuilder2.toString()));
            stringBuilder.append(":");
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append("PORT.");
            stringBuilder2.append(index);
            stringBuilder.append(parsedProps.getProperty(stringBuilder2.toString()));
            hostList.add(stringBuilder.toString());
        }
        return FailoverConnectionProxy.createProxyInstance(hostList, parsedProps);
    }

    protected Connection connectReplicationConnection(String url, Properties info) throws SQLException {
        Properties parsedProps = parseURL(url, info);
        if (parsedProps == null) {
            return null;
        }
        Properties masterProps = (Properties) parsedProps.clone();
        Properties slavesProps = (Properties) parsedProps.clone();
        slavesProps.setProperty("com.mysql.jdbc.ReplicationConnection.isSlave", "true");
        int numHosts = Integer.parseInt(parsedProps.getProperty(NUM_HOSTS_PROPERTY_KEY));
        if (numHosts < 2) {
            throw SQLError.createSQLException("Must specify at least one slave host to connect to for master/slave replication load-balancing functionality", SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE, null);
        }
        NonRegisteringDriver nonRegisteringDriver;
        List<String> slaveHostList = new ArrayList();
        List<String> masterHostList = new ArrayList();
        String firstHost = new StringBuilder();
        firstHost.append(masterProps.getProperty("HOST.1"));
        firstHost.append(":");
        firstHost.append(masterProps.getProperty("PORT.1"));
        boolean usesExplicitServerType = isHostPropertiesList(firstHost.toString());
        for (int i = 0; i < numHosts; i++) {
            int index = i + 1;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("HOST.");
            stringBuilder.append(index);
            masterProps.remove(stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append("PORT.");
            stringBuilder.append(index);
            masterProps.remove(stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append("HOST.");
            stringBuilder.append(index);
            slavesProps.remove(stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append("PORT.");
            stringBuilder.append(index);
            slavesProps.remove(stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append("HOST.");
            stringBuilder.append(index);
            String host = parsedProps.getProperty(stringBuilder.toString());
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("PORT.");
            stringBuilder2.append(index);
            String port = parsedProps.getProperty(stringBuilder2.toString());
            if (!usesExplicitServerType) {
                nonRegisteringDriver = this;
                StringBuilder stringBuilder3;
                if (i == 0) {
                    stringBuilder3 = new StringBuilder();
                    stringBuilder3.append(host);
                    stringBuilder3.append(":");
                    stringBuilder3.append(port);
                    masterHostList.add(stringBuilder3.toString());
                } else {
                    stringBuilder3 = new StringBuilder();
                    stringBuilder3.append(host);
                    stringBuilder3.append(":");
                    stringBuilder3.append(port);
                    slaveHostList.add(stringBuilder3.toString());
                }
            } else if (isHostMaster(host)) {
                masterHostList.add(host);
            } else {
                slaveHostList.add(host);
            }
        }
        nonRegisteringDriver = this;
        slavesProps.remove(NUM_HOSTS_PROPERTY_KEY);
        masterProps.remove(NUM_HOSTS_PROPERTY_KEY);
        masterProps.remove(HOST_PROPERTY_KEY);
        masterProps.remove(PORT_PROPERTY_KEY);
        slavesProps.remove(HOST_PROPERTY_KEY);
        slavesProps.remove(PORT_PROPERTY_KEY);
        return ReplicationConnectionProxy.createProxyInstance(masterHostList, masterProps, slaveHostList, slavesProps);
    }

    private boolean isHostMaster(String host) {
        if (isHostPropertiesList(host)) {
            Properties hostSpecificProps = expandHostKeyValues(host);
            if (hostSpecificProps.containsKey("type") && "master".equalsIgnoreCase(hostSpecificProps.get("type").toString())) {
                return true;
            }
        }
        return false;
    }

    public String database(Properties props) {
        return props.getProperty(DBNAME_PROPERTY_KEY);
    }

    public int getMajorVersion() {
        return getMajorVersionInternal();
    }

    public int getMinorVersion() {
        return getMinorVersionInternal();
    }

    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        if (info == null) {
            info = new Properties();
        }
        if (url != null && url.startsWith(URL_PREFIX)) {
            info = parseURL(url, info);
        }
        DriverPropertyInfo hostProp = new DriverPropertyInfo(HOST_PROPERTY_KEY, info.getProperty(HOST_PROPERTY_KEY));
        hostProp.required = true;
        hostProp.description = Messages.getString("NonRegisteringDriver.3");
        DriverPropertyInfo portProp = new DriverPropertyInfo(PORT_PROPERTY_KEY, info.getProperty(PORT_PROPERTY_KEY, "3306"));
        portProp.required = false;
        portProp.description = Messages.getString("NonRegisteringDriver.7");
        DriverPropertyInfo dbProp = new DriverPropertyInfo(DBNAME_PROPERTY_KEY, info.getProperty(DBNAME_PROPERTY_KEY));
        dbProp.required = false;
        dbProp.description = "Database name";
        DriverPropertyInfo userProp = new DriverPropertyInfo(USER_PROPERTY_KEY, info.getProperty(USER_PROPERTY_KEY));
        userProp.required = true;
        userProp.description = Messages.getString("NonRegisteringDriver.13");
        DriverPropertyInfo passwordProp = new DriverPropertyInfo(PASSWORD_PROPERTY_KEY, info.getProperty(PASSWORD_PROPERTY_KEY));
        passwordProp.required = true;
        passwordProp.description = Messages.getString("NonRegisteringDriver.16");
        DriverPropertyInfo[] dpi = ConnectionPropertiesImpl.exposeAsDriverPropertyInfo(info, 5);
        dpi[0] = hostProp;
        dpi[1] = portProp;
        dpi[2] = dbProp;
        dpi[3] = userProp;
        dpi[4] = passwordProp;
        return dpi;
    }

    public String host(Properties props) {
        return props.getProperty(HOST_PROPERTY_KEY, "localhost");
    }

    public boolean jdbcCompliant() {
        return false;
    }

    public Properties parseURL(String url, Properties defaults) throws SQLException {
        String url2 = url;
        Properties properties = defaults;
        Properties urlProps = properties != null ? new Properties(properties) : new Properties();
        if (url2 == null) {
            return null;
        }
        if (!StringUtils.startsWithIgnoreCase(url2, URL_PREFIX) && !StringUtils.startsWithIgnoreCase(url2, MXJ_URL_PREFIX) && !StringUtils.startsWithIgnoreCase(url2, LOADBALANCE_URL_PREFIX) && !StringUtils.startsWithIgnoreCase(url2, REPLICATION_URL_PREFIX)) {
            return null;
        }
        String parameter;
        String value;
        String hostStuff;
        StringBuilder stringBuilder;
        String configs;
        int beginningOfSlashes = url2.indexOf("//");
        if (StringUtils.startsWithIgnoreCase(url2, MXJ_URL_PREFIX)) {
            urlProps.setProperty("socketFactory", "com.mysql.management.driverlaunched.ServerLauncherSocketFactory");
        }
        int index = url2.indexOf("?");
        int i = 0;
        if (index != -1) {
            String paramString = url2.substring(index + 1, url.length());
            url2 = url2.substring(0, index);
            StringTokenizer queryParams = new StringTokenizer(paramString, "&");
            while (queryParams.hasMoreTokens()) {
                String parameterValuePair = queryParams.nextToken();
                int indexOfEquals = StringUtils.indexOfIgnoreCase(0, parameterValuePair, "=");
                parameter = null;
                value = null;
                if (indexOfEquals != -1) {
                    parameter = parameterValuePair.substring(0, indexOfEquals);
                    if (indexOfEquals + 1 < parameterValuePair.length()) {
                        value = parameterValuePair.substring(indexOfEquals + 1);
                    }
                }
                if (value != null && value.length() > 0 && parameter != null && parameter.length() > 0) {
                    try {
                        urlProps.setProperty(parameter, URLDecoder.decode(value, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        UnsupportedEncodingException badEncoding = e;
                        urlProps.setProperty(parameter, URLDecoder.decode(value));
                    } catch (NoSuchMethodError e2) {
                        NoSuchMethodError nsme = e2;
                        urlProps.setProperty(parameter, URLDecoder.decode(value));
                    }
                }
            }
        }
        url2 = url2.substring(beginningOfSlashes + 2);
        int slashIndex = StringUtils.indexOfIgnoreCase(0, url2, "/", ALLOWED_QUOTES, ALLOWED_QUOTES, StringUtils.SEARCH_MODE__ALL);
        if (slashIndex != -1) {
            hostStuff = url2.substring(0, slashIndex);
            if (slashIndex + 1 < url2.length()) {
                urlProps.put(DBNAME_PROPERTY_KEY, url2.substring(slashIndex + 1, url2.length()));
            }
        } else {
            hostStuff = url2;
        }
        int numHosts = 0;
        if (hostStuff == null || hostStuff.trim().length() <= 0) {
            numHosts = 1;
            urlProps.setProperty("HOST.1", "localhost");
            urlProps.setProperty("PORT.1", "3306");
        } else {
            for (String parameter2 : StringUtils.split(hostStuff, ",", ALLOWED_QUOTES, ALLOWED_QUOTES, false)) {
                StringBuilder stringBuilder2;
                numHosts++;
                String[] hostPortPair = parseHostPortPair(parameter2);
                if (hostPortPair[i] == null || hostPortPair[i].trim().length() <= 0) {
                    StringBuilder stringBuilder3 = new StringBuilder();
                    stringBuilder3.append("HOST.");
                    stringBuilder3.append(numHosts);
                    urlProps.setProperty(stringBuilder3.toString(), "localhost");
                } else {
                    stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("HOST.");
                    stringBuilder2.append(numHosts);
                    urlProps.setProperty(stringBuilder2.toString(), hostPortPair[i]);
                }
                if (hostPortPair[1] != null) {
                    stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("PORT.");
                    stringBuilder2.append(numHosts);
                    urlProps.setProperty(stringBuilder2.toString(), hostPortPair[1]);
                } else {
                    StringBuilder stringBuilder4 = new StringBuilder();
                    stringBuilder4.append("PORT.");
                    stringBuilder4.append(numHosts);
                    urlProps.setProperty(stringBuilder4.toString(), "3306");
                }
                i = 0;
            }
        }
        urlProps.setProperty(NUM_HOSTS_PROPERTY_KEY, String.valueOf(numHosts));
        urlProps.setProperty(HOST_PROPERTY_KEY, urlProps.getProperty("HOST.1"));
        urlProps.setProperty(PORT_PROPERTY_KEY, urlProps.getProperty("PORT.1"));
        String propertiesTransformClassName = urlProps.getProperty(PROPERTIES_TRANSFORM_KEY);
        if (propertiesTransformClassName != null) {
            try {
                urlProps = ((ConnectionPropertiesTransform) Class.forName(propertiesTransformClassName).newInstance()).transformProperties(urlProps);
            } catch (InstantiationException e3) {
                InstantiationException e4 = e3;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Unable to create properties transform instance '");
                stringBuilder.append(propertiesTransformClassName);
                stringBuilder.append("' due to underlying exception: ");
                stringBuilder.append(e4.toString());
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE, null);
            } catch (IllegalAccessException e5) {
                IllegalAccessException e6 = e5;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Unable to create properties transform instance '");
                stringBuilder.append(propertiesTransformClassName);
                stringBuilder.append("' due to underlying exception: ");
                stringBuilder.append(e6.toString());
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE, null);
            } catch (ClassNotFoundException e7) {
                ClassNotFoundException e8 = e7;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Unable to create properties transform instance '");
                stringBuilder.append(propertiesTransformClassName);
                stringBuilder.append("' due to underlying exception: ");
                stringBuilder.append(e8.toString());
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE, null);
            }
        }
        if (Util.isColdFusion() && urlProps.getProperty("autoConfigureForColdFusion", "true").equalsIgnoreCase("true")) {
            configs = urlProps.getProperty(USE_CONFIG_PROPERTY_KEY);
            stringBuilder = new StringBuilder();
            if (configs != null) {
                stringBuilder.append(configs);
                stringBuilder.append(",");
            }
            stringBuilder.append("coldFusion");
            urlProps.setProperty(USE_CONFIG_PROPERTY_KEY, stringBuilder.toString());
        }
        configs = null;
        if (properties != null) {
            configs = properties.getProperty(USE_CONFIG_PROPERTY_KEY);
        }
        if (configs == null) {
            configs = urlProps.getProperty(USE_CONFIG_PROPERTY_KEY);
        }
        String hostStuff2;
        int beginningOfSlashes2;
        if (configs != null) {
            List<String> splitNames = StringUtils.split(configs, ",", true);
            Properties configProps = new Properties();
            for (String value2 : splitNames) {
                String url3;
                try {
                    Class cls = getClass();
                    url3 = url2;
                    try {
                        url2 = new StringBuilder();
                        hostStuff2 = hostStuff;
                        try {
                            url2.append("configs/");
                            url2.append(value2);
                            url2.append(".properties");
                            url2 = cls.getResourceAsStream(url2.toString());
                            if (url2 == null) {
                                StringBuilder stringBuilder5 = new StringBuilder();
                                stringBuilder5.append("Can't find configuration template named '");
                                stringBuilder5.append(value2);
                                stringBuilder5.append("'");
                                try {
                                    throw SQLError.createSQLException(stringBuilder5.toString(), SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE, (ExceptionInterceptor) 0);
                                } catch (IOException e9) {
                                    url2 = e9;
                                    stringBuilder5 = new StringBuilder();
                                    stringBuilder5.append("Unable to load configuration template '");
                                    stringBuilder5.append(value2);
                                    stringBuilder5.append("' due to underlying IOException: ");
                                    stringBuilder5.append(url2);
                                    hostStuff = SQLError.createSQLException(stringBuilder5.toString(), SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE, null);
                                    hostStuff.initCause(url2);
                                    throw hostStuff;
                                }
                            }
                            beginningOfSlashes2 = beginningOfSlashes;
                            configProps.load(url2);
                            url2 = url3;
                            hostStuff = hostStuff2;
                            beginningOfSlashes = beginningOfSlashes2;
                        } catch (IOException e92) {
                            beginningOfSlashes2 = beginningOfSlashes;
                            url2 = e92;
                        }
                    } catch (IOException e922) {
                        hostStuff2 = hostStuff;
                        beginningOfSlashes2 = beginningOfSlashes;
                        url2 = e922;
                    }
                } catch (IOException e9222) {
                    url3 = url2;
                    hostStuff2 = hostStuff;
                    beginningOfSlashes2 = beginningOfSlashes;
                    url2 = e9222;
                }
            }
            hostStuff2 = hostStuff;
            beginningOfSlashes2 = beginningOfSlashes;
            for (String hostStuff3 : urlProps.keySet()) {
                hostStuff3 = hostStuff3.toString();
                configProps.setProperty(hostStuff3, urlProps.getProperty(hostStuff3));
            }
            urlProps = configProps;
        } else {
            hostStuff2 = hostStuff3;
            beginningOfSlashes2 = beginningOfSlashes;
        }
        if (properties != null) {
            Iterator<Object> propsIter = defaults.keySet().iterator();
            while (propsIter.hasNext()) {
                hostStuff3 = propsIter.next().toString();
                if (!hostStuff3.equals(NUM_HOSTS_PROPERTY_KEY)) {
                    urlProps.setProperty(hostStuff3, properties.getProperty(hostStuff3));
                }
            }
        }
        return urlProps;
    }

    public int port(Properties props) {
        return Integer.parseInt(props.getProperty(PORT_PROPERTY_KEY, "3306"));
    }

    public String property(String name, Properties props) {
        return props.getProperty(name);
    }

    public static Properties expandHostKeyValues(String host) {
        Properties hostProps = new Properties();
        if (isHostPropertiesList(host)) {
            for (String propDef : StringUtils.split(host.substring("address=".length() + 1), ")", "'\"", "'\"", true)) {
                String propDef2;
                if (propDef2.startsWith("(")) {
                    propDef2 = propDef2.substring(1);
                }
                List<String> kvp = StringUtils.split(propDef2, "=", "'\"", "'\"", true);
                String key = (String) kvp.get(0);
                String value = kvp.size() > 1 ? (String) kvp.get(1) : null;
                if (value != null && ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'")))) {
                    value = value.substring(1, value.length() - 1);
                }
                if (value != null) {
                    if (!(HOST_PROPERTY_KEY.equalsIgnoreCase(key) || DBNAME_PROPERTY_KEY.equalsIgnoreCase(key) || PORT_PROPERTY_KEY.equalsIgnoreCase(key) || PROTOCOL_PROPERTY_KEY.equalsIgnoreCase(key))) {
                        if (!PATH_PROPERTY_KEY.equalsIgnoreCase(key)) {
                            if (USER_PROPERTY_KEY.equalsIgnoreCase(key) || PASSWORD_PROPERTY_KEY.equalsIgnoreCase(key)) {
                                key = key.toLowerCase(Locale.ENGLISH);
                            }
                            hostProps.setProperty(key, value);
                        }
                    }
                    key = key.toUpperCase(Locale.ENGLISH);
                    hostProps.setProperty(key, value);
                }
            }
        }
        return hostProps;
    }

    public static boolean isHostPropertiesList(String host) {
        return host != null && StringUtils.startsWithIgnoreCase(host, "address=");
    }
}
