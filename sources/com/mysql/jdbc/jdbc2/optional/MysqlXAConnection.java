package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Messages;
import com.mysql.jdbc.MysqlErrorNumbers;
import com.mysql.jdbc.StringUtils;
import com.mysql.jdbc.Util;
import com.mysql.jdbc.log.Log;
import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.XAConnection;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class MysqlXAConnection extends MysqlPooledConnection implements XAConnection, XAResource {
    private static final Constructor<?> JDBC_4_XA_CONNECTION_WRAPPER_CTOR;
    private static final int MAX_COMMAND_LENGTH = 300;
    private static final Map<Integer, Integer> MYSQL_ERROR_CODES_TO_XA_ERROR_CODES;
    private Log log;
    protected boolean logXaCommands;
    private Connection underlyingConnection;

    static {
        HashMap<Integer, Integer> temp = new HashMap();
        temp.put(Integer.valueOf(MysqlErrorNumbers.ER_XAER_NOTA), Integer.valueOf(-4));
        temp.put(Integer.valueOf(MysqlErrorNumbers.ER_XAER_INVAL), Integer.valueOf(-5));
        temp.put(Integer.valueOf(MysqlErrorNumbers.ER_XAER_RMFAIL), Integer.valueOf(-7));
        temp.put(Integer.valueOf(MysqlErrorNumbers.ER_XAER_OUTSIDE), Integer.valueOf(-9));
        temp.put(Integer.valueOf(MysqlErrorNumbers.ER_XA_RMERR), Integer.valueOf(-3));
        temp.put(Integer.valueOf(MysqlErrorNumbers.ER_XA_RBROLLBACK), Integer.valueOf(100));
        temp.put(Integer.valueOf(MysqlErrorNumbers.ER_XAER_DUPID), Integer.valueOf(-8));
        temp.put(Integer.valueOf(MysqlErrorNumbers.ER_XA_RBTIMEOUT), Integer.valueOf(106));
        temp.put(Integer.valueOf(MysqlErrorNumbers.ER_XA_RBDEADLOCK), Integer.valueOf(102));
        MYSQL_ERROR_CODES_TO_XA_ERROR_CODES = Collections.unmodifiableMap(temp);
        if (Util.isJdbc4()) {
            try {
                JDBC_4_XA_CONNECTION_WRAPPER_CTOR = Class.forName("com.mysql.jdbc.jdbc2.optional.JDBC4MysqlXAConnection").getConstructor(new Class[]{Connection.class, Boolean.TYPE});
                return;
            } catch (SecurityException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e2) {
                throw new RuntimeException(e2);
            } catch (ClassNotFoundException e3) {
                throw new RuntimeException(e3);
            }
        }
        JDBC_4_XA_CONNECTION_WRAPPER_CTOR = null;
    }

    protected static MysqlXAConnection getInstance(Connection mysqlConnection, boolean logXaCommands) throws SQLException {
        if (!Util.isJdbc4()) {
            return new MysqlXAConnection(mysqlConnection, logXaCommands);
        }
        return (MysqlXAConnection) Util.handleNewInstance(JDBC_4_XA_CONNECTION_WRAPPER_CTOR, new Object[]{mysqlConnection, Boolean.valueOf(logXaCommands)}, mysqlConnection.getExceptionInterceptor());
    }

    public MysqlXAConnection(Connection connection, boolean logXaCommands) throws SQLException {
        super(connection);
        this.underlyingConnection = connection;
        this.log = connection.getLog();
        this.logXaCommands = logXaCommands;
    }

    public XAResource getXAResource() throws SQLException {
        return this;
    }

    public int getTransactionTimeout() throws XAException {
        return 0;
    }

    public boolean setTransactionTimeout(int arg0) throws XAException {
        return false;
    }

    public boolean isSameRM(XAResource xares) throws XAException {
        if (xares instanceof MysqlXAConnection) {
            return this.underlyingConnection.isSameResource(((MysqlXAConnection) xares).underlyingConnection);
        }
        return false;
    }

    public Xid[] recover(int flag) throws XAException {
        return recover(this.underlyingConnection, flag);
    }

    protected static Xid[] recover(java.sql.Connection c, int flag) throws XAException {
        SQLException sqlEx;
        java.sql.Connection connection;
        int i;
        ResultSet rs;
        int i2 = 1;
        int i3 = 0;
        boolean startRscan = (flag & 16777216) > 0;
        boolean endRscan = (flag & 8388608) > 0;
        if (!startRscan && !endRscan && flag != 0) {
            throw new MysqlXAException(-5, Messages.getString("MysqlXAConnection.001"), null);
        } else if (!startRscan) {
            return new Xid[0];
        } else {
            ResultSet rs2 = null;
            Statement stmt = null;
            List<MysqlXid> recoveredXidList = new ArrayList();
            try {
                stmt = c.createStatement();
                Statement stmt2;
                Statement stmt3;
                try {
                    rs2 = stmt.executeQuery("XA RECOVER");
                    while (rs2.next()) {
                        int formatId = rs2.getInt(i2);
                        int gtridLength = rs2.getInt(2);
                        int bqualLength = rs2.getInt(3);
                        byte[] gtridAndBqual = rs2.getBytes(4);
                        byte[] gtrid = new byte[gtridLength];
                        byte[] bqual = new byte[bqualLength];
                        if (gtridAndBqual.length != gtridLength + bqualLength) {
                            stmt2 = stmt;
                            try {
                                throw new MysqlXAException(105, Messages.getString("MysqlXAConnection.002"), null);
                            } catch (SQLException e) {
                                sqlEx = e;
                                stmt = stmt2;
                                try {
                                    throw mapXAExceptionFromSQLException(sqlEx);
                                } catch (Throwable th) {
                                    sqlEx = th;
                                    stmt2 = stmt;
                                    connection = c;
                                    i = flag;
                                    rs = rs2;
                                    stmt3 = stmt2;
                                    if (rs != null) {
                                        try {
                                            rs.close();
                                        } catch (SQLException e2) {
                                            throw mapXAExceptionFromSQLException(e2);
                                        }
                                    }
                                    if (stmt3 != null) {
                                        try {
                                            stmt3.close();
                                        } catch (SQLException e22) {
                                            throw mapXAExceptionFromSQLException(e22);
                                        }
                                    }
                                    throw sqlEx;
                                }
                            } catch (Throwable th2) {
                                sqlEx = th2;
                                connection = c;
                                i = flag;
                                rs = rs2;
                                stmt3 = stmt2;
                                if (rs != null) {
                                    rs.close();
                                }
                                if (stmt3 != null) {
                                    stmt3.close();
                                }
                                throw sqlEx;
                            }
                        }
                        stmt2 = stmt;
                        System.arraycopy(gtridAndBqual, 0, gtrid, 0, gtridLength);
                        System.arraycopy(gtridAndBqual, gtridLength, bqual, 0, bqualLength);
                        recoveredXidList.add(new MysqlXid(gtrid, bqual, formatId));
                        i3 = 0;
                        stmt = stmt2;
                        i2 = 1;
                    }
                    rs = rs2;
                    stmt3 = stmt;
                    List<MysqlXid> stmt4 = recoveredXidList;
                    if (rs != null) {
                        try {
                            rs.close();
                        } catch (SQLException e222) {
                            throw mapXAExceptionFromSQLException(e222);
                        }
                    }
                    if (stmt3 != null) {
                        try {
                            stmt3.close();
                        } catch (SQLException e2222) {
                            throw mapXAExceptionFromSQLException(e2222);
                        }
                    }
                    recoveredXidList = stmt4.size();
                    Xid[] asXids = new Xid[recoveredXidList];
                    Object[] asObjects = stmt4.toArray();
                    for (i2 = i3; i2 < recoveredXidList; i2++) {
                        asXids[i2] = (Xid) asObjects[i2];
                    }
                    return asXids;
                } catch (SQLException e22222) {
                    stmt2 = stmt;
                    sqlEx = e22222;
                    throw mapXAExceptionFromSQLException(sqlEx);
                } catch (Throwable th22) {
                    stmt2 = stmt;
                    sqlEx = th22;
                    connection = c;
                    i = flag;
                    rs = rs2;
                    stmt3 = stmt2;
                    if (rs != null) {
                        rs.close();
                    }
                    if (stmt3 != null) {
                        stmt3.close();
                    }
                    throw sqlEx;
                }
            } catch (SQLException e222222) {
                sqlEx = e222222;
                throw mapXAExceptionFromSQLException(sqlEx);
            }
        }
    }

    public int prepare(Xid xid) throws XAException {
        StringBuilder commandBuf = new StringBuilder(MAX_COMMAND_LENGTH);
        commandBuf.append("XA PREPARE ");
        appendXid(commandBuf, xid);
        dispatchCommand(commandBuf.toString());
        return 0;
    }

    public void forget(Xid xid) throws XAException {
    }

    public void rollback(Xid xid) throws XAException {
        StringBuilder commandBuf = new StringBuilder(MAX_COMMAND_LENGTH);
        commandBuf.append("XA ROLLBACK ");
        appendXid(commandBuf, xid);
        try {
            dispatchCommand(commandBuf.toString());
        } finally {
            this.underlyingConnection.setInGlobalTx(false);
        }
    }

    public void end(Xid xid, int flags) throws XAException {
        StringBuilder commandBuf = new StringBuilder(MAX_COMMAND_LENGTH);
        commandBuf.append("XA END ");
        appendXid(commandBuf, xid);
        if (flags == 33554432) {
            commandBuf.append(" SUSPEND");
        } else if (flags != 67108864) {
            if (flags != 536870912) {
                throw new XAException(-5);
            }
        }
        dispatchCommand(commandBuf.toString());
    }

    public void start(Xid xid, int flags) throws XAException {
        StringBuilder commandBuf = new StringBuilder(MAX_COMMAND_LENGTH);
        commandBuf.append("XA START ");
        appendXid(commandBuf, xid);
        if (flags != 0) {
            if (flags == 2097152) {
                commandBuf.append(" JOIN");
            } else if (flags != 134217728) {
                throw new XAException(-5);
            } else {
                commandBuf.append(" RESUME");
            }
        }
        dispatchCommand(commandBuf.toString());
        this.underlyingConnection.setInGlobalTx(true);
    }

    public void commit(Xid xid, boolean onePhase) throws XAException {
        StringBuilder commandBuf = new StringBuilder(MAX_COMMAND_LENGTH);
        commandBuf.append("XA COMMIT ");
        appendXid(commandBuf, xid);
        if (onePhase) {
            commandBuf.append(" ONE PHASE");
        }
        try {
            dispatchCommand(commandBuf.toString());
        } finally {
            this.underlyingConnection.setInGlobalTx(false);
        }
    }

    private ResultSet dispatchCommand(String command) throws XAException {
        Statement stmt = null;
        try {
            if (this.logXaCommands) {
                Log log = this.log;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Executing XA statement: ");
                stringBuilder.append(command);
                log.logDebug(stringBuilder.toString());
            }
            stmt = this.underlyingConnection.createStatement();
            stmt.execute(command);
            ResultSet rs = stmt.getResultSet();
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
            }
            return rs;
        } catch (SQLException sqlEx) {
            throw mapXAExceptionFromSQLException(sqlEx);
        } catch (Throwable th) {
            MysqlXAConnection mysqlXAConnection = this;
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e2) {
                }
            }
        }
    }

    protected static XAException mapXAExceptionFromSQLException(SQLException sqlEx) {
        Integer xaCode = (Integer) MYSQL_ERROR_CODES_TO_XA_ERROR_CODES.get(Integer.valueOf(sqlEx.getErrorCode()));
        if (xaCode != null) {
            return (XAException) new MysqlXAException(xaCode.intValue(), sqlEx.getMessage(), null).initCause(sqlEx);
        }
        return (XAException) new MysqlXAException(-7, Messages.getString("MysqlXAConnection.003"), null).initCause(sqlEx);
    }

    private static void appendXid(StringBuilder builder, Xid xid) {
        byte[] gtrid = xid.getGlobalTransactionId();
        byte[] btrid = xid.getBranchQualifier();
        if (gtrid != null) {
            StringUtils.appendAsHex(builder, gtrid);
        }
        builder.append(',');
        if (btrid != null) {
            StringUtils.appendAsHex(builder, btrid);
        }
        builder.append(',');
        StringUtils.appendAsHex(builder, xid.getFormatId());
    }

    public synchronized java.sql.Connection getConnection() throws SQLException {
        return getConnection(null, true);
    }
}
