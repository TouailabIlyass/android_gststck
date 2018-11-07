package com.mysql.jdbc;

import java.sql.SQLException;

public interface ConnectionProperties {
    String exposeAsXml() throws SQLException;

    boolean getAllowLoadLocalInfile();

    boolean getAllowMasterDownConnections();

    boolean getAllowMultiQueries();

    boolean getAllowNanAndInf();

    boolean getAllowPublicKeyRetrieval();

    boolean getAllowSlaveDownConnections();

    boolean getAllowUrlInLocalInfile();

    boolean getAlwaysSendSetIsolation();

    String getAuthenticationPlugins();

    boolean getAutoClosePStmtStreams();

    boolean getAutoDeserialize();

    boolean getAutoGenerateTestcaseScript();

    boolean getAutoReconnectForPools();

    boolean getAutoSlowLog();

    int getBlobSendChunkSize();

    boolean getBlobsAreStrings();

    boolean getCacheCallableStatements();

    boolean getCacheCallableStmts();

    boolean getCacheDefaultTimezone();

    boolean getCachePrepStmts();

    boolean getCachePreparedStatements();

    boolean getCacheResultSetMetadata();

    boolean getCacheServerConfiguration();

    int getCallableStatementCacheSize();

    int getCallableStmtCacheSize();

    boolean getCapitalizeTypeNames();

    String getCharacterSetResults();

    String getClientCertificateKeyStorePassword();

    String getClientCertificateKeyStoreType();

    String getClientCertificateKeyStoreUrl();

    String getClientInfoProvider();

    String getClobCharacterEncoding();

    boolean getClobberStreamingResults();

    boolean getCompensateOnDuplicateKeyUpdateCounts();

    int getConnectTimeout();

    String getConnectionAttributes() throws SQLException;

    String getConnectionCollation();

    String getConnectionLifecycleInterceptors();

    boolean getContinueBatchOnError();

    boolean getCreateDatabaseIfNotExist();

    String getDefaultAuthenticationPlugin();

    int getDefaultFetchSize();

    boolean getDetectCustomCollations();

    String getDisabledAuthenticationPlugins();

    boolean getDisconnectOnExpiredPasswords();

    boolean getDontCheckOnDuplicateKeyUpdateInSQL();

    boolean getDontTrackOpenResources();

    boolean getDumpMetadataOnColumnNotFound();

    boolean getDumpQueriesOnException();

    boolean getDynamicCalendars();

    boolean getElideSetAutoCommits();

    boolean getEmptyStringsConvertToZero();

    boolean getEmulateLocators();

    boolean getEmulateUnsupportedPstmts();

    boolean getEnableEscapeProcessing();

    boolean getEnablePacketDebug();

    boolean getEnableQueryTimeouts();

    String getEnabledSSLCipherSuites();

    String getEnabledTLSProtocols();

    String getEncoding();

    ExceptionInterceptor getExceptionInterceptor();

    String getExceptionInterceptors();

    boolean getExplainSlowQueries();

    boolean getFailOverReadOnly();

    boolean getFunctionsNeverReturnBlobs();

    boolean getGatherPerfMetrics();

    boolean getGatherPerformanceMetrics();

    boolean getGenerateSimpleParameterMetadata();

    boolean getGetProceduresReturnsFunctions();

    boolean getHoldResultsOpenOverStatementClose();

    boolean getIgnoreNonTxTables();

    boolean getIncludeInnodbStatusInDeadlockExceptions();

    boolean getIncludeThreadDumpInDeadlockExceptions();

    boolean getIncludeThreadNamesAsStatementComment();

    int getInitialTimeout();

    boolean getInteractiveClient();

    boolean getIsInteractiveClient();

    boolean getJdbcCompliantTruncation();

    boolean getJdbcCompliantTruncationForReads();

    String getLargeRowSizeThreshold();

    String getLoadBalanceAutoCommitStatementRegex();

    int getLoadBalanceAutoCommitStatementThreshold();

    int getLoadBalanceBlacklistTimeout();

    String getLoadBalanceConnectionGroup();

    boolean getLoadBalanceEnableJMX();

    String getLoadBalanceExceptionChecker();

    int getLoadBalanceHostRemovalGracePeriod();

    int getLoadBalancePingTimeout();

    String getLoadBalanceSQLExceptionSubclassFailover();

    String getLoadBalanceSQLStateFailover();

    String getLoadBalanceStrategy();

    boolean getLoadBalanceValidateConnectionOnSwapServer();

    String getLocalSocketAddress();

    int getLocatorFetchBufferSize();

    boolean getLogSlowQueries();

    boolean getLogXaCommands();

    String getLogger();

    String getLoggerClassName();

    boolean getMaintainTimeStats();

    int getMaxAllowedPacket();

    int getMaxQuerySizeToLog();

    int getMaxReconnects();

    int getMaxRows();

    int getMetadataCacheSize();

    int getNetTimeoutForStreamingResults();

    boolean getNoAccessToProcedureBodies();

    boolean getNoDatetimeStringSync();

    boolean getNoTimezoneConversionForDateType();

    boolean getNoTimezoneConversionForTimeType();

    boolean getNullCatalogMeansCurrent();

    boolean getNullNamePatternMatchesAll();

    boolean getOverrideSupportsIntegrityEnhancementFacility();

    int getPacketDebugBufferSize();

    boolean getPadCharsWithSpace();

    boolean getParanoid();

    String getParseInfoCacheFactory();

    String getPasswordCharacterEncoding();

    boolean getPedantic();

    boolean getPinGlobalTxToPhysicalConnection();

    boolean getPopulateInsertRowWithDefaultValues();

    int getPrepStmtCacheSize();

    int getPrepStmtCacheSqlLimit();

    int getPreparedStatementCacheSize();

    int getPreparedStatementCacheSqlLimit();

    boolean getProcessEscapeCodesForPrepStmts();

    boolean getProfileSQL();

    boolean getProfileSql();

    String getProfilerEventHandler();

    String getPropertiesTransform();

    int getQueriesBeforeRetryMaster();

    boolean getQueryTimeoutKillsConnection();

    boolean getReadFromMasterWhenNoSlaves();

    boolean getReadOnlyPropagatesToServer();

    boolean getReconnectAtTxEnd();

    boolean getRelaxAutoCommit();

    boolean getReplicationEnableJMX();

    int getReportMetricsIntervalMillis();

    boolean getRequireSSL();

    String getResourceId();

    int getResultSetSizeThreshold();

    boolean getRetainStatementAfterResultSetClose();

    int getRetriesAllDown();

    boolean getRewriteBatchedStatements();

    boolean getRollbackOnPooledClose();

    boolean getRoundRobinLoadBalance();

    boolean getRunningCTS13();

    int getSecondsBeforeRetryMaster();

    int getSelfDestructOnPingMaxOperations();

    int getSelfDestructOnPingSecondsLifetime();

    boolean getSendFractionalSeconds();

    String getServerAffinityOrder();

    String getServerConfigCacheFactory();

    String getServerRSAPublicKeyFile();

    String getServerTimezone();

    String getSessionVariables();

    int getSlowQueryThresholdMillis();

    long getSlowQueryThresholdNanos();

    String getSocketFactory();

    String getSocketFactoryClassName();

    int getSocketTimeout();

    String getSocksProxyHost();

    int getSocksProxyPort();

    String getStatementInterceptors();

    boolean getStrictFloatingPoint();

    boolean getStrictUpdates();

    boolean getTcpKeepAlive();

    boolean getTcpNoDelay();

    int getTcpRcvBuf();

    int getTcpSndBuf();

    int getTcpTrafficClass();

    boolean getTinyInt1isBit();

    boolean getTraceProtocol();

    boolean getTransformedBitIsBoolean();

    boolean getTreatUtilDateAsTimestamp();

    String getTrustCertificateKeyStorePassword();

    String getTrustCertificateKeyStoreType();

    String getTrustCertificateKeyStoreUrl();

    boolean getUltraDevHack();

    boolean getUseAffectedRows();

    boolean getUseBlobToStoreUTF8OutsideBMP();

    boolean getUseColumnNamesInFindColumn();

    boolean getUseCompression();

    String getUseConfigs();

    boolean getUseCursorFetch();

    boolean getUseDirectRowUnpack();

    boolean getUseDynamicCharsetInfo();

    boolean getUseFastDateParsing();

    boolean getUseFastIntParsing();

    boolean getUseGmtMillisForDatetimes();

    boolean getUseHostsInPrivileges();

    boolean getUseInformationSchema();

    boolean getUseJDBCCompliantTimezoneShift();

    boolean getUseJvmCharsetConverters();

    boolean getUseLegacyDatetimeCode();

    boolean getUseLocalSessionState();

    boolean getUseLocalTransactionState();

    boolean getUseNanosForElapsedTime();

    boolean getUseOldAliasMetadataBehavior();

    boolean getUseOldUTF8Behavior();

    boolean getUseOnlyServerErrorMessages();

    boolean getUseReadAheadInput();

    boolean getUseSSL();

    boolean getUseSSPSCompatibleTimezoneShift();

    boolean getUseServerPrepStmts();

    boolean getUseServerPreparedStmts();

    boolean getUseSqlStateCodes();

    boolean getUseStreamLengthsInPrepStmts();

    boolean getUseTimezone();

    boolean getUseUltraDevWorkAround();

    boolean getUseUnbufferedInput();

    boolean getUseUnicode();

    boolean getUseUsageAdvisor();

    String getUtf8OutsideBmpExcludedColumnNamePattern();

    String getUtf8OutsideBmpIncludedColumnNamePattern();

    boolean getVerifyServerCertificate();

    boolean getYearIsDateType();

    String getZeroDateTimeBehavior();

    boolean isUseSSLExplicit();

    void setAllowLoadLocalInfile(boolean z);

    void setAllowMasterDownConnections(boolean z);

    void setAllowMultiQueries(boolean z);

    void setAllowNanAndInf(boolean z);

    void setAllowPublicKeyRetrieval(boolean z) throws SQLException;

    void setAllowSlaveDownConnections(boolean z);

    void setAllowUrlInLocalInfile(boolean z);

    void setAlwaysSendSetIsolation(boolean z);

    void setAuthenticationPlugins(String str);

    void setAutoClosePStmtStreams(boolean z);

    void setAutoDeserialize(boolean z);

    void setAutoGenerateTestcaseScript(boolean z);

    void setAutoReconnect(boolean z);

    void setAutoReconnectForConnectionPools(boolean z);

    void setAutoReconnectForPools(boolean z);

    void setAutoSlowLog(boolean z);

    void setBlobSendChunkSize(String str) throws SQLException;

    void setBlobsAreStrings(boolean z);

    void setCacheCallableStatements(boolean z);

    void setCacheCallableStmts(boolean z);

    void setCacheDefaultTimezone(boolean z);

    void setCachePrepStmts(boolean z);

    void setCachePreparedStatements(boolean z);

    void setCacheResultSetMetadata(boolean z);

    void setCacheServerConfiguration(boolean z);

    void setCallableStatementCacheSize(int i) throws SQLException;

    void setCallableStmtCacheSize(int i) throws SQLException;

    void setCapitalizeDBMDTypes(boolean z);

    void setCapitalizeTypeNames(boolean z);

    void setCharacterEncoding(String str);

    void setCharacterSetResults(String str);

    void setClientCertificateKeyStorePassword(String str);

    void setClientCertificateKeyStoreType(String str);

    void setClientCertificateKeyStoreUrl(String str);

    void setClientInfoProvider(String str);

    void setClobCharacterEncoding(String str);

    void setClobberStreamingResults(boolean z);

    void setCompensateOnDuplicateKeyUpdateCounts(boolean z);

    void setConnectTimeout(int i) throws SQLException;

    void setConnectionCollation(String str);

    void setConnectionLifecycleInterceptors(String str);

    void setContinueBatchOnError(boolean z);

    void setCreateDatabaseIfNotExist(boolean z);

    void setDefaultAuthenticationPlugin(String str);

    void setDefaultFetchSize(int i) throws SQLException;

    void setDetectCustomCollations(boolean z);

    void setDetectServerPreparedStmts(boolean z);

    void setDisabledAuthenticationPlugins(String str);

    void setDisconnectOnExpiredPasswords(boolean z);

    void setDontCheckOnDuplicateKeyUpdateInSQL(boolean z);

    void setDontTrackOpenResources(boolean z);

    void setDumpMetadataOnColumnNotFound(boolean z);

    void setDumpQueriesOnException(boolean z);

    void setDynamicCalendars(boolean z);

    void setElideSetAutoCommits(boolean z);

    void setEmptyStringsConvertToZero(boolean z);

    void setEmulateLocators(boolean z);

    void setEmulateUnsupportedPstmts(boolean z);

    void setEnableEscapeProcessing(boolean z);

    void setEnablePacketDebug(boolean z);

    void setEnableQueryTimeouts(boolean z);

    void setEnabledSSLCipherSuites(String str);

    void setEnabledTLSProtocols(String str);

    void setEncoding(String str);

    void setExceptionInterceptors(String str);

    void setExplainSlowQueries(boolean z);

    void setFailOverReadOnly(boolean z);

    void setFunctionsNeverReturnBlobs(boolean z);

    void setGatherPerfMetrics(boolean z);

    void setGatherPerformanceMetrics(boolean z);

    void setGenerateSimpleParameterMetadata(boolean z);

    void setGetProceduresReturnsFunctions(boolean z);

    void setHoldResultsOpenOverStatementClose(boolean z);

    void setIgnoreNonTxTables(boolean z);

    void setIncludeInnodbStatusInDeadlockExceptions(boolean z);

    void setIncludeThreadDumpInDeadlockExceptions(boolean z);

    void setIncludeThreadNamesAsStatementComment(boolean z);

    void setInitialTimeout(int i) throws SQLException;

    void setInteractiveClient(boolean z);

    void setIsInteractiveClient(boolean z);

    void setJdbcCompliantTruncation(boolean z);

    void setJdbcCompliantTruncationForReads(boolean z);

    void setLargeRowSizeThreshold(String str) throws SQLException;

    void setLoadBalanceAutoCommitStatementRegex(String str);

    void setLoadBalanceAutoCommitStatementThreshold(int i) throws SQLException;

    void setLoadBalanceBlacklistTimeout(int i) throws SQLException;

    void setLoadBalanceConnectionGroup(String str);

    void setLoadBalanceEnableJMX(boolean z);

    void setLoadBalanceExceptionChecker(String str);

    void setLoadBalanceHostRemovalGracePeriod(int i) throws SQLException;

    void setLoadBalancePingTimeout(int i) throws SQLException;

    void setLoadBalanceSQLExceptionSubclassFailover(String str);

    void setLoadBalanceSQLStateFailover(String str);

    void setLoadBalanceStrategy(String str);

    void setLoadBalanceValidateConnectionOnSwapServer(boolean z);

    void setLocalSocketAddress(String str);

    void setLocatorFetchBufferSize(String str) throws SQLException;

    void setLogSlowQueries(boolean z);

    void setLogXaCommands(boolean z);

    void setLogger(String str);

    void setLoggerClassName(String str);

    void setMaintainTimeStats(boolean z);

    void setMaxQuerySizeToLog(int i) throws SQLException;

    void setMaxReconnects(int i) throws SQLException;

    void setMaxRows(int i) throws SQLException;

    void setMetadataCacheSize(int i) throws SQLException;

    void setNetTimeoutForStreamingResults(int i) throws SQLException;

    void setNoAccessToProcedureBodies(boolean z);

    void setNoDatetimeStringSync(boolean z);

    void setNoTimezoneConversionForDateType(boolean z);

    void setNoTimezoneConversionForTimeType(boolean z);

    void setNullCatalogMeansCurrent(boolean z);

    void setNullNamePatternMatchesAll(boolean z);

    void setOverrideSupportsIntegrityEnhancementFacility(boolean z);

    void setPacketDebugBufferSize(int i) throws SQLException;

    void setPadCharsWithSpace(boolean z);

    void setParanoid(boolean z);

    void setParseInfoCacheFactory(String str);

    void setPasswordCharacterEncoding(String str);

    void setPedantic(boolean z);

    void setPinGlobalTxToPhysicalConnection(boolean z);

    void setPopulateInsertRowWithDefaultValues(boolean z);

    void setPrepStmtCacheSize(int i) throws SQLException;

    void setPrepStmtCacheSqlLimit(int i) throws SQLException;

    void setPreparedStatementCacheSize(int i) throws SQLException;

    void setPreparedStatementCacheSqlLimit(int i) throws SQLException;

    void setProcessEscapeCodesForPrepStmts(boolean z);

    void setProfileSQL(boolean z);

    void setProfileSql(boolean z);

    void setProfilerEventHandler(String str);

    void setPropertiesTransform(String str);

    void setQueriesBeforeRetryMaster(int i) throws SQLException;

    void setQueryTimeoutKillsConnection(boolean z);

    void setReadFromMasterWhenNoSlaves(boolean z);

    void setReadOnlyPropagatesToServer(boolean z);

    void setReconnectAtTxEnd(boolean z);

    void setRelaxAutoCommit(boolean z);

    void setReplicationEnableJMX(boolean z);

    void setReportMetricsIntervalMillis(int i) throws SQLException;

    void setRequireSSL(boolean z);

    void setResourceId(String str);

    void setResultSetSizeThreshold(int i) throws SQLException;

    void setRetainStatementAfterResultSetClose(boolean z);

    void setRetriesAllDown(int i) throws SQLException;

    void setRewriteBatchedStatements(boolean z);

    void setRollbackOnPooledClose(boolean z);

    void setRoundRobinLoadBalance(boolean z);

    void setRunningCTS13(boolean z);

    void setSecondsBeforeRetryMaster(int i) throws SQLException;

    void setSelfDestructOnPingMaxOperations(int i) throws SQLException;

    void setSelfDestructOnPingSecondsLifetime(int i) throws SQLException;

    void setSendFractionalSeconds(boolean z);

    void setServerAffinityOrder(String str);

    void setServerConfigCacheFactory(String str);

    void setServerRSAPublicKeyFile(String str) throws SQLException;

    void setServerTimezone(String str);

    void setSessionVariables(String str);

    void setSlowQueryThresholdMillis(int i) throws SQLException;

    void setSlowQueryThresholdNanos(long j) throws SQLException;

    void setSocketFactory(String str);

    void setSocketFactoryClassName(String str);

    void setSocketTimeout(int i) throws SQLException;

    void setSocksProxyHost(String str);

    void setSocksProxyPort(int i) throws SQLException;

    void setStatementInterceptors(String str);

    void setStrictFloatingPoint(boolean z);

    void setStrictUpdates(boolean z);

    void setTcpKeepAlive(boolean z);

    void setTcpNoDelay(boolean z);

    void setTcpRcvBuf(int i) throws SQLException;

    void setTcpSndBuf(int i) throws SQLException;

    void setTcpTrafficClass(int i) throws SQLException;

    void setTinyInt1isBit(boolean z);

    void setTraceProtocol(boolean z);

    void setTransformedBitIsBoolean(boolean z);

    void setTreatUtilDateAsTimestamp(boolean z);

    void setTrustCertificateKeyStorePassword(String str);

    void setTrustCertificateKeyStoreType(String str);

    void setTrustCertificateKeyStoreUrl(String str);

    void setUltraDevHack(boolean z);

    void setUseAffectedRows(boolean z);

    void setUseBlobToStoreUTF8OutsideBMP(boolean z);

    void setUseColumnNamesInFindColumn(boolean z);

    void setUseCompression(boolean z);

    void setUseConfigs(String str);

    void setUseCursorFetch(boolean z);

    void setUseDirectRowUnpack(boolean z);

    void setUseDynamicCharsetInfo(boolean z);

    void setUseFastDateParsing(boolean z);

    void setUseFastIntParsing(boolean z);

    void setUseGmtMillisForDatetimes(boolean z);

    void setUseHostsInPrivileges(boolean z);

    void setUseInformationSchema(boolean z);

    void setUseJDBCCompliantTimezoneShift(boolean z);

    void setUseJvmCharsetConverters(boolean z);

    void setUseLegacyDatetimeCode(boolean z);

    void setUseLocalSessionState(boolean z);

    void setUseLocalTransactionState(boolean z);

    void setUseNanosForElapsedTime(boolean z);

    void setUseOldAliasMetadataBehavior(boolean z);

    void setUseOldUTF8Behavior(boolean z);

    void setUseOnlyServerErrorMessages(boolean z);

    void setUseReadAheadInput(boolean z);

    void setUseSSL(boolean z);

    void setUseSSPSCompatibleTimezoneShift(boolean z);

    void setUseServerPrepStmts(boolean z);

    void setUseServerPreparedStmts(boolean z);

    void setUseSqlStateCodes(boolean z);

    void setUseStreamLengthsInPrepStmts(boolean z);

    void setUseTimezone(boolean z);

    void setUseUltraDevWorkAround(boolean z);

    void setUseUnbufferedInput(boolean z);

    void setUseUnicode(boolean z);

    void setUseUsageAdvisor(boolean z);

    void setUtf8OutsideBmpExcludedColumnNamePattern(String str);

    void setUtf8OutsideBmpIncludedColumnNamePattern(String str);

    void setVerifyServerCertificate(boolean z);

    void setYearIsDateType(boolean z);

    void setZeroDateTimeBehavior(String str);

    boolean useUnbufferedInput();
}
