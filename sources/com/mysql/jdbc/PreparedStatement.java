package com.mysql.jdbc;

import com.mysql.jdbc.profiler.ProfilerEventHandler;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.TimerTask;

public class PreparedStatement extends StatementImpl implements java.sql.PreparedStatement {
    private static final byte[] HEX_DIGITS = new byte[]{(byte) 48, (byte) 49, (byte) 50, (byte) 51, (byte) 52, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57, (byte) 65, (byte) 66, (byte) 67, (byte) 68, (byte) 69, (byte) 70};
    private static final Constructor<?> JDBC_4_PSTMT_2_ARG_CTOR;
    private static final Constructor<?> JDBC_4_PSTMT_3_ARG_CTOR;
    private static final Constructor<?> JDBC_4_PSTMT_4_ARG_CTOR;
    protected int batchCommandIndex;
    protected boolean batchHasPlainStatements = false;
    protected String batchedValuesClause;
    private CharsetEncoder charsetEncoder;
    private boolean compensateForOnDuplicateKeyUpdate;
    private DatabaseMetaData dbmd = null;
    private SimpleDateFormat ddf;
    private boolean doPingInstead;
    protected char firstCharOfStmt = '\u0000';
    protected boolean isLoadDataQuery = false;
    protected boolean[] isNull = null;
    private boolean[] isStream = null;
    protected int numberOfExecutions = 0;
    protected String originalSql = null;
    protected int parameterCount;
    protected MysqlParameterMetadata parameterMetaData;
    private InputStream[] parameterStreams = null;
    protected int[] parameterTypes;
    private byte[][] parameterValues;
    protected ParseInfo parseInfo;
    private ResultSetMetaData pstmtResultMetaData;
    protected int rewrittenBatchSize;
    protected boolean serverSupportsFracSecs;
    private byte[][] staticSqlStrings;
    private byte[] streamConvertBuf;
    private int[] streamLengths;
    private SimpleDateFormat tdf;
    private SimpleDateFormat tsdf;
    protected boolean useTrueBoolean;
    protected boolean usingAnsiMode;

    public class BatchParams {
        public boolean[] isNull = null;
        public boolean[] isStream = null;
        public InputStream[] parameterStreams = null;
        public byte[][] parameterStrings = ((byte[][]) null);
        public int[] streamLengths = null;

        BatchParams(byte[][] strings, InputStream[] streams, boolean[] isStreamFlags, int[] lengths, boolean[] isNullFlags) {
            this.parameterStrings = new byte[strings.length][];
            this.parameterStreams = new InputStream[streams.length];
            this.isStream = new boolean[isStreamFlags.length];
            this.streamLengths = new int[lengths.length];
            this.isNull = new boolean[isNullFlags.length];
            System.arraycopy(strings, 0, this.parameterStrings, 0, strings.length);
            System.arraycopy(streams, 0, this.parameterStreams, 0, streams.length);
            System.arraycopy(isStreamFlags, 0, this.isStream, 0, isStreamFlags.length);
            System.arraycopy(lengths, 0, this.streamLengths, 0, lengths.length);
            System.arraycopy(isNullFlags, 0, this.isNull, 0, isNullFlags.length);
        }
    }

    interface BatchVisitor {
        BatchVisitor append(byte[] bArr);

        BatchVisitor decrement();

        BatchVisitor increment();

        BatchVisitor merge(byte[] bArr, byte[] bArr2);
    }

    class EndPoint {
        int begin;
        int end;

        EndPoint(int b, int e) {
            this.begin = b;
            this.end = e;
        }
    }

    public static final class ParseInfo {
        private ParseInfo batchHead;
        private ParseInfo batchODKUClause;
        private ParseInfo batchValues;
        boolean canRewriteAsMultiValueInsert;
        String charEncoding;
        char firstStmtChar;
        boolean foundLoadData;
        boolean isOnDuplicateKeyUpdate;
        long lastUsed;
        int locationOfOnDuplicateKeyUpdate;
        boolean parametersInDuplicateKeyClause;
        int statementLength;
        int statementStartPos;
        byte[][] staticSql;
        String valuesClause;

        ParseInfo(String sql, MySQLConnection conn, DatabaseMetaData dbmd, String encoding, SingleByteCharsetConverter converter) throws SQLException {
            this(sql, conn, dbmd, encoding, converter, true);
        }

        public ParseInfo(String sql, MySQLConnection conn, DatabaseMetaData dbmd, String encoding, SingleByteCharsetConverter converter, boolean buildRewriteInfo) throws SQLException {
            String str = sql;
            String str2 = encoding;
            this.firstStmtChar = '\u0000';
            this.foundLoadData = false;
            this.lastUsed = 0;
            this.statementLength = 0;
            this.statementStartPos = 0;
            this.canRewriteAsMultiValueInsert = false;
            this.staticSql = (byte[][]) null;
            this.isOnDuplicateKeyUpdate = false;
            int i = -1;
            this.locationOfOnDuplicateKeyUpdate = -1;
            this.parametersInDuplicateKeyClause = false;
            if (str == null) {
                try {
                    throw SQLError.createSQLException(Messages.getString("PreparedStatement.61"), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, conn.getExceptionInterceptor());
                } catch (StringIndexOutOfBoundsException e) {
                    StringIndexOutOfBoundsException oobEx = e;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Parse error for ");
                    stringBuilder.append(str);
                    SQLException sqlEx = new SQLException(stringBuilder.toString());
                    sqlEx.initCause(oobEx);
                    throw sqlEx;
                }
            }
            boolean z;
            boolean z2;
            r1.charEncoding = str2;
            r1.lastUsed = System.currentTimeMillis();
            String quotedIdentifierString = dbmd.getIdentifierQuoteString();
            char quotedIdentifierChar = '\u0000';
            if (!(quotedIdentifierString == null || quotedIdentifierString.equals(" ") || quotedIdentifierString.length() <= 0)) {
                quotedIdentifierChar = quotedIdentifierString.charAt(0);
            }
            char quotedIdentifierChar2 = quotedIdentifierChar;
            r1.statementLength = sql.length();
            ArrayList<int[]> endpointList = new ArrayList();
            boolean noBackslashEscapes = conn.isNoBackslashEscapesSet();
            r1.statementStartPos = StatementImpl.findStartOfStatement(sql);
            int i2 = r1.statementStartPos;
            boolean inQuotes = false;
            char quoteChar = '\u0000';
            boolean inQuotedId = false;
            int lastParmEnd = 0;
            while (true) {
                int i3 = i2;
                z = true;
                if (i3 >= r1.statementLength) {
                    break;
                }
                char c = str.charAt(i3);
                if (r1.firstStmtChar == '\u0000' && Character.isLetter(c)) {
                    r1.firstStmtChar = Character.toUpperCase(c);
                    if (r1.firstStmtChar == 'I') {
                        r1.locationOfOnDuplicateKeyUpdate = StatementImpl.getOnDuplicateKeyLocation(str, conn.getDontCheckOnDuplicateKeyUpdateInSQL(), conn.getRewriteBatchedStatements(), conn.isNoBackslashEscapesSet());
                        r1.isOnDuplicateKeyUpdate = r1.locationOfOnDuplicateKeyUpdate != i;
                    }
                }
                if (noBackslashEscapes || c != '\\' || i3 >= r1.statementLength - 1) {
                    if (!inQuotes && quotedIdentifierChar2 != '\u0000' && c == quotedIdentifierChar2) {
                        inQuotedId = !inQuotedId;
                    } else if (!inQuotedId) {
                        if (!inQuotes) {
                            if (c != '#') {
                                if (c != '-' || i3 + 1 >= r1.statementLength || str.charAt(i3 + 1) != '-') {
                                    if (c == '/' && i3 + 1 < r1.statementLength) {
                                        char cNext = str.charAt(i3 + 1);
                                        char c2 = '*';
                                        if (cNext == '*') {
                                            i3 += 2;
                                            int i4 = i3;
                                            while (i3 < r1.statementLength) {
                                                i4++;
                                                if (str.charAt(i3) == c2 && i3 + 1 < r1.statementLength && str.charAt(i3 + 1) == '/') {
                                                    i4++;
                                                    if (i4 < r1.statementLength) {
                                                        c = str.charAt(i4);
                                                    }
                                                    i3 = i4;
                                                } else {
                                                    i3++;
                                                    c2 = '*';
                                                }
                                            }
                                            i3 = i4;
                                        }
                                    } else if (c == '\'' || c == '\"') {
                                        inQuotes = true;
                                        quoteChar = c;
                                    }
                                }
                            }
                            i = r1.statementLength - 1;
                            while (i3 < i) {
                                c = str.charAt(i3);
                                if (c == '\r') {
                                    break;
                                } else if (c == '\n') {
                                    break;
                                } else {
                                    i3++;
                                }
                            }
                        } else if ((c == '\'' || c == '\"') && c == quoteChar) {
                            if (i3 >= r1.statementLength - 1 || str.charAt(i3 + 1) != quoteChar) {
                                inQuotes = !inQuotes;
                                quoteChar = '\u0000';
                            } else {
                                i3++;
                            }
                        } else if ((c == '\'' || c == '\"') && c == quoteChar) {
                            inQuotes = !inQuotes;
                            quoteChar = '\u0000';
                        }
                    }
                    if (c == '?' && !inQuotes && !inQuotedId) {
                        endpointList.add(new int[]{lastParmEnd, i3});
                        i = i3 + 1;
                        if (r1.isOnDuplicateKeyUpdate && i3 > r1.locationOfOnDuplicateKeyUpdate) {
                            r1.parametersInDuplicateKeyClause = true;
                        }
                        lastParmEnd = i;
                    }
                } else {
                    i3++;
                }
                i2 = i3 + 1;
                i = -1;
            }
            if (r1.firstStmtChar != 'L') {
                i = 0;
                r1.foundLoadData = false;
            } else if (StringUtils.startsWithIgnoreCaseAndWs(str, "LOAD DATA")) {
                r1.foundLoadData = true;
                i = 0;
            } else {
                i = 0;
                r1.foundLoadData = false;
            }
            endpointList.add(new int[]{lastParmEnd, r1.statementLength});
            r1.staticSql = new byte[endpointList.size()][];
            int i5 = 0;
            while (i5 < r1.staticSql.length) {
                char quoteChar2;
                int[] ep = (int[]) endpointList.get(i5);
                int end = ep[z];
                i2 = ep[0];
                int len = end - i2;
                if (r1.foundLoadData) {
                    r1.staticSql[i5] = StringUtils.getBytes(str, i2, len);
                } else if (str2 == null) {
                    byte[] buf = new byte[len];
                    for (i3 = 0; i3 < len; i3++) {
                        buf[i3] = (byte) str.charAt(i2 + i3);
                    }
                    r1.staticSql[i5] = buf;
                } else {
                    if (converter != null) {
                        z2 = z;
                        quoteChar2 = quoteChar;
                        r1.staticSql[i5] = StringUtils.getBytes(str, converter, str2, conn.getServerCharset(), i2, len, conn.parserKnowsUnicode(), conn.getExceptionInterceptor());
                    } else {
                        z2 = z;
                        quoteChar2 = quoteChar;
                        r1.staticSql[i5] = StringUtils.getBytes(str, str2, conn.getServerCharset(), i2, len, conn.parserKnowsUnicode(), conn, conn.getExceptionInterceptor());
                    }
                    i5++;
                    quoteChar = quoteChar2;
                    z = z2;
                }
                z2 = z;
                quoteChar2 = quoteChar;
                i5++;
                quoteChar = quoteChar2;
                z = z2;
            }
            z2 = z;
            if (buildRewriteInfo) {
                boolean z3 = (!PreparedStatement.canRewrite(str, r1.isOnDuplicateKeyUpdate, r1.locationOfOnDuplicateKeyUpdate, r1.statementStartPos) || r1.parametersInDuplicateKeyClause) ? false : z2;
                r1.canRewriteAsMultiValueInsert = z3;
                if (r1.canRewriteAsMultiValueInsert && conn.getRewriteBatchedStatements() != null) {
                    buildRewriteBatchedParams(sql, conn, dbmd, encoding, converter);
                }
            }
        }

        private void buildRewriteBatchedParams(String sql, MySQLConnection conn, DatabaseMetaData metadata, String encoding, SingleByteCharsetConverter converter) throws SQLException {
            String headSql;
            String str = sql;
            this.valuesClause = extractValuesClause(str, conn.getMetaData().getIdentifierQuoteString());
            String odkuClause = this.isOnDuplicateKeyUpdate ? str.substring(r0.locationOfOnDuplicateKeyUpdate) : null;
            if (r0.isOnDuplicateKeyUpdate) {
                headSql = str.substring(0, r0.locationOfOnDuplicateKeyUpdate);
            } else {
                headSql = str;
            }
            r0.batchHead = new ParseInfo(headSql, conn, metadata, encoding, converter, false);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(",");
            stringBuilder.append(r0.valuesClause);
            r0.batchValues = new ParseInfo(stringBuilder.toString(), conn, metadata, encoding, converter, false);
            r0.batchODKUClause = null;
            if (odkuClause != null && odkuClause.length() > 0) {
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append(",");
                stringBuilder2.append(r0.valuesClause);
                stringBuilder2.append(" ");
                stringBuilder2.append(odkuClause);
                r0.batchODKUClause = new ParseInfo(stringBuilder2.toString(), conn, metadata, encoding, converter, false);
            }
        }

        private String extractValuesClause(String sql, String quoteCharStr) throws SQLException {
            int indexOfValues = -1;
            int valuesSearchStart = this.statementStartPos;
            while (indexOfValues == -1) {
                if (quoteCharStr.length() > 0) {
                    indexOfValues = StringUtils.indexOfIgnoreCase(valuesSearchStart, sql, "VALUES", quoteCharStr, quoteCharStr, StringUtils.SEARCH_MODE__MRK_COM_WS);
                } else {
                    indexOfValues = StringUtils.indexOfIgnoreCase(valuesSearchStart, sql, "VALUES");
                }
                if (indexOfValues <= 0) {
                    break;
                }
                char c = sql.charAt(indexOfValues - 1);
                if (Character.isWhitespace(c) || c == ')' || c == '`') {
                    c = sql.charAt(indexOfValues + 6);
                    if (!(Character.isWhitespace(c) || c == '(')) {
                        valuesSearchStart = indexOfValues + 6;
                        indexOfValues = -1;
                    }
                } else {
                    valuesSearchStart = indexOfValues + 6;
                    indexOfValues = -1;
                }
            }
            if (indexOfValues == -1) {
                return null;
            }
            int indexOfFirstParen = sql.indexOf(40, indexOfValues + 6);
            if (indexOfFirstParen == -1) {
                return null;
            }
            int endOfValuesClause = sql.lastIndexOf(41);
            if (endOfValuesClause == -1) {
                return null;
            }
            if (this.isOnDuplicateKeyUpdate) {
                endOfValuesClause = this.locationOfOnDuplicateKeyUpdate - 1;
            }
            return sql.substring(indexOfFirstParen, endOfValuesClause + 1);
        }

        synchronized ParseInfo getParseInfoForBatch(int numBatch) {
            AppendingBatchVisitor apv;
            apv = new AppendingBatchVisitor();
            buildInfoForBatch(numBatch, apv);
            return new ParseInfo(apv.getStaticSqlStrings(), this.firstStmtChar, this.foundLoadData, this.isOnDuplicateKeyUpdate, this.locationOfOnDuplicateKeyUpdate, this.statementLength, this.statementStartPos);
        }

        String getSqlForBatch(int numBatch) throws UnsupportedEncodingException {
            return getSqlForBatch(getParseInfoForBatch(numBatch));
        }

        String getSqlForBatch(ParseInfo batchInfo) throws UnsupportedEncodingException {
            byte[][] sqlStrings = batchInfo.staticSql;
            int i = 0;
            int size = 0;
            for (byte[] length : sqlStrings) {
                size = (size + length.length) + 1;
            }
            StringBuilder buf = new StringBuilder(size);
            while (i < sqlStringsLength - 1) {
                buf.append(StringUtils.toString(sqlStrings[i], this.charEncoding));
                buf.append("?");
                i++;
            }
            buf.append(StringUtils.toString(sqlStrings[sqlStringsLength - 1]));
            return buf.toString();
        }

        private void buildInfoForBatch(int numBatch, BatchVisitor visitor) {
            int j;
            byte[][] headStaticSql = this.batchHead.staticSql;
            int i = 1;
            int headStaticSqlLength = headStaticSql.length;
            if (headStaticSqlLength > 1) {
                for (int i2 = 0; i2 < headStaticSqlLength - 1; i2++) {
                    visitor.append(headStaticSql[i2]).increment();
                }
            }
            byte[] endOfHead = headStaticSql[headStaticSqlLength - 1];
            byte[][] valuesStaticSql = this.batchValues.staticSql;
            byte[] beginOfValues = valuesStaticSql[0];
            visitor.merge(endOfHead, beginOfValues).increment();
            int numValueRepeats = numBatch - 1;
            if (this.batchODKUClause != null) {
                numValueRepeats--;
            }
            int valuesStaticSqlLength = valuesStaticSql.length;
            byte[] endOfValues = valuesStaticSql[valuesStaticSqlLength - 1];
            for (int i3 = 0; i3 < numValueRepeats; i3++) {
                for (j = 1; j < valuesStaticSqlLength - 1; j++) {
                    visitor.append(valuesStaticSql[j]).increment();
                }
                visitor.merge(endOfValues, beginOfValues).increment();
            }
            if (this.batchODKUClause != null) {
                byte[][] batchOdkuStaticSql = this.batchODKUClause.staticSql;
                visitor.decrement().merge(endOfValues, batchOdkuStaticSql[0]).increment();
                j = batchOdkuStaticSql.length;
                if (numBatch > 1) {
                    while (i < j) {
                        visitor.append(batchOdkuStaticSql[i]).increment();
                        i++;
                    }
                } else {
                    visitor.decrement().append(batchOdkuStaticSql[j - 1]);
                }
                return;
            }
            visitor.decrement().append(this.staticSql[this.staticSql.length - 1]);
        }

        private ParseInfo(byte[][] staticSql, char firstStmtChar, boolean foundLoadData, boolean isOnDuplicateKeyUpdate, int locationOfOnDuplicateKeyUpdate, int statementLength, int statementStartPos) {
            this.firstStmtChar = '\u0000';
            this.foundLoadData = false;
            this.lastUsed = 0;
            this.statementLength = 0;
            this.statementStartPos = 0;
            this.canRewriteAsMultiValueInsert = false;
            this.staticSql = (byte[][]) null;
            this.isOnDuplicateKeyUpdate = false;
            this.locationOfOnDuplicateKeyUpdate = -1;
            this.parametersInDuplicateKeyClause = false;
            this.firstStmtChar = firstStmtChar;
            this.foundLoadData = foundLoadData;
            this.isOnDuplicateKeyUpdate = isOnDuplicateKeyUpdate;
            this.locationOfOnDuplicateKeyUpdate = locationOfOnDuplicateKeyUpdate;
            this.statementLength = statementLength;
            this.statementStartPos = statementStartPos;
            this.staticSql = staticSql;
        }
    }

    static class AppendingBatchVisitor implements BatchVisitor {
        LinkedList<byte[]> statementComponents = new LinkedList();

        AppendingBatchVisitor() {
        }

        public BatchVisitor append(byte[] values) {
            this.statementComponents.addLast(values);
            return this;
        }

        public BatchVisitor increment() {
            return this;
        }

        public BatchVisitor decrement() {
            this.statementComponents.removeLast();
            return this;
        }

        public BatchVisitor merge(byte[] front, byte[] back) {
            byte[] merged = new byte[(front.length + back.length)];
            System.arraycopy(front, 0, merged, 0, front.length);
            System.arraycopy(back, 0, merged, front.length, back.length);
            this.statementComponents.addLast(merged);
            return this;
        }

        public byte[][] getStaticSqlStrings() {
            byte[][] asBytes = new byte[this.statementComponents.size()][];
            this.statementComponents.toArray(asBytes);
            return asBytes;
        }

        public String toString() {
            StringBuilder buf = new StringBuilder();
            Iterator<byte[]> iter = this.statementComponents.iterator();
            while (iter.hasNext()) {
                buf.append(StringUtils.toString((byte[]) iter.next()));
            }
            return buf.toString();
        }
    }

    class EmulatedPreparedStatementBindings implements ParameterBindings {
        private ResultSetImpl bindingsAsRs;
        private boolean[] parameterIsNull;

        EmulatedPreparedStatementBindings() throws SQLException {
            List<ResultSetRow> rows = new ArrayList();
            this.parameterIsNull = new boolean[PreparedStatement.this.parameterCount];
            int i = 0;
            System.arraycopy(PreparedStatement.this.isNull, 0, this.parameterIsNull, 0, PreparedStatement.this.parameterCount);
            byte[][] rowData = new byte[PreparedStatement.this.parameterCount][];
            Field[] typeMetadata = new Field[PreparedStatement.this.parameterCount];
            while (true) {
                int i2 = i;
                if (i2 < PreparedStatement.this.parameterCount) {
                    StringBuilder stringBuilder;
                    Field parameterMetadata;
                    if (PreparedStatement.this.batchCommandIndex == -1) {
                        rowData[i2] = PreparedStatement.this.getBytesRepresentation(i2);
                    } else {
                        rowData[i2] = PreparedStatement.this.getBytesRepresentationForBatch(i2, PreparedStatement.this.batchCommandIndex);
                    }
                    if (PreparedStatement.this.parameterTypes[i2] != -2) {
                        if (PreparedStatement.this.parameterTypes[i2] != 2004) {
                            try {
                                i = CharsetMapping.getCollationIndexForJavaEncoding(PreparedStatement.this.connection.getEncoding(), PreparedStatement.this.connection);
                                stringBuilder = new StringBuilder();
                                stringBuilder.append("parameter_");
                                stringBuilder.append(i2 + 1);
                                parameterMetadata = new Field(null, stringBuilder.toString(), i, PreparedStatement.this.parameterTypes[i2], rowData[i2].length);
                                parameterMetadata.setConnection(PreparedStatement.this.connection);
                                typeMetadata[i2] = parameterMetadata;
                                i = i2 + 1;
                            } catch (SQLException ex) {
                                throw ex;
                            } catch (RuntimeException ex2) {
                                SQLException sqlEx = SQLError.createSQLException(ex2.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, null);
                                sqlEx.initCause(ex2);
                                throw sqlEx;
                            }
                        }
                    }
                    i = 63;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("parameter_");
                    stringBuilder.append(i2 + 1);
                    parameterMetadata = new Field(null, stringBuilder.toString(), i, PreparedStatement.this.parameterTypes[i2], rowData[i2].length);
                    parameterMetadata.setConnection(PreparedStatement.this.connection);
                    typeMetadata[i2] = parameterMetadata;
                    i = i2 + 1;
                } else {
                    rows.add(new ByteArrayRow(rowData, PreparedStatement.this.getExceptionInterceptor()));
                    this.bindingsAsRs = new ResultSetImpl(PreparedStatement.this.connection.getCatalog(), typeMetadata, new RowDataStatic(rows), PreparedStatement.this.connection, null);
                    this.bindingsAsRs.next();
                    return;
                }
            }
        }

        public Array getArray(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getArray(parameterIndex);
        }

        public InputStream getAsciiStream(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getAsciiStream(parameterIndex);
        }

        public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getBigDecimal(parameterIndex);
        }

        public InputStream getBinaryStream(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getBinaryStream(parameterIndex);
        }

        public Blob getBlob(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getBlob(parameterIndex);
        }

        public boolean getBoolean(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getBoolean(parameterIndex);
        }

        public byte getByte(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getByte(parameterIndex);
        }

        public byte[] getBytes(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getBytes(parameterIndex);
        }

        public Reader getCharacterStream(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getCharacterStream(parameterIndex);
        }

        public Clob getClob(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getClob(parameterIndex);
        }

        public Date getDate(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getDate(parameterIndex);
        }

        public double getDouble(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getDouble(parameterIndex);
        }

        public float getFloat(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getFloat(parameterIndex);
        }

        public int getInt(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getInt(parameterIndex);
        }

        public long getLong(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getLong(parameterIndex);
        }

        public Reader getNCharacterStream(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getCharacterStream(parameterIndex);
        }

        public Reader getNClob(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getCharacterStream(parameterIndex);
        }

        public Object getObject(int parameterIndex) throws SQLException {
            PreparedStatement.this.checkBounds(parameterIndex, 0);
            if (this.parameterIsNull[parameterIndex - 1]) {
                return null;
            }
            int i = PreparedStatement.this.parameterTypes[parameterIndex - 1];
            if (i == 8) {
                return Double.valueOf(getDouble(parameterIndex));
            }
            switch (i) {
                case -6:
                    return Byte.valueOf(getByte(parameterIndex));
                case -5:
                    return Long.valueOf(getLong(parameterIndex));
                default:
                    switch (i) {
                        case 4:
                            return Integer.valueOf(getInt(parameterIndex));
                        case 5:
                            return Short.valueOf(getShort(parameterIndex));
                        case 6:
                            return Float.valueOf(getFloat(parameterIndex));
                        default:
                            return this.bindingsAsRs.getObject(parameterIndex);
                    }
            }
        }

        public Ref getRef(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getRef(parameterIndex);
        }

        public short getShort(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getShort(parameterIndex);
        }

        public String getString(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getString(parameterIndex);
        }

        public Time getTime(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getTime(parameterIndex);
        }

        public Timestamp getTimestamp(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getTimestamp(parameterIndex);
        }

        public URL getURL(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getURL(parameterIndex);
        }

        public boolean isNull(int parameterIndex) throws SQLException {
            PreparedStatement.this.checkBounds(parameterIndex, 0);
            return this.parameterIsNull[parameterIndex - 1];
        }
    }

    protected void setBytes(int r19, byte[] r20, boolean r21, boolean r22) throws java.sql.SQLException {
        /* JADX: method processing error */
/*
Error: java.lang.NullPointerException
	at jadx.core.dex.visitors.ssa.SSATransform.placePhi(SSATransform.java:82)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:50)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:56)
	at jadx.core.ProcessClass.process(ProcessClass.java:39)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:282)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
	at jadx.api.JadxDecompiler$$Lambda$8/1205406622.run(Unknown Source)
*/
        /*
        r18 = this;
        r1 = r18;
        r2 = r19;
        r3 = r20;
        r4 = r18.checkClosed();
        r4 = r4.getConnectionMutex();
        monitor-enter(r4);
        if (r3 != 0) goto L_0x001b;
    L_0x0011:
        r5 = -2;
        r1.setNull(r2, r5);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        goto L_0x010a;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x0017:
        r0 = move-exception;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r5 = r0;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        goto L_0x0164;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x001b:
        r5 = r1.connection;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r5 = r5.getEncoding();	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r6 = r1.connection;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r6 = r6.isNoBackslashEscapesSet();	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r7 = 0;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r8 = 39;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        if (r6 != 0) goto L_0x010c;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x002c:
        if (r22 == 0) goto L_0x0040;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x002e:
        r9 = r1.connection;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r9 = r9.getUseUnicode();	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        if (r9 == 0) goto L_0x0040;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x0036:
        if (r5 == 0) goto L_0x0040;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x0038:
        r9 = com.mysql.jdbc.CharsetMapping.isMultibyteCharset(r5);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        if (r9 == 0) goto L_0x0040;
    L_0x003e:
        goto L_0x010c;
        r9 = r3.length;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r10 = 2;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r11 = 1;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        if (r21 == 0) goto L_0x0050;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x0046:
        r12 = r1.connection;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r13 = 4;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r12 = r12.versionMeetsMinimum(r13, r11, r7);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        if (r12 == 0) goto L_0x0050;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x004f:
        goto L_0x0051;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x0050:
        r11 = r7;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x0051:
        if (r11 == 0) goto L_0x0055;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x0053:
        r10 = r10 + 7;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x0055:
        r12 = new java.io.ByteArrayOutputStream;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r13 = r9 + r10;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r12.<init>(r13);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r13 = 114; // 0x72 float:1.6E-43 double:5.63E-322;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r14 = 110; // 0x6e float:1.54E-43 double:5.43E-322;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        if (r11 == 0) goto L_0x0081;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x0062:
        r7 = 95;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r12.write(r7);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r7 = 98;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r12.write(r7);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r7 = 105; // 0x69 float:1.47E-43 double:5.2E-322;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r12.write(r7);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r12.write(r14);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r7 = 97;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r12.write(r7);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r12.write(r13);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r7 = 121; // 0x79 float:1.7E-43 double:6.0E-322;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r12.write(r7);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x0081:
        r12.write(r8);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r15 = 0;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x0085:
        r7 = r15;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        if (r7 >= r9) goto L_0x00fe;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x0088:
        r15 = r3[r7];	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r16 = r15;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r13 = r16;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        if (r13 == 0) goto L_0x00e8;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x0090:
        r14 = 10;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        if (r13 == r14) goto L_0x00db;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x0094:
        r14 = 13;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        if (r13 == r14) goto L_0x00d0;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x0098:
        r14 = 26;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        if (r13 == r14) goto L_0x00c5;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x009c:
        r14 = 34;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        if (r13 == r14) goto L_0x00bc;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x00a0:
        if (r13 == r8) goto L_0x00b3;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x00a2:
        r14 = 92;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        if (r13 == r14) goto L_0x00ac;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x00a6:
        r12.write(r13);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x00a9:
        r14 = 110; // 0x6e float:1.54E-43 double:5.43E-322;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        goto L_0x00f7;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x00ac:
        r12.write(r14);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r12.write(r14);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        goto L_0x00a9;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x00b3:
        r14 = 92;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r12.write(r14);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r12.write(r8);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        goto L_0x00a9;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x00bc:
        r8 = 92;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r12.write(r8);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r12.write(r14);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        goto L_0x00a9;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x00c5:
        r8 = 92;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r12.write(r8);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r8 = 90;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r12.write(r8);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        goto L_0x00a9;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x00d0:
        r8 = 92;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r12.write(r8);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r8 = 114; // 0x72 float:1.6E-43 double:5.63E-322;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r12.write(r8);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        goto L_0x00a9;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x00db:
        r8 = 114; // 0x72 float:1.6E-43 double:5.63E-322;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r14 = 92;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r12.write(r14);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r14 = 110; // 0x6e float:1.54E-43 double:5.43E-322;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r12.write(r14);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        goto L_0x00f7;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x00e8:
        r8 = 114; // 0x72 float:1.6E-43 double:5.63E-322;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r14 = 110; // 0x6e float:1.54E-43 double:5.43E-322;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r8 = 92;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r12.write(r8);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r8 = 48;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r12.write(r8);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x00f7:
        r15 = r7 + 1;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r8 = 39;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r13 = 114; // 0x72 float:1.6E-43 double:5.63E-322;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        goto L_0x0085;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x00fe:
        r7 = 39;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r12.write(r7);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r7 = r12.toByteArray();	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r1.setInternal(r2, r7);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x010a:
        monitor-exit(r4);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        return;
    L_0x010c:
        r7 = new java.io.ByteArrayOutputStream;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r8 = r3.length;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r8 = r8 * 2;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r8 = r8 + 3;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r7.<init>(r8);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r8 = 120; // 0x78 float:1.68E-43 double:5.93E-322;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r7.write(r8);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r8 = 39;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r7.write(r8);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r15 = 0;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x0121:
        r8 = r15;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r9 = r3.length;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        if (r8 >= r9) goto L_0x0142;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x0125:
        r9 = r3[r8];	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r9 = r9 & 255;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r9 = r9 / 16;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r10 = r3[r8];	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r10 = r10 & 255;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r10 = r10 % 16;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r11 = HEX_DIGITS;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r11 = r11[r9];	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r7.write(r11);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r11 = HEX_DIGITS;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r11 = r11[r10];	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r7.write(r11);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r15 = r8 + 1;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        goto L_0x0121;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x0142:
        r8 = 39;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r7.write(r8);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r8 = r7.toByteArray();	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r1.setInternal(r2, r8);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        monitor-exit(r4);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        return;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x0150:
        r0 = move-exception;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r7 = r0;
        r8 = r7.toString();	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r9 = "S1009";	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r10 = 0;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r8 = com.mysql.jdbc.SQLError.createSQLException(r8, r9, r10);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r8.initCause(r7);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        throw r8;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x0161:
        r0 = move-exception;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        r7 = r0;
        throw r7;	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
    L_0x0164:
        monitor-exit(r4);	 Catch:{ SQLException -> 0x0161, RuntimeException -> 0x0150, all -> 0x0017 }
        throw r5;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.PreparedStatement.setBytes(int, byte[], boolean, boolean):void");
    }

    static {
        if (Util.isJdbc4()) {
            try {
                String jdbc4ClassName = Util.isJdbc42() ? "com.mysql.jdbc.JDBC42PreparedStatement" : "com.mysql.jdbc.JDBC4PreparedStatement";
                JDBC_4_PSTMT_2_ARG_CTOR = Class.forName(jdbc4ClassName).getConstructor(new Class[]{MySQLConnection.class, String.class});
                JDBC_4_PSTMT_3_ARG_CTOR = Class.forName(jdbc4ClassName).getConstructor(new Class[]{MySQLConnection.class, String.class, String.class});
                JDBC_4_PSTMT_4_ARG_CTOR = Class.forName(jdbc4ClassName).getConstructor(new Class[]{MySQLConnection.class, String.class, String.class, ParseInfo.class});
            } catch (SecurityException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e2) {
                throw new RuntimeException(e2);
            } catch (ClassNotFoundException e3) {
                throw new RuntimeException(e3);
            }
        }
        JDBC_4_PSTMT_2_ARG_CTOR = null;
        JDBC_4_PSTMT_3_ARG_CTOR = null;
        JDBC_4_PSTMT_4_ARG_CTOR = null;
    }

    protected static int readFully(Reader reader, char[] buf, int length) throws IOException {
        int numCharsRead = 0;
        while (numCharsRead < length) {
            int count = reader.read(buf, numCharsRead, length - numCharsRead);
            if (count < 0) {
                break;
            }
            numCharsRead += count;
        }
        return numCharsRead;
    }

    protected static PreparedStatement getInstance(MySQLConnection conn, String catalog) throws SQLException {
        if (!Util.isJdbc4()) {
            return new PreparedStatement(conn, catalog);
        }
        return (PreparedStatement) Util.handleNewInstance(JDBC_4_PSTMT_2_ARG_CTOR, new Object[]{conn, catalog}, conn.getExceptionInterceptor());
    }

    protected static PreparedStatement getInstance(MySQLConnection conn, String sql, String catalog) throws SQLException {
        if (!Util.isJdbc4()) {
            return new PreparedStatement(conn, sql, catalog);
        }
        return (PreparedStatement) Util.handleNewInstance(JDBC_4_PSTMT_3_ARG_CTOR, new Object[]{conn, sql, catalog}, conn.getExceptionInterceptor());
    }

    protected static PreparedStatement getInstance(MySQLConnection conn, String sql, String catalog, ParseInfo cachedParseInfo) throws SQLException {
        if (!Util.isJdbc4()) {
            return new PreparedStatement(conn, sql, catalog, cachedParseInfo);
        }
        return (PreparedStatement) Util.handleNewInstance(JDBC_4_PSTMT_4_ARG_CTOR, new Object[]{conn, sql, catalog, cachedParseInfo}, conn.getExceptionInterceptor());
    }

    public PreparedStatement(MySQLConnection conn, String catalog) throws SQLException {
        super(conn, catalog);
        byte[][] bArr = (byte[][]) null;
        this.parameterValues = bArr;
        this.parameterTypes = null;
        this.staticSqlStrings = bArr;
        this.streamConvertBuf = null;
        this.streamLengths = null;
        this.tsdf = null;
        this.useTrueBoolean = false;
        this.compensateForOnDuplicateKeyUpdate = false;
        this.batchCommandIndex = -1;
        this.rewrittenBatchSize = 0;
        detectFractionalSecondsSupport();
        this.compensateForOnDuplicateKeyUpdate = this.connection.getCompensateOnDuplicateKeyUpdateCounts();
    }

    protected void detectFractionalSecondsSupport() throws SQLException {
        boolean z = this.connection != null && this.connection.versionMeetsMinimum(5, 6, 4);
        this.serverSupportsFracSecs = z;
    }

    public PreparedStatement(MySQLConnection conn, String sql, String catalog) throws SQLException {
        super(conn, catalog);
        byte[][] bArr = (byte[][]) null;
        this.parameterValues = bArr;
        this.parameterTypes = null;
        this.staticSqlStrings = bArr;
        this.streamConvertBuf = null;
        this.streamLengths = null;
        this.tsdf = null;
        this.useTrueBoolean = false;
        this.compensateForOnDuplicateKeyUpdate = false;
        this.batchCommandIndex = -1;
        this.rewrittenBatchSize = 0;
        if (sql == null) {
            throw SQLError.createSQLException(Messages.getString("PreparedStatement.0"), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
        detectFractionalSecondsSupport();
        this.originalSql = sql;
        this.doPingInstead = this.originalSql.startsWith("/* ping */");
        this.dbmd = this.connection.getMetaData();
        this.useTrueBoolean = this.connection.versionMeetsMinimum(3, 21, 23);
        this.parseInfo = new ParseInfo(sql, this.connection, this.dbmd, this.charEncoding, this.charConverter);
        initializeFromParseInfo();
        this.compensateForOnDuplicateKeyUpdate = this.connection.getCompensateOnDuplicateKeyUpdateCounts();
        if (conn.getRequiresEscapingEncoder()) {
            this.charsetEncoder = Charset.forName(conn.getEncoding()).newEncoder();
        }
    }

    public PreparedStatement(MySQLConnection conn, String sql, String catalog, ParseInfo cachedParseInfo) throws SQLException {
        super(conn, catalog);
        byte[][] bArr = (byte[][]) null;
        this.parameterValues = bArr;
        this.parameterTypes = null;
        this.staticSqlStrings = bArr;
        this.streamConvertBuf = null;
        this.streamLengths = null;
        this.tsdf = null;
        this.useTrueBoolean = false;
        this.compensateForOnDuplicateKeyUpdate = false;
        this.batchCommandIndex = -1;
        this.rewrittenBatchSize = 0;
        if (sql == null) {
            throw SQLError.createSQLException(Messages.getString("PreparedStatement.1"), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
        detectFractionalSecondsSupport();
        this.originalSql = sql;
        this.dbmd = this.connection.getMetaData();
        this.useTrueBoolean = this.connection.versionMeetsMinimum(3, 21, 23);
        this.parseInfo = cachedParseInfo;
        this.usingAnsiMode = this.connection.useAnsiQuotedIdentifiers() ^ 1;
        initializeFromParseInfo();
        this.compensateForOnDuplicateKeyUpdate = this.connection.getCompensateOnDuplicateKeyUpdateCounts();
        if (conn.getRequiresEscapingEncoder()) {
            this.charsetEncoder = Charset.forName(conn.getEncoding()).newEncoder();
        }
    }

    public void addBatch() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.batchedArgs == null) {
                this.batchedArgs = new ArrayList();
            }
            for (int i = 0; i < this.parameterValues.length; i++) {
                checkAllParametersSet(this.parameterValues[i], this.parameterStreams[i], i);
            }
            this.batchedArgs.add(new BatchParams(this.parameterValues, this.parameterStreams, this.isStream, this.streamLengths, this.isNull));
        }
    }

    public void addBatch(String sql) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            this.batchHasPlainStatements = true;
            super.addBatch(sql);
        }
    }

    public String asSql() throws SQLException {
        return asSql(false);
    }

    public String asSql(boolean quoteStreamsAndUnknowns) throws SQLException {
        String stringBuilder;
        synchronized (checkClosed().getConnectionMutex()) {
            StringBuilder buf = new StringBuilder();
            try {
                int realParameterCount = this.parameterCount + getParameterIndexOffset();
                Object batchArg = null;
                if (this.batchCommandIndex != -1) {
                    batchArg = this.batchedArgs.get(this.batchCommandIndex);
                }
                for (int i = 0; i < realParameterCount; i++) {
                    if (this.charEncoding != null) {
                        buf.append(StringUtils.toString(this.staticSqlStrings[i], this.charEncoding));
                    } else {
                        buf.append(StringUtils.toString(this.staticSqlStrings[i]));
                    }
                    if (batchArg == null || !(batchArg instanceof String)) {
                        byte[] val;
                        boolean isStreamParam;
                        if (this.batchCommandIndex == -1) {
                            val = this.parameterValues[i];
                        } else {
                            val = ((BatchParams) batchArg).parameterStrings[i];
                        }
                        if (this.batchCommandIndex == -1) {
                            isStreamParam = this.isStream[i];
                        } else {
                            isStreamParam = ((BatchParams) batchArg).isStream[i];
                        }
                        if (val == null && !isStreamParam) {
                            if (quoteStreamsAndUnknowns) {
                                buf.append("'");
                            }
                            buf.append("** NOT SPECIFIED **");
                            if (quoteStreamsAndUnknowns) {
                                buf.append("'");
                            }
                        } else if (isStreamParam) {
                            if (quoteStreamsAndUnknowns) {
                                buf.append("'");
                            }
                            buf.append("** STREAM DATA **");
                            if (quoteStreamsAndUnknowns) {
                                buf.append("'");
                            }
                        } else if (this.charConverter != null) {
                            buf.append(this.charConverter.toString(val));
                        } else if (this.charEncoding != null) {
                            buf.append(new String(val, this.charEncoding));
                        } else {
                            buf.append(StringUtils.toAsciiString(val));
                        }
                    } else {
                        buf.append((String) batchArg);
                    }
                }
                if (this.charEncoding != null) {
                    buf.append(StringUtils.toString(this.staticSqlStrings[this.parameterCount + getParameterIndexOffset()], this.charEncoding));
                } else {
                    buf.append(StringUtils.toAsciiString(this.staticSqlStrings[this.parameterCount + getParameterIndexOffset()]));
                }
                stringBuilder = buf.toString();
            } catch (UnsupportedEncodingException e) {
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append(Messages.getString("PreparedStatement.32"));
                stringBuilder2.append(this.charEncoding);
                stringBuilder2.append(Messages.getString("PreparedStatement.33"));
                throw new RuntimeException(stringBuilder2.toString());
            }
        }
        return stringBuilder;
    }

    public void clearBatch() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            this.batchHasPlainStatements = false;
            super.clearBatch();
        }
    }

    public void clearParameters() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            for (int i = 0; i < this.parameterValues.length; i++) {
                this.parameterValues[i] = null;
                this.parameterStreams[i] = null;
                this.isStream[i] = false;
                this.isNull[i] = false;
                this.parameterTypes[i] = 0;
            }
        }
    }

    private final void escapeblockFast(byte[] buf, Buffer packet, int size) throws SQLException {
        int lastwritten = 0;
        for (int i = 0; i < size; i++) {
            byte b = buf[i];
            if (b == (byte) 0) {
                if (i > lastwritten) {
                    packet.writeBytesNoNull(buf, lastwritten, i - lastwritten);
                }
                packet.writeByte((byte) 92);
                packet.writeByte((byte) 48);
                lastwritten = i + 1;
            } else if (b == (byte) 92 || b == (byte) 39 || (!this.usingAnsiMode && b == (byte) 34)) {
                if (i > lastwritten) {
                    packet.writeBytesNoNull(buf, lastwritten, i - lastwritten);
                }
                packet.writeByte((byte) 92);
                lastwritten = i;
            }
        }
        if (lastwritten < size) {
            packet.writeBytesNoNull(buf, lastwritten, size - lastwritten);
        }
    }

    private final void escapeblockFast(byte[] buf, ByteArrayOutputStream bytesOut, int size) {
        int lastwritten = 0;
        for (int i = 0; i < size; i++) {
            byte b = buf[i];
            if (b == (byte) 0) {
                if (i > lastwritten) {
                    bytesOut.write(buf, lastwritten, i - lastwritten);
                }
                bytesOut.write(92);
                bytesOut.write(48);
                lastwritten = i + 1;
            } else if (b == (byte) 92 || b == (byte) 39 || (!this.usingAnsiMode && b == (byte) 34)) {
                if (i > lastwritten) {
                    bytesOut.write(buf, lastwritten, i - lastwritten);
                }
                bytesOut.write(92);
                lastwritten = i;
            }
        }
        if (lastwritten < size) {
            bytesOut.write(buf, lastwritten, size - lastwritten);
        }
    }

    protected boolean checkReadOnlySafeStatement() throws SQLException {
        boolean z;
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.firstCharOfStmt != 'S') {
                if (this.connection.isReadOnly()) {
                    z = false;
                }
            }
            z = true;
        }
        return z;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean execute() throws java.sql.SQLException {
        /*
        r21 = this;
        r8 = r21;
        r1 = r21.checkClosed();
        r9 = r1.getConnectionMutex();
        monitor-enter(r9);
        r1 = r8.connection;	 Catch:{ all -> 0x0116 }
        r10 = r1;
        r1 = r8.doPingInstead;	 Catch:{ all -> 0x0116 }
        if (r1 != 0) goto L_0x003e;
    L_0x0012:
        r1 = r21.checkReadOnlySafeStatement();	 Catch:{ all -> 0x0116 }
        if (r1 != 0) goto L_0x003e;
    L_0x0018:
        r1 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0116 }
        r1.<init>();	 Catch:{ all -> 0x0116 }
        r2 = "PreparedStatement.20";
        r2 = com.mysql.jdbc.Messages.getString(r2);	 Catch:{ all -> 0x0116 }
        r1.append(r2);	 Catch:{ all -> 0x0116 }
        r2 = "PreparedStatement.21";
        r2 = com.mysql.jdbc.Messages.getString(r2);	 Catch:{ all -> 0x0116 }
        r1.append(r2);	 Catch:{ all -> 0x0116 }
        r1 = r1.toString();	 Catch:{ all -> 0x0116 }
        r2 = "S1009";
        r3 = r21.getExceptionInterceptor();	 Catch:{ all -> 0x0116 }
        r1 = com.mysql.jdbc.SQLError.createSQLException(r1, r2, r3);	 Catch:{ all -> 0x0116 }
        throw r1;	 Catch:{ all -> 0x0116 }
    L_0x003e:
        r11 = 0;
        r12 = 0;
        r8.lastQueryIsOnDupKeyUpdate = r12;	 Catch:{ all -> 0x0116 }
        r1 = r8.retrieveGeneratedKeys;	 Catch:{ all -> 0x0116 }
        if (r1 == 0) goto L_0x004c;
    L_0x0046:
        r1 = r21.containsOnDuplicateKeyUpdateInSQL();	 Catch:{ all -> 0x0116 }
        r8.lastQueryIsOnDupKeyUpdate = r1;	 Catch:{ all -> 0x0116 }
    L_0x004c:
        r13 = 0;
        r8.batchedGeneratedKeys = r13;	 Catch:{ all -> 0x0116 }
        r21.resetCancelledState();	 Catch:{ all -> 0x0116 }
        r21.implicitlyCloseAllOpenResults();	 Catch:{ all -> 0x0116 }
        r21.clearWarnings();	 Catch:{ all -> 0x0116 }
        r1 = r8.doPingInstead;	 Catch:{ all -> 0x0116 }
        r14 = 1;
        if (r1 == 0) goto L_0x0062;
    L_0x005d:
        r21.doPingInstead();	 Catch:{ all -> 0x0116 }
        monitor-exit(r9);	 Catch:{ all -> 0x0116 }
        return r14;
    L_0x0062:
        r8.setupStreamingTimeout(r10);	 Catch:{ all -> 0x0116 }
        r3 = r21.fillSendPacket();	 Catch:{ all -> 0x0116 }
        r1 = 0;
        r2 = r10.getCatalog();	 Catch:{ all -> 0x0116 }
        r4 = r8.currentCatalog;	 Catch:{ all -> 0x0116 }
        r2 = r2.equals(r4);	 Catch:{ all -> 0x0116 }
        if (r2 != 0) goto L_0x0080;
    L_0x0076:
        r2 = r10.getCatalog();	 Catch:{ all -> 0x0116 }
        r1 = r2;
        r2 = r8.currentCatalog;	 Catch:{ all -> 0x0116 }
        r10.setCatalog(r2);	 Catch:{ all -> 0x0116 }
    L_0x0080:
        r15 = r1;
        r1 = 0;
        r2 = r10.getCacheResultSetMetadata();	 Catch:{ all -> 0x0116 }
        if (r2 == 0) goto L_0x008f;
    L_0x0088:
        r2 = r8.originalSql;	 Catch:{ all -> 0x0116 }
        r2 = r10.getCachedMetaData(r2);	 Catch:{ all -> 0x0116 }
        r1 = r2;
    L_0x008f:
        r7 = r1;
        r1 = 0;
        if (r7 == 0) goto L_0x0096;
    L_0x0093:
        r2 = r7.fields;	 Catch:{ all -> 0x0116 }
        r1 = r2;
    L_0x0096:
        r16 = r1;
        r1 = 0;
        r2 = r8.retrieveGeneratedKeys;	 Catch:{ all -> 0x0116 }
        if (r2 == 0) goto L_0x00a5;
    L_0x009d:
        r2 = r10.isReadInfoMsgEnabled();	 Catch:{ all -> 0x0116 }
        r1 = r2;
        r10.setReadInfoMsgEnabled(r14);	 Catch:{ all -> 0x0116 }
    L_0x00a5:
        r6 = r1;
        r1 = r8.firstCharOfStmt;	 Catch:{ all -> 0x0116 }
        r2 = 83;
        if (r1 != r2) goto L_0x00af;
    L_0x00ac:
        r1 = r8.maxRows;	 Catch:{ all -> 0x0116 }
        goto L_0x00b0;
    L_0x00af:
        r1 = -1;
    L_0x00b0:
        r10.setSessionMaxRows(r1);	 Catch:{ all -> 0x0116 }
        r4 = r8.maxRows;	 Catch:{ all -> 0x0116 }
        r5 = r21.createStreamingResultSet();	 Catch:{ all -> 0x0116 }
        r1 = r8.firstCharOfStmt;	 Catch:{ all -> 0x0116 }
        if (r1 != r2) goto L_0x00c0;
    L_0x00bd:
        r17 = r14;
        goto L_0x00c2;
    L_0x00c0:
        r17 = r12;
    L_0x00c2:
        r18 = 0;
        r1 = r8;
        r2 = r4;
        r4 = r5;
        r5 = r17;
        r12 = r6;
        r6 = r16;
        r14 = r7;
        r7 = r18;
        r1 = r1.executeInternal(r2, r3, r4, r5, r6, r7);	 Catch:{ all -> 0x0116 }
        if (r14 == 0) goto L_0x00db;
    L_0x00d5:
        r2 = r8.originalSql;	 Catch:{ all -> 0x0116 }
        r10.initializeResultsMetadataFromCache(r2, r14, r1);	 Catch:{ all -> 0x0116 }
        goto L_0x00ec;
    L_0x00db:
        r2 = r1.reallyResult();	 Catch:{ all -> 0x0116 }
        if (r2 == 0) goto L_0x00ec;
    L_0x00e1:
        r2 = r10.getCacheResultSetMetadata();	 Catch:{ all -> 0x0116 }
        if (r2 == 0) goto L_0x00ec;
    L_0x00e7:
        r2 = r8.originalSql;	 Catch:{ all -> 0x0116 }
        r10.initializeResultsMetadataFromCache(r2, r13, r1);	 Catch:{ all -> 0x0116 }
    L_0x00ec:
        r2 = r8.retrieveGeneratedKeys;	 Catch:{ all -> 0x0116 }
        if (r2 == 0) goto L_0x00f8;
    L_0x00f0:
        r10.setReadInfoMsgEnabled(r12);	 Catch:{ all -> 0x0116 }
        r2 = r8.firstCharOfStmt;	 Catch:{ all -> 0x0116 }
        r1.setFirstCharOfQuery(r2);	 Catch:{ all -> 0x0116 }
    L_0x00f8:
        if (r15 == 0) goto L_0x00fd;
    L_0x00fa:
        r10.setCatalog(r15);	 Catch:{ all -> 0x0116 }
    L_0x00fd:
        if (r1 == 0) goto L_0x0107;
    L_0x00ff:
        r4 = r1.getUpdateID();	 Catch:{ all -> 0x0116 }
        r8.lastInsertId = r4;	 Catch:{ all -> 0x0116 }
        r8.results = r1;	 Catch:{ all -> 0x0116 }
    L_0x0107:
        if (r1 == 0) goto L_0x0112;
    L_0x0109:
        r2 = r1.reallyResult();	 Catch:{ all -> 0x0116 }
        if (r2 == 0) goto L_0x0112;
    L_0x010f:
        r19 = 1;
        goto L_0x0114;
    L_0x0112:
        r19 = 0;
    L_0x0114:
        monitor-exit(r9);	 Catch:{ all -> 0x0116 }
        return r19;
    L_0x0116:
        r0 = move-exception;
        r1 = r0;
        monitor-exit(r9);	 Catch:{ all -> 0x0116 }
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.PreparedStatement.execute():boolean");
    }

    protected long[] executeBatchInternal() throws SQLException {
        Throwable th;
        synchronized (checkClosed().getConnectionMutex()) {
            PreparedStatement this;
            try {
                if (this.connection.isReadOnly()) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(Messages.getString("PreparedStatement.25"));
                    stringBuilder.append(Messages.getString("PreparedStatement.26"));
                    throw new SQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
                }
                if (this.batchedArgs != null) {
                    if (this.batchedArgs.size() != 0) {
                        int batchTimeout = this.timeoutInMillis;
                        this.timeoutInMillis = 0;
                        resetCancelledState();
                        try {
                            long[] executeBatchedInserts;
                            statementBegins();
                            clearWarnings();
                            if (!this.batchHasPlainStatements && this.connection.getRewriteBatchedStatements()) {
                                if (canRewriteAsMultiValueInsertAtSqlLevel()) {
                                    executeBatchedInserts = executeBatchedInserts(batchTimeout);
                                    this.statementExecuting.set(false);
                                    clearBatch();
                                    return executeBatchedInserts;
                                } else if (this.connection.versionMeetsMinimum(4, 1, 0) && !this.batchHasPlainStatements && this.batchedArgs != null && this.batchedArgs.size() > 3) {
                                    executeBatchedInserts = executePreparedBatchAsMultiStatement(batchTimeout);
                                    this.statementExecuting.set(false);
                                    clearBatch();
                                    return executeBatchedInserts;
                                }
                            }
                            executeBatchedInserts = executeBatchSerially(batchTimeout);
                            this.statementExecuting.set(false);
                            clearBatch();
                            return executeBatchedInserts;
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    }
                }
                long[] jArr = new long[0];
                return jArr;
            } catch (Throwable th3) {
                th = th3;
                this = this;
                throw th;
            }
        }
    }

    public boolean canRewriteAsMultiValueInsertAtSqlLevel() throws SQLException {
        return this.parseInfo.canRewriteAsMultiValueInsert;
    }

    protected int getLocationOfOnDuplicateKeyUpdate() throws SQLException {
        return this.parseInfo.locationOfOnDuplicateKeyUpdate;
    }

    protected long[] executePreparedBatchAsMultiStatement(int batchTimeout) throws SQLException {
        Throwable th;
        int i;
        Throwable th2;
        PreparedStatement this;
        MySQLConnection locallyScopedConn;
        boolean multiQueriesEnabled;
        CancelTask timeoutTask;
        PreparedStatement this2;
        int batchTimeout2;
        TimerTask timeoutTask2;
        java.sql.PreparedStatement batchedStatement;
        int i2;
        int i3;
        SQLException sqlEx;
        java.sql.PreparedStatement batchedStatement2;
        StatementImpl statementImpl = this;
        int i4 = batchTimeout;
        synchronized (checkClosed().getConnectionMutex()) {
            try {
                if (statementImpl.batchedValuesClause == null) {
                    try {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(statementImpl.originalSql);
                        stringBuilder.append(";");
                        statementImpl.batchedValuesClause = stringBuilder.toString();
                    } catch (Throwable th3) {
                        th = th3;
                        StatementImpl statementImpl2 = statementImpl;
                        i = i4;
                        th2 = th;
                        throw th2;
                    }
                }
                MySQLConnection locallyScopedConn2 = statementImpl.connection;
                boolean multiQueriesEnabled2 = locallyScopedConn2.getAllowMultiQueries();
                CancelTask timeoutTask3 = null;
                try {
                    SQLException sqlEx2;
                    clearWarnings();
                    int numBatchedArgs = statementImpl.batchedArgs.size();
                    if (statementImpl.retrieveGeneratedKeys) {
                        try {
                            statementImpl.batchedGeneratedKeys = new ArrayList(numBatchedArgs);
                        } catch (Throwable th4) {
                            th = th4;
                            this = statementImpl;
                            i = i4;
                            th2 = th;
                            locallyScopedConn = locallyScopedConn2;
                            multiQueriesEnabled = multiQueriesEnabled2;
                            timeoutTask = timeoutTask3;
                            this2 = this;
                            batchTimeout2 = i;
                            if (timeoutTask != null) {
                                try {
                                    timeoutTask.cancel();
                                    locallyScopedConn.getCancelTimer().purge();
                                } catch (Throwable th5) {
                                    th2 = th5;
                                    this = this2;
                                    i = batchTimeout2;
                                    throw th2;
                                }
                            }
                            resetCancelledState();
                            if (!multiQueriesEnabled) {
                                locallyScopedConn.getIO().disableMultiQueries();
                            }
                            clearBatch();
                            throw th2;
                        }
                    }
                    int numValuesPerBatch = computeBatchSize(numBatchedArgs);
                    if (numBatchedArgs < numValuesPerBatch) {
                        numValuesPerBatch = numBatchedArgs;
                    }
                    java.sql.PreparedStatement batchedStatement3 = null;
                    int batchedParamIndex = 1;
                    int numberToExecuteAsMultiValue = 0;
                    int batchCounter = 0;
                    int updateCountCounter = 0;
                    long[] updateCounts = new long[numBatchedArgs];
                    if (multiQueriesEnabled2) {
                        sqlEx2 = null;
                    } else {
                        sqlEx2 = null;
                        locallyScopedConn2.getIO().enableMultiQueries();
                    }
                    try {
                        java.sql.PreparedStatement batchedStatement4;
                        int numberToExecuteAsMultiValue2;
                        int i5;
                        timeoutTask2 = timeoutTask3;
                        if (statementImpl.retrieveGeneratedKeys) {
                            try {
                                batchedStatement4 = (java.sql.PreparedStatement) ((Wrapper) locallyScopedConn2.prepareStatement(generateMultiStatementForBatch(numValuesPerBatch), 1)).unwrap(java.sql.PreparedStatement.class);
                            } catch (Throwable th52) {
                                th2 = th52;
                                timeoutTask3 = timeoutTask2;
                                batchedStatement = batchedStatement3;
                                this = this;
                                i = batchTimeout;
                                if (batchedStatement != null) {
                                    batchedStatement.close();
                                }
                                throw th2;
                            }
                        }
                        try {
                            batchedStatement4 = (java.sql.PreparedStatement) ((Wrapper) locallyScopedConn2.prepareStatement(generateMultiStatementForBatch(numValuesPerBatch))).unwrap(java.sql.PreparedStatement.class);
                        } catch (Throwable th522) {
                            i2 = 1;
                            i3 = 0;
                            th2 = th522;
                            timeoutTask3 = timeoutTask2;
                            batchedStatement = batchedStatement3;
                            this = this;
                            i = batchTimeout;
                            if (batchedStatement != null) {
                                batchedStatement.close();
                            }
                            throw th2;
                        }
                        batchedStatement3 = batchedStatement4;
                        if (locallyScopedConn2.getEnableQueryTimeouts() && i4 != 0 && locallyScopedConn2.versionMeetsMinimum(5, 0, 0)) {
                            timeoutTask3 = new CancelTask((StatementImpl) batchedStatement3);
                            try {
                                i2 = 1;
                                i3 = 0;
                                locallyScopedConn2.getCancelTimer().schedule(timeoutTask3, (long) i4);
                            } catch (Throwable th5222) {
                                th2 = th5222;
                                batchedParamIndex = i2;
                                numberToExecuteAsMultiValue = i3;
                                batchedStatement = batchedStatement3;
                                this = this;
                                i = batchTimeout;
                                if (batchedStatement != null) {
                                    batchedStatement.close();
                                }
                                throw th2;
                            }
                        }
                        i2 = 1;
                        i3 = 0;
                        timeoutTask3 = timeoutTask2;
                        if (numBatchedArgs < numValuesPerBatch) {
                            numberToExecuteAsMultiValue2 = numBatchedArgs;
                        } else {
                            numberToExecuteAsMultiValue2 = numBatchedArgs / numValuesPerBatch;
                        }
                        numberToExecuteAsMultiValue = numberToExecuteAsMultiValue2;
                        numberToExecuteAsMultiValue2 = numberToExecuteAsMultiValue * numValuesPerBatch;
                        batchedParamIndex = i2;
                        int i6 = 0;
                        while (true) {
                            i4 = i6;
                            if (i4 >= numberToExecuteAsMultiValue2) {
                                break;
                            }
                            int batchCounter2;
                            if (i4 != 0) {
                                try {
                                    if (i4 % numValuesPerBatch == 0) {
                                        batchedStatement3.execute();
                                        i5 = numberToExecuteAsMultiValue2;
                                        numberToExecuteAsMultiValue2 = sqlEx2;
                                        SQLException sqlEx3 = numberToExecuteAsMultiValue2;
                                        try {
                                            updateCountCounter = processMultiCountsAndKeys((StatementImpl) batchedStatement3, updateCountCounter, updateCounts);
                                            batchedStatement3.clearParameters();
                                            batchedParamIndex = 1;
                                            sqlEx2 = sqlEx3;
                                            batchCounter2 = batchCounter + 1;
                                            batchedParamIndex = setOneBatchedParameterSet(batchedStatement3, batchedParamIndex, statementImpl.batchedArgs.get(batchCounter));
                                            i6 = i4 + 1;
                                            batchCounter = batchCounter2;
                                            numberToExecuteAsMultiValue2 = i5;
                                            i4 = batchTimeout;
                                        } catch (Throwable th52222) {
                                            th2 = th52222;
                                            sqlEx2 = sqlEx3;
                                        }
                                    }
                                } catch (SQLException e) {
                                    i5 = numberToExecuteAsMultiValue2;
                                    numberToExecuteAsMultiValue2 = handleExceptionForBatch(batchCounter, numValuesPerBatch, updateCounts, e);
                                } catch (Throwable th522222) {
                                    th2 = th522222;
                                }
                            }
                            i5 = numberToExecuteAsMultiValue2;
                            batchCounter2 = batchCounter + 1;
                            try {
                                batchedParamIndex = setOneBatchedParameterSet(batchedStatement3, batchedParamIndex, statementImpl.batchedArgs.get(batchCounter));
                                i6 = i4 + 1;
                                batchCounter = batchCounter2;
                                numberToExecuteAsMultiValue2 = i5;
                                i4 = batchTimeout;
                            } catch (Throwable th5222222) {
                                th2 = th5222222;
                                batchCounter = batchCounter2;
                            }
                        }
                        i5 = numberToExecuteAsMultiValue2;
                        try {
                            batchedStatement3.execute();
                            sqlEx = sqlEx2;
                        } catch (SQLException e2) {
                            sqlEx = handleExceptionForBatch(batchCounter - 1, numValuesPerBatch, updateCounts, e2);
                            SQLException ex = sqlEx;
                        }
                        try {
                            updateCountCounter = processMultiCountsAndKeys((StatementImpl) batchedStatement3, updateCountCounter, updateCounts);
                            batchedStatement3.clearParameters();
                            i4 = numBatchedArgs - batchCounter;
                            java.sql.PreparedStatement batchedStatement5 = batchedStatement3;
                            int batchedParamIndex2 = batchedParamIndex;
                            batchedParamIndex = numberToExecuteAsMultiValue;
                            numberToExecuteAsMultiValue = batchCounter;
                            batchCounter = updateCounts;
                            this = statementImpl;
                            i = batchTimeout;
                            if (batchedStatement5 != null) {
                                try {
                                    batchedStatement5.close();
                                    batchedStatement5 = null;
                                } catch (Throwable th6) {
                                    th5222222 = th6;
                                    th2 = th5222222;
                                    locallyScopedConn = locallyScopedConn2;
                                    multiQueriesEnabled = multiQueriesEnabled2;
                                    timeoutTask = timeoutTask3;
                                    this2 = this;
                                    batchTimeout2 = i;
                                    if (timeoutTask != null) {
                                        timeoutTask.cancel();
                                        locallyScopedConn.getCancelTimer().purge();
                                    }
                                    resetCancelledState();
                                    if (multiQueriesEnabled) {
                                        locallyScopedConn.getIO().disableMultiQueries();
                                    }
                                    clearBatch();
                                    throw th2;
                                }
                            }
                            if (i4 > 0) {
                                int batchedParamIndex3;
                                SQLException sqlEx4 = sqlEx;
                                try {
                                    if (this.retrieveGeneratedKeys != null) {
                                        batchedStatement2 = batchedStatement5;
                                        try {
                                            batchedStatement4 = locallyScopedConn2.prepareStatement(generateMultiStatementForBatch(i4), 1);
                                        } catch (Throwable th52222222) {
                                            th2 = th52222222;
                                            sqlEx = sqlEx4;
                                            batchedStatement5 = batchedStatement2;
                                            if (batchedStatement5 != null) {
                                                batchedStatement5.close();
                                            }
                                            throw th2;
                                        }
                                    }
                                    batchedStatement4 = locallyScopedConn2.prepareStatement(generateMultiStatementForBatch(i4));
                                    batchedStatement5 = batchedStatement4;
                                    if (timeoutTask3 != null) {
                                        try {
                                            timeoutTask3.toCancel = (StatementImpl) batchedStatement5;
                                        } catch (Throwable th522222222) {
                                            th2 = th522222222;
                                            if (batchedStatement5 != null) {
                                                batchedStatement5.close();
                                            }
                                            throw th2;
                                        }
                                    }
                                    batchedParamIndex2 = 1;
                                    while (numberToExecuteAsMultiValue < numBatchedArgs) {
                                        CancelTask timeoutTask4 = numberToExecuteAsMultiValue + 1;
                                        try {
                                            batchedParamIndex2 = setOneBatchedParameterSet(batchedStatement5, batchedParamIndex2, this.batchedArgs.get(numberToExecuteAsMultiValue));
                                            numberToExecuteAsMultiValue = timeoutTask4;
                                        } catch (Throwable th5222222222) {
                                            th2 = th5222222222;
                                            CancelTask cancelTask = timeoutTask4;
                                        }
                                    }
                                    try {
                                        batchedStatement5.execute();
                                        batchedParamIndex3 = batchedParamIndex2;
                                        sqlEx = sqlEx4;
                                    } catch (SQLException e22) {
                                        batchedParamIndex3 = batchedParamIndex2;
                                        sqlEx = handleExceptionForBatch(numberToExecuteAsMultiValue - 1, i4, batchCounter, e22);
                                    } catch (Throwable th52222222222) {
                                        th2 = th52222222222;
                                        sqlEx = sqlEx4;
                                        if (batchedStatement5 != null) {
                                            batchedStatement5.close();
                                        }
                                        throw th2;
                                    }
                                } catch (Throwable th522222222222) {
                                    batchedStatement2 = batchedStatement5;
                                    th2 = th522222222222;
                                    sqlEx = sqlEx4;
                                    if (batchedStatement5 != null) {
                                        batchedStatement5.close();
                                    }
                                    throw th2;
                                }
                                try {
                                    updateCountCounter = processMultiCountsAndKeys((StatementImpl) batchedStatement5, updateCountCounter, batchCounter);
                                    batchedStatement5.clearParameters();
                                    batchedParamIndex2 = batchedParamIndex3;
                                } catch (Throwable th5222222222222) {
                                    th2 = th5222222222222;
                                    if (batchedStatement5 != null) {
                                        batchedStatement5.close();
                                    }
                                    throw th2;
                                }
                            }
                            batchedStatement2 = batchedStatement5;
                            if (timeoutTask3 != null) {
                                try {
                                    if (timeoutTask3.caughtWhileCancelling != null) {
                                        throw timeoutTask3.caughtWhileCancelling;
                                    }
                                    timeoutTask3.cancel();
                                    locallyScopedConn2.getCancelTimer().purge();
                                    timeoutTask3 = null;
                                } catch (Throwable th52222222222222) {
                                    th2 = th52222222222222;
                                    if (batchedStatement5 != null) {
                                        batchedStatement5.close();
                                    }
                                    throw th2;
                                }
                            }
                            if (sqlEx != null) {
                                throw SQLError.createBatchUpdateException(sqlEx, batchCounter, getExceptionInterceptor());
                            }
                            int numBatchedArgs2 = numBatchedArgs;
                            numBatchedArgs = batchedStatement5;
                            numValuesPerBatch = batchedParamIndex2;
                            batchedStatement3 = batchedParamIndex;
                            batchedParamIndex = numberToExecuteAsMultiValue;
                            numberToExecuteAsMultiValue = updateCountCounter;
                            updateCountCounter = batchCounter;
                            if (numBatchedArgs != 0) {
                                numBatchedArgs.close();
                            }
                            if (timeoutTask3 != null) {
                                try {
                                    timeoutTask3.cancel();
                                    locallyScopedConn2.getCancelTimer().purge();
                                } catch (Throwable th7) {
                                    th52222222222222 = th7;
                                    th2 = th52222222222222;
                                    throw th2;
                                }
                            }
                            resetCancelledState();
                            if (!multiQueriesEnabled2) {
                                locallyScopedConn2.getIO().disableMultiQueries();
                            }
                            clearBatch();
                            return batchCounter;
                        } catch (Throwable th522222222222222) {
                            th2 = th522222222222222;
                            sqlEx2 = sqlEx;
                            batchedStatement = batchedStatement3;
                            this = this;
                            i = batchTimeout;
                            if (batchedStatement != null) {
                                batchedStatement.close();
                            }
                            throw th2;
                        }
                    } catch (Throwable th5222222222222222) {
                        timeoutTask2 = timeoutTask3;
                        i2 = 1;
                        i3 = 0;
                        th2 = th5222222222222222;
                        batchedStatement = batchedStatement3;
                        this = this;
                        i = batchTimeout;
                        if (batchedStatement != null) {
                            batchedStatement.close();
                        }
                        throw th2;
                    }
                } catch (Throwable th52222222222222222) {
                    timeoutTask2 = timeoutTask3;
                    this = this;
                    i = batchTimeout;
                    th2 = th52222222222222222;
                    locallyScopedConn = locallyScopedConn2;
                    multiQueriesEnabled = multiQueriesEnabled2;
                    timeoutTask = timeoutTask3;
                    this2 = this;
                    batchTimeout2 = i;
                    if (timeoutTask != null) {
                        timeoutTask.cancel();
                        locallyScopedConn.getCancelTimer().purge();
                    }
                    resetCancelledState();
                    if (multiQueriesEnabled) {
                        locallyScopedConn.getIO().disableMultiQueries();
                    }
                    clearBatch();
                    throw th2;
                }
            } catch (Throwable th8) {
                th52222222222222222 = th8;
                this = this;
                i = batchTimeout;
                th2 = th52222222222222222;
                throw th2;
            }
        }
    }

    private String generateMultiStatementForBatch(int numBatches) throws SQLException {
        String stringBuilder;
        synchronized (checkClosed().getConnectionMutex()) {
            StringBuilder newStatementSql = new StringBuilder((this.originalSql.length() + 1) * numBatches);
            newStatementSql.append(this.originalSql);
            for (int i = 0; i < numBatches - 1; i++) {
                newStatementSql.append(';');
                newStatementSql.append(this.originalSql);
            }
            stringBuilder = newStatementSql.toString();
        }
        return stringBuilder;
    }

    protected long[] executeBatchedInserts(int batchTimeout) throws SQLException {
        Throwable th;
        Throwable th2;
        int i;
        int batchCounter;
        SQLException sqlEx;
        int batchCounter2;
        PreparedStatement batchedStatement;
        long updateCountRunningTotal;
        int batchTimeout2;
        StatementImpl statementImpl = this;
        int batchTimeout3 = batchTimeout;
        synchronized (checkClosed().getConnectionMutex()) {
            int batchedParamIndex;
            PreparedStatement preparedStatement;
            try {
                String valuesClause = getValuesClause();
                MySQLConnection locallyScopedConn = statementImpl.connection;
                if (valuesClause == null) {
                    try {
                        long[] executeBatchSerially = executeBatchSerially(batchTimeout);
                        return executeBatchSerially;
                    } catch (Throwable th3) {
                        th = th3;
                        StatementImpl statementImpl2 = statementImpl;
                        th2 = th;
                        throw th2;
                    }
                }
                int numBatchedArgs = statementImpl.batchedArgs.size();
                if (statementImpl.retrieveGeneratedKeys) {
                    statementImpl.batchedGeneratedKeys = new ArrayList(numBatchedArgs);
                }
                int numValuesPerBatch = computeBatchSize(numBatchedArgs);
                if (numBatchedArgs < numValuesPerBatch) {
                    numValuesPerBatch = numBatchedArgs;
                }
                int numberToExecuteAsMultiValue = 0;
                CancelTask cancelTask = 0;
                CancelTask timeoutTask = null;
                preparedStatement = 0;
                PreparedStatement batchedStatement2 = null;
                long[] updateCounts = new long[numBatchedArgs];
                long updateCountRunningTotal2;
                int batchedParamIndex2;
                long j;
                PreparedStatement batchedStatement3;
                try {
                    updateCountRunningTotal2 = prepareBatchedInsertSQL(locallyScopedConn, numValuesPerBatch);
                    PreparedStatement batchedStatement4 = updateCountRunningTotal2;
                    try {
                        int numberToExecuteAsMultiValue2;
                        int i2;
                        batchedParamIndex2 = 1;
                        if (!locallyScopedConn.getEnableQueryTimeouts() || batchTimeout3 == 0) {
                            j = 0;
                            i = 0;
                            batchCounter = 0;
                            batchedStatement3 = batchedStatement4;
                        } else {
                            j = 0;
                            try {
                                if (locallyScopedConn.versionMeetsMinimum(5, 0, 0)) {
                                    batchedStatement3 = batchedStatement4;
                                    try {
                                        timeoutTask = new CancelTask(batchedStatement3);
                                        i = 0;
                                        batchCounter = 0;
                                        locallyScopedConn.getCancelTimer().schedule(timeoutTask, (long) batchTimeout3);
                                    } catch (Throwable th4) {
                                        th2 = th4;
                                        sqlEx = null;
                                        numberToExecuteAsMultiValue = i;
                                        batchCounter2 = batchCounter;
                                        batchedStatement = batchedStatement3;
                                        updateCountRunningTotal2 = j;
                                        if (batchedStatement != null) {
                                            batchedStatement.close();
                                        }
                                        throw th2;
                                    }
                                }
                                i = 0;
                                batchCounter = 0;
                                batchedStatement3 = batchedStatement4;
                            } catch (Throwable th42) {
                                i = 0;
                                batchedStatement3 = batchedStatement4;
                                th2 = th42;
                                sqlEx = null;
                                batchCounter2 = 0;
                                batchedStatement = batchedStatement3;
                                updateCountRunningTotal2 = j;
                                if (batchedStatement != null) {
                                    batchedStatement.close();
                                }
                                throw th2;
                            }
                        }
                        if (numBatchedArgs < numValuesPerBatch) {
                            numberToExecuteAsMultiValue2 = numBatchedArgs;
                        } else {
                            numberToExecuteAsMultiValue2 = numBatchedArgs / numValuesPerBatch;
                        }
                        numberToExecuteAsMultiValue = numberToExecuteAsMultiValue2;
                        numberToExecuteAsMultiValue2 = numberToExecuteAsMultiValue * numValuesPerBatch;
                        cancelTask = 0;
                        sqlEx = null;
                        preparedStatement = batchCounter;
                        while (cancelTask < numberToExecuteAsMultiValue2) {
                            int batchCounter3;
                            if (cancelTask != 0) {
                                if (cancelTask % numValuesPerBatch == 0) {
                                    try {
                                        i2 = numberToExecuteAsMultiValue2;
                                        j += batchedStatement3.executeLargeUpdate();
                                    } catch (SQLException e) {
                                        i2 = numberToExecuteAsMultiValue2;
                                        sqlEx = handleExceptionForBatch(preparedStatement - 1, numValuesPerBatch, updateCounts, e);
                                    } catch (Throwable th422) {
                                        th2 = th422;
                                    }
                                    getBatchedGeneratedKeys((Statement) batchedStatement3);
                                    batchedStatement3.clearParameters();
                                    batchedParamIndex = 1;
                                    batchCounter3 = preparedStatement + 1;
                                    try {
                                        batchedParamIndex2 = setOneBatchedParameterSet(batchedStatement3, batchedParamIndex, statementImpl.batchedArgs.get(preparedStatement));
                                        cancelTask++;
                                        preparedStatement = batchCounter3;
                                        numberToExecuteAsMultiValue2 = i2;
                                    } catch (Throwable th4222) {
                                        th2 = th4222;
                                        batchedParamIndex2 = batchedParamIndex;
                                        batchCounter2 = batchCounter3;
                                    }
                                }
                            }
                            i2 = numberToExecuteAsMultiValue2;
                            batchedParamIndex = batchedParamIndex2;
                            try {
                                batchCounter3 = preparedStatement + 1;
                                batchedParamIndex2 = setOneBatchedParameterSet(batchedStatement3, batchedParamIndex, statementImpl.batchedArgs.get(preparedStatement));
                                cancelTask++;
                                preparedStatement = batchCounter3;
                                numberToExecuteAsMultiValue2 = i2;
                            } catch (Throwable th42222) {
                                th2 = th42222;
                                batchedParamIndex2 = batchedParamIndex;
                            }
                        }
                        i2 = numberToExecuteAsMultiValue2;
                        try {
                            updateCountRunningTotal2 = j + batchedStatement3.executeLargeUpdate();
                        } catch (SQLException e2) {
                            sqlEx = handleExceptionForBatch(preparedStatement - 1, numValuesPerBatch, updateCounts, e2);
                            updateCountRunningTotal2 = j;
                        }
                        try {
                            getBatchedGeneratedKeys((Statement) batchedStatement3);
                            numValuesPerBatch = numBatchedArgs - preparedStatement;
                            batchedParamIndex = batchedStatement3;
                            batchedStatement3 = numberToExecuteAsMultiValue;
                            numberToExecuteAsMultiValue = preparedStatement;
                            cancelTask = timeoutTask;
                            timeoutTask = sqlEx;
                            preparedStatement = statementImpl;
                            if (batchedParamIndex != null) {
                                batchedParamIndex.close();
                                batchedParamIndex = null;
                            }
                            SQLException sqlEx2;
                            if (numValuesPerBatch > 0) {
                                try {
                                    SQLException sqlEx3;
                                    int batchedParamIndex3;
                                    batchedParamIndex = prepareBatchedInsertSQL(locallyScopedConn, numValuesPerBatch);
                                    if (cancelTask != null) {
                                        try {
                                            cancelTask.toCancel = batchedParamIndex;
                                        } catch (Throwable th422222) {
                                            th2 = th422222;
                                            if (batchedParamIndex != 0) {
                                                batchedParamIndex.close();
                                            }
                                            throw th2;
                                        }
                                    }
                                    numberToExecuteAsMultiValue2 = 1;
                                    while (numberToExecuteAsMultiValue < numBatchedArgs) {
                                        sqlEx2 = sqlEx3;
                                        try {
                                            sqlEx = numberToExecuteAsMultiValue + 1;
                                            try {
                                                numberToExecuteAsMultiValue2 = setOneBatchedParameterSet(batchedParamIndex, numberToExecuteAsMultiValue2, preparedStatement.batchedArgs.get(numberToExecuteAsMultiValue));
                                                numberToExecuteAsMultiValue = sqlEx;
                                                sqlEx3 = sqlEx2;
                                            } catch (Throwable th4222222) {
                                                th2 = th4222222;
                                                numberToExecuteAsMultiValue = sqlEx;
                                            }
                                        } catch (Throwable th42222222) {
                                            th2 = th42222222;
                                        }
                                    }
                                    sqlEx2 = sqlEx3;
                                    try {
                                        batchedParamIndex3 = numberToExecuteAsMultiValue2;
                                        updateCountRunningTotal2 += batchedParamIndex.executeLargeUpdate();
                                        timeoutTask = sqlEx2;
                                    } catch (SQLException e22) {
                                        batchedParamIndex3 = numberToExecuteAsMultiValue2;
                                        timeoutTask = handleExceptionForBatch(numberToExecuteAsMultiValue - 1, numValuesPerBatch, updateCounts, e22);
                                    } catch (Throwable th422222222) {
                                        th2 = th422222222;
                                        sqlEx3 = sqlEx2;
                                        updateCountRunningTotal = batchedParamIndex3;
                                        if (batchedParamIndex != 0) {
                                            batchedParamIndex.close();
                                        }
                                        throw th2;
                                    }
                                    try {
                                        getBatchedGeneratedKeys((Statement) batchedParamIndex);
                                        numberToExecuteAsMultiValue2 = batchedParamIndex3;
                                    } catch (Throwable th4222222222) {
                                        th2 = th4222222222;
                                        updateCountRunningTotal = batchedParamIndex3;
                                        if (batchedParamIndex != 0) {
                                            batchedParamIndex.close();
                                        }
                                        throw th2;
                                    }
                                } catch (Throwable th42222222222) {
                                    sqlEx2 = timeoutTask;
                                    th2 = th42222222222;
                                    if (batchedParamIndex != 0) {
                                        batchedParamIndex.close();
                                    }
                                    throw th2;
                                }
                            }
                            sqlEx2 = timeoutTask;
                            if (timeoutTask != null) {
                                throw SQLError.createBatchUpdateException(timeoutTask, updateCounts, getExceptionInterceptor());
                            }
                            if (numBatchedArgs > 1) {
                                batchedParamIndex2 = 0;
                                if (updateCountRunningTotal2 > 0) {
                                    batchedParamIndex2 = -2;
                                }
                                int j2 = 0;
                                while (true) {
                                    int j3 = j2;
                                    if (j3 >= numBatchedArgs) {
                                        break;
                                    }
                                    updateCounts[j3] = batchedParamIndex2;
                                    j2 = j3 + 1;
                                }
                            } else {
                                updateCounts[0] = updateCountRunningTotal2;
                            }
                            long[] updateCounts2 = updateCounts;
                            if (batchedParamIndex != 0) {
                                try {
                                    batchedParamIndex.close();
                                } catch (Throwable th5) {
                                    th42222222222 = th5;
                                    updateCounts = updateCounts2;
                                    th2 = th42222222222;
                                    if (cancelTask == null) {
                                    } else {
                                        try {
                                            cancelTask.cancel();
                                            batchTimeout2 = batchTimeout3;
                                            locallyScopedConn.getCancelTimer().purge();
                                        } catch (Throwable th422222222222) {
                                            th2 = th422222222222;
                                            batchTimeout3 = batchTimeout2;
                                            throw th2;
                                        }
                                    }
                                    preparedStatement.resetCancelledState();
                                    throw th2;
                                }
                            }
                            if (cancelTask != null) {
                                try {
                                    cancelTask.cancel();
                                    locallyScopedConn.getCancelTimer().purge();
                                } catch (Throwable th6) {
                                    th422222222222 = th6;
                                    th2 = th422222222222;
                                    throw th2;
                                }
                            }
                            resetCancelledState();
                            return updateCounts;
                        } catch (Throwable th7) {
                            th422222222222 = th7;
                            th2 = th422222222222;
                            if (cancelTask == null) {
                                cancelTask.cancel();
                                batchTimeout2 = batchTimeout3;
                                locallyScopedConn.getCancelTimer().purge();
                            }
                            preparedStatement.resetCancelledState();
                            throw th2;
                        }
                    } catch (Throwable th4222222222222) {
                        batchedParamIndex2 = 1;
                        j = 0;
                        i = 0;
                        batchedStatement3 = batchedStatement4;
                        th2 = th4222222222222;
                        sqlEx = null;
                        batchCounter2 = 0;
                        batchedStatement = batchedStatement3;
                        updateCountRunningTotal2 = j;
                        if (batchedStatement != null) {
                            batchedStatement.close();
                        }
                        throw th2;
                    }
                } catch (Throwable th42222222222222) {
                    batchedParamIndex2 = 1;
                    j = 0;
                    i = 0;
                    th2 = th42222222222222;
                    batchedStatement3 = batchedStatement2;
                    sqlEx = null;
                    batchCounter2 = 0;
                    batchedStatement = batchedStatement3;
                    updateCountRunningTotal2 = j;
                    if (batchedStatement != null) {
                        batchedStatement.close();
                    }
                    throw th2;
                }
            } catch (Throwable th8) {
                th42222222222222 = th8;
                preparedStatement = this;
                th2 = th42222222222222;
                throw th2;
            }
        }
        if (batchedParamIndex != 0) {
            batchedParamIndex.close();
        }
        throw th2;
    }

    protected String getValuesClause() throws SQLException {
        return this.parseInfo.valuesClause;
    }

    protected int computeBatchSize(int numBatchedArgs) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            long[] combinedValues = computeMaxParameterSetSizeAndBatchSize(numBatchedArgs);
            long maxSizeOfParameterSet = combinedValues[0];
            long sizeOfEntireBatch = combinedValues[1];
            int maxAllowedPacket = this.connection.getMaxAllowedPacket();
            if (sizeOfEntireBatch < ((long) (maxAllowedPacket - this.originalSql.length()))) {
                return numBatchedArgs;
            }
            int max = (int) Math.max(1, ((long) (maxAllowedPacket - this.originalSql.length())) / maxSizeOfParameterSet);
            return max;
        }
    }

    protected long[] computeMaxParameterSetSizeAndBatchSize(int r22) throws java.sql.SQLException {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxOverflowException: Regions stack size limit reached
	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:37)
	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:61)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:56)
	at jadx.core.ProcessClass.process(ProcessClass.java:39)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:282)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
	at jadx.api.JadxDecompiler$$Lambda$8/1205406622.run(Unknown Source)
*/
        /*
        r21 = this;
        r1 = r21;
        r2 = r21.checkClosed();
        r2 = r2.getConnectionMutex();
        monitor-enter(r2);
        r3 = 0;
        r5 = 0;
        r8 = r3;
        r3 = 0;
    L_0x0011:
        r10 = r22;
        if (r3 >= r10) goto L_0x00a1;
    L_0x0015:
        r11 = r1.batchedArgs;	 Catch:{ all -> 0x009e }
        r11 = r11.get(r3);	 Catch:{ all -> 0x009e }
        r11 = (com.mysql.jdbc.PreparedStatement.BatchParams) r11;	 Catch:{ all -> 0x009e }
        r12 = r11.isNull;	 Catch:{ all -> 0x009e }
        r13 = r11.isStream;	 Catch:{ all -> 0x009e }
        r14 = 0;	 Catch:{ all -> 0x009e }
        r15 = r14;	 Catch:{ all -> 0x009e }
        r14 = 0;	 Catch:{ all -> 0x009e }
    L_0x0025:
        r7 = r12.length;	 Catch:{ all -> 0x009e }
        if (r14 >= r7) goto L_0x006c;	 Catch:{ all -> 0x009e }
    L_0x0028:
        r7 = r12[r14];	 Catch:{ all -> 0x009e }
        if (r7 != 0) goto L_0x005b;	 Catch:{ all -> 0x009e }
    L_0x002c:
        r7 = r13[r14];	 Catch:{ all -> 0x009e }
        if (r7 == 0) goto L_0x004e;	 Catch:{ all -> 0x009e }
    L_0x0030:
        r7 = r11.streamLengths;	 Catch:{ all -> 0x009e }
        r7 = r7[r14];	 Catch:{ all -> 0x009e }
        r4 = -1;	 Catch:{ all -> 0x009e }
        if (r7 == r4) goto L_0x0041;	 Catch:{ all -> 0x009e }
    L_0x0037:
        r4 = r7 * 2;	 Catch:{ all -> 0x009e }
        r17 = r12;	 Catch:{ all -> 0x009e }
        r18 = r13;	 Catch:{ all -> 0x009e }
        r12 = (long) r4;	 Catch:{ all -> 0x009e }
        r19 = r15 + r12;	 Catch:{ all -> 0x009e }
        goto L_0x004d;	 Catch:{ all -> 0x009e }
    L_0x0041:
        r17 = r12;	 Catch:{ all -> 0x009e }
        r18 = r13;	 Catch:{ all -> 0x009e }
        r4 = r11.parameterStrings;	 Catch:{ all -> 0x009e }
        r4 = r4[r14];	 Catch:{ all -> 0x009e }
        r4 = r4.length;	 Catch:{ all -> 0x009e }
        r12 = (long) r4;	 Catch:{ all -> 0x009e }
        r19 = r15 + r12;	 Catch:{ all -> 0x009e }
    L_0x004d:
        goto L_0x0063;	 Catch:{ all -> 0x009e }
    L_0x004e:
        r17 = r12;	 Catch:{ all -> 0x009e }
        r18 = r13;	 Catch:{ all -> 0x009e }
        r4 = r11.parameterStrings;	 Catch:{ all -> 0x009e }
        r4 = r4[r14];	 Catch:{ all -> 0x009e }
        r4 = r4.length;	 Catch:{ all -> 0x009e }
        r12 = (long) r4;	 Catch:{ all -> 0x009e }
        r19 = r15 + r12;	 Catch:{ all -> 0x009e }
        goto L_0x0063;	 Catch:{ all -> 0x009e }
    L_0x005b:
        r17 = r12;	 Catch:{ all -> 0x009e }
        r18 = r13;	 Catch:{ all -> 0x009e }
        r12 = 4;	 Catch:{ all -> 0x009e }
        r19 = r15 + r12;	 Catch:{ all -> 0x009e }
    L_0x0063:
        r15 = r19;	 Catch:{ all -> 0x009e }
        r14 = r14 + 1;	 Catch:{ all -> 0x009e }
        r12 = r17;	 Catch:{ all -> 0x009e }
        r13 = r18;	 Catch:{ all -> 0x009e }
        goto L_0x0025;	 Catch:{ all -> 0x009e }
    L_0x006c:
        r17 = r12;	 Catch:{ all -> 0x009e }
        r18 = r13;	 Catch:{ all -> 0x009e }
        r4 = r21.getValuesClause();	 Catch:{ all -> 0x009e }
        if (r4 == 0) goto L_0x0084;	 Catch:{ all -> 0x009e }
    L_0x0076:
        r4 = r21.getValuesClause();	 Catch:{ all -> 0x009e }
        r4 = r4.length();	 Catch:{ all -> 0x009e }
        r7 = 1;	 Catch:{ all -> 0x009e }
        r4 = r4 + r7;	 Catch:{ all -> 0x009e }
        r12 = (long) r4;	 Catch:{ all -> 0x009e }
        r19 = r15 + r12;	 Catch:{ all -> 0x009e }
    L_0x0083:
        goto L_0x0090;	 Catch:{ all -> 0x009e }
    L_0x0084:
        r4 = r1.originalSql;	 Catch:{ all -> 0x009e }
        r4 = r4.length();	 Catch:{ all -> 0x009e }
        r7 = 1;	 Catch:{ all -> 0x009e }
        r4 = r4 + r7;	 Catch:{ all -> 0x009e }
        r12 = (long) r4;	 Catch:{ all -> 0x009e }
        r19 = r15 + r12;	 Catch:{ all -> 0x009e }
        goto L_0x0083;	 Catch:{ all -> 0x009e }
    L_0x0090:
        r12 = r8 + r19;	 Catch:{ all -> 0x009e }
        r4 = (r19 > r5 ? 1 : (r19 == r5 ? 0 : -1));	 Catch:{ all -> 0x009e }
        if (r4 <= 0) goto L_0x0099;	 Catch:{ all -> 0x009e }
    L_0x0096:
        r4 = r19;	 Catch:{ all -> 0x009e }
        r5 = r4;	 Catch:{ all -> 0x009e }
    L_0x0099:
        r3 = r3 + 1;	 Catch:{ all -> 0x009e }
        r8 = r12;	 Catch:{ all -> 0x009e }
        goto L_0x0011;	 Catch:{ all -> 0x009e }
    L_0x009e:
        r0 = move-exception;	 Catch:{ all -> 0x009e }
        r3 = r0;	 Catch:{ all -> 0x009e }
        goto L_0x00ac;	 Catch:{ all -> 0x009e }
    L_0x00a1:
        r3 = 2;	 Catch:{ all -> 0x009e }
        r3 = new long[r3];	 Catch:{ all -> 0x009e }
        r4 = 0;	 Catch:{ all -> 0x009e }
        r3[r4] = r5;	 Catch:{ all -> 0x009e }
        r4 = 1;	 Catch:{ all -> 0x009e }
        r3[r4] = r8;	 Catch:{ all -> 0x009e }
        monitor-exit(r2);	 Catch:{ all -> 0x009e }
        return r3;	 Catch:{ all -> 0x009e }
    L_0x00ac:
        monitor-exit(r2);	 Catch:{ all -> 0x009e }
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.PreparedStatement.computeMaxParameterSetSizeAndBatchSize(int):long[]");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected long[] executeBatchSerially(int r23) throws java.sql.SQLException {
        /*
        r22 = this;
        r8 = r22;
        r9 = r23;
        r1 = r22.checkClosed();
        r10 = r1.getConnectionMutex();
        monitor-enter(r10);
        r1 = r8.connection;	 Catch:{ all -> 0x01f8 }
        r11 = r1;
        if (r11 != 0) goto L_0x0015;
    L_0x0012:
        r22.checkClosed();	 Catch:{ all -> 0x01f8 }
    L_0x0015:
        r1 = 0;
        r2 = r8.batchedArgs;	 Catch:{ all -> 0x01f8 }
        r12 = 0;
        if (r2 == 0) goto L_0x01e9;
    L_0x001b:
        r2 = r8.batchedArgs;	 Catch:{ all -> 0x01f8 }
        r2 = r2.size();	 Catch:{ all -> 0x01f8 }
        r13 = r2;
        r2 = new long[r13];	 Catch:{ all -> 0x01f8 }
        r14 = r2;
        r1 = r12;
    L_0x0026:
        r15 = -3;
        if (r1 >= r13) goto L_0x002f;
    L_0x002a:
        r14[r1] = r15;	 Catch:{ all -> 0x01f8 }
        r1 = r1 + 1;
        goto L_0x0026;
    L_0x002f:
        r1 = 0;
        r2 = 0;
        r3 = r11.getEnableQueryTimeouts();	 Catch:{ NullPointerException -> 0x01a2, all -> 0x019b }
        if (r3 == 0) goto L_0x005b;
    L_0x0037:
        if (r9 == 0) goto L_0x005b;
    L_0x0039:
        r3 = 5;
        r3 = r11.versionMeetsMinimum(r3, r12, r12);	 Catch:{ NullPointerException -> 0x0056, all -> 0x004f }
        if (r3 == 0) goto L_0x005b;
    L_0x0040:
        r3 = new com.mysql.jdbc.StatementImpl$CancelTask;	 Catch:{ NullPointerException -> 0x0056, all -> 0x004f }
        r3.<init>(r8);	 Catch:{ NullPointerException -> 0x0056, all -> 0x004f }
        r2 = r3;
        r3 = r11.getCancelTimer();	 Catch:{ NullPointerException -> 0x0056, all -> 0x004f }
        r4 = (long) r9;	 Catch:{ NullPointerException -> 0x0056, all -> 0x004f }
        r3.schedule(r2, r4);	 Catch:{ NullPointerException -> 0x0056, all -> 0x004f }
        goto L_0x005b;
    L_0x004f:
        r0 = move-exception;
        r12 = r1;
        r17 = r2;
    L_0x0053:
        r15 = -1;
        goto L_0x01a0;
    L_0x0056:
        r0 = move-exception;
        r12 = r1;
    L_0x0058:
        r15 = -1;
        goto L_0x01a5;
    L_0x005b:
        r17 = r2;
        r2 = r8.retrieveGeneratedKeys;	 Catch:{ NullPointerException -> 0x0195, all -> 0x0191 }
        if (r2 == 0) goto L_0x0071;
    L_0x0061:
        r2 = new java.util.ArrayList;	 Catch:{ NullPointerException -> 0x006c, all -> 0x0069 }
        r2.<init>(r13);	 Catch:{ NullPointerException -> 0x006c, all -> 0x0069 }
        r8.batchedGeneratedKeys = r2;	 Catch:{ NullPointerException -> 0x006c, all -> 0x0069 }
        goto L_0x0071;
    L_0x0069:
        r0 = move-exception;
        r12 = r1;
        goto L_0x0053;
    L_0x006c:
        r0 = move-exception;
        r12 = r1;
        r2 = r17;
        goto L_0x0058;
    L_0x0071:
        r8.batchCommandIndex = r12;	 Catch:{ NullPointerException -> 0x0195, all -> 0x0191 }
        r6 = r1;
    L_0x0074:
        r1 = r8.batchCommandIndex;	 Catch:{ NullPointerException -> 0x018a, all -> 0x0185 }
        if (r1 >= r13) goto L_0x0155;
    L_0x0078:
        r1 = r8.batchedArgs;	 Catch:{ NullPointerException -> 0x018a, all -> 0x0185 }
        r2 = r8.batchCommandIndex;	 Catch:{ NullPointerException -> 0x018a, all -> 0x0185 }
        r1 = r1.get(r2);	 Catch:{ NullPointerException -> 0x018a, all -> 0x0185 }
        r5 = r1;
        r4 = 1;
        r1 = r5 instanceof java.lang.String;	 Catch:{ SQLException -> 0x010c }
        if (r1 == 0) goto L_0x00ca;
    L_0x0086:
        r1 = r8.batchCommandIndex;	 Catch:{ SQLException -> 0x00c1, NullPointerException -> 0x00b9, all -> 0x00b3 }
        r2 = r5;
        r2 = (java.lang.String) r2;	 Catch:{ SQLException -> 0x00c1, NullPointerException -> 0x00b9, all -> 0x00b3 }
        r3 = r8.retrieveGeneratedKeys;	 Catch:{ SQLException -> 0x00c1, NullPointerException -> 0x00b9, all -> 0x00b3 }
        r2 = r8.executeUpdateInternal(r2, r4, r3);	 Catch:{ SQLException -> 0x00c1, NullPointerException -> 0x00b9, all -> 0x00b3 }
        r14[r1] = r2;	 Catch:{ SQLException -> 0x00c1, NullPointerException -> 0x00b9, all -> 0x00b3 }
        r1 = r8.results;	 Catch:{ SQLException -> 0x00c1, NullPointerException -> 0x00b9, all -> 0x00b3 }
        r1 = r1.getFirstCharOfQuery();	 Catch:{ SQLException -> 0x00c1, NullPointerException -> 0x00b9, all -> 0x00b3 }
        r2 = 73;
        if (r1 != r2) goto L_0x00a8;
    L_0x009d:
        r1 = r5;
        r1 = (java.lang.String) r1;	 Catch:{ SQLException -> 0x00c1, NullPointerException -> 0x00b9, all -> 0x00b3 }
        r1 = r8.containsOnDuplicateKeyInString(r1);	 Catch:{ SQLException -> 0x00c1, NullPointerException -> 0x00b9, all -> 0x00b3 }
        if (r1 == 0) goto L_0x00a8;
    L_0x00a6:
        r1 = r4;
        goto L_0x00a9;
    L_0x00a8:
        r1 = r12;
    L_0x00a9:
        r8.getBatchedGeneratedKeys(r1);	 Catch:{ SQLException -> 0x00c1, NullPointerException -> 0x00b9, all -> 0x00b3 }
        r18 = r4;
        r21 = r5;
        r12 = r6;
        r15 = -1;
        goto L_0x00fe;
    L_0x00b3:
        r0 = move-exception;
        r1 = r0;
        r12 = r6;
        r15 = -1;
        goto L_0x01cb;
    L_0x00b9:
        r0 = move-exception;
        r1 = r0;
        r12 = r6;
        r2 = r17;
        r15 = -1;
        goto L_0x01a6;
    L_0x00c1:
        r0 = move-exception;
        r1 = r0;
        r18 = r4;
        r21 = r5;
        r12 = r6;
        r15 = -1;
        goto L_0x0114;
    L_0x00ca:
        r1 = r5;
        r1 = (com.mysql.jdbc.PreparedStatement.BatchParams) r1;	 Catch:{ SQLException -> 0x010c }
        r3 = r1;
        r2 = r8.batchCommandIndex;	 Catch:{ SQLException -> 0x010c }
        r1 = r3.parameterStrings;	 Catch:{ SQLException -> 0x010c }
        r4 = r3.parameterStreams;	 Catch:{ SQLException -> 0x0104 }
        r7 = r3.isStream;	 Catch:{ SQLException -> 0x0104 }
        r12 = r3.streamLengths;	 Catch:{ SQLException -> 0x0104 }
        r15 = r3.isNull;	 Catch:{ SQLException -> 0x0104 }
        r16 = 1;
        r19 = r1;
        r1 = r8;
        r20 = r2;
        r2 = r19;
        r19 = r3;
        r3 = r4;
        r18 = 1;
        r4 = r7;
        r21 = r5;
        r5 = r12;
        r12 = r6;
        r6 = r15;
        r15 = -1;
        r7 = r16;
        r1 = r1.executeUpdateInternal(r2, r3, r4, r5, r6, r7);	 Catch:{ SQLException -> 0x0101 }
        r14[r20] = r1;	 Catch:{ SQLException -> 0x0101 }
        r1 = r22.containsOnDuplicateKeyUpdateInSQL();	 Catch:{ SQLException -> 0x0101 }
        r8.getBatchedGeneratedKeys(r1);	 Catch:{ SQLException -> 0x0101 }
        r6 = r12;
        goto L_0x012e;
    L_0x0101:
        r0 = move-exception;
        r1 = r0;
        goto L_0x0114;
    L_0x0104:
        r0 = move-exception;
        r21 = r5;
        r12 = r6;
        r15 = -1;
        r18 = 1;
        goto L_0x0113;
    L_0x010c:
        r0 = move-exception;
        r18 = r4;
        r21 = r5;
        r12 = r6;
        r15 = -1;
    L_0x0113:
        r1 = r0;
    L_0x0114:
        r2 = r8.batchCommandIndex;	 Catch:{ NullPointerException -> 0x0164, all -> 0x0162 }
        r3 = -3;
        r14[r2] = r3;	 Catch:{ NullPointerException -> 0x0164, all -> 0x0162 }
        r2 = r8.continueBatchOnError;	 Catch:{ NullPointerException -> 0x0164, all -> 0x0162 }
        if (r2 == 0) goto L_0x0142;
    L_0x011e:
        r2 = r1 instanceof com.mysql.jdbc.exceptions.MySQLTimeoutException;	 Catch:{ NullPointerException -> 0x0164, all -> 0x0162 }
        if (r2 != 0) goto L_0x0142;
    L_0x0122:
        r2 = r1 instanceof com.mysql.jdbc.exceptions.MySQLStatementCancelledException;	 Catch:{ NullPointerException -> 0x0164, all -> 0x0162 }
        if (r2 != 0) goto L_0x0142;
    L_0x0126:
        r2 = r8.hasDeadlockOrTimeoutRolledBackTx(r1);	 Catch:{ NullPointerException -> 0x0164, all -> 0x0162 }
        if (r2 != 0) goto L_0x0142;
    L_0x012c:
        r2 = r1;
        r6 = r2;
    L_0x012e:
        r1 = r8.batchCommandIndex;	 Catch:{ NullPointerException -> 0x013e, all -> 0x0139 }
        r1 = r1 + 1;
        r8.batchCommandIndex = r1;	 Catch:{ NullPointerException -> 0x013e, all -> 0x0139 }
        r12 = 0;
        r15 = -3;
        goto L_0x0074;
    L_0x0139:
        r0 = move-exception;
        r1 = r0;
        r12 = r6;
        goto L_0x01cb;
    L_0x013e:
        r0 = move-exception;
        r1 = r0;
        r12 = r6;
        goto L_0x0166;
    L_0x0142:
        r2 = r8.batchCommandIndex;	 Catch:{ NullPointerException -> 0x0164, all -> 0x0162 }
        r2 = new long[r2];	 Catch:{ NullPointerException -> 0x0164, all -> 0x0162 }
        r3 = r8.batchCommandIndex;	 Catch:{ NullPointerException -> 0x0164, all -> 0x0162 }
        r4 = 0;
        java.lang.System.arraycopy(r14, r4, r2, r4, r3);	 Catch:{ NullPointerException -> 0x0164, all -> 0x0162 }
        r3 = r22.getExceptionInterceptor();	 Catch:{ NullPointerException -> 0x0164, all -> 0x0162 }
        r3 = com.mysql.jdbc.SQLError.createBatchUpdateException(r1, r2, r3);	 Catch:{ NullPointerException -> 0x0164, all -> 0x0162 }
        throw r3;	 Catch:{ NullPointerException -> 0x0164, all -> 0x0162 }
    L_0x0155:
        r12 = r6;
        r15 = -1;
        if (r12 == 0) goto L_0x0169;
    L_0x0159:
        r1 = r22.getExceptionInterceptor();	 Catch:{ NullPointerException -> 0x0164, all -> 0x0162 }
        r1 = com.mysql.jdbc.SQLError.createBatchUpdateException(r12, r14, r1);	 Catch:{ NullPointerException -> 0x0164, all -> 0x0162 }
        throw r1;	 Catch:{ NullPointerException -> 0x0164, all -> 0x0162 }
    L_0x0162:
        r0 = move-exception;
        goto L_0x01a0;
    L_0x0164:
        r0 = move-exception;
        r1 = r0;
    L_0x0166:
        r2 = r17;
        goto L_0x01a6;
    L_0x0169:
        r1 = r13;
        r2 = r12;
        r3 = r17;
        r4 = r14;
        r5 = r8;
        r6 = r9;
        r5.batchCommandIndex = r15;	 Catch:{ all -> 0x01f4 }
        if (r3 == 0) goto L_0x017e;
    L_0x0174:
        r3.cancel();	 Catch:{ all -> 0x01f4 }
        r7 = r11.getCancelTimer();	 Catch:{ all -> 0x01f4 }
        r7.purge();	 Catch:{ all -> 0x01f4 }
    L_0x017e:
        r5.resetCancelledState();	 Catch:{ all -> 0x01f4 }
        r1 = r4;
        goto L_0x01eb;
    L_0x0185:
        r0 = move-exception;
        r12 = r6;
        r15 = -1;
        r1 = r0;
        goto L_0x01cb;
    L_0x018a:
        r0 = move-exception;
        r12 = r6;
        r15 = -1;
        r1 = r0;
        r2 = r17;
        goto L_0x01a6;
    L_0x0191:
        r0 = move-exception;
        r15 = -1;
        r12 = r1;
        goto L_0x01a0;
    L_0x0195:
        r0 = move-exception;
        r15 = -1;
        r12 = r1;
        r2 = r17;
        goto L_0x01a5;
    L_0x019b:
        r0 = move-exception;
        r15 = -1;
        r12 = r1;
        r17 = r2;
    L_0x01a0:
        r1 = r0;
        goto L_0x01cb;
    L_0x01a2:
        r0 = move-exception;
        r15 = -1;
        r12 = r1;
    L_0x01a5:
        r1 = r0;
    L_0x01a6:
        r22.checkClosed();	 Catch:{ SQLException -> 0x01b0 }
        throw r1;	 Catch:{ all -> 0x01ab }
    L_0x01ab:
        r0 = move-exception;
        r1 = r0;
        r17 = r2;
        goto L_0x01cb;
    L_0x01b0:
        r0 = move-exception;
        r3 = r0;
        r4 = r8.batchCommandIndex;	 Catch:{ all -> 0x01ab }
        r5 = -3;
        r14[r4] = r5;	 Catch:{ all -> 0x01ab }
        r4 = r8.batchCommandIndex;	 Catch:{ all -> 0x01ab }
        r4 = new long[r4];	 Catch:{ all -> 0x01ab }
        r5 = r8.batchCommandIndex;	 Catch:{ all -> 0x01ab }
        r6 = 0;
        java.lang.System.arraycopy(r14, r6, r4, r6, r5);	 Catch:{ all -> 0x01ab }
        r5 = r22.getExceptionInterceptor();	 Catch:{ all -> 0x01ab }
        r5 = com.mysql.jdbc.SQLError.createBatchUpdateException(r3, r4, r5);	 Catch:{ all -> 0x01ab }
        throw r5;	 Catch:{ all -> 0x01ab }
        r2 = r13;
        r3 = r12;
        r4 = r17;
        r5 = r11;
        r6 = r14;
        r7 = r8;
        r7.batchCommandIndex = r15;	 Catch:{ all -> 0x01e5 }
        if (r4 == 0) goto L_0x01e1;
    L_0x01d7:
        r4.cancel();	 Catch:{ all -> 0x01e5 }
        r11 = r5.getCancelTimer();	 Catch:{ all -> 0x01e5 }
        r11.purge();	 Catch:{ all -> 0x01e5 }
    L_0x01e1:
        r7.resetCancelledState();	 Catch:{ all -> 0x01e5 }
        throw r1;	 Catch:{ all -> 0x01e5 }
    L_0x01e5:
        r0 = move-exception;
        r1 = r0;
        r5 = r7;
        goto L_0x01fb;
    L_0x01e9:
        r5 = r8;
        r6 = r9;
    L_0x01eb:
        if (r1 == 0) goto L_0x01ef;
    L_0x01ed:
        r2 = r1;
        goto L_0x01f2;
    L_0x01ef:
        r2 = 0;
        r2 = new long[r2];	 Catch:{ all -> 0x01f4 }
    L_0x01f2:
        monitor-exit(r10);	 Catch:{ all -> 0x01f4 }
        return r2;
    L_0x01f4:
        r0 = move-exception;
        r1 = r0;
        r9 = r6;
        goto L_0x01fb;
    L_0x01f8:
        r0 = move-exception;
        r1 = r0;
        r5 = r8;
    L_0x01fb:
        monitor-exit(r10);	 Catch:{ all -> 0x01fd }
        throw r1;
    L_0x01fd:
        r0 = move-exception;
        r1 = r0;
        goto L_0x01fb;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.PreparedStatement.executeBatchSerially(int):long[]");
    }

    public String getDateTime(String pattern) {
        return new SimpleDateFormat(pattern).format(new java.util.Date());
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected com.mysql.jdbc.ResultSetInternalMethods executeInternal(int r20, com.mysql.jdbc.Buffer r21, boolean r22, boolean r23, com.mysql.jdbc.Field[] r24, boolean r25) throws java.sql.SQLException {
        /*
        r19 = this;
        r12 = r19;
        r1 = r19.checkClosed();
        r14 = r1.getConnectionMutex();
        monitor-enter(r14);
        r1 = r12.connection;	 Catch:{ NullPointerException -> 0x0112 }
        r15 = r1;
        r1 = r12.numberOfExecutions;	 Catch:{ NullPointerException -> 0x0112 }
        r1 = r1 + 1;
        r12.numberOfExecutions = r1;	 Catch:{ NullPointerException -> 0x0112 }
        r16 = 0;
        r1 = r16;
        r11 = 0;
        r2 = r15.getEnableQueryTimeouts();	 Catch:{ all -> 0x00d5 }
        if (r2 == 0) goto L_0x0040;
    L_0x001f:
        r2 = r12.timeoutInMillis;	 Catch:{ all -> 0x003b }
        if (r2 == 0) goto L_0x0040;
    L_0x0023:
        r2 = 5;
        r2 = r15.versionMeetsMinimum(r2, r11, r11);	 Catch:{ all -> 0x003b }
        if (r2 == 0) goto L_0x0040;
    L_0x002a:
        r2 = new com.mysql.jdbc.StatementImpl$CancelTask;	 Catch:{ all -> 0x003b }
        r2.<init>(r12);	 Catch:{ all -> 0x003b }
        r1 = r2;
        r2 = r15.getCancelTimer();	 Catch:{ all -> 0x003b }
        r3 = r12.timeoutInMillis;	 Catch:{ all -> 0x003b }
        r3 = (long) r3;	 Catch:{ all -> 0x003b }
        r2.schedule(r1, r3);	 Catch:{ all -> 0x003b }
        goto L_0x0040;
    L_0x003b:
        r0 = move-exception;
        r10 = r1;
        r3 = r11;
        goto L_0x00d8;
    L_0x0040:
        r10 = r1;
        if (r25 != 0) goto L_0x004c;
    L_0x0043:
        r19.statementBegins();	 Catch:{ all -> 0x0047 }
        goto L_0x004c;
    L_0x0047:
        r0 = move-exception;
        r1 = r0;
        r3 = r11;
        goto L_0x00d9;
    L_0x004c:
        r3 = 0;
        r6 = r12.resultSetType;	 Catch:{ all -> 0x00d0 }
        r7 = r12.resultSetConcurrency;	 Catch:{ all -> 0x00d0 }
        r9 = r12.currentCatalog;	 Catch:{ all -> 0x00d0 }
        r1 = r15;
        r2 = r12;
        r4 = r20;
        r5 = r21;
        r8 = r22;
        r17 = r10;
        r10 = r24;
        r11 = r25;
        r1 = r1.execSQL(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11);	 Catch:{ all -> 0x00c9 }
        r16 = r1;
        r1 = r17;
        if (r1 == 0) goto L_0x0083;
    L_0x006b:
        r1.cancel();	 Catch:{ all -> 0x007e }
        r2 = r15.getCancelTimer();	 Catch:{ all -> 0x007e }
        r2.purge();	 Catch:{ all -> 0x007e }
        r2 = r1.caughtWhileCancelling;	 Catch:{ all -> 0x007e }
        if (r2 == 0) goto L_0x007c;
    L_0x0079:
        r2 = r1.caughtWhileCancelling;	 Catch:{ all -> 0x007e }
        throw r2;	 Catch:{ all -> 0x007e }
    L_0x007c:
        r1 = 0;
        goto L_0x0083;
    L_0x007e:
        r0 = move-exception;
        r10 = r1;
        r3 = 0;
        goto L_0x00d8;
    L_0x0083:
        r2 = r12.cancelTimeoutMutex;	 Catch:{ all -> 0x00c6 }
        monitor-enter(r2);	 Catch:{ all -> 0x00c6 }
        r3 = r12.wasCancelled;	 Catch:{ all -> 0x00bd }
        if (r3 == 0) goto L_0x00a4;
    L_0x008a:
        r3 = 0;
        r4 = r12.wasCancelledByTimeout;	 Catch:{ all -> 0x00a0 }
        if (r4 == 0) goto L_0x0096;
    L_0x008f:
        r4 = new com.mysql.jdbc.exceptions.MySQLTimeoutException;	 Catch:{ all -> 0x00a0 }
        r4.<init>();	 Catch:{ all -> 0x00a0 }
        r3 = r4;
        goto L_0x009c;
    L_0x0096:
        r4 = new com.mysql.jdbc.exceptions.MySQLStatementCancelledException;	 Catch:{ all -> 0x00a0 }
        r4.<init>();	 Catch:{ all -> 0x00a0 }
        r3 = r4;
    L_0x009c:
        r19.resetCancelledState();	 Catch:{ all -> 0x00a0 }
        throw r3;	 Catch:{ all -> 0x00a0 }
    L_0x00a0:
        r0 = move-exception;
        r4 = r0;
        r3 = 0;
        goto L_0x00c0;
    L_0x00a4:
        monitor-exit(r2);	 Catch:{ all -> 0x00bd }
        if (r25 != 0) goto L_0x00ae;
    L_0x00a8:
        r2 = r12.statementExecuting;	 Catch:{ NullPointerException -> 0x0112 }
        r3 = 0;
        r2.set(r3);	 Catch:{ NullPointerException -> 0x0112 }
    L_0x00ae:
        if (r1 == 0) goto L_0x00ba;
    L_0x00b0:
        r1.cancel();	 Catch:{ NullPointerException -> 0x0112 }
        r2 = r15.getCancelTimer();	 Catch:{ NullPointerException -> 0x0112 }
        r2.purge();	 Catch:{ NullPointerException -> 0x0112 }
        monitor-exit(r14);	 Catch:{ all -> 0x0102 }
        return r16;
    L_0x00bd:
        r0 = move-exception;
        r3 = 0;
    L_0x00bf:
        r4 = r0;
    L_0x00c0:
        monitor-exit(r2);	 Catch:{ all -> 0x00c4 }
        throw r4;	 Catch:{ all -> 0x00c2 }
    L_0x00c2:
        r0 = move-exception;
        goto L_0x00d7;
    L_0x00c4:
        r0 = move-exception;
        goto L_0x00bf;
    L_0x00c6:
        r0 = move-exception;
        r3 = 0;
        goto L_0x00d7;
    L_0x00c9:
        r0 = move-exception;
        r1 = r17;
        r3 = 0;
        r10 = r1;
        r1 = r0;
        goto L_0x00d9;
    L_0x00d0:
        r0 = move-exception;
        r1 = r10;
        r3 = r11;
        r1 = r0;
        goto L_0x00d9;
    L_0x00d5:
        r0 = move-exception;
        r3 = r11;
    L_0x00d7:
        r10 = r1;
    L_0x00d8:
        r1 = r0;
    L_0x00d9:
        r2 = r15;
        r4 = r16;
        r5 = r10;
        r6 = r12;
        r7 = r20;
        r8 = r21;
        r9 = r22;
        r10 = r23;
        r11 = r24;
        r13 = r25;
        if (r13 != 0) goto L_0x00f5;
    L_0x00ec:
        r15 = r6.statementExecuting;	 Catch:{ NullPointerException -> 0x00f2 }
        r15.set(r3);	 Catch:{ NullPointerException -> 0x00f2 }
        goto L_0x00f5;
    L_0x00f2:
        r0 = move-exception;
        r1 = r0;
        goto L_0x0121;
    L_0x00f5:
        if (r5 == 0) goto L_0x0101;
    L_0x00f7:
        r5.cancel();	 Catch:{ NullPointerException -> 0x00f2 }
        r3 = r2.getCancelTimer();	 Catch:{ NullPointerException -> 0x00f2 }
        r3.purge();	 Catch:{ NullPointerException -> 0x00f2 }
    L_0x0101:
        throw r1;	 Catch:{ NullPointerException -> 0x00f2 }
    L_0x0102:
        r0 = move-exception;
        r7 = r20;
        r8 = r21;
        r9 = r22;
        r10 = r23;
        r11 = r24;
        r13 = r25;
        r1 = r0;
        r6 = r12;
        goto L_0x0127;
    L_0x0112:
        r0 = move-exception;
        r7 = r20;
        r8 = r21;
        r9 = r22;
        r10 = r23;
        r11 = r24;
        r13 = r25;
        r1 = r0;
        r6 = r12;
    L_0x0121:
        r6.checkClosed();	 Catch:{ all -> 0x0125 }
        throw r1;	 Catch:{ all -> 0x0125 }
    L_0x0125:
        r0 = move-exception;
        r1 = r0;
    L_0x0127:
        monitor-exit(r14);	 Catch:{ all -> 0x0125 }
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.PreparedStatement.executeInternal(int, com.mysql.jdbc.Buffer, boolean, boolean, com.mysql.jdbc.Field[], boolean):com.mysql.jdbc.ResultSetInternalMethods");
    }

    public ResultSet executeQuery() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            MySQLConnection locallyScopedConn = this.connection;
            checkForDml(this.originalSql, this.firstCharOfStmt);
            this.batchedGeneratedKeys = null;
            resetCancelledState();
            implicitlyCloseAllOpenResults();
            clearWarnings();
            if (this.doPingInstead) {
                doPingInstead();
                ResultSet resultSet = this.results;
                return resultSet;
            }
            setupStreamingTimeout(locallyScopedConn);
            Buffer sendPacket = fillSendPacket();
            String oldCatalog = null;
            if (!locallyScopedConn.getCatalog().equals(this.currentCatalog)) {
                oldCatalog = locallyScopedConn.getCatalog();
                locallyScopedConn.setCatalog(this.currentCatalog);
            }
            String oldCatalog2 = oldCatalog;
            CachedResultSetMetaData cachedMetadata = null;
            if (locallyScopedConn.getCacheResultSetMetadata()) {
                cachedMetadata = locallyScopedConn.getCachedMetaData(this.originalSql);
            }
            CachedResultSetMetaData cachedMetadata2 = cachedMetadata;
            Field[] metadataFromCache = null;
            if (cachedMetadata2 != null) {
                metadataFromCache = cachedMetadata2.fields;
            }
            Field[] metadataFromCache2 = metadataFromCache;
            locallyScopedConn.setSessionMaxRows(this.maxRows);
            this.results = executeInternal(this.maxRows, sendPacket, createStreamingResultSet(), true, metadataFromCache2, false);
            if (oldCatalog2 != null) {
                locallyScopedConn.setCatalog(oldCatalog2);
            }
            if (cachedMetadata2 != null) {
                locallyScopedConn.initializeResultsMetadataFromCache(this.originalSql, cachedMetadata2, this.results);
            } else if (locallyScopedConn.getCacheResultSetMetadata()) {
                locallyScopedConn.initializeResultsMetadataFromCache(this.originalSql, null, this.results);
            }
            this.lastInsertId = this.results.getUpdateID();
            resultSet = this.results;
            return resultSet;
        }
    }

    public int executeUpdate() throws SQLException {
        return Util.truncateAndConvertToInt(executeLargeUpdate());
    }

    protected long executeUpdateInternal(boolean clearBatchedGeneratedKeysAndWarnings, boolean isBatch) throws SQLException {
        long executeUpdateInternal;
        synchronized (checkClosed().getConnectionMutex()) {
            if (clearBatchedGeneratedKeysAndWarnings) {
                clearWarnings();
                this.batchedGeneratedKeys = null;
            }
            executeUpdateInternal = executeUpdateInternal(this.parameterValues, this.parameterStreams, this.isStream, this.streamLengths, this.isNull, isBatch);
        }
        return executeUpdateInternal;
    }

    protected long executeUpdateInternal(byte[][] batchedParameterStrings, InputStream[] batchedParameterStreams, boolean[] batchedIsStream, int[] batchedStreamLengths, boolean[] batchedIsNull, boolean isReallyBatch) throws SQLException {
        long j;
        synchronized (checkClosed().getConnectionMutex()) {
            try {
                MySQLConnection locallyScopedConn = r8.connection;
                if (locallyScopedConn.isReadOnly(false)) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(Messages.getString("PreparedStatement.34"));
                    stringBuilder.append(Messages.getString("PreparedStatement.35"));
                    throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                } else if (r8.firstCharOfStmt == 'S' && isSelectQuery()) {
                    throw SQLError.createSQLException(Messages.getString("PreparedStatement.37"), SQLError.SQL_STATE_NO_ROWS_UPDATED_OR_DELETED, getExceptionInterceptor());
                } else {
                    resetCancelledState();
                    implicitlyCloseAllOpenResults();
                    Buffer sendPacket = fillSendPacket(batchedParameterStrings, batchedParameterStreams, batchedIsStream, batchedStreamLengths);
                    String oldCatalog = null;
                    if (!locallyScopedConn.getCatalog().equals(r8.currentCatalog)) {
                        String catalog = locallyScopedConn.getCatalog();
                    }
                    String oldCatalog2 = oldCatalog;
                    locallyScopedConn.setSessionMaxRows(-1);
                    boolean oldInfoMsgState = false;
                    if (r8.retrieveGeneratedKeys) {
                        oldInfoMsgState = locallyScopedConn.isReadInfoMsgEnabled();
                        locallyScopedConn.setReadInfoMsgEnabled(true);
                    }
                    boolean oldInfoMsgState2 = oldInfoMsgState;
                    ResultSetInternalMethods rs = executeInternal(-1, sendPacket, false, false, null, isReallyBatch);
                    if (r8.retrieveGeneratedKeys) {
                        locallyScopedConn.setReadInfoMsgEnabled(oldInfoMsgState2);
                        rs.setFirstCharOfQuery(r8.firstCharOfStmt);
                    }
                    if (oldCatalog2 != null) {
                        locallyScopedConn.setCatalog(oldCatalog2);
                    }
                    r8.results = rs;
                    r8.updateCount = rs.getUpdateCount();
                    if (containsOnDuplicateKeyUpdateInSQL() && r8.compensateForOnDuplicateKeyUpdate && (r8.updateCount == 2 || r8.updateCount == 0)) {
                        r8.updateCount = 1;
                    }
                    r8.lastInsertId = rs.getUpdateID();
                    j = r8.updateCount;
                }
            } finally {
                oldCatalog = r0;
            }
        }
        return j;
    }

    protected boolean containsOnDuplicateKeyUpdateInSQL() {
        return this.parseInfo.isOnDuplicateKeyUpdate;
    }

    protected Buffer fillSendPacket() throws SQLException {
        Buffer fillSendPacket;
        synchronized (checkClosed().getConnectionMutex()) {
            fillSendPacket = fillSendPacket(this.parameterValues, this.parameterStreams, this.isStream, this.streamLengths);
        }
        return fillSendPacket;
    }

    protected Buffer fillSendPacket(byte[][] batchedParameterStrings, InputStream[] batchedParameterStreams, boolean[] batchedIsStream, int[] batchedStreamLengths) throws SQLException {
        Buffer sendPacket;
        PreparedStatement preparedStatement = this;
        byte[][] bArr = batchedParameterStrings;
        synchronized (checkClosed().getConnectionMutex()) {
            int ensurePacketSize;
            try {
                sendPacket = preparedStatement.connection.getIO().getSharedSendPacket();
                sendPacket.clear();
                sendPacket.writeByte((byte) 3);
                boolean useStreamLengths = preparedStatement.connection.getUseStreamLengthsInPrepStmts();
                ensurePacketSize = 0;
                String statementComment = preparedStatement.connection.getStatementComment();
                byte[] commentAsBytes = null;
                if (statementComment != null) {
                    if (preparedStatement.charConverter != null) {
                        byte[] toBytes = preparedStatement.charConverter.toBytes(statementComment);
                    } else {
                        SingleByteCharsetConverter singleByteCharsetConverter = preparedStatement.charConverter;
                        commentAsBytes = StringUtils.getBytes(statementComment, singleByteCharsetConverter, preparedStatement.charEncoding, preparedStatement.connection.getServerCharset(), preparedStatement.connection.parserKnowsUnicode(), getExceptionInterceptor());
                    }
                    ensurePacketSize = (ensurePacketSize + commentAsBytes.length) + 6;
                }
                byte[] commentAsBytes2 = commentAsBytes;
                int i = 0;
                int ensurePacketSize2 = ensurePacketSize;
                for (ensurePacketSize = 0; ensurePacketSize < bArr.length; ensurePacketSize++) {
                    if (batchedIsStream[ensurePacketSize] && useStreamLengths) {
                        ensurePacketSize2 += batchedStreamLengths[ensurePacketSize];
                    }
                }
                if (ensurePacketSize2 != 0) {
                    sendPacket.ensureCapacity(ensurePacketSize2);
                }
                if (commentAsBytes2 != null) {
                    sendPacket.writeBytesNoNull(Constants.SLASH_STAR_SPACE_AS_BYTES);
                    sendPacket.writeBytesNoNull(commentAsBytes2);
                    sendPacket.writeBytesNoNull(Constants.SPACE_STAR_SLASH_SPACE_AS_BYTES);
                }
                while (true) {
                    int i2 = i;
                    String statementComment2;
                    if (i2 < bArr.length) {
                        int i3;
                        int ensurePacketSize3;
                        checkAllParametersSet(bArr[i2], batchedParameterStreams[i2], i2);
                        sendPacket.writeBytesNoNull(preparedStatement.staticSqlStrings[i2]);
                        if (batchedIsStream[i2]) {
                            InputStream inputStream = batchedParameterStreams[i2];
                            i3 = i2;
                            ensurePacketSize3 = ensurePacketSize2;
                            ensurePacketSize2 = batchedStreamLengths[i2];
                            statementComment2 = statementComment;
                            streamToBytes(sendPacket, inputStream, true, ensurePacketSize2, useStreamLengths);
                        } else {
                            i3 = i2;
                            ensurePacketSize3 = ensurePacketSize2;
                            statementComment2 = statementComment;
                            sendPacket.writeBytesNoNull(bArr[i3]);
                        }
                        i = i3 + 1;
                        ensurePacketSize2 = ensurePacketSize3;
                        statementComment = statementComment2;
                    } else {
                        statementComment2 = statementComment;
                        sendPacket.writeBytesNoNull(preparedStatement.staticSqlStrings[bArr.length]);
                    }
                }
            } finally {
                ensurePacketSize = r0;
            }
        }
        return sendPacket;
    }

    private void checkAllParametersSet(byte[] parameterString, InputStream parameterStream, int columnIndex) throws SQLException {
        if (parameterString == null && parameterStream == null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Messages.getString("PreparedStatement.40"));
            stringBuilder.append(columnIndex + 1);
            throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_WRONG_NO_OF_PARAMETERS, getExceptionInterceptor());
        }
    }

    protected PreparedStatement prepareBatchedInsertSQL(MySQLConnection localConn, int numBatches) throws SQLException {
        PreparedStatement pstmt;
        synchronized (checkClosed().getConnectionMutex()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Rewritten batch of: ");
            stringBuilder.append(this.originalSql);
            pstmt = new PreparedStatement(localConn, stringBuilder.toString(), this.currentCatalog, this.parseInfo.getParseInfoForBatch(numBatches));
            pstmt.setRetrieveGeneratedKeys(this.retrieveGeneratedKeys);
            pstmt.rewrittenBatchSize = numBatches;
        }
        return pstmt;
    }

    protected void setRetrieveGeneratedKeys(boolean flag) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            this.retrieveGeneratedKeys = flag;
        }
    }

    public int getRewrittenBatchSize() {
        return this.rewrittenBatchSize;
    }

    public String getNonRewrittenSql() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            int indexOfBatch = this.originalSql.indexOf(" of: ");
            if (indexOfBatch != -1) {
                String substring = this.originalSql.substring(indexOfBatch + 5);
                return substring;
            }
            substring = this.originalSql;
            return substring;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public byte[] getBytesRepresentation(int r7) throws java.sql.SQLException {
        /*
        r6 = this;
        r0 = r6.checkClosed();
        r0 = r0.getConnectionMutex();
        monitor-enter(r0);
        r1 = r6.isStream;	 Catch:{ all -> 0x0049 }
        r1 = r1[r7];	 Catch:{ all -> 0x0049 }
        r2 = 0;
        if (r1 == 0) goto L_0x0024;
    L_0x0010:
        r1 = r6.parameterStreams;	 Catch:{ all -> 0x0049 }
        r1 = r1[r7];	 Catch:{ all -> 0x0049 }
        r3 = r6.streamLengths;	 Catch:{ all -> 0x0049 }
        r3 = r3[r7];	 Catch:{ all -> 0x0049 }
        r4 = r6.connection;	 Catch:{ all -> 0x0049 }
        r4 = r4.getUseStreamLengthsInPrepStmts();	 Catch:{ all -> 0x0049 }
        r1 = r6.streamToBytes(r1, r2, r3, r4);	 Catch:{ all -> 0x0049 }
        monitor-exit(r0);	 Catch:{ all -> 0x0049 }
        return r1;
    L_0x0024:
        r1 = r6.parameterValues;	 Catch:{ all -> 0x0049 }
        r1 = r1[r7];	 Catch:{ all -> 0x0049 }
        if (r1 != 0) goto L_0x002d;
    L_0x002a:
        r2 = 0;
        monitor-exit(r0);	 Catch:{ all -> 0x0049 }
        return r2;
    L_0x002d:
        r3 = r1[r2];	 Catch:{ all -> 0x0049 }
        r4 = 39;
        if (r3 != r4) goto L_0x0047;
    L_0x0033:
        r3 = r1.length;	 Catch:{ all -> 0x0049 }
        r5 = 1;
        r3 = r3 - r5;
        r3 = r1[r3];	 Catch:{ all -> 0x0049 }
        if (r3 != r4) goto L_0x0047;
    L_0x003a:
        r3 = r1.length;	 Catch:{ all -> 0x0049 }
        r3 = r3 + -2;
        r3 = new byte[r3];	 Catch:{ all -> 0x0049 }
        r4 = r1.length;	 Catch:{ all -> 0x0049 }
        r4 = r4 + -2;
        java.lang.System.arraycopy(r1, r5, r3, r2, r4);	 Catch:{ all -> 0x0049 }
        monitor-exit(r0);	 Catch:{ all -> 0x0049 }
        return r3;
    L_0x0047:
        monitor-exit(r0);	 Catch:{ all -> 0x0049 }
        return r1;
    L_0x0049:
        r1 = move-exception;
        monitor-exit(r0);	 Catch:{ all -> 0x0049 }
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.PreparedStatement.getBytesRepresentation(int):byte[]");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected byte[] getBytesRepresentationForBatch(int r9, int r10) throws java.sql.SQLException {
        /*
        r8 = this;
        r0 = r8.checkClosed();
        r0 = r0.getConnectionMutex();
        monitor-enter(r0);
        r1 = r8.batchedArgs;	 Catch:{ all -> 0x0088 }
        r1 = r1.get(r10);	 Catch:{ all -> 0x0088 }
        r2 = r1 instanceof java.lang.String;	 Catch:{ all -> 0x0088 }
        if (r2 == 0) goto L_0x0045;
    L_0x0013:
        r2 = r1;
        r2 = (java.lang.String) r2;	 Catch:{ UnsupportedEncodingException -> 0x001e }
        r3 = r8.charEncoding;	 Catch:{ UnsupportedEncodingException -> 0x001e }
        r2 = com.mysql.jdbc.StringUtils.getBytes(r2, r3);	 Catch:{ UnsupportedEncodingException -> 0x001e }
        monitor-exit(r0);	 Catch:{ all -> 0x0088 }
        return r2;
    L_0x001e:
        r2 = move-exception;
        r3 = new java.lang.RuntimeException;	 Catch:{ all -> 0x0088 }
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0088 }
        r4.<init>();	 Catch:{ all -> 0x0088 }
        r5 = "PreparedStatement.32";
        r5 = com.mysql.jdbc.Messages.getString(r5);	 Catch:{ all -> 0x0088 }
        r4.append(r5);	 Catch:{ all -> 0x0088 }
        r5 = r8.charEncoding;	 Catch:{ all -> 0x0088 }
        r4.append(r5);	 Catch:{ all -> 0x0088 }
        r5 = "PreparedStatement.33";
        r5 = com.mysql.jdbc.Messages.getString(r5);	 Catch:{ all -> 0x0088 }
        r4.append(r5);	 Catch:{ all -> 0x0088 }
        r4 = r4.toString();	 Catch:{ all -> 0x0088 }
        r3.<init>(r4);	 Catch:{ all -> 0x0088 }
        throw r3;	 Catch:{ all -> 0x0088 }
    L_0x0045:
        r2 = r1;
        r2 = (com.mysql.jdbc.PreparedStatement.BatchParams) r2;	 Catch:{ all -> 0x0088 }
        r3 = r2.isStream;	 Catch:{ all -> 0x0088 }
        r3 = r3[r9];	 Catch:{ all -> 0x0088 }
        r4 = 0;
        if (r3 == 0) goto L_0x0063;
    L_0x004f:
        r3 = r2.parameterStreams;	 Catch:{ all -> 0x0088 }
        r3 = r3[r9];	 Catch:{ all -> 0x0088 }
        r5 = r2.streamLengths;	 Catch:{ all -> 0x0088 }
        r5 = r5[r9];	 Catch:{ all -> 0x0088 }
        r6 = r8.connection;	 Catch:{ all -> 0x0088 }
        r6 = r6.getUseStreamLengthsInPrepStmts();	 Catch:{ all -> 0x0088 }
        r3 = r8.streamToBytes(r3, r4, r5, r6);	 Catch:{ all -> 0x0088 }
        monitor-exit(r0);	 Catch:{ all -> 0x0088 }
        return r3;
    L_0x0063:
        r3 = r2.parameterStrings;	 Catch:{ all -> 0x0088 }
        r3 = r3[r9];	 Catch:{ all -> 0x0088 }
        if (r3 != 0) goto L_0x006c;
    L_0x0069:
        r4 = 0;
        monitor-exit(r0);	 Catch:{ all -> 0x0088 }
        return r4;
    L_0x006c:
        r5 = r3[r4];	 Catch:{ all -> 0x0088 }
        r6 = 39;
        if (r5 != r6) goto L_0x0086;
    L_0x0072:
        r5 = r3.length;	 Catch:{ all -> 0x0088 }
        r7 = 1;
        r5 = r5 - r7;
        r5 = r3[r5];	 Catch:{ all -> 0x0088 }
        if (r5 != r6) goto L_0x0086;
    L_0x0079:
        r5 = r3.length;	 Catch:{ all -> 0x0088 }
        r5 = r5 + -2;
        r5 = new byte[r5];	 Catch:{ all -> 0x0088 }
        r6 = r3.length;	 Catch:{ all -> 0x0088 }
        r6 = r6 + -2;
        java.lang.System.arraycopy(r3, r7, r5, r4, r6);	 Catch:{ all -> 0x0088 }
        monitor-exit(r0);	 Catch:{ all -> 0x0088 }
        return r5;
    L_0x0086:
        monitor-exit(r0);	 Catch:{ all -> 0x0088 }
        return r3;
    L_0x0088:
        r1 = move-exception;
        monitor-exit(r0);	 Catch:{ all -> 0x0088 }
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.PreparedStatement.getBytesRepresentationForBatch(int, int):byte[]");
    }

    private final String getDateTimePattern(String dt, boolean toTime) throws Exception {
        PreparedStatement preparedStatement = this;
        String str = dt;
        int i = 0;
        int dtLength = str != null ? dt.length() : 0;
        int i2 = 2;
        if (dtLength >= 8 && dtLength <= 10) {
            boolean isDateOnly = true;
            int dashCount = 0;
            for (int i3 = 0; i3 < dtLength; i3++) {
                char c = str.charAt(i3);
                if (!Character.isDigit(c) && c != '-') {
                    isDateOnly = false;
                    break;
                }
                if (c == '-') {
                    dashCount++;
                }
            }
            if (isDateOnly && dashCount == 2) {
                return "yyyy-MM-dd";
            }
        }
        boolean colonsOnly = true;
        for (int i4 = 0; i4 < dtLength; i4++) {
            char c2 = str.charAt(i4);
            if (!Character.isDigit(c2) && c2 != ':') {
                colonsOnly = false;
                break;
            }
        }
        if (colonsOnly) {
            return "HH:mm:ss";
        }
        int i5;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(str);
        stringBuilder.append(" ");
        StringReader reader = new StringReader(stringBuilder.toString());
        ArrayList<Object[]> vec = new ArrayList();
        ArrayList<Object[]> vecRemovelist = new ArrayList();
        Object[] nv = new Object[]{Character.valueOf('y'), new StringBuilder(), Integer.valueOf(0)};
        vec.add(nv);
        if (toTime) {
            nv = new Object[]{Character.valueOf('h'), new StringBuilder(), Integer.valueOf(0)};
            vec.add(nv);
        }
        while (true) {
            int read = reader.read();
            int z = read;
            if (read == -1) {
                break;
            }
            int dtLength2;
            char separator = (char) z;
            read = vec.size();
            Object[] nv2 = nv;
            int count = i;
            while (count < read) {
                Object[] v = (Object[]) vec.get(count);
                i2 = ((Integer) v[i2]).intValue();
                char c3 = getSuccessor(((Character) v[i]).charValue(), i2);
                if (Character.isLetterOrDigit(separator)) {
                    if (c3 == 'X') {
                        c3 = 'y';
                        Object[] nv3 = new Object[3];
                        dtLength2 = dtLength;
                        StringBuilder stringBuilder2 = new StringBuilder(((StringBuilder) v[1]).toString());
                        stringBuilder2.append(77);
                        nv3[1] = stringBuilder2;
                        nv3[0] = Character.valueOf('M');
                        nv3[2] = Integer.valueOf(1);
                        vec.add(nv3);
                        nv2 = nv3;
                    } else {
                        dtLength2 = dtLength;
                        if (c3 == 'Y') {
                            c3 = 'M';
                            Object[] nv4 = new Object[3];
                            dtLength = new StringBuilder(((StringBuilder) v[1]).toString());
                            dtLength.append('d');
                            i5 = 1;
                            nv4[1] = dtLength;
                            nv4[0] = Character.valueOf(100);
                            nv4[2] = Integer.valueOf(1);
                            vec.add(nv4);
                            nv2 = nv4;
                            ((StringBuilder) v[i5]).append(c3);
                            if (c3 != ((Character) v[0]).charValue()) {
                                v[2] = Integer.valueOf(i2 + 1);
                            } else {
                                v[0] = Character.valueOf(c3);
                                v[2] = Integer.valueOf(1);
                            }
                        }
                    }
                    i5 = 1;
                    ((StringBuilder) v[i5]).append(c3);
                    if (c3 != ((Character) v[0]).charValue()) {
                        v[0] = Character.valueOf(c3);
                        v[2] = Integer.valueOf(1);
                    } else {
                        v[2] = Integer.valueOf(i2 + 1);
                    }
                } else {
                    if (c3 != ((Character) v[0]).charValue() || c3 == 'S') {
                        ((StringBuilder) v[1]).append(separator);
                        if (c3 == 'X' || c3 == 'Y') {
                            v[2] = Integer.valueOf(4);
                        }
                    } else {
                        vecRemovelist.add(v);
                    }
                    dtLength2 = dtLength;
                }
                count++;
                dtLength = dtLength2;
                str = dt;
                i = 0;
                i2 = 2;
            }
            dtLength2 = dtLength;
            i5 = vecRemovelist.size();
            for (i = 0; i < i5; i++) {
                vec.remove((Object[]) vecRemovelist.get(i));
            }
            vecRemovelist.clear();
            nv = nv2;
            dtLength = dtLength2;
            str = dt;
            i = 0;
            i2 = 2;
        }
        i5 = vec.size();
        i = 0;
        while (i < i5) {
            boolean atEnd;
            boolean finishesAtDate;
            boolean containsEnd;
            Object[] v2 = (Object[]) vec.get(i);
            char c4 = ((Character) v2[0]).charValue();
            boolean bk = preparedStatement.getSuccessor(c4, ((Integer) v2[2]).intValue()) != c4;
            if (c4 != 's' && c4 != 'm') {
                if (c4 == 'h' && toTime) {
                }
                atEnd = false;
                if (!bk) {
                    if (c4 == 'd' && !toTime) {
                        finishesAtDate = true;
                        containsEnd = ((StringBuilder) v2[1]).toString().indexOf(87) == -1;
                        if ((atEnd && !finishesAtDate) || containsEnd) {
                            vecRemovelist.add(v2);
                        }
                        i++;
                        preparedStatement = this;
                    }
                }
                finishesAtDate = false;
                if (((StringBuilder) v2[1]).toString().indexOf(87) == -1) {
                }
                if (atEnd) {
                }
                i++;
                preparedStatement = this;
            }
            if (bk) {
                atEnd = true;
                if (!bk) {
                    finishesAtDate = true;
                    if (((StringBuilder) v2[1]).toString().indexOf(87) == -1) {
                    }
                    if (atEnd) {
                    }
                    i++;
                    preparedStatement = this;
                }
                finishesAtDate = false;
                if (((StringBuilder) v2[1]).toString().indexOf(87) == -1) {
                }
                if (atEnd) {
                }
                i++;
                preparedStatement = this;
            }
            atEnd = false;
            if (!bk) {
                finishesAtDate = true;
                if (((StringBuilder) v2[1]).toString().indexOf(87) == -1) {
                }
                if (atEnd) {
                }
                i++;
                preparedStatement = this;
            }
            finishesAtDate = false;
            if (((StringBuilder) v2[1]).toString().indexOf(87) == -1) {
            }
            if (atEnd) {
            }
            i++;
            preparedStatement = this;
        }
        int size = vecRemovelist.size();
        for (i5 = 0; i5 < size; i5++) {
            vec.remove(vecRemovelist.get(i5));
        }
        vecRemovelist.clear();
        StringBuilder format = ((Object[]) vec.get(0))[1];
        format.setLength(format.length() - 1);
        return format.toString();
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        SQLException sqlEx;
        PreparedStatement this;
        Throwable th;
        synchronized (checkClosed().getConnectionMutex()) {
            PreparedStatement this2;
            try {
                if (isSelectQuery()) {
                    PreparedStatement mdStmt = null;
                    ResultSet mdRs = null;
                    if (this.pstmtResultMetaData == null) {
                        try {
                            mdStmt = new PreparedStatement(this.connection, this.originalSql, this.currentCatalog, this.parseInfo);
                            int i = 1;
                            mdStmt.setMaxRows(1);
                            int paramCount = this.parameterValues.length;
                            while (i <= paramCount) {
                                mdStmt.setString(i, "");
                                i++;
                            }
                            if (mdStmt.execute()) {
                                mdRs = mdStmt.getResultSet();
                                this.pstmtResultMetaData = mdRs.getMetaData();
                            } else {
                                this.pstmtResultMetaData = new ResultSetMetaData(new Field[0], this.connection.getUseOldAliasMetadataBehavior(), this.connection.getYearIsDateType(), getExceptionInterceptor());
                            }
                            this2 = this;
                            SQLException sqlExRethrow = null;
                            if (mdRs != null) {
                                try {
                                    mdRs.close();
                                } catch (SQLException sqlEx2) {
                                    sqlExRethrow = sqlEx2;
                                }
                            }
                            if (mdStmt != null) {
                                try {
                                    mdStmt.close();
                                } catch (SQLException sqlEx22) {
                                    sqlExRethrow = sqlEx22;
                                }
                            }
                            if (sqlExRethrow != null) {
                                throw sqlExRethrow;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            this2 = this;
                            throw th;
                        }
                    }
                    this2 = this;
                    ResultSetMetaData resultSetMetaData = this2.pstmtResultMetaData;
                    return resultSetMetaData;
                }
                return null;
            } catch (Throwable th3) {
                th = th3;
            }
        }
    }

    protected boolean isSelectQuery() throws SQLException {
        boolean startsWithIgnoreCaseAndWs;
        synchronized (checkClosed().getConnectionMutex()) {
            startsWithIgnoreCaseAndWs = StringUtils.startsWithIgnoreCaseAndWs(StringUtils.stripComments(this.originalSql, "'\"", "'\"", true, false, true, true), "SELECT");
        }
        return startsWithIgnoreCaseAndWs;
    }

    public ParameterMetaData getParameterMetaData() throws SQLException {
        ParameterMetaData parameterMetaData;
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.parameterMetaData == null) {
                if (this.connection.getGenerateSimpleParameterMetadata()) {
                    this.parameterMetaData = new MysqlParameterMetadata(this.parameterCount);
                } else {
                    this.parameterMetaData = new MysqlParameterMetadata(null, this.parameterCount, getExceptionInterceptor());
                }
            }
            parameterMetaData = this.parameterMetaData;
        }
        return parameterMetaData;
    }

    ParseInfo getParseInfo() {
        return this.parseInfo;
    }

    private final char getSuccessor(char c, int n) {
        if (c == 'y' && n == 2) {
            return 'X';
        }
        if (c == 'y' && n < 4) {
            return 'y';
        }
        if (c != 'y') {
            if (c == 'M' && n == 2) {
                return 'Y';
            }
            if (c != 'M' || n >= 3) {
                if (c != 'M') {
                    if (c != 'd' || n >= 2) {
                        if (c != 'd') {
                            if (c != 'H' || n >= 2) {
                                if (c != 'H') {
                                    if (c != 'm' || n >= 2) {
                                        if (c != 'm') {
                                            if (c != 's' || n >= 2) {
                                                return 'W';
                                            }
                                        }
                                        return 's';
                                    }
                                }
                                return 'm';
                            }
                        }
                        return 'H';
                    }
                }
                return 'd';
            }
        }
        return 'M';
    }

    private final void hexEscapeBlock(byte[] buf, Buffer packet, int size) throws SQLException {
        for (int i = 0; i < size; i++) {
            byte b = buf[i];
            int highBits = (b & 255) % 16;
            packet.writeByte(HEX_DIGITS[(b & 255) / 16]);
            packet.writeByte(HEX_DIGITS[highBits]);
        }
    }

    private void initializeFromParseInfo() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            this.staticSqlStrings = this.parseInfo.staticSql;
            this.isLoadDataQuery = this.parseInfo.foundLoadData;
            this.firstCharOfStmt = this.parseInfo.firstStmtChar;
            this.parameterCount = this.staticSqlStrings.length - 1;
            this.parameterValues = new byte[this.parameterCount][];
            this.parameterStreams = new InputStream[this.parameterCount];
            this.isStream = new boolean[this.parameterCount];
            this.streamLengths = new int[this.parameterCount];
            this.isNull = new boolean[this.parameterCount];
            this.parameterTypes = new int[this.parameterCount];
            clearParameters();
            for (int j = 0; j < this.parameterCount; j++) {
                this.isStream[j] = false;
            }
        }
    }

    boolean isNull(int paramIndex) throws SQLException {
        boolean z;
        synchronized (checkClosed().getConnectionMutex()) {
            z = this.isNull[paramIndex];
        }
        return z;
    }

    private final int readblock(InputStream i, byte[] b) throws SQLException {
        try {
            return i.read(b);
        } catch (Throwable ex) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Messages.getString("PreparedStatement.56"));
            stringBuilder.append(ex.getClass().getName());
            SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor()).initCause(ex);
        }
    }

    private final int readblock(InputStream i, byte[] b, int length) throws SQLException {
        int lengthToRead = length;
        try {
            if (lengthToRead > b.length) {
                lengthToRead = b.length;
            }
            return i.read(b, 0, lengthToRead);
        } catch (Throwable ex) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Messages.getString("PreparedStatement.56"));
            stringBuilder.append(ex.getClass().getName());
            SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor()).initCause(ex);
        }
    }

    protected void realClose(boolean calledExplicitly, boolean closeOpenResults) throws SQLException {
        MySQLConnection locallyScopedConn = this.connection;
        if (locallyScopedConn != null) {
            synchronized (locallyScopedConn.getConnectionMutex()) {
                try {
                    if (r1.isClosed) {
                        return;
                    }
                    if (r1.useUsageAdvisor && r1.numberOfExecutions <= 1) {
                        String message = Messages.getString("PreparedStatement.43");
                        ProfilerEventHandler profilerEventHandler = r1.eventSink;
                        String str = "";
                        String str2 = r1.currentCatalog;
                        long j = r1.connectionId;
                        int id = getId();
                        long currentTimeMillis = System.currentTimeMillis();
                        String str3 = Constants.MILLIS_I18N;
                        String str4 = r1.pointOfOrigin;
                    }
                    super.realClose(calledExplicitly, closeOpenResults);
                    r1.dbmd = null;
                    r1.originalSql = null;
                    r1.staticSqlStrings = (byte[][]) null;
                    r1.parameterValues = (byte[][]) null;
                    r1.parameterStreams = null;
                    r1.isStream = null;
                    r1.streamLengths = null;
                    r1.isNull = null;
                    r1.streamConvertBuf = null;
                    r1.parameterTypes = null;
                } finally {
                    Object obj = r0;
                }
            }
        }
    }

    public void setArray(int i, Array x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        if (x == null) {
            setNull(parameterIndex, 12);
        } else {
            setBinaryStream(parameterIndex, x, length);
        }
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        if (x == null) {
            setNull(parameterIndex, 3);
            return;
        }
        setInternal(parameterIndex, StringUtils.fixDecimalExponent(StringUtils.consistentToString(x)));
        this.parameterTypes[(parameterIndex - 1) + getParameterIndexOffset()] = 3;
    }

    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (x == null) {
                setNull(parameterIndex, -2);
            } else {
                int parameterIndexOffset = getParameterIndexOffset();
                if (parameterIndex >= 1) {
                    if (parameterIndex <= this.staticSqlStrings.length) {
                        if (parameterIndexOffset == -1 && parameterIndex == 1) {
                            throw SQLError.createSQLException("Can't set IN parameter for return value of stored function call.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                        }
                        this.parameterStreams[(parameterIndex - 1) + parameterIndexOffset] = x;
                        this.isStream[(parameterIndex - 1) + parameterIndexOffset] = true;
                        this.streamLengths[(parameterIndex - 1) + parameterIndexOffset] = length;
                        this.isNull[(parameterIndex - 1) + parameterIndexOffset] = false;
                        this.parameterTypes[(parameterIndex - 1) + getParameterIndexOffset()] = 2004;
                    }
                }
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(Messages.getString("PreparedStatement.2"));
                stringBuilder.append(parameterIndex);
                stringBuilder.append(Messages.getString("PreparedStatement.3"));
                stringBuilder.append(this.staticSqlStrings.length);
                stringBuilder.append(Messages.getString("PreparedStatement.4"));
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            }
        }
    }

    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        setBinaryStream(parameterIndex, inputStream, (int) length);
    }

    public void setBlob(int i, Blob x) throws SQLException {
        if (x == null) {
            setNull(i, 2004);
            return;
        }
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        bytesOut.write(39);
        escapeblockFast(x.getBytes(1, (int) x.length()), bytesOut, (int) x.length());
        bytesOut.write(39);
        setInternal(i, bytesOut.toByteArray());
        this.parameterTypes[(i - 1) + getParameterIndexOffset()] = 2004;
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        if (this.useTrueBoolean) {
            setInternal(parameterIndex, x ? "1" : "0");
            return;
        }
        setInternal(parameterIndex, x ? "'t'" : "'f'");
        this.parameterTypes[(parameterIndex - 1) + getParameterIndexOffset()] = 16;
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        setInternal(parameterIndex, String.valueOf(x));
        this.parameterTypes[(parameterIndex - 1) + getParameterIndexOffset()] = -6;
    }

    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        setBytes(parameterIndex, x, true, true);
        if (x != null) {
            this.parameterTypes[(parameterIndex - 1) + getParameterIndexOffset()] = -2;
        }
    }

    protected void setBytesNoEscape(int parameterIndex, byte[] parameterAsBytes) throws SQLException {
        byte[] parameterWithQuotes = new byte[(parameterAsBytes.length + 2)];
        parameterWithQuotes[0] = (byte) 39;
        System.arraycopy(parameterAsBytes, 0, parameterWithQuotes, 1, parameterAsBytes.length);
        parameterWithQuotes[parameterAsBytes.length + 1] = (byte) 39;
        setInternal(parameterIndex, parameterWithQuotes);
    }

    protected void setBytesNoEscapeNoQuotes(int parameterIndex, byte[] parameterAsBytes) throws SQLException {
        setInternal(parameterIndex, parameterAsBytes);
    }

    public void setCharacterStream(int r11, java.io.Reader r12, int r13) throws java.sql.SQLException {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:com.mysql.jdbc.PreparedStatement.setCharacterStream(int, java.io.Reader, int):void. bs: [B:4:0x000c, B:17:0x003b, B:32:0x0085]
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:86)
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:56)
	at jadx.core.ProcessClass.process(ProcessClass.java:39)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:282)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
	at jadx.api.JadxDecompiler$$Lambda$8/1205406622.run(Unknown Source)
*/
        /*
        r10 = this;
        r0 = r10.checkClosed();
        r0 = r0.getConnectionMutex();
        monitor-enter(r0);
        r1 = -1;
        if (r12 != 0) goto L_0x0017;
    L_0x000c:
        r10.setNull(r11, r1);	 Catch:{ IOException -> 0x0014 }
        goto L_0x009f;	 Catch:{ IOException -> 0x0014 }
    L_0x0011:
        r1 = move-exception;	 Catch:{ IOException -> 0x0014 }
        goto L_0x00cf;	 Catch:{ IOException -> 0x0014 }
    L_0x0014:
        r1 = move-exception;	 Catch:{ IOException -> 0x0014 }
        goto L_0x00bf;	 Catch:{ IOException -> 0x0014 }
    L_0x0017:
        r2 = 0;	 Catch:{ IOException -> 0x0014 }
        r3 = 0;	 Catch:{ IOException -> 0x0014 }
        r4 = r10.connection;	 Catch:{ IOException -> 0x0014 }
        r4 = r4.getUseStreamLengthsInPrepStmts();	 Catch:{ IOException -> 0x0014 }
        r5 = r10.connection;	 Catch:{ IOException -> 0x0014 }
        r5 = r5.getClobCharacterEncoding();	 Catch:{ IOException -> 0x0014 }
        r6 = 0;	 Catch:{ IOException -> 0x0014 }
        if (r4 == 0) goto L_0x0066;	 Catch:{ IOException -> 0x0014 }
    L_0x0028:
        if (r13 == r1) goto L_0x0066;	 Catch:{ IOException -> 0x0014 }
    L_0x002a:
        r1 = new char[r13];	 Catch:{ IOException -> 0x0014 }
        r2 = readFully(r12, r1, r13);	 Catch:{ IOException -> 0x0014 }
        if (r5 != 0) goto L_0x003b;	 Catch:{ IOException -> 0x0014 }
    L_0x0032:
        r7 = new java.lang.String;	 Catch:{ IOException -> 0x0014 }
        r7.<init>(r1, r6, r2);	 Catch:{ IOException -> 0x0014 }
        r10.setString(r11, r7);	 Catch:{ IOException -> 0x0014 }
        goto L_0x0048;
    L_0x003b:
        r7 = new java.lang.String;	 Catch:{ UnsupportedEncodingException -> 0x0049 }
        r7.<init>(r1, r6, r2);	 Catch:{ UnsupportedEncodingException -> 0x0049 }
        r6 = com.mysql.jdbc.StringUtils.getBytes(r7, r5);	 Catch:{ UnsupportedEncodingException -> 0x0049 }
        r10.setBytes(r11, r6);	 Catch:{ UnsupportedEncodingException -> 0x0049 }
    L_0x0048:
        goto L_0x0092;
    L_0x0049:
        r6 = move-exception;
        r7 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x0014 }
        r7.<init>();	 Catch:{ IOException -> 0x0014 }
        r8 = "Unsupported character encoding ";	 Catch:{ IOException -> 0x0014 }
        r7.append(r8);	 Catch:{ IOException -> 0x0014 }
        r7.append(r5);	 Catch:{ IOException -> 0x0014 }
        r7 = r7.toString();	 Catch:{ IOException -> 0x0014 }
        r8 = "S1009";	 Catch:{ IOException -> 0x0014 }
        r9 = r10.getExceptionInterceptor();	 Catch:{ IOException -> 0x0014 }
        r7 = com.mysql.jdbc.SQLError.createSQLException(r7, r8, r9);	 Catch:{ IOException -> 0x0014 }
        throw r7;	 Catch:{ IOException -> 0x0014 }
    L_0x0066:
        r7 = 4096; // 0x1000 float:5.74E-42 double:2.0237E-320;	 Catch:{ IOException -> 0x0014 }
        r7 = new char[r7];	 Catch:{ IOException -> 0x0014 }
        r2 = r7;	 Catch:{ IOException -> 0x0014 }
        r7 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x0014 }
        r7.<init>();	 Catch:{ IOException -> 0x0014 }
    L_0x0070:
        r8 = r12.read(r2);	 Catch:{ IOException -> 0x0014 }
        r3 = r8;	 Catch:{ IOException -> 0x0014 }
        if (r8 == r1) goto L_0x007b;	 Catch:{ IOException -> 0x0014 }
    L_0x0077:
        r7.append(r2, r6, r3);	 Catch:{ IOException -> 0x0014 }
        goto L_0x0070;	 Catch:{ IOException -> 0x0014 }
    L_0x007b:
        if (r5 != 0) goto L_0x0085;	 Catch:{ IOException -> 0x0014 }
    L_0x007d:
        r1 = r7.toString();	 Catch:{ IOException -> 0x0014 }
        r10.setString(r11, r1);	 Catch:{ IOException -> 0x0014 }
        goto L_0x0091;
    L_0x0085:
        r1 = r7.toString();	 Catch:{ UnsupportedEncodingException -> 0x00a2 }
        r1 = com.mysql.jdbc.StringUtils.getBytes(r1, r5);	 Catch:{ UnsupportedEncodingException -> 0x00a2 }
        r10.setBytes(r11, r1);	 Catch:{ UnsupportedEncodingException -> 0x00a2 }
    L_0x0091:
        r1 = r2;
    L_0x0092:
        r2 = r10.parameterTypes;	 Catch:{ IOException -> 0x0014 }
        r6 = r11 + -1;	 Catch:{ IOException -> 0x0014 }
        r7 = r10.getParameterIndexOffset();	 Catch:{ IOException -> 0x0014 }
        r6 = r6 + r7;	 Catch:{ IOException -> 0x0014 }
        r7 = 2005; // 0x7d5 float:2.81E-42 double:9.906E-321;	 Catch:{ IOException -> 0x0014 }
        r2[r6] = r7;	 Catch:{ IOException -> 0x0014 }
        monitor-exit(r0);	 Catch:{ all -> 0x0011 }
        return;
    L_0x00a2:
        r1 = move-exception;
        r6 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x0014 }
        r6.<init>();	 Catch:{ IOException -> 0x0014 }
        r8 = "Unsupported character encoding ";	 Catch:{ IOException -> 0x0014 }
        r6.append(r8);	 Catch:{ IOException -> 0x0014 }
        r6.append(r5);	 Catch:{ IOException -> 0x0014 }
        r6 = r6.toString();	 Catch:{ IOException -> 0x0014 }
        r8 = "S1009";	 Catch:{ IOException -> 0x0014 }
        r9 = r10.getExceptionInterceptor();	 Catch:{ IOException -> 0x0014 }
        r6 = com.mysql.jdbc.SQLError.createSQLException(r6, r8, r9);	 Catch:{ IOException -> 0x0014 }
        throw r6;	 Catch:{ IOException -> 0x0014 }
        r2 = r1.toString();	 Catch:{ all -> 0x0011 }
        r3 = "S1000";	 Catch:{ all -> 0x0011 }
        r4 = r10.getExceptionInterceptor();	 Catch:{ all -> 0x0011 }
        r2 = com.mysql.jdbc.SQLError.createSQLException(r2, r3, r4);	 Catch:{ all -> 0x0011 }
        throw r2;	 Catch:{ all -> 0x0011 }
    L_0x00cf:
        monitor-exit(r0);	 Catch:{ all -> 0x0011 }
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.PreparedStatement.setCharacterStream(int, java.io.Reader, int):void");
    }

    public void setClob(int i, Clob x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (x == null) {
                setNull(i, 2005);
            } else {
                String forcedEncoding = this.connection.getClobCharacterEncoding();
                if (forcedEncoding == null) {
                    setString(i, x.getSubString(1, (int) x.length()));
                } else {
                    try {
                        setBytes(i, StringUtils.getBytes(x.getSubString(1, (int) x.length()), forcedEncoding));
                    } catch (UnsupportedEncodingException e) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Unsupported character encoding ");
                        stringBuilder.append(forcedEncoding);
                        throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
                    }
                }
                this.parameterTypes[(i - 1) + getParameterIndexOffset()] = 2005;
            }
        }
    }

    public void setDate(int parameterIndex, Date x) throws SQLException {
        setDate(parameterIndex, x, null);
    }

    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        if (x == null) {
            setNull(parameterIndex, 91);
        } else if (this.useLegacyDatetimeCode) {
            synchronized (checkClosed().getConnectionMutex()) {
                if (this.ddf == null) {
                    this.ddf = new SimpleDateFormat("''yyyy-MM-dd''", Locale.US);
                }
                if (cal != null) {
                    this.ddf.setTimeZone(cal.getTimeZone());
                }
                setInternal(parameterIndex, this.ddf.format(x));
                this.parameterTypes[(parameterIndex - 1) + getParameterIndexOffset()] = 91;
            }
        } else {
            newSetDateInternal(parameterIndex, x, cal);
        }
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.connection.getAllowNanAndInf() || !(x == Double.POSITIVE_INFINITY || x == Double.NEGATIVE_INFINITY || Double.isNaN(x))) {
                setInternal(parameterIndex, StringUtils.fixDecimalExponent(String.valueOf(x)));
                this.parameterTypes[(parameterIndex - 1) + getParameterIndexOffset()] = 8;
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("'");
                stringBuilder.append(x);
                stringBuilder.append("' is not a valid numeric or approximate numeric value");
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            }
        }
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        setInternal(parameterIndex, StringUtils.fixDecimalExponent(String.valueOf(x)));
        this.parameterTypes[(parameterIndex - 1) + getParameterIndexOffset()] = 6;
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        setInternal(parameterIndex, String.valueOf(x));
        this.parameterTypes[(parameterIndex - 1) + getParameterIndexOffset()] = 4;
    }

    protected final void setInternal(int paramIndex, byte[] val) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            int parameterIndexOffset = getParameterIndexOffset();
            checkBounds(paramIndex, parameterIndexOffset);
            this.isStream[(paramIndex - 1) + parameterIndexOffset] = false;
            this.isNull[(paramIndex - 1) + parameterIndexOffset] = false;
            this.parameterStreams[(paramIndex - 1) + parameterIndexOffset] = null;
            this.parameterValues[(paramIndex - 1) + parameterIndexOffset] = val;
        }
    }

    protected void checkBounds(int paramIndex, int parameterIndexOffset) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            StringBuilder stringBuilder;
            if (paramIndex < 1) {
                stringBuilder = new StringBuilder();
                stringBuilder.append(Messages.getString("PreparedStatement.49"));
                stringBuilder.append(paramIndex);
                stringBuilder.append(Messages.getString("PreparedStatement.50"));
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            } else if (paramIndex > this.parameterCount) {
                stringBuilder = new StringBuilder();
                stringBuilder.append(Messages.getString("PreparedStatement.51"));
                stringBuilder.append(paramIndex);
                stringBuilder.append(Messages.getString("PreparedStatement.52"));
                stringBuilder.append(this.parameterValues.length);
                stringBuilder.append(Messages.getString("PreparedStatement.53"));
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            } else if (parameterIndexOffset == -1 && paramIndex == 1) {
                throw SQLError.createSQLException("Can't set IN parameter for return value of stored function call.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            }
        }
    }

    protected final void setInternal(int paramIndex, String val) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            byte[] parameterAsBytes;
            if (this.charConverter != null) {
                parameterAsBytes = this.charConverter.toBytes(val);
            } else {
                parameterAsBytes = StringUtils.getBytes(val, this.charConverter, this.charEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), getExceptionInterceptor());
            }
            setInternal(paramIndex, parameterAsBytes);
        }
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        setInternal(parameterIndex, String.valueOf(x));
        this.parameterTypes[(parameterIndex - 1) + getParameterIndexOffset()] = -5;
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            setInternal(parameterIndex, "null");
            this.isNull[(parameterIndex - 1) + getParameterIndexOffset()] = true;
            this.parameterTypes[(parameterIndex - 1) + getParameterIndexOffset()] = 0;
        }
    }

    public void setNull(int parameterIndex, int sqlType, String arg) throws SQLException {
        setNull(parameterIndex, sqlType);
        this.parameterTypes[(parameterIndex - 1) + getParameterIndexOffset()] = 0;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setNumericObject(int r8, java.lang.Object r9, int r10, int r11) throws java.sql.SQLException {
        /*
        r7 = this;
        r0 = r9 instanceof java.lang.Boolean;
        r1 = 0;
        r2 = 1;
        if (r0 == 0) goto L_0x001a;
    L_0x0006:
        r0 = r9;
        r0 = (java.lang.Boolean) r0;
        r0 = r0.booleanValue();
        if (r0 == 0) goto L_0x0014;
    L_0x000f:
        r0 = java.lang.Integer.valueOf(r2);
        goto L_0x0018;
    L_0x0014:
        r0 = java.lang.Integer.valueOf(r1);
    L_0x0018:
        goto L_0x007f;
    L_0x001a:
        r0 = r9 instanceof java.lang.String;
        if (r0 == 0) goto L_0x007c;
    L_0x001e:
        switch(r10) {
            case -7: goto L_0x004d;
            case -6: goto L_0x0045;
            case -5: goto L_0x003d;
            default: goto L_0x0021;
        };
    L_0x0021:
        switch(r10) {
            case 4: goto L_0x0045;
            case 5: goto L_0x0045;
            case 6: goto L_0x0035;
            case 7: goto L_0x002d;
            case 8: goto L_0x0035;
            default: goto L_0x0024;
        };
    L_0x0024:
        r0 = new java.math.BigDecimal;
        r1 = r9;
        r1 = (java.lang.String) r1;
        r0.<init>(r1);
        goto L_0x0018;
    L_0x002d:
        r0 = r9;
        r0 = (java.lang.String) r0;
        r0 = java.lang.Float.valueOf(r0);
        goto L_0x007f;
    L_0x0035:
        r0 = r9;
        r0 = (java.lang.String) r0;
        r0 = java.lang.Double.valueOf(r0);
        goto L_0x007f;
    L_0x003d:
        r0 = r9;
        r0 = (java.lang.String) r0;
        r0 = java.lang.Long.valueOf(r0);
        goto L_0x007f;
    L_0x0045:
        r0 = r9;
        r0 = (java.lang.String) r0;
        r0 = java.lang.Integer.valueOf(r0);
        goto L_0x007f;
    L_0x004d:
        r0 = "1";
        r0 = r0.equals(r9);
        if (r0 != 0) goto L_0x0074;
    L_0x0055:
        r0 = "0";
        r0 = r0.equals(r9);
        if (r0 == 0) goto L_0x005e;
    L_0x005d:
        goto L_0x0074;
    L_0x005e:
        r0 = "true";
        r3 = r9;
        r3 = (java.lang.String) r3;
        r0 = r0.equalsIgnoreCase(r3);
        if (r0 == 0) goto L_0x006e;
    L_0x0069:
        r1 = java.lang.Integer.valueOf(r2);
        goto L_0x0072;
    L_0x006e:
        r1 = java.lang.Integer.valueOf(r1);
    L_0x0072:
        r0 = r1;
        goto L_0x007f;
    L_0x0074:
        r0 = r9;
        r0 = (java.lang.String) r0;
        r0 = java.lang.Integer.valueOf(r0);
        goto L_0x0018;
    L_0x007c:
        r0 = r9;
        r0 = (java.lang.Number) r0;
    L_0x007f:
        switch(r10) {
            case -7: goto L_0x0105;
            case -6: goto L_0x0105;
            case -5: goto L_0x00fd;
            default: goto L_0x0082;
        };
    L_0x0082:
        switch(r10) {
            case 2: goto L_0x0099;
            case 3: goto L_0x0099;
            case 4: goto L_0x0105;
            case 5: goto L_0x0105;
            case 6: goto L_0x0090;
            case 7: goto L_0x0087;
            case 8: goto L_0x0090;
            default: goto L_0x0085;
        };
    L_0x0085:
        goto L_0x010d;
    L_0x0087:
        r1 = r0.floatValue();
        r7.setFloat(r8, r1);
        goto L_0x010d;
    L_0x0090:
        r1 = r0.doubleValue();
        r7.setDouble(r8, r1);
        goto L_0x010d;
    L_0x0099:
        r1 = r0 instanceof java.math.BigDecimal;
        if (r1 == 0) goto L_0x00e0;
    L_0x009d:
        r1 = 0;
        r2 = r0;
        r2 = (java.math.BigDecimal) r2;	 Catch:{ ArithmeticException -> 0x00a7 }
        r2 = r2.setScale(r11);	 Catch:{ ArithmeticException -> 0x00a7 }
        r1 = r2;
        goto L_0x00b2;
    L_0x00a7:
        r2 = move-exception;
        r3 = r0;
        r3 = (java.math.BigDecimal) r3;	 Catch:{ ArithmeticException -> 0x00b6 }
        r4 = 4;
        r3 = r3.setScale(r11, r4);	 Catch:{ ArithmeticException -> 0x00b6 }
        r1 = r3;
    L_0x00b2:
        r7.setBigDecimal(r8, r1);
        goto L_0x010d;
    L_0x00b6:
        r3 = move-exception;
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r5 = "Can't set scale of '";
        r4.append(r5);
        r4.append(r11);
        r5 = "' for DECIMAL argument '";
        r4.append(r5);
        r4.append(r0);
        r5 = "'";
        r4.append(r5);
        r4 = r4.toString();
        r5 = "S1009";
        r6 = r7.getExceptionInterceptor();
        r4 = com.mysql.jdbc.SQLError.createSQLException(r4, r5, r6);
        throw r4;
    L_0x00e0:
        r1 = r0 instanceof java.math.BigInteger;
        if (r1 == 0) goto L_0x00f0;
    L_0x00e4:
        r1 = new java.math.BigDecimal;
        r2 = r0;
        r2 = (java.math.BigInteger) r2;
        r1.<init>(r2, r11);
        r7.setBigDecimal(r8, r1);
        goto L_0x010d;
    L_0x00f0:
        r1 = new java.math.BigDecimal;
        r2 = r0.doubleValue();
        r1.<init>(r2);
        r7.setBigDecimal(r8, r1);
        goto L_0x010d;
    L_0x00fd:
        r1 = r0.longValue();
        r7.setLong(r8, r1);
        goto L_0x010d;
    L_0x0105:
        r1 = r0.intValue();
        r7.setInt(r8, r1);
    L_0x010d:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.PreparedStatement.setNumericObject(int, java.lang.Object, int, int):void");
    }

    public void setObject(int parameterIndex, Object parameterObj) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (parameterObj == null) {
                setNull(parameterIndex, MysqlErrorNumbers.ER_INVALID_GROUP_FUNC_USE);
            } else if (parameterObj instanceof Byte) {
                setInt(parameterIndex, ((Byte) parameterObj).intValue());
            } else if (parameterObj instanceof String) {
                setString(parameterIndex, (String) parameterObj);
            } else if (parameterObj instanceof BigDecimal) {
                setBigDecimal(parameterIndex, (BigDecimal) parameterObj);
            } else if (parameterObj instanceof Short) {
                setShort(parameterIndex, ((Short) parameterObj).shortValue());
            } else if (parameterObj instanceof Integer) {
                setInt(parameterIndex, ((Integer) parameterObj).intValue());
            } else if (parameterObj instanceof Long) {
                setLong(parameterIndex, ((Long) parameterObj).longValue());
            } else if (parameterObj instanceof Float) {
                setFloat(parameterIndex, ((Float) parameterObj).floatValue());
            } else if (parameterObj instanceof Double) {
                setDouble(parameterIndex, ((Double) parameterObj).doubleValue());
            } else if (parameterObj instanceof byte[]) {
                setBytes(parameterIndex, (byte[]) parameterObj);
            } else if (parameterObj instanceof Date) {
                setDate(parameterIndex, (Date) parameterObj);
            } else if (parameterObj instanceof Time) {
                setTime(parameterIndex, (Time) parameterObj);
            } else if (parameterObj instanceof Timestamp) {
                setTimestamp(parameterIndex, (Timestamp) parameterObj);
            } else if (parameterObj instanceof Boolean) {
                setBoolean(parameterIndex, ((Boolean) parameterObj).booleanValue());
            } else if (parameterObj instanceof InputStream) {
                setBinaryStream(parameterIndex, (InputStream) parameterObj, -1);
            } else if (parameterObj instanceof Blob) {
                setBlob(parameterIndex, (Blob) parameterObj);
            } else if (parameterObj instanceof Clob) {
                setClob(parameterIndex, (Clob) parameterObj);
            } else if (this.connection.getTreatUtilDateAsTimestamp() && (parameterObj instanceof java.util.Date)) {
                setTimestamp(parameterIndex, new Timestamp(((java.util.Date) parameterObj).getTime()));
            } else if (parameterObj instanceof BigInteger) {
                setString(parameterIndex, parameterObj.toString());
            } else {
                setSerializableObject(parameterIndex, parameterObj);
            }
        }
    }

    public void setObject(int parameterIndex, Object parameterObj, int targetSqlType) throws SQLException {
        if (parameterObj instanceof BigDecimal) {
            setObject(parameterIndex, parameterObj, targetSqlType, ((BigDecimal) parameterObj).scale());
        } else {
            setObject(parameterIndex, parameterObj, targetSqlType, 0);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setObject(int r9, java.lang.Object r10, int r11, int r12) throws java.sql.SQLException {
        /*
        r8 = this;
        r0 = r8.checkClosed();
        r0 = r0.getConnectionMutex();
        monitor-enter(r0);
        r1 = 1111; // 0x457 float:1.557E-42 double:5.49E-321;
        if (r10 != 0) goto L_0x0015;
    L_0x000d:
        r8.setNull(r9, r1);	 Catch:{ all -> 0x0012 }
        goto L_0x01c4;
    L_0x0012:
        r1 = move-exception;
        goto L_0x021e;
    L_0x0015:
        r2 = 12;
        if (r11 == r2) goto L_0x01a8;
    L_0x0019:
        r2 = 16;
        r3 = 1;
        r4 = 0;
        if (r11 == r2) goto L_0x013b;
    L_0x001f:
        if (r11 == r1) goto L_0x0136;
    L_0x0021:
        switch(r11) {
            case -7: goto L_0x0131;
            case -6: goto L_0x0131;
            case -5: goto L_0x0131;
            case -4: goto L_0x00f8;
            case -3: goto L_0x00f8;
            case -2: goto L_0x00f8;
            case -1: goto L_0x01a8;
            default: goto L_0x0024;
        };
    L_0x0024:
        switch(r11) {
            case 1: goto L_0x01a8;
            case 2: goto L_0x0131;
            case 3: goto L_0x0131;
            case 4: goto L_0x0131;
            case 5: goto L_0x0131;
            case 6: goto L_0x0131;
            case 7: goto L_0x0131;
            case 8: goto L_0x0131;
            default: goto L_0x0027;
        };
    L_0x0027:
        switch(r11) {
            case 91: goto L_0x009a;
            case 92: goto L_0x0056;
            case 93: goto L_0x009a;
            default: goto L_0x002a;
        };
    L_0x002a:
        switch(r11) {
            case 2004: goto L_0x00f8;
            case 2005: goto L_0x0041;
            default: goto L_0x002d;
        };
    L_0x002d:
        r1 = "PreparedStatement.16";
        r1 = com.mysql.jdbc.Messages.getString(r1);	 Catch:{ Exception -> 0x003e }
        r2 = "S1000";
        r3 = r8.getExceptionInterceptor();	 Catch:{ Exception -> 0x003e }
        r1 = com.mysql.jdbc.SQLError.createSQLException(r1, r2, r3);	 Catch:{ Exception -> 0x003e }
        throw r1;	 Catch:{ Exception -> 0x003e }
    L_0x003e:
        r1 = move-exception;
        goto L_0x01c6;
    L_0x0041:
        r1 = r10 instanceof java.sql.Clob;	 Catch:{ Exception -> 0x003e }
        if (r1 == 0) goto L_0x004d;
    L_0x0045:
        r1 = r10;
        r1 = (java.sql.Clob) r1;	 Catch:{ Exception -> 0x003e }
        r8.setClob(r9, r1);	 Catch:{ Exception -> 0x003e }
        goto L_0x01c3;
    L_0x004d:
        r1 = r10.toString();	 Catch:{ Exception -> 0x003e }
        r8.setString(r9, r1);	 Catch:{ Exception -> 0x003e }
        goto L_0x01c3;
    L_0x0056:
        r1 = r10 instanceof java.lang.String;	 Catch:{ Exception -> 0x003e }
        if (r1 == 0) goto L_0x007d;
    L_0x005a:
        r1 = new java.text.SimpleDateFormat;	 Catch:{ Exception -> 0x003e }
        r2 = r10;
        r2 = (java.lang.String) r2;	 Catch:{ Exception -> 0x003e }
        r2 = r8.getDateTimePattern(r2, r3);	 Catch:{ Exception -> 0x003e }
        r3 = java.util.Locale.US;	 Catch:{ Exception -> 0x003e }
        r1.<init>(r2, r3);	 Catch:{ Exception -> 0x003e }
        r2 = new java.sql.Time;	 Catch:{ Exception -> 0x003e }
        r3 = r10;
        r3 = (java.lang.String) r3;	 Catch:{ Exception -> 0x003e }
        r3 = r1.parse(r3);	 Catch:{ Exception -> 0x003e }
        r3 = r3.getTime();	 Catch:{ Exception -> 0x003e }
        r2.<init>(r3);	 Catch:{ Exception -> 0x003e }
        r8.setTime(r9, r2);	 Catch:{ Exception -> 0x003e }
        goto L_0x01c3;
    L_0x007d:
        r1 = r10 instanceof java.sql.Timestamp;	 Catch:{ Exception -> 0x003e }
        if (r1 == 0) goto L_0x0092;
    L_0x0081:
        r1 = r10;
        r1 = (java.sql.Timestamp) r1;	 Catch:{ Exception -> 0x003e }
        r2 = new java.sql.Time;	 Catch:{ Exception -> 0x003e }
        r3 = r1.getTime();	 Catch:{ Exception -> 0x003e }
        r2.<init>(r3);	 Catch:{ Exception -> 0x003e }
        r8.setTime(r9, r2);	 Catch:{ Exception -> 0x003e }
        goto L_0x01c3;
    L_0x0092:
        r1 = r10;
        r1 = (java.sql.Time) r1;	 Catch:{ Exception -> 0x003e }
        r8.setTime(r9, r1);	 Catch:{ Exception -> 0x003e }
        goto L_0x01c3;
    L_0x009a:
        r1 = r10 instanceof java.lang.String;	 Catch:{ Exception -> 0x003e }
        if (r1 == 0) goto L_0x00ba;
    L_0x009e:
        r1 = new java.text.ParsePosition;	 Catch:{ Exception -> 0x003e }
        r1.<init>(r4);	 Catch:{ Exception -> 0x003e }
        r2 = new java.text.SimpleDateFormat;	 Catch:{ Exception -> 0x003e }
        r3 = r10;
        r3 = (java.lang.String) r3;	 Catch:{ Exception -> 0x003e }
        r3 = r8.getDateTimePattern(r3, r4);	 Catch:{ Exception -> 0x003e }
        r4 = java.util.Locale.US;	 Catch:{ Exception -> 0x003e }
        r2.<init>(r3, r4);	 Catch:{ Exception -> 0x003e }
        r3 = r10;
        r3 = (java.lang.String) r3;	 Catch:{ Exception -> 0x003e }
        r3 = r2.parse(r3, r1);	 Catch:{ Exception -> 0x003e }
        r1 = r3;
        goto L_0x00bd;
    L_0x00ba:
        r1 = r10;
        r1 = (java.util.Date) r1;	 Catch:{ Exception -> 0x003e }
    L_0x00bd:
        r2 = 91;
        if (r11 == r2) goto L_0x00de;
    L_0x00c1:
        r2 = 93;
        if (r11 == r2) goto L_0x00c6;
    L_0x00c5:
        goto L_0x00f6;
    L_0x00c6:
        r2 = r1 instanceof java.sql.Timestamp;	 Catch:{ Exception -> 0x003e }
        if (r2 == 0) goto L_0x00d1;
    L_0x00ca:
        r2 = r1;
        r2 = (java.sql.Timestamp) r2;	 Catch:{ Exception -> 0x003e }
        r8.setTimestamp(r9, r2);	 Catch:{ Exception -> 0x003e }
        goto L_0x00f6;
    L_0x00d1:
        r2 = new java.sql.Timestamp;	 Catch:{ Exception -> 0x003e }
        r3 = r1.getTime();	 Catch:{ Exception -> 0x003e }
        r2.<init>(r3);	 Catch:{ Exception -> 0x003e }
        r8.setTimestamp(r9, r2);	 Catch:{ Exception -> 0x003e }
        goto L_0x00f6;
    L_0x00de:
        r2 = r1 instanceof java.sql.Date;	 Catch:{ Exception -> 0x003e }
        if (r2 == 0) goto L_0x00e9;
    L_0x00e2:
        r2 = r1;
        r2 = (java.sql.Date) r2;	 Catch:{ Exception -> 0x003e }
        r8.setDate(r9, r2);	 Catch:{ Exception -> 0x003e }
        goto L_0x00f6;
    L_0x00e9:
        r2 = new java.sql.Date;	 Catch:{ Exception -> 0x003e }
        r3 = r1.getTime();	 Catch:{ Exception -> 0x003e }
        r2.<init>(r3);	 Catch:{ Exception -> 0x003e }
        r8.setDate(r9, r2);	 Catch:{ Exception -> 0x003e }
    L_0x00f6:
        goto L_0x01c3;
    L_0x00f8:
        r1 = r10 instanceof byte[];	 Catch:{ Exception -> 0x003e }
        if (r1 == 0) goto L_0x0104;
    L_0x00fc:
        r1 = r10;
        r1 = (byte[]) r1;	 Catch:{ Exception -> 0x003e }
        r8.setBytes(r9, r1);	 Catch:{ Exception -> 0x003e }
        goto L_0x01c3;
    L_0x0104:
        r1 = r10 instanceof java.sql.Blob;	 Catch:{ Exception -> 0x003e }
        if (r1 == 0) goto L_0x0110;
    L_0x0108:
        r1 = r10;
        r1 = (java.sql.Blob) r1;	 Catch:{ Exception -> 0x003e }
        r8.setBlob(r9, r1);	 Catch:{ Exception -> 0x003e }
        goto L_0x01c3;
    L_0x0110:
        r2 = r10.toString();	 Catch:{ Exception -> 0x003e }
        r3 = r8.charConverter;	 Catch:{ Exception -> 0x003e }
        r4 = r8.charEncoding;	 Catch:{ Exception -> 0x003e }
        r1 = r8.connection;	 Catch:{ Exception -> 0x003e }
        r5 = r1.getServerCharset();	 Catch:{ Exception -> 0x003e }
        r1 = r8.connection;	 Catch:{ Exception -> 0x003e }
        r6 = r1.parserKnowsUnicode();	 Catch:{ Exception -> 0x003e }
        r7 = r8.getExceptionInterceptor();	 Catch:{ Exception -> 0x003e }
        r1 = com.mysql.jdbc.StringUtils.getBytes(r2, r3, r4, r5, r6, r7);	 Catch:{ Exception -> 0x003e }
        r8.setBytes(r9, r1);	 Catch:{ Exception -> 0x003e }
        goto L_0x01c3;
    L_0x0131:
        r8.setNumericObject(r9, r10, r11, r12);	 Catch:{ Exception -> 0x003e }
        goto L_0x01c3;
    L_0x0136:
        r8.setSerializableObject(r9, r10);	 Catch:{ Exception -> 0x003e }
        goto L_0x01c3;
    L_0x013b:
        r1 = r10 instanceof java.lang.Boolean;	 Catch:{ Exception -> 0x003e }
        if (r1 == 0) goto L_0x014b;
    L_0x013f:
        r1 = r10;
        r1 = (java.lang.Boolean) r1;	 Catch:{ Exception -> 0x003e }
        r1 = r1.booleanValue();	 Catch:{ Exception -> 0x003e }
        r8.setBoolean(r9, r1);	 Catch:{ Exception -> 0x003e }
        goto L_0x01c3;
    L_0x014b:
        r1 = r10 instanceof java.lang.String;	 Catch:{ Exception -> 0x003e }
        if (r1 == 0) goto L_0x016c;
    L_0x014f:
        r1 = "true";
        r2 = r10;
        r2 = (java.lang.String) r2;	 Catch:{ Exception -> 0x003e }
        r1 = r1.equalsIgnoreCase(r2);	 Catch:{ Exception -> 0x003e }
        if (r1 != 0) goto L_0x0168;
    L_0x015a:
        r1 = "0";
        r2 = r10;
        r2 = (java.lang.String) r2;	 Catch:{ Exception -> 0x003e }
        r1 = r1.equalsIgnoreCase(r2);	 Catch:{ Exception -> 0x003e }
        if (r1 != 0) goto L_0x0166;
    L_0x0165:
        goto L_0x0168;
    L_0x0166:
        r3 = r4;
    L_0x0168:
        r8.setBoolean(r9, r3);	 Catch:{ Exception -> 0x003e }
        goto L_0x01c3;
    L_0x016c:
        r1 = r10 instanceof java.lang.Number;	 Catch:{ Exception -> 0x003e }
        if (r1 == 0) goto L_0x017f;
    L_0x0170:
        r1 = r10;
        r1 = (java.lang.Number) r1;	 Catch:{ Exception -> 0x003e }
        r1 = r1.intValue();	 Catch:{ Exception -> 0x003e }
        if (r1 == 0) goto L_0x017a;
    L_0x0179:
        goto L_0x017b;
    L_0x017a:
        r3 = r4;
    L_0x017b:
        r8.setBoolean(r9, r3);	 Catch:{ Exception -> 0x003e }
        goto L_0x01c3;
    L_0x017f:
        r1 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x003e }
        r1.<init>();	 Catch:{ Exception -> 0x003e }
        r2 = "No conversion from ";
        r1.append(r2);	 Catch:{ Exception -> 0x003e }
        r2 = r10.getClass();	 Catch:{ Exception -> 0x003e }
        r2 = r2.getName();	 Catch:{ Exception -> 0x003e }
        r1.append(r2);	 Catch:{ Exception -> 0x003e }
        r2 = " to Types.BOOLEAN possible.";
        r1.append(r2);	 Catch:{ Exception -> 0x003e }
        r1 = r1.toString();	 Catch:{ Exception -> 0x003e }
        r2 = "S1009";
        r3 = r8.getExceptionInterceptor();	 Catch:{ Exception -> 0x003e }
        r1 = com.mysql.jdbc.SQLError.createSQLException(r1, r2, r3);	 Catch:{ Exception -> 0x003e }
        throw r1;	 Catch:{ Exception -> 0x003e }
    L_0x01a8:
        r1 = r10 instanceof java.math.BigDecimal;	 Catch:{ Exception -> 0x003e }
        if (r1 == 0) goto L_0x01bb;
    L_0x01ac:
        r1 = r10;
        r1 = (java.math.BigDecimal) r1;	 Catch:{ Exception -> 0x003e }
        r1 = com.mysql.jdbc.StringUtils.consistentToString(r1);	 Catch:{ Exception -> 0x003e }
        r1 = com.mysql.jdbc.StringUtils.fixDecimalExponent(r1);	 Catch:{ Exception -> 0x003e }
        r8.setString(r9, r1);	 Catch:{ Exception -> 0x003e }
        goto L_0x01c3;
    L_0x01bb:
        r1 = r10.toString();	 Catch:{ Exception -> 0x003e }
        r8.setString(r9, r1);	 Catch:{ Exception -> 0x003e }
    L_0x01c4:
        monitor-exit(r0);	 Catch:{ all -> 0x0012 }
        return;
        r2 = r1 instanceof java.sql.SQLException;	 Catch:{ all -> 0x0012 }
        if (r2 == 0) goto L_0x01cf;
    L_0x01cb:
        r2 = r1;
        r2 = (java.sql.SQLException) r2;	 Catch:{ all -> 0x0012 }
        throw r2;	 Catch:{ all -> 0x0012 }
    L_0x01cf:
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0012 }
        r2.<init>();	 Catch:{ all -> 0x0012 }
        r3 = "PreparedStatement.17";
        r3 = com.mysql.jdbc.Messages.getString(r3);	 Catch:{ all -> 0x0012 }
        r2.append(r3);	 Catch:{ all -> 0x0012 }
        r3 = r10.getClass();	 Catch:{ all -> 0x0012 }
        r3 = r3.toString();	 Catch:{ all -> 0x0012 }
        r2.append(r3);	 Catch:{ all -> 0x0012 }
        r3 = "PreparedStatement.18";
        r3 = com.mysql.jdbc.Messages.getString(r3);	 Catch:{ all -> 0x0012 }
        r2.append(r3);	 Catch:{ all -> 0x0012 }
        r3 = r1.getClass();	 Catch:{ all -> 0x0012 }
        r3 = r3.getName();	 Catch:{ all -> 0x0012 }
        r2.append(r3);	 Catch:{ all -> 0x0012 }
        r3 = "PreparedStatement.19";
        r3 = com.mysql.jdbc.Messages.getString(r3);	 Catch:{ all -> 0x0012 }
        r2.append(r3);	 Catch:{ all -> 0x0012 }
        r3 = r1.getMessage();	 Catch:{ all -> 0x0012 }
        r2.append(r3);	 Catch:{ all -> 0x0012 }
        r2 = r2.toString();	 Catch:{ all -> 0x0012 }
        r3 = "S1000";
        r4 = r8.getExceptionInterceptor();	 Catch:{ all -> 0x0012 }
        r2 = com.mysql.jdbc.SQLError.createSQLException(r2, r3, r4);	 Catch:{ all -> 0x0012 }
        r2.initCause(r1);	 Catch:{ all -> 0x0012 }
        throw r2;	 Catch:{ all -> 0x0012 }
    L_0x021e:
        monitor-exit(r0);	 Catch:{ all -> 0x0012 }
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.PreparedStatement.setObject(int, java.lang.Object, int, int):void");
    }

    protected int setOneBatchedParameterSet(java.sql.PreparedStatement batchedStatement, int batchedParamIndex, Object paramSet) throws SQLException {
        BatchParams paramArg = (BatchParams) paramSet;
        boolean[] isNullBatch = paramArg.isNull;
        boolean[] isStreamBatch = paramArg.isStream;
        int batchedParamIndex2 = batchedParamIndex;
        for (batchedParamIndex = 0; batchedParamIndex < isNullBatch.length; batchedParamIndex++) {
            int batchedParamIndex3;
            if (isNullBatch[batchedParamIndex]) {
                batchedParamIndex3 = batchedParamIndex2 + 1;
                batchedStatement.setNull(batchedParamIndex2, 0);
            } else if (isStreamBatch[batchedParamIndex]) {
                batchedParamIndex3 = batchedParamIndex2 + 1;
                batchedStatement.setBinaryStream(batchedParamIndex2, paramArg.parameterStreams[batchedParamIndex], paramArg.streamLengths[batchedParamIndex]);
            } else {
                int batchedParamIndex4 = batchedParamIndex2 + 1;
                ((PreparedStatement) batchedStatement).setBytesNoEscapeNoQuotes(batchedParamIndex2, paramArg.parameterStrings[batchedParamIndex]);
                batchedParamIndex2 = batchedParamIndex4;
            }
            batchedParamIndex2 = batchedParamIndex3;
        }
        return batchedParamIndex2;
    }

    public void setRef(int i, Ref x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    private final void setSerializableObject(int parameterIndex, Object parameterObj) throws SQLException {
        try {
            ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
            ObjectOutputStream objectOut = new ObjectOutputStream(bytesOut);
            objectOut.writeObject(parameterObj);
            objectOut.flush();
            objectOut.close();
            bytesOut.flush();
            bytesOut.close();
            byte[] buf = bytesOut.toByteArray();
            setBinaryStream(parameterIndex, new ByteArrayInputStream(buf), buf.length);
            this.parameterTypes[(parameterIndex - 1) + getParameterIndexOffset()] = -2;
        } catch (Exception ex) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Messages.getString("PreparedStatement.54"));
            stringBuilder.append(ex.getClass().getName());
            SQLException sqlEx = SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            sqlEx.initCause(ex);
            throw sqlEx;
        }
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        setInternal(parameterIndex, String.valueOf(x));
        this.parameterTypes[(parameterIndex - 1) + getParameterIndexOffset()] = 5;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setString(int r22, java.lang.String r23) throws java.sql.SQLException {
        /*
        r21 = this;
        r1 = r21;
        r2 = r22;
        r9 = r23;
        r3 = r21.checkClosed();
        r10 = r3.getConnectionMutex();
        monitor-enter(r10);
        r3 = 1;
        if (r9 != 0) goto L_0x001b;
    L_0x0012:
        r1.setNull(r2, r3);	 Catch:{ all -> 0x0017 }
        goto L_0x01cd;
    L_0x0017:
        r0 = move-exception;
        r3 = r0;
        goto L_0x01cf;
    L_0x001b:
        r21.checkClosed();	 Catch:{ all -> 0x0017 }
        r4 = r23.length();	 Catch:{ all -> 0x0017 }
        r11 = r4;
        r4 = r1.connection;	 Catch:{ all -> 0x0017 }
        r4 = r4.isNoBackslashEscapesSet();	 Catch:{ all -> 0x0017 }
        r5 = 39;
        if (r4 == 0) goto L_0x00a0;
    L_0x002d:
        r3 = r1.isEscapeNeededForString(r9, r11);	 Catch:{ all -> 0x0017 }
        r12 = r3;
        if (r12 != 0) goto L_0x0078;
    L_0x0034:
        r3 = 0;
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0017 }
        r6 = r23.length();	 Catch:{ all -> 0x0017 }
        r6 = r6 + 2;
        r4.<init>(r6);	 Catch:{ all -> 0x0017 }
        r4.append(r5);	 Catch:{ all -> 0x0017 }
        r4.append(r9);	 Catch:{ all -> 0x0017 }
        r4.append(r5);	 Catch:{ all -> 0x0017 }
        r5 = r1.isLoadDataQuery;	 Catch:{ all -> 0x0017 }
        if (r5 != 0) goto L_0x006b;
    L_0x004d:
        r13 = r4.toString();	 Catch:{ all -> 0x0017 }
        r14 = r1.charConverter;	 Catch:{ all -> 0x0017 }
        r15 = r1.charEncoding;	 Catch:{ all -> 0x0017 }
        r5 = r1.connection;	 Catch:{ all -> 0x0017 }
        r16 = r5.getServerCharset();	 Catch:{ all -> 0x0017 }
        r5 = r1.connection;	 Catch:{ all -> 0x0017 }
        r17 = r5.parserKnowsUnicode();	 Catch:{ all -> 0x0017 }
        r18 = r21.getExceptionInterceptor();	 Catch:{ all -> 0x0017 }
        r5 = com.mysql.jdbc.StringUtils.getBytes(r13, r14, r15, r16, r17, r18);	 Catch:{ all -> 0x0017 }
        r3 = r5;
        goto L_0x0074;
    L_0x006b:
        r5 = r4.toString();	 Catch:{ all -> 0x0017 }
        r5 = com.mysql.jdbc.StringUtils.getBytes(r5);	 Catch:{ all -> 0x0017 }
        r3 = r5;
    L_0x0074:
        r1.setInternal(r2, r3);	 Catch:{ all -> 0x0017 }
        goto L_0x009e;
    L_0x0078:
        r13 = 0;
        r3 = r1.isLoadDataQuery;	 Catch:{ all -> 0x0017 }
        if (r3 != 0) goto L_0x0097;
    L_0x007d:
        r4 = r1.charConverter;	 Catch:{ all -> 0x0017 }
        r5 = r1.charEncoding;	 Catch:{ all -> 0x0017 }
        r3 = r1.connection;	 Catch:{ all -> 0x0017 }
        r6 = r3.getServerCharset();	 Catch:{ all -> 0x0017 }
        r3 = r1.connection;	 Catch:{ all -> 0x0017 }
        r7 = r3.parserKnowsUnicode();	 Catch:{ all -> 0x0017 }
        r8 = r21.getExceptionInterceptor();	 Catch:{ all -> 0x0017 }
        r3 = r9;
        r3 = com.mysql.jdbc.StringUtils.getBytes(r3, r4, r5, r6, r7, r8);	 Catch:{ all -> 0x0017 }
        goto L_0x009b;
    L_0x0097:
        r3 = com.mysql.jdbc.StringUtils.getBytes(r23);	 Catch:{ all -> 0x0017 }
    L_0x009b:
        r1.setBytes(r2, r3);	 Catch:{ all -> 0x0017 }
    L_0x009e:
        monitor-exit(r10);	 Catch:{ all -> 0x0017 }
        return;
    L_0x00a0:
        r4 = r9;
        r6 = 1;
        r7 = r1.isLoadDataQuery;	 Catch:{ all -> 0x0017 }
        if (r7 != 0) goto L_0x00ac;
    L_0x00a6:
        r7 = r1.isEscapeNeededForString(r9, r11);	 Catch:{ all -> 0x0017 }
        if (r7 == 0) goto L_0x0175;
    L_0x00ac:
        r6 = 0;
        r7 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0017 }
        r8 = r23.length();	 Catch:{ all -> 0x0017 }
        r12 = (double) r8;	 Catch:{ all -> 0x0017 }
        r14 = 4607632778762754458; // 0x3ff199999999999a float:-1.5881868E-23 double:1.1;
        r12 = r12 * r14;
        r8 = (int) r12;	 Catch:{ all -> 0x0017 }
        r7.<init>(r8);	 Catch:{ all -> 0x0017 }
        r7.append(r5);	 Catch:{ all -> 0x0017 }
        r8 = 0;
        r12 = r8;
    L_0x00c3:
        if (r12 >= r11) goto L_0x016b;
    L_0x00c5:
        r13 = r9.charAt(r12);	 Catch:{ all -> 0x0017 }
        r14 = 92;
        if (r13 == 0) goto L_0x0159;
    L_0x00cd:
        r15 = 10;
        if (r13 == r15) goto L_0x014e;
    L_0x00d1:
        r15 = 13;
        if (r13 == r15) goto L_0x0143;
    L_0x00d5:
        r15 = 26;
        if (r13 == r15) goto L_0x0138;
    L_0x00d9:
        r15 = 34;
        if (r13 == r15) goto L_0x012b;
    L_0x00dd:
        if (r13 == r5) goto L_0x0121;
    L_0x00df:
        if (r13 == r14) goto L_0x0118;
    L_0x00e1:
        r15 = 165; // 0xa5 float:2.31E-43 double:8.15E-322;
        if (r13 == r15) goto L_0x00ee;
    L_0x00e5:
        r15 = 8361; // 0x20a9 float:1.1716E-41 double:4.131E-320;
        if (r13 == r15) goto L_0x00ee;
    L_0x00e9:
        r7.append(r13);	 Catch:{ all -> 0x0017 }
        goto L_0x0164;
    L_0x00ee:
        r15 = r1.charsetEncoder;	 Catch:{ all -> 0x0017 }
        if (r15 == 0) goto L_0x0114;
    L_0x00f2:
        r15 = java.nio.CharBuffer.allocate(r3);	 Catch:{ all -> 0x0017 }
        r16 = java.nio.ByteBuffer.allocate(r3);	 Catch:{ all -> 0x0017 }
        r19 = r16;
        r15.put(r13);	 Catch:{ all -> 0x0017 }
        r15.position(r8);	 Catch:{ all -> 0x0017 }
        r5 = r1.charsetEncoder;	 Catch:{ all -> 0x0017 }
        r14 = r19;
        r5.encode(r15, r14, r3);	 Catch:{ all -> 0x0017 }
        r5 = r14.get(r8);	 Catch:{ all -> 0x0017 }
        r3 = 92;
        if (r5 != r3) goto L_0x0114;
    L_0x0111:
        r7.append(r3);	 Catch:{ all -> 0x0017 }
    L_0x0114:
        r7.append(r13);	 Catch:{ all -> 0x0017 }
        goto L_0x0164;
    L_0x0118:
        r3 = 92;
        r7.append(r3);	 Catch:{ all -> 0x0017 }
        r7.append(r3);	 Catch:{ all -> 0x0017 }
        goto L_0x0164;
    L_0x0121:
        r3 = r14;
        r7.append(r3);	 Catch:{ all -> 0x0017 }
        r3 = 39;
        r7.append(r3);	 Catch:{ all -> 0x0017 }
        goto L_0x0164;
    L_0x012b:
        r3 = r1.usingAnsiMode;	 Catch:{ all -> 0x0017 }
        if (r3 == 0) goto L_0x0134;
    L_0x012f:
        r3 = 92;
        r7.append(r3);	 Catch:{ all -> 0x0017 }
    L_0x0134:
        r7.append(r15);	 Catch:{ all -> 0x0017 }
        goto L_0x0164;
    L_0x0138:
        r3 = 92;
        r7.append(r3);	 Catch:{ all -> 0x0017 }
        r3 = 90;
        r7.append(r3);	 Catch:{ all -> 0x0017 }
        goto L_0x0164;
    L_0x0143:
        r3 = 92;
        r7.append(r3);	 Catch:{ all -> 0x0017 }
        r3 = 114; // 0x72 float:1.6E-43 double:5.63E-322;
        r7.append(r3);	 Catch:{ all -> 0x0017 }
        goto L_0x0164;
    L_0x014e:
        r3 = 92;
        r7.append(r3);	 Catch:{ all -> 0x0017 }
        r3 = 110; // 0x6e float:1.54E-43 double:5.43E-322;
        r7.append(r3);	 Catch:{ all -> 0x0017 }
        goto L_0x0164;
    L_0x0159:
        r3 = 92;
        r7.append(r3);	 Catch:{ all -> 0x0017 }
        r3 = 48;
        r7.append(r3);	 Catch:{ all -> 0x0017 }
    L_0x0164:
        r12 = r12 + 1;
        r3 = 1;
        r5 = 39;
        goto L_0x00c3;
    L_0x016b:
        r3 = 39;
        r7.append(r3);	 Catch:{ all -> 0x0017 }
        r3 = r7.toString();	 Catch:{ all -> 0x0017 }
        r4 = r3;
    L_0x0175:
        r3 = 0;
        r5 = r1.isLoadDataQuery;	 Catch:{ all -> 0x0017 }
        if (r5 != 0) goto L_0x01b8;
    L_0x017a:
        if (r6 == 0) goto L_0x019d;
    L_0x017c:
        r13 = 39;
        r14 = 39;
        r15 = r1.charConverter;	 Catch:{ all -> 0x0017 }
        r5 = r1.charEncoding;	 Catch:{ all -> 0x0017 }
        r7 = r1.connection;	 Catch:{ all -> 0x0017 }
        r17 = r7.getServerCharset();	 Catch:{ all -> 0x0017 }
        r7 = r1.connection;	 Catch:{ all -> 0x0017 }
        r18 = r7.parserKnowsUnicode();	 Catch:{ all -> 0x0017 }
        r19 = r21.getExceptionInterceptor();	 Catch:{ all -> 0x0017 }
        r12 = r4;
        r16 = r5;
        r5 = com.mysql.jdbc.StringUtils.getBytesWrapped(r12, r13, r14, r15, r16, r17, r18, r19);	 Catch:{ all -> 0x0017 }
        r3 = r5;
        goto L_0x01bd;
    L_0x019d:
        r13 = r1.charConverter;	 Catch:{ all -> 0x0017 }
        r14 = r1.charEncoding;	 Catch:{ all -> 0x0017 }
        r5 = r1.connection;	 Catch:{ all -> 0x0017 }
        r15 = r5.getServerCharset();	 Catch:{ all -> 0x0017 }
        r5 = r1.connection;	 Catch:{ all -> 0x0017 }
        r16 = r5.parserKnowsUnicode();	 Catch:{ all -> 0x0017 }
        r17 = r21.getExceptionInterceptor();	 Catch:{ all -> 0x0017 }
        r12 = r4;
        r5 = com.mysql.jdbc.StringUtils.getBytes(r12, r13, r14, r15, r16, r17);	 Catch:{ all -> 0x0017 }
        r3 = r5;
        goto L_0x01bd;
    L_0x01b8:
        r5 = com.mysql.jdbc.StringUtils.getBytes(r4);	 Catch:{ all -> 0x0017 }
        r3 = r5;
    L_0x01bd:
        r1.setInternal(r2, r3);	 Catch:{ all -> 0x0017 }
        r5 = r1.parameterTypes;	 Catch:{ all -> 0x0017 }
        r7 = r2 + -1;
        r8 = r21.getParameterIndexOffset();	 Catch:{ all -> 0x0017 }
        r7 = r7 + r8;
        r8 = 12;
        r5[r7] = r8;	 Catch:{ all -> 0x0017 }
    L_0x01cd:
        monitor-exit(r10);	 Catch:{ all -> 0x0017 }
        return;
    L_0x01cf:
        monitor-exit(r10);	 Catch:{ all -> 0x0017 }
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.PreparedStatement.setString(int, java.lang.String):void");
    }

    private boolean isEscapeNeededForString(String x, int stringLength) {
        boolean needsHexEscape = false;
        for (int i = 0; i < stringLength; i++) {
            char c = x.charAt(i);
            if (c == '\u0000') {
                needsHexEscape = true;
            } else if (c == '\n') {
                needsHexEscape = true;
            } else if (c == '\r') {
                needsHexEscape = true;
            } else if (c == '\u001a') {
                needsHexEscape = true;
            } else if (c == '\"') {
                needsHexEscape = true;
            } else if (c == '\'') {
                needsHexEscape = true;
            } else if (c == '\\') {
                needsHexEscape = true;
            }
            if (needsHexEscape) {
                break;
            }
        }
        return needsHexEscape;
    }

    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            setTimeInternal(parameterIndex, x, cal, cal.getTimeZone(), true);
        }
    }

    public void setTime(int parameterIndex, Time x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            setTimeInternal(parameterIndex, x, null, this.connection.getDefaultTimeZone(), false);
        }
    }

    private void setTimeInternal(int parameterIndex, Time x, Calendar targetCalendar, TimeZone tz, boolean rollForward) throws SQLException {
        if (x == null) {
            setNull(parameterIndex, 92);
            return;
        }
        checkClosed();
        if (this.useLegacyDatetimeCode) {
            Calendar sessionCalendar = getCalendarInstanceForSessionOrNew();
            x = TimeUtil.changeTimezone(this.connection, sessionCalendar, targetCalendar, x, tz, this.connection.getServerTimezoneTZ(), rollForward);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("'");
            stringBuilder.append(x.toString());
            stringBuilder.append("'");
            setInternal(parameterIndex, stringBuilder.toString());
        } else {
            newSetTimeInternal(parameterIndex, x, targetCalendar);
        }
        this.parameterTypes[(parameterIndex - 1) + getParameterIndexOffset()] = 92;
    }

    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            setTimestampInternal(parameterIndex, x, cal, cal.getTimeZone(), true);
        }
    }

    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            setTimestampInternal(parameterIndex, x, null, this.connection.getDefaultTimeZone(), false);
        }
    }

    private void setTimestampInternal(int parameterIndex, Timestamp x, Calendar targetCalendar, TimeZone tz, boolean rollForward) throws SQLException {
        if (x == null) {
            setNull(parameterIndex, 93);
            return;
        }
        checkClosed();
        if (!this.sendFractionalSeconds) {
            x = TimeUtil.truncateFractionalSeconds(x);
        }
        if (this.useLegacyDatetimeCode) {
            x = TimeUtil.changeTimezone(this.connection, this.connection.getUseJDBCCompliantTimezoneShift() ? this.connection.getUtcCalendar() : getCalendarInstanceForSessionOrNew(), targetCalendar, x, tz, this.connection.getServerTimezoneTZ(), rollForward);
            if (this.connection.getUseSSPSCompatibleTimezoneShift()) {
                doSSPSCompatibleTimezoneShift(parameterIndex, x);
            } else {
                synchronized (this) {
                    if (this.tsdf == null) {
                        this.tsdf = new SimpleDateFormat("''yyyy-MM-dd HH:mm:ss", Locale.US);
                    }
                    StringBuffer buf = new StringBuffer();
                    buf.append(this.tsdf.format(x));
                    if (this.serverSupportsFracSecs) {
                        int nanos = x.getNanos();
                        if (nanos != 0) {
                            buf.append('.');
                            buf.append(TimeUtil.formatNanos(nanos, this.serverSupportsFracSecs, true));
                        }
                    }
                    buf.append('\'');
                    setInternal(parameterIndex, buf.toString());
                }
            }
        } else {
            newSetTimestampInternal(parameterIndex, x, targetCalendar);
        }
        this.parameterTypes[(parameterIndex - 1) + getParameterIndexOffset()] = 93;
    }

    private void newSetTimestampInternal(int parameterIndex, Timestamp x, Calendar targetCalendar) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.tsdf == null) {
                this.tsdf = new SimpleDateFormat("''yyyy-MM-dd HH:mm:ss", Locale.US);
            }
            if (targetCalendar != null) {
                this.tsdf.setTimeZone(targetCalendar.getTimeZone());
            } else {
                this.tsdf.setTimeZone(this.connection.getServerTimezoneTZ());
            }
            StringBuffer buf = new StringBuffer();
            buf.append(this.tsdf.format(x));
            buf.append('.');
            buf.append(TimeUtil.formatNanos(x.getNanos(), this.serverSupportsFracSecs, true));
            buf.append('\'');
            setInternal(parameterIndex, buf.toString());
        }
    }

    private void newSetTimeInternal(int parameterIndex, Time x, Calendar targetCalendar) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.tdf == null) {
                this.tdf = new SimpleDateFormat("''HH:mm:ss''", Locale.US);
            }
            if (targetCalendar != null) {
                this.tdf.setTimeZone(targetCalendar.getTimeZone());
            } else {
                this.tdf.setTimeZone(this.connection.getServerTimezoneTZ());
            }
            setInternal(parameterIndex, this.tdf.format(x));
        }
    }

    private void newSetDateInternal(int parameterIndex, Date x, Calendar targetCalendar) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.ddf == null) {
                this.ddf = new SimpleDateFormat("''yyyy-MM-dd''", Locale.US);
            }
            if (targetCalendar != null) {
                this.ddf.setTimeZone(targetCalendar.getTimeZone());
            } else if (this.connection.getNoTimezoneConversionForDateType()) {
                this.ddf.setTimeZone(this.connection.getDefaultTimeZone());
            } else {
                this.ddf.setTimeZone(this.connection.getServerTimezoneTZ());
            }
            setInternal(parameterIndex, this.ddf.format(x));
        }
    }

    private void doSSPSCompatibleTimezoneShift(int parameterIndex, Timestamp x) throws SQLException {
        int date;
        int i;
        Throwable th;
        Throwable th2;
        PreparedStatement this;
        Throwable th3;
        Throwable th4;
        Timestamp timestamp;
        PreparedStatement preparedStatement = this;
        synchronized (checkClosed().getConnectionMutex()) {
            try {
                Calendar sessionCalendar2 = preparedStatement.connection.getUseJDBCCompliantTimezoneShift() ? preparedStatement.connection.getUtcCalendar() : getCalendarInstanceForSessionOrNew();
                synchronized (sessionCalendar2) {
                    try {
                        java.util.Date oldTime = sessionCalendar2.getTime();
                        try {
                            sessionCalendar2.setTime(x);
                            int year = sessionCalendar2.get(1);
                            int month = sessionCalendar2.get(2) + 1;
                            date = sessionCalendar2.get(5);
                            int hour = sessionCalendar2.get(11);
                            int minute = sessionCalendar2.get(12);
                            int seconds = sessionCalendar2.get(13);
                            StringBuilder tsBuf = new StringBuilder();
                            tsBuf.append('\'');
                            tsBuf.append(year);
                            tsBuf.append("-");
                            if (month < 10) {
                                tsBuf.append('0');
                            }
                            tsBuf.append(month);
                            tsBuf.append('-');
                            if (date < 10) {
                                tsBuf.append('0');
                            }
                            tsBuf.append(date);
                            tsBuf.append(' ');
                            if (hour < 10) {
                                tsBuf.append('0');
                            }
                            tsBuf.append(hour);
                            tsBuf.append(':');
                            if (minute < 10) {
                                tsBuf.append('0');
                            }
                            tsBuf.append(minute);
                            tsBuf.append(':');
                            if (seconds < 10) {
                                tsBuf.append('0');
                            }
                            tsBuf.append(seconds);
                            tsBuf.append('.');
                            tsBuf.append(TimeUtil.formatNanos(x.getNanos(), preparedStatement.serverSupportsFracSecs, true));
                            tsBuf.append('\'');
                            i = parameterIndex;
                            try {
                                setInternal(i, tsBuf.toString());
                                try {
                                    sessionCalendar2.setTime(oldTime);
                                } catch (Throwable th5) {
                                    th = th5;
                                    th2 = th;
                                    this = preparedStatement;
                                    while (true) {
                                        try {
                                            break;
                                        } catch (Throwable th6) {
                                            th3 = th6;
                                        }
                                    }
                                    throw th2;
                                }
                            } catch (Throwable th7) {
                                th6 = th7;
                                th4 = th6;
                                this = preparedStatement;
                                date = i;
                                try {
                                    sessionCalendar2.setTime(oldTime);
                                    throw th4;
                                } catch (Throwable th62) {
                                    th2 = th62;
                                    i = date;
                                    while (true) {
                                        break;
                                    }
                                    throw th2;
                                }
                            }
                        } catch (Throwable th8) {
                            th62 = th8;
                            i = parameterIndex;
                            th4 = th62;
                            this = preparedStatement;
                            date = i;
                            sessionCalendar2.setTime(oldTime);
                            throw th4;
                        }
                    } catch (Throwable th9) {
                        th62 = th9;
                        timestamp = x;
                        th2 = th62;
                        this = preparedStatement;
                        while (true) {
                            break;
                        }
                        throw th2;
                    }
                    try {
                    } catch (Throwable th10) {
                        th62 = th10;
                        th3 = th62;
                        this = preparedStatement;
                        throw th3;
                    }
                }
            } catch (Throwable th11) {
                th62 = th11;
                i = parameterIndex;
                timestamp = x;
                th3 = th62;
                this = preparedStatement;
                throw th3;
            }
        }
    }

    @Deprecated
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        if (x == null) {
            setNull(parameterIndex, 12);
            return;
        }
        setBinaryStream(parameterIndex, x, length);
        this.parameterTypes[(parameterIndex - 1) + getParameterIndexOffset()] = 2005;
    }

    public void setURL(int parameterIndex, URL arg) throws SQLException {
        if (arg != null) {
            setString(parameterIndex, arg.toString());
            this.parameterTypes[(parameterIndex - 1) + getParameterIndexOffset()] = 70;
            return;
        }
        setNull(parameterIndex, 1);
    }

    private final void streamToBytes(Buffer packet, InputStream in, boolean escape, int streamLength, boolean useLength) throws SQLException {
        Throwable th;
        synchronized (checkClosed().getConnectionMutex()) {
            try {
                int bc;
                if (this.streamConvertBuf == null) {
                    this.streamConvertBuf = new byte[4096];
                }
                String connectionEncoding = this.connection.getEncoding();
                boolean hexEscape = false;
                if (this.connection.isNoBackslashEscapesSet() || (this.connection.getUseUnicode() && connectionEncoding != null && CharsetMapping.isMultibyteCharset(connectionEncoding) && !this.connection.parserKnowsUnicode())) {
                    hexEscape = true;
                }
                if (streamLength == -1) {
                    useLength = false;
                }
                if (useLength) {
                    bc = readblock(in, this.streamConvertBuf, streamLength);
                } else {
                    bc = readblock(in, this.streamConvertBuf);
                }
                int lengthLeftToRead = streamLength - bc;
                if (hexEscape) {
                    packet.writeStringNoNull("x");
                } else if (this.connection.getIO().versionMeetsMinimum(4, 1, 0)) {
                    packet.writeStringNoNull("_binary");
                }
                if (escape) {
                    packet.writeByte((byte) 39);
                }
                while (bc > 0) {
                    if (hexEscape) {
                        hexEscapeBlock(this.streamConvertBuf, packet, bc);
                    } else if (escape) {
                        escapeblockFast(this.streamConvertBuf, packet, bc);
                    } else {
                        packet.writeBytesNoNull(this.streamConvertBuf, 0, bc);
                    }
                    if (useLength) {
                        bc = readblock(in, this.streamConvertBuf, lengthLeftToRead);
                        if (bc > 0) {
                            lengthLeftToRead -= bc;
                        }
                    } else {
                        bc = readblock(in, this.streamConvertBuf);
                    }
                }
                if (escape) {
                    packet.writeByte((byte) 39);
                }
                try {
                    if (this.connection.getAutoClosePStmtStreams()) {
                        try {
                            in.close();
                        } catch (IOException e) {
                        }
                    }
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    PreparedStatement preparedStatement = this;
                    th = th3;
                    throw th;
                }
            } catch (RuntimeException ex) {
                SQLException sqlEx = SQLError.createSQLException(ex.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, null);
                sqlEx.initCause(ex);
                throw sqlEx;
            } catch (Throwable th4) {
                try {
                    if (this.connection.getAutoClosePStmtStreams()) {
                        try {
                            in.close();
                        } catch (IOException e2) {
                        }
                    }
                } catch (Throwable th5) {
                    th = th5;
                    throw th;
                }
            }
        }
    }

    private final byte[] streamToBytes(InputStream in, boolean escape, int streamLength, boolean useLength) throws SQLException {
        PreparedStatement this;
        PreparedStatement this2;
        Throwable th;
        synchronized (checkClosed().getConnectionMutex()) {
            try {
                byte[] toByteArray;
                in.mark(ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED);
                try {
                    int bc;
                    if (this.streamConvertBuf == null) {
                        this.streamConvertBuf = new byte[4096];
                    }
                    if (streamLength == -1) {
                        useLength = false;
                    }
                    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
                    if (useLength) {
                        bc = readblock(in, this.streamConvertBuf, streamLength);
                    } else {
                        bc = readblock(in, this.streamConvertBuf);
                    }
                    int lengthLeftToRead = streamLength - bc;
                    if (escape) {
                        if (this.connection.versionMeetsMinimum(4, 1, 0)) {
                            bytesOut.write(95);
                            bytesOut.write(98);
                            bytesOut.write(105);
                            bytesOut.write(110);
                            bytesOut.write(97);
                            bytesOut.write(114);
                            bytesOut.write(121);
                        }
                        bytesOut.write(39);
                    }
                    while (bc > 0) {
                        if (escape) {
                            escapeblockFast(this.streamConvertBuf, bytesOut, bc);
                        } else {
                            bytesOut.write(this.streamConvertBuf, 0, bc);
                        }
                        if (useLength) {
                            bc = readblock(in, this.streamConvertBuf, lengthLeftToRead);
                            if (bc > 0) {
                                lengthLeftToRead -= bc;
                            }
                        } else {
                            bc = readblock(in, this.streamConvertBuf);
                        }
                    }
                    if (escape) {
                        bytesOut.write(39);
                    }
                    toByteArray = bytesOut.toByteArray();
                    this = this;
                    try {
                        in.reset();
                    } catch (IOException e) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
                try {
                    if (this.connection.getAutoClosePStmtStreams()) {
                        try {
                            in.close();
                        } catch (IOException e2) {
                        }
                    }
                    return toByteArray;
                } catch (Throwable th3) {
                    th = th3;
                    this2 = this;
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                this2 = this;
                throw th;
            }
        }
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
        buf.append(": ");
        try {
            buf.append(asSql());
        } catch (SQLException sqlEx) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("EXCEPTION: ");
            stringBuilder.append(sqlEx.toString());
            buf.append(stringBuilder.toString());
        }
        return buf.toString();
    }

    protected int getParameterIndexOffset() {
        return 0;
    }

    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        setAsciiStream(parameterIndex, x, -1);
    }

    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        setAsciiStream(parameterIndex, x, (int) length);
        this.parameterTypes[(parameterIndex - 1) + getParameterIndexOffset()] = 2005;
    }

    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        setBinaryStream(parameterIndex, x, -1);
    }

    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        setBinaryStream(parameterIndex, x, (int) length);
    }

    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        setBinaryStream(parameterIndex, inputStream);
    }

    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        setCharacterStream(parameterIndex, reader, -1);
    }

    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        setCharacterStream(parameterIndex, reader, (int) length);
    }

    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        setCharacterStream(parameterIndex, reader);
    }

    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        setCharacterStream(parameterIndex, reader, length);
    }

    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        setNCharacterStream(parameterIndex, value, -1);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setNString(int r12, java.lang.String r13) throws java.sql.SQLException {
        /*
        r11 = this;
        r0 = r11.checkClosed();
        r0 = r0.getConnectionMutex();
        monitor-enter(r0);
        r1 = r11.charEncoding;	 Catch:{ all -> 0x00f3 }
        r2 = "UTF-8";
        r1 = r1.equalsIgnoreCase(r2);	 Catch:{ all -> 0x00f3 }
        if (r1 != 0) goto L_0x00ee;
    L_0x0013:
        r1 = r11.charEncoding;	 Catch:{ all -> 0x00f3 }
        r2 = "utf8";
        r1 = r1.equalsIgnoreCase(r2);	 Catch:{ all -> 0x00f3 }
        if (r1 == 0) goto L_0x001f;
    L_0x001d:
        goto L_0x00ee;
    L_0x001f:
        if (r13 != 0) goto L_0x0027;
    L_0x0021:
        r1 = 1;
        r11.setNull(r12, r1);	 Catch:{ all -> 0x00f3 }
        goto L_0x00ec;
    L_0x0027:
        r1 = r13.length();	 Catch:{ all -> 0x00f3 }
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00f3 }
        r3 = r13.length();	 Catch:{ all -> 0x00f3 }
        r3 = (double) r3;	 Catch:{ all -> 0x00f3 }
        r5 = 4607632778762754458; // 0x3ff199999999999a float:-1.5881868E-23 double:1.1;
        r3 = r3 * r5;
        r5 = 4616189618054758400; // 0x4010000000000000 float:0.0 double:4.0;
        r3 = r3 + r5;
        r3 = (int) r3;	 Catch:{ all -> 0x00f3 }
        r2.<init>(r3);	 Catch:{ all -> 0x00f3 }
        r3 = "_utf8";
        r2.append(r3);	 Catch:{ all -> 0x00f3 }
        r3 = 39;
        r2.append(r3);	 Catch:{ all -> 0x00f3 }
        r4 = 0;
    L_0x004a:
        if (r4 >= r1) goto L_0x00ac;
    L_0x004c:
        r5 = r13.charAt(r4);	 Catch:{ all -> 0x00f3 }
        r6 = 92;
        if (r5 == 0) goto L_0x00a0;
    L_0x0054:
        r7 = 10;
        if (r5 == r7) goto L_0x0097;
    L_0x0058:
        r7 = 13;
        if (r5 == r7) goto L_0x008e;
    L_0x005c:
        r7 = 26;
        if (r5 == r7) goto L_0x0085;
    L_0x0060:
        r7 = 34;
        if (r5 == r7) goto L_0x007a;
    L_0x0064:
        if (r5 == r3) goto L_0x0073;
    L_0x0066:
        if (r5 == r6) goto L_0x006c;
    L_0x0068:
        r2.append(r5);	 Catch:{ all -> 0x00f3 }
        goto L_0x00a9;
    L_0x006c:
        r2.append(r6);	 Catch:{ all -> 0x00f3 }
        r2.append(r6);	 Catch:{ all -> 0x00f3 }
        goto L_0x00a9;
    L_0x0073:
        r2.append(r6);	 Catch:{ all -> 0x00f3 }
        r2.append(r3);	 Catch:{ all -> 0x00f3 }
        goto L_0x00a9;
    L_0x007a:
        r8 = r11.usingAnsiMode;	 Catch:{ all -> 0x00f3 }
        if (r8 == 0) goto L_0x0081;
    L_0x007e:
        r2.append(r6);	 Catch:{ all -> 0x00f3 }
    L_0x0081:
        r2.append(r7);	 Catch:{ all -> 0x00f3 }
        goto L_0x00a9;
    L_0x0085:
        r2.append(r6);	 Catch:{ all -> 0x00f3 }
        r6 = 90;
        r2.append(r6);	 Catch:{ all -> 0x00f3 }
        goto L_0x00a9;
    L_0x008e:
        r2.append(r6);	 Catch:{ all -> 0x00f3 }
        r6 = 114; // 0x72 float:1.6E-43 double:5.63E-322;
        r2.append(r6);	 Catch:{ all -> 0x00f3 }
        goto L_0x00a9;
    L_0x0097:
        r2.append(r6);	 Catch:{ all -> 0x00f3 }
        r6 = 110; // 0x6e float:1.54E-43 double:5.43E-322;
        r2.append(r6);	 Catch:{ all -> 0x00f3 }
        goto L_0x00a9;
    L_0x00a0:
        r2.append(r6);	 Catch:{ all -> 0x00f3 }
        r6 = 48;
        r2.append(r6);	 Catch:{ all -> 0x00f3 }
    L_0x00a9:
        r4 = r4 + 1;
        goto L_0x004a;
    L_0x00ac:
        r2.append(r3);	 Catch:{ all -> 0x00f3 }
        r3 = r2.toString();	 Catch:{ all -> 0x00f3 }
        r10 = 0;
        r4 = r11.isLoadDataQuery;	 Catch:{ all -> 0x00f3 }
        if (r4 != 0) goto L_0x00d8;
    L_0x00b8:
        r4 = r11.connection;	 Catch:{ all -> 0x00f3 }
        r5 = "UTF-8";
        r5 = r4.getCharsetConverter(r5);	 Catch:{ all -> 0x00f3 }
        r6 = "UTF-8";
        r4 = r11.connection;	 Catch:{ all -> 0x00f3 }
        r7 = r4.getServerCharset();	 Catch:{ all -> 0x00f3 }
        r4 = r11.connection;	 Catch:{ all -> 0x00f3 }
        r8 = r4.parserKnowsUnicode();	 Catch:{ all -> 0x00f3 }
        r9 = r11.getExceptionInterceptor();	 Catch:{ all -> 0x00f3 }
        r4 = r3;
        r4 = com.mysql.jdbc.StringUtils.getBytes(r4, r5, r6, r7, r8, r9);	 Catch:{ all -> 0x00f3 }
        goto L_0x00dc;
    L_0x00d8:
        r4 = com.mysql.jdbc.StringUtils.getBytes(r3);	 Catch:{ all -> 0x00f3 }
    L_0x00dc:
        r11.setInternal(r12, r4);	 Catch:{ all -> 0x00f3 }
        r5 = r11.parameterTypes;	 Catch:{ all -> 0x00f3 }
        r6 = r12 + -1;
        r7 = r11.getParameterIndexOffset();	 Catch:{ all -> 0x00f3 }
        r6 = r6 + r7;
        r7 = -9;
        r5[r6] = r7;	 Catch:{ all -> 0x00f3 }
    L_0x00ec:
        monitor-exit(r0);	 Catch:{ all -> 0x00f3 }
        return;
    L_0x00ee:
        r11.setString(r12, r13);	 Catch:{ all -> 0x00f3 }
        monitor-exit(r0);	 Catch:{ all -> 0x00f3 }
        return;
    L_0x00f3:
        r1 = move-exception;
        monitor-exit(r0);	 Catch:{ all -> 0x00f3 }
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.PreparedStatement.setNString(int, java.lang.String):void");
    }

    public void setNCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        IOException ioEx;
        synchronized (checkClosed().getConnectionMutex()) {
            if (reader == null) {
                try {
                    setNull(parameterIndex, -1);
                } catch (IOException ioEx2) {
                    throw SQLError.createSQLException(ioEx2.toString(), SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                }
            }
            if (!this.connection.getUseStreamLengthsInPrepStmts() || length == -1) {
                char[] c = new char[4096];
                StringBuilder buf = new StringBuilder();
                while (true) {
                    int read = reader.read(c);
                    int len = read;
                    if (read == -1) {
                        break;
                    }
                    buf.append(c, 0, len);
                }
                setNString(parameterIndex, buf.toString());
                ioEx2 = c;
            } else {
                ioEx2 = new char[((int) length)];
                setNString(parameterIndex, new String(ioEx2, 0, readFully(reader, ioEx2, (int) length)));
            }
            this.parameterTypes[(parameterIndex - 1) + getParameterIndexOffset()] = 2011;
        }
    }

    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        setNCharacterStream(parameterIndex, reader);
    }

    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        if (reader == null) {
            setNull(parameterIndex, -1);
        } else {
            setNCharacterStream(parameterIndex, reader, length);
        }
    }

    public ParameterBindings getParameterBindings() throws SQLException {
        ParameterBindings emulatedPreparedStatementBindings;
        synchronized (checkClosed().getConnectionMutex()) {
            emulatedPreparedStatementBindings = new EmulatedPreparedStatementBindings();
        }
        return emulatedPreparedStatementBindings;
    }

    public String getPreparedSql() {
        try {
            synchronized (checkClosed().getConnectionMutex()) {
                String str;
                if (this.rewrittenBatchSize == 0) {
                    str = this.originalSql;
                    return str;
                }
                try {
                    str = this.parseInfo.getSqlForBatch(this.parseInfo);
                    return str;
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (SQLException e2) {
            throw new RuntimeException(e2);
        }
    }

    public int getUpdateCount() throws SQLException {
        int count = super.getUpdateCount();
        if (!containsOnDuplicateKeyUpdateInSQL() || !this.compensateForOnDuplicateKeyUpdate) {
            return count;
        }
        if (count == 2 || count == 0) {
            return 1;
        }
        return count;
    }

    protected static boolean canRewrite(String sql, boolean isOnDuplicateKeyUpdate, int locationOfOnDuplicateKeyUpdate, int statementStartPos) {
        boolean z = true;
        if (StringUtils.startsWithIgnoreCaseAndWs(sql, "INSERT", statementStartPos)) {
            if (StringUtils.indexOfIgnoreCase(statementStartPos, sql, "SELECT", "\"'`", "\"'`", StringUtils.SEARCH_MODE__MRK_COM_WS) != -1) {
                return false;
            }
            if (isOnDuplicateKeyUpdate) {
                int updateClausePos = StringUtils.indexOfIgnoreCase(locationOfOnDuplicateKeyUpdate, sql, " UPDATE ");
                if (updateClausePos != -1) {
                    if (StringUtils.indexOfIgnoreCase(updateClausePos, sql, "LAST_INSERT_ID", "\"'`", "\"'`", StringUtils.SEARCH_MODE__MRK_COM_WS) != -1) {
                        z = false;
                    }
                    return z;
                }
            }
            return true;
        }
        if (StringUtils.startsWithIgnoreCaseAndWs(sql, "REPLACE", statementStartPos)) {
            if (StringUtils.indexOfIgnoreCase(statementStartPos, sql, "SELECT", "\"'`", "\"'`", StringUtils.SEARCH_MODE__MRK_COM_WS) == -1) {
                return z;
            }
        }
        z = false;
        return z;
    }

    public long executeLargeUpdate() throws SQLException {
        return executeUpdateInternal(true, false);
    }
}
