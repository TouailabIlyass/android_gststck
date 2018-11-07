package com.mysql.jdbc;

import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Util {
    private static final String MYSQL_JDBC_PACKAGE_ROOT;
    private static Util enclosingInstance = new Util();
    private static final ConcurrentMap<Class<?>, Class<?>[]> implementedInterfacesCache = new ConcurrentHashMap();
    private static boolean isColdFusion;
    private static boolean isJdbc4;
    private static boolean isJdbc42;
    private static final ConcurrentMap<Class<?>, Boolean> isJdbcInterfaceCache = new ConcurrentHashMap();
    private static int jvmUpdateNumber;
    private static int jvmVersion;

    class RandStructcture {
        long maxValue;
        double maxValueDbl;
        long seed1;
        long seed2;

        RandStructcture() {
        }
    }

    static {
        jvmVersion = -1;
        jvmUpdateNumber = -1;
        isColdFusion = false;
        boolean z = true;
        try {
            Class.forName("java.sql.NClob");
            isJdbc4 = true;
        } catch (ClassNotFoundException e) {
            isJdbc4 = false;
        }
        try {
            Class.forName("java.sql.JDBCType");
            isJdbc42 = true;
        } catch (Throwable th) {
            isJdbc42 = false;
        }
        String jvmVersionString = System.getProperty("java.version");
        int startPos = jvmVersionString.indexOf(46);
        int endPos = startPos + 1;
        if (startPos != -1) {
            while (Character.isDigit(jvmVersionString.charAt(endPos))) {
                endPos++;
                if (endPos >= jvmVersionString.length()) {
                    break;
                }
            }
        }
        startPos++;
        if (endPos > startPos) {
            jvmVersion = Integer.parseInt(jvmVersionString.substring(startPos, endPos));
        } else {
            int i = isJdbc42 ? 8 : isJdbc4 ? 6 : 5;
            jvmVersion = i;
        }
        startPos = jvmVersionString.indexOf("_");
        endPos = startPos + 1;
        if (startPos != -1) {
            while (Character.isDigit(jvmVersionString.charAt(endPos))) {
                endPos++;
                if (endPos >= jvmVersionString.length()) {
                    break;
                }
            }
        }
        startPos++;
        if (endPos > startPos) {
            jvmUpdateNumber = Integer.parseInt(jvmVersionString.substring(startPos, endPos));
        }
        String loadedFrom = stackTraceToString(new Throwable());
        if (loadedFrom != null) {
            if (loadedFrom.indexOf("coldfusion") == -1) {
                z = false;
            }
            isColdFusion = z;
        } else {
            isColdFusion = false;
        }
        String packageName = getPackageName(MultiHostConnectionProxy.class);
        MYSQL_JDBC_PACKAGE_ROOT = packageName.substring(0, packageName.indexOf("jdbc") + 4);
    }

    public static boolean isJdbc4() {
        return isJdbc4;
    }

    public static boolean isJdbc42() {
        return isJdbc42;
    }

    public static int getJVMVersion() {
        return jvmVersion;
    }

    public static boolean jvmMeetsMinimum(int version, int updateNumber) {
        if (getJVMVersion() <= version) {
            if (getJVMVersion() != version || getJVMUpdateNumber() < updateNumber) {
                return false;
            }
        }
        return true;
    }

    public static int getJVMUpdateNumber() {
        return jvmUpdateNumber;
    }

    public static boolean isColdFusion() {
        return isColdFusion;
    }

    public static boolean isCommunityEdition(String serverVersion) {
        return isEnterpriseEdition(serverVersion) ^ 1;
    }

    public static boolean isEnterpriseEdition(String serverVersion) {
        if (!(serverVersion.contains("enterprise") || serverVersion.contains("commercial"))) {
            if (!serverVersion.contains("advanced")) {
                return false;
            }
        }
        return true;
    }

    public static String newCrypt(String password, String seed, String encoding) {
        String str = password;
        if (str != null) {
            if (password.length() != 0) {
                char[] chars;
                long[] pw = newHash(seed.getBytes());
                long[] msg = hashPre41Password(str, encoding);
                long seed1 = (pw[0] ^ msg[0]) % 1073741823;
                long seed2 = (pw[1] ^ msg[1]) % 1073741823;
                char[] chars2 = new char[seed.length()];
                int i = 0;
                while (i < seed.length()) {
                    seed1 = ((3 * seed1) + seed2) % 1073741823;
                    seed2 = ((seed1 + seed2) + 33) % 1073741823;
                    chars = chars2;
                    chars[i] = (char) ((byte) ((int) Math.floor((31.0d * (((double) seed1) / ((double) 1073741823))) + 64.0d)));
                    i++;
                    chars2 = chars;
                }
                chars = chars2;
                long seed12 = ((3 * seed1) + seed2) % 1073741823;
                long seed22 = ((seed12 + seed2) + 33) % 1073741823;
                byte b = (byte) ((int) Math.floor((((double) seed12) / ((double) 1073741823)) * 31.0d));
                int i2 = 0;
                while (true) {
                    int i3 = i2;
                    if (i3 >= seed.length()) {
                        return new String(chars);
                    }
                    chars[i3] = (char) (chars[i3] ^ ((char) b));
                    i2 = i3 + 1;
                }
            }
        }
        String str2 = encoding;
        return str;
    }

    public static long[] hashPre41Password(String password, String encoding) {
        try {
            return newHash(password.replaceAll("\\s", "").getBytes(encoding));
        } catch (UnsupportedEncodingException e) {
            return new long[0];
        }
    }

    public static long[] hashPre41Password(String password) {
        return hashPre41Password(password, Charset.defaultCharset().name());
    }

    static long[] newHash(byte[] password) {
        byte[] arr$ = password;
        long nr2 = 305419889;
        long add = 7;
        long nr = 1345345333;
        int i$ = 0;
        while (i$ < arr$.length) {
            long tmp = (long) (255 & arr$[i$]);
            long nr3 = nr ^ ((((nr & 63) + add) * tmp) + (nr << 8));
            i$++;
            add += tmp;
            nr2 += (nr2 << 8) ^ nr3;
            nr = nr3;
        }
        return new long[]{nr & 2147483647L, nr2 & 2147483647L};
    }

    public static String oldCrypt(String password, String seed) {
        if (password != null) {
            if (password.length() != 0) {
                long hm;
                long hp = oldHash(seed);
                long hm2 = oldHash(password);
                long nr = (hp ^ hm2) % 33554431;
                long s1 = nr;
                long s2 = nr / 2;
                char[] chars = new char[seed.length()];
                int i = 0;
                while (i < seed.length()) {
                    s1 = ((3 * s1) + s2) % 33554431;
                    s2 = ((s1 + s2) + 33) % 33554431;
                    long hp2 = hp;
                    hm = hm2;
                    chars[i] = (char) ((byte) ((int) Math.floor((31.0d * (((double) s1) / ((double) 33554431))) + 64.0d)));
                    i++;
                    hp = hp2;
                    hm2 = hm;
                }
                hm = hm2;
                return new String(chars);
            }
        }
        return password;
    }

    static long oldHash(String password) {
        long nr = 1345345333;
        long nr2 = 7;
        for (int i = 0; i < password.length(); i++) {
            if (password.charAt(i) != ' ') {
                if (password.charAt(i) != '\t') {
                    long tmp = (long) password.charAt(i);
                    long nr3 = nr ^ ((((nr & 63) + nr2) * tmp) + (nr << 8));
                    nr2 += tmp;
                    nr = nr3;
                }
            }
        }
        return nr & 2147483647L;
    }

    private static RandStructcture randomInit(long seed1, long seed2) {
        Util util = enclosingInstance;
        util.getClass();
        RandStructcture randStruct = new RandStructcture();
        randStruct.maxValue = 1073741823;
        randStruct.maxValueDbl = (double) randStruct.maxValue;
        randStruct.seed1 = seed1 % randStruct.maxValue;
        randStruct.seed2 = seed2 % randStruct.maxValue;
        return randStruct;
    }

    public static Object readObject(ResultSet resultSet, int index) throws Exception {
        ObjectInputStream objIn = new ObjectInputStream(resultSet.getBinaryStream(index));
        Object obj = objIn.readObject();
        objIn.close();
        return obj;
    }

    private static double rnd(RandStructcture randStruct) {
        randStruct.seed1 = ((randStruct.seed1 * 3) + randStruct.seed2) % randStruct.maxValue;
        randStruct.seed2 = ((randStruct.seed1 + randStruct.seed2) + 33) % randStruct.maxValue;
        return ((double) randStruct.seed1) / randStruct.maxValueDbl;
    }

    public static String scramble(String message, String password) {
        byte[] to = new byte[8];
        String val = "";
        String message2 = message.substring(0, 8);
        if (password == null || password.length() <= 0) {
            return val;
        }
        long[] hashPass = hashPre41Password(password);
        long[] hashMessage = newHash(message2.getBytes());
        RandStructcture randStruct = randomInit(hashPass[0] ^ hashMessage[0], hashPass[1] ^ hashMessage[1]);
        int msgLength = message2.length();
        int msgPos = 0;
        int toPos = 0;
        while (true) {
            int msgPos2 = msgPos + 1;
            if (msgPos >= msgLength) {
                break;
            }
            msgPos = toPos + 1;
            to[toPos] = (byte) ((int) (Math.floor(rnd(randStruct) * 31.0d) + 64.0d));
            toPos = msgPos;
            msgPos = msgPos2;
        }
        byte extra = (byte) ((int) Math.floor(rnd(randStruct) * 31.0d));
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 >= to.length) {
                return StringUtils.toString(to);
            }
            to[i2] = (byte) (to[i2] ^ extra);
            i = i2 + 1;
        }
    }

    public static String stackTraceToString(Throwable ex) {
        StringBuilder traceBuf = new StringBuilder();
        traceBuf.append(Messages.getString("Util.1"));
        if (ex != null) {
            traceBuf.append(ex.getClass().getName());
            String message = ex.getMessage();
            if (message != null) {
                traceBuf.append(Messages.getString("Util.2"));
                traceBuf.append(message);
            }
            StringWriter out = new StringWriter();
            ex.printStackTrace(new PrintWriter(out));
            traceBuf.append(Messages.getString("Util.3"));
            traceBuf.append(out.toString());
        }
        traceBuf.append(Messages.getString("Util.4"));
        return traceBuf.toString();
    }

    public static Object getInstance(String className, Class<?>[] argTypes, Object[] args, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        try {
            return handleNewInstance(Class.forName(className).getConstructor(argTypes), args, exceptionInterceptor);
        } catch (Throwable e) {
            throw SQLError.createSQLException("Can't instantiate required class", SQLError.SQL_STATE_GENERAL_ERROR, e, exceptionInterceptor);
        } catch (Throwable e2) {
            throw SQLError.createSQLException("Can't instantiate required class", SQLError.SQL_STATE_GENERAL_ERROR, e2, exceptionInterceptor);
        } catch (Throwable e22) {
            throw SQLError.createSQLException("Can't instantiate required class", SQLError.SQL_STATE_GENERAL_ERROR, e22, exceptionInterceptor);
        }
    }

    public static final Object handleNewInstance(Constructor<?> ctor, Object[] args, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        try {
            return ctor.newInstance(args);
        } catch (Throwable e) {
            throw SQLError.createSQLException("Can't instantiate required class", SQLError.SQL_STATE_GENERAL_ERROR, e, exceptionInterceptor);
        } catch (Throwable e2) {
            throw SQLError.createSQLException("Can't instantiate required class", SQLError.SQL_STATE_GENERAL_ERROR, e2, exceptionInterceptor);
        } catch (Throwable e22) {
            throw SQLError.createSQLException("Can't instantiate required class", SQLError.SQL_STATE_GENERAL_ERROR, e22, exceptionInterceptor);
        } catch (InvocationTargetException e3) {
            Throwable target = e3.getTargetException();
            if (target instanceof SQLException) {
                throw ((SQLException) target);
            }
            if (target instanceof ExceptionInInitializerError) {
                target = ((ExceptionInInitializerError) target).getException();
            }
            throw SQLError.createSQLException(target.toString(), SQLError.SQL_STATE_GENERAL_ERROR, target, exceptionInterceptor);
        }
    }

    public static boolean interfaceExists(String hostname) {
        boolean z = false;
        try {
            Class<?> networkInterfaceClass = Class.forName("java.net.NetworkInterface");
            if (networkInterfaceClass.getMethod("getByName", (Class[]) null).invoke(networkInterfaceClass, new Object[]{hostname}) != null) {
                z = true;
            }
            return z;
        } catch (Throwable th) {
            return false;
        }
    }

    public static void resultSetToMap(Map mappedValues, ResultSet rs) throws SQLException {
        while (rs.next()) {
            mappedValues.put(rs.getObject(1), rs.getObject(2));
        }
    }

    public static void resultSetToMap(Map mappedValues, ResultSet rs, int key, int value) throws SQLException {
        while (rs.next()) {
            mappedValues.put(rs.getObject(key), rs.getObject(value));
        }
    }

    public static void resultSetToMap(Map mappedValues, ResultSet rs, String key, String value) throws SQLException {
        while (rs.next()) {
            mappedValues.put(rs.getObject(key), rs.getObject(value));
        }
    }

    public static Map<Object, Object> calculateDifferences(Map<?, ?> map1, Map<?, ?> map2) {
        Map<Object, Object> diffMap = new HashMap();
        for (Entry<?, ?> entry : map1.entrySet()) {
            Number value1;
            Number value2;
            Object key = entry.getKey();
            if (entry.getValue() instanceof Number) {
                value1 = (Number) entry.getValue();
                value2 = (Number) map2.get(key);
            } else {
                try {
                    value1 = new Double(entry.getValue().toString());
                    value2 = new Double(map2.get(key).toString());
                } catch (NumberFormatException e) {
                }
            }
            if (!value1.equals(value2)) {
                if (value1 instanceof Byte) {
                    diffMap.put(key, Byte.valueOf((byte) (((Byte) value2).byteValue() - ((Byte) value1).byteValue())));
                } else if (value1 instanceof Short) {
                    diffMap.put(key, Short.valueOf((short) (((Short) value2).shortValue() - ((Short) value1).shortValue())));
                } else if (value1 instanceof Integer) {
                    diffMap.put(key, Integer.valueOf(((Integer) value2).intValue() - ((Integer) value1).intValue()));
                } else if (value1 instanceof Long) {
                    diffMap.put(key, Long.valueOf(((Long) value2).longValue() - ((Long) value1).longValue()));
                } else if (value1 instanceof Float) {
                    diffMap.put(key, Float.valueOf(((Float) value2).floatValue() - ((Float) value1).floatValue()));
                } else if (value1 instanceof Double) {
                    diffMap.put(key, Double.valueOf((double) (((Double) value2).shortValue() - ((Double) value1).shortValue())));
                } else if (value1 instanceof BigDecimal) {
                    diffMap.put(key, ((BigDecimal) value2).subtract((BigDecimal) value1));
                } else if (value1 instanceof BigInteger) {
                    diffMap.put(key, ((BigInteger) value2).subtract((BigInteger) value1));
                }
            }
        }
        return diffMap;
    }

    public static List<Extension> loadExtensions(Connection conn, Properties props, String extensionClassNames, String errorMessageKey, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        List<Extension> extensionList = new LinkedList();
        List<String> interceptorsToCreate = StringUtils.split(extensionClassNames, ",", true);
        try {
            int s = interceptorsToCreate.size();
            for (int i = 0; i < s; i++) {
                Extension extensionInstance = (Extension) Class.forName((String) interceptorsToCreate.get(i)).newInstance();
                extensionInstance.init(conn, props);
                extensionList.add(extensionInstance);
            }
            return extensionList;
        } catch (Throwable t) {
            SQLError.createSQLException(Messages.getString(errorMessageKey, new Object[]{null}), exceptionInterceptor).initCause(t);
        }
    }

    public static boolean isJdbcInterface(Class<?> clazz) {
        if (isJdbcInterfaceCache.containsKey(clazz)) {
            return ((Boolean) isJdbcInterfaceCache.get(clazz)).booleanValue();
        }
        if (clazz.isInterface()) {
            try {
                if (isJdbcPackage(getPackageName(clazz))) {
                    isJdbcInterfaceCache.putIfAbsent(clazz, Boolean.valueOf(true));
                    return true;
                }
            } catch (Exception e) {
            }
        }
        for (Class<?> iface : clazz.getInterfaces()) {
            if (isJdbcInterface(iface)) {
                isJdbcInterfaceCache.putIfAbsent(clazz, Boolean.valueOf(true));
                return true;
            }
        }
        if (clazz.getSuperclass() == null || !isJdbcInterface(clazz.getSuperclass())) {
            isJdbcInterfaceCache.putIfAbsent(clazz, Boolean.valueOf(false));
            return false;
        }
        isJdbcInterfaceCache.putIfAbsent(clazz, Boolean.valueOf(true));
        return true;
    }

    public static boolean isJdbcPackage(String packageName) {
        return packageName != null && (packageName.startsWith("java.sql") || packageName.startsWith("javax.sql") || packageName.startsWith(MYSQL_JDBC_PACKAGE_ROOT));
    }

    public static Class<?>[] getImplementedInterfaces(Class<?> clazz) {
        Class[] implementedInterfaces = (Class[]) implementedInterfacesCache.get(clazz);
        if (implementedInterfaces != null) {
            return implementedInterfaces;
        }
        Set<Class<?>> interfaces = new LinkedHashSet();
        Class<?> superClass = clazz;
        Class<?> superclass;
        do {
            Collections.addAll(interfaces, superClass.getInterfaces());
            superclass = superClass.getSuperclass();
            superClass = superclass;
        } while (superclass != null);
        Class<?>[] implementedInterfaces2 = (Class[]) interfaces.toArray(new Class[interfaces.size()]);
        Class<?>[] oldValue = (Class[]) implementedInterfacesCache.putIfAbsent(clazz, implementedInterfaces2);
        if (oldValue != null) {
            implementedInterfaces2 = oldValue;
        }
        return implementedInterfaces2;
    }

    public static long secondsSinceMillis(long timeInMillis) {
        return (System.currentTimeMillis() - timeInMillis) / 1000;
    }

    public static int truncateAndConvertToInt(long longValue) {
        if (longValue > 2147483647L) {
            return ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED;
        }
        return longValue < -2147483648L ? Integer.MIN_VALUE : (int) longValue;
    }

    public static int[] truncateAndConvertToInt(long[] longArray) {
        int[] intArray = new int[longArray.length];
        for (int i = 0; i < longArray.length; i++) {
            int i2 = longArray[i] > 2147483647L ? ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED : longArray[i] < -2147483648L ? Integer.MIN_VALUE : (int) longArray[i];
            intArray[i] = i2;
        }
        return intArray;
    }

    public static String getPackageName(Class<?> clazz) {
        String fqcn = clazz.getName();
        int classNameStartsAt = fqcn.lastIndexOf(46);
        if (classNameStartsAt > 0) {
            return fqcn.substring(0, classNameStartsAt);
        }
        return "";
    }
}
