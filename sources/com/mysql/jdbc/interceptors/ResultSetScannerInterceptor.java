package com.mysql.jdbc.interceptors;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ResultSetInternalMethods;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.StatementInterceptor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Pattern;

public class ResultSetScannerInterceptor implements StatementInterceptor {
    protected Pattern regexP;

    public void init(Connection conn, Properties props) throws SQLException {
        String regexFromUser = props.getProperty("resultSetScannerRegex");
        if (regexFromUser != null) {
            if (regexFromUser.length() != 0) {
                try {
                    this.regexP = Pattern.compile(regexFromUser);
                    return;
                } catch (Throwable t) {
                    new SQLException("Can't use configured regex due to underlying exception.").initCause(t);
                }
            }
        }
        throw new SQLException("resultSetScannerRegex must be configured, and must be > 0 characters");
    }

    public ResultSetInternalMethods postProcess(String sql, Statement interceptedStatement, ResultSetInternalMethods originalResultSet, Connection connection) throws SQLException {
        final ResultSetInternalMethods finalResultSet = originalResultSet;
        return (ResultSetInternalMethods) Proxy.newProxyInstance(originalResultSet.getClass().getClassLoader(), new Class[]{ResultSetInternalMethods.class}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if ("equals".equals(method.getName())) {
                    return Boolean.valueOf(args[0].equals(this));
                }
                Object invocationResult = method.invoke(finalResultSet, args);
                String methodName = method.getName();
                if (((invocationResult == null || !(invocationResult instanceof String)) && !"getString".equals(methodName) && !"getObject".equals(methodName) && !"getObjectStoredProc".equals(methodName)) || !ResultSetScannerInterceptor.this.regexP.matcher(invocationResult.toString()).matches()) {
                    return invocationResult;
                }
                throw new SQLException("value disallowed by filter");
            }
        });
    }

    public ResultSetInternalMethods preProcess(String sql, Statement interceptedStatement, Connection connection) throws SQLException {
        return null;
    }

    public boolean executeTopLevelOnly() {
        return false;
    }

    public void destroy() {
    }
}
