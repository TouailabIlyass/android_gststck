package com.mysql.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CallableStatement extends PreparedStatement implements java.sql.CallableStatement {
    protected static final Constructor<?> JDBC_4_CSTMT_2_ARGS_CTOR;
    protected static final Constructor<?> JDBC_4_CSTMT_4_ARGS_CTOR;
    private static final int NOT_OUTPUT_PARAMETER_INDICATOR = Integer.MIN_VALUE;
    private static final String PARAMETER_NAMESPACE_PREFIX = "@com_mysql_jdbc_outparam_";
    private boolean callingStoredFunction = false;
    private ResultSetInternalMethods functionReturnValueResults;
    private boolean hasOutputParams = false;
    protected boolean outputParamWasNull = false;
    private ResultSetInternalMethods outputParameterResults;
    protected CallableStatementParamInfo paramInfo;
    private int[] parameterIndexToRsIndex;
    private int[] placeholderToParameterIndexMap;
    private CallableStatementParam returnValueParam;

    protected static class CallableStatementParam {
        int desiredJdbcType;
        int inOutModifier;
        int index;
        boolean isIn;
        boolean isOut;
        int jdbcType;
        short nullability;
        String paramName;
        int precision;
        int scale;
        String typeName;

        CallableStatementParam(String name, int idx, boolean in, boolean out, int jdbcType, String typeName, int precision, int scale, short nullability, int inOutModifier) {
            this.paramName = name;
            this.isIn = in;
            this.isOut = out;
            this.index = idx;
            this.jdbcType = jdbcType;
            this.typeName = typeName;
            this.precision = precision;
            this.scale = scale;
            this.nullability = nullability;
            this.inOutModifier = inOutModifier;
        }

        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    protected class CallableStatementParamInfo implements ParameterMetaData {
        String catalogInUse;
        boolean isFunctionCall;
        boolean isReadOnlySafeChecked = false;
        boolean isReadOnlySafeProcedure = false;
        String nativeSql;
        int numParameters;
        List<CallableStatementParam> parameterList;
        Map<String, CallableStatementParam> parameterMap;

        CallableStatementParamInfo(CallableStatementParamInfo fullParamInfo) {
            int i = 0;
            this.nativeSql = CallableStatement.this.originalSql;
            this.catalogInUse = CallableStatement.this.currentCatalog;
            this.isFunctionCall = fullParamInfo.isFunctionCall;
            int[] localParameterMap = CallableStatement.this.placeholderToParameterIndexMap;
            int parameterMapLength = localParameterMap.length;
            this.isReadOnlySafeProcedure = fullParamInfo.isReadOnlySafeProcedure;
            this.isReadOnlySafeChecked = fullParamInfo.isReadOnlySafeChecked;
            this.parameterList = new ArrayList(fullParamInfo.numParameters);
            this.parameterMap = new HashMap(fullParamInfo.numParameters);
            if (this.isFunctionCall) {
                this.parameterList.add(fullParamInfo.parameterList.get(0));
            }
            int offset = this.isFunctionCall;
            while (i < parameterMapLength) {
                if (localParameterMap[i] != 0) {
                    CallableStatementParam param = (CallableStatementParam) fullParamInfo.parameterList.get(localParameterMap[i] + offset);
                    this.parameterList.add(param);
                    this.parameterMap.put(param.paramName, param);
                }
                i++;
            }
            this.numParameters = this.parameterList.size();
        }

        CallableStatementParamInfo(ResultSet paramTypesRs) throws SQLException {
            boolean hadRows = paramTypesRs.last();
            this.nativeSql = CallableStatement.this.originalSql;
            this.catalogInUse = CallableStatement.this.currentCatalog;
            this.isFunctionCall = CallableStatement.this.callingStoredFunction;
            if (hadRows) {
                this.numParameters = paramTypesRs.getRow();
                this.parameterList = new ArrayList(this.numParameters);
                this.parameterMap = new HashMap(this.numParameters);
                paramTypesRs.beforeFirst();
                addParametersFromDBMD(paramTypesRs);
            } else {
                this.numParameters = 0;
            }
            if (this.isFunctionCall != null) {
                this.numParameters++;
            }
        }

        private void addParametersFromDBMD(ResultSet paramTypesRs) throws SQLException {
            CallableStatementParamInfo callableStatementParamInfo = this;
            ResultSet resultSet = paramTypesRs;
            int i = 0;
            int inOutModifier = 0;
            while (true) {
                int i2 = i;
                if (paramTypesRs.next()) {
                    boolean isInParameter;
                    boolean isOutParameter;
                    boolean isOutParameter2;
                    int i3;
                    CallableStatementParam paramInfoToAdd;
                    String paramName = resultSet.getString(4);
                    switch (resultSet.getInt(5)) {
                        case 1:
                            inOutModifier = 1;
                            break;
                        case 2:
                            inOutModifier = 2;
                            break;
                        case 4:
                        case 5:
                            inOutModifier = 4;
                            break;
                        default:
                            inOutModifier = 0;
                            break;
                    }
                    if (i2 == 0 && callableStatementParamInfo.isFunctionCall) {
                        isInParameter = true;
                        isOutParameter = false;
                    } else if (inOutModifier == 2) {
                        isInParameter = true;
                        isOutParameter = true;
                    } else if (inOutModifier == 1) {
                        isInParameter = false;
                        isOutParameter = true;
                    } else if (inOutModifier == 4) {
                        isInParameter = true;
                        isOutParameter = false;
                    } else {
                        isOutParameter2 = false;
                        isInParameter = false;
                        i3 = i2 + 1;
                        paramInfoToAdd = new CallableStatementParam(paramName, i2, isInParameter, isOutParameter2, resultSet.getInt(6), resultSet.getString(7), resultSet.getInt(8), resultSet.getInt(10), resultSet.getShort(12), inOutModifier);
                        callableStatementParamInfo.parameterList.add(paramInfoToAdd);
                        callableStatementParamInfo.parameterMap.put(paramName, paramInfoToAdd);
                        i = i3;
                    }
                    isOutParameter2 = isInParameter;
                    isInParameter = isOutParameter;
                    i3 = i2 + 1;
                    paramInfoToAdd = new CallableStatementParam(paramName, i2, isInParameter, isOutParameter2, resultSet.getInt(6), resultSet.getString(7), resultSet.getInt(8), resultSet.getInt(10), resultSet.getShort(12), inOutModifier);
                    callableStatementParamInfo.parameterList.add(paramInfoToAdd);
                    callableStatementParamInfo.parameterMap.put(paramName, paramInfoToAdd);
                    i = i3;
                } else {
                    return;
                }
            }
        }

        protected void checkBounds(int paramIndex) throws SQLException {
            int localParamIndex = paramIndex - 1;
            if (paramIndex >= 0) {
                if (localParamIndex < this.numParameters) {
                    return;
                }
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Messages.getString("CallableStatement.11"));
            stringBuilder.append(paramIndex);
            stringBuilder.append(Messages.getString("CallableStatement.12"));
            stringBuilder.append(this.numParameters);
            stringBuilder.append(Messages.getString("CallableStatement.13"));
            throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, CallableStatement.this.getExceptionInterceptor());
        }

        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        CallableStatementParam getParameter(int index) {
            return (CallableStatementParam) this.parameterList.get(index);
        }

        CallableStatementParam getParameter(String name) {
            return (CallableStatementParam) this.parameterMap.get(name);
        }

        public String getParameterClassName(int arg0) throws SQLException {
            boolean isBinaryOrBlob;
            boolean isUnsigned;
            int mysqlTypeIfKnown;
            String mysqlTypeName = getParameterTypeName(arg0);
            if (StringUtils.indexOfIgnoreCase(mysqlTypeName, "BLOB") == -1) {
                if (StringUtils.indexOfIgnoreCase(mysqlTypeName, "BINARY") == -1) {
                    isBinaryOrBlob = false;
                    isUnsigned = StringUtils.indexOfIgnoreCase(mysqlTypeName, "UNSIGNED") == -1;
                    mysqlTypeIfKnown = 0;
                    if (StringUtils.startsWithIgnoreCase(mysqlTypeName, "MEDIUMINT")) {
                        mysqlTypeIfKnown = 9;
                    }
                    return ResultSetMetaData.getClassNameForJavaType(getParameterType(arg0), isUnsigned, mysqlTypeIfKnown, isBinaryOrBlob, false, CallableStatement.this.connection.getYearIsDateType());
                }
            }
            isBinaryOrBlob = true;
            if (StringUtils.indexOfIgnoreCase(mysqlTypeName, "UNSIGNED") == -1) {
            }
            mysqlTypeIfKnown = 0;
            if (StringUtils.startsWithIgnoreCase(mysqlTypeName, "MEDIUMINT")) {
                mysqlTypeIfKnown = 9;
            }
            return ResultSetMetaData.getClassNameForJavaType(getParameterType(arg0), isUnsigned, mysqlTypeIfKnown, isBinaryOrBlob, false, CallableStatement.this.connection.getYearIsDateType());
        }

        public int getParameterCount() throws SQLException {
            if (this.parameterList == null) {
                return 0;
            }
            return this.parameterList.size();
        }

        public int getParameterMode(int arg0) throws SQLException {
            checkBounds(arg0);
            return getParameter(arg0 - 1).inOutModifier;
        }

        public int getParameterType(int arg0) throws SQLException {
            checkBounds(arg0);
            return getParameter(arg0 - 1).jdbcType;
        }

        public String getParameterTypeName(int arg0) throws SQLException {
            checkBounds(arg0);
            return getParameter(arg0 - 1).typeName;
        }

        public int getPrecision(int arg0) throws SQLException {
            checkBounds(arg0);
            return getParameter(arg0 - 1).precision;
        }

        public int getScale(int arg0) throws SQLException {
            checkBounds(arg0);
            return getParameter(arg0 - 1).scale;
        }

        public int isNullable(int arg0) throws SQLException {
            checkBounds(arg0);
            return getParameter(arg0 - 1).nullability;
        }

        public boolean isSigned(int arg0) throws SQLException {
            checkBounds(arg0);
            return false;
        }

        Iterator<CallableStatementParam> iterator() {
            return this.parameterList.iterator();
        }

        int numberOfParameters() {
            return this.numParameters;
        }

        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            CallableStatement.this.checkClosed();
            return iface.isInstance(this);
        }

        public <T> T unwrap(Class<T> iface) throws SQLException {
            try {
                return iface.cast(this);
            } catch (ClassCastException e) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unable to unwrap to ");
                stringBuilder.append(iface.toString());
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, CallableStatement.this.getExceptionInterceptor());
            }
        }
    }

    static {
        if (Util.isJdbc4()) {
            try {
                String jdbc4ClassName = Util.isJdbc42() ? "com.mysql.jdbc.JDBC42CallableStatement" : "com.mysql.jdbc.JDBC4CallableStatement";
                JDBC_4_CSTMT_2_ARGS_CTOR = Class.forName(jdbc4ClassName).getConstructor(new Class[]{MySQLConnection.class, CallableStatementParamInfo.class});
                JDBC_4_CSTMT_4_ARGS_CTOR = Class.forName(jdbc4ClassName).getConstructor(new Class[]{MySQLConnection.class, String.class, String.class, Boolean.TYPE});
                return;
            } catch (SecurityException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e2) {
                throw new RuntimeException(e2);
            } catch (ClassNotFoundException e3) {
                throw new RuntimeException(e3);
            }
        }
        JDBC_4_CSTMT_4_ARGS_CTOR = null;
        JDBC_4_CSTMT_2_ARGS_CTOR = null;
    }

    private static String mangleParameterName(String origParameterName) {
        if (origParameterName == null) {
            return null;
        }
        int offset = 0;
        if (origParameterName.length() > 0 && origParameterName.charAt(0) == '@') {
            offset = 1;
        }
        StringBuilder paramNameBuf = new StringBuilder(PARAMETER_NAMESPACE_PREFIX.length() + origParameterName.length());
        paramNameBuf.append(PARAMETER_NAMESPACE_PREFIX);
        paramNameBuf.append(origParameterName.substring(offset));
        return paramNameBuf.toString();
    }

    public CallableStatement(MySQLConnection conn, CallableStatementParamInfo paramInfo) throws SQLException {
        super(conn, paramInfo.nativeSql, paramInfo.catalogInUse);
        this.paramInfo = paramInfo;
        this.callingStoredFunction = this.paramInfo.isFunctionCall;
        if (this.callingStoredFunction) {
            this.parameterCount++;
        }
        this.retrieveGeneratedKeys = true;
    }

    protected static CallableStatement getInstance(MySQLConnection conn, String sql, String catalog, boolean isFunctionCall) throws SQLException {
        if (!Util.isJdbc4()) {
            return new CallableStatement(conn, sql, catalog, isFunctionCall);
        }
        return (CallableStatement) Util.handleNewInstance(JDBC_4_CSTMT_4_ARGS_CTOR, new Object[]{conn, sql, catalog, Boolean.valueOf(isFunctionCall)}, conn.getExceptionInterceptor());
    }

    protected static CallableStatement getInstance(MySQLConnection conn, CallableStatementParamInfo paramInfo) throws SQLException {
        if (!Util.isJdbc4()) {
            return new CallableStatement(conn, paramInfo);
        }
        return (CallableStatement) Util.handleNewInstance(JDBC_4_CSTMT_2_ARGS_CTOR, new Object[]{conn, paramInfo}, conn.getExceptionInterceptor());
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void generateParameterMap() throws java.sql.SQLException {
        /*
        r11 = this;
        r0 = r11.checkClosed();
        r0 = r0.getConnectionMutex();
        monitor-enter(r0);
        r1 = r11.paramInfo;	 Catch:{ all -> 0x0092 }
        if (r1 != 0) goto L_0x000f;
    L_0x000d:
        monitor-exit(r0);	 Catch:{ all -> 0x0092 }
        return;
    L_0x000f:
        r1 = r11.paramInfo;	 Catch:{ all -> 0x0092 }
        r1 = r1.getParameterCount();	 Catch:{ all -> 0x0092 }
        r2 = r11.callingStoredFunction;	 Catch:{ all -> 0x0092 }
        if (r2 == 0) goto L_0x001b;
    L_0x0019:
        r1 = r1 + -1;
    L_0x001b:
        r2 = r11.paramInfo;	 Catch:{ all -> 0x0092 }
        if (r2 == 0) goto L_0x0090;
    L_0x001f:
        r2 = r11.parameterCount;	 Catch:{ all -> 0x0092 }
        if (r2 == r1) goto L_0x0090;
    L_0x0023:
        r2 = r11.parameterCount;	 Catch:{ all -> 0x0092 }
        r2 = new int[r2];	 Catch:{ all -> 0x0092 }
        r11.placeholderToParameterIndexMap = r2;	 Catch:{ all -> 0x0092 }
        r2 = r11.callingStoredFunction;	 Catch:{ all -> 0x0092 }
        if (r2 == 0) goto L_0x0036;
    L_0x002d:
        r2 = r11.originalSql;	 Catch:{ all -> 0x0092 }
        r3 = "SELECT";
    L_0x0031:
        r2 = com.mysql.jdbc.StringUtils.indexOfIgnoreCase(r2, r3);	 Catch:{ all -> 0x0092 }
        goto L_0x003b;
    L_0x0036:
        r2 = r11.originalSql;	 Catch:{ all -> 0x0092 }
        r3 = "CALL";
        goto L_0x0031;
    L_0x003b:
        r3 = -1;
        if (r2 == r3) goto L_0x0090;
    L_0x003e:
        r4 = r11.originalSql;	 Catch:{ all -> 0x0092 }
        r5 = 40;
        r6 = r2 + 4;
        r4 = r4.indexOf(r5, r6);	 Catch:{ all -> 0x0092 }
        if (r4 == r3) goto L_0x0090;
    L_0x004a:
        r6 = r11.originalSql;	 Catch:{ all -> 0x0092 }
        r7 = ")";
        r8 = "'";
        r9 = "'";
        r10 = com.mysql.jdbc.StringUtils.SEARCH_MODE__ALL;	 Catch:{ all -> 0x0092 }
        r5 = r4;
        r5 = com.mysql.jdbc.StringUtils.indexOfIgnoreCase(r5, r6, r7, r8, r9, r10);	 Catch:{ all -> 0x0092 }
        if (r5 == r3) goto L_0x0090;
    L_0x005b:
        r3 = r11.originalSql;	 Catch:{ all -> 0x0092 }
        r6 = r4 + 1;
        r3 = r3.substring(r6, r5);	 Catch:{ all -> 0x0092 }
        r6 = ",";
        r7 = "'\"";
        r8 = "'\"";
        r9 = 1;
        r3 = com.mysql.jdbc.StringUtils.split(r3, r6, r7, r8, r9);	 Catch:{ all -> 0x0092 }
        r6 = r3.size();	 Catch:{ all -> 0x0092 }
        r7 = r11.parameterCount;	 Catch:{ all -> 0x0092 }
        r7 = 0;
        r8 = 0;
    L_0x0076:
        if (r8 >= r6) goto L_0x0090;
    L_0x0078:
        r9 = r3.get(r8);	 Catch:{ all -> 0x0092 }
        r9 = (java.lang.String) r9;	 Catch:{ all -> 0x0092 }
        r10 = "?";
        r9 = r9.equals(r10);	 Catch:{ all -> 0x0092 }
        if (r9 == 0) goto L_0x008d;
    L_0x0086:
        r9 = r11.placeholderToParameterIndexMap;	 Catch:{ all -> 0x0092 }
        r10 = r7 + 1;
        r9[r7] = r8;	 Catch:{ all -> 0x0092 }
        r7 = r10;
    L_0x008d:
        r8 = r8 + 1;
        goto L_0x0076;
    L_0x0090:
        monitor-exit(r0);	 Catch:{ all -> 0x0092 }
        return;
    L_0x0092:
        r1 = move-exception;
        monitor-exit(r0);	 Catch:{ all -> 0x0092 }
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.CallableStatement.generateParameterMap():void");
    }

    public CallableStatement(MySQLConnection conn, String sql, String catalog, boolean isFunctionCall) throws SQLException {
        super(conn, sql, catalog);
        this.callingStoredFunction = isFunctionCall;
        if (this.callingStoredFunction) {
            determineParameterTypes();
            generateParameterMap();
            this.parameterCount++;
        } else {
            if (StringUtils.startsWithIgnoreCaseAndWs(sql, "CALL")) {
                determineParameterTypes();
            } else {
                fakeParameterTypes(false);
            }
            generateParameterMap();
        }
        this.retrieveGeneratedKeys = true;
    }

    public void addBatch() throws SQLException {
        setOutParams();
        super.addBatch();
    }

    private CallableStatementParam checkIsOutputParam(int paramIndex) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                if (paramIndex == 1) {
                    if (this.returnValueParam == null) {
                        this.returnValueParam = new CallableStatementParam("", 0, false, true, 12, "VARCHAR", 0, 0, (short) 2, 5);
                    }
                    CallableStatementParam callableStatementParam = this.returnValueParam;
                    return callableStatementParam;
                }
                paramIndex--;
            }
            checkParameterIndexBounds(paramIndex);
            int localParamIndex = paramIndex - 1;
            if (this.placeholderToParameterIndexMap != null) {
                localParamIndex = this.placeholderToParameterIndexMap[localParamIndex];
            }
            CallableStatementParam paramDescriptor = this.paramInfo.getParameter(localParamIndex);
            if (this.connection.getNoAccessToProcedureBodies()) {
                paramDescriptor.isOut = true;
                paramDescriptor.isIn = true;
                paramDescriptor.inOutModifier = 2;
            } else if (!paramDescriptor.isOut) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(Messages.getString("CallableStatement.9"));
                stringBuilder.append(paramIndex);
                stringBuilder.append(Messages.getString("CallableStatement.10"));
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            }
            this.hasOutputParams = true;
            return paramDescriptor;
        }
    }

    private void checkParameterIndexBounds(int paramIndex) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            this.paramInfo.checkBounds(paramIndex);
        }
    }

    private void checkStreamability() throws SQLException {
        if (this.hasOutputParams && createStreamingResultSet()) {
            throw SQLError.createSQLException(Messages.getString("CallableStatement.14"), SQLError.SQL_STATE_DRIVER_NOT_CAPABLE, getExceptionInterceptor());
        }
    }

    public void clearParameters() throws SQLException {
        Throwable th;
        synchronized (checkClosed().getConnectionMutex()) {
            try {
                super.clearParameters();
                try {
                    if (this.outputParameterResults != null) {
                        this.outputParameterResults.close();
                    }
                    this.outputParameterResults = null;
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                CallableStatement callableStatement = this;
                throw th;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void fakeParameterTypes(boolean r22) throws java.sql.SQLException {
        /*
        r21 = this;
        r1 = r21;
        r2 = r21.checkClosed();
        r2 = r2.getConnectionMutex();
        monitor-enter(r2);
        r3 = 13;
        r4 = new com.mysql.jdbc.Field[r3];	 Catch:{ all -> 0x0190 }
        r5 = new com.mysql.jdbc.Field;	 Catch:{ all -> 0x0190 }
        r6 = "";
        r7 = "PROCEDURE_CAT";
        r8 = 1;
        r9 = 0;
        r5.<init>(r6, r7, r8, r9);	 Catch:{ all -> 0x0190 }
        r4[r9] = r5;	 Catch:{ all -> 0x0190 }
        r5 = new com.mysql.jdbc.Field;	 Catch:{ all -> 0x0190 }
        r6 = "";
        r7 = "PROCEDURE_SCHEM";
        r5.<init>(r6, r7, r8, r9);	 Catch:{ all -> 0x0190 }
        r4[r8] = r5;	 Catch:{ all -> 0x0190 }
        r5 = new com.mysql.jdbc.Field;	 Catch:{ all -> 0x0190 }
        r6 = "";
        r7 = "PROCEDURE_NAME";
        r5.<init>(r6, r7, r8, r9);	 Catch:{ all -> 0x0190 }
        r6 = 2;
        r4[r6] = r5;	 Catch:{ all -> 0x0190 }
        r5 = new com.mysql.jdbc.Field;	 Catch:{ all -> 0x0190 }
        r7 = "";
        r10 = "COLUMN_NAME";
        r5.<init>(r7, r10, r8, r9);	 Catch:{ all -> 0x0190 }
        r7 = 3;
        r4[r7] = r5;	 Catch:{ all -> 0x0190 }
        r5 = new com.mysql.jdbc.Field;	 Catch:{ all -> 0x0190 }
        r10 = "";
        r11 = "COLUMN_TYPE";
        r5.<init>(r10, r11, r8, r9);	 Catch:{ all -> 0x0190 }
        r10 = 4;
        r4[r10] = r5;	 Catch:{ all -> 0x0190 }
        r5 = new com.mysql.jdbc.Field;	 Catch:{ all -> 0x0190 }
        r11 = "";
        r12 = "DATA_TYPE";
        r13 = 5;
        r5.<init>(r11, r12, r13, r9);	 Catch:{ all -> 0x0190 }
        r4[r13] = r5;	 Catch:{ all -> 0x0190 }
        r5 = new com.mysql.jdbc.Field;	 Catch:{ all -> 0x0190 }
        r11 = "";
        r12 = "TYPE_NAME";
        r5.<init>(r11, r12, r8, r9);	 Catch:{ all -> 0x0190 }
        r11 = 6;
        r4[r11] = r5;	 Catch:{ all -> 0x0190 }
        r5 = new com.mysql.jdbc.Field;	 Catch:{ all -> 0x0190 }
        r12 = "";
        r14 = "PRECISION";
        r5.<init>(r12, r14, r10, r9);	 Catch:{ all -> 0x0190 }
        r12 = 7;
        r4[r12] = r5;	 Catch:{ all -> 0x0190 }
        r5 = new com.mysql.jdbc.Field;	 Catch:{ all -> 0x0190 }
        r14 = "";
        r15 = "LENGTH";
        r5.<init>(r14, r15, r10, r9);	 Catch:{ all -> 0x0190 }
        r14 = 8;
        r4[r14] = r5;	 Catch:{ all -> 0x0190 }
        r5 = new com.mysql.jdbc.Field;	 Catch:{ all -> 0x0190 }
        r15 = "";
        r14 = "SCALE";
        r5.<init>(r15, r14, r13, r9);	 Catch:{ all -> 0x0190 }
        r14 = 9;
        r4[r14] = r5;	 Catch:{ all -> 0x0190 }
        r5 = new com.mysql.jdbc.Field;	 Catch:{ all -> 0x0190 }
        r15 = "";
        r14 = "RADIX";
        r5.<init>(r15, r14, r13, r9);	 Catch:{ all -> 0x0190 }
        r14 = 10;
        r4[r14] = r5;	 Catch:{ all -> 0x0190 }
        r5 = new com.mysql.jdbc.Field;	 Catch:{ all -> 0x0190 }
        r15 = "";
        r14 = "NULLABLE";
        r5.<init>(r15, r14, r13, r9);	 Catch:{ all -> 0x0190 }
        r14 = 11;
        r4[r14] = r5;	 Catch:{ all -> 0x0190 }
        r5 = new com.mysql.jdbc.Field;	 Catch:{ all -> 0x0190 }
        r15 = "";
        r14 = "REMARKS";
        r5.<init>(r15, r14, r8, r9);	 Catch:{ all -> 0x0190 }
        r14 = 12;
        r4[r14] = r5;	 Catch:{ all -> 0x0190 }
        r5 = 0;
        if (r22 == 0) goto L_0x00b8;
    L_0x00b3:
        r17 = r21.extractProcedureName();	 Catch:{ all -> 0x0190 }
        goto L_0x00ba;
    L_0x00b8:
        r17 = r5;
    L_0x00ba:
        r18 = r17;
        r17 = r5;
        r12 = r18;
        if (r12 != 0) goto L_0x00c4;
    L_0x00c2:
        r11 = r5;
        goto L_0x00ca;
    L_0x00c4:
        r11 = "UTF-8";
        r11 = com.mysql.jdbc.StringUtils.getBytes(r12, r11);	 Catch:{ UnsupportedEncodingException -> 0x00cb }
    L_0x00ca:
        goto L_0x00d4;
    L_0x00cb:
        r0 = move-exception;
        r11 = r0;
        r13 = r1.connection;	 Catch:{ all -> 0x0190 }
        r13 = com.mysql.jdbc.StringUtils.s2b(r12, r13);	 Catch:{ all -> 0x0190 }
        r11 = r13;
    L_0x00d4:
        r13 = new java.util.ArrayList;	 Catch:{ all -> 0x0190 }
        r13.<init>();	 Catch:{ all -> 0x0190 }
        r17 = r9;
    L_0x00db:
        r19 = r17;
        r14 = r1.parameterCount;	 Catch:{ all -> 0x0190 }
        r10 = r19;
        if (r10 >= r14) goto L_0x0185;
    L_0x00e3:
        r14 = new byte[r3][];	 Catch:{ all -> 0x0190 }
        r14[r9] = r5;	 Catch:{ all -> 0x0190 }
        r14[r8] = r5;	 Catch:{ all -> 0x0190 }
        r14[r6] = r11;	 Catch:{ all -> 0x0190 }
        r3 = java.lang.String.valueOf(r10);	 Catch:{ all -> 0x0190 }
        r5 = r1.connection;	 Catch:{ all -> 0x0190 }
        r3 = com.mysql.jdbc.StringUtils.s2b(r3, r5);	 Catch:{ all -> 0x0190 }
        r14[r7] = r3;	 Catch:{ all -> 0x0190 }
        r3 = java.lang.String.valueOf(r8);	 Catch:{ all -> 0x0190 }
        r5 = r1.connection;	 Catch:{ all -> 0x0190 }
        r3 = com.mysql.jdbc.StringUtils.s2b(r3, r5);	 Catch:{ all -> 0x0190 }
        r5 = 4;
        r14[r5] = r3;	 Catch:{ all -> 0x0190 }
        r3 = 12;
        r5 = java.lang.String.valueOf(r3);	 Catch:{ all -> 0x0190 }
        r3 = r1.connection;	 Catch:{ all -> 0x0190 }
        r3 = com.mysql.jdbc.StringUtils.s2b(r5, r3);	 Catch:{ all -> 0x0190 }
        r5 = 5;
        r14[r5] = r3;	 Catch:{ all -> 0x0190 }
        r3 = "VARCHAR";
        r5 = r1.connection;	 Catch:{ all -> 0x0190 }
        r3 = com.mysql.jdbc.StringUtils.s2b(r3, r5);	 Catch:{ all -> 0x0190 }
        r5 = 6;
        r14[r5] = r3;	 Catch:{ all -> 0x0190 }
        r3 = 65535; // 0xffff float:9.1834E-41 double:3.23786E-319;
        r3 = java.lang.Integer.toString(r3);	 Catch:{ all -> 0x0190 }
        r5 = r1.connection;	 Catch:{ all -> 0x0190 }
        r3 = com.mysql.jdbc.StringUtils.s2b(r3, r5);	 Catch:{ all -> 0x0190 }
        r5 = 7;
        r14[r5] = r3;	 Catch:{ all -> 0x0190 }
        r3 = 65535; // 0xffff float:9.1834E-41 double:3.23786E-319;
        r3 = java.lang.Integer.toString(r3);	 Catch:{ all -> 0x0190 }
        r5 = r1.connection;	 Catch:{ all -> 0x0190 }
        r3 = com.mysql.jdbc.StringUtils.s2b(r3, r5);	 Catch:{ all -> 0x0190 }
        r5 = 8;
        r14[r5] = r3;	 Catch:{ all -> 0x0190 }
        r3 = java.lang.Integer.toString(r9);	 Catch:{ all -> 0x0190 }
        r5 = r1.connection;	 Catch:{ all -> 0x0190 }
        r3 = com.mysql.jdbc.StringUtils.s2b(r3, r5);	 Catch:{ all -> 0x0190 }
        r5 = 9;
        r14[r5] = r3;	 Catch:{ all -> 0x0190 }
        r3 = 10;
        r5 = java.lang.Integer.toString(r3);	 Catch:{ all -> 0x0190 }
        r7 = r1.connection;	 Catch:{ all -> 0x0190 }
        r5 = com.mysql.jdbc.StringUtils.s2b(r5, r7);	 Catch:{ all -> 0x0190 }
        r14[r3] = r5;	 Catch:{ all -> 0x0190 }
        r5 = java.lang.Integer.toString(r6);	 Catch:{ all -> 0x0190 }
        r7 = r1.connection;	 Catch:{ all -> 0x0190 }
        r5 = com.mysql.jdbc.StringUtils.s2b(r5, r7);	 Catch:{ all -> 0x0190 }
        r7 = 11;
        r14[r7] = r5;	 Catch:{ all -> 0x0190 }
        r5 = 0;
        r16 = 12;
        r14[r16] = r5;	 Catch:{ all -> 0x0190 }
        r3 = new com.mysql.jdbc.ByteArrayRow;	 Catch:{ all -> 0x0190 }
        r5 = r21.getExceptionInterceptor();	 Catch:{ all -> 0x0190 }
        r3.<init>(r14, r5);	 Catch:{ all -> 0x0190 }
        r13.add(r3);	 Catch:{ all -> 0x0190 }
        r17 = r10 + 1;
        r14 = r16;
        r3 = 13;
        r5 = 0;
        r7 = 3;
        r10 = 4;
        goto L_0x00db;
    L_0x0185:
        r3 = r1.connection;	 Catch:{ all -> 0x0190 }
        r3 = com.mysql.jdbc.DatabaseMetaData.buildResultSet(r4, r13, r3);	 Catch:{ all -> 0x0190 }
        r1.convertGetProcedureColumnsToInternalDescriptors(r3);	 Catch:{ all -> 0x0190 }
        monitor-exit(r2);	 Catch:{ all -> 0x0190 }
        return;
    L_0x0190:
        r0 = move-exception;
        r3 = r0;
        monitor-exit(r2);	 Catch:{ all -> 0x0190 }
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.CallableStatement.fakeParameterTypes(boolean):void");
    }

    private void determineParameterTypes() throws SQLException {
        SQLException sqlExRethrow;
        synchronized (checkClosed().getConnectionMutex()) {
            String procName;
            String quotedId;
            ResultSet paramTypesRs = null;
            try {
                procName = extractProcedureName();
                quotedId = "";
                quotedId = this.connection.supportsQuotedIdentifiers() ? this.connection.getMetaData().getIdentifierQuoteString() : "";
            } catch (SQLException sqlEx) {
                AssertionFailedException.shouldNotHappen(sqlEx);
            } catch (Throwable th) {
                sqlExRethrow = null;
                if (paramTypesRs != null) {
                    try {
                        paramTypesRs.close();
                    } catch (SQLException sqlEx2) {
                        sqlExRethrow = sqlEx2;
                    }
                }
                if (sqlExRethrow != null) {
                    try {
                    } catch (Throwable th2) {
                        Throwable th3 = th2;
                        throw th3;
                    }
                }
            }
            List<?> parseList = StringUtils.splitDBdotName(procName, "", quotedId, this.connection.isNoBackslashEscapesSet());
            String tmpCatalog = "";
            if (parseList.size() == 2) {
                tmpCatalog = (String) parseList.get(0);
                procName = (String) parseList.get(1);
            }
            DatabaseMetaData dbmd = this.connection.getMetaData();
            boolean useCatalog = false;
            if (tmpCatalog.length() <= 0) {
                useCatalog = true;
            }
            String str = (this.connection.versionMeetsMinimum(5, 0, 2) && useCatalog) ? this.currentCatalog : tmpCatalog;
            paramTypesRs = dbmd.getProcedureColumns(str, null, procName, "%");
            boolean hasResults = false;
            try {
                if (paramTypesRs.next()) {
                    paramTypesRs.previous();
                    hasResults = true;
                }
            } catch (Exception e) {
            }
            if (hasResults) {
                convertGetProcedureColumnsToInternalDescriptors(paramTypesRs);
            } else {
                fakeParameterTypes(true);
            }
            SQLException sqlExRethrow2 = null;
            if (paramTypesRs != null) {
                try {
                    paramTypesRs.close();
                } catch (SQLException sqlEx3) {
                    sqlExRethrow2 = sqlEx3;
                }
            }
            if (sqlExRethrow2 != null) {
                try {
                    throw sqlExRethrow2;
                } catch (Throwable th4) {
                    th3 = th4;
                    CallableStatement callableStatement = this;
                    throw th3;
                }
            }
        }
    }

    private void convertGetProcedureColumnsToInternalDescriptors(ResultSet paramTypesRs) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            this.paramInfo = new CallableStatementParamInfo(paramTypesRs);
        }
    }

    public boolean execute() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            checkStreamability();
            setInOutParamsOnServer();
            setOutParams();
            boolean returnVal = super.execute();
            if (this.callingStoredFunction) {
                this.functionReturnValueResults = this.results;
                this.functionReturnValueResults.next();
                this.results = null;
            }
            retrieveOutParams();
            if (this.callingStoredFunction) {
                return false;
            }
            return returnVal;
        }
    }

    public ResultSet executeQuery() throws SQLException {
        ResultSet execResults;
        synchronized (checkClosed().getConnectionMutex()) {
            checkStreamability();
            setInOutParamsOnServer();
            setOutParams();
            execResults = super.executeQuery();
            retrieveOutParams();
        }
        return execResults;
    }

    public int executeUpdate() throws SQLException {
        return Util.truncateAndConvertToInt(executeLargeUpdate());
    }

    private String extractProcedureName() throws SQLException {
        String sanitizedSql = StringUtils.stripComments(this.originalSql, "`\"'", "`\"'", true, false, true, true);
        int endCallIndex = StringUtils.indexOfIgnoreCase(sanitizedSql, "CALL ");
        int offset = 5;
        if (endCallIndex == -1) {
            endCallIndex = StringUtils.indexOfIgnoreCase(sanitizedSql, "SELECT ");
            offset = 7;
        }
        if (endCallIndex != -1) {
            StringBuilder nameBuf = new StringBuilder();
            String trimmedStatement = sanitizedSql.substring(endCallIndex + offset).trim();
            int statementLength = trimmedStatement.length();
            int i = 0;
            while (i < statementLength) {
                char c = trimmedStatement.charAt(i);
                if (Character.isWhitespace(c) || c == '(') {
                    break;
                } else if (c == '?') {
                    break;
                } else {
                    nameBuf.append(c);
                    i++;
                }
            }
            return nameBuf.toString();
        }
        throw SQLError.createSQLException(Messages.getString("CallableStatement.1"), SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
    }

    protected String fixParameterName(String paramNameIn) throws SQLException {
        String mangleParameterName;
        synchronized (checkClosed().getConnectionMutex()) {
            if (paramNameIn != null) {
                if (paramNameIn.length() == 0) {
                }
                if (paramNameIn == null && hasParametersView()) {
                    paramNameIn = "nullpn";
                }
                if (this.connection.getNoAccessToProcedureBodies()) {
                    mangleParameterName = mangleParameterName(paramNameIn);
                } else {
                    throw SQLError.createSQLException("No access to parameters by name when connection has been configured not to access procedure bodies", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                }
            }
            if (!hasParametersView()) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(Messages.getString("CallableStatement.0"));
                stringBuilder.append(paramNameIn);
                throw SQLError.createSQLException(Messages.getString(stringBuilder.toString() == null ? "CallableStatement.15" : "CallableStatement.16"), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            }
            paramNameIn = "nullpn";
            if (this.connection.getNoAccessToProcedureBodies()) {
                mangleParameterName = mangleParameterName(paramNameIn);
            } else {
                throw SQLError.createSQLException("No access to parameters by name when connection has been configured not to access procedure bodies", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            }
        }
        return mangleParameterName;
    }

    public Array getArray(int i) throws SQLException {
        Array retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(i);
            retValue = rs.getArray(mapOutputParameterIndexToRsIndex(i));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public Array getArray(String parameterName) throws SQLException {
        Array retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(null);
            retValue = rs.getArray(fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
        BigDecimal retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
            retValue = rs.getBigDecimal(mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    @Deprecated
    public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
        BigDecimal retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
            retValue = rs.getBigDecimal(mapOutputParameterIndexToRsIndex(parameterIndex), scale);
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public BigDecimal getBigDecimal(String parameterName) throws SQLException {
        BigDecimal retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(null);
            retValue = rs.getBigDecimal(fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public Blob getBlob(int parameterIndex) throws SQLException {
        Blob retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
            retValue = rs.getBlob(mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public Blob getBlob(String parameterName) throws SQLException {
        Blob retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(null);
            retValue = rs.getBlob(fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public boolean getBoolean(int parameterIndex) throws SQLException {
        boolean retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
            retValue = rs.getBoolean(mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public boolean getBoolean(String parameterName) throws SQLException {
        boolean retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(null);
            retValue = rs.getBoolean(fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public byte getByte(int parameterIndex) throws SQLException {
        byte retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
            retValue = rs.getByte(mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public byte getByte(String parameterName) throws SQLException {
        byte retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(null);
            retValue = rs.getByte(fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public byte[] getBytes(int parameterIndex) throws SQLException {
        byte[] retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
            retValue = rs.getBytes(mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public byte[] getBytes(String parameterName) throws SQLException {
        byte[] retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(null);
            retValue = rs.getBytes(fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public Clob getClob(int parameterIndex) throws SQLException {
        Clob retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
            retValue = rs.getClob(mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public Clob getClob(String parameterName) throws SQLException {
        Clob retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(null);
            retValue = rs.getClob(fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public Date getDate(int parameterIndex) throws SQLException {
        Date retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
            retValue = rs.getDate(mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
        Date retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
            retValue = rs.getDate(mapOutputParameterIndexToRsIndex(parameterIndex), cal);
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public Date getDate(String parameterName) throws SQLException {
        Date retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(null);
            retValue = rs.getDate(fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public Date getDate(String parameterName, Calendar cal) throws SQLException {
        Date retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(null);
            retValue = rs.getDate(fixParameterName(parameterName), cal);
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public double getDouble(int parameterIndex) throws SQLException {
        double retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
            retValue = rs.getDouble(mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public double getDouble(String parameterName) throws SQLException {
        double retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(null);
            retValue = rs.getDouble(fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public float getFloat(int parameterIndex) throws SQLException {
        float retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
            retValue = rs.getFloat(mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public float getFloat(String parameterName) throws SQLException {
        float retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(null);
            retValue = rs.getFloat(fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public int getInt(int parameterIndex) throws SQLException {
        int retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
            retValue = rs.getInt(mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public int getInt(String parameterName) throws SQLException {
        int retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(null);
            retValue = rs.getInt(fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public long getLong(int parameterIndex) throws SQLException {
        long retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
            retValue = rs.getLong(mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public long getLong(String parameterName) throws SQLException {
        long retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(null);
            retValue = rs.getLong(fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    protected int getNamedParamIndex(String paramName, boolean forOut) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.connection.getNoAccessToProcedureBodies()) {
                throw SQLError.createSQLException("No access to parameters by name when connection has been configured not to access procedure bodies", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            }
            if (paramName != null) {
                if (paramName.length() != 0) {
                    StringBuilder stringBuilder;
                    if (this.paramInfo != null) {
                        CallableStatementParam parameter = this.paramInfo.getParameter(paramName);
                        CallableStatementParam namedParamInfo = parameter;
                        if (parameter != null) {
                            if (forOut && !namedParamInfo.isOut) {
                                stringBuilder = new StringBuilder();
                                stringBuilder.append(Messages.getString("CallableStatement.5"));
                                stringBuilder.append(paramName);
                                stringBuilder.append(Messages.getString("CallableStatement.6"));
                                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                            } else if (this.placeholderToParameterIndexMap == null) {
                                r1 = namedParamInfo.index + 1;
                                return r1;
                            } else {
                                for (r1 = 0; r1 < this.placeholderToParameterIndexMap.length; r1++) {
                                    if (this.placeholderToParameterIndexMap[r1] == namedParamInfo.index) {
                                        int i = r1 + 1;
                                        return i;
                                    }
                                }
                                stringBuilder = new StringBuilder();
                                stringBuilder.append("Can't find local placeholder mapping for parameter named \"");
                                stringBuilder.append(paramName);
                                stringBuilder.append("\".");
                                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                            }
                        }
                    }
                    stringBuilder = new StringBuilder();
                    stringBuilder.append(Messages.getString("CallableStatement.3"));
                    stringBuilder.append(paramName);
                    stringBuilder.append(Messages.getString("CallableStatement.4"));
                    throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                }
            }
            throw SQLError.createSQLException(Messages.getString("CallableStatement.2"), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
    }

    public Object getObject(int parameterIndex) throws SQLException {
        Object retVal;
        synchronized (checkClosed().getConnectionMutex()) {
            CallableStatementParam paramDescriptor = checkIsOutputParam(parameterIndex);
            ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
            retVal = rs.getObjectStoredProc(mapOutputParameterIndexToRsIndex(parameterIndex), paramDescriptor.desiredJdbcType);
            this.outputParamWasNull = rs.wasNull();
        }
        return retVal;
    }

    public Object getObject(int parameterIndex, Map<String, Class<?>> map) throws SQLException {
        Object retVal;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
            retVal = rs.getObject(mapOutputParameterIndexToRsIndex(parameterIndex), map);
            this.outputParamWasNull = rs.wasNull();
        }
        return retVal;
    }

    public Object getObject(String parameterName) throws SQLException {
        Object retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(null);
            retValue = rs.getObject(fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public Object getObject(String parameterName, Map<String, Class<?>> map) throws SQLException {
        Object retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(null);
            retValue = rs.getObject(fixParameterName(parameterName), map);
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException {
        T retVal;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
            retVal = ((ResultSetImpl) rs).getObject(mapOutputParameterIndexToRsIndex(parameterIndex), (Class) type);
            this.outputParamWasNull = rs.wasNull();
        }
        return retVal;
    }

    public <T> T getObject(String parameterName, Class<T> type) throws SQLException {
        T retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(null);
            retValue = ((ResultSetImpl) rs).getObject(fixParameterName(parameterName), (Class) type);
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    protected ResultSetInternalMethods getOutputParameters(int paramIndex) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            this.outputParamWasNull = false;
            ResultSetInternalMethods resultSetInternalMethods;
            if (paramIndex == 1 && this.callingStoredFunction && this.returnValueParam != null) {
                resultSetInternalMethods = this.functionReturnValueResults;
                return resultSetInternalMethods;
            } else if (this.outputParameterResults != null) {
                resultSetInternalMethods = this.outputParameterResults;
                return resultSetInternalMethods;
            } else if (this.paramInfo.numberOfParameters() == 0) {
                throw SQLError.createSQLException(Messages.getString("CallableStatement.7"), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            } else {
                throw SQLError.createSQLException(Messages.getString("CallableStatement.8"), SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
            }
        }
    }

    public ParameterMetaData getParameterMetaData() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.placeholderToParameterIndexMap == null) {
                ParameterMetaData parameterMetaData = this.paramInfo;
                return parameterMetaData;
            }
            parameterMetaData = new CallableStatementParamInfo(this.paramInfo);
            return parameterMetaData;
        }
    }

    public Ref getRef(int parameterIndex) throws SQLException {
        Ref retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
            retValue = rs.getRef(mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public Ref getRef(String parameterName) throws SQLException {
        Ref retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(null);
            retValue = rs.getRef(fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public short getShort(int parameterIndex) throws SQLException {
        short retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
            retValue = rs.getShort(mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public short getShort(String parameterName) throws SQLException {
        short retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(null);
            retValue = rs.getShort(fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public String getString(int parameterIndex) throws SQLException {
        String retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
            retValue = rs.getString(mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public String getString(String parameterName) throws SQLException {
        String retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(null);
            retValue = rs.getString(fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public Time getTime(int parameterIndex) throws SQLException {
        Time retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
            retValue = rs.getTime(mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
        Time retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
            retValue = rs.getTime(mapOutputParameterIndexToRsIndex(parameterIndex), cal);
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public Time getTime(String parameterName) throws SQLException {
        Time retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(null);
            retValue = rs.getTime(fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public Time getTime(String parameterName, Calendar cal) throws SQLException {
        Time retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(null);
            retValue = rs.getTime(fixParameterName(parameterName), cal);
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public Timestamp getTimestamp(int parameterIndex) throws SQLException {
        Timestamp retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
            retValue = rs.getTimestamp(mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException {
        Timestamp retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
            retValue = rs.getTimestamp(mapOutputParameterIndexToRsIndex(parameterIndex), cal);
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public Timestamp getTimestamp(String parameterName) throws SQLException {
        Timestamp retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(null);
            retValue = rs.getTimestamp(fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException {
        Timestamp retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(null);
            retValue = rs.getTimestamp(fixParameterName(parameterName), cal);
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public URL getURL(int parameterIndex) throws SQLException {
        URL retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
            retValue = rs.getURL(mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    public URL getURL(String parameterName) throws SQLException {
        URL retValue;
        synchronized (checkClosed().getConnectionMutex()) {
            ResultSetInternalMethods rs = getOutputParameters(null);
            retValue = rs.getURL(fixParameterName(parameterName));
            this.outputParamWasNull = rs.wasNull();
        }
        return retValue;
    }

    protected int mapOutputParameterIndexToRsIndex(int paramIndex) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.returnValueParam == null || paramIndex != 1) {
                checkParameterIndexBounds(paramIndex);
                int localParamIndex = paramIndex - 1;
                if (this.placeholderToParameterIndexMap != null) {
                    localParamIndex = this.placeholderToParameterIndexMap[localParamIndex];
                }
                int rsIndex = this.parameterIndexToRsIndex[localParamIndex];
                if (rsIndex == Integer.MIN_VALUE) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(Messages.getString("CallableStatement.21"));
                    stringBuilder.append(paramIndex);
                    stringBuilder.append(Messages.getString("CallableStatement.22"));
                    throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                }
                int i = rsIndex + 1;
                return i;
            }
            return 1;
        }
    }

    public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException {
        checkIsOutputParam(parameterIndex).desiredJdbcType = sqlType;
    }

    public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException {
        registerOutParameter(parameterIndex, sqlType);
    }

    public void registerOutParameter(int parameterIndex, int sqlType, String typeName) throws SQLException {
        checkIsOutputParam(parameterIndex);
    }

    public void registerOutParameter(String parameterName, int sqlType) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            registerOutParameter(getNamedParamIndex(parameterName, true), sqlType);
        }
    }

    public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException {
        registerOutParameter(getNamedParamIndex(parameterName, true), sqlType);
    }

    public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException {
        registerOutParameter(getNamedParamIndex(parameterName, true), sqlType, typeName);
    }

    private void retrieveOutParams() throws SQLException {
        CallableStatement this;
        Throwable th;
        synchronized (checkClosed().getConnectionMutex()) {
            CallableStatement outParamRs;
            try {
                int i;
                int numParameters = this.paramInfo.numberOfParameters();
                this.parameterIndexToRsIndex = new int[numParameters];
                for (i = 0; i < numParameters; i++) {
                    this.parameterIndexToRsIndex[i] = Integer.MIN_VALUE;
                }
                i = 0;
                if (numParameters > 0) {
                    StringBuilder outParameterQuery = new StringBuilder("SELECT ");
                    boolean firstParam = true;
                    boolean hadOutputParams = false;
                    Iterator<CallableStatementParam> paramIter = this.paramInfo.iterator();
                    while (paramIter.hasNext()) {
                        CallableStatementParam retrParamInfo = (CallableStatementParam) paramIter.next();
                        if (retrParamInfo.isOut) {
                            hadOutputParams = true;
                            int localParamIndex = i + 1;
                            this.parameterIndexToRsIndex[retrParamInfo.index] = i;
                            if (retrParamInfo.paramName == null && hasParametersView()) {
                                StringBuilder stringBuilder = new StringBuilder();
                                stringBuilder.append("nullnp");
                                stringBuilder.append(retrParamInfo.index);
                                retrParamInfo.paramName = stringBuilder.toString();
                            }
                            String outParameterName = mangleParameterName(retrParamInfo.paramName);
                            if (firstParam) {
                                firstParam = false;
                            } else {
                                outParameterQuery.append(",");
                            }
                            if (!outParameterName.startsWith("@")) {
                                outParameterQuery.append('@');
                            }
                            outParameterQuery.append(outParameterName);
                            i = localParamIndex;
                        }
                    }
                    if (hadOutputParams) {
                        Statement outParameterStmt = null;
                        ResultSet outParamRs2 = null;
                        try {
                            outParameterStmt = this.connection.createStatement();
                            outParamRs2 = outParameterStmt.executeQuery(outParameterQuery.toString());
                            this.outputParameterResults = ((ResultSetInternalMethods) outParamRs2).copy();
                            if (!this.outputParameterResults.next()) {
                                this.outputParameterResults.close();
                                this.outputParameterResults = null;
                            }
                            Statement outParameterStmt2 = outParameterStmt;
                            ResultSet outParameterStmt3 = outParamRs2;
                            outParamRs = this;
                            if (outParameterStmt2 != null) {
                                outParameterStmt2.close();
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            outParamRs = this;
                            throw th;
                        }
                    }
                    this.outputParameterResults = null;
                } else {
                    this.outputParameterResults = null;
                    outParamRs = this;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException {
        setAsciiStream(getNamedParamIndex(parameterName, false), x, length);
    }

    public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
        setBigDecimal(getNamedParamIndex(parameterName, false), x);
    }

    public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException {
        setBinaryStream(getNamedParamIndex(parameterName, false), x, length);
    }

    public void setBoolean(String parameterName, boolean x) throws SQLException {
        setBoolean(getNamedParamIndex(parameterName, false), x);
    }

    public void setByte(String parameterName, byte x) throws SQLException {
        setByte(getNamedParamIndex(parameterName, false), x);
    }

    public void setBytes(String parameterName, byte[] x) throws SQLException {
        setBytes(getNamedParamIndex(parameterName, false), x);
    }

    public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException {
        setCharacterStream(getNamedParamIndex(parameterName, false), reader, length);
    }

    public void setDate(String parameterName, Date x) throws SQLException {
        setDate(getNamedParamIndex(parameterName, false), x);
    }

    public void setDate(String parameterName, Date x, Calendar cal) throws SQLException {
        setDate(getNamedParamIndex(parameterName, false), x, cal);
    }

    public void setDouble(String parameterName, double x) throws SQLException {
        setDouble(getNamedParamIndex(parameterName, false), x);
    }

    public void setFloat(String parameterName, float x) throws SQLException {
        setFloat(getNamedParamIndex(parameterName, false), x);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setInOutParamsOnServer() throws java.sql.SQLException {
        /*
        r13 = this;
        r0 = r13.checkClosed();
        r0 = r0.getConnectionMutex();
        monitor-enter(r0);
        r1 = r13.paramInfo;	 Catch:{ all -> 0x010b }
        r1 = r1.numParameters;	 Catch:{ all -> 0x010b }
        if (r1 <= 0) goto L_0x0108;
    L_0x000f:
        r1 = r13.paramInfo;	 Catch:{ all -> 0x010b }
        r1 = r1.iterator();	 Catch:{ all -> 0x010b }
        r2 = r13;
    L_0x0016:
        r3 = r1.hasNext();	 Catch:{ all -> 0x010f }
        if (r3 == 0) goto L_0x0109;
    L_0x001c:
        r3 = r1.next();	 Catch:{ all -> 0x010f }
        r3 = (com.mysql.jdbc.CallableStatement.CallableStatementParam) r3;	 Catch:{ all -> 0x010f }
        r4 = r3.isOut;	 Catch:{ all -> 0x010f }
        if (r4 == 0) goto L_0x0106;
    L_0x0026:
        r4 = r3.isIn;	 Catch:{ all -> 0x010f }
        if (r4 == 0) goto L_0x0106;
    L_0x002a:
        r4 = r3.paramName;	 Catch:{ all -> 0x010f }
        if (r4 != 0) goto L_0x0049;
    L_0x002e:
        r4 = r2.hasParametersView();	 Catch:{ all -> 0x010f }
        if (r4 == 0) goto L_0x0049;
    L_0x0034:
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x010f }
        r4.<init>();	 Catch:{ all -> 0x010f }
        r5 = "nullnp";
        r4.append(r5);	 Catch:{ all -> 0x010f }
        r5 = r3.index;	 Catch:{ all -> 0x010f }
        r4.append(r5);	 Catch:{ all -> 0x010f }
        r4 = r4.toString();	 Catch:{ all -> 0x010f }
        r3.paramName = r4;	 Catch:{ all -> 0x010f }
    L_0x0049:
        r4 = r3.paramName;	 Catch:{ all -> 0x010f }
        r4 = mangleParameterName(r4);	 Catch:{ all -> 0x010f }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x010f }
        r6 = r4.length();	 Catch:{ all -> 0x010f }
        r7 = 4;
        r6 = r6 + r7;
        r8 = 1;
        r6 = r6 + r8;
        r6 = r6 + r8;
        r5.<init>(r6);	 Catch:{ all -> 0x010f }
        r6 = "SET ";
        r5.append(r6);	 Catch:{ all -> 0x010f }
        r5.append(r4);	 Catch:{ all -> 0x010f }
        r6 = "=?";
        r5.append(r6);	 Catch:{ all -> 0x010f }
        r6 = 0;
        r9 = r2.connection;	 Catch:{ all -> 0x00ff }
        r10 = r5.toString();	 Catch:{ all -> 0x00ff }
        r9 = r9.clientPrepareStatement(r10);	 Catch:{ all -> 0x00ff }
        r9 = (com.mysql.jdbc.Wrapper) r9;	 Catch:{ all -> 0x00ff }
        r10 = com.mysql.jdbc.PreparedStatement.class;
        r9 = r9.unwrap(r10);	 Catch:{ all -> 0x00ff }
        r9 = (com.mysql.jdbc.PreparedStatement) r9;	 Catch:{ all -> 0x00ff }
        r6 = r9;
        r9 = r2.isNull;	 Catch:{ all -> 0x00ff }
        r10 = r3.index;	 Catch:{ all -> 0x00ff }
        r9 = r9[r10];	 Catch:{ all -> 0x00ff }
        if (r9 == 0) goto L_0x0092;
    L_0x0088:
        r7 = "NULL";
        r7 = r7.getBytes();	 Catch:{ all -> 0x00ff }
        r6.setBytesNoEscapeNoQuotes(r8, r7);	 Catch:{ all -> 0x00ff }
        goto L_0x00f5;
    L_0x0092:
        r9 = r3.index;	 Catch:{ all -> 0x00ff }
        r9 = r2.getBytesRepresentation(r9);	 Catch:{ all -> 0x00ff }
        r10 = 0;
        if (r9 == 0) goto L_0x00f2;
    L_0x009b:
        r11 = r9.length;	 Catch:{ all -> 0x00ff }
        r12 = 8;
        if (r11 <= r12) goto L_0x00d9;
    L_0x00a0:
        r10 = r9[r10];	 Catch:{ all -> 0x00ff }
        r11 = 95;
        if (r10 != r11) goto L_0x00d9;
    L_0x00a6:
        r10 = r9[r8];	 Catch:{ all -> 0x00ff }
        r11 = 98;
        if (r10 != r11) goto L_0x00d9;
    L_0x00ac:
        r10 = 2;
        r10 = r9[r10];	 Catch:{ all -> 0x00ff }
        r11 = 105; // 0x69 float:1.47E-43 double:5.2E-322;
        if (r10 != r11) goto L_0x00d9;
    L_0x00b3:
        r10 = 3;
        r10 = r9[r10];	 Catch:{ all -> 0x00ff }
        r11 = 110; // 0x6e float:1.54E-43 double:5.43E-322;
        if (r10 != r11) goto L_0x00d9;
    L_0x00ba:
        r7 = r9[r7];	 Catch:{ all -> 0x00ff }
        r10 = 97;
        if (r7 != r10) goto L_0x00d9;
    L_0x00c0:
        r7 = 5;
        r7 = r9[r7];	 Catch:{ all -> 0x00ff }
        r10 = 114; // 0x72 float:1.6E-43 double:5.63E-322;
        if (r7 != r10) goto L_0x00d9;
    L_0x00c7:
        r7 = 6;
        r7 = r9[r7];	 Catch:{ all -> 0x00ff }
        r10 = 121; // 0x79 float:1.7E-43 double:6.0E-322;
        if (r7 != r10) goto L_0x00d9;
    L_0x00ce:
        r7 = 7;
        r7 = r9[r7];	 Catch:{ all -> 0x00ff }
        r10 = 39;
        if (r7 != r10) goto L_0x00d9;
    L_0x00d5:
        r6.setBytesNoEscapeNoQuotes(r8, r9);	 Catch:{ all -> 0x00ff }
        goto L_0x00f5;
    L_0x00d9:
        r7 = r3.desiredJdbcType;	 Catch:{ all -> 0x00ff }
        r10 = -7;
        if (r7 == r10) goto L_0x00ed;
    L_0x00de:
        r10 = 2000; // 0x7d0 float:2.803E-42 double:9.88E-321;
        if (r7 == r10) goto L_0x00ed;
    L_0x00e2:
        r10 = 2004; // 0x7d4 float:2.808E-42 double:9.9E-321;
        if (r7 == r10) goto L_0x00ed;
    L_0x00e6:
        switch(r7) {
            case -4: goto L_0x00ed;
            case -3: goto L_0x00ed;
            case -2: goto L_0x00ed;
            default: goto L_0x00e9;
        };	 Catch:{ all -> 0x00ff }
    L_0x00e9:
        r6.setBytesNoEscape(r8, r9);	 Catch:{ all -> 0x00ff }
        goto L_0x00f1;
    L_0x00ed:
        r6.setBytes(r8, r9);	 Catch:{ all -> 0x00ff }
    L_0x00f1:
        goto L_0x00f5;
    L_0x00f2:
        r6.setNull(r8, r10);	 Catch:{ all -> 0x00ff }
    L_0x00f5:
        r6.executeUpdate();	 Catch:{ all -> 0x00ff }
        if (r6 == 0) goto L_0x00fe;
    L_0x00fb:
        r6.close();	 Catch:{ all -> 0x010f }
    L_0x00fe:
        goto L_0x0106;
    L_0x00ff:
        r7 = move-exception;
        if (r6 == 0) goto L_0x0105;
    L_0x0102:
        r6.close();	 Catch:{ all -> 0x010f }
    L_0x0105:
        throw r7;	 Catch:{ all -> 0x010f }
    L_0x0106:
        goto L_0x0016;
    L_0x0108:
        r2 = r13;
    L_0x0109:
        monitor-exit(r0);	 Catch:{ all -> 0x010f }
        return;
    L_0x010b:
        r1 = move-exception;
        r2 = r13;
    L_0x010d:
        monitor-exit(r0);	 Catch:{ all -> 0x010f }
        throw r1;
    L_0x010f:
        r1 = move-exception;
        goto L_0x010d;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.CallableStatement.setInOutParamsOnServer():void");
    }

    public void setInt(String parameterName, int x) throws SQLException {
        setInt(getNamedParamIndex(parameterName, false), x);
    }

    public void setLong(String parameterName, long x) throws SQLException {
        setLong(getNamedParamIndex(parameterName, false), x);
    }

    public void setNull(String parameterName, int sqlType) throws SQLException {
        setNull(getNamedParamIndex(parameterName, false), sqlType);
    }

    public void setNull(String parameterName, int sqlType, String typeName) throws SQLException {
        setNull(getNamedParamIndex(parameterName, false), sqlType, typeName);
    }

    public void setObject(String parameterName, Object x) throws SQLException {
        setObject(getNamedParamIndex(parameterName, false), x);
    }

    public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException {
        setObject(getNamedParamIndex(parameterName, false), x, targetSqlType);
    }

    public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException {
    }

    private void setOutParams() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.paramInfo.numParameters > 0) {
                Iterator<CallableStatementParam> paramIter = this.paramInfo.iterator();
                while (paramIter.hasNext()) {
                    CallableStatementParam outParamInfo = (CallableStatementParam) paramIter.next();
                    if (!this.callingStoredFunction && outParamInfo.isOut) {
                        if (outParamInfo.paramName == null && hasParametersView()) {
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("nullnp");
                            stringBuilder.append(outParamInfo.index);
                            outParamInfo.paramName = stringBuilder.toString();
                        }
                        String outParameterName = mangleParameterName(outParamInfo.paramName);
                        int outParamIndex = 0;
                        if (this.placeholderToParameterIndexMap == null) {
                            outParamIndex = outParamInfo.index + 1;
                        } else {
                            boolean found = false;
                            for (int i = 0; i < this.placeholderToParameterIndexMap.length; i++) {
                                if (this.placeholderToParameterIndexMap[i] == outParamInfo.index) {
                                    outParamIndex = i + 1;
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                StringBuilder stringBuilder2 = new StringBuilder();
                                stringBuilder2.append(Messages.getString("CallableStatement.21"));
                                stringBuilder2.append(outParamInfo.paramName);
                                stringBuilder2.append(Messages.getString("CallableStatement.22"));
                                throw SQLError.createSQLException(stringBuilder2.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                            }
                        }
                        setBytesNoEscapeNoQuotes(outParamIndex, StringUtils.getBytes(outParameterName, this.charConverter, this.charEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), getExceptionInterceptor()));
                    }
                }
            }
        }
    }

    public void setShort(String parameterName, short x) throws SQLException {
        setShort(getNamedParamIndex(parameterName, false), x);
    }

    public void setString(String parameterName, String x) throws SQLException {
        setString(getNamedParamIndex(parameterName, false), x);
    }

    public void setTime(String parameterName, Time x) throws SQLException {
        setTime(getNamedParamIndex(parameterName, false), x);
    }

    public void setTime(String parameterName, Time x, Calendar cal) throws SQLException {
        setTime(getNamedParamIndex(parameterName, false), x, cal);
    }

    public void setTimestamp(String parameterName, Timestamp x) throws SQLException {
        setTimestamp(getNamedParamIndex(parameterName, false), x);
    }

    public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException {
        setTimestamp(getNamedParamIndex(parameterName, false), x, cal);
    }

    public void setURL(String parameterName, URL val) throws SQLException {
        setURL(getNamedParamIndex(parameterName, false), val);
    }

    public boolean wasNull() throws SQLException {
        boolean z;
        synchronized (checkClosed().getConnectionMutex()) {
            z = this.outputParamWasNull;
        }
        return z;
    }

    public int[] executeBatch() throws SQLException {
        return Util.truncateAndConvertToInt(executeLargeBatch());
    }

    protected int getParameterIndexOffset() {
        if (this.callingStoredFunction) {
            return -1;
        }
        return super.getParameterIndexOffset();
    }

    public void setAsciiStream(String parameterName, InputStream x) throws SQLException {
        setAsciiStream(getNamedParamIndex(parameterName, false), x);
    }

    public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException {
        setAsciiStream(getNamedParamIndex(parameterName, false), x, length);
    }

    public void setBinaryStream(String parameterName, InputStream x) throws SQLException {
        setBinaryStream(getNamedParamIndex(parameterName, false), x);
    }

    public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException {
        setBinaryStream(getNamedParamIndex(parameterName, false), x, length);
    }

    public void setBlob(String parameterName, Blob x) throws SQLException {
        setBlob(getNamedParamIndex(parameterName, false), x);
    }

    public void setBlob(String parameterName, InputStream inputStream) throws SQLException {
        setBlob(getNamedParamIndex(parameterName, false), inputStream);
    }

    public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException {
        setBlob(getNamedParamIndex(parameterName, false), inputStream, length);
    }

    public void setCharacterStream(String parameterName, Reader reader) throws SQLException {
        setCharacterStream(getNamedParamIndex(parameterName, false), reader);
    }

    public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException {
        setCharacterStream(getNamedParamIndex(parameterName, false), reader, length);
    }

    public void setClob(String parameterName, Clob x) throws SQLException {
        setClob(getNamedParamIndex(parameterName, false), x);
    }

    public void setClob(String parameterName, Reader reader) throws SQLException {
        setClob(getNamedParamIndex(parameterName, false), reader);
    }

    public void setClob(String parameterName, Reader reader, long length) throws SQLException {
        setClob(getNamedParamIndex(parameterName, false), reader, length);
    }

    public void setNCharacterStream(String parameterName, Reader value) throws SQLException {
        setNCharacterStream(getNamedParamIndex(parameterName, false), value);
    }

    public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException {
        setNCharacterStream(getNamedParamIndex(parameterName, false), value, length);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean checkReadOnlyProcedure() throws java.sql.SQLException {
        /*
        r10 = this;
        r0 = r10.checkClosed();
        r0 = r0.getConnectionMutex();
        monitor-enter(r0);
        r1 = r10.connection;	 Catch:{ all -> 0x00f8 }
        r1 = r1.getNoAccessToProcedureBodies();	 Catch:{ all -> 0x00f8 }
        r2 = 0;
        if (r1 == 0) goto L_0x0014;
    L_0x0012:
        monitor-exit(r0);	 Catch:{ all -> 0x00f8 }
        return r2;
    L_0x0014:
        r1 = r10.paramInfo;	 Catch:{ all -> 0x00f8 }
        r1 = r1.isReadOnlySafeChecked;	 Catch:{ all -> 0x00f8 }
        if (r1 == 0) goto L_0x0020;
    L_0x001a:
        r1 = r10.paramInfo;	 Catch:{ all -> 0x00f8 }
        r1 = r1.isReadOnlySafeProcedure;	 Catch:{ all -> 0x00f8 }
        monitor-exit(r0);	 Catch:{ all -> 0x00f8 }
        return r1;
    L_0x0020:
        r1 = 0;
        r3 = 0;
        r4 = r10.extractProcedureName();	 Catch:{ SQLException -> 0x00e0, all -> 0x00d3 }
        r5 = r10.currentCatalog;	 Catch:{ SQLException -> 0x00e0, all -> 0x00d3 }
        r6 = ".";
        r6 = r4.indexOf(r6);	 Catch:{ SQLException -> 0x00e0, all -> 0x00d3 }
        r7 = -1;
        r8 = 1;
        if (r6 == r7) goto L_0x0078;
    L_0x0032:
        r6 = ".";
        r6 = r4.indexOf(r6);	 Catch:{ SQLException -> 0x00e0, all -> 0x00d3 }
        r6 = r4.substring(r2, r6);	 Catch:{ SQLException -> 0x00e0, all -> 0x00d3 }
        r5 = r6;
        r6 = "`";
        r6 = com.mysql.jdbc.StringUtils.startsWithIgnoreCaseAndWs(r5, r6);	 Catch:{ SQLException -> 0x00e0, all -> 0x00d3 }
        if (r6 == 0) goto L_0x005b;
    L_0x0045:
        r6 = r5.trim();	 Catch:{ SQLException -> 0x00e0, all -> 0x00d3 }
        r7 = "`";
        r6 = r6.endsWith(r7);	 Catch:{ SQLException -> 0x00e0, all -> 0x00d3 }
        if (r6 == 0) goto L_0x005b;
    L_0x0051:
        r6 = r5.length();	 Catch:{ SQLException -> 0x00e0, all -> 0x00d3 }
        r6 = r6 - r8;
        r6 = r5.substring(r8, r6);	 Catch:{ SQLException -> 0x00e0, all -> 0x00d3 }
        r5 = r6;
    L_0x005b:
        r6 = ".";
        r6 = r4.indexOf(r6);	 Catch:{ SQLException -> 0x00e0, all -> 0x00d3 }
        r6 = r6 + r8;
        r6 = r4.substring(r6);	 Catch:{ SQLException -> 0x00e0, all -> 0x00d3 }
        r4 = r6;
        r6 = com.mysql.jdbc.StringUtils.getBytes(r4);	 Catch:{ SQLException -> 0x00e0, all -> 0x00d3 }
        r7 = "`";
        r9 = "`";
        r6 = com.mysql.jdbc.StringUtils.stripEnclosure(r6, r7, r9);	 Catch:{ SQLException -> 0x00e0, all -> 0x00d3 }
        r6 = com.mysql.jdbc.StringUtils.toString(r6);	 Catch:{ SQLException -> 0x00e0, all -> 0x00d3 }
        r4 = r6;
    L_0x0078:
        r6 = r10.connection;	 Catch:{ SQLException -> 0x00e0, all -> 0x00d3 }
        r7 = "SELECT SQL_DATA_ACCESS FROM information_schema.routines WHERE routine_schema = ? AND routine_name = ?";
        r6 = r6.prepareStatement(r7);	 Catch:{ SQLException -> 0x00e0, all -> 0x00d3 }
        r3 = r6;
        r3.setMaxRows(r2);	 Catch:{ SQLException -> 0x00e0, all -> 0x00d3 }
        r3.setFetchSize(r2);	 Catch:{ SQLException -> 0x00e0, all -> 0x00d3 }
        r3.setString(r8, r5);	 Catch:{ SQLException -> 0x00e0, all -> 0x00d3 }
        r6 = 2;
        r3.setString(r6, r4);	 Catch:{ SQLException -> 0x00e0, all -> 0x00d3 }
        r6 = r3.executeQuery();	 Catch:{ SQLException -> 0x00e0, all -> 0x00d3 }
        r1 = r6;
        r6 = r1.next();	 Catch:{ SQLException -> 0x00e0, all -> 0x00d3 }
        if (r6 == 0) goto L_0x00c9;
    L_0x0099:
        r6 = r1.getString(r8);	 Catch:{ SQLException -> 0x00e0, all -> 0x00d3 }
        r7 = "READS SQL DATA";
        r7 = r7.equalsIgnoreCase(r6);	 Catch:{ SQLException -> 0x00e0, all -> 0x00d3 }
        if (r7 != 0) goto L_0x00ad;
    L_0x00a5:
        r7 = "NO SQL";
        r7 = r7.equalsIgnoreCase(r6);	 Catch:{ SQLException -> 0x00e0, all -> 0x00d3 }
        if (r7 == 0) goto L_0x00c9;
    L_0x00ad:
        r7 = r10.paramInfo;	 Catch:{ SQLException -> 0x00e0, all -> 0x00d3 }
        monitor-enter(r7);	 Catch:{ SQLException -> 0x00e0, all -> 0x00d3 }
        r9 = r10.paramInfo;	 Catch:{ all -> 0x00c6 }
        r9.isReadOnlySafeChecked = r8;	 Catch:{ all -> 0x00c6 }
        r9 = r10.paramInfo;	 Catch:{ all -> 0x00c6 }
        r9.isReadOnlySafeProcedure = r8;	 Catch:{ all -> 0x00c6 }
        monitor-exit(r7);	 Catch:{ all -> 0x00c6 }
        if (r1 == 0) goto L_0x00bf;
    L_0x00bc:
        r1.close();	 Catch:{ all -> 0x00f8 }
    L_0x00bf:
        if (r3 == 0) goto L_0x00c4;
    L_0x00c1:
        r3.close();	 Catch:{ all -> 0x00f8 }
    L_0x00c4:
        monitor-exit(r0);	 Catch:{ all -> 0x00f8 }
        return r8;
    L_0x00c6:
        r8 = move-exception;
        monitor-exit(r7);	 Catch:{ all -> 0x00c6 }
        throw r8;	 Catch:{ SQLException -> 0x00e0, all -> 0x00d3 }
        r4 = r10;
        if (r1 == 0) goto L_0x00d0;
    L_0x00cd:
        r1.close();	 Catch:{ all -> 0x00fc }
    L_0x00d0:
        if (r3 == 0) goto L_0x00ed;
    L_0x00d2:
        goto L_0x00ea;
    L_0x00d3:
        r2 = move-exception;
        r4 = r10;
        if (r1 == 0) goto L_0x00da;
    L_0x00d7:
        r1.close();	 Catch:{ all -> 0x00fc }
    L_0x00da:
        if (r3 == 0) goto L_0x00df;
    L_0x00dc:
        r3.close();	 Catch:{ all -> 0x00fc }
    L_0x00df:
        throw r2;	 Catch:{ all -> 0x00fc }
    L_0x00e0:
        r4 = move-exception;
        r4 = r10;
        if (r1 == 0) goto L_0x00e8;
    L_0x00e5:
        r1.close();	 Catch:{ all -> 0x00fc }
    L_0x00e8:
        if (r3 == 0) goto L_0x00ed;
    L_0x00ea:
        r3.close();	 Catch:{ all -> 0x00fc }
        r5 = r4.paramInfo;	 Catch:{ all -> 0x00fc }
        r5.isReadOnlySafeChecked = r2;	 Catch:{ all -> 0x00fc }
        r5 = r4.paramInfo;	 Catch:{ all -> 0x00fc }
        r5.isReadOnlySafeProcedure = r2;	 Catch:{ all -> 0x00fc }
        monitor-exit(r0);	 Catch:{ all -> 0x00fc }
        return r2;
    L_0x00f8:
        r1 = move-exception;
        r4 = r10;
    L_0x00fa:
        monitor-exit(r0);	 Catch:{ all -> 0x00fc }
        throw r1;
    L_0x00fc:
        r1 = move-exception;
        goto L_0x00fa;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.CallableStatement.checkReadOnlyProcedure():boolean");
    }

    protected boolean checkReadOnlySafeStatement() throws SQLException {
        if (!super.checkReadOnlySafeStatement()) {
            if (!checkReadOnlyProcedure()) {
                return false;
            }
        }
        return true;
    }

    private boolean hasParametersView() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            try {
                if (this.connection.versionMeetsMinimum(5, 5, 0)) {
                    boolean z = ((DatabaseMetaDataUsingInfoSchema) new DatabaseMetaDataUsingInfoSchema(this.connection, this.connection.getCatalog())).gethasParametersView();
                    return z;
                }
                return false;
            } catch (SQLException e) {
                return false;
            }
        }
    }

    public long executeLargeUpdate() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            checkStreamability();
            if (this.callingStoredFunction) {
                execute();
                return -1;
            }
            setInOutParamsOnServer();
            setOutParams();
            long returnVal = super.executeLargeUpdate();
            retrieveOutParams();
            return returnVal;
        }
    }

    public long[] executeLargeBatch() throws SQLException {
        if (!this.hasOutputParams) {
            return super.executeLargeBatch();
        }
        throw SQLError.createSQLException("Can't call executeBatch() on CallableStatement with OUTPUT parameters", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
    }
}
