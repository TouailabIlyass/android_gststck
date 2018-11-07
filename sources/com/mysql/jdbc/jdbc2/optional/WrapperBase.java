package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.ExceptionInterceptor;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.Util;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.Map;

abstract class WrapperBase {
    protected ExceptionInterceptor exceptionInterceptor;
    protected MysqlPooledConnection pooledConnection;
    protected Map<Class<?>, Object> unwrappedInterfaces = null;

    protected class ConnectionErrorFiringInvocationHandler implements InvocationHandler {
        Object invokeOn = null;

        public ConnectionErrorFiringInvocationHandler(Object toInvokeOn) {
            this.invokeOn = toInvokeOn;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("equals".equals(method.getName())) {
                return Boolean.valueOf(args[0].equals(this));
            }
            Object result = null;
            try {
                result = method.invoke(this.invokeOn, args);
                if (result != null) {
                    result = proxyIfInterfaceIsJdbc(result, result.getClass());
                }
            } catch (InvocationTargetException e) {
                if (e.getTargetException() instanceof SQLException) {
                    WrapperBase.this.checkAndFireConnectionError((SQLException) e.getTargetException());
                } else {
                    throw e;
                }
            }
            return result;
        }

        private Object proxyIfInterfaceIsJdbc(Object toProxy, Class<?> clazz) {
            Class<?>[] interfaces = clazz.getInterfaces();
            Class<?>[] arr$ = interfaces;
            if (0 >= arr$.length) {
                return toProxy;
            }
            Class<?> iclass = arr$[0];
            String packageName = Util.getPackageName(iclass);
            if (!"java.sql".equals(packageName)) {
                if (!"javax.sql".equals(packageName)) {
                    return proxyIfInterfaceIsJdbc(toProxy, iclass);
                }
            }
            return Proxy.newProxyInstance(toProxy.getClass().getClassLoader(), interfaces, new ConnectionErrorFiringInvocationHandler(toProxy));
        }
    }

    protected void checkAndFireConnectionError(SQLException sqlEx) throws SQLException {
        if (this.pooledConnection != null && SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE.equals(sqlEx.getSQLState())) {
            this.pooledConnection.callConnectionEventListeners(1, sqlEx);
        }
        throw sqlEx;
    }

    protected WrapperBase(MysqlPooledConnection pooledConnection) {
        this.pooledConnection = pooledConnection;
        this.exceptionInterceptor = this.pooledConnection.getExceptionInterceptor();
    }
}
