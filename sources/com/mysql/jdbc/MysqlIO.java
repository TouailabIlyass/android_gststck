package com.mysql.jdbc;

import android.support.v4.internal.view.SupportMenu;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import com.mysql.jdbc.authentication.MysqlClearPasswordPlugin;
import com.mysql.jdbc.authentication.MysqlNativePasswordPlugin;
import com.mysql.jdbc.authentication.MysqlOldPasswordPlugin;
import com.mysql.jdbc.authentication.Sha256PasswordPlugin;
import com.mysql.jdbc.exceptions.MySQLStatementCancelledException;
import com.mysql.jdbc.exceptions.MySQLTimeoutException;
import com.mysql.jdbc.log.Log;
import com.mysql.jdbc.log.LogUtils;
import com.mysql.jdbc.profiler.ProfilerEvent;
import com.mysql.jdbc.profiler.ProfilerEventHandler;
import com.mysql.jdbc.util.ReadAheadInputStream;
import com.mysql.jdbc.util.ResultSetUtil;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.Deflater;

public class MysqlIO {
    protected static final int AUTH_411_OVERHEAD = 33;
    private static final int CLIENT_CAN_HANDLE_EXPIRED_PASSWORD = 4194304;
    private static final int CLIENT_COMPRESS = 32;
    private static final int CLIENT_CONNECT_ATTRS = 1048576;
    protected static final int CLIENT_CONNECT_WITH_DB = 8;
    private static final int CLIENT_DEPRECATE_EOF = 16777216;
    private static final int CLIENT_FOUND_ROWS = 2;
    private static final int CLIENT_INTERACTIVE = 1024;
    private static final int CLIENT_LOCAL_FILES = 128;
    private static final int CLIENT_LONG_FLAG = 4;
    private static final int CLIENT_LONG_PASSWORD = 1;
    private static final int CLIENT_MULTI_RESULTS = 131072;
    private static final int CLIENT_MULTI_STATEMENTS = 65536;
    private static final int CLIENT_PLUGIN_AUTH = 524288;
    private static final int CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA = 2097152;
    private static final int CLIENT_PROTOCOL_41 = 512;
    protected static final int CLIENT_RESERVED = 16384;
    protected static final int CLIENT_SECURE_CONNECTION = 32768;
    private static final int CLIENT_SESSION_TRACK = 8388608;
    protected static final int CLIENT_SSL = 2048;
    private static final int CLIENT_TRANSACTIONS = 8192;
    private static final String CODE_PAGE_1252 = "Cp1252";
    protected static final int COMP_HEADER_LENGTH = 3;
    private static final String EXPLAINABLE_STATEMENT = "SELECT";
    private static final String[] EXPLAINABLE_STATEMENT_EXTENSION = new String[]{"INSERT", "UPDATE", "REPLACE", "DELETE"};
    private static final String FALSE_SCRAMBLE = "xxxxxxxx";
    protected static final int HEADER_LENGTH = 4;
    protected static final int INITIAL_PACKET_SIZE = 1024;
    private static final int MAX_PACKET_DUMP_LENGTH = 1024;
    protected static final int MAX_QUERY_SIZE_TO_EXPLAIN = 1048576;
    protected static final int MAX_QUERY_SIZE_TO_LOG = 1024;
    protected static final int MIN_COMPRESS_LEN = 50;
    private static final String NONE = "none";
    protected static final int NULL_LENGTH = -1;
    public static final int SEED_LENGTH = 20;
    static final int SERVER_MORE_RESULTS_EXISTS = 8;
    private static final int SERVER_QUERY_NO_GOOD_INDEX_USED = 16;
    private static final int SERVER_QUERY_NO_INDEX_USED = 32;
    private static final int SERVER_QUERY_WAS_SLOW = 2048;
    private static final int SERVER_STATUS_AUTOCOMMIT = 2;
    private static final int SERVER_STATUS_CURSOR_EXISTS = 64;
    private static final int SERVER_STATUS_IN_TRANS = 1;
    protected static final String ZERO_DATETIME_VALUE_MARKER = "0000-00-00 00:00:00";
    protected static final String ZERO_DATE_VALUE_MARKER = "0000-00-00";
    private static String jvmPlatformCharset;
    private static int maxBufferSize = SupportMenu.USER_MASK;
    private int authPluginDataLength = 0;
    private Map<String, AuthenticationPlugin> authenticationPlugins = null;
    private boolean autoGenerateTestcaseScript;
    private boolean checkPacketSequence = false;
    private String clientDefaultAuthenticationPlugin = null;
    private String clientDefaultAuthenticationPluginName = null;
    protected long clientParam = 0;
    private boolean colDecimalNeedsBump = false;
    private int commandCount = 0;
    private SoftReference<Buffer> compressBufRef;
    private byte compressedPacketSequence = (byte) 0;
    protected MySQLConnection connection;
    private Deflater deflater = null;
    private List<String> disabledAuthenticationPlugins = null;
    private boolean enablePacketDebug = false;
    private ExceptionInterceptor exceptionInterceptor;
    private boolean hadWarnings = false;
    private boolean has41NewNewProt = false;
    private boolean hasLongColumnInfo = false;
    protected String host = null;
    private boolean isInteractiveClient = false;
    protected long lastPacketReceivedTimeMs = 0;
    protected long lastPacketSentTimeMs = 0;
    private SoftReference<Buffer> loadFileBufRef;
    private boolean logSlowQueries = false;
    private int maxAllowedPacket = 1048576;
    protected int maxThreeBytes = 16581375;
    public Socket mysqlConnection = null;
    protected InputStream mysqlInput = null;
    protected BufferedOutputStream mysqlOutput = null;
    private boolean needToGrabQueryFromPacket;
    private int oldServerStatus = 0;
    private LinkedList<StringBuilder> packetDebugRingBuffer = null;
    private byte[] packetHeaderBuf = new byte[4];
    private byte packetSequence = (byte) 0;
    private boolean packetSequenceReset = false;
    private boolean platformDbCharsetMatches = true;
    protected int port = 3306;
    private boolean profileSql = false;
    private byte protocolVersion = (byte) 0;
    private boolean queryBadIndexUsed = false;
    private boolean queryNoIndexUsed = false;
    private String queryTimingUnits;
    private byte readPacketSequence = (byte) -1;
    private Buffer reusablePacket = null;
    protected String seed;
    private Buffer sendPacket = null;
    protected int serverCapabilities;
    protected int serverCharsetIndex;
    private String serverDefaultAuthenticationPluginName = null;
    private int serverMajorVersion = 0;
    private int serverMinorVersion = 0;
    private boolean serverQueryWasSlow = false;
    private int serverStatus = 0;
    private int serverSubMinorVersion = 0;
    private String serverVersion = null;
    private Buffer sharedSendPacket = null;
    private long slowQueryThreshold;
    protected SocketFactory socketFactory = null;
    private String socketFactoryClassName = null;
    private SoftReference<Buffer> splitBufRef;
    private int statementExecutionDepth = 0;
    private List<StatementInterceptorV2> statementInterceptors;
    private RowData streamingData = null;
    private long threadId;
    private boolean traceProtocol = false;
    private boolean use41Extensions = false;
    private boolean useAutoSlowLog;
    private int useBufferRowSizeThreshold;
    private boolean useCompression = false;
    private boolean useConnectWithDb;
    private boolean useDirectRowUnpack = true;
    private boolean useNanosForElapsedTime;
    private boolean useNewLargePackets = false;
    private boolean useNewUpdateCounts = false;
    private int warningCount = 0;

    static {
        OutputStreamWriter outWriter = null;
        jvmPlatformCharset = null;
        try {
            outWriter = new OutputStreamWriter(new ByteArrayOutputStream());
            jvmPlatformCharset = outWriter.getEncoding();
            if (outWriter != null) {
                try {
                    outWriter.close();
                } catch (IOException e) {
                }
            }
        } catch (Throwable th) {
            if (outWriter != null) {
                try {
                    outWriter.close();
                } catch (IOException e2) {
                }
            }
        }
    }

    public MysqlIO(String host, int port, Properties props, String socketFactoryClassName, MySQLConnection conn, int socketTimeout, int useBufferRowSizeThreshold) throws IOException, SQLException {
        boolean z = false;
        this.connection = conn;
        if (this.connection.getEnablePacketDebug()) {
            this.packetDebugRingBuffer = new LinkedList();
        }
        this.traceProtocol = this.connection.getTraceProtocol();
        this.useAutoSlowLog = this.connection.getAutoSlowLog();
        this.useBufferRowSizeThreshold = useBufferRowSizeThreshold;
        this.useDirectRowUnpack = this.connection.getUseDirectRowUnpack();
        this.logSlowQueries = this.connection.getLogSlowQueries();
        this.reusablePacket = new Buffer(1024);
        this.sendPacket = new Buffer(1024);
        this.port = port;
        this.host = host;
        this.socketFactoryClassName = socketFactoryClassName;
        this.socketFactory = createSocketFactory();
        this.exceptionInterceptor = this.connection.getExceptionInterceptor();
        try {
            this.mysqlConnection = this.socketFactory.connect(this.host, this.port, props);
            if (socketTimeout != 0) {
                try {
                    this.mysqlConnection.setSoTimeout(socketTimeout);
                } catch (Exception e) {
                }
            }
            this.mysqlConnection = this.socketFactory.beforeHandshake();
            if (this.connection.getUseReadAheadInput()) {
                this.mysqlInput = new ReadAheadInputStream(this.mysqlConnection.getInputStream(), 16384, this.connection.getTraceProtocol(), this.connection.getLog());
            } else if (this.connection.useUnbufferedInput()) {
                this.mysqlInput = this.mysqlConnection.getInputStream();
            } else {
                this.mysqlInput = new BufferedInputStream(this.mysqlConnection.getInputStream(), 16384);
            }
            this.mysqlOutput = new BufferedOutputStream(this.mysqlConnection.getOutputStream(), 16384);
            this.isInteractiveClient = this.connection.getInteractiveClient();
            this.profileSql = this.connection.getProfileSql();
            this.autoGenerateTestcaseScript = this.connection.getAutoGenerateTestcaseScript();
            if (!(this.profileSql || this.logSlowQueries)) {
                if (!this.autoGenerateTestcaseScript) {
                    this.needToGrabQueryFromPacket = z;
                    if (this.connection.getUseNanosForElapsedTime() || !TimeUtil.nanoTimeAvailable()) {
                        this.queryTimingUnits = Messages.getString("Milliseconds");
                    } else {
                        this.useNanosForElapsedTime = true;
                        this.queryTimingUnits = Messages.getString("Nanoseconds");
                    }
                    if (this.connection.getLogSlowQueries()) {
                        calculateSlowQueryThreshold();
                    }
                }
            }
            z = true;
            this.needToGrabQueryFromPacket = z;
            if (this.connection.getUseNanosForElapsedTime()) {
            }
            this.queryTimingUnits = Messages.getString("Milliseconds");
            if (this.connection.getLogSlowQueries()) {
                calculateSlowQueryThreshold();
            }
        } catch (IOException e2) {
            throw SQLError.createCommunicationsException(this.connection, 0, 0, e2, getExceptionInterceptor());
        }
    }

    public boolean hasLongColumnInfo() {
        return this.hasLongColumnInfo;
    }

    protected boolean isDataAvailable() throws SQLException {
        try {
            return this.mysqlInput.available() > 0;
        } catch (IOException e) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, e, getExceptionInterceptor());
        }
    }

    protected long getLastPacketSentTimeMs() {
        return this.lastPacketSentTimeMs;
    }

    protected long getLastPacketReceivedTimeMs() {
        return this.lastPacketReceivedTimeMs;
    }

    protected ResultSetImpl getResultSet(StatementImpl callingStatement, long columnCount, int maxRows, int resultSetType, int resultSetConcurrency, boolean streamResults, String catalog, boolean isBinaryEncoded, Field[] metadataFromCache) throws SQLException {
        RowData rowData;
        MysqlIO mysqlIO = this;
        long j = columnCount;
        boolean z = isBinaryEncoded;
        Field[] fields = null;
        boolean z2 = false;
        int i;
        if (metadataFromCache == null) {
            fields = new Field[((int) j)];
            for (i = 0; ((long) i) < j; i++) {
                fields[i] = unpackField(readPacket(), false);
            }
        } else {
            for (i = 0; ((long) i) < j; i++) {
                skipPacket();
            }
        }
        Field[] fields2 = fields;
        if (!isEOFDeprecated() || (mysqlIO.connection.versionMeetsMinimum(5, 0, 2) && callingStatement != null && z && callingStatement.isCursorRequired())) {
            readServerStatusForResultSets(reuseAndReadPacket(mysqlIO.reusablePacket));
        }
        if (mysqlIO.connection.versionMeetsMinimum(5, 0, 2) && mysqlIO.connection.getUseCursorFetch() && z && callingStatement != null && callingStatement.getFetchSize() != 0 && callingStatement.getResultSetType() == 1003) {
            ServerPreparedStatement prepStmt = (ServerPreparedStatement) callingStatement;
            boolean usingCursor = true;
            if (mysqlIO.connection.versionMeetsMinimum(5, 0, 5)) {
                if ((mysqlIO.serverStatus & 64) != 0) {
                    z2 = true;
                }
                usingCursor = z2;
            }
            boolean usingCursor2 = usingCursor;
            if (usingCursor2) {
                ResultSetImpl rs = buildResultSetWithRows(callingStatement, catalog, fields2, new RowDataCursor(mysqlIO, prepStmt, fields2), resultSetType, resultSetConcurrency, z);
                if (usingCursor2) {
                    rs.setFetchSize(callingStatement.getFetchSize());
                }
                return rs;
            }
        }
        if (streamResults) {
            rowData = new RowDataDynamic(mysqlIO, (int) j, metadataFromCache == null ? fields2 : metadataFromCache, z);
            mysqlIO.streamingData = rowData;
        } else {
            rowData = readSingleRowSet(j, maxRows, resultSetConcurrency, z, metadataFromCache == null ? fields2 : metadataFromCache);
        }
        return buildResultSetWithRows(callingStatement, catalog, metadataFromCache == null ? fields2 : metadataFromCache, rowData, resultSetType, resultSetConcurrency, z);
    }

    protected NetworkResources getNetworkResources() {
        return new NetworkResources(this.mysqlConnection, this.mysqlInput, this.mysqlOutput);
    }

    protected final void forceClose() {
        try {
            getNetworkResources().forceClose();
        } finally {
            this.mysqlConnection = null;
            this.mysqlInput = null;
            this.mysqlOutput = null;
        }
    }

    protected final void skipPacket() throws SQLException {
        try {
            if (readFully(this.mysqlInput, this.packetHeaderBuf, 0, 4) < 4) {
                forceClose();
                throw new IOException(Messages.getString("MysqlIO.1"));
            }
            int packetLength = ((this.packetHeaderBuf[0] & 255) + ((this.packetHeaderBuf[1] & 255) << 8)) + ((this.packetHeaderBuf[2] & 255) << 16);
            if (this.traceProtocol) {
                StringBuilder traceMessageBuf = new StringBuilder();
                traceMessageBuf.append(Messages.getString("MysqlIO.2"));
                traceMessageBuf.append(packetLength);
                traceMessageBuf.append(Messages.getString("MysqlIO.3"));
                traceMessageBuf.append(StringUtils.dumpAsHex(this.packetHeaderBuf, 4));
                this.connection.getLog().logTrace(traceMessageBuf.toString());
            }
            byte multiPacketSeq = this.packetHeaderBuf[3];
            if (this.packetSequenceReset) {
                this.packetSequenceReset = false;
            } else if (this.enablePacketDebug && this.checkPacketSequence) {
                checkPacketSequencing(multiPacketSeq);
            }
            this.readPacketSequence = multiPacketSeq;
            skipFully(this.mysqlInput, (long) packetLength);
        } catch (IOException e) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, e, getExceptionInterceptor());
        } catch (OutOfMemoryError oom) {
            try {
                this.connection.realClose(false, false, true, oom);
            } catch (Exception e2) {
            }
            throw oom;
        }
    }

    protected final Buffer readPacket() throws SQLException {
        try {
            if (readFully(this.mysqlInput, this.packetHeaderBuf, 0, 4) < 4) {
                forceClose();
                throw new IOException(Messages.getString("MysqlIO.1"));
            }
            int packetLength = ((this.packetHeaderBuf[0] & 255) + ((this.packetHeaderBuf[1] & 255) << 8)) + ((this.packetHeaderBuf[2] & 255) << 16);
            if (packetLength > this.maxAllowedPacket) {
                throw new PacketTooBigException((long) packetLength, (long) this.maxAllowedPacket);
            }
            if (this.traceProtocol) {
                StringBuilder traceMessageBuf = new StringBuilder();
                traceMessageBuf.append(Messages.getString("MysqlIO.2"));
                traceMessageBuf.append(packetLength);
                traceMessageBuf.append(Messages.getString("MysqlIO.3"));
                traceMessageBuf.append(StringUtils.dumpAsHex(this.packetHeaderBuf, 4));
                this.connection.getLog().logTrace(traceMessageBuf.toString());
            }
            byte multiPacketSeq = this.packetHeaderBuf[3];
            if (this.packetSequenceReset) {
                this.packetSequenceReset = false;
            } else if (this.enablePacketDebug && this.checkPacketSequence) {
                checkPacketSequencing(multiPacketSeq);
            }
            this.readPacketSequence = multiPacketSeq;
            byte[] buffer = new byte[packetLength];
            int numBytesRead = readFully(this.mysqlInput, buffer, 0, packetLength);
            StringBuilder stringBuilder;
            if (numBytesRead != packetLength) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("Short read, expected ");
                stringBuilder.append(packetLength);
                stringBuilder.append(" bytes, only read ");
                stringBuilder.append(numBytesRead);
                throw new IOException(stringBuilder.toString());
            }
            Buffer packet = new Buffer(buffer);
            if (this.traceProtocol) {
                stringBuilder = new StringBuilder();
                stringBuilder.append(Messages.getString("MysqlIO.4"));
                stringBuilder.append(getPacketDumpToLog(packet, packetLength));
                this.connection.getLog().logTrace(stringBuilder.toString());
            }
            if (this.enablePacketDebug) {
                enqueuePacketForDebugging(false, false, 0, this.packetHeaderBuf, packet);
            }
            if (this.connection.getMaintainTimeStats()) {
                this.lastPacketReceivedTimeMs = System.currentTimeMillis();
            }
            return packet;
        } catch (IOException e) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, e, getExceptionInterceptor());
        } catch (OutOfMemoryError oom) {
            try {
                this.connection.realClose(false, false, true, oom);
            } catch (Exception e2) {
            }
            throw oom;
        }
    }

    protected final Field unpackField(Buffer packet, boolean extractDefaultValues) throws SQLException {
        short colFlag;
        if (this.use41Extensions) {
            long colLength;
            int defaultValueLength;
            int defaultValueStart;
            if (r0.has41NewNewProt) {
                adjustStartForFieldLength(packet.getPosition() + 1, packet.fastSkipLenString());
            }
            int databaseNameStart = packet.getPosition() + 1;
            int databaseNameLength = packet.fastSkipLenString();
            databaseNameStart = adjustStartForFieldLength(databaseNameStart, databaseNameLength);
            int tableNameStart = packet.getPosition() + 1;
            int tableNameLength = packet.fastSkipLenString();
            int tableNameStart2 = adjustStartForFieldLength(tableNameStart, tableNameLength);
            tableNameStart = packet.getPosition() + 1;
            int originalTableNameLength = packet.fastSkipLenString();
            int originalTableNameStart = adjustStartForFieldLength(tableNameStart, originalTableNameLength);
            tableNameStart = packet.getPosition() + 1;
            int fastSkipLenString = packet.fastSkipLenString();
            int nameStart = adjustStartForFieldLength(tableNameStart, fastSkipLenString);
            tableNameStart = packet.getPosition() + 1;
            int originalColumnNameLength = packet.fastSkipLenString();
            int originalColumnNameStart = adjustStartForFieldLength(tableNameStart, originalColumnNameLength);
            packet.readByte();
            short charSetNumber = (short) packet.readInt();
            if (r0.has41NewNewProt) {
                colLength = packet.readLong();
            } else {
                colLength = (long) packet.readLongInt();
            }
            long colLength2 = colLength;
            int colType = packet.readByte() & 255;
            if (r0.hasLongColumnInfo) {
                colFlag = (short) packet.readInt();
            } else {
                colFlag = (short) (packet.readByte() & 255);
            }
            short colFlag2 = colFlag;
            int colDecimals = packet.readByte() & 255;
            if (extractDefaultValues) {
                int defaultValueStart2 = packet.getPosition() + 1;
                defaultValueLength = packet.fastSkipLenString();
                defaultValueStart = defaultValueStart2;
            } else {
                defaultValueStart = -1;
                defaultValueLength = -1;
            }
            int nameLength = fastSkipLenString;
            return new Field(r0.connection, packet.getByteBuffer(), databaseNameStart, databaseNameLength, tableNameStart2, tableNameLength, originalTableNameStart, originalTableNameLength, nameStart, fastSkipLenString, originalColumnNameStart, originalColumnNameLength, colLength2, colType, colFlag2, colDecimals, defaultValueStart, defaultValueLength, charSetNumber);
        }
        databaseNameStart = packet.getPosition() + 1;
        originalTableNameLength = packet.fastSkipLenString();
        databaseNameStart = adjustStartForFieldLength(databaseNameStart, originalTableNameLength);
        tableNameStart = packet.getPosition() + 1;
        tableNameLength = packet.fastSkipLenString();
        databaseNameLength = adjustStartForFieldLength(tableNameStart, tableNameLength);
        int colLength3 = packet.readnBytes();
        int colType2 = packet.readnBytes();
        packet.readByte();
        if (r0.hasLongColumnInfo) {
            colFlag = (short) packet.readInt();
        } else {
            colFlag = (short) (packet.readByte() & 255);
        }
        short colFlag3 = colFlag;
        tableNameStart = packet.readByte() & 255;
        if (r0.colDecimalNeedsBump) {
            tableNameStart++;
        }
        return new Field(r0.connection, packet.getByteBuffer(), databaseNameLength, tableNameLength, databaseNameStart, originalTableNameLength, colLength3, colType2, colFlag3, tableNameStart);
    }

    private int adjustStartForFieldLength(int nameStart, int nameLength) {
        if (nameLength < 251) {
            return nameStart;
        }
        if (nameLength >= 251 && nameLength < 65536) {
            return nameStart + 2;
        }
        if (nameLength < 65536 || nameLength >= 16777216) {
            return nameStart + 8;
        }
        return nameStart + 3;
    }

    protected boolean isSetNeededForAutoCommitMode(boolean autoCommitFlag) {
        boolean z = true;
        if (!this.use41Extensions || !this.connection.getElideSetAutoCommits()) {
            return true;
        }
        boolean autoCommitModeOnServer = (this.serverStatus & 2) != 0;
        if (!autoCommitFlag && versionMeetsMinimum(5, 0, 0)) {
            return true ^ inTransactionOnServer();
        }
        if (autoCommitModeOnServer == autoCommitFlag) {
            z = false;
        }
        return z;
    }

    protected boolean inTransactionOnServer() {
        return (this.serverStatus & 1) != 0;
    }

    protected void changeUser(String userName, String password, String database) throws SQLException {
        String str = userName;
        String str2 = password;
        String str3 = database;
        this.packetSequence = (byte) -1;
        this.compressedPacketSequence = (byte) -1;
        boolean localUseConnectWithDb = false;
        int packLength = ((((((str != null ? userName.length() : 0) + 16) + (str3 != null ? database.length() : 0)) * 3) + 7) + 4) + 33;
        if ((r7.serverCapabilities & 524288) != 0) {
            proceedHandshakeWithPluggableAuthentication(str, str2, str3, null);
        } else if ((r7.serverCapabilities & 32768) != 0) {
            Buffer changeUserPacket = new Buffer(packLength + 1);
            changeUserPacket.writeByte((byte) 17);
            if (versionMeetsMinimum(4, 1, 1)) {
                secureAuth411(changeUserPacket, packLength, str, str2, str3, false);
            } else {
                secureAuth(changeUserPacket, packLength, str, str2, str3, false);
            }
        } else {
            Buffer packet = new Buffer(packLength);
            packet.writeByte((byte) 17);
            packet.writeString(str);
            if (r7.protocolVersion > (byte) 9) {
                packet.writeString(Util.newCrypt(str2, r7.seed, r7.connection.getPasswordCharacterEncoding()));
            } else {
                packet.writeString(Util.oldCrypt(str2, r7.seed));
            }
            if (r7.useConnectWithDb && str3 != null && database.length() > 0) {
                localUseConnectWithDb = true;
            }
            if (localUseConnectWithDb) {
                packet.writeString(str3);
            }
            send(packet, packet.getPosition());
            checkErrorPacket();
            if (!localUseConnectWithDb) {
                changeDatabaseTo(str3);
            }
        }
    }

    protected Buffer checkErrorPacket() throws SQLException {
        return checkErrorPacket(-1);
    }

    protected void checkForCharsetMismatch() {
        if (this.connection.getUseUnicode() && this.connection.getEncoding() != null) {
            String encodingToCheck = jvmPlatformCharset;
            if (encodingToCheck == null) {
                encodingToCheck = System.getProperty("file.encoding");
            }
            if (encodingToCheck == null) {
                this.platformDbCharsetMatches = false;
            } else {
                this.platformDbCharsetMatches = encodingToCheck.equals(this.connection.getEncoding());
            }
        }
    }

    protected void clearInputStream() throws SQLException {
        while (true) {
            try {
                int available = this.mysqlInput.available();
                int len = available;
                if (available <= 0 || this.mysqlInput.skip((long) len) <= 0) {
                }
            } catch (IOException e) {
                throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, e, getExceptionInterceptor());
            }
        }
    }

    protected void resetReadPacketSequence() {
        this.readPacketSequence = (byte) 0;
    }

    protected void dumpPacketRingBuffer() throws SQLException {
        if (this.packetDebugRingBuffer != null && this.connection.getEnablePacketDebug()) {
            StringBuilder dumpBuffer = new StringBuilder();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Last ");
            stringBuilder.append(this.packetDebugRingBuffer.size());
            stringBuilder.append(" packets received from server, from oldest->newest:\n");
            dumpBuffer.append(stringBuilder.toString());
            dumpBuffer.append("\n");
            Iterator<StringBuilder> ringBufIter = this.packetDebugRingBuffer.iterator();
            while (ringBufIter.hasNext()) {
                dumpBuffer.append((CharSequence) ringBufIter.next());
                dumpBuffer.append("\n");
            }
            this.connection.getLog().logTrace(dumpBuffer.toString());
        }
    }

    protected void explainSlowQuery(byte[] querySQL, String truncatedQuery) throws SQLException {
        if (StringUtils.startsWithIgnoreCaseAndWs(truncatedQuery, EXPLAINABLE_STATEMENT) || (versionMeetsMinimum(5, 6, 3) && StringUtils.startsWithIgnoreCaseAndWs(truncatedQuery, EXPLAINABLE_STATEMENT_EXTENSION) != -1)) {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = (PreparedStatement) this.connection.clientPrepareStatement("EXPLAIN ?");
                stmt.setBytesNoEscapeNoQuotes(1, querySQL);
                rs = stmt.executeQuery();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(Messages.getString("MysqlIO.8"));
                stringBuilder.append(truncatedQuery);
                stringBuilder.append(Messages.getString("MysqlIO.9"));
                StringBuilder explainResults = new StringBuilder(stringBuilder.toString());
                ResultSetUtil.appendResultSetSlashGStyle(explainResults, rs);
                this.connection.getLog().logWarn(explainResults.toString());
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                return;
            } catch (Throwable th) {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            }
        }
        MysqlIO mysqlIO = this;
    }

    static int getMaxBuf() {
        return maxBufferSize;
    }

    final int getServerMajorVersion() {
        return this.serverMajorVersion;
    }

    final int getServerMinorVersion() {
        return this.serverMinorVersion;
    }

    final int getServerSubMinorVersion() {
        return this.serverSubMinorVersion;
    }

    String getServerVersion() {
        return this.serverVersion;
    }

    void doHandshake(String user, String password, String database) throws SQLException {
        String str = user;
        String str2 = password;
        String str3 = database;
        boolean z = false;
        this.checkPacketSequence = false;
        this.readPacketSequence = (byte) 0;
        Buffer buf = readPacket();
        this.protocolVersion = buf.readByte();
        if (this.protocolVersion == (byte) -1) {
            try {
                r8.mysqlConnection.close();
            } catch (Exception e) {
            }
            int errno = buf.readInt();
            String serverErrorMessage = buf.readString("ASCII", getExceptionInterceptor());
            StringBuilder errorBuf = new StringBuilder(Messages.getString("MysqlIO.10"));
            errorBuf.append(serverErrorMessage);
            errorBuf.append("\"");
            String xOpen = SQLError.mysqlToSqlState(errno, r8.connection.getUseSqlStateCodes());
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(SQLError.get(xOpen));
            stringBuilder.append(", ");
            stringBuilder.append(errorBuf.toString());
            throw SQLError.createSQLException(stringBuilder.toString(), xOpen, errno, getExceptionInterceptor());
        }
        String remaining;
        r8.serverVersion = buf.readString("ASCII", getExceptionInterceptor());
        int point = r8.serverVersion.indexOf(46);
        if (point != -1) {
            try {
                r8.serverMajorVersion = Integer.parseInt(r8.serverVersion.substring(0, point));
            } catch (NumberFormatException e2) {
            }
            String remaining2 = r8.serverVersion.substring(point + 1, r8.serverVersion.length());
            point = remaining2.indexOf(46);
            if (point != -1) {
                try {
                    r8.serverMinorVersion = Integer.parseInt(remaining2.substring(0, point));
                } catch (NumberFormatException e3) {
                }
                remaining = remaining2.substring(point + 1, remaining2.length());
                int pos = 0;
                while (pos < remaining.length() && remaining.charAt(pos) >= '0') {
                    if (remaining.charAt(pos) > '9') {
                        break;
                    }
                    pos++;
                }
                try {
                    r8.serverSubMinorVersion = Integer.parseInt(remaining.substring(0, pos));
                } catch (NumberFormatException e4) {
                }
            }
        }
        if (versionMeetsMinimum(4, 0, 8)) {
            r8.maxThreeBytes = ViewCompat.MEASURED_SIZE_MASK;
            r8.useNewLargePackets = true;
        } else {
            r8.maxThreeBytes = 16581375;
            r8.useNewLargePackets = false;
        }
        r8.colDecimalNeedsBump = versionMeetsMinimum(3, 23, 0);
        r8.colDecimalNeedsBump = versionMeetsMinimum(3, 23, 15) ^ true;
        r8.useNewUpdateCounts = versionMeetsMinimum(3, 22, 5);
        r8.threadId = buf.readLong();
        if (r8.protocolVersion > (byte) 9) {
            r8.seed = buf.readString("ASCII", getExceptionInterceptor(), 8);
            buf.readByte();
        } else {
            r8.seed = buf.readString("ASCII", getExceptionInterceptor());
        }
        r8.serverCapabilities = 0;
        if (buf.getPosition() < buf.getBufLength()) {
            r8.serverCapabilities = buf.readInt();
        }
        if (versionMeetsMinimum(4, 1, 1) || (r8.protocolVersion > (byte) 9 && (r8.serverCapabilities & 512) != 0)) {
            r8.serverCharsetIndex = buf.readByte() & 255;
            r8.serverStatus = buf.readInt();
            checkTransactionState(0);
            r8.serverCapabilities |= buf.readInt() << 16;
            if ((r8.serverCapabilities & 524288) != 0) {
                r8.authPluginDataLength = buf.readByte() & 255;
            } else {
                buf.readByte();
            }
            buf.setPosition(buf.getPosition() + 10);
            if ((r8.serverCapabilities & 32768) != 0) {
                if (r8.authPluginDataLength > 0) {
                    remaining = buf.readString("ASCII", getExceptionInterceptor(), r8.authPluginDataLength - 8);
                    stringBuilder = new StringBuilder(r8.authPluginDataLength);
                } else {
                    remaining = buf.readString("ASCII", getExceptionInterceptor());
                    stringBuilder = new StringBuilder(20);
                }
                stringBuilder.append(r8.seed);
                stringBuilder.append(remaining);
                r8.seed = stringBuilder.toString();
            }
        }
        if ((r8.serverCapabilities & 32) != 0 && r8.connection.getUseCompression()) {
            r8.clientParam |= 32;
        }
        boolean z2 = (str3 == null || database.length() <= 0 || r8.connection.getCreateDatabaseIfNotExist()) ? false : true;
        r8.useConnectWithDb = z2;
        if (r8.useConnectWithDb) {
            r8.clientParam |= 8;
        }
        if (!(!versionMeetsMinimum(5, 7, 0) || r8.connection.getUseSSL() || r8.connection.isUseSSLExplicit())) {
            r8.connection.setUseSSL(true);
            r8.connection.setVerifyServerCertificate(false);
            r8.connection.getLog().logWarn(Messages.getString("MysqlIO.SSLWarning"));
        }
        if ((r8.serverCapabilities & 2048) == 0 && r8.connection.getUseSSL()) {
            if (r8.connection.getRequireSSL()) {
                r8.connection.close();
                forceClose();
                throw SQLError.createSQLException(Messages.getString("MysqlIO.15"), SQLError.SQL_STATE_UNABLE_TO_CONNECT_TO_DATASOURCE, getExceptionInterceptor());
            }
            r8.connection.setUseSSL(false);
        }
        if ((r8.serverCapabilities & 4) != 0) {
            r8.clientParam |= 4;
            r8.hasLongColumnInfo = true;
        }
        if (!r8.connection.getUseAffectedRows()) {
            r8.clientParam |= 2;
        }
        if (r8.connection.getAllowLoadLocalInfile()) {
            r8.clientParam |= 128;
        }
        if (r8.isInteractiveClient) {
            r8.clientParam |= PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID;
        }
        point = r8.serverCapabilities;
        if ((r8.serverCapabilities & 16777216) != 0) {
            r8.clientParam |= 16777216;
        }
        if ((r8.serverCapabilities & 524288) != 0) {
            proceedHandshakeWithPluggableAuthentication(str, str2, str3, buf);
            return;
        }
        Buffer packet;
        int i;
        if (r8.protocolVersion > (byte) 9) {
            r8.clientParam |= 1;
        } else {
            r8.clientParam &= -2;
        }
        if (versionMeetsMinimum(4, 1, 0) || (r8.protocolVersion > (byte) 9 && (r8.serverCapabilities & 16384) != 0)) {
            if (!versionMeetsMinimum(4, 1, 1)) {
                if (r8.protocolVersion <= (byte) 9 || (r8.serverCapabilities & 512) == 0) {
                    r8.clientParam |= PlaybackStateCompat.ACTION_PREPARE;
                    r8.has41NewNewProt = false;
                    r8.use41Extensions = true;
                }
            }
            r8.clientParam |= 512;
            r8.has41NewNewProt = true;
            r8.clientParam |= PlaybackStateCompat.ACTION_PLAY_FROM_URI;
            r8.clientParam |= PlaybackStateCompat.ACTION_PREPARE_FROM_URI;
            if (r8.connection.getAllowMultiQueries()) {
                r8.clientParam |= PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH;
            }
            r8.use41Extensions = true;
        }
        int userLength = str != null ? user.length() : 0;
        if (str3 != null) {
            z = database.length();
        }
        int packLength = (((((userLength + 16) + z) * 3) + 7) + 4) + 33;
        if (r8.connection.getUseSSL()) {
            buf = 9;
            negotiateSSLConnection(str, str2, str3, packLength);
            if ((r8.serverCapabilities & 32768) == 0) {
                packet = new Buffer(packLength);
                if (r8.use41Extensions) {
                    packet.writeLong(r8.clientParam);
                    packet.writeLong((long) r8.maxThreeBytes);
                } else {
                    packet.writeInt((int) r8.clientParam);
                    packet.writeLongInt(r8.maxThreeBytes);
                }
                packet.writeString(str);
                if (r8.protocolVersion > (byte) 9) {
                    packet.writeString(Util.newCrypt(str2, r8.seed, r8.connection.getPasswordCharacterEncoding()));
                } else {
                    packet.writeString(Util.oldCrypt(str2, r8.seed));
                }
                if (!((r8.serverCapabilities & 8) == 0 || str3 == null || database.length() <= 0)) {
                    packet.writeString(str3);
                }
                send(packet, packet.getPosition());
                checkErrorPacket();
                r8.deflater = new Deflater();
                r8.useCompression = true;
                r8.mysqlInput = new CompressedInputStream(r8.connection, r8.mysqlInput);
                if (!r8.useConnectWithDb) {
                    changeDatabaseTo(str3);
                }
                r8.mysqlConnection = r8.socketFactory.afterHandshake();
            } else if (versionMeetsMinimum(4, 1, 1)) {
                i = packLength;
                secureAuth411(null, packLength, str, str2, str3, 1);
            } else {
                i = packLength;
                secureAuth411(null, i, str, str2, str3, true);
            }
        } else if ((r8.serverCapabilities & 32768) != 0) {
            int packLength2;
            r8.clientParam |= PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID;
            if (versionMeetsMinimum(4, 1, 1)) {
                packLength2 = packLength;
                r27 = buf;
                buf = 9;
            } else if (r8.protocolVersion <= (byte) 9 || (r8.serverCapabilities & 512) == 0) {
                packLength2 = packLength;
                buf = 9;
                secureAuth(null, packLength, str, str2, str3, 1);
                i = packLength2;
            } else {
                packLength2 = packLength;
                r27 = buf;
                buf = 9;
            }
            secureAuth411(null, packLength2, str, str2, str3, true);
            i = packLength2;
        } else {
            r27 = buf;
            buf = 9;
            packet = new Buffer(packLength);
            if ((r8.clientParam & PlaybackStateCompat.ACTION_PREPARE) != 0) {
                if (!versionMeetsMinimum(4, 1, 1)) {
                    if (r8.protocolVersion <= (byte) 9 || (r8.serverCapabilities & 512) == 0) {
                        packet.writeLong(r8.clientParam);
                        packet.writeLong((long) r8.maxThreeBytes);
                    }
                }
                packet.writeLong(r8.clientParam);
                packet.writeLong((long) r8.maxThreeBytes);
                packet.writeByte((byte) 8);
                packet.writeBytesNoNull(new byte[23]);
            } else {
                packet.writeInt((int) r8.clientParam);
                packet.writeLongInt(r8.maxThreeBytes);
            }
            packet.writeString(str, CODE_PAGE_1252, r8.connection);
            if (r8.protocolVersion > (byte) 9) {
                packet.writeString(Util.newCrypt(str2, r8.seed, r8.connection.getPasswordCharacterEncoding()), CODE_PAGE_1252, r8.connection);
            } else {
                packet.writeString(Util.oldCrypt(str2, r8.seed), CODE_PAGE_1252, r8.connection);
            }
            if (r8.useConnectWithDb) {
                packet.writeString(str3, CODE_PAGE_1252, r8.connection);
            }
            send(packet, packet.getPosition());
            point = packLength;
            if (!versionMeetsMinimum(4, 1, 1) || r8.protocolVersion <= r12 || (r8.serverCapabilities & 512) == 0) {
                checkErrorPacket();
            }
            if (!((r8.serverCapabilities & 32) == 0 || !r8.connection.getUseCompression() || (r8.mysqlInput instanceof CompressedInputStream))) {
                r8.deflater = new Deflater();
                r8.useCompression = true;
                r8.mysqlInput = new CompressedInputStream(r8.connection, r8.mysqlInput);
            }
            if (r8.useConnectWithDb) {
                changeDatabaseTo(str3);
            }
            r8.mysqlConnection = r8.socketFactory.afterHandshake();
        }
        packet = null;
        point = i;
        checkErrorPacket();
        r8.deflater = new Deflater();
        r8.useCompression = true;
        r8.mysqlInput = new CompressedInputStream(r8.connection, r8.mysqlInput);
        if (r8.useConnectWithDb) {
            changeDatabaseTo(str3);
        }
        try {
            r8.mysqlConnection = r8.socketFactory.afterHandshake();
        } catch (IOException e5) {
            throw SQLError.createCommunicationsException(r8.connection, r8.lastPacketSentTimeMs, r8.lastPacketReceivedTimeMs, e5, getExceptionInterceptor());
        }
    }

    private void loadAuthenticationPlugins() throws SQLException {
        this.clientDefaultAuthenticationPlugin = this.connection.getDefaultAuthenticationPlugin();
        if (this.clientDefaultAuthenticationPlugin != null) {
            if (!"".equals(this.clientDefaultAuthenticationPlugin.trim())) {
                String disabledPlugins = this.connection.getDisabledAuthenticationPlugins();
                if (!(disabledPlugins == null || "".equals(disabledPlugins))) {
                    this.disabledAuthenticationPlugins = new ArrayList();
                    for (Object add : StringUtils.split(disabledPlugins, ",", true)) {
                        this.disabledAuthenticationPlugins.add(add);
                    }
                }
                this.authenticationPlugins = new HashMap();
                AuthenticationPlugin plugin = new MysqlOldPasswordPlugin();
                plugin.init(this.connection, this.connection.getProperties());
                boolean defaultIsFound = addAuthenticationPlugin(plugin);
                MysqlNativePasswordPlugin plugin2 = new MysqlNativePasswordPlugin();
                plugin2.init(this.connection, this.connection.getProperties());
                if (addAuthenticationPlugin(plugin2)) {
                    defaultIsFound = true;
                }
                MysqlClearPasswordPlugin plugin3 = new MysqlClearPasswordPlugin();
                plugin3.init(this.connection, this.connection.getProperties());
                if (addAuthenticationPlugin(plugin3)) {
                    defaultIsFound = true;
                }
                Sha256PasswordPlugin plugin4 = new Sha256PasswordPlugin();
                plugin4.init(this.connection, this.connection.getProperties());
                if (addAuthenticationPlugin(plugin4)) {
                    defaultIsFound = true;
                }
                String authenticationPluginClasses = this.connection.getAuthenticationPlugins();
                if (!(authenticationPluginClasses == null || "".equals(authenticationPluginClasses))) {
                    for (Extension object : Util.loadExtensions(this.connection, this.connection.getProperties(), authenticationPluginClasses, "Connection.BadAuthenticationPlugin", getExceptionInterceptor())) {
                        if (addAuthenticationPlugin((AuthenticationPlugin) object)) {
                            defaultIsFound = true;
                        }
                    }
                }
                if (!defaultIsFound) {
                    throw SQLError.createSQLException(Messages.getString("Connection.DefaultAuthenticationPluginIsNotListed", new Object[]{this.clientDefaultAuthenticationPlugin}), getExceptionInterceptor());
                }
                return;
            }
        }
        throw SQLError.createSQLException(Messages.getString("Connection.BadDefaultAuthenticationPlugin", new Object[]{this.clientDefaultAuthenticationPlugin}), getExceptionInterceptor());
    }

    private boolean addAuthenticationPlugin(AuthenticationPlugin plugin) throws SQLException {
        String pluginClassName = plugin.getClass().getName();
        String pluginProtocolName = plugin.getProtocolPluginName();
        boolean disabledByClassName = this.disabledAuthenticationPlugins != null && this.disabledAuthenticationPlugins.contains(pluginClassName);
        boolean disabledByMechanism = this.disabledAuthenticationPlugins != null && this.disabledAuthenticationPlugins.contains(pluginProtocolName);
        if (!disabledByClassName) {
            if (!disabledByMechanism) {
                this.authenticationPlugins.put(pluginProtocolName, plugin);
                if (!this.clientDefaultAuthenticationPlugin.equals(pluginClassName)) {
                    return false;
                }
                this.clientDefaultAuthenticationPluginName = pluginProtocolName;
                return true;
            }
        }
        if (!this.clientDefaultAuthenticationPlugin.equals(pluginClassName)) {
            return false;
        }
        String str = "Connection.BadDisabledAuthenticationPlugin";
        Object[] objArr = new Object[1];
        objArr[0] = disabledByClassName ? pluginClassName : pluginProtocolName;
        throw SQLError.createSQLException(Messages.getString(str, objArr), getExceptionInterceptor());
    }

    private AuthenticationPlugin getAuthenticationPlugin(String pluginName) throws SQLException {
        AuthenticationPlugin plugin = (AuthenticationPlugin) this.authenticationPlugins.get(pluginName);
        if (!(plugin == null || plugin.isReusable())) {
            try {
                plugin = (AuthenticationPlugin) plugin.getClass().newInstance();
                plugin.init(this.connection, this.connection.getProperties());
            } catch (Throwable t) {
                SQLError.createSQLException(Messages.getString("Connection.BadAuthenticationPlugin", new Object[]{plugin.getClass().getName()}), getExceptionInterceptor()).initCause(t);
            }
        }
        return plugin;
    }

    private void checkConfidentiality(AuthenticationPlugin plugin) throws SQLException {
        if (plugin.requiresConfidentiality() && !isSSLEstablished()) {
            throw SQLError.createSQLException(Messages.getString("Connection.AuthenticationPluginRequiresSSL", new Object[]{plugin.getProtocolPluginName()}), getExceptionInterceptor());
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void proceedHandshakeWithPluggableAuthentication(java.lang.String r40, java.lang.String r41, java.lang.String r42, com.mysql.jdbc.Buffer r43) throws java.sql.SQLException {
        /*
        r39 = this;
        r1 = r39;
        r2 = r40;
        r3 = r42;
        r4 = r1.authenticationPlugins;
        if (r4 != 0) goto L_0x000d;
    L_0x000a:
        r39.loadAuthenticationPlugins();
    L_0x000d:
        r4 = 0;
        r5 = 16;
        if (r2 == 0) goto L_0x0017;
    L_0x0012:
        r7 = r40.length();
        goto L_0x0018;
    L_0x0017:
        r7 = 0;
    L_0x0018:
        if (r3 == 0) goto L_0x001f;
    L_0x001a:
        r8 = r42.length();
        goto L_0x0020;
    L_0x001f:
        r8 = 0;
    L_0x0020:
        r9 = r7 + r5;
        r9 = r9 + r8;
        r9 = r9 * 3;
        r9 = r9 + 7;
        r9 = r9 + 4;
        r9 = r9 + 33;
        r10 = 0;
        r11 = 0;
        r12 = new java.util.ArrayList;
        r12.<init>();
        r13 = 0;
        r14 = 0;
        r15 = 0;
        r16 = 100;
        r17 = r4;
        r4 = r43;
    L_0x003b:
        r18 = r16 + -1;
        if (r16 <= 0) goto L_0x0462;
    L_0x003f:
        r16 = 2097152; // 0x200000 float:2.938736E-39 double:1.0361308E-317;
        r20 = 524288; // 0x80000 float:7.34684E-40 double:2.590327E-318;
        r21 = 1048576; // 0x100000 float:1.469368E-39 double:5.180654E-318;
        if (r13 != 0) goto L_0x01a1;
    L_0x0048:
        if (r4 == 0) goto L_0x0176;
    L_0x004a:
        r24 = r4.isOKPacket();
        if (r24 == 0) goto L_0x006c;
    L_0x0050:
        r6 = "Connection.UnexpectedAuthenticationApproval";
        r25 = r5;
        r5 = 1;
        r5 = new java.lang.Object[r5];
        r16 = r10.getProtocolPluginName();
        r19 = 0;
        r5[r19] = r16;
        r5 = com.mysql.jdbc.Messages.getString(r6, r5);
        r6 = r39.getExceptionInterceptor();
        r5 = com.mysql.jdbc.SQLError.createSQLException(r5, r6);
        throw r5;
    L_0x006c:
        r25 = r5;
        r26 = r7;
        r6 = r1.clientParam;
        r27 = 696833; // 0xaa201 float:9.76471E-40 double:3.44281E-318;
        r29 = r13;
        r30 = r14;
        r13 = r6 | r27;
        r1.clientParam = r13;
        r5 = r1.connection;
        r5 = r5.getAllowMultiQueries();
        if (r5 == 0) goto L_0x0091;
    L_0x0085:
        r5 = r1.clientParam;
        r13 = 65536; // 0x10000 float:9.18355E-41 double:3.2379E-319;
        r31 = r8;
        r7 = r5 | r13;
        r1.clientParam = r7;
        goto L_0x0093;
    L_0x0091:
        r31 = r8;
    L_0x0093:
        r5 = r1.serverCapabilities;
        r6 = 4194304; // 0x400000 float:5.877472E-39 double:2.0722615E-317;
        r5 = r5 & r6;
        if (r5 == 0) goto L_0x00ab;
    L_0x009a:
        r5 = r1.connection;
        r5 = r5.getDisconnectOnExpiredPasswords();
        if (r5 != 0) goto L_0x00ab;
    L_0x00a2:
        r5 = r1.clientParam;
        r7 = 4194304; // 0x400000 float:5.877472E-39 double:2.0722615E-317;
        r13 = r5 | r7;
        r1.clientParam = r13;
    L_0x00ab:
        r5 = r1.serverCapabilities;
        r6 = 1048576; // 0x100000 float:1.469368E-39 double:5.180654E-318;
        r5 = r5 & r6;
        if (r5 == 0) goto L_0x00c6;
    L_0x00b2:
        r5 = "none";
        r6 = r1.connection;
        r6 = r6.getConnectionAttributes();
        r5 = r5.equals(r6);
        if (r5 != 0) goto L_0x00c6;
    L_0x00c0:
        r5 = r1.clientParam;
        r7 = r5 | r21;
        r1.clientParam = r7;
    L_0x00c6:
        r5 = r1.serverCapabilities;
        r5 = r5 & r16;
        if (r5 == 0) goto L_0x00d5;
    L_0x00cc:
        r5 = r1.clientParam;
        r7 = 2097152; // 0x200000 float:2.938736E-39 double:1.0361308E-317;
        r13 = r5 | r7;
        r1.clientParam = r13;
    L_0x00d5:
        r5 = 1;
        r1.has41NewNewProt = r5;
        r1.use41Extensions = r5;
        r5 = r1.connection;
        r5 = r5.getUseSSL();
        if (r5 == 0) goto L_0x00e8;
    L_0x00e2:
        r5 = r41;
        r1.negotiateSSLConnection(r2, r5, r3, r9);
        goto L_0x00ea;
    L_0x00e8:
        r5 = r41;
    L_0x00ea:
        r6 = 0;
        r7 = r1.serverCapabilities;
        r7 = r7 & r20;
        if (r7 == 0) goto L_0x0121;
    L_0x00f1:
        r7 = 10;
        r8 = 5;
        r7 = r1.versionMeetsMinimum(r8, r8, r7);
        if (r7 == 0) goto L_0x0115;
    L_0x00fa:
        r7 = 6;
        r13 = 0;
        r14 = r1.versionMeetsMinimum(r8, r7, r13);
        if (r14 == 0) goto L_0x010a;
    L_0x0102:
        r13 = 2;
        r7 = r1.versionMeetsMinimum(r8, r7, r13);
        if (r7 != 0) goto L_0x010a;
    L_0x0109:
        goto L_0x0115;
    L_0x010a:
        r7 = "ASCII";
        r8 = r39.getExceptionInterceptor();
        r6 = r4.readString(r7, r8);
        goto L_0x0121;
    L_0x0115:
        r7 = "ASCII";
        r8 = r39.getExceptionInterceptor();
        r13 = r1.authPluginDataLength;
        r6 = r4.readString(r7, r8, r13);
    L_0x0121:
        r7 = r1.getAuthenticationPlugin(r6);
        if (r7 != 0) goto L_0x012e;
    L_0x0127:
        r8 = r1.clientDefaultAuthenticationPluginName;
        r7 = r1.getAuthenticationPlugin(r8);
        goto L_0x015c;
    L_0x012e:
        r8 = com.mysql.jdbc.authentication.Sha256PasswordPlugin.PLUGIN_NAME;
        r8 = r6.equals(r8);
        if (r8 == 0) goto L_0x015c;
    L_0x0136:
        r8 = r39.isSSLEstablished();
        if (r8 != 0) goto L_0x015c;
    L_0x013c:
        r8 = r1.connection;
        r8 = r8.getServerRSAPublicKeyFile();
        if (r8 != 0) goto L_0x015c;
    L_0x0144:
        r8 = r1.connection;
        r8 = r8.getAllowPublicKeyRetrieval();
        if (r8 != 0) goto L_0x015c;
    L_0x014c:
        r8 = r1.clientDefaultAuthenticationPluginName;
        r7 = r1.getAuthenticationPlugin(r8);
        r8 = r1.clientDefaultAuthenticationPluginName;
        r8 = r8.equals(r6);
        r10 = 1;
        r8 = r8 ^ r10;
        r17 = r8;
    L_0x015c:
        r8 = r7.getProtocolPluginName();
        r1.serverDefaultAuthenticationPluginName = r8;
        r1.checkConfidentiality(r7);
        r8 = new com.mysql.jdbc.Buffer;
        r10 = r1.seed;
        r10 = com.mysql.jdbc.StringUtils.getBytes(r10);
        r8.<init>(r10);
        r6 = r8;
        r11 = r6;
        r10 = r7;
        goto L_0x027d;
    L_0x0176:
        r25 = r5;
        r26 = r7;
        r31 = r8;
        r29 = r13;
        r30 = r14;
        r5 = r41;
        r6 = r1.serverDefaultAuthenticationPluginName;
        if (r6 != 0) goto L_0x0189;
    L_0x0186:
        r6 = r1.clientDefaultAuthenticationPluginName;
        goto L_0x018b;
    L_0x0189:
        r6 = r1.serverDefaultAuthenticationPluginName;
    L_0x018b:
        r6 = r1.getAuthenticationPlugin(r6);
        r1.checkConfidentiality(r6);
        r7 = new com.mysql.jdbc.Buffer;
        r8 = r1.seed;
        r8 = com.mysql.jdbc.StringUtils.getBytes(r8);
        r7.<init>(r8);
        r10 = r6;
    L_0x019e:
        r11 = r7;
        goto L_0x027d;
    L_0x01a1:
        r25 = r5;
        r26 = r7;
        r31 = r8;
        r29 = r13;
        r30 = r14;
        r5 = r41;
        r4 = r39.checkErrorPacket();
        r15 = 0;
        r6 = r1.packetSequence;
        r7 = 1;
        r6 = r6 + r7;
        r6 = (byte) r6;
        r1.packetSequence = r6;
        r6 = r1.compressedPacketSequence;
        r6 = r6 + r7;
        r6 = (byte) r6;
        r1.compressedPacketSequence = r6;
        if (r10 != 0) goto L_0x01cf;
    L_0x01c1:
        r6 = r1.serverDefaultAuthenticationPluginName;
        if (r6 == 0) goto L_0x01c8;
    L_0x01c5:
        r6 = r1.serverDefaultAuthenticationPluginName;
        goto L_0x01ca;
    L_0x01c8:
        r6 = r1.clientDefaultAuthenticationPluginName;
    L_0x01ca:
        r6 = r1.getAuthenticationPlugin(r6);
        r10 = r6;
    L_0x01cf:
        r6 = r4.isOKPacket();
        if (r6 == 0) goto L_0x01ea;
    L_0x01d5:
        r4.newReadLength();
        r4.newReadLength();
        r6 = r1.serverStatus;
        r1.oldServerStatus = r6;
        r6 = r4.readInt();
        r1.serverStatus = r6;
        r10.destroy();
        goto L_0x046c;
    L_0x01ea:
        r6 = r4.isAuthMethodSwitchRequestPacket();
        if (r6 == 0) goto L_0x023f;
    L_0x01f0:
        r6 = 0;
        r7 = "ASCII";
        r8 = r39.getExceptionInterceptor();
        r7 = r4.readString(r7, r8);
        r8 = r10.getProtocolPluginName();
        r8 = r8.equals(r7);
        if (r8 != 0) goto L_0x0223;
    L_0x0205:
        r10.destroy();
        r10 = r1.getAuthenticationPlugin(r7);
        if (r10 != 0) goto L_0x0223;
    L_0x020e:
        r8 = "Connection.BadAuthenticationPlugin";
        r13 = 1;
        r13 = new java.lang.Object[r13];
        r14 = 0;
        r13[r14] = r7;
        r8 = com.mysql.jdbc.Messages.getString(r8, r13);
        r13 = r39.getExceptionInterceptor();
        r8 = com.mysql.jdbc.SQLError.createSQLException(r8, r13);
        throw r8;
    L_0x0223:
        r1.checkConfidentiality(r10);
        r8 = new com.mysql.jdbc.Buffer;
        r13 = "ASCII";
        r14 = r39.getExceptionInterceptor();
        r13 = r4.readString(r13, r14);
        r13 = com.mysql.jdbc.StringUtils.getBytes(r13);
        r8.<init>(r13);
        r7 = r8;
        r17 = r6;
        goto L_0x019e;
    L_0x023f:
        r6 = 16;
        r7 = 5;
        r6 = r1.versionMeetsMinimum(r7, r7, r6);
        if (r6 == 0) goto L_0x0260;
    L_0x0248:
        r6 = new com.mysql.jdbc.Buffer;
        r7 = r4.getPosition();
        r8 = r4.getBufLength();
        r13 = r4.getPosition();
        r8 = r8 - r13;
        r7 = r4.getBytes(r7, r8);
        r6.<init>(r7);
        r11 = r6;
        goto L_0x027d;
    L_0x0260:
        r6 = 1;
        r7 = new com.mysql.jdbc.Buffer;
        r8 = r4.getPosition();
        r13 = 1;
        r8 = r8 - r13;
        r14 = r4.getBufLength();
        r15 = r4.getPosition();
        r14 = r14 - r15;
        r14 = r14 + r13;
        r8 = r4.getBytes(r8, r14);
        r7.<init>(r8);
        r15 = r6;
        goto L_0x019e;
    L_0x027d:
        if (r17 == 0) goto L_0x0281;
    L_0x027f:
        r6 = 0;
        goto L_0x0282;
    L_0x0281:
        r6 = r5;
    L_0x0282:
        r10.setAuthenticationParameters(r2, r6);	 Catch:{ SQLException -> 0x044f }
        r6 = r10.nextAuthenticationStep(r11, r12);	 Catch:{ SQLException -> 0x044f }
        r13 = r6;
        r6 = r12.size();
        if (r6 <= 0) goto L_0x0443;
    L_0x0291:
        if (r4 != 0) goto L_0x0321;
    L_0x0293:
        r8 = r39.getEncodingForHandshake();
        r14 = new com.mysql.jdbc.Buffer;
        r6 = r9 + 1;
        r14.<init>(r6);
        r6 = 17;
        r14.writeByte(r6);
        r6 = r1.connection;
        r14.writeString(r2, r8, r6);
        r6 = 0;
        r7 = r12.get(r6);
        r7 = (com.mysql.jdbc.Buffer) r7;
        r7 = r7.getBufLength();
        r6 = 256; // 0x100 float:3.59E-43 double:1.265E-321;
        if (r7 >= r6) goto L_0x02e0;
    L_0x02b7:
        r6 = 0;
        r7 = r12.get(r6);
        r7 = (com.mysql.jdbc.Buffer) r7;
        r7 = r7.getBufLength();
        r7 = (byte) r7;
        r14.writeByte(r7);
        r7 = r12.get(r6);
        r7 = (com.mysql.jdbc.Buffer) r7;
        r7 = r7.getByteBuffer();
        r16 = r12.get(r6);
        r5 = r16;
        r5 = (com.mysql.jdbc.Buffer) r5;
        r5 = r5.getBufLength();
        r14.writeBytesNoNull(r7, r6, r5);
        goto L_0x02e4;
    L_0x02e0:
        r6 = 0;
        r14.writeByte(r6);
    L_0x02e4:
        r5 = r1.useConnectWithDb;
        if (r5 == 0) goto L_0x02ee;
    L_0x02e8:
        r5 = r1.connection;
        r14.writeString(r3, r8, r5);
        goto L_0x02f1;
    L_0x02ee:
        r14.writeByte(r6);
    L_0x02f1:
        r1.appendCharsetByteForHandshake(r14, r8);
        r14.writeByte(r6);
        r5 = r1.serverCapabilities;
        r5 = r5 & r20;
        if (r5 == 0) goto L_0x0306;
    L_0x02fd:
        r5 = r10.getProtocolPluginName();
        r6 = r1.connection;
        r14.writeString(r5, r8, r6);
    L_0x0306:
        r5 = r1.clientParam;
        r23 = r5 & r21;
        r5 = 0;
        r7 = (r23 > r5 ? 1 : (r23 == r5 ? 0 : -1));
        if (r7 == 0) goto L_0x0319;
    L_0x0310:
        r5 = r1.connection;
        r1.sendConnectionAttributes(r14, r8, r5);
        r5 = 0;
        r14.writeByte(r5);
    L_0x0319:
        r5 = r14.getPosition();
        r1.send(r14, r5);
        goto L_0x0358;
    L_0x0321:
        r5 = r4.isAuthMethodSwitchRequestPacket();
        if (r5 == 0) goto L_0x0360;
    L_0x0327:
        r5 = new com.mysql.jdbc.Buffer;
        r6 = 0;
        r7 = r12.get(r6);
        r7 = (com.mysql.jdbc.Buffer) r7;
        r7 = r7.getBufLength();
        r7 = r7 + 4;
        r5.<init>(r7);
        r14 = r5;
        r5 = r12.get(r6);
        r5 = (com.mysql.jdbc.Buffer) r5;
        r5 = r5.getByteBuffer();
        r7 = r12.get(r6);
        r7 = (com.mysql.jdbc.Buffer) r7;
        r7 = r7.getBufLength();
        r14.writeBytesNoNull(r5, r6, r7);
        r5 = r14.getPosition();
        r1.send(r14, r5);
    L_0x0358:
        r16 = r18;
        r5 = r25;
        r7 = r26;
        goto L_0x044b;
    L_0x0360:
        r5 = r4.isRawPacket();
        if (r5 != 0) goto L_0x0407;
    L_0x0366:
        if (r15 == 0) goto L_0x036a;
    L_0x0368:
        goto L_0x0407;
    L_0x036a:
        r5 = r39.getEncodingForHandshake();
        r6 = new com.mysql.jdbc.Buffer;
        r6.<init>(r9);
        r14 = r6;
        r6 = r1.clientParam;
        r14.writeLong(r6);
        r6 = r1.maxThreeBytes;
        r6 = (long) r6;
        r14.writeLong(r6);
        r1.appendCharsetByteForHandshake(r14, r5);
        r6 = 23;
        r6 = new byte[r6];
        r14.writeBytesNoNull(r6);
        r6 = r1.connection;
        r14.writeString(r2, r5, r6);
        r6 = r1.serverCapabilities;
        r6 = r6 & r16;
        if (r6 == 0) goto L_0x03ad;
    L_0x0394:
        r6 = 0;
        r7 = r12.get(r6);
        r7 = (com.mysql.jdbc.Buffer) r7;
        r8 = r12.get(r6);
        r8 = (com.mysql.jdbc.Buffer) r8;
        r8 = r8.getBufLength();
        r7 = r7.getBytes(r8);
        r14.writeLenBytes(r7);
        goto L_0x03d3;
    L_0x03ad:
        r6 = 0;
        r7 = r12.get(r6);
        r7 = (com.mysql.jdbc.Buffer) r7;
        r7 = r7.getBufLength();
        r7 = (byte) r7;
        r14.writeByte(r7);
        r7 = r12.get(r6);
        r7 = (com.mysql.jdbc.Buffer) r7;
        r7 = r7.getByteBuffer();
        r8 = r12.get(r6);
        r8 = (com.mysql.jdbc.Buffer) r8;
        r8 = r8.getBufLength();
        r14.writeBytesNoNull(r7, r6, r8);
    L_0x03d3:
        r7 = r1.useConnectWithDb;
        if (r7 == 0) goto L_0x03dd;
    L_0x03d7:
        r7 = r1.connection;
        r14.writeString(r3, r5, r7);
        goto L_0x03e0;
    L_0x03dd:
        r14.writeByte(r6);
    L_0x03e0:
        r6 = r1.serverCapabilities;
        r6 = r6 & r20;
        if (r6 == 0) goto L_0x03ef;
    L_0x03e6:
        r6 = r10.getProtocolPluginName();
        r7 = r1.connection;
        r14.writeString(r6, r5, r7);
    L_0x03ef:
        r6 = r1.clientParam;
        r23 = r6 & r21;
        r6 = 0;
        r8 = (r23 > r6 ? 1 : (r23 == r6 ? 0 : -1));
        if (r8 == 0) goto L_0x03fe;
    L_0x03f9:
        r6 = r1.connection;
        r1.sendConnectionAttributes(r14, r5, r6);
    L_0x03fe:
        r6 = r14.getPosition();
        r1.send(r14, r6);
        goto L_0x0358;
    L_0x0407:
        r5 = r12.iterator();
        r14 = r30;
    L_0x040d:
        r6 = r5.hasNext();
        if (r6 == 0) goto L_0x0358;
    L_0x0413:
        r6 = r5.next();
        r6 = (com.mysql.jdbc.Buffer) r6;
        r7 = new com.mysql.jdbc.Buffer;
        r8 = r6.getBufLength();
        r8 = r8 + 4;
        r7.<init>(r8);
        r14 = r7;
        r7 = r6.getByteBuffer();
        r8 = 0;
        r16 = r12.get(r8);
        r2 = r16;
        r2 = (com.mysql.jdbc.Buffer) r2;
        r2 = r2.getBufLength();
        r14.writeBytesNoNull(r7, r8, r2);
        r2 = r14.getPosition();
        r1.send(r14, r2);
        r2 = r40;
        goto L_0x040d;
    L_0x0443:
        r16 = r18;
        r5 = r25;
        r7 = r26;
        r14 = r30;
    L_0x044b:
        r8 = r31;
        goto L_0x003b;
    L_0x044f:
        r0 = move-exception;
        r2 = r0;
        r5 = r2.getMessage();
        r6 = r2.getSQLState();
        r7 = r39.getExceptionInterceptor();
        r5 = com.mysql.jdbc.SQLError.createSQLException(r5, r6, r2, r7);
        throw r5;
    L_0x0462:
        r25 = r5;
        r26 = r7;
        r31 = r8;
        r29 = r13;
        r30 = r14;
    L_0x046c:
        if (r18 != 0) goto L_0x047d;
    L_0x046e:
        r2 = "CommunicationsException.TooManyAuthenticationPluginNegotiations";
        r2 = com.mysql.jdbc.Messages.getString(r2);
        r5 = r39.getExceptionInterceptor();
        r2 = com.mysql.jdbc.SQLError.createSQLException(r2, r5);
        throw r2;
    L_0x047d:
        r2 = r1.serverCapabilities;
        r2 = r2 & 32;
        if (r2 == 0) goto L_0x04a6;
    L_0x0483:
        r2 = r1.connection;
        r2 = r2.getUseCompression();
        if (r2 == 0) goto L_0x04a6;
    L_0x048b:
        r2 = r1.mysqlInput;
        r2 = r2 instanceof com.mysql.jdbc.CompressedInputStream;
        if (r2 != 0) goto L_0x04a6;
    L_0x0491:
        r2 = new java.util.zip.Deflater;
        r2.<init>();
        r1.deflater = r2;
        r2 = 1;
        r1.useCompression = r2;
        r2 = new com.mysql.jdbc.CompressedInputStream;
        r5 = r1.connection;
        r6 = r1.mysqlInput;
        r2.<init>(r5, r6);
        r1.mysqlInput = r2;
    L_0x04a6:
        r2 = r1.useConnectWithDb;
        if (r2 != 0) goto L_0x04ad;
    L_0x04aa:
        r1.changeDatabaseTo(r3);
    L_0x04ad:
        r2 = r1.socketFactory;	 Catch:{ IOException -> 0x04b7 }
        r2 = r2.afterHandshake();	 Catch:{ IOException -> 0x04b7 }
        r1.mysqlConnection = r2;	 Catch:{ IOException -> 0x04b7 }
        return;
    L_0x04b7:
        r0 = move-exception;
        r37 = r0;
        r2 = r1.connection;
        r5 = r1.lastPacketSentTimeMs;
        r7 = r1.lastPacketReceivedTimeMs;
        r38 = r39.getExceptionInterceptor();
        r32 = r2;
        r33 = r5;
        r35 = r7;
        r2 = com.mysql.jdbc.SQLError.createCommunicationsException(r32, r33, r35, r37, r38);
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.MysqlIO.proceedHandshakeWithPluggableAuthentication(java.lang.String, java.lang.String, java.lang.String, com.mysql.jdbc.Buffer):void");
    }

    private Properties getConnectionAttributesAsProperties(String atts) throws SQLException {
        Properties props = new Properties();
        if (atts != null) {
            for (String pair : atts.split(",")) {
                int keyEnd = pair.indexOf(":");
                if (keyEnd > 0 && keyEnd + 1 < pair.length()) {
                    props.setProperty(pair.substring(0, keyEnd), pair.substring(keyEnd + 1));
                }
            }
        }
        props.setProperty("_client_name", NonRegisteringDriver.NAME);
        props.setProperty("_client_version", NonRegisteringDriver.VERSION);
        props.setProperty("_runtime_vendor", NonRegisteringDriver.RUNTIME_VENDOR);
        props.setProperty("_runtime_version", NonRegisteringDriver.RUNTIME_VERSION);
        props.setProperty("_client_license", NonRegisteringDriver.LICENSE);
        return props;
    }

    private void sendConnectionAttributes(Buffer buf, String enc, MySQLConnection conn) throws SQLException {
        String atts = conn.getConnectionAttributes();
        Buffer lb = new Buffer(100);
        try {
            Properties props = getConnectionAttributesAsProperties(atts);
            Iterator i$ = props.keySet().iterator();
            while (true) {
                Iterator i$2 = i$;
                if (!i$2.hasNext()) {
                    break;
                }
                Object key = i$2.next();
                lb.writeLenString((String) key, enc, conn.getServerCharset(), null, conn.parserKnowsUnicode(), conn);
                lb.writeLenString(props.getProperty((String) key), enc, conn.getServerCharset(), null, conn.parserKnowsUnicode(), conn);
                i$ = i$2;
            }
        } catch (UnsupportedEncodingException e) {
        }
        buf.writeByte((byte) (lb.getPosition() - 4));
        buf.writeBytesNoNull(lb.getByteBuffer(), 4, lb.getBufLength() - 4);
    }

    private void changeDatabaseTo(String database) throws SQLException {
        MysqlIO mysqlIO = this;
        String str = database;
        if (str != null) {
            if (database.length() != 0) {
                try {
                    sendCommand(2, str, null, false, null, 0);
                } catch (Exception e) {
                    Exception ex = e;
                    if (mysqlIO.connection.getCreateDatabaseIfNotExist()) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("CREATE DATABASE IF NOT EXISTS ");
                        stringBuilder.append(str);
                        sendCommand(3, stringBuilder.toString(), null, false, null, 0);
                        sendCommand(2, str, null, false, null, 0);
                    } else {
                        throw SQLError.createCommunicationsException(mysqlIO.connection, mysqlIO.lastPacketSentTimeMs, mysqlIO.lastPacketReceivedTimeMs, ex, getExceptionInterceptor());
                    }
                }
            }
        }
    }

    final ResultSetRow nextRow(Field[] fields, int columnCount, boolean isBinaryEncoded, int resultSetConcurrency, boolean useBufferRowIfPossible, boolean useBufferRowExplicit, boolean canReuseRowPacketForBufferRow, Buffer existingRowPacket) throws SQLException {
        if (this.useDirectRowUnpack && existingRowPacket == null && !isBinaryEncoded && !useBufferRowIfPossible && !useBufferRowExplicit) {
            return nextRowFast(fields, columnCount, isBinaryEncoded, resultSetConcurrency, useBufferRowIfPossible, useBufferRowExplicit, canReuseRowPacketForBufferRow);
        }
        Buffer rowPacket;
        if (existingRowPacket == null) {
            rowPacket = checkErrorPacket();
            if (!useBufferRowExplicit && useBufferRowIfPossible && rowPacket.getBufLength() > this.useBufferRowSizeThreshold) {
                useBufferRowExplicit = true;
            }
        } else {
            rowPacket = existingRowPacket;
            checkErrorPacket(existingRowPacket);
        }
        if (isBinaryEncoded) {
            if (isEOFDeprecated() || !rowPacket.isEOFPacket()) {
                if (isEOFDeprecated()) {
                    if (!rowPacket.isResultSetOKPacket()) {
                    }
                }
                if (resultSetConcurrency != 1008) {
                    if (useBufferRowIfPossible || useBufferRowExplicit) {
                        if (!canReuseRowPacketForBufferRow) {
                            this.reusablePacket = new Buffer(rowPacket.getBufLength());
                        }
                        return new BufferRow(rowPacket, fields, true, getExceptionInterceptor());
                    }
                }
                return unpackBinaryResultSetRow(fields, rowPacket, resultSetConcurrency);
            }
            rowPacket.setPosition(rowPacket.getPosition() - 1);
            readServerStatusForResultSets(rowPacket);
            return null;
        }
        rowPacket.setPosition(rowPacket.getPosition() - 1);
        if (isEOFDeprecated() || !rowPacket.isEOFPacket()) {
            if (isEOFDeprecated()) {
                if (!rowPacket.isResultSetOKPacket()) {
                }
            }
            if (resultSetConcurrency != 1008) {
                if (useBufferRowIfPossible || useBufferRowExplicit) {
                    if (!canReuseRowPacketForBufferRow) {
                        this.reusablePacket = new Buffer(rowPacket.getBufLength());
                    }
                    return new BufferRow(rowPacket, fields, false, getExceptionInterceptor());
                }
            }
            byte[][] rowData = new byte[columnCount][];
            for (int i = 0; i < columnCount; i++) {
                rowData[i] = rowPacket.readLenByteArray(0);
            }
            return new ByteArrayRow(rowData, getExceptionInterceptor());
        }
        readServerStatusForResultSets(rowPacket);
        return null;
    }

    final ResultSetRow nextRowFast(Field[] fields, int columnCount, boolean isBinaryEncoded, int resultSetConcurrency, boolean useBufferRowIfPossible, boolean useBufferRowExplicit, boolean canReuseRowPacket) throws SQLException {
        int i = columnCount;
        try {
            int i2 = 4;
            int i3 = 0;
            int lengthRead = readFully(this.mysqlInput, r10.packetHeaderBuf, 0, 4);
            if (lengthRead < 4) {
                forceClose();
                throw new RuntimeException(Messages.getString("MysqlIO.43"));
            }
            byte[][] rowData = 1;
            int i4 = 2;
            int packetLength = ((r10.packetHeaderBuf[0] & 255) + ((r10.packetHeaderBuf[1] & 255) << 8)) + ((r10.packetHeaderBuf[2] & 255) << 16);
            if (packetLength == r10.maxThreeBytes) {
                reuseAndReadPacket(r10.reusablePacket, packetLength);
                return nextRow(fields, i, isBinaryEncoded, resultSetConcurrency, useBufferRowIfPossible, useBufferRowExplicit, canReuseRowPacket, r10.reusablePacket);
            } else if (packetLength > r10.useBufferRowSizeThreshold) {
                reuseAndReadPacket(r10.reusablePacket, packetLength);
                return nextRow(fields, i, isBinaryEncoded, resultSetConcurrency, true, true, false, r10.reusablePacket);
            } else {
                byte[][] rowData2 = null;
                boolean firstTime = true;
                int remaining = packetLength;
                int i5 = 0;
                while (i5 < i) {
                    int lengthRead2;
                    int i6;
                    Object obj;
                    Object obj2;
                    int sw = r10.mysqlInput.read() & 255;
                    remaining--;
                    if (firstTime) {
                        if (sw == 255) {
                            Buffer errorPacket = new Buffer(packetLength + 4);
                            errorPacket.setPosition(i3);
                            errorPacket.writeByte(r10.packetHeaderBuf[i3]);
                            errorPacket.writeByte(r10.packetHeaderBuf[rowData]);
                            errorPacket.writeByte(r10.packetHeaderBuf[i4]);
                            errorPacket.writeByte(rowData);
                            errorPacket.writeByte((byte) sw);
                            readFully(r10.mysqlInput, errorPacket.getByteBuffer(), 5, packetLength - 1);
                            errorPacket.setPosition(i2);
                            checkErrorPacket(errorPacket);
                        }
                        if (sw != 254 || packetLength >= ViewCompat.MEASURED_SIZE_MASK) {
                            rowData2 = new byte[i][];
                            firstTime = false;
                        } else {
                            if (r10.use41Extensions) {
                                if (isEOFDeprecated()) {
                                    remaining = (remaining - skipLengthEncodedInteger(r10.mysqlInput)) - skipLengthEncodedInteger(r10.mysqlInput);
                                    r10.oldServerStatus = r10.serverStatus;
                                    r10.serverStatus = (r10.mysqlInput.read() & 255) | ((r10.mysqlInput.read() & 255) << 8);
                                    checkTransactionState(r10.oldServerStatus);
                                    remaining -= 2;
                                    r10.warningCount = ((255 & r10.mysqlInput.read()) << 8) | (r10.mysqlInput.read() & 255);
                                    remaining -= 2;
                                    if (r10.warningCount > 0) {
                                        r10.hadWarnings = true;
                                    }
                                } else {
                                    r10.warningCount = (r10.mysqlInput.read() & 255) | ((r10.mysqlInput.read() & 255) << 8);
                                    remaining -= 2;
                                    if (r10.warningCount > 0) {
                                        r10.hadWarnings = true;
                                    }
                                    r10.oldServerStatus = r10.serverStatus;
                                    r10.serverStatus = ((255 & r10.mysqlInput.read()) << 8) | (r10.mysqlInput.read() & 255);
                                    checkTransactionState(r10.oldServerStatus);
                                    remaining -= 2;
                                }
                                setServerSlowQueryFlags();
                                if (remaining > 0) {
                                    skipFully(r10.mysqlInput, (long) remaining);
                                }
                            }
                            return null;
                        }
                    }
                    byte[][] bArr = rowData;
                    switch (sw) {
                        case 251:
                            lengthRead2 = lengthRead;
                            i6 = 16;
                            i4 = 8;
                            i2 = -1;
                            break;
                        case MysqlDefs.FIELD_TYPE_BLOB /*252*/:
                            lengthRead2 = lengthRead;
                            i6 = 16;
                            i4 = 8;
                            i2 = (r10.mysqlInput.read() & 255) | ((r10.mysqlInput.read() & 255) << 8);
                            remaining -= 2;
                            break;
                        case 253:
                            lengthRead2 = lengthRead;
                            i6 = 16;
                            i2 = ((r10.mysqlInput.read() & 255) | ((r10.mysqlInput.read() & 255) << 8)) | ((r10.mysqlInput.read() & 255) << 16);
                            remaining -= 3;
                            break;
                        case 254:
                            lengthRead2 = lengthRead;
                            i2 = (int) (((((((((long) (r10.mysqlInput.read() & 255)) | (((long) (r10.mysqlInput.read() & 255)) << 8)) | (((long) (r10.mysqlInput.read() & 255)) << 16)) | (((long) (r10.mysqlInput.read() & 255)) << 24)) | (((long) (r10.mysqlInput.read() & 255)) << 32)) | (((long) (r10.mysqlInput.read() & 255)) << 40)) | (((long) (r10.mysqlInput.read() & 255)) << 48)) | (((long) (r10.mysqlInput.read() & 255)) << 56));
                            remaining -= 8;
                            i6 = 16;
                            break;
                        default:
                            lengthRead2 = lengthRead;
                            i6 = 16;
                            i4 = 8;
                            i2 = sw;
                            break;
                    }
                    i4 = 8;
                    if (i2 == -1) {
                        obj = null;
                        rowData2[i5] = null;
                    } else {
                        obj = null;
                        if (i2 == 0) {
                            rowData2[i5] = Constants.EMPTY_BYTE_ARRAY;
                        } else {
                            rowData2[i5] = new byte[i2];
                            lengthRead = 0;
                            int bytesRead = readFully(r10.mysqlInput, rowData2[i5], 0, i2);
                            if (bytesRead != i2) {
                                MySQLConnection mySQLConnection = r10.connection;
                                long j = r10.lastPacketSentTimeMs;
                                long j2 = r10.lastPacketReceivedTimeMs;
                                throw SQLError.createCommunicationsException(mySQLConnection, j, j2, new IOException(Messages.getString("MysqlIO.43")), getExceptionInterceptor());
                            }
                            remaining -= bytesRead;
                            i5++;
                            obj2 = obj;
                            sw = i6;
                            i6 = i4;
                            i3 = lengthRead;
                            lengthRead = lengthRead2;
                            i2 = 4;
                            rowData = 1;
                            i4 = 2;
                            i = columnCount;
                        }
                    }
                    lengthRead = 0;
                    i5++;
                    obj2 = obj;
                    sw = i6;
                    i6 = i4;
                    i3 = lengthRead;
                    lengthRead = lengthRead2;
                    i2 = 4;
                    rowData = 1;
                    i4 = 2;
                    i = columnCount;
                }
                if (remaining > 0) {
                    skipFully(r10.mysqlInput, (long) remaining);
                }
                return new ByteArrayRow(rowData2, getExceptionInterceptor());
            }
        } catch (IOException e) {
            IOException ioEx = e;
            MySQLConnection mySQLConnection2 = r10.connection;
            long j3 = r10.lastPacketSentTimeMs;
            throw SQLError.createCommunicationsException(mySQLConnection2, j3, r10.lastPacketReceivedTimeMs, ioEx, getExceptionInterceptor());
        }
    }

    final void quit() throws SQLException {
        try {
            if (!this.mysqlConnection.isClosed()) {
                try {
                    this.mysqlConnection.shutdownInput();
                } catch (UnsupportedOperationException e) {
                }
            }
        } catch (IOException ioEx) {
            this.connection.getLog().logWarn("Caught while disconnecting...", ioEx);
        } catch (Throwable th) {
            forceClose();
        }
        Buffer packet = new Buffer(6);
        this.packetSequence = (byte) -1;
        this.compressedPacketSequence = (byte) -1;
        packet.writeByte((byte) 1);
        send(packet, packet.getPosition());
        forceClose();
    }

    Buffer getSharedSendPacket() {
        if (this.sharedSendPacket == null) {
            this.sharedSendPacket = new Buffer(1024);
        }
        return this.sharedSendPacket;
    }

    void closeStreamer(RowData streamer) throws SQLException {
        StringBuilder stringBuilder;
        if (this.streamingData == null) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(Messages.getString("MysqlIO.17"));
            stringBuilder.append(streamer);
            stringBuilder.append(Messages.getString("MysqlIO.18"));
            throw SQLError.createSQLException(stringBuilder.toString(), getExceptionInterceptor());
        } else if (streamer != this.streamingData) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(Messages.getString("MysqlIO.19"));
            stringBuilder.append(streamer);
            stringBuilder.append(Messages.getString("MysqlIO.20"));
            stringBuilder.append(Messages.getString("MysqlIO.21"));
            stringBuilder.append(Messages.getString("MysqlIO.22"));
            throw SQLError.createSQLException(stringBuilder.toString(), getExceptionInterceptor());
        } else {
            this.streamingData = null;
        }
    }

    boolean tackOnMoreStreamingResults(ResultSetImpl addingTo) throws SQLException {
        if ((this.serverStatus & 8) == 0) {
            return false;
        }
        ResultSetImpl resultSetImpl;
        boolean moreRowSetsExist = true;
        ResultSetImpl currentResultSet = addingTo;
        boolean firstTime = true;
        while (moreRowSetsExist) {
            if (!firstTime && currentResultSet.reallyResult()) {
                resultSetImpl = currentResultSet;
                break;
            }
            Buffer fieldPacket = checkErrorPacket();
            fieldPacket.setPosition(0);
            Statement owningStatement = addingTo.getStatement();
            StatementImpl statementImpl = (StatementImpl) owningStatement;
            resultSetImpl = currentResultSet;
            ResultSetImpl newResultSet = readResultsForQueryOrUpdate(statementImpl, owningStatement.getMaxRows(), owningStatement.getResultSetType(), owningStatement.getResultSetConcurrency(), true, owningStatement.getConnection().getCatalog(), fieldPacket, addingTo.isBinaryEncoded, -1, null);
            resultSetImpl.setNextResultSet(newResultSet);
            currentResultSet = newResultSet;
            moreRowSetsExist = (r12.serverStatus & 8) != 0;
            if (!currentResultSet.reallyResult() && !moreRowSetsExist) {
                return false;
            }
            firstTime = false;
        }
        resultSetImpl = currentResultSet;
        return true;
    }

    ResultSetImpl readAllResults(StatementImpl callingStatement, int maxRows, int resultSetType, int resultSetConcurrency, boolean streamResults, String catalog, Buffer resultPacket, boolean isBinaryEncoded, long preSentColumnCount, Field[] metadataFromCache) throws SQLException {
        resultPacket.setPosition(resultPacket.getPosition() - 1);
        ResultSetImpl topLevelResultSet = readResultsForQueryOrUpdate(callingStatement, maxRows, resultSetType, resultSetConcurrency, streamResults, catalog, resultPacket, isBinaryEncoded, preSentColumnCount, metadataFromCache);
        ResultSetImpl currentResultSet = topLevelResultSet;
        boolean z = false;
        boolean checkForMoreResults = (this.clientParam & PlaybackStateCompat.ACTION_PREPARE_FROM_URI) != 0;
        boolean serverHasMoreResults = (r12.serverStatus & 8) != 0;
        if (serverHasMoreResults && streamResults) {
            if (topLevelResultSet.getUpdateCount() != -1) {
                tackOnMoreStreamingResults(topLevelResultSet);
            }
            reclaimLargeReusablePacket();
            return topLevelResultSet;
        }
        ResultSetImpl currentResultSet2;
        ResultSetImpl topLevelResultSet2;
        boolean moreRowSetsExist = checkForMoreResults & serverHasMoreResults;
        ResultSetImpl currentResultSet3 = currentResultSet;
        while (moreRowSetsExist) {
            Buffer fieldPacket = checkErrorPacket();
            fieldPacket.setPosition(z);
            boolean z2 = z;
            currentResultSet2 = currentResultSet3;
            topLevelResultSet2 = topLevelResultSet;
            currentResultSet = readResultsForQueryOrUpdate(callingStatement, maxRows, resultSetType, resultSetConcurrency, streamResults, catalog, fieldPacket, isBinaryEncoded, preSentColumnCount, metadataFromCache);
            currentResultSet2.setNextResultSet(currentResultSet);
            currentResultSet3 = currentResultSet;
            moreRowSetsExist = (r12.serverStatus & 8) != 0 ? true : z2;
            z = z2;
            topLevelResultSet = topLevelResultSet2;
        }
        currentResultSet2 = currentResultSet3;
        topLevelResultSet2 = topLevelResultSet;
        if (!streamResults) {
            clearInputStream();
        }
        reclaimLargeReusablePacket();
        return topLevelResultSet2;
    }

    void resetMaxBuf() {
        this.maxAllowedPacket = this.connection.getMaxAllowedPacket();
    }

    final Buffer sendCommand(int command, String extraData, Buffer queryPacket, boolean skipCheck, String extraDataCharEncoding, int timeoutMillis) throws SQLException {
        MysqlIO this;
        int command2 = command;
        String str = extraData;
        Buffer buffer = queryPacket;
        int i = timeoutMillis;
        this.commandCount++;
        this.enablePacketDebug = this.connection.getEnablePacketDebug();
        this.readPacketSequence = (byte) 0;
        int oldTimeout = 0;
        if (i != 0) {
            try {
                oldTimeout = r1.mysqlConnection.getSoTimeout();
                r1.mysqlConnection.setSoTimeout(i);
            } catch (SocketException e) {
                SocketException e2 = e;
                throw SQLError.createCommunicationsException(r1.connection, r1.lastPacketSentTimeMs, r1.lastPacketReceivedTimeMs, e2, getExceptionInterceptor());
            }
        }
        int oldTimeout2 = oldTimeout;
        String extraData2;
        Buffer queryPacket2;
        int oldTimeout3;
        try {
            checkForOutstandingStreamingData();
            r1.oldServerStatus = r1.serverStatus;
            r1.serverStatus = 0;
            r1.hadWarnings = false;
            r1.warningCount = 0;
            r1.queryNoIndexUsed = false;
            r1.queryBadIndexUsed = false;
            r1.serverQueryWasSlow = false;
            if (r1.useCompression) {
                oldTimeout = r1.mysqlInput.available();
                if (oldTimeout > 0) {
                    r1.mysqlInput.skip((long) oldTimeout);
                }
            }
            clearInputStream();
            if (buffer == null) {
                int packLength = (8 + (str != null ? extraData.length() : 0)) + 2;
                if (r1.sendPacket == null) {
                    r1.sendPacket = new Buffer(packLength);
                }
                r1.packetSequence = (byte) -1;
                r1.compressedPacketSequence = (byte) -1;
                r1.readPacketSequence = (byte) 0;
                r1.checkPacketSequence = true;
                r1.sendPacket.clear();
                r1.sendPacket.writeByte((byte) command2);
                if (command2 == 2 || command2 == 3 || command2 == 22) {
                    if (extraDataCharEncoding == null) {
                        r1.sendPacket.writeStringNoNull(str);
                    } else {
                        r1.sendPacket.writeStringNoNull(str, extraDataCharEncoding, r1.connection.getServerCharset(), r1.connection.parserKnowsUnicode(), r1.connection);
                    }
                }
                send(r1.sendPacket, r1.sendPacket.getPosition());
            } else {
                r1.packetSequence = (byte) -1;
                r1.compressedPacketSequence = (byte) -1;
                send(buffer, queryPacket.getPosition());
            }
            Buffer returnPacket = null;
            if (!skipCheck) {
                if (command2 == 23 || command2 == 26) {
                    r1.readPacketSequence = (byte) 0;
                    r1.packetSequenceReset = true;
                }
                returnPacket = checkErrorPacket(command);
            }
            this = r1;
            extraData2 = str;
            queryPacket2 = buffer;
            oldTimeout3 = oldTimeout2;
            if (i != 0) {
                try {
                    this.mysqlConnection.setSoTimeout(oldTimeout3);
                    int i2 = command2;
                } catch (SocketException e3) {
                    throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, e3, getExceptionInterceptor());
                }
            }
            return returnPacket;
        } catch (SQLException e4) {
            throw e4;
        } catch (Exception e5) {
            Exception ex = e5;
            throw SQLError.createCommunicationsException(r1.connection, r1.lastPacketSentTimeMs, r1.lastPacketReceivedTimeMs, ex, getExceptionInterceptor());
        } catch (IOException e6) {
            IOException ioEx = e6;
            preserveOldTransactionState();
            throw SQLError.createCommunicationsException(r1.connection, r1.lastPacketSentTimeMs, r1.lastPacketReceivedTimeMs, ioEx, getExceptionInterceptor());
        } catch (SQLException e42) {
            SQLException e7 = e42;
            preserveOldTransactionState();
            throw e7;
        } catch (Throwable th) {
            Throwable th2 = th;
            this = r1;
            extraData2 = str;
            queryPacket2 = buffer;
            boolean z = skipCheck;
            String str2 = extraDataCharEncoding;
            oldTimeout3 = oldTimeout2;
            if (i != 0) {
                try {
                    this.mysqlConnection.setSoTimeout(oldTimeout3);
                    int i3 = command2;
                } catch (SocketException e32) {
                    throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, e32, getExceptionInterceptor());
                }
            }
        }
    }

    protected boolean shouldIntercept() {
        return this.statementInterceptors != null;
    }

    final ResultSetInternalMethods sqlQueryDirect(StatementImpl callingStatement, String query, String characterEncoding, Buffer queryPacket, int maxRows, int resultSetType, int resultSetConcurrency, boolean streamResults, String catalog, Field[] cachedMetadata) throws Exception {
        Buffer queryPacket2;
        SQLException sQLException;
        SQLException e;
        SQLException sqlEx;
        SQLException cause;
        Throwable th;
        Throwable th2;
        StatementImpl callingStatement2;
        StatementImpl statementImpl = callingStatement;
        String str = query;
        this.statementExecutionDepth++;
        Buffer queryPacket3;
        try {
            ResultSetInternalMethods interceptedResults;
            StringBuilder stringBuilder;
            String stringBuilder2;
            int packLength;
            long j;
            int i;
            if (r13.statementInterceptors != null) {
                interceptedResults = invokeStatementInterceptorsPre(str, statementImpl, false);
                if (interceptedResults != null) {
                    r13.statementExecutionDepth--;
                    return interceptedResults;
                }
            }
            int oldPacketPosition = 0;
            String statementComment = r13.connection.getStatementComment();
            if (r13.connection.getIncludeThreadNamesAsStatementComment()) {
                stringBuilder = new StringBuilder();
                if (statementComment != null) {
                    StringBuilder stringBuilder3 = new StringBuilder();
                    stringBuilder3.append(statementComment);
                    stringBuilder3.append(", ");
                    stringBuilder2 = stringBuilder3.toString();
                } else {
                    stringBuilder2 = "";
                }
                stringBuilder.append(stringBuilder2);
                stringBuilder.append("java thread: ");
                stringBuilder.append(Thread.currentThread().getName());
                statementComment = stringBuilder.toString();
            }
            String statementComment2 = statementComment;
            long queryEndTime;
            if (str != null) {
                packLength = ((query.length() * 3) + 5) + 2;
                byte[] commentAsBytes = null;
                if (statementComment2 != null) {
                    commentAsBytes = StringUtils.getBytes(statementComment2, null, characterEncoding, r13.connection.getServerCharset(), r13.connection.parserKnowsUnicode(), getExceptionInterceptor());
                    packLength = (packLength + commentAsBytes.length) + 6;
                }
                int packLength2 = packLength;
                byte[] commentAsBytes2 = commentAsBytes;
                if (r13.sendPacket == null) {
                    r13.sendPacket = new Buffer(packLength2);
                } else {
                    r13.sendPacket.clear();
                }
                r13.sendPacket.writeByte((byte) 3);
                if (commentAsBytes2 != null) {
                    r13.sendPacket.writeBytesNoNull(Constants.SLASH_STAR_SPACE_AS_BYTES);
                    r13.sendPacket.writeBytesNoNull(commentAsBytes2);
                    r13.sendPacket.writeBytesNoNull(Constants.SPACE_STAR_SLASH_SPACE_AS_BYTES);
                }
                byte[] bArr;
                if (characterEncoding == null) {
                    bArr = commentAsBytes2;
                    j = 0;
                    i = 5;
                    queryEndTime = 3;
                    r13.sendPacket.writeStringNoNull(str);
                } else if (r13.platformDbCharsetMatches) {
                    j = 0;
                    i = 5;
                    queryEndTime = 3;
                    r13.sendPacket.writeStringNoNull(str, characterEncoding, r13.connection.getServerCharset(), r13.connection.parserKnowsUnicode(), r13.connection);
                } else {
                    bArr = commentAsBytes2;
                    j = 0;
                    i = 5;
                    queryEndTime = 3;
                    if (StringUtils.startsWithIgnoreCaseAndWs(str, "LOAD DATA")) {
                        r13.sendPacket.writeBytesNoNull(StringUtils.getBytes(query));
                    } else {
                        r13.sendPacket.writeStringNoNull(str, characterEncoding, r13.connection.getServerCharset(), r13.connection.parserKnowsUnicode(), r13.connection);
                    }
                }
                queryPacket2 = r13.sendPacket;
            } else {
                j = 0;
                i = 5;
                queryEndTime = 3;
                queryPacket2 = queryPacket;
            }
            byte[] bArr2 = null;
            int i2 = 0;
            try {
                if (r13.needToGrabQueryFromPacket) {
                    try {
                        bArr2 = queryPacket2.getByteBuffer();
                        i2 = queryPacket2.getPosition();
                        oldPacketPosition = getCurrentTimeNanosOrMillis();
                    } catch (SQLException e2) {
                        sQLException = e2;
                        queryPacket3 = queryPacket2;
                        sqlEx = sQLException;
                        try {
                            if (r13.statementInterceptors != null) {
                                invokeStatementInterceptorsPost(query, statementImpl, null, false, sqlEx);
                            }
                            if (statementImpl != null) {
                                synchronized (statementImpl.cancelTimeoutMutex) {
                                    if (statementImpl.wasCancelled) {
                                        if (statementImpl.wasCancelledByTimeout) {
                                            cause = new MySQLTimeoutException();
                                        } else {
                                            cause = new MySQLStatementCancelledException();
                                        }
                                        callingStatement.resetCancelledState();
                                        throw cause;
                                    }
                                }
                            }
                            throw sqlEx;
                        } catch (Throwable th3) {
                            th = th3;
                            th2 = th;
                            callingStatement2 = statementImpl;
                            queryPacket2 = queryPacket3;
                            r13.statementExecutionDepth--;
                            throw th2;
                        }
                    } catch (Throwable th4) {
                        th2 = th4;
                        queryPacket3 = queryPacket2;
                        callingStatement2 = statementImpl;
                        queryPacket2 = queryPacket3;
                        r13.statementExecutionDepth--;
                        throw th2;
                    }
                }
                long queryStartTime = oldPacketPosition;
                byte[] queryBuf = bArr2;
                oldPacketPosition = i2;
                if (r13.autoGenerateTestcaseScript) {
                    if (str == null) {
                        statementComment = StringUtils.toString(queryBuf, i, oldPacketPosition - 5);
                    } else if (statementComment2 != null) {
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("/* ");
                        stringBuilder.append(statementComment2);
                        stringBuilder.append(" */ ");
                        stringBuilder.append(str);
                        statementComment = stringBuilder.toString();
                    } else {
                        statementComment = str;
                    }
                    stringBuilder = new StringBuilder(statementComment.length() + 32);
                    r13.connection.generateConnectionCommentBlock(stringBuilder);
                    stringBuilder.append(statementComment);
                    stringBuilder.append(';');
                    r13.connection.dumpTestcaseQuery(stringBuilder.toString());
                }
                Buffer queryPacket4 = queryPacket2;
                int oldPacketPosition2 = oldPacketPosition;
                try {
                    long fetchBeginTime;
                    boolean queryWasSlow;
                    String statementComment3;
                    int oldPacketPosition3;
                    int oldPacketPosition4;
                    ResultSetInternalMethods rs;
                    Object[] objArr;
                    int oldPacketPosition5;
                    Log log;
                    StringBuilder stringBuilder4;
                    ProfilerEventHandler eventSink;
                    String str2;
                    long id;
                    int id2;
                    int i3;
                    long currentTimeMillis;
                    long j2;
                    String str3;
                    String findCallingClassAndMethod;
                    long fetchEndTime;
                    String str4;
                    long id3;
                    StatementImpl callingStatement3;
                    byte[] queryBuf2 = queryBuf;
                    Buffer resultPacket = sendCommand(3, null, queryPacket2, false, null, 0);
                    stringBuilder2 = null;
                    boolean queryWasSlow2 = false;
                    if (!r13.profileSql) {
                        try {
                            if (!r13.logSlowQueries) {
                                fetchBeginTime = 0;
                                queryWasSlow = false;
                                statementComment3 = statementComment2;
                                oldPacketPosition3 = oldPacketPosition2;
                                statementComment2 = null;
                                str = statementComment2;
                                oldPacketPosition4 = oldPacketPosition3;
                                rs = readAllResults(statementImpl, maxRows, resultSetType, resultSetConcurrency, streamResults, catalog, resultPacket, false, -1, cachedMetadata);
                                packLength = 999;
                                if (queryWasSlow && !r13.serverQueryWasSlow) {
                                    stringBuilder = new StringBuilder(48 + str.length());
                                    stringBuilder2 = "MysqlIO.SlowQuery";
                                    objArr = new Object[3];
                                    objArr[0] = String.valueOf(r13.useAutoSlowLog ? " 95% of all queries " : Long.valueOf(r13.slowQueryThreshold));
                                    objArr[1] = r13.queryTimingUnits;
                                    objArr[2] = Long.valueOf(j - queryStartTime);
                                    stringBuilder.append(Messages.getString(stringBuilder2, objArr));
                                    stringBuilder.append(str);
                                    ProfilerEventHandlerFactory.getInstance(r13.connection).consumeEvent(new ProfilerEvent((byte) 6, "", catalog, r13.connection.getId(), statementImpl == null ? callingStatement.getId() : 999, ((ResultSetImpl) rs).resultId, System.currentTimeMillis(), (long) ((int) (j - queryStartTime)), r13.queryTimingUnits, null, LogUtils.findCallingClassAndMethod(new Throwable()), stringBuilder.toString()));
                                    if (r13.connection.getExplainSlowQueries()) {
                                        oldPacketPosition5 = oldPacketPosition4;
                                        if (oldPacketPosition5 >= 1048576) {
                                            queryPacket3 = queryPacket4;
                                            try {
                                                explainSlowQuery(queryPacket3.getBytes(5, oldPacketPosition5 - 5), str);
                                            } catch (SQLException e3) {
                                                e2 = e3;
                                                sQLException = e2;
                                                sqlEx = sQLException;
                                                if (r13.statementInterceptors != null) {
                                                    invokeStatementInterceptorsPost(query, statementImpl, null, false, sqlEx);
                                                }
                                                if (statementImpl != null) {
                                                    synchronized (statementImpl.cancelTimeoutMutex) {
                                                        if (statementImpl.wasCancelled) {
                                                        } else {
                                                            if (statementImpl.wasCancelledByTimeout) {
                                                                cause = new MySQLStatementCancelledException();
                                                            } else {
                                                                cause = new MySQLTimeoutException();
                                                            }
                                                            callingStatement.resetCancelledState();
                                                            throw cause;
                                                        }
                                                    }
                                                }
                                                throw sqlEx;
                                            }
                                        }
                                        queryPacket3 = queryPacket4;
                                        log = r13.connection.getLog();
                                        stringBuilder4 = new StringBuilder();
                                        stringBuilder4.append(Messages.getString("MysqlIO.28"));
                                        stringBuilder4.append(1048576);
                                        stringBuilder4.append(Messages.getString("MysqlIO.29"));
                                        log.logWarn(stringBuilder4.toString());
                                        if (r13.logSlowQueries) {
                                            eventSink = ProfilerEventHandlerFactory.getInstance(r13.connection);
                                            if (r13.queryBadIndexUsed && r13.profileSql) {
                                                str2 = "";
                                                id = r13.connection.getId();
                                                id2 = statementImpl == null ? callingStatement.getId() : 999;
                                                i3 = ((ResultSetImpl) rs).resultId;
                                                currentTimeMillis = System.currentTimeMillis();
                                                j2 = j - queryStartTime;
                                                str3 = r13.queryTimingUnits;
                                                findCallingClassAndMethod = LogUtils.findCallingClassAndMethod(new Throwable());
                                                stringBuilder4 = new StringBuilder();
                                                stringBuilder4.append(Messages.getString("MysqlIO.33"));
                                                stringBuilder4.append(str);
                                                eventSink.consumeEvent(new ProfilerEvent((byte) 6, str2, catalog, id, id2, i3, currentTimeMillis, j2, str3, null, findCallingClassAndMethod, stringBuilder4.toString()));
                                            }
                                            if (r13.queryNoIndexUsed && r13.profileSql) {
                                                str2 = "";
                                                id = r13.connection.getId();
                                                id2 = statementImpl == null ? callingStatement.getId() : 999;
                                                i3 = ((ResultSetImpl) rs).resultId;
                                                currentTimeMillis = System.currentTimeMillis();
                                                j2 = j - queryStartTime;
                                                str3 = r13.queryTimingUnits;
                                                findCallingClassAndMethod = LogUtils.findCallingClassAndMethod(new Throwable());
                                                stringBuilder4 = new StringBuilder();
                                                stringBuilder4.append(Messages.getString("MysqlIO.35"));
                                                stringBuilder4.append(str);
                                                eventSink.consumeEvent(new ProfilerEvent((byte) 6, str2, catalog, id, id2, i3, currentTimeMillis, j2, str3, null, findCallingClassAndMethod, stringBuilder4.toString()));
                                            }
                                            if (r13.serverQueryWasSlow && r13.profileSql) {
                                                str2 = "";
                                                id = r13.connection.getId();
                                                id2 = statementImpl == null ? callingStatement.getId() : 999;
                                                i3 = ((ResultSetImpl) rs).resultId;
                                                currentTimeMillis = System.currentTimeMillis();
                                                j2 = j - queryStartTime;
                                                str3 = r13.queryTimingUnits;
                                                findCallingClassAndMethod = LogUtils.findCallingClassAndMethod(new Throwable());
                                                stringBuilder4 = new StringBuilder();
                                                stringBuilder4.append(Messages.getString("MysqlIO.ServerSlowQuery"));
                                                stringBuilder4.append(str);
                                                eventSink.consumeEvent(new ProfilerEvent((byte) 6, str2, catalog, id, id2, i3, currentTimeMillis, j2, str3, null, findCallingClassAndMethod, stringBuilder4.toString()));
                                            }
                                        }
                                        if (r13.profileSql) {
                                            fetchEndTime = getCurrentTimeNanosOrMillis();
                                            eventSink = ProfilerEventHandlerFactory.getInstance(r13.connection);
                                            eventSink.consumeEvent(new ProfilerEvent((byte) 3, "", catalog, r13.connection.getId(), statementImpl == null ? callingStatement.getId() : 999, ((ResultSetImpl) rs).resultId, System.currentTimeMillis(), j - queryStartTime, r13.queryTimingUnits, null, LogUtils.findCallingClassAndMethod(new Throwable()), str));
                                            str4 = "";
                                            id3 = r13.connection.getId();
                                            if (statementImpl != null) {
                                                packLength = callingStatement.getId();
                                            }
                                            eventSink.consumeEvent(new ProfilerEvent((byte) 5, str4, catalog, id3, packLength, ((ResultSetImpl) rs).resultId, System.currentTimeMillis(), fetchEndTime - fetchBeginTime, r13.queryTimingUnits, null, LogUtils.findCallingClassAndMethod(new Throwable()), null));
                                        }
                                        if (r13.hadWarnings) {
                                            scanForAndThrowDataTruncation();
                                        }
                                        if (r13.statementInterceptors != null) {
                                            interceptedResults = invokeStatementInterceptorsPost(query, statementImpl, rs, false, null);
                                            if (interceptedResults != null) {
                                                rs = interceptedResults;
                                            }
                                        }
                                        callingStatement3 = statementImpl;
                                        queryPacket2 = maxRows;
                                        queryPacket3 = resultSetType;
                                        r13.statementExecutionDepth--;
                                        return rs;
                                    }
                                }
                                queryPacket3 = queryPacket4;
                                oldPacketPosition5 = oldPacketPosition4;
                                if (r13.logSlowQueries) {
                                    eventSink = ProfilerEventHandlerFactory.getInstance(r13.connection);
                                    str2 = "";
                                    id = r13.connection.getId();
                                    if (statementImpl == null) {
                                    }
                                    i3 = ((ResultSetImpl) rs).resultId;
                                    currentTimeMillis = System.currentTimeMillis();
                                    j2 = j - queryStartTime;
                                    str3 = r13.queryTimingUnits;
                                    findCallingClassAndMethod = LogUtils.findCallingClassAndMethod(new Throwable());
                                    stringBuilder4 = new StringBuilder();
                                    stringBuilder4.append(Messages.getString("MysqlIO.33"));
                                    stringBuilder4.append(str);
                                    eventSink.consumeEvent(new ProfilerEvent((byte) 6, str2, catalog, id, id2, i3, currentTimeMillis, j2, str3, null, findCallingClassAndMethod, stringBuilder4.toString()));
                                    str2 = "";
                                    id = r13.connection.getId();
                                    if (statementImpl == null) {
                                    }
                                    i3 = ((ResultSetImpl) rs).resultId;
                                    currentTimeMillis = System.currentTimeMillis();
                                    j2 = j - queryStartTime;
                                    str3 = r13.queryTimingUnits;
                                    findCallingClassAndMethod = LogUtils.findCallingClassAndMethod(new Throwable());
                                    stringBuilder4 = new StringBuilder();
                                    stringBuilder4.append(Messages.getString("MysqlIO.35"));
                                    stringBuilder4.append(str);
                                    eventSink.consumeEvent(new ProfilerEvent((byte) 6, str2, catalog, id, id2, i3, currentTimeMillis, j2, str3, null, findCallingClassAndMethod, stringBuilder4.toString()));
                                    str2 = "";
                                    id = r13.connection.getId();
                                    if (statementImpl == null) {
                                    }
                                    i3 = ((ResultSetImpl) rs).resultId;
                                    currentTimeMillis = System.currentTimeMillis();
                                    j2 = j - queryStartTime;
                                    str3 = r13.queryTimingUnits;
                                    findCallingClassAndMethod = LogUtils.findCallingClassAndMethod(new Throwable());
                                    stringBuilder4 = new StringBuilder();
                                    stringBuilder4.append(Messages.getString("MysqlIO.ServerSlowQuery"));
                                    stringBuilder4.append(str);
                                    eventSink.consumeEvent(new ProfilerEvent((byte) 6, str2, catalog, id, id2, i3, currentTimeMillis, j2, str3, null, findCallingClassAndMethod, stringBuilder4.toString()));
                                }
                                if (r13.profileSql) {
                                    fetchEndTime = getCurrentTimeNanosOrMillis();
                                    eventSink = ProfilerEventHandlerFactory.getInstance(r13.connection);
                                    if (statementImpl == null) {
                                    }
                                    eventSink.consumeEvent(new ProfilerEvent((byte) 3, "", catalog, r13.connection.getId(), statementImpl == null ? callingStatement.getId() : 999, ((ResultSetImpl) rs).resultId, System.currentTimeMillis(), j - queryStartTime, r13.queryTimingUnits, null, LogUtils.findCallingClassAndMethod(new Throwable()), str));
                                    str4 = "";
                                    id3 = r13.connection.getId();
                                    if (statementImpl != null) {
                                        packLength = callingStatement.getId();
                                    }
                                    eventSink.consumeEvent(new ProfilerEvent((byte) 5, str4, catalog, id3, packLength, ((ResultSetImpl) rs).resultId, System.currentTimeMillis(), fetchEndTime - fetchBeginTime, r13.queryTimingUnits, null, LogUtils.findCallingClassAndMethod(new Throwable()), null));
                                }
                                if (r13.hadWarnings) {
                                    scanForAndThrowDataTruncation();
                                }
                                if (r13.statementInterceptors != null) {
                                    interceptedResults = invokeStatementInterceptorsPost(query, statementImpl, rs, false, null);
                                    if (interceptedResults != null) {
                                        rs = interceptedResults;
                                    }
                                }
                                callingStatement3 = statementImpl;
                                queryPacket2 = maxRows;
                                queryPacket3 = resultSetType;
                                r13.statementExecutionDepth--;
                                return rs;
                            }
                        } catch (SQLException e22) {
                            sQLException = e22;
                            queryPacket3 = queryPacket4;
                            sqlEx = sQLException;
                            if (r13.statementInterceptors != null) {
                                invokeStatementInterceptorsPost(query, statementImpl, null, false, sqlEx);
                            }
                            if (statementImpl != null) {
                                synchronized (statementImpl.cancelTimeoutMutex) {
                                    if (statementImpl.wasCancelled) {
                                        if (statementImpl.wasCancelledByTimeout) {
                                            cause = new MySQLTimeoutException();
                                        } else {
                                            cause = new MySQLStatementCancelledException();
                                        }
                                        callingStatement.resetCancelledState();
                                        throw cause;
                                    }
                                }
                            }
                            throw sqlEx;
                        } catch (Throwable th42) {
                            th2 = th42;
                            queryPacket3 = queryPacket4;
                            callingStatement2 = statementImpl;
                            queryPacket2 = queryPacket3;
                            r13.statementExecutionDepth--;
                            throw th2;
                        }
                    }
                    long queryEndTime2 = getCurrentTimeNanosOrMillis();
                    queryWasSlow = false;
                    long j3;
                    if (r13.profileSql) {
                        queryWasSlow = true;
                        j3 = 0;
                        statementComment3 = statementComment2;
                    } else if (r13.logSlowQueries) {
                        long fetchBeginTime2;
                        statementComment3 = statementComment2;
                        long queryTime = queryEndTime2 - queryStartTime;
                        if (r13.useAutoSlowLog) {
                            j3 = 0;
                            fetchBeginTime2 = r13.connection.isAbonormallyLongQuery(queryTime);
                            r13.connection.reportQueryTime(queryTime);
                        } else {
                            j3 = 0;
                            fetchBeginTime2 = queryTime > ((long) r13.connection.getSlowQueryThresholdMillis()) ? 1 : null;
                        }
                        if (fetchBeginTime2 != null) {
                            queryWasSlow = true;
                            queryWasSlow2 = 1;
                        }
                    } else {
                        j3 = 0;
                        statementComment3 = statementComment2;
                    }
                    if (queryWasSlow) {
                        boolean truncated = false;
                        i2 = oldPacketPosition2;
                        oldPacketPosition3 = oldPacketPosition2;
                        if (oldPacketPosition3 > r13.connection.getMaxQuerySizeToLog()) {
                            i2 = r13.connection.getMaxQuerySizeToLog() + 5;
                            truncated = true;
                        }
                        stringBuilder2 = StringUtils.toString(queryBuf2, 5, i2 - 5);
                        if (truncated) {
                            StringBuilder stringBuilder5 = new StringBuilder();
                            stringBuilder5.append(stringBuilder2);
                            stringBuilder5.append(Messages.getString("MysqlIO.25"));
                            stringBuilder2 = stringBuilder5.toString();
                        }
                    } else {
                        oldPacketPosition3 = oldPacketPosition2;
                    }
                    fetchBeginTime = queryEndTime2;
                    statementComment2 = stringBuilder2;
                    queryWasSlow = queryWasSlow2;
                    j = queryEndTime2;
                    str = statementComment2;
                    oldPacketPosition4 = oldPacketPosition3;
                    rs = readAllResults(statementImpl, maxRows, resultSetType, resultSetConcurrency, streamResults, catalog, resultPacket, false, -1, cachedMetadata);
                    packLength = 999;
                    stringBuilder = new StringBuilder(48 + str.length());
                    stringBuilder2 = "MysqlIO.SlowQuery";
                    objArr = new Object[3];
                    if (r13.useAutoSlowLog) {
                    }
                    objArr[0] = String.valueOf(r13.useAutoSlowLog ? " 95% of all queries " : Long.valueOf(r13.slowQueryThreshold));
                    objArr[1] = r13.queryTimingUnits;
                    objArr[2] = Long.valueOf(j - queryStartTime);
                    stringBuilder.append(Messages.getString(stringBuilder2, objArr));
                    stringBuilder.append(str);
                    if (statementImpl == null) {
                    }
                    ProfilerEventHandlerFactory.getInstance(r13.connection).consumeEvent(new ProfilerEvent((byte) 6, "", catalog, r13.connection.getId(), statementImpl == null ? callingStatement.getId() : 999, ((ResultSetImpl) rs).resultId, System.currentTimeMillis(), (long) ((int) (j - queryStartTime)), r13.queryTimingUnits, null, LogUtils.findCallingClassAndMethod(new Throwable()), stringBuilder.toString()));
                    if (r13.connection.getExplainSlowQueries()) {
                        oldPacketPosition5 = oldPacketPosition4;
                        if (oldPacketPosition5 >= 1048576) {
                            queryPacket3 = queryPacket4;
                            log = r13.connection.getLog();
                            stringBuilder4 = new StringBuilder();
                            stringBuilder4.append(Messages.getString("MysqlIO.28"));
                            stringBuilder4.append(1048576);
                            stringBuilder4.append(Messages.getString("MysqlIO.29"));
                            log.logWarn(stringBuilder4.toString());
                        } else {
                            queryPacket3 = queryPacket4;
                            explainSlowQuery(queryPacket3.getBytes(5, oldPacketPosition5 - 5), str);
                        }
                        if (r13.logSlowQueries) {
                            eventSink = ProfilerEventHandlerFactory.getInstance(r13.connection);
                            str2 = "";
                            id = r13.connection.getId();
                            if (statementImpl == null) {
                            }
                            i3 = ((ResultSetImpl) rs).resultId;
                            currentTimeMillis = System.currentTimeMillis();
                            j2 = j - queryStartTime;
                            str3 = r13.queryTimingUnits;
                            findCallingClassAndMethod = LogUtils.findCallingClassAndMethod(new Throwable());
                            stringBuilder4 = new StringBuilder();
                            stringBuilder4.append(Messages.getString("MysqlIO.33"));
                            stringBuilder4.append(str);
                            eventSink.consumeEvent(new ProfilerEvent((byte) 6, str2, catalog, id, id2, i3, currentTimeMillis, j2, str3, null, findCallingClassAndMethod, stringBuilder4.toString()));
                            str2 = "";
                            id = r13.connection.getId();
                            if (statementImpl == null) {
                            }
                            i3 = ((ResultSetImpl) rs).resultId;
                            currentTimeMillis = System.currentTimeMillis();
                            j2 = j - queryStartTime;
                            str3 = r13.queryTimingUnits;
                            findCallingClassAndMethod = LogUtils.findCallingClassAndMethod(new Throwable());
                            stringBuilder4 = new StringBuilder();
                            stringBuilder4.append(Messages.getString("MysqlIO.35"));
                            stringBuilder4.append(str);
                            eventSink.consumeEvent(new ProfilerEvent((byte) 6, str2, catalog, id, id2, i3, currentTimeMillis, j2, str3, null, findCallingClassAndMethod, stringBuilder4.toString()));
                            str2 = "";
                            id = r13.connection.getId();
                            if (statementImpl == null) {
                            }
                            i3 = ((ResultSetImpl) rs).resultId;
                            currentTimeMillis = System.currentTimeMillis();
                            j2 = j - queryStartTime;
                            str3 = r13.queryTimingUnits;
                            findCallingClassAndMethod = LogUtils.findCallingClassAndMethod(new Throwable());
                            stringBuilder4 = new StringBuilder();
                            stringBuilder4.append(Messages.getString("MysqlIO.ServerSlowQuery"));
                            stringBuilder4.append(str);
                            eventSink.consumeEvent(new ProfilerEvent((byte) 6, str2, catalog, id, id2, i3, currentTimeMillis, j2, str3, null, findCallingClassAndMethod, stringBuilder4.toString()));
                        }
                        if (r13.profileSql) {
                            fetchEndTime = getCurrentTimeNanosOrMillis();
                            eventSink = ProfilerEventHandlerFactory.getInstance(r13.connection);
                            if (statementImpl == null) {
                            }
                            eventSink.consumeEvent(new ProfilerEvent((byte) 3, "", catalog, r13.connection.getId(), statementImpl == null ? callingStatement.getId() : 999, ((ResultSetImpl) rs).resultId, System.currentTimeMillis(), j - queryStartTime, r13.queryTimingUnits, null, LogUtils.findCallingClassAndMethod(new Throwable()), str));
                            str4 = "";
                            id3 = r13.connection.getId();
                            if (statementImpl != null) {
                                packLength = callingStatement.getId();
                            }
                            eventSink.consumeEvent(new ProfilerEvent((byte) 5, str4, catalog, id3, packLength, ((ResultSetImpl) rs).resultId, System.currentTimeMillis(), fetchEndTime - fetchBeginTime, r13.queryTimingUnits, null, LogUtils.findCallingClassAndMethod(new Throwable()), null));
                        }
                        if (r13.hadWarnings) {
                            scanForAndThrowDataTruncation();
                        }
                        if (r13.statementInterceptors != null) {
                            interceptedResults = invokeStatementInterceptorsPost(query, statementImpl, rs, false, null);
                            if (interceptedResults != null) {
                                rs = interceptedResults;
                            }
                        }
                        callingStatement3 = statementImpl;
                        queryPacket2 = maxRows;
                        queryPacket3 = resultSetType;
                        r13.statementExecutionDepth--;
                        return rs;
                    }
                    queryPacket3 = queryPacket4;
                    oldPacketPosition5 = oldPacketPosition4;
                    if (r13.logSlowQueries) {
                        eventSink = ProfilerEventHandlerFactory.getInstance(r13.connection);
                        str2 = "";
                        id = r13.connection.getId();
                        if (statementImpl == null) {
                        }
                        i3 = ((ResultSetImpl) rs).resultId;
                        currentTimeMillis = System.currentTimeMillis();
                        j2 = j - queryStartTime;
                        str3 = r13.queryTimingUnits;
                        findCallingClassAndMethod = LogUtils.findCallingClassAndMethod(new Throwable());
                        stringBuilder4 = new StringBuilder();
                        stringBuilder4.append(Messages.getString("MysqlIO.33"));
                        stringBuilder4.append(str);
                        eventSink.consumeEvent(new ProfilerEvent((byte) 6, str2, catalog, id, id2, i3, currentTimeMillis, j2, str3, null, findCallingClassAndMethod, stringBuilder4.toString()));
                        str2 = "";
                        id = r13.connection.getId();
                        if (statementImpl == null) {
                        }
                        i3 = ((ResultSetImpl) rs).resultId;
                        currentTimeMillis = System.currentTimeMillis();
                        j2 = j - queryStartTime;
                        str3 = r13.queryTimingUnits;
                        findCallingClassAndMethod = LogUtils.findCallingClassAndMethod(new Throwable());
                        stringBuilder4 = new StringBuilder();
                        stringBuilder4.append(Messages.getString("MysqlIO.35"));
                        stringBuilder4.append(str);
                        eventSink.consumeEvent(new ProfilerEvent((byte) 6, str2, catalog, id, id2, i3, currentTimeMillis, j2, str3, null, findCallingClassAndMethod, stringBuilder4.toString()));
                        str2 = "";
                        id = r13.connection.getId();
                        if (statementImpl == null) {
                        }
                        i3 = ((ResultSetImpl) rs).resultId;
                        currentTimeMillis = System.currentTimeMillis();
                        j2 = j - queryStartTime;
                        str3 = r13.queryTimingUnits;
                        findCallingClassAndMethod = LogUtils.findCallingClassAndMethod(new Throwable());
                        stringBuilder4 = new StringBuilder();
                        stringBuilder4.append(Messages.getString("MysqlIO.ServerSlowQuery"));
                        stringBuilder4.append(str);
                        eventSink.consumeEvent(new ProfilerEvent((byte) 6, str2, catalog, id, id2, i3, currentTimeMillis, j2, str3, null, findCallingClassAndMethod, stringBuilder4.toString()));
                    }
                    if (r13.profileSql) {
                        fetchEndTime = getCurrentTimeNanosOrMillis();
                        eventSink = ProfilerEventHandlerFactory.getInstance(r13.connection);
                        if (statementImpl == null) {
                        }
                        eventSink.consumeEvent(new ProfilerEvent((byte) 3, "", catalog, r13.connection.getId(), statementImpl == null ? callingStatement.getId() : 999, ((ResultSetImpl) rs).resultId, System.currentTimeMillis(), j - queryStartTime, r13.queryTimingUnits, null, LogUtils.findCallingClassAndMethod(new Throwable()), str));
                        str4 = "";
                        id3 = r13.connection.getId();
                        if (statementImpl != null) {
                            packLength = callingStatement.getId();
                        }
                        eventSink.consumeEvent(new ProfilerEvent((byte) 5, str4, catalog, id3, packLength, ((ResultSetImpl) rs).resultId, System.currentTimeMillis(), fetchEndTime - fetchBeginTime, r13.queryTimingUnits, null, LogUtils.findCallingClassAndMethod(new Throwable()), null));
                    }
                    if (r13.hadWarnings) {
                        scanForAndThrowDataTruncation();
                    }
                    if (r13.statementInterceptors != null) {
                        interceptedResults = invokeStatementInterceptorsPost(query, statementImpl, rs, false, null);
                        if (interceptedResults != null) {
                            rs = interceptedResults;
                        }
                    }
                    callingStatement3 = statementImpl;
                    queryPacket2 = maxRows;
                    queryPacket3 = resultSetType;
                    r13.statementExecutionDepth--;
                    return rs;
                } catch (SQLException e222) {
                    queryPacket3 = queryPacket4;
                    sQLException = e222;
                    sqlEx = sQLException;
                    if (r13.statementInterceptors != null) {
                        invokeStatementInterceptorsPost(query, statementImpl, null, false, sqlEx);
                    }
                    if (statementImpl != null) {
                        synchronized (statementImpl.cancelTimeoutMutex) {
                            if (statementImpl.wasCancelled) {
                            } else {
                                if (statementImpl.wasCancelledByTimeout) {
                                    cause = new MySQLStatementCancelledException();
                                } else {
                                    cause = new MySQLTimeoutException();
                                }
                                callingStatement.resetCancelledState();
                                throw cause;
                            }
                        }
                    }
                    throw sqlEx;
                } catch (Throwable th422) {
                    queryPacket3 = queryPacket4;
                    th2 = th422;
                    callingStatement2 = statementImpl;
                    queryPacket2 = queryPacket3;
                    r13.statementExecutionDepth--;
                    throw th2;
                }
            } catch (SQLException e2222) {
                queryPacket3 = queryPacket2;
                sQLException = e2222;
                sqlEx = sQLException;
                if (r13.statementInterceptors != null) {
                    invokeStatementInterceptorsPost(query, statementImpl, null, false, sqlEx);
                }
                if (statementImpl != null) {
                    synchronized (statementImpl.cancelTimeoutMutex) {
                        if (statementImpl.wasCancelled) {
                            if (statementImpl.wasCancelledByTimeout) {
                                cause = new MySQLTimeoutException();
                            } else {
                                cause = new MySQLStatementCancelledException();
                            }
                            callingStatement.resetCancelledState();
                            throw cause;
                        }
                    }
                }
                throw sqlEx;
            } catch (Throwable th4222) {
                queryPacket3 = queryPacket2;
                th2 = th4222;
                callingStatement2 = statementImpl;
                queryPacket2 = queryPacket3;
                r13.statementExecutionDepth--;
                throw th2;
            }
        } catch (SQLException e4) {
            e2222 = e4;
            queryPacket3 = queryPacket;
            sQLException = e2222;
            sqlEx = sQLException;
            if (r13.statementInterceptors != null) {
                invokeStatementInterceptorsPost(query, statementImpl, null, false, sqlEx);
            }
            if (statementImpl != null) {
                synchronized (statementImpl.cancelTimeoutMutex) {
                    if (statementImpl.wasCancelled) {
                        if (statementImpl.wasCancelledByTimeout) {
                            cause = new MySQLTimeoutException();
                        } else {
                            cause = new MySQLStatementCancelledException();
                        }
                        callingStatement.resetCancelledState();
                        throw cause;
                    }
                }
            }
            throw sqlEx;
        } catch (Throwable th5) {
            th4222 = th5;
            queryPacket3 = queryPacket;
            th2 = th4222;
            callingStatement2 = statementImpl;
            queryPacket2 = queryPacket3;
            r13.statementExecutionDepth--;
            throw th2;
        }
    }

    ResultSetInternalMethods invokeStatementInterceptorsPre(String sql, Statement interceptedStatement, boolean forceExecute) throws SQLException {
        ResultSetInternalMethods previousResultSet = null;
        int s = this.statementInterceptors.size();
        for (int i = 0; i < s; i++) {
            StatementInterceptorV2 interceptor = (StatementInterceptorV2) this.statementInterceptors.get(i);
            boolean executeTopLevelOnly = interceptor.executeTopLevelOnly();
            boolean shouldExecute = true;
            if (!(executeTopLevelOnly && (this.statementExecutionDepth == 1 || forceExecute)) && executeTopLevelOnly) {
                shouldExecute = false;
            }
            if (shouldExecute) {
                ResultSetInternalMethods interceptedResultSet = interceptor.preProcess(sql, interceptedStatement, this.connection);
                if (interceptedResultSet != null) {
                    previousResultSet = interceptedResultSet;
                }
            }
        }
        return previousResultSet;
    }

    ResultSetInternalMethods invokeStatementInterceptorsPost(String sql, Statement interceptedStatement, ResultSetInternalMethods originalResultSet, boolean forceExecute, SQLException statementException) throws SQLException {
        int s = this.statementInterceptors.size();
        ResultSetInternalMethods originalResultSet2 = originalResultSet;
        for (int i = 0; i < s; i++) {
            StatementInterceptorV2 interceptor = (StatementInterceptorV2) r0.statementInterceptors.get(i);
            boolean executeTopLevelOnly = interceptor.executeTopLevelOnly();
            boolean z = true;
            if (!(executeTopLevelOnly && (r0.statementExecutionDepth == 1 || forceExecute)) && executeTopLevelOnly) {
                z = false;
            }
            if (z) {
                ResultSetInternalMethods originalResultSet3 = interceptor.postProcess(sql, interceptedStatement, originalResultSet2, r0.connection, r0.warningCount, r0.queryNoIndexUsed, r0.queryBadIndexUsed, statementException);
                if (originalResultSet3 != null) {
                    originalResultSet2 = originalResultSet3;
                }
            }
        }
        return originalResultSet2;
    }

    private void calculateSlowQueryThreshold() {
        this.slowQueryThreshold = (long) this.connection.getSlowQueryThresholdMillis();
        if (this.connection.getUseNanosForElapsedTime()) {
            long nanosThreshold = this.connection.getSlowQueryThresholdNanos();
            if (nanosThreshold != 0) {
                this.slowQueryThreshold = nanosThreshold;
            } else {
                this.slowQueryThreshold *= 1000000;
            }
        }
    }

    protected long getCurrentTimeNanosOrMillis() {
        if (this.useNanosForElapsedTime) {
            return TimeUtil.getCurrentTimeNanosOrMillis();
        }
        return System.currentTimeMillis();
    }

    String getHost() {
        return this.host;
    }

    boolean isVersion(int major, int minor, int subminor) {
        return major == getServerMajorVersion() && minor == getServerMinorVersion() && subminor == getServerSubMinorVersion();
    }

    boolean versionMeetsMinimum(int major, int minor, int subminor) {
        boolean z = false;
        if (getServerMajorVersion() < major) {
            return false;
        }
        if (getServerMajorVersion() != major) {
            return true;
        }
        if (getServerMinorVersion() < minor) {
            return false;
        }
        if (getServerMinorVersion() != minor) {
            return true;
        }
        if (getServerSubMinorVersion() >= subminor) {
            z = true;
        }
        return z;
    }

    private static final String getPacketDumpToLog(Buffer packetToDump, int packetLength) {
        if (packetLength < 1024) {
            return packetToDump.dump(packetLength);
        }
        StringBuilder packetDumpBuf = new StringBuilder(4096);
        packetDumpBuf.append(packetToDump.dump(1024));
        packetDumpBuf.append(Messages.getString("MysqlIO.36"));
        packetDumpBuf.append(1024);
        packetDumpBuf.append(Messages.getString("MysqlIO.37"));
        return packetDumpBuf.toString();
    }

    private final int readFully(InputStream in, byte[] b, int off, int len) throws IOException {
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }
        int n = 0;
        while (n < len) {
            int count = in.read(b, off + n, len - n);
            if (count < 0) {
                throw new EOFException(Messages.getString("MysqlIO.EOF", new Object[]{Integer.valueOf(len), Integer.valueOf(n)}));
            }
            n += count;
        }
        return n;
    }

    private final long skipFully(InputStream in, long len) throws IOException {
        if (len < 0) {
            throw new IOException("Negative skip length not allowed");
        }
        long n = 0;
        while (n < len) {
            long count = in.skip(len - n);
            if (count < 0) {
                throw new EOFException(Messages.getString("MysqlIO.EOF", new Object[]{Long.valueOf(len), Long.valueOf(n)}));
            }
            n += count;
        }
        return n;
    }

    private final int skipLengthEncodedInteger(InputStream in) throws IOException {
        switch (in.read() & 255) {
            case MysqlDefs.FIELD_TYPE_BLOB /*252*/:
                return ((int) skipFully(in, 2)) + 1;
            case 253:
                return ((int) skipFully(in, 3)) + 1;
            case 254:
                return ((int) skipFully(in, 8)) + 1;
            default:
                return 1;
        }
    }

    protected final ResultSetImpl readResultsForQueryOrUpdate(StatementImpl callingStatement, int maxRows, int resultSetType, int resultSetConcurrency, boolean streamResults, String catalog, Buffer resultPacket, boolean isBinaryEncoded, long preSentColumnCount, Field[] metadataFromCache) throws SQLException {
        MysqlIO mysqlIO = this;
        StatementImpl statementImpl = callingStatement;
        Buffer buffer = resultPacket;
        long columnCount = resultPacket.readFieldLength();
        if (columnCount == 0) {
            return buildResultSetWithUpdates(statementImpl, buffer);
        }
        if (columnCount != -1) {
            return getResultSet(statementImpl, columnCount, maxRows, resultSetType, resultSetConcurrency, streamResults, catalog, isBinaryEncoded, metadataFromCache);
        }
        String fileName;
        String charEncoding = null;
        if (mysqlIO.connection.getUseUnicode()) {
            charEncoding = mysqlIO.connection.getEncoding();
        }
        if (mysqlIO.platformDbCharsetMatches) {
            fileName = charEncoding != null ? buffer.readString(charEncoding, getExceptionInterceptor()) : resultPacket.readString();
        } else {
            fileName = resultPacket.readString();
        }
        return sendFileToServer(statementImpl, fileName);
    }

    private int alignPacketSize(int a, int l) {
        return ((a + l) - 1) & ((l - 1) ^ -1);
    }

    private ResultSetImpl buildResultSetWithRows(StatementImpl callingStatement, String catalog, Field[] fields, RowData rows, int resultSetType, int resultSetConcurrency, boolean isBinaryEncoded) throws SQLException {
        ResultSetImpl rs;
        switch (resultSetConcurrency) {
            case 1007:
                rs = ResultSetImpl.getInstance(catalog, fields, rows, this.connection, callingStatement, false);
                if (isBinaryEncoded) {
                    rs.setBinaryEncoded();
                    break;
                }
                break;
            case 1008:
                rs = ResultSetImpl.getInstance(catalog, fields, rows, this.connection, callingStatement, true);
                break;
            default:
                return ResultSetImpl.getInstance(catalog, fields, rows, this.connection, callingStatement, false);
        }
        rs.setResultSetType(resultSetType);
        rs.setResultSetConcurrency(resultSetConcurrency);
        return rs;
    }

    private ResultSetImpl buildResultSetWithUpdates(StatementImpl callingStatement, Buffer resultPacket) throws SQLException {
        String info = null;
        try {
            long updateCount;
            long updateID;
            if (this.useNewUpdateCounts) {
                updateCount = resultPacket.newReadLength();
                updateID = resultPacket.newReadLength();
            } else {
                updateCount = resultPacket.readLength();
                updateID = resultPacket.readLength();
            }
            if (this.use41Extensions) {
                this.serverStatus = resultPacket.readInt();
                checkTransactionState(this.oldServerStatus);
                this.warningCount = resultPacket.readInt();
                if (this.warningCount > 0) {
                    this.hadWarnings = true;
                }
                resultPacket.readByte();
                setServerSlowQueryFlags();
            }
            if (this.connection.isReadInfoMsgEnabled()) {
                info = resultPacket.readString(this.connection.getErrorMessageEncoding(), getExceptionInterceptor());
            }
            ResultSetInternalMethods updateRs = ResultSetImpl.getInstance(updateCount, updateID, this.connection, callingStatement);
            if (info != null) {
                ((ResultSetImpl) updateRs).setServerInfo(info);
            }
            return (ResultSetImpl) updateRs;
        } catch (Exception ex) {
            SQLException sqlEx = SQLError.createSQLException(SQLError.get(SQLError.SQL_STATE_GENERAL_ERROR), SQLError.SQL_STATE_GENERAL_ERROR, -1, getExceptionInterceptor());
            sqlEx.initCause(ex);
            throw sqlEx;
        }
    }

    private void setServerSlowQueryFlags() {
        boolean z = false;
        this.queryBadIndexUsed = (this.serverStatus & 16) != 0;
        this.queryNoIndexUsed = (this.serverStatus & 32) != 0;
        if ((this.serverStatus & 2048) != 0) {
            z = true;
        }
        this.serverQueryWasSlow = z;
    }

    private void checkForOutstandingStreamingData() throws SQLException {
        if (this.streamingData == null) {
            return;
        }
        if (this.connection.getClobberStreamingResults()) {
            this.streamingData.getOwner().realClose(false);
            clearInputStream();
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Messages.getString("MysqlIO.39"));
        stringBuilder.append(this.streamingData);
        stringBuilder.append(Messages.getString("MysqlIO.40"));
        stringBuilder.append(Messages.getString("MysqlIO.41"));
        stringBuilder.append(Messages.getString("MysqlIO.42"));
        throw SQLError.createSQLException(stringBuilder.toString(), getExceptionInterceptor());
    }

    private Buffer compressPacket(Buffer packet, int offset, int packetLen) throws SQLException {
        byte[] compressedBytes;
        int compressedLength = packetLen;
        int uncompressedLength = 0;
        int offsetWrite = offset;
        if (packetLen < 50) {
            compressedBytes = packet.getByteBuffer();
        } else {
            byte[] bytesToCompress = packet.getByteBuffer();
            compressedBytes = new byte[(bytesToCompress.length * 2)];
            if (this.deflater == null) {
                this.deflater = new Deflater();
            }
            this.deflater.reset();
            this.deflater.setInput(bytesToCompress, offset, packetLen);
            this.deflater.finish();
            compressedLength = this.deflater.deflate(compressedBytes);
            if (compressedLength > packetLen) {
                compressedBytes = packet.getByteBuffer();
                compressedLength = packetLen;
            } else {
                uncompressedLength = packetLen;
                offsetWrite = 0;
            }
        }
        Buffer compressedPacket = new Buffer(7 + compressedLength);
        compressedPacket.setPosition(0);
        compressedPacket.writeLongInt(compressedLength);
        compressedPacket.writeByte(this.compressedPacketSequence);
        compressedPacket.writeLongInt(uncompressedLength);
        compressedPacket.writeBytesNoNull(compressedBytes, offsetWrite, compressedLength);
        return compressedPacket;
    }

    private final void readServerStatusForResultSets(Buffer rowPacket) throws SQLException {
        if (this.use41Extensions) {
            rowPacket.readByte();
            if (isEOFDeprecated()) {
                rowPacket.newReadLength();
                rowPacket.newReadLength();
                this.oldServerStatus = this.serverStatus;
                this.serverStatus = rowPacket.readInt();
                checkTransactionState(this.oldServerStatus);
                this.warningCount = rowPacket.readInt();
                if (this.warningCount > 0) {
                    this.hadWarnings = true;
                }
                rowPacket.readByte();
                if (this.connection.isReadInfoMsgEnabled()) {
                    rowPacket.readString(this.connection.getErrorMessageEncoding(), getExceptionInterceptor());
                }
            } else {
                this.warningCount = rowPacket.readInt();
                if (this.warningCount > 0) {
                    this.hadWarnings = true;
                }
                this.oldServerStatus = this.serverStatus;
                this.serverStatus = rowPacket.readInt();
                checkTransactionState(this.oldServerStatus);
            }
            setServerSlowQueryFlags();
        }
    }

    private SocketFactory createSocketFactory() throws SQLException {
        try {
            if (this.socketFactoryClassName != null) {
                return (SocketFactory) Class.forName(this.socketFactoryClassName).newInstance();
            }
            throw SQLError.createSQLException(Messages.getString("MysqlIO.75"), SQLError.SQL_STATE_UNABLE_TO_CONNECT_TO_DATASOURCE, getExceptionInterceptor());
        } catch (Exception ex) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Messages.getString("MysqlIO.76"));
            stringBuilder.append(this.socketFactoryClassName);
            stringBuilder.append(Messages.getString("MysqlIO.77"));
            SQLException sqlEx = SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_UNABLE_TO_CONNECT_TO_DATASOURCE, getExceptionInterceptor());
            sqlEx.initCause(ex);
            throw sqlEx;
        }
    }

    private void enqueuePacketForDebugging(boolean isPacketBeingSent, boolean isPacketReused, int sendLength, byte[] header, Buffer packet) throws SQLException {
        StringBuilder packetDump;
        if (this.packetDebugRingBuffer.size() + 1 > this.connection.getPacketDebugBufferSize()) {
            this.packetDebugRingBuffer.removeFirst();
        }
        int bytesToDump;
        if (isPacketBeingSent) {
            bytesToDump = Math.min(1024, sendLength);
            String packetPayload = packet.dump(bytesToDump);
            packetDump = new StringBuilder(68 + packetPayload.length());
            packetDump.append("Client ");
            packetDump.append(packet.toSuperString());
            packetDump.append("--------------------> Server\n");
            packetDump.append("\nPacket payload:\n\n");
            packetDump.append(packetPayload);
            if (bytesToDump == 1024) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("\nNote: Packet of ");
                stringBuilder.append(sendLength);
                stringBuilder.append(" bytes truncated to ");
                stringBuilder.append(1024);
                stringBuilder.append(" bytes.\n");
                packetDump.append(stringBuilder.toString());
            }
        } else {
            bytesToDump = Math.min(1024, packet.getBufLength());
            Buffer packetToDump = new Buffer(4 + bytesToDump);
            packetToDump.setPosition(0);
            packetToDump.writeBytesNoNull(header);
            packetToDump.writeBytesNoNull(packet.getBytes(0, bytesToDump));
            String packetPayload2 = packetToDump.dump(bytesToDump);
            packetDump = new StringBuilder(96 + packetPayload2.length());
            packetDump.append("Server ");
            packetDump.append(isPacketReused ? "(re-used) " : "(new) ");
            packetDump.append(packet.toSuperString());
            packetDump.append(" --------------------> Client\n");
            packetDump.append("\nPacket payload:\n\n");
            packetDump.append(packetPayload2);
            if (bytesToDump == 1024) {
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("\nNote: Packet of ");
                stringBuilder2.append(packet.getBufLength());
                stringBuilder2.append(" bytes truncated to ");
                stringBuilder2.append(1024);
                stringBuilder2.append(" bytes.\n");
                packetDump.append(stringBuilder2.toString());
            }
        }
        this.packetDebugRingBuffer.addLast(packetDump);
    }

    private RowData readSingleRowSet(long columnCount, int maxRows, int resultSetConcurrency, boolean isBinaryEncoded, Field[] fields) throws SQLException {
        long j = columnCount;
        int i = maxRows;
        ArrayList<ResultSetRow> rows = new ArrayList();
        boolean useBufferRowExplicit = useBufferRowExplicit(fields);
        ResultSetRow row = nextRow(fields, (int) j, isBinaryEncoded, resultSetConcurrency, false, useBufferRowExplicit, false, null);
        int rowCount = 0;
        if (row != null) {
            rows.add(row);
            rowCount = 1;
        }
        ResultSetRow row2 = row;
        int rowCount2 = rowCount;
        while (row2 != null) {
            row2 = nextRow(fields, (int) j, isBinaryEncoded, resultSetConcurrency, false, useBufferRowExplicit, false, null);
            if (row2 != null && (i == -1 || rowCount2 < i)) {
                rows.add(row2);
                rowCount2++;
            }
        }
        return new RowDataStatic(rows);
    }

    public static boolean useBufferRowExplicit(Field[] fields) {
        if (fields == null) {
            return false;
        }
        int i = 0;
        while (i < fields.length) {
            int sQLType = fields[i].getSQLType();
            if (!(sQLType == -4 || sQLType == -1)) {
                switch (sQLType) {
                    case 2004:
                    case 2005:
                        break;
                    default:
                        i++;
                }
            }
            return true;
        }
        return false;
    }

    private void reclaimLargeReusablePacket() {
        if (this.reusablePacket != null && this.reusablePacket.getCapacity() > 1048576) {
            this.reusablePacket = new Buffer(1024);
        }
    }

    private final Buffer reuseAndReadPacket(Buffer reuse) throws SQLException {
        return reuseAndReadPacket(reuse, -1);
    }

    private final Buffer reuseAndReadPacket(Buffer reuse, int existingPacketLength) throws SQLException {
        try {
            int packetLength;
            reuse.setWasMultiPacket(false);
            if (existingPacketLength != -1) {
                packetLength = existingPacketLength;
            } else if (readFully(this.mysqlInput, this.packetHeaderBuf, 0, 4) < 4) {
                forceClose();
                throw new IOException(Messages.getString("MysqlIO.43"));
            } else {
                packetLength = ((this.packetHeaderBuf[0] & 255) + ((this.packetHeaderBuf[1] & 255) << 8)) + ((this.packetHeaderBuf[2] & 255) << 16);
            }
            if (this.traceProtocol) {
                StringBuilder traceMessageBuf = new StringBuilder();
                traceMessageBuf.append(Messages.getString("MysqlIO.44"));
                traceMessageBuf.append(packetLength);
                traceMessageBuf.append(Messages.getString("MysqlIO.45"));
                traceMessageBuf.append(StringUtils.dumpAsHex(this.packetHeaderBuf, 4));
                this.connection.getLog().logTrace(traceMessageBuf.toString());
            }
            byte multiPacketSeq = this.packetHeaderBuf[3];
            if (this.packetSequenceReset) {
                this.packetSequenceReset = false;
            } else if (this.enablePacketDebug && this.checkPacketSequence) {
                checkPacketSequencing(multiPacketSeq);
            }
            this.readPacketSequence = multiPacketSeq;
            reuse.setPosition(0);
            if (reuse.getByteBuffer().length <= packetLength) {
                reuse.setByteBuffer(new byte[(packetLength + 1)]);
            }
            reuse.setBufLength(packetLength);
            int numBytesRead = readFully(this.mysqlInput, reuse.getByteBuffer(), 0, packetLength);
            if (numBytesRead != packetLength) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Short read, expected ");
                stringBuilder.append(packetLength);
                stringBuilder.append(" bytes, only read ");
                stringBuilder.append(numBytesRead);
                throw new IOException(stringBuilder.toString());
            }
            if (this.traceProtocol) {
                StringBuilder traceMessageBuf2 = new StringBuilder();
                traceMessageBuf2.append(Messages.getString("MysqlIO.46"));
                traceMessageBuf2.append(getPacketDumpToLog(reuse, packetLength));
                this.connection.getLog().logTrace(traceMessageBuf2.toString());
            }
            if (this.enablePacketDebug) {
                enqueuePacketForDebugging(false, true, 0, this.packetHeaderBuf, reuse);
            }
            boolean isMultiPacket = false;
            if (packetLength == this.maxThreeBytes) {
                reuse.setPosition(this.maxThreeBytes);
                isMultiPacket = true;
                packetLength = readRemainingMultiPackets(reuse, multiPacketSeq);
            }
            if (!isMultiPacket) {
                reuse.getByteBuffer()[packetLength] = (byte) 0;
            }
            if (this.connection.getMaintainTimeStats()) {
                this.lastPacketReceivedTimeMs = System.currentTimeMillis();
            }
            return reuse;
        } catch (IOException e) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, e, getExceptionInterceptor());
        } catch (OutOfMemoryError oom) {
            try {
                clearInputStream();
            } catch (Exception e2) {
            }
            try {
                this.connection.realClose(false, false, true, oom);
            } catch (Exception e3) {
            }
            throw oom;
        }
    }

    private int readRemainingMultiPackets(Buffer reuse, byte multiPacketSeq) throws IOException, SQLException {
        MysqlIO mysqlIO = this;
        Buffer buffer = reuse;
        Buffer multiPacket = null;
        int packetLength = -1;
        byte multiPacketSeq2 = multiPacketSeq;
        while (readFully(mysqlIO.mysqlInput, mysqlIO.packetHeaderBuf, 0, 4) >= 4) {
            packetLength = ((mysqlIO.packetHeaderBuf[0] & 255) + ((mysqlIO.packetHeaderBuf[1] & 255) << 8)) + ((mysqlIO.packetHeaderBuf[2] & 255) << 16);
            if (multiPacket == null) {
                multiPacket = new Buffer(packetLength);
            }
            if (mysqlIO.useNewLargePackets || packetLength != 1) {
                multiPacketSeq2 = (byte) (multiPacketSeq2 + 1);
                if (multiPacketSeq2 != mysqlIO.packetHeaderBuf[3]) {
                    throw new IOException(Messages.getString("MysqlIO.49"));
                }
                multiPacket.setPosition(0);
                multiPacket.setBufLength(packetLength);
                byte[] byteBuf = multiPacket.getByteBuffer();
                int lengthToWrite = packetLength;
                int bytesRead = readFully(mysqlIO.mysqlInput, byteBuf, 0, packetLength);
                if (bytesRead != lengthToWrite) {
                    MySQLConnection mySQLConnection = mysqlIO.connection;
                    long j = mysqlIO.lastPacketSentTimeMs;
                    long j2 = mysqlIO.lastPacketReceivedTimeMs;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(Messages.getString("MysqlIO.50"));
                    stringBuilder.append(lengthToWrite);
                    stringBuilder.append(Messages.getString("MysqlIO.51"));
                    stringBuilder.append(bytesRead);
                    stringBuilder.append(".");
                    throw SQLError.createCommunicationsException(mySQLConnection, j, j2, SQLError.createSQLException(stringBuilder.toString(), getExceptionInterceptor()), getExceptionInterceptor());
                }
                buffer.writeBytesNoNull(byteBuf, 0, lengthToWrite);
                if (packetLength != mysqlIO.maxThreeBytes) {
                }
            } else {
                clearInputStream();
            }
            buffer.setPosition(0);
            buffer.setWasMultiPacket(true);
            return packetLength;
        }
        forceClose();
        throw new IOException(Messages.getString("MysqlIO.47"));
    }

    private void checkPacketSequencing(byte multiPacketSeq) throws SQLException {
        MySQLConnection mySQLConnection;
        long j;
        long j2;
        StringBuilder stringBuilder;
        if (multiPacketSeq == Byte.MIN_VALUE && this.readPacketSequence != Byte.MAX_VALUE) {
            mySQLConnection = this.connection;
            j = this.lastPacketSentTimeMs;
            j2 = this.lastPacketReceivedTimeMs;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Packets out of order, expected packet # -128, but received packet # ");
            stringBuilder.append(multiPacketSeq);
            throw SQLError.createCommunicationsException(mySQLConnection, j, j2, new IOException(stringBuilder.toString()), getExceptionInterceptor());
        } else if (this.readPacketSequence == (byte) -1 && multiPacketSeq != (byte) 0) {
            mySQLConnection = this.connection;
            j = this.lastPacketSentTimeMs;
            j2 = this.lastPacketReceivedTimeMs;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Packets out of order, expected packet # -1, but received packet # ");
            stringBuilder.append(multiPacketSeq);
            throw SQLError.createCommunicationsException(mySQLConnection, j, j2, new IOException(stringBuilder.toString()), getExceptionInterceptor());
        } else if (multiPacketSeq != Byte.MIN_VALUE && this.readPacketSequence != (byte) -1 && multiPacketSeq != this.readPacketSequence + 1) {
            MySQLConnection mySQLConnection2 = this.connection;
            long j3 = this.lastPacketSentTimeMs;
            j = this.lastPacketReceivedTimeMs;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Packets out of order, expected packet # ");
            stringBuilder.append(this.readPacketSequence + 1);
            stringBuilder.append(", but received packet # ");
            stringBuilder.append(multiPacketSeq);
            throw SQLError.createCommunicationsException(mySQLConnection2, j3, j, new IOException(stringBuilder.toString()), getExceptionInterceptor());
        }
    }

    void enableMultiQueries() throws SQLException {
        Buffer buf = getSharedSendPacket();
        buf.clear();
        buf.writeByte((byte) 27);
        buf.writeInt(0);
        sendCommand(27, null, buf, false, null, 0);
    }

    void disableMultiQueries() throws SQLException {
        Buffer buf = getSharedSendPacket();
        buf.clear();
        buf.writeByte((byte) 27);
        buf.writeInt(1);
        sendCommand(27, null, buf, false, null, 0);
    }

    private final void send(Buffer packet, int packetLen) throws SQLException {
        try {
            if (this.maxAllowedPacket <= 0 || packetLen <= this.maxAllowedPacket) {
                if (this.serverMajorVersion < 4 || (packetLen - 4 < this.maxThreeBytes && (!this.useCompression || packetLen - 4 < this.maxThreeBytes - 3))) {
                    this.packetSequence = (byte) (this.packetSequence + 1);
                    Buffer packetToSend = packet;
                    packetToSend.setPosition(0);
                    packetToSend.writeLongInt(packetLen - 4);
                    packetToSend.writeByte(this.packetSequence);
                    if (this.useCompression) {
                        this.compressedPacketSequence = (byte) (this.compressedPacketSequence + 1);
                        int originalPacketLen = packetLen;
                        packetToSend = compressPacket(packetToSend, 0, packetLen);
                        packetLen = packetToSend.getPosition();
                        if (this.traceProtocol) {
                            StringBuilder traceMessageBuf = new StringBuilder();
                            traceMessageBuf.append(Messages.getString("MysqlIO.57"));
                            traceMessageBuf.append(getPacketDumpToLog(packetToSend, packetLen));
                            traceMessageBuf.append(Messages.getString("MysqlIO.58"));
                            traceMessageBuf.append(getPacketDumpToLog(packet, originalPacketLen));
                            this.connection.getLog().logTrace(traceMessageBuf.toString());
                        }
                    } else if (this.traceProtocol) {
                        StringBuilder traceMessageBuf2 = new StringBuilder();
                        traceMessageBuf2.append(Messages.getString("MysqlIO.59"));
                        traceMessageBuf2.append("host: '");
                        traceMessageBuf2.append(this.host);
                        traceMessageBuf2.append("' threadId: '");
                        traceMessageBuf2.append(this.threadId);
                        traceMessageBuf2.append("'\n");
                        traceMessageBuf2.append(packetToSend.dump(packetLen));
                        this.connection.getLog().logTrace(traceMessageBuf2.toString());
                    }
                    this.mysqlOutput.write(packetToSend.getByteBuffer(), 0, packetLen);
                    this.mysqlOutput.flush();
                } else {
                    sendSplitPackets(packet, packetLen);
                }
                if (this.enablePacketDebug) {
                    enqueuePacketForDebugging(true, false, packetLen + 5, this.packetHeaderBuf, packet);
                }
                if (packet == this.sharedSendPacket) {
                    reclaimLargeSharedSendPacket();
                }
                if (this.connection.getMaintainTimeStats()) {
                    this.lastPacketSentTimeMs = System.currentTimeMillis();
                }
                return;
            }
            throw new PacketTooBigException((long) packetLen, (long) this.maxAllowedPacket);
        } catch (IOException e) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, e, getExceptionInterceptor());
        }
    }

    private final ResultSetImpl sendFileToServer(StatementImpl callingStatement, String fileName) throws SQLException {
        if (this.useCompression) {
            this.compressedPacketSequence = (byte) (this.compressedPacketSequence + 1);
        }
        BufferedInputStream fileIn = null;
        Buffer filePacket = this.loadFileBufRef == null ? fileIn : (Buffer) this.loadFileBufRef.get();
        int packetLength = Math.min(Math.min(1048576 - 12, alignPacketSize(1048576 - 16, 4096) - 12), Math.min(this.connection.getMaxAllowedPacket() - 12, alignPacketSize(this.connection.getMaxAllowedPacket() - 16, 4096) - 12));
        if (filePacket == null) {
            try {
                filePacket = new Buffer(packetLength + 4);
                this.loadFileBufRef = new SoftReference(filePacket);
            } catch (OutOfMemoryError e) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Could not allocate packet of ");
                stringBuilder.append(packetLength);
                stringBuilder.append(" bytes required for LOAD DATA LOCAL INFILE operation.");
                stringBuilder.append(" Try increasing max heap allocation for JVM or decreasing server variable 'max_allowed_packet'");
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_MEMORY_ALLOCATION_FAILURE, getExceptionInterceptor());
            }
        }
        filePacket.clear();
        send(filePacket, 0);
        byte[] fileBuf = new byte[packetLength];
        try {
            if (this.connection.getAllowLoadLocalInfile()) {
                InputStream hookedStream = null;
                if (callingStatement != null) {
                    hookedStream = callingStatement.getLocalInfileInputStream();
                }
                if (hookedStream != null) {
                    fileIn = new BufferedInputStream(hookedStream);
                } else if (!this.connection.getAllowUrlInLocalInfile()) {
                    fileIn = new BufferedInputStream(new FileInputStream(fileName));
                } else if (fileName.indexOf(58) != -1) {
                    try {
                        fileIn = new BufferedInputStream(new URL(fileName).openStream());
                    } catch (MalformedURLException e2) {
                        fileIn = new BufferedInputStream(new FileInputStream(fileName));
                    }
                } else {
                    fileIn = new BufferedInputStream(new FileInputStream(fileName));
                }
                int bytesRead = 0;
                while (true) {
                    int read = fileIn.read(fileBuf);
                    bytesRead = read;
                    if (read == -1) {
                        break;
                    }
                    filePacket.clear();
                    filePacket.writeBytesNoNull(fileBuf, 0, bytesRead);
                    send(filePacket, filePacket.getPosition());
                }
                MysqlIO this = this;
                if (fileIn != null) {
                    try {
                        fileIn.close();
                    } catch (Throwable ex) {
                        throw SQLError.createSQLException(Messages.getString("MysqlIO.65"), SQLError.SQL_STATE_GENERAL_ERROR, ex, getExceptionInterceptor());
                    }
                }
                filePacket.clear();
                send(filePacket, filePacket.getPosition());
                checkErrorPacket();
                filePacket.clear();
                send(filePacket, filePacket.getPosition());
                return buildResultSetWithUpdates(callingStatement, checkErrorPacket());
            }
            throw SQLError.createSQLException(Messages.getString("MysqlIO.LoadDataLocalNotAllowed"), SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
        } catch (IOException ioEx) {
            StringBuilder messageBuf = new StringBuilder(Messages.getString("MysqlIO.60"));
            if (!(fileName == null || this.connection.getParanoid())) {
                messageBuf.append("'");
                messageBuf.append(fileName);
                messageBuf.append("'");
            }
            messageBuf.append(Messages.getString("MysqlIO.63"));
            if (!this.connection.getParanoid()) {
                messageBuf.append(Messages.getString("MysqlIO.64"));
                messageBuf.append(Util.stackTraceToString(ioEx));
            }
            throw SQLError.createSQLException(messageBuf.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        } catch (Throwable th) {
            MysqlIO this2 = this;
            if (fileIn != null) {
                try {
                    fileIn.close();
                } catch (Throwable ex2) {
                    throw SQLError.createSQLException(Messages.getString("MysqlIO.65"), SQLError.SQL_STATE_GENERAL_ERROR, ex2, getExceptionInterceptor());
                }
            }
            filePacket.clear();
            send(filePacket, filePacket.getPosition());
            checkErrorPacket();
        }
    }

    private Buffer checkErrorPacket(int command) throws SQLException {
        this.serverStatus = 0;
        try {
            Buffer resultPacket = reuseAndReadPacket(this.reusablePacket);
            checkErrorPacket(resultPacket);
            return resultPacket;
        } catch (SQLException sqlEx) {
            throw sqlEx;
        } catch (Exception e) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, e, getExceptionInterceptor());
        }
    }

    private void checkErrorPacket(Buffer resultPacket) throws SQLException {
        MysqlIO mysqlIO = this;
        Buffer buffer = resultPacket;
        if (resultPacket.readByte() != -1) {
            return;
        }
        String serverErrorMessage;
        StringBuilder errorBuf;
        if (mysqlIO.protocolVersion > (byte) 9) {
            int errno;
            int errno2 = resultPacket.readInt();
            serverErrorMessage = buffer.readString(mysqlIO.connection.getErrorMessageEncoding(), getExceptionInterceptor());
            if (serverErrorMessage.charAt(0) != '#') {
                errno = SQLError.mysqlToSqlState(errno2, mysqlIO.connection.getUseSqlStateCodes());
            } else if (serverErrorMessage.length() > 6) {
                errno = serverErrorMessage.substring(1, 6);
                serverErrorMessage = serverErrorMessage.substring(6);
                if (errno.equals(SQLError.SQL_STATE_CLI_SPECIFIC_CONDITION)) {
                    errno = SQLError.mysqlToSqlState(errno2, mysqlIO.connection.getUseSqlStateCodes());
                }
            } else {
                errno = SQLError.mysqlToSqlState(errno2, mysqlIO.connection.getUseSqlStateCodes());
            }
            clearInputStream();
            errorBuf = new StringBuilder();
            String xOpenErrorMessage = SQLError.get(errno);
            if (!(mysqlIO.connection.getUseOnlyServerErrorMessages() || xOpenErrorMessage == null)) {
                errorBuf.append(xOpenErrorMessage);
                errorBuf.append(Messages.getString("MysqlIO.68"));
            }
            errorBuf.append(serverErrorMessage);
            if (!(mysqlIO.connection.getUseOnlyServerErrorMessages() || xOpenErrorMessage == null)) {
                errorBuf.append("\"");
            }
            appendDeadlockStatusInformation(errno, errorBuf);
            if (errno == 0 || !errno.startsWith("22")) {
                throw SQLError.createSQLException(errorBuf.toString(), errno, errno2, false, getExceptionInterceptor(), mysqlIO.connection);
            }
            throw new MysqlDataTruncation(errorBuf.toString(), 0, true, false, 0, 0, errno2);
        }
        serverErrorMessage = buffer.readString(mysqlIO.connection.getErrorMessageEncoding(), getExceptionInterceptor());
        clearInputStream();
        if (serverErrorMessage.indexOf(Messages.getString("MysqlIO.70")) != -1) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(SQLError.get(SQLError.SQL_STATE_COLUMN_NOT_FOUND));
            stringBuilder.append(", ");
            stringBuilder.append(serverErrorMessage);
            throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_COLUMN_NOT_FOUND, -1, false, getExceptionInterceptor(), mysqlIO.connection);
        }
        stringBuilder = new StringBuilder(Messages.getString("MysqlIO.72"));
        stringBuilder.append(serverErrorMessage);
        stringBuilder.append("\"");
        errorBuf = new StringBuilder();
        errorBuf.append(SQLError.get(SQLError.SQL_STATE_GENERAL_ERROR));
        errorBuf.append(", ");
        errorBuf.append(stringBuilder.toString());
        throw SQLError.createSQLException(errorBuf.toString(), SQLError.SQL_STATE_GENERAL_ERROR, -1, false, getExceptionInterceptor(), mysqlIO.connection);
    }

    private void appendDeadlockStatusInformation(String xOpen, StringBuilder errorBuf) throws SQLException {
        MysqlIO this;
        if (this.connection.getIncludeInnodbStatusInDeadlockExceptions() && xOpen != null && ((xOpen.startsWith("40") || xOpen.startsWith("41")) && this.streamingData == null)) {
            ResultSet rs = null;
            try {
                ResultSetInternalMethods rs2 = sqlQueryDirect(null, "SHOW ENGINE INNODB STATUS", this.connection.getEncoding(), null, -1, 1003, 1007, false, this.connection.getCatalog(), null);
                if (rs2.next()) {
                    errorBuf.append("\n\n");
                    errorBuf.append(rs2.getString("Status"));
                } else {
                    errorBuf.append("\n\n");
                    errorBuf.append(Messages.getString("MysqlIO.NoInnoDBStatusFound"));
                }
                if (rs2 != null) {
                    rs2.close();
                }
            } catch (Exception e) {
                this = e;
                errorBuf.append("\n\n");
                errorBuf.append(Messages.getString("MysqlIO.InnoDBStatusFailed"));
                errorBuf.append("\n\n");
                errorBuf.append(Util.stackTraceToString(this));
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
        }
        this = this;
        if (this.connection.getIncludeThreadDumpInDeadlockExceptions()) {
            errorBuf.append("\n\n*** Java threads running at time of deadlock ***\n\n");
            ThreadMXBean threadMBean = ManagementFactory.getThreadMXBean();
            ThreadInfo[] threads = threadMBean.getThreadInfo(threadMBean.getAllThreadIds(), 2147483647);
            List<ThreadInfo> activeThreads = new ArrayList();
            for (ThreadInfo info : threads) {
                if (info != null) {
                    activeThreads.add(info);
                }
            }
            for (ThreadInfo threadInfo : activeThreads) {
                errorBuf.append('\"');
                errorBuf.append(threadInfo.getThreadName());
                errorBuf.append("\" tid=");
                errorBuf.append(threadInfo.getThreadId());
                errorBuf.append(" ");
                errorBuf.append(threadInfo.getThreadState());
                if (threadInfo.getLockName() != null) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(" on lock=");
                    stringBuilder.append(threadInfo.getLockName());
                    errorBuf.append(stringBuilder.toString());
                }
                if (threadInfo.isSuspended()) {
                    errorBuf.append(" (suspended)");
                }
                if (threadInfo.isInNative()) {
                    errorBuf.append(" (running in native)");
                }
                StackTraceElement[] stackTrace = threadInfo.getStackTrace();
                if (stackTrace.length > 0) {
                    errorBuf.append(" in ");
                    errorBuf.append(stackTrace[0].getClassName());
                    errorBuf.append(".");
                    errorBuf.append(stackTrace[0].getMethodName());
                    errorBuf.append("()");
                }
                errorBuf.append("\n");
                if (threadInfo.getLockOwnerName() != null) {
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("\t owned by ");
                    stringBuilder2.append(threadInfo.getLockOwnerName());
                    stringBuilder2.append(" Id=");
                    stringBuilder2.append(threadInfo.getLockOwnerId());
                    errorBuf.append(stringBuilder2.toString());
                    errorBuf.append("\n");
                }
                for (StackTraceElement ste : stackTrace) {
                    StringBuilder stringBuilder3 = new StringBuilder();
                    stringBuilder3.append("\tat ");
                    stringBuilder3.append(ste.toString());
                    errorBuf.append(stringBuilder3.toString());
                    errorBuf.append("\n");
                }
            }
        }
    }

    private final void sendSplitPackets(Buffer packet, int packetLen) throws SQLException {
        try {
            int cbuflen;
            Buffer toCompress = null;
            Buffer packetToSend = this.splitBufRef == null ? null : (Buffer) this.splitBufRef.get();
            if (this.useCompression) {
                if (this.compressBufRef != null) {
                    toCompress = (Buffer) this.compressBufRef.get();
                }
            }
            if (packetToSend == null) {
                packetToSend = new Buffer(this.maxThreeBytes + 4);
                this.splitBufRef = new SoftReference(packetToSend);
            }
            if (this.useCompression) {
                cbuflen = (((packetLen / this.maxThreeBytes) + 1) * 4) + packetLen;
                if (toCompress == null) {
                    toCompress = new Buffer(cbuflen);
                    this.compressBufRef = new SoftReference(toCompress);
                } else if (toCompress.getBufLength() < cbuflen) {
                    toCompress.setPosition(toCompress.getBufLength());
                    toCompress.ensureCapacity(cbuflen - toCompress.getBufLength());
                }
            }
            cbuflen = packetLen - 4;
            int splitSize = this.maxThreeBytes;
            byte[] origPacketBytes = packet.getByteBuffer();
            int originalPacketPos = 4;
            int splitSize2 = splitSize;
            splitSize = cbuflen;
            cbuflen = 0;
            while (splitSize >= 0) {
                this.packetSequence = (byte) (this.packetSequence + 1);
                if (splitSize < splitSize2) {
                    splitSize2 = splitSize;
                }
                packetToSend.setPosition(0);
                packetToSend.writeLongInt(splitSize2);
                packetToSend.writeByte(this.packetSequence);
                if (splitSize > 0) {
                    System.arraycopy(origPacketBytes, originalPacketPos, packetToSend.getByteBuffer(), 4, splitSize2);
                }
                if (this.useCompression) {
                    System.arraycopy(packetToSend.getByteBuffer(), 0, toCompress.getByteBuffer(), cbuflen, 4 + splitSize2);
                    cbuflen += 4 + splitSize2;
                } else {
                    this.mysqlOutput.write(packetToSend.getByteBuffer(), 0, 4 + splitSize2);
                    this.mysqlOutput.flush();
                }
                originalPacketPos += splitSize2;
                splitSize -= this.maxThreeBytes;
            }
            if (this.useCompression) {
                int len = cbuflen;
                cbuflen = 0;
                splitSize = this.maxThreeBytes - 3;
                while (len >= 0) {
                    this.compressedPacketSequence = (byte) (this.compressedPacketSequence + 1);
                    if (len < splitSize) {
                        splitSize = len;
                    }
                    Buffer compressedPacketToSend = compressPacket(toCompress, cbuflen, splitSize);
                    this.mysqlOutput.write(compressedPacketToSend.getByteBuffer(), 0, compressedPacketToSend.getPosition());
                    this.mysqlOutput.flush();
                    cbuflen += splitSize;
                    len -= this.maxThreeBytes - 3;
                }
            }
        } catch (IOException e) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, e, getExceptionInterceptor());
        }
    }

    private void reclaimLargeSharedSendPacket() {
        if (this.sharedSendPacket != null && this.sharedSendPacket.getCapacity() > 1048576) {
            this.sharedSendPacket = new Buffer(1024);
        }
    }

    boolean hadWarnings() {
        return this.hadWarnings;
    }

    void scanForAndThrowDataTruncation() throws SQLException {
        if (this.streamingData == null && versionMeetsMinimum(4, 1, 0) && this.connection.getJdbcCompliantTruncation() && this.warningCount > 0) {
            SQLError.convertShowWarningsToSQLWarnings(this.connection, this.warningCount, true);
        }
    }

    private void secureAuth(Buffer packet, int packLength, String user, String password, String database, boolean writeClientParams) throws SQLException {
        Buffer packet2;
        NoSuchAlgorithmException e;
        NoSuchAlgorithmException nse;
        StringBuilder stringBuilder;
        String str;
        MysqlIO mysqlIO = this;
        int i = packLength;
        if (packet == null) {
            packet2 = new Buffer(i);
        } else {
            packet2 = packet;
        }
        if (writeClientParams) {
            if (!mysqlIO.use41Extensions) {
                packet2.writeInt((int) mysqlIO.clientParam);
                packet2.writeLongInt(mysqlIO.maxThreeBytes);
            } else if (versionMeetsMinimum(4, 1, 1)) {
                packet2.writeLong(mysqlIO.clientParam);
                packet2.writeLong((long) mysqlIO.maxThreeBytes);
                packet2.writeByte((byte) 8);
                packet2.writeBytesNoNull(new byte[23]);
            } else {
                packet2.writeLong(mysqlIO.clientParam);
                packet2.writeLong((long) mysqlIO.maxThreeBytes);
            }
        }
        packet2.writeString(user, CODE_PAGE_1252, mysqlIO.connection);
        if (password.length() != 0) {
            packet2.writeString(FALSE_SCRAMBLE, CODE_PAGE_1252, mysqlIO.connection);
        } else {
            packet2.writeString("", CODE_PAGE_1252, mysqlIO.connection);
        }
        if (mysqlIO.useConnectWithDb) {
            packet2.writeString(database, CODE_PAGE_1252, mysqlIO.connection);
        } else {
            String str2 = database;
        }
        send(packet2, packet2.getPosition());
        if (password.length() > 0) {
            Buffer b = readPacket();
            b.setPosition(0);
            byte[] replyAsBytes = b.getByteBuffer();
            if (replyAsBytes.length == 24 && replyAsBytes[0] != (byte) 0) {
                byte[] passwordHash;
                byte[] mysqlScrambleBuff;
                if (replyAsBytes[0] != (byte) 42) {
                    try {
                        byte[] buff = Security.passwordHashStage1(password);
                        byte[] passwordHash2 = new byte[buff.length];
                        System.arraycopy(buff, 0, passwordHash2, 0, buff.length);
                        passwordHash = Security.passwordHashStage2(passwordHash2, replyAsBytes);
                        passwordHash2 = new byte[(replyAsBytes.length - 4)];
                        System.arraycopy(replyAsBytes, 4, passwordHash2, 0, replyAsBytes.length - 4);
                        mysqlScrambleBuff = new byte[20];
                        Security.xorString(passwordHash2, mysqlScrambleBuff, passwordHash, 20);
                        Security.xorString(mysqlScrambleBuff, buff, buff, 20);
                        Buffer packet22 = new Buffer(25);
                        packet22.writeBytesNoNull(buff);
                        mysqlIO.packetSequence = (byte) (mysqlIO.packetSequence + 1);
                        send(packet22, 24);
                    } catch (NoSuchAlgorithmException e2) {
                        NoSuchAlgorithmException nse2 = e2;
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Messages.getString("MysqlIO.91"));
                        stringBuilder2.append(Messages.getString("MysqlIO.92"));
                        throw SQLError.createSQLException(stringBuilder2.toString(), SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                    }
                }
                try {
                    mysqlScrambleBuff = Security.createKeyFromOldPassword(password);
                    passwordHash = new byte[(replyAsBytes.length - 4)];
                    System.arraycopy(replyAsBytes, 4, passwordHash, 0, replyAsBytes.length - 4);
                    byte[] mysqlScrambleBuff2 = new byte[20];
                    Security.xorString(passwordHash, mysqlScrambleBuff2, mysqlScrambleBuff, 20);
                    try {
                        String scrambledPassword = Util.scramble(StringUtils.toString(mysqlScrambleBuff2), password);
                        Buffer packet23 = new Buffer(i);
                        packet23.writeString(scrambledPassword, CODE_PAGE_1252, mysqlIO.connection);
                        mysqlIO.packetSequence = (byte) (mysqlIO.packetSequence + 1);
                        send(packet23, 24);
                        return;
                    } catch (NoSuchAlgorithmException e3) {
                        e2 = e3;
                        nse = e2;
                        stringBuilder = new StringBuilder();
                        stringBuilder.append(Messages.getString("MysqlIO.91"));
                        stringBuilder.append(Messages.getString("MysqlIO.92"));
                        throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                    }
                } catch (NoSuchAlgorithmException e4) {
                    e2 = e4;
                    str = password;
                    nse = e2;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append(Messages.getString("MysqlIO.91"));
                    stringBuilder.append(Messages.getString("MysqlIO.92"));
                    throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                }
            }
        }
        str = password;
    }

    void secureAuth411(Buffer packet, int packLength, String user, String password, String database, boolean writeClientParams) throws SQLException {
        StringBuilder stringBuilder;
        String enc = getEncodingForHandshake();
        if (packet == null) {
            packet = new Buffer(packLength);
        }
        if (writeClientParams) {
            if (!this.use41Extensions) {
                packet.writeInt((int) this.clientParam);
                packet.writeLongInt(this.maxThreeBytes);
            } else if (versionMeetsMinimum(4, 1, 1)) {
                packet.writeLong(this.clientParam);
                packet.writeLong((long) this.maxThreeBytes);
                appendCharsetByteForHandshake(packet, enc);
                packet.writeBytesNoNull(new byte[23]);
            } else {
                packet.writeLong(this.clientParam);
                packet.writeLong((long) this.maxThreeBytes);
            }
        }
        if (user != null) {
            packet.writeString(user, enc, this.connection);
        }
        if (password.length() != 0) {
            packet.writeByte((byte) 20);
            try {
                packet.writeBytesNoNull(Security.scramble411(password, this.seed, this.connection.getPasswordCharacterEncoding()));
            } catch (NoSuchAlgorithmException e) {
                stringBuilder = new StringBuilder();
                stringBuilder.append(Messages.getString("MysqlIO.91"));
                stringBuilder.append(Messages.getString("MysqlIO.92"));
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
            } catch (UnsupportedEncodingException e2) {
                stringBuilder = new StringBuilder();
                stringBuilder.append(Messages.getString("MysqlIO.91"));
                stringBuilder.append(Messages.getString("MysqlIO.92"));
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
            }
        }
        packet.writeByte((byte) 0);
        if (this.useConnectWithDb) {
            packet.writeString(database, enc, this.connection);
        } else {
            packet.writeByte((byte) 0);
        }
        if ((this.serverCapabilities & 1048576) != 0) {
            sendConnectionAttributes(packet, enc, this.connection);
        }
        send(packet, packet.getPosition());
        byte savePacketSequence = this.packetSequence;
        this.packetSequence = (byte) (savePacketSequence + 1);
        if (checkErrorPacket().isAuthMethodSwitchRequestPacket()) {
            this.packetSequence = (byte) (savePacketSequence + 1);
            packet.clear();
            packet.writeString(Util.newCrypt(password, this.seed.substring(0, 8), this.connection.getPasswordCharacterEncoding()));
            send(packet, packet.getPosition());
            checkErrorPacket();
        }
    }

    private final ResultSetRow unpackBinaryResultSetRow(Field[] fields, Buffer binaryData, int resultSetConcurrency) throws SQLException {
        int numFields = fields.length;
        byte[][] unpackedRowData = new byte[numFields][];
        int nullCount = (numFields + 9) / 8;
        int nullMaskPos = binaryData.getPosition();
        binaryData.setPosition(nullMaskPos + nullCount);
        int bit = 4;
        for (int i = 0; i < numFields; i++) {
            if ((binaryData.readByte(nullMaskPos) & bit) != 0) {
                unpackedRowData[i] = null;
            } else if (resultSetConcurrency != 1008) {
                extractNativeEncodedColumn(binaryData, fields, i, unpackedRowData);
            } else {
                unpackNativeEncodedColumn(binaryData, fields, i, unpackedRowData);
            }
            int i2 = bit << 1;
            bit = i2;
            if ((i2 & 255) == 0) {
                bit = 1;
                nullMaskPos++;
            }
        }
        return new ByteArrayRow(unpackedRowData, getExceptionInterceptor());
    }

    private final void extractNativeEncodedColumn(Buffer binaryData, Field[] fields, int columnIndex, byte[][] unpackedRowData) throws SQLException {
        Field curField = fields[columnIndex];
        int mysqlType = curField.getMysqlType();
        switch (mysqlType) {
            case 0:
                break;
            case 1:
                unpackedRowData[columnIndex] = new byte[]{binaryData.readByte()};
                return;
            case 2:
            case 13:
                unpackedRowData[columnIndex] = binaryData.getBytes(2);
                return;
            case 3:
            case 9:
                unpackedRowData[columnIndex] = binaryData.getBytes(4);
                return;
            case 4:
                unpackedRowData[columnIndex] = binaryData.getBytes(4);
                return;
            case 5:
                unpackedRowData[columnIndex] = binaryData.getBytes(8);
                return;
            case 6:
                return;
            case 7:
            case 12:
                unpackedRowData[columnIndex] = binaryData.getBytes((int) binaryData.readFieldLength());
                return;
            case 8:
                unpackedRowData[columnIndex] = binaryData.getBytes(8);
                return;
            case 10:
                unpackedRowData[columnIndex] = binaryData.getBytes((int) binaryData.readFieldLength());
                return;
            case 11:
                unpackedRowData[columnIndex] = binaryData.getBytes((int) binaryData.readFieldLength());
                return;
            default:
                switch (mysqlType) {
                    case 15:
                    case 16:
                        break;
                    default:
                        switch (mysqlType) {
                            case 245:
                            case 246:
                                break;
                            default:
                                switch (mysqlType) {
                                    case 249:
                                    case Callback.DEFAULT_SWIPE_ANIMATION_DURATION /*250*/:
                                    case 251:
                                    case MysqlDefs.FIELD_TYPE_BLOB /*252*/:
                                    case 253:
                                    case 254:
                                    case 255:
                                        break;
                                    default:
                                        StringBuilder stringBuilder = new StringBuilder();
                                        stringBuilder.append(Messages.getString("MysqlIO.97"));
                                        stringBuilder.append(curField.getMysqlType());
                                        stringBuilder.append(Messages.getString("MysqlIO.98"));
                                        stringBuilder.append(columnIndex);
                                        stringBuilder.append(Messages.getString("MysqlIO.99"));
                                        stringBuilder.append(fields.length);
                                        stringBuilder.append(Messages.getString("MysqlIO.100"));
                                        throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
                                }
                        }
                }
        }
        unpackedRowData[columnIndex] = binaryData.readLenByteArray(0);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final void unpackNativeEncodedColumn(com.mysql.jdbc.Buffer r23, com.mysql.jdbc.Field[] r24, int r25, byte[][] r26) throws java.sql.SQLException {
        /*
        r22 = this;
        r0 = r22;
        r1 = r24;
        r2 = r25;
        r3 = r1[r2];
        r4 = r3.getMysqlType();
        r9 = 3;
        r10 = 2;
        r13 = 58;
        r14 = 1;
        r11 = 8;
        r5 = 0;
        r6 = 10;
        switch(r4) {
            case 0: goto L_0x0386;
            case 1: goto L_0x0363;
            case 2: goto L_0x033e;
            case 3: goto L_0x0315;
            case 4: goto L_0x02fe;
            case 5: goto L_0x02eb;
            case 6: goto L_0x02ea;
            case 7: goto L_0x01a8;
            case 8: goto L_0x0182;
            case 9: goto L_0x0315;
            case 10: goto L_0x00cd;
            case 11: goto L_0x0068;
            case 12: goto L_0x01a8;
            case 13: goto L_0x033e;
            default: goto L_0x0019;
        };
    L_0x0019:
        switch(r4) {
            case 15: goto L_0x0386;
            case 16: goto L_0x0386;
            default: goto L_0x001c;
        };
    L_0x001c:
        switch(r4) {
            case 245: goto L_0x0386;
            case 246: goto L_0x0386;
            default: goto L_0x001f;
        };
    L_0x001f:
        switch(r4) {
            case 249: goto L_0x0386;
            case 250: goto L_0x0386;
            case 251: goto L_0x0386;
            case 252: goto L_0x0386;
            case 253: goto L_0x0386;
            case 254: goto L_0x0386;
            default: goto L_0x0022;
        };
    L_0x0022:
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r5 = "MysqlIO.97";
        r5 = com.mysql.jdbc.Messages.getString(r5);
        r4.append(r5);
        r5 = r3.getMysqlType();
        r4.append(r5);
        r5 = "MysqlIO.98";
        r5 = com.mysql.jdbc.Messages.getString(r5);
        r4.append(r5);
        r4.append(r2);
        r5 = "MysqlIO.99";
        r5 = com.mysql.jdbc.Messages.getString(r5);
        r4.append(r5);
        r5 = r1.length;
        r4.append(r5);
        r5 = "MysqlIO.100";
        r5 = com.mysql.jdbc.Messages.getString(r5);
        r4.append(r5);
        r4 = r4.toString();
        r5 = "S1000";
        r6 = r22.getExceptionInterceptor();
        r4 = com.mysql.jdbc.SQLError.createSQLException(r4, r5, r6);
        throw r4;
    L_0x0068:
        r7 = r23.readFieldLength();
        r4 = (int) r7;
        r7 = 0;
        r8 = 0;
        r15 = 0;
        if (r4 == 0) goto L_0x0089;
    L_0x0072:
        r23.readByte();
        r23.readLong();
        r7 = r23.readByte();
        r8 = r23.readByte();
        r15 = r23.readByte();
        if (r4 <= r11) goto L_0x0089;
    L_0x0086:
        r23.readLong();
    L_0x0089:
        r11 = new byte[r11];
        r12 = r7 / 10;
        r12 = java.lang.Character.forDigit(r12, r6);
        r12 = (byte) r12;
        r11[r5] = r12;
        r5 = r7 % 10;
        r5 = java.lang.Character.forDigit(r5, r6);
        r5 = (byte) r5;
        r11[r14] = r5;
        r11[r10] = r13;
        r5 = r8 / 10;
        r5 = java.lang.Character.forDigit(r5, r6);
        r5 = (byte) r5;
        r11[r9] = r5;
        r5 = r8 % 10;
        r5 = java.lang.Character.forDigit(r5, r6);
        r5 = (byte) r5;
        r9 = 4;
        r11[r9] = r5;
        r5 = 5;
        r11[r5] = r13;
        r5 = r15 / 10;
        r5 = java.lang.Character.forDigit(r5, r6);
        r5 = (byte) r5;
        r9 = 6;
        r11[r9] = r5;
        r5 = r15 % 10;
        r5 = java.lang.Character.forDigit(r5, r6);
        r5 = (byte) r5;
        r6 = 7;
        r11[r6] = r5;
        r26[r2] = r11;
        goto L_0x0311;
    L_0x00cd:
        r7 = r23.readFieldLength();
        r4 = (int) r7;
        r7 = 0;
        r8 = 0;
        r12 = 0;
        r13 = 0;
        r17 = 0;
        r18 = 0;
        if (r4 == 0) goto L_0x00e8;
    L_0x00dc:
        r7 = r23.readInt();
        r8 = r23.readByte();
        r12 = r23.readByte();
    L_0x00e8:
        if (r7 != 0) goto L_0x011f;
    L_0x00ea:
        if (r8 != 0) goto L_0x011f;
    L_0x00ec:
        if (r12 != 0) goto L_0x011f;
    L_0x00ee:
        r11 = "convertToNull";
        r9 = r0.connection;
        r9 = r9.getZeroDateTimeBehavior();
        r9 = r11.equals(r9);
        if (r9 == 0) goto L_0x0101;
    L_0x00fc:
        r5 = 0;
        r26[r2] = r5;
        goto L_0x0311;
    L_0x0101:
        r9 = "exception";
        r11 = r0.connection;
        r11 = r11.getZeroDateTimeBehavior();
        r9 = r9.equals(r11);
        if (r9 == 0) goto L_0x011c;
    L_0x010f:
        r5 = "Value '0000-00-00' can not be represented as java.sql.Date";
        r6 = "S1009";
        r9 = r22.getExceptionInterceptor();
        r5 = com.mysql.jdbc.SQLError.createSQLException(r5, r6, r9);
        throw r5;
    L_0x011c:
        r7 = 1;
        r8 = 1;
        r12 = 1;
    L_0x011f:
        r9 = new byte[r6];
        r11 = r7 / 1000;
        r11 = java.lang.Character.forDigit(r11, r6);
        r11 = (byte) r11;
        r9[r5] = r11;
        r5 = r7 % 1000;
        r11 = r5 / 100;
        r11 = java.lang.Character.forDigit(r11, r6);
        r11 = (byte) r11;
        r9[r14] = r11;
        r11 = r5 % 100;
        r14 = r11 / 10;
        r14 = java.lang.Character.forDigit(r14, r6);
        r14 = (byte) r14;
        r9[r10] = r14;
        r10 = r11 % 10;
        r10 = java.lang.Character.forDigit(r10, r6);
        r10 = (byte) r10;
        r14 = 3;
        r9[r14] = r10;
        r10 = 4;
        r14 = 45;
        r9[r10] = r14;
        r10 = r8 / 10;
        r10 = java.lang.Character.forDigit(r10, r6);
        r10 = (byte) r10;
        r14 = 5;
        r9[r14] = r10;
        r10 = r8 % 10;
        r10 = java.lang.Character.forDigit(r10, r6);
        r10 = (byte) r10;
        r14 = 6;
        r9[r14] = r10;
        r10 = 45;
        r14 = 7;
        r9[r14] = r10;
        r10 = r12 / 10;
        r10 = java.lang.Character.forDigit(r10, r6);
        r10 = (byte) r10;
        r14 = 8;
        r9[r14] = r10;
        r10 = 9;
        r14 = r12 % 10;
        r6 = java.lang.Character.forDigit(r14, r6);
        r6 = (byte) r6;
        r9[r10] = r6;
        r26[r2] = r9;
        goto L_0x0311;
    L_0x0182:
        r4 = r23.readLongLong();
        r6 = r3.isUnsigned();
        if (r6 != 0) goto L_0x0198;
    L_0x018c:
        r6 = java.lang.String.valueOf(r4);
        r6 = com.mysql.jdbc.StringUtils.getBytes(r6);
        r26[r2] = r6;
        goto L_0x0311;
    L_0x0198:
        r6 = com.mysql.jdbc.ResultSetImpl.convertLongToUlong(r4);
        r7 = r6.toString();
        r7 = com.mysql.jdbc.StringUtils.getBytes(r7);
        r26[r2] = r7;
        goto L_0x0311;
    L_0x01a8:
        r7 = r23.readFieldLength();
        r4 = (int) r7;
        r7 = 0;
        r8 = 0;
        r9 = 0;
        r11 = 0;
        r12 = 0;
        r17 = 0;
        r13 = 0;
        if (r4 == 0) goto L_0x01d2;
    L_0x01b7:
        r7 = r23.readInt();
        r8 = r23.readByte();
        r9 = r23.readByte();
        r10 = 4;
        if (r4 <= r10) goto L_0x01d2;
    L_0x01c6:
        r11 = r23.readByte();
        r12 = r23.readByte();
        r17 = r23.readByte();
    L_0x01d2:
        if (r7 != 0) goto L_0x0209;
    L_0x01d4:
        if (r8 != 0) goto L_0x0209;
    L_0x01d6:
        if (r9 != 0) goto L_0x0209;
    L_0x01d8:
        r10 = "convertToNull";
        r5 = r0.connection;
        r5 = r5.getZeroDateTimeBehavior();
        r5 = r10.equals(r5);
        if (r5 == 0) goto L_0x01eb;
    L_0x01e6:
        r5 = 0;
        r26[r2] = r5;
        goto L_0x0311;
    L_0x01eb:
        r5 = "exception";
        r10 = r0.connection;
        r10 = r10.getZeroDateTimeBehavior();
        r5 = r5.equals(r10);
        if (r5 == 0) goto L_0x0206;
    L_0x01f9:
        r5 = "Value '0000-00-00' can not be represented as java.sql.Timestamp";
        r6 = "S1009";
        r10 = r22.getExceptionInterceptor();
        r5 = com.mysql.jdbc.SQLError.createSQLException(r5, r6, r10);
        throw r5;
    L_0x0206:
        r7 = 1;
        r8 = 1;
        r9 = 1;
    L_0x0209:
        r5 = 19;
        r10 = java.lang.Integer.toString(r13);
        r10 = com.mysql.jdbc.StringUtils.getBytes(r10);
        r6 = r10.length;
        r6 = r6 + r14;
        r5 = r5 + r6;
        r6 = new byte[r5];
        r14 = r7 / 1000;
        r0 = 10;
        r14 = java.lang.Character.forDigit(r14, r0);
        r14 = (byte) r14;
        r16 = 0;
        r6[r16] = r14;
        r14 = r7 % 1000;
        r1 = r14 / 100;
        r1 = java.lang.Character.forDigit(r1, r0);
        r1 = (byte) r1;
        r16 = 1;
        r6[r16] = r1;
        r1 = r14 % 100;
        r19 = r4;
        r4 = r1 / 10;
        r4 = java.lang.Character.forDigit(r4, r0);
        r4 = (byte) r4;
        r16 = 2;
        r6[r16] = r4;
        r4 = r1 % 10;
        r4 = java.lang.Character.forDigit(r4, r0);
        r4 = (byte) r4;
        r16 = 3;
        r6[r16] = r4;
        r4 = 4;
        r15 = 45;
        r6[r4] = r15;
        r4 = r8 / 10;
        r4 = java.lang.Character.forDigit(r4, r0);
        r4 = (byte) r4;
        r16 = 5;
        r6[r16] = r4;
        r4 = r8 % 10;
        r4 = java.lang.Character.forDigit(r4, r0);
        r4 = (byte) r4;
        r16 = 6;
        r6[r16] = r4;
        r4 = 45;
        r15 = 7;
        r6[r15] = r4;
        r4 = r9 / 10;
        r4 = java.lang.Character.forDigit(r4, r0);
        r4 = (byte) r4;
        r15 = 8;
        r6[r15] = r4;
        r4 = r9 % 10;
        r4 = java.lang.Character.forDigit(r4, r0);
        r4 = (byte) r4;
        r15 = 9;
        r6[r15] = r4;
        r4 = 32;
        r6[r0] = r4;
        r4 = r11 / 10;
        r4 = java.lang.Character.forDigit(r4, r0);
        r4 = (byte) r4;
        r15 = 11;
        r6[r15] = r4;
        r4 = r11 % 10;
        r4 = java.lang.Character.forDigit(r4, r0);
        r4 = (byte) r4;
        r15 = 12;
        r6[r15] = r4;
        r4 = 13;
        r15 = 58;
        r6[r4] = r15;
        r4 = r12 / 10;
        r4 = java.lang.Character.forDigit(r4, r0);
        r4 = (byte) r4;
        r15 = 14;
        r6[r15] = r4;
        r4 = r12 % 10;
        r4 = java.lang.Character.forDigit(r4, r0);
        r4 = (byte) r4;
        r15 = 15;
        r6[r15] = r4;
        r4 = 16;
        r15 = 58;
        r6[r4] = r15;
        r4 = r17 / 10;
        r4 = java.lang.Character.forDigit(r4, r0);
        r4 = (byte) r4;
        r15 = 17;
        r6[r15] = r4;
        r4 = r17 % 10;
        r0 = java.lang.Character.forDigit(r4, r0);
        r0 = (byte) r0;
        r4 = 18;
        r6[r4] = r0;
        r0 = 19;
        r4 = 46;
        r6[r0] = r4;
        r0 = 20;
        r4 = 20;
        r20 = r0;
        r0 = r10.length;
        r21 = r1;
        r1 = 0;
        java.lang.System.arraycopy(r10, r1, r6, r4, r0);
        r26[r2] = r6;
        goto L_0x0311;
    L_0x02ea:
        goto L_0x0311;
    L_0x02eb:
        r0 = r23.readLongLong();
        r0 = java.lang.Double.longBitsToDouble(r0);
        r4 = java.lang.String.valueOf(r0);
        r4 = com.mysql.jdbc.StringUtils.getBytes(r4);
        r26[r2] = r4;
        goto L_0x0311;
    L_0x02fe:
        r0 = r23.readIntAsLong();
        r0 = java.lang.Float.intBitsToFloat(r0);
        r1 = java.lang.String.valueOf(r0);
        r1 = com.mysql.jdbc.StringUtils.getBytes(r1);
        r26[r2] = r1;
    L_0x0311:
        r0 = r23;
        goto L_0x0390;
    L_0x0315:
        r0 = r23.readLong();
        r0 = (int) r0;
        r1 = r3.isUnsigned();
        if (r1 != 0) goto L_0x032b;
    L_0x0320:
        r1 = java.lang.String.valueOf(r0);
        r1 = com.mysql.jdbc.StringUtils.getBytes(r1);
        r26[r2] = r1;
        goto L_0x0311;
    L_0x032b:
        r4 = (long) r0;
        r6 = 4294967295; // 0xffffffff float:NaN double:2.1219957905E-314;
        r8 = r4 & r6;
        r1 = java.lang.String.valueOf(r8);
        r1 = com.mysql.jdbc.StringUtils.getBytes(r1);
        r26[r2] = r1;
        goto L_0x0311;
    L_0x033e:
        r0 = r23.readInt();
        r0 = (short) r0;
        r1 = r3.isUnsigned();
        if (r1 != 0) goto L_0x0354;
    L_0x0349:
        r1 = java.lang.String.valueOf(r0);
        r1 = com.mysql.jdbc.StringUtils.getBytes(r1);
        r26[r2] = r1;
        goto L_0x0311;
    L_0x0354:
        r1 = 65535; // 0xffff float:9.1834E-41 double:3.23786E-319;
        r1 = r1 & r0;
        r4 = java.lang.String.valueOf(r1);
        r4 = com.mysql.jdbc.StringUtils.getBytes(r4);
        r26[r2] = r4;
        goto L_0x0311;
    L_0x0363:
        r0 = r23.readByte();
        r1 = r3.isUnsigned();
        if (r1 != 0) goto L_0x0378;
    L_0x036d:
        r1 = java.lang.String.valueOf(r0);
        r1 = com.mysql.jdbc.StringUtils.getBytes(r1);
        r26[r2] = r1;
        goto L_0x0311;
    L_0x0378:
        r1 = r0 & 255;
        r1 = (short) r1;
        r4 = java.lang.String.valueOf(r1);
        r4 = com.mysql.jdbc.StringUtils.getBytes(r4);
        r26[r2] = r4;
        goto L_0x0311;
    L_0x0386:
        r0 = r23;
        r1 = 0;
        r1 = r0.readLenByteArray(r1);
        r26[r2] = r1;
    L_0x0390:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.MysqlIO.unpackNativeEncodedColumn(com.mysql.jdbc.Buffer, com.mysql.jdbc.Field[], int, byte[][]):void");
    }

    private void negotiateSSLConnection(String user, String password, String database, int packLength) throws SQLException {
        if (ExportControlled.enabled()) {
            if ((this.serverCapabilities & 32768) != 0) {
                this.clientParam |= PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID;
            }
            this.clientParam |= PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH;
            Buffer packet = new Buffer(packLength);
            if (this.use41Extensions) {
                packet.writeLong(this.clientParam);
                packet.writeLong((long) this.maxThreeBytes);
                appendCharsetByteForHandshake(packet, getEncodingForHandshake());
                packet.writeBytesNoNull(new byte[23]);
            } else {
                packet.writeInt((int) this.clientParam);
            }
            send(packet, packet.getPosition());
            ExportControlled.transformSocketToSSLSocket(this);
            return;
        }
        throw new ConnectionFeatureNotAvailableException(this.connection, this.lastPacketSentTimeMs, null);
    }

    public boolean isSSLEstablished() {
        return ExportControlled.enabled() && ExportControlled.isSSLEstablished(this);
    }

    protected int getServerStatus() {
        return this.serverStatus;
    }

    protected List<ResultSetRow> fetchRowsViaCursor(List<ResultSetRow> fetchedRows, long statementId, Field[] columnTypes, int fetchSize, boolean useBufferRowExplicit) throws SQLException {
        List<ResultSetRow> fetchedRows2;
        MysqlIO mysqlIO = this;
        int i = fetchSize;
        if (fetchedRows == null) {
            fetchedRows2 = new ArrayList(i);
        } else {
            fetchedRows.clear();
            fetchedRows2 = fetchedRows;
        }
        mysqlIO.sharedSendPacket.clear();
        mysqlIO.sharedSendPacket.writeByte((byte) 28);
        mysqlIO.sharedSendPacket.writeLong(statementId);
        mysqlIO.sharedSendPacket.writeLong((long) i);
        sendCommand(28, null, mysqlIO.sharedSendPacket, true, null, 0);
        ResultSetRow row = null;
        while (true) {
            Field[] fieldArr = columnTypes;
            row = nextRow(fieldArr, fieldArr.length, true, 1007, false, useBufferRowExplicit, false, null);
            ResultSetRow row2 = row;
            if (row == null) {
                return fetchedRows2;
            }
            fetchedRows2.add(row2);
            row = row2;
        }
    }

    protected long getThreadId() {
        return this.threadId;
    }

    protected boolean useNanosForElapsedTime() {
        return this.useNanosForElapsedTime;
    }

    protected long getSlowQueryThreshold() {
        return this.slowQueryThreshold;
    }

    protected String getQueryTimingUnits() {
        return this.queryTimingUnits;
    }

    protected int getCommandCount() {
        return this.commandCount;
    }

    private void checkTransactionState(int oldStatus) throws SQLException {
        boolean previouslyInTrans = (oldStatus & 1) != 0;
        boolean currentlyInTrans = inTransactionOnServer();
        if (previouslyInTrans && !currentlyInTrans) {
            this.connection.transactionCompleted();
        } else if (!previouslyInTrans && currentlyInTrans) {
            this.connection.transactionBegun();
        }
    }

    private void preserveOldTransactionState() {
        this.serverStatus |= this.oldServerStatus & 1;
    }

    protected void setStatementInterceptors(List<StatementInterceptorV2> statementInterceptors) {
        this.statementInterceptors = statementInterceptors.isEmpty() ? null : statementInterceptors;
    }

    protected ExceptionInterceptor getExceptionInterceptor() {
        return this.exceptionInterceptor;
    }

    protected void setSocketTimeout(int milliseconds) throws SQLException {
        try {
            if (this.mysqlConnection != null) {
                this.mysqlConnection.setSoTimeout(milliseconds);
            }
        } catch (SocketException e) {
            SQLException sqlEx = SQLError.createSQLException("Invalid socket timeout value or state", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            sqlEx.initCause(e);
            throw sqlEx;
        }
    }

    protected void releaseResources() {
        if (this.deflater != null) {
            this.deflater.end();
            this.deflater = null;
        }
    }

    String getEncodingForHandshake() {
        String enc = this.connection.getEncoding();
        if (enc == null) {
            return "UTF-8";
        }
        return enc;
    }

    private void appendCharsetByteForHandshake(Buffer packet, String enc) throws SQLException {
        int charsetIndex = 0;
        if (enc != null) {
            charsetIndex = CharsetMapping.getCollationIndexForJavaEncoding(enc, this.connection);
        }
        if (charsetIndex == 0) {
            charsetIndex = 33;
        }
        if (charsetIndex > 255) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Invalid character set index for encoding: ");
            stringBuilder.append(enc);
            throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
        packet.writeByte((byte) charsetIndex);
    }

    public boolean isEOFDeprecated() {
        return (this.clientParam & 16777216) != 0;
    }
}
