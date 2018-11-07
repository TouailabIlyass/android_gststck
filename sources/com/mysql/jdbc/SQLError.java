package com.mysql.jdbc;

import com.mysql.jdbc.exceptions.MySQLDataException;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.exceptions.MySQLNonTransientConnectionException;
import com.mysql.jdbc.exceptions.MySQLQueryInterruptedException;
import com.mysql.jdbc.exceptions.MySQLSyntaxErrorException;
import com.mysql.jdbc.exceptions.MySQLTransactionRollbackException;
import com.mysql.jdbc.exceptions.MySQLTransientConnectionException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.BindException;
import java.sql.BatchUpdateException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;

public class SQLError {
    private static final long DEFAULT_WAIT_TIMEOUT_SECONDS = 28800;
    private static final int DUE_TO_TIMEOUT_FALSE = 0;
    private static final int DUE_TO_TIMEOUT_MAYBE = 2;
    private static final int DUE_TO_TIMEOUT_TRUE = 1;
    static final int ER_WARNING_NOT_COMPLETE_ROLLBACK = 1196;
    private static final Constructor<?> JDBC_4_COMMUNICATIONS_EXCEPTION_CTOR;
    public static final String SQL_STATE_ACTIVE_SQL_TRANSACTION = "25001";
    public static final String SQL_STATE_BASE_TABLE_NOT_FOUND = "S0002";
    public static final String SQL_STATE_BASE_TABLE_OR_VIEW_ALREADY_EXISTS = "S0001";
    public static final String SQL_STATE_BASE_TABLE_OR_VIEW_NOT_FOUND = "42S02";
    public static final String SQL_STATE_CARDINALITY_VIOLATION = "21000";
    public static final String SQL_STATE_CASE_NOT_FOUND_FOR_CASE_STATEMENT = "20000";
    public static final String SQL_STATE_CLI_SPECIFIC_CONDITION = "HY000";
    public static final String SQL_STATE_COLUMN_ALREADY_EXISTS = "S0021";
    public static final String SQL_STATE_COLUMN_NOT_FOUND = "S0022";
    public static final String SQL_STATE_COMMUNICATION_LINK_FAILURE = "08S01";
    public static final String SQL_STATE_CONNECTION_FAILURE = "08006";
    public static final String SQL_STATE_CONNECTION_IN_USE = "08002";
    public static final String SQL_STATE_CONNECTION_NOT_OPEN = "08003";
    public static final String SQL_STATE_CONNECTION_REJECTED = "08004";
    public static final String SQL_STATE_DATA_TRUNCATED = "01004";
    public static final String SQL_STATE_DATETIME_FIELD_OVERFLOW = "22008";
    public static final String SQL_STATE_DISCONNECT_ERROR = "01002";
    public static final String SQL_STATE_DIVISION_BY_ZERO = "22012";
    public static final String SQL_STATE_DRIVER_NOT_CAPABLE = "S1C00";
    public static final String SQL_STATE_ERROR_IN_ROW = "01S01";
    public static final String SQL_STATE_ER_BAD_FIELD_ERROR = "42S22";
    public static final String SQL_STATE_ER_DUP_FIELDNAME = "42S21";
    public static final String SQL_STATE_ER_NO_SUCH_INDEX = "42S12";
    public static final String SQL_STATE_ER_QUERY_INTERRUPTED = "70100";
    public static final String SQL_STATE_ER_TABLE_EXISTS_ERROR = "42S01";
    public static final String SQL_STATE_FEATURE_NOT_SUPPORTED = "0A000";
    public static final String SQL_STATE_GENERAL_ERROR = "S1000";
    public static final String SQL_STATE_ILLEGAL_ARGUMENT = "S1009";
    public static final String SQL_STATE_INDEX_ALREADY_EXISTS = "S0011";
    public static final String SQL_STATE_INDEX_NOT_FOUND = "S0012";
    public static final String SQL_STATE_INSERT_VALUE_LIST_NO_MATCH_COL_LIST = "21S01";
    public static final String SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION = "23000";
    public static final String SQL_STATE_INVALID_AUTH_SPEC = "28000";
    public static final String SQL_STATE_INVALID_CATALOG_NAME = "3D000";
    public static final String SQL_STATE_INVALID_CHARACTER_VALUE_FOR_CAST = "22018";
    public static final String SQL_STATE_INVALID_COLUMN_NUMBER = "S1002";
    public static final String SQL_STATE_INVALID_CONDITION_NUMBER = "35000";
    public static final String SQL_STATE_INVALID_CONNECTION_ATTRIBUTE = "01S00";
    public static final String SQL_STATE_INVALID_CURSOR_STATE = "24000";
    public static final String SQL_STATE_INVALID_DATETIME_FORMAT = "22007";
    public static final String SQL_STATE_INVALID_LOGARITHM_ARGUMENT = "2201E";
    public static final String SQL_STATE_INVALID_TRANSACTION_STATE = "25000";
    public static final String SQL_STATE_INVALID_TRANSACTION_TERMINATION = "2D000";
    public static final String SQL_STATE_MEMORY_ALLOCATION_ERROR = "HY001";
    public static final String SQL_STATE_MEMORY_ALLOCATION_FAILURE = "S1001";
    public static final String SQL_STATE_MORE_THAN_ONE_ROW_UPDATED_OR_DELETED = "01S04";
    public static final String SQL_STATE_NO_DATA = "02000";
    public static final String SQL_STATE_NO_DEFAULT_FOR_COLUMN = "S0023";
    public static final String SQL_STATE_NO_ROWS_UPDATED_OR_DELETED = "01S03";
    public static final String SQL_STATE_NULL_VALUE_NOT_ALLOWED = "22004";
    public static final String SQL_STATE_NUMERIC_VALUE_OUT_OF_RANGE = "22003";
    public static final String SQL_STATE_PRIVILEGE_NOT_REVOKED = "01006";
    public static final String SQL_STATE_READ_ONLY_SQL_TRANSACTION = "25006";
    public static final String SQL_STATE_RESIGNAL_WHEN_HANDLER_NOT_ACTIVE = "0K000";
    public static final String SQL_STATE_ROLLBACK_SERIALIZATION_FAILURE = "40001";
    public static final String SQL_STATE_SRE_FUNCTION_EXECUTED_NO_RETURN_STATEMENT = "2F005";
    public static final String SQL_STATE_SRE_PROHIBITED_SQL_STATEMENT_ATTEMPTED = "2F003";
    public static final String SQL_STATE_STACKED_DIAGNOSTICS_ACCESSED_WITHOUT_ACTIVE_HANDLER = "0Z002";
    public static final String SQL_STATE_STRING_DATA_RIGHT_TRUNCATION = "22001";
    public static final String SQL_STATE_SYNTAX_ERROR = "42000";
    public static final String SQL_STATE_TIMEOUT_EXPIRED = "S1T00";
    public static final String SQL_STATE_TRANSACTION_RESOLUTION_UNKNOWN = "08007";
    public static final String SQL_STATE_UNABLE_TO_CONNECT_TO_DATASOURCE = "08001";
    public static final String SQL_STATE_WARNING = "01000";
    public static final String SQL_STATE_WRONG_NO_OF_PARAMETERS = "07001";
    public static final String SQL_STATE_XAER_DUPID = "XAE08";
    public static final String SQL_STATE_XAER_INVAL = "XAE05";
    public static final String SQL_STATE_XAER_NOTA = "XAE04";
    public static final String SQL_STATE_XAER_OUTSIDE = "XAE09";
    public static final String SQL_STATE_XAER_RMFAIL = "XAE07";
    public static final String SQL_STATE_XA_RBDEADLOCK = "XA102";
    public static final String SQL_STATE_XA_RBROLLBACK = "XA100";
    public static final String SQL_STATE_XA_RBTIMEOUT = "XA106";
    public static final String SQL_STATE_XA_RMERR = "XAE03";
    private static Map<Integer, String> mysqlToSql99State = new HashMap();
    private static Map<Integer, String> mysqlToSqlState = new Hashtable();
    private static Map<String, String> sqlStateMessages = new HashMap();

    static {
        if (Util.isJdbc4()) {
            try {
                JDBC_4_COMMUNICATIONS_EXCEPTION_CTOR = Class.forName("com.mysql.jdbc.exceptions.jdbc4.CommunicationsException").getConstructor(new Class[]{MySQLConnection.class, Long.TYPE, Long.TYPE, Exception.class});
            } catch (SecurityException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e2) {
                throw new RuntimeException(e2);
            } catch (ClassNotFoundException e3) {
                throw new RuntimeException(e3);
            }
        }
        JDBC_4_COMMUNICATIONS_EXCEPTION_CTOR = null;
        sqlStateMessages.put(SQL_STATE_DISCONNECT_ERROR, Messages.getString("SQLError.35"));
        sqlStateMessages.put(SQL_STATE_DATA_TRUNCATED, Messages.getString("SQLError.36"));
        sqlStateMessages.put(SQL_STATE_PRIVILEGE_NOT_REVOKED, Messages.getString("SQLError.37"));
        sqlStateMessages.put(SQL_STATE_INVALID_CONNECTION_ATTRIBUTE, Messages.getString("SQLError.38"));
        sqlStateMessages.put(SQL_STATE_ERROR_IN_ROW, Messages.getString("SQLError.39"));
        sqlStateMessages.put(SQL_STATE_NO_ROWS_UPDATED_OR_DELETED, Messages.getString("SQLError.40"));
        sqlStateMessages.put(SQL_STATE_MORE_THAN_ONE_ROW_UPDATED_OR_DELETED, Messages.getString("SQLError.41"));
        sqlStateMessages.put(SQL_STATE_WRONG_NO_OF_PARAMETERS, Messages.getString("SQLError.42"));
        sqlStateMessages.put(SQL_STATE_UNABLE_TO_CONNECT_TO_DATASOURCE, Messages.getString("SQLError.43"));
        sqlStateMessages.put(SQL_STATE_CONNECTION_IN_USE, Messages.getString("SQLError.44"));
        sqlStateMessages.put(SQL_STATE_CONNECTION_NOT_OPEN, Messages.getString("SQLError.45"));
        sqlStateMessages.put(SQL_STATE_CONNECTION_REJECTED, Messages.getString("SQLError.46"));
        sqlStateMessages.put(SQL_STATE_TRANSACTION_RESOLUTION_UNKNOWN, Messages.getString("SQLError.47"));
        sqlStateMessages.put(SQL_STATE_COMMUNICATION_LINK_FAILURE, Messages.getString("SQLError.48"));
        sqlStateMessages.put(SQL_STATE_INSERT_VALUE_LIST_NO_MATCH_COL_LIST, Messages.getString("SQLError.49"));
        sqlStateMessages.put(SQL_STATE_NUMERIC_VALUE_OUT_OF_RANGE, Messages.getString("SQLError.50"));
        sqlStateMessages.put(SQL_STATE_DATETIME_FIELD_OVERFLOW, Messages.getString("SQLError.51"));
        sqlStateMessages.put(SQL_STATE_DIVISION_BY_ZERO, Messages.getString("SQLError.52"));
        sqlStateMessages.put(SQL_STATE_ROLLBACK_SERIALIZATION_FAILURE, Messages.getString("SQLError.53"));
        sqlStateMessages.put(SQL_STATE_INVALID_AUTH_SPEC, Messages.getString("SQLError.54"));
        sqlStateMessages.put(SQL_STATE_SYNTAX_ERROR, Messages.getString("SQLError.55"));
        sqlStateMessages.put(SQL_STATE_BASE_TABLE_OR_VIEW_NOT_FOUND, Messages.getString("SQLError.56"));
        sqlStateMessages.put(SQL_STATE_BASE_TABLE_OR_VIEW_ALREADY_EXISTS, Messages.getString("SQLError.57"));
        sqlStateMessages.put(SQL_STATE_BASE_TABLE_NOT_FOUND, Messages.getString("SQLError.58"));
        sqlStateMessages.put(SQL_STATE_INDEX_ALREADY_EXISTS, Messages.getString("SQLError.59"));
        sqlStateMessages.put(SQL_STATE_INDEX_NOT_FOUND, Messages.getString("SQLError.60"));
        sqlStateMessages.put(SQL_STATE_COLUMN_ALREADY_EXISTS, Messages.getString("SQLError.61"));
        sqlStateMessages.put(SQL_STATE_COLUMN_NOT_FOUND, Messages.getString("SQLError.62"));
        sqlStateMessages.put(SQL_STATE_NO_DEFAULT_FOR_COLUMN, Messages.getString("SQLError.63"));
        sqlStateMessages.put(SQL_STATE_GENERAL_ERROR, Messages.getString("SQLError.64"));
        sqlStateMessages.put(SQL_STATE_MEMORY_ALLOCATION_FAILURE, Messages.getString("SQLError.65"));
        sqlStateMessages.put(SQL_STATE_INVALID_COLUMN_NUMBER, Messages.getString("SQLError.66"));
        sqlStateMessages.put(SQL_STATE_ILLEGAL_ARGUMENT, Messages.getString("SQLError.67"));
        sqlStateMessages.put(SQL_STATE_DRIVER_NOT_CAPABLE, Messages.getString("SQLError.68"));
        sqlStateMessages.put(SQL_STATE_TIMEOUT_EXPIRED, Messages.getString("SQLError.69"));
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SELECT_REDUCED), SQL_STATE_WARNING);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_WARN_TOO_FEW_RECORDS), SQL_STATE_WARNING);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_WARN_TOO_MANY_RECORDS), SQL_STATE_WARNING);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_WARN_DATA_TRUNCATED), SQL_STATE_WARNING);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_UNINIT_VAR), SQL_STATE_WARNING);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SIGNAL_WARN), SQL_STATE_WARNING);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_CON_COUNT_ERROR), SQL_STATE_CONNECTION_REJECTED);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_NOT_SUPPORTED_AUTH_MODE), SQL_STATE_CONNECTION_REJECTED);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_BAD_HOST_ERROR), SQL_STATE_CONNECTION_REJECTED);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_HANDSHAKE_ERROR), SQL_STATE_CONNECTION_REJECTED);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_HOST_IS_BLOCKED), SQL_STATE_CONNECTION_REJECTED);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_HOST_NOT_PRIVILEGED), SQL_STATE_CONNECTION_REJECTED);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_UNKNOWN_COM_ERROR), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SERVER_SHUTDOWN), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_FORCING_CLOSE), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_IPSOCK_ERROR), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_ABORTING_CONNECTION), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_NET_PACKET_TOO_LARGE), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_NET_READ_ERROR_FROM_PIPE), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_NET_FCNTL_ERROR), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_NET_PACKETS_OUT_OF_ORDER), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_NET_UNCOMPRESS_ERROR), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_NET_READ_ERROR), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_NET_READ_INTERRUPTED), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_NET_ERROR_ON_WRITE), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_NET_WRITE_INTERRUPTED), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_NEW_ABORTING_CONNECTION), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_MASTER_NET_READ), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_MASTER_NET_WRITE), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_CONNECT_TO_MASTER), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_BADSELECT), SQL_STATE_FEATURE_NOT_SUPPORTED);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_BADSTATEMENT), SQL_STATE_FEATURE_NOT_SUPPORTED);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_SUBSELECT_NYI), SQL_STATE_FEATURE_NOT_SUPPORTED);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_STMT_NOT_ALLOWED_IN_SF_OR_TRG), SQL_STATE_FEATURE_NOT_SUPPORTED);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_NO_RETSET), SQL_STATE_FEATURE_NOT_SUPPORTED);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_ALTER_OPERATION_NOT_SUPPORTED), SQL_STATE_FEATURE_NOT_SUPPORTED);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_ALTER_OPERATION_NOT_SUPPORTED_REASON), SQL_STATE_FEATURE_NOT_SUPPORTED);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_DBACCESS_DENIED_ERROR), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_BAD_DB_ERROR), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_FIELD_WITH_GROUP), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_GROUP_FIELD), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_SUM_SELECT), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_TOO_LONG_IDENT), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_DUP_FIELDNAME), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_DUP_KEYNAME), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_DUP_ENTRY), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_FIELD_SPEC), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_PARSE_ERROR), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_EMPTY_QUERY), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_NONUNIQ_TABLE), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_INVALID_DEFAULT), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_MULTIPLE_PRI_KEY), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_TOO_MANY_KEYS), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_TOO_MANY_KEY_PARTS), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_TOO_LONG_KEY), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_KEY_COLUMN_DOES_NOT_EXITS), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_BLOB_USED_AS_KEY), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_TOO_BIG_FIELDLENGTH), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_AUTO_KEY), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_NO_SUCH_INDEX), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_FIELD_TERMINATORS), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_BLOBS_AND_NO_TERMINATED), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_CANT_REMOVE_ALL_FIELDS), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_CANT_DROP_FIELD_OR_KEY), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_BLOB_CANT_HAVE_DEFAULT), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_DB_NAME), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_TABLE_NAME), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_TOO_BIG_SELECT), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_UNKNOWN_PROCEDURE), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_PARAMCOUNT_TO_PROCEDURE), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_FIELD_SPECIFIED_TWICE), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_UNSUPPORTED_EXTENSION), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_TABLE_MUST_HAVE_COLUMNS), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_UNKNOWN_CHARACTER_SET), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_TOO_BIG_ROWSIZE), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_OUTER_JOIN), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_NULL_COLUMN_IN_INDEX), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_PASSWORD_ANONYMOUS_USER), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_PASSWORD_NOT_ALLOWED), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_PASSWORD_NO_MATCH), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_REGEXP_ERROR), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_MIX_OF_GROUP_FUNC_AND_FIELDS), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_NONEXISTING_GRANT), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_TABLEACCESS_DENIED_ERROR), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_COLUMNACCESS_DENIED_ERROR), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_ILLEGAL_GRANT_FOR_TABLE), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_GRANT_WRONG_HOST_OR_USER), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_NONEXISTING_TABLE_GRANT), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_NOT_ALLOWED_COMMAND), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SYNTAX_ERROR), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_TOO_LONG_STRING), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_TABLE_CANT_HANDLE_BLOB), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_TABLE_CANT_HANDLE_AUTO_INCREMENT), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_COLUMN_NAME), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_KEY_COLUMN), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_BLOB_KEY_WITHOUT_LENGTH), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_PRIMARY_CANT_HAVE_NULL), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_TOO_MANY_ROWS), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_REQUIRES_PRIMARY_KEY), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_KEY_DOES_NOT_EXITS), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_CHECK_NO_SUCH_TABLE), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_CHECK_NOT_IMPLEMENTED), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_TOO_MANY_USER_CONNECTIONS), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_NO_PERMISSION_TO_CREATE_USER), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_USER_LIMIT_REACHED), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SPECIFIC_ACCESS_DENIED_ERROR), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_NO_DEFAULT), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_VALUE_FOR_VAR), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_TYPE_FOR_VAR), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_CANT_USE_OPTION_HERE), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_NOT_SUPPORTED_YET), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_FK_DEF), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_DERIVED_MUST_HAVE_ALIAS), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_TABLENAME_NOT_ALLOWED_HERE), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SPATIAL_CANT_HAVE_NULL), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_COLLATION_CHARSET_MISMATCH), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_NAME_FOR_INDEX), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_NAME_FOR_CATALOG), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_UNKNOWN_STORAGE_ENGINE), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_ALREADY_EXISTS), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_DOES_NOT_EXIST), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_LILABEL_MISMATCH), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_LABEL_REDEFINE), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_LABEL_MISMATCH), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_BADRETURN), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_UPDATE_LOG_DEPRECATED_IGNORED), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_UPDATE_LOG_DEPRECATED_TRANSLATED), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_WRONG_NO_OF_ARGS), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_COND_MISMATCH), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_NORETURN), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_BAD_CURSOR_QUERY), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_BAD_CURSOR_SELECT), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_CURSOR_MISMATCH), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_UNDECLARED_VAR), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_DUP_PARAM), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_DUP_VAR), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_DUP_COND), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_DUP_CURS), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_VARCOND_AFTER_CURSHNDLR), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_CURSOR_AFTER_HANDLER), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_PROCACCESS_DENIED_ERROR), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_NONEXISTING_PROC_GRANT), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_BAD_SQLSTATE), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_CANT_CREATE_USER_WITH_GRANT), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_DUP_HANDLER), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_NOT_VAR_ARG), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_TOO_BIG_SCALE), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_TOO_BIG_PRECISION), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_M_BIGGER_THAN_D), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_TOO_LONG_BODY), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_TOO_BIG_DISPLAYWIDTH), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_BAD_VAR_SHADOW), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_WRONG_NAME), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_NO_AGGREGATE), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_MAX_PREPARED_STMT_COUNT_REACHED), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_NON_GROUPING_FIELD_USED), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_PARAMCOUNT_TO_NATIVE_FCT), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_PARAMETERS_TO_NATIVE_FCT), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_PARAMETERS_TO_STORED_FCT), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_FUNC_INEXISTENT_NAME_COLLISION), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_DUP_SIGNAL_SET), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SPATIAL_MUST_HAVE_GEOM_COL), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_TRUNCATE_ILLEGAL_FK), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_NUMBER_OF_COLUMNS_IN_SELECT), SQL_STATE_CARDINALITY_VIOLATION);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_OPERAND_COLUMNS), SQL_STATE_CARDINALITY_VIOLATION);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SUBQUERY_NO_1_ROW), SQL_STATE_CARDINALITY_VIOLATION);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_DUP_KEY), SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_BAD_NULL_ERROR), SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_NON_UNIQ_ERROR), SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_DUP_UNIQUE), SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_NO_REFERENCED_ROW), SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_ROW_IS_REFERENCED), SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_ROW_IS_REFERENCED_2), SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_NO_REFERENCED_ROW_2), SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_FOREIGN_DUPLICATE_KEY), SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_DUP_ENTRY_WITH_KEY_NAME), SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_FOREIGN_DUPLICATE_KEY_WITH_CHILD_INFO), SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_FOREIGN_DUPLICATE_KEY_WITHOUT_CHILD_INFO), SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_DUP_UNKNOWN_IN_INDEX), SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_DATA_TOO_LONG), SQL_STATE_STRING_DATA_RIGHT_TRUNCATION);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_WARN_DATA_OUT_OF_RANGE), SQL_STATE_WARNING);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_CANT_CREATE_GEOMETRY_OBJECT), SQL_STATE_NUMERIC_VALUE_OUT_OF_RANGE);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_DATA_OUT_OF_RANGE), SQL_STATE_NUMERIC_VALUE_OUT_OF_RANGE);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_TRUNCATED_WRONG_VALUE), SQL_STATE_INVALID_DATETIME_FORMAT);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_ILLEGAL_VALUE_FOR_TYPE), SQL_STATE_INVALID_DATETIME_FORMAT);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_DATETIME_FUNCTION_OVERFLOW), SQL_STATE_DATETIME_FIELD_OVERFLOW);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_DIVISION_BY_ZERO), SQL_STATE_DIVISION_BY_ZERO);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_CURSOR_ALREADY_OPEN), SQL_STATE_INVALID_CURSOR_STATE);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_CURSOR_NOT_OPEN), SQL_STATE_INVALID_CURSOR_STATE);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_CANT_DO_THIS_DURING_AN_TRANSACTION), SQL_STATE_INVALID_TRANSACTION_STATE);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_READ_ONLY_TRANSACTION), SQL_STATE_INVALID_TRANSACTION_STATE);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_ACCESS_DENIED_ERROR), SQL_STATE_INVALID_AUTH_SPEC);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_ACCESS_DENIED_NO_PASSWORD_ERROR), SQL_STATE_INVALID_AUTH_SPEC);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_ACCESS_DENIED_CHANGE_USER_ERROR), SQL_STATE_INVALID_AUTH_SPEC);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_DA_INVALID_CONDITION_NUMBER), SQL_STATE_INVALID_CONDITION_NUMBER);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_NO_DB_ERROR), SQL_STATE_INVALID_CATALOG_NAME);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_VALUE_COUNT), SQL_STATE_INSERT_VALUE_LIST_NO_MATCH_COL_LIST);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_VALUE_COUNT_ON_ROW), SQL_STATE_INSERT_VALUE_LIST_NO_MATCH_COL_LIST);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_TABLE_EXISTS_ERROR), SQL_STATE_ER_TABLE_EXISTS_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_BAD_TABLE_ERROR), SQL_STATE_BASE_TABLE_OR_VIEW_NOT_FOUND);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_UNKNOWN_TABLE), SQL_STATE_BASE_TABLE_OR_VIEW_NOT_FOUND);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_NO_SUCH_TABLE), SQL_STATE_BASE_TABLE_OR_VIEW_NOT_FOUND);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_BAD_FIELD_ERROR), SQL_STATE_COLUMN_NOT_FOUND);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_ILLEGAL_REFERENCE), SQL_STATE_ER_BAD_FIELD_ERROR);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_OUTOFMEMORY), SQL_STATE_MEMORY_ALLOCATION_FAILURE);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_OUT_OF_SORTMEMORY), SQL_STATE_MEMORY_ALLOCATION_FAILURE);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_LOCK_WAIT_TIMEOUT), SQL_STATE_ROLLBACK_SERIALIZATION_FAILURE);
        mysqlToSqlState.put(Integer.valueOf(MysqlErrorNumbers.ER_LOCK_DEADLOCK), SQL_STATE_ROLLBACK_SERIALIZATION_FAILURE);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SELECT_REDUCED), SQL_STATE_WARNING);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_WARN_TOO_FEW_RECORDS), SQL_STATE_WARNING);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_WARN_TOO_MANY_RECORDS), SQL_STATE_WARNING);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_WARN_DATA_TRUNCATED), SQL_STATE_WARNING);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_WARN_NULL_TO_NOTNULL), SQL_STATE_WARNING);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_WARN_DATA_OUT_OF_RANGE), SQL_STATE_WARNING);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_UNINIT_VAR), SQL_STATE_WARNING);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SIGNAL_WARN), SQL_STATE_WARNING);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_FETCH_NO_DATA), SQL_STATE_NO_DATA);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SIGNAL_NOT_FOUND), SQL_STATE_NO_DATA);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_CON_COUNT_ERROR), SQL_STATE_CONNECTION_REJECTED);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_NOT_SUPPORTED_AUTH_MODE), SQL_STATE_CONNECTION_REJECTED);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_BAD_HOST_ERROR), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_HANDSHAKE_ERROR), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_UNKNOWN_COM_ERROR), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SERVER_SHUTDOWN), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_FORCING_CLOSE), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_IPSOCK_ERROR), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_ABORTING_CONNECTION), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_NET_PACKET_TOO_LARGE), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_NET_READ_ERROR_FROM_PIPE), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_NET_FCNTL_ERROR), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_NET_PACKETS_OUT_OF_ORDER), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_NET_UNCOMPRESS_ERROR), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_NET_READ_ERROR), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_NET_READ_INTERRUPTED), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_NET_ERROR_ON_WRITE), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_NET_WRITE_INTERRUPTED), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_NEW_ABORTING_CONNECTION), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_MASTER_NET_READ), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_MASTER_NET_WRITE), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_CONNECT_TO_MASTER), SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_BADSELECT), SQL_STATE_FEATURE_NOT_SUPPORTED);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_BADSTATEMENT), SQL_STATE_FEATURE_NOT_SUPPORTED);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_SUBSELECT_NYI), SQL_STATE_FEATURE_NOT_SUPPORTED);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_STMT_NOT_ALLOWED_IN_SF_OR_TRG), SQL_STATE_FEATURE_NOT_SUPPORTED);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_NO_RETSET), SQL_STATE_FEATURE_NOT_SUPPORTED);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_ALTER_OPERATION_NOT_SUPPORTED), SQL_STATE_FEATURE_NOT_SUPPORTED);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_ALTER_OPERATION_NOT_SUPPORTED_REASON), SQL_STATE_FEATURE_NOT_SUPPORTED);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_DBACCESS_DENIED_ERROR), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_BAD_DB_ERROR), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_FIELD_WITH_GROUP), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_GROUP_FIELD), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_SUM_SELECT), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_TOO_LONG_IDENT), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_DUP_KEYNAME), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_FIELD_SPEC), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_PARSE_ERROR), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_EMPTY_QUERY), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_NONUNIQ_TABLE), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_INVALID_DEFAULT), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_MULTIPLE_PRI_KEY), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_TOO_MANY_KEYS), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_TOO_MANY_KEY_PARTS), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_TOO_LONG_KEY), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_KEY_COLUMN_DOES_NOT_EXITS), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_BLOB_USED_AS_KEY), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_TOO_BIG_FIELDLENGTH), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_AUTO_KEY), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_FIELD_TERMINATORS), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_BLOBS_AND_NO_TERMINATED), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_CANT_REMOVE_ALL_FIELDS), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_CANT_DROP_FIELD_OR_KEY), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_BLOB_CANT_HAVE_DEFAULT), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_DB_NAME), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_TABLE_NAME), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_TOO_BIG_SELECT), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_UNKNOWN_PROCEDURE), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_PARAMCOUNT_TO_PROCEDURE), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_FIELD_SPECIFIED_TWICE), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_UNSUPPORTED_EXTENSION), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_TABLE_MUST_HAVE_COLUMNS), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_UNKNOWN_CHARACTER_SET), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_TOO_BIG_ROWSIZE), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_OUTER_JOIN), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_NULL_COLUMN_IN_INDEX), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_PASSWORD_ANONYMOUS_USER), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_PASSWORD_NOT_ALLOWED), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_PASSWORD_NO_MATCH), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_REGEXP_ERROR), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_MIX_OF_GROUP_FUNC_AND_FIELDS), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_NONEXISTING_GRANT), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_TABLEACCESS_DENIED_ERROR), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_COLUMNACCESS_DENIED_ERROR), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_ILLEGAL_GRANT_FOR_TABLE), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_GRANT_WRONG_HOST_OR_USER), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_NONEXISTING_TABLE_GRANT), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_NOT_ALLOWED_COMMAND), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SYNTAX_ERROR), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_TOO_LONG_STRING), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_TABLE_CANT_HANDLE_BLOB), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_TABLE_CANT_HANDLE_AUTO_INCREMENT), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_COLUMN_NAME), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_KEY_COLUMN), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_BLOB_KEY_WITHOUT_LENGTH), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_PRIMARY_CANT_HAVE_NULL), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_TOO_MANY_ROWS), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_REQUIRES_PRIMARY_KEY), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_KEY_DOES_NOT_EXITS), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_CHECK_NO_SUCH_TABLE), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_CHECK_NOT_IMPLEMENTED), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_TOO_MANY_USER_CONNECTIONS), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_NO_PERMISSION_TO_CREATE_USER), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_USER_LIMIT_REACHED), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SPECIFIC_ACCESS_DENIED_ERROR), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_NO_DEFAULT), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_VALUE_FOR_VAR), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_TYPE_FOR_VAR), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_CANT_USE_OPTION_HERE), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_NOT_SUPPORTED_YET), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_FK_DEF), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_DERIVED_MUST_HAVE_ALIAS), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_TABLENAME_NOT_ALLOWED_HERE), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SPATIAL_CANT_HAVE_NULL), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_COLLATION_CHARSET_MISMATCH), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_NAME_FOR_INDEX), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_NAME_FOR_CATALOG), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_UNKNOWN_STORAGE_ENGINE), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_ALREADY_EXISTS), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_DOES_NOT_EXIST), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_LILABEL_MISMATCH), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_LABEL_REDEFINE), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_LABEL_MISMATCH), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_BADRETURN), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_UPDATE_LOG_DEPRECATED_IGNORED), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_UPDATE_LOG_DEPRECATED_TRANSLATED), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_WRONG_NO_OF_ARGS), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_COND_MISMATCH), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_NORETURN), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_BAD_CURSOR_QUERY), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_BAD_CURSOR_SELECT), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_CURSOR_MISMATCH), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_UNDECLARED_VAR), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_DUP_PARAM), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_DUP_VAR), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_DUP_COND), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_DUP_CURS), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_VARCOND_AFTER_CURSHNDLR), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_CURSOR_AFTER_HANDLER), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_PROCACCESS_DENIED_ERROR), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_NONEXISTING_PROC_GRANT), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_BAD_SQLSTATE), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_CANT_CREATE_USER_WITH_GRANT), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_DUP_HANDLER), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_NOT_VAR_ARG), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_TOO_BIG_SCALE), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_TOO_BIG_PRECISION), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_M_BIGGER_THAN_D), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_TOO_LONG_BODY), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_TOO_BIG_DISPLAYWIDTH), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_BAD_VAR_SHADOW), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_WRONG_NAME), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_NO_AGGREGATE), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_MAX_PREPARED_STMT_COUNT_REACHED), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_NON_GROUPING_FIELD_USED), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_PARAMCOUNT_TO_NATIVE_FCT), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_PARAMETERS_TO_NATIVE_FCT), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_PARAMETERS_TO_STORED_FCT), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_FUNC_INEXISTENT_NAME_COLLISION), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_DUP_SIGNAL_SET), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SPATIAL_MUST_HAVE_GEOM_COL), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_TRUNCATE_ILLEGAL_FK), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_NUMBER_OF_COLUMNS_IN_SELECT), SQL_STATE_CARDINALITY_VIOLATION);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_OPERAND_COLUMNS), SQL_STATE_CARDINALITY_VIOLATION);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SUBQUERY_NO_1_ROW), SQL_STATE_CARDINALITY_VIOLATION);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_DUP_KEY), SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_BAD_NULL_ERROR), SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_NON_UNIQ_ERROR), SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_DUP_ENTRY), SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_DUP_UNIQUE), SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_NO_REFERENCED_ROW), SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_ROW_IS_REFERENCED), SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_ROW_IS_REFERENCED_2), SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_NO_REFERENCED_ROW_2), SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_FOREIGN_DUPLICATE_KEY), SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_DUP_ENTRY_WITH_KEY_NAME), SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_FOREIGN_DUPLICATE_KEY_WITH_CHILD_INFO), SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_FOREIGN_DUPLICATE_KEY_WITHOUT_CHILD_INFO), SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_DUP_UNKNOWN_IN_INDEX), SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_DATA_TOO_LONG), SQL_STATE_STRING_DATA_RIGHT_TRUNCATION);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_CANT_CREATE_GEOMETRY_OBJECT), SQL_STATE_NUMERIC_VALUE_OUT_OF_RANGE);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_DATA_OUT_OF_RANGE), SQL_STATE_NUMERIC_VALUE_OUT_OF_RANGE);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_TRUNCATED_WRONG_VALUE), SQL_STATE_INVALID_DATETIME_FORMAT);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_ILLEGAL_VALUE_FOR_TYPE), SQL_STATE_INVALID_DATETIME_FORMAT);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_DATETIME_FUNCTION_OVERFLOW), SQL_STATE_DATETIME_FIELD_OVERFLOW);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_DIVISION_BY_ZERO), SQL_STATE_DIVISION_BY_ZERO);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_CURSOR_ALREADY_OPEN), SQL_STATE_INVALID_CURSOR_STATE);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_CURSOR_NOT_OPEN), SQL_STATE_INVALID_CURSOR_STATE);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_CANT_DO_THIS_DURING_AN_TRANSACTION), SQL_STATE_INVALID_TRANSACTION_STATE);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_READ_ONLY_TRANSACTION), SQL_STATE_INVALID_TRANSACTION_STATE);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_ACCESS_DENIED_ERROR), SQL_STATE_INVALID_AUTH_SPEC);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_ACCESS_DENIED_NO_PASSWORD_ERROR), SQL_STATE_INVALID_AUTH_SPEC);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_ACCESS_DENIED_CHANGE_USER_ERROR), SQL_STATE_INVALID_AUTH_SPEC);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_DA_INVALID_CONDITION_NUMBER), SQL_STATE_INVALID_CONDITION_NUMBER);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_NO_DB_ERROR), SQL_STATE_INVALID_CATALOG_NAME);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_RESIGNAL_WITHOUT_ACTIVE_HANDLER), SQL_STATE_RESIGNAL_WHEN_HANDLER_NOT_ACTIVE);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_GET_STACKED_DA_WITHOUT_ACTIVE_HANDLER), SQL_STATE_STACKED_DIAGNOSTICS_ACCESSED_WITHOUT_ACTIVE_HANDLER);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_CASE_NOT_FOUND), SQL_STATE_CASE_NOT_FOUND_FOR_CASE_STATEMENT);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_VALUE_COUNT), SQL_STATE_INSERT_VALUE_LIST_NO_MATCH_COL_LIST);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_WRONG_VALUE_COUNT_ON_ROW), SQL_STATE_INSERT_VALUE_LIST_NO_MATCH_COL_LIST);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_INVALID_USE_OF_NULL), SQL_STATE_SYNTAX_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_INVALID_ARGUMENT_FOR_LOGARITHM), SQL_STATE_INVALID_LOGARITHM_ARGUMENT);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_CANT_CHANGE_TX_ISOLATION), SQL_STATE_ACTIVE_SQL_TRANSACTION);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_CANT_EXECUTE_IN_READ_ONLY_TRANSACTION), SQL_STATE_READ_ONLY_SQL_TRANSACTION);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_NO_RECURSIVE_CREATE), SQL_STATE_SRE_PROHIBITED_SQL_STATEMENT_ATTEMPTED);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_SP_NORETURNEND), SQL_STATE_SRE_FUNCTION_EXECUTED_NO_RETURN_STATEMENT);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_TABLE_EXISTS_ERROR), SQL_STATE_ER_TABLE_EXISTS_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_BAD_TABLE_ERROR), SQL_STATE_BASE_TABLE_OR_VIEW_NOT_FOUND);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_UNKNOWN_TABLE), SQL_STATE_BASE_TABLE_OR_VIEW_NOT_FOUND);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_NO_SUCH_TABLE), SQL_STATE_BASE_TABLE_OR_VIEW_NOT_FOUND);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_NO_SUCH_INDEX), SQL_STATE_ER_NO_SUCH_INDEX);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_DUP_FIELDNAME), SQL_STATE_ER_DUP_FIELDNAME);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_BAD_FIELD_ERROR), SQL_STATE_ER_BAD_FIELD_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_ILLEGAL_REFERENCE), SQL_STATE_ER_BAD_FIELD_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_QUERY_INTERRUPTED), SQL_STATE_ER_QUERY_INTERRUPTED);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_OUTOFMEMORY), SQL_STATE_MEMORY_ALLOCATION_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_OUT_OF_SORTMEMORY), SQL_STATE_MEMORY_ALLOCATION_ERROR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_XA_RBROLLBACK), SQL_STATE_XA_RBROLLBACK);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_XA_RBDEADLOCK), SQL_STATE_XA_RBDEADLOCK);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_XA_RBTIMEOUT), SQL_STATE_XA_RBTIMEOUT);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_XA_RMERR), SQL_STATE_XA_RMERR);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_XAER_NOTA), SQL_STATE_XAER_NOTA);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_XAER_INVAL), SQL_STATE_XAER_INVAL);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_XAER_RMFAIL), SQL_STATE_XAER_RMFAIL);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_XAER_DUPID), SQL_STATE_XAER_DUPID);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_XAER_OUTSIDE), SQL_STATE_XAER_OUTSIDE);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_LOCK_WAIT_TIMEOUT), SQL_STATE_ROLLBACK_SERIALIZATION_FAILURE);
        mysqlToSql99State.put(Integer.valueOf(MysqlErrorNumbers.ER_LOCK_DEADLOCK), SQL_STATE_ROLLBACK_SERIALIZATION_FAILURE);
    }

    static SQLWarning convertShowWarningsToSQLWarnings(Connection connection) throws SQLException {
        return convertShowWarningsToSQLWarnings(connection, 0, false);
    }

    static SQLWarning convertShowWarningsToSQLWarnings(Connection connection, int warningCountIfKnown, boolean forTruncationOnly) throws SQLException {
        SQLException sQLException;
        Statement stmt = null;
        ResultSet warnRs = null;
        SQLWarning currentWarning = null;
        if (warningCountIfKnown < 100) {
            try {
                stmt = connection.createStatement();
                stmt.setFetchSize(0);
                if (stmt.getMaxRows() != 0) {
                    stmt.setMaxRows(0);
                }
            } catch (Throwable th) {
                sQLException = null;
                if (warnRs != null) {
                    try {
                        warnRs.close();
                    } catch (SQLException sqlEx) {
                        sQLException = sqlEx;
                    }
                }
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException sqlEx2) {
                        sQLException = sqlEx2;
                    }
                }
                if (sQLException != null) {
                }
            }
        } else {
            stmt = connection.createStatement(1003, 1007);
            stmt.setFetchSize(Integer.MIN_VALUE);
        }
        warnRs = stmt.executeQuery("SHOW WARNINGS");
        while (warnRs.next()) {
            int code = warnRs.getInt("Code");
            if (!forTruncationOnly) {
                SQLWarning newWarning = new SQLWarning(warnRs.getString("Message"), mysqlToSqlState(code, connection.getUseSqlStateCodes()), code);
                if (currentWarning == null) {
                    currentWarning = newWarning;
                } else {
                    currentWarning.setNextWarning(newWarning);
                }
            } else if (code == MysqlErrorNumbers.ER_WARN_DATA_TRUNCATED || code == MysqlErrorNumbers.ER_WARN_DATA_OUT_OF_RANGE) {
                SQLWarning newTruncation = new MysqlDataTruncation(warnRs.getString("Message"), 0, false, false, 0, 0, code);
                if (currentWarning == null) {
                    currentWarning = newTruncation;
                } else {
                    currentWarning.setNextWarning(newTruncation);
                }
            }
        }
        if (!forTruncationOnly || currentWarning == null) {
            sQLException = null;
            if (warnRs != null) {
                try {
                    warnRs.close();
                } catch (SQLException sqlEx22) {
                    sQLException = sqlEx22;
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx222) {
                    sQLException = sqlEx222;
                }
            }
            if (sQLException == null) {
                return currentWarning;
            }
            throw sQLException;
        }
        throw currentWarning;
    }

    public static void dumpSqlStatesMappingsAsXml() throws Exception {
        TreeMap<Integer, Integer> allErrorNumbers = new TreeMap();
        Map<Object, String> mysqlErrorNumbersToNames = new HashMap();
        for (Integer errorNumber : mysqlToSql99State.keySet()) {
            allErrorNumbers.put(errorNumber, errorNumber);
        }
        for (Integer errorNumber2 : mysqlToSqlState.keySet()) {
            allErrorNumbers.put(errorNumber2, errorNumber2);
        }
        Field[] possibleFields = MysqlErrorNumbers.class.getDeclaredFields();
        for (int i = 0; i < possibleFields.length; i++) {
            String fieldName = possibleFields[i].getName();
            if (fieldName.startsWith("ER_")) {
                mysqlErrorNumbersToNames.put(possibleFields[i].get(null), fieldName);
            }
        }
        System.out.println("<ErrorMappings>");
        for (Integer errorNumber3 : allErrorNumbers.keySet()) {
            String sql92State = mysqlToSql99(errorNumber3.intValue());
            String oldSqlState = mysqlToXOpen(errorNumber3.intValue());
            PrintStream printStream = System.out;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("   <ErrorMapping mysqlErrorNumber=\"");
            stringBuilder.append(errorNumber3);
            stringBuilder.append("\" mysqlErrorName=\"");
            stringBuilder.append((String) mysqlErrorNumbersToNames.get(errorNumber3));
            stringBuilder.append("\" legacySqlState=\"");
            stringBuilder.append(oldSqlState == null ? "" : oldSqlState);
            stringBuilder.append("\" sql92SqlState=\"");
            stringBuilder.append(sql92State == null ? "" : sql92State);
            stringBuilder.append("\"/>");
            printStream.println(stringBuilder.toString());
        }
        System.out.println("</ErrorMappings>");
    }

    static String get(String stateCode) {
        return (String) sqlStateMessages.get(stateCode);
    }

    private static String mysqlToSql99(int errno) {
        Integer err = Integer.valueOf(errno);
        if (mysqlToSql99State.containsKey(err)) {
            return (String) mysqlToSql99State.get(err);
        }
        return SQL_STATE_CLI_SPECIFIC_CONDITION;
    }

    static String mysqlToSqlState(int errno, boolean useSql92States) {
        if (useSql92States) {
            return mysqlToSql99(errno);
        }
        return mysqlToXOpen(errno);
    }

    private static String mysqlToXOpen(int errno) {
        Integer err = Integer.valueOf(errno);
        if (mysqlToSqlState.containsKey(err)) {
            return (String) mysqlToSqlState.get(err);
        }
        return SQL_STATE_GENERAL_ERROR;
    }

    public static SQLException createSQLException(String message, String sqlState, ExceptionInterceptor interceptor) {
        return createSQLException(message, sqlState, 0, interceptor);
    }

    public static SQLException createSQLException(String message, ExceptionInterceptor interceptor) {
        return createSQLException(message, interceptor, null);
    }

    public static SQLException createSQLException(String message, ExceptionInterceptor interceptor, Connection conn) {
        return runThroughExceptionInterceptor(interceptor, new SQLException(message), conn);
    }

    public static SQLException createSQLException(String message, String sqlState, Throwable cause, ExceptionInterceptor interceptor) {
        return createSQLException(message, sqlState, cause, interceptor, null);
    }

    public static SQLException createSQLException(String message, String sqlState, Throwable cause, ExceptionInterceptor interceptor, Connection conn) {
        SQLException sqlEx = createSQLException(message, sqlState, (ExceptionInterceptor) null);
        if (sqlEx.getCause() == null) {
            sqlEx.initCause(cause);
        }
        return runThroughExceptionInterceptor(interceptor, sqlEx, conn);
    }

    public static SQLException createSQLException(String message, String sqlState, int vendorErrorCode, ExceptionInterceptor interceptor) {
        return createSQLException(message, sqlState, vendorErrorCode, false, interceptor);
    }

    public static SQLException createSQLException(String message, String sqlState, int vendorErrorCode, boolean isTransient, ExceptionInterceptor interceptor) {
        return createSQLException(message, sqlState, vendorErrorCode, isTransient, interceptor, null);
    }

    public static SQLException createSQLException(String message, String sqlState, int vendorErrorCode, boolean isTransient, ExceptionInterceptor interceptor, Connection conn) {
        SQLException sqlEx;
        if (sqlState != null) {
            try {
                if (sqlState.startsWith("08")) {
                    if (isTransient) {
                        if (Util.isJdbc4()) {
                            sqlEx = (SQLException) Util.getInstance("com.mysql.jdbc.exceptions.jdbc4.MySQLTransientConnectionException", new Class[]{String.class, String.class, Integer.TYPE}, new Object[]{message, sqlState, Integer.valueOf(vendorErrorCode)}, interceptor);
                        } else {
                            sqlEx = new MySQLTransientConnectionException(message, sqlState, vendorErrorCode);
                        }
                    } else if (Util.isJdbc4()) {
                        sqlEx = (SQLException) Util.getInstance("com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException", new Class[]{String.class, String.class, Integer.TYPE}, new Object[]{message, sqlState, Integer.valueOf(vendorErrorCode)}, interceptor);
                    } else {
                        sqlEx = new MySQLNonTransientConnectionException(message, sqlState, vendorErrorCode);
                    }
                } else if (sqlState.startsWith("22")) {
                    if (Util.isJdbc4()) {
                        sqlEx = (SQLException) Util.getInstance("com.mysql.jdbc.exceptions.jdbc4.MySQLDataException", new Class[]{String.class, String.class, Integer.TYPE}, new Object[]{message, sqlState, Integer.valueOf(vendorErrorCode)}, interceptor);
                    } else {
                        sqlEx = new MySQLDataException(message, sqlState, vendorErrorCode);
                    }
                } else if (sqlState.startsWith("23")) {
                    if (Util.isJdbc4()) {
                        sqlEx = (SQLException) Util.getInstance("com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException", new Class[]{String.class, String.class, Integer.TYPE}, new Object[]{message, sqlState, Integer.valueOf(vendorErrorCode)}, interceptor);
                    } else {
                        sqlEx = new MySQLIntegrityConstraintViolationException(message, sqlState, vendorErrorCode);
                    }
                } else if (sqlState.startsWith("42")) {
                    if (Util.isJdbc4()) {
                        sqlEx = (SQLException) Util.getInstance("com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException", new Class[]{String.class, String.class, Integer.TYPE}, new Object[]{message, sqlState, Integer.valueOf(vendorErrorCode)}, interceptor);
                    } else {
                        sqlEx = new MySQLSyntaxErrorException(message, sqlState, vendorErrorCode);
                    }
                } else if (sqlState.startsWith("40")) {
                    if (Util.isJdbc4()) {
                        sqlEx = (SQLException) Util.getInstance("com.mysql.jdbc.exceptions.jdbc4.MySQLTransactionRollbackException", new Class[]{String.class, String.class, Integer.TYPE}, new Object[]{message, sqlState, Integer.valueOf(vendorErrorCode)}, interceptor);
                    } else {
                        sqlEx = new MySQLTransactionRollbackException(message, sqlState, vendorErrorCode);
                    }
                } else if (!sqlState.startsWith(SQL_STATE_ER_QUERY_INTERRUPTED)) {
                    sqlEx = new SQLException(message, sqlState, vendorErrorCode);
                } else if (Util.isJdbc4()) {
                    sqlEx = (SQLException) Util.getInstance("com.mysql.jdbc.exceptions.jdbc4.MySQLQueryInterruptedException", new Class[]{String.class, String.class, Integer.TYPE}, new Object[]{message, sqlState, Integer.valueOf(vendorErrorCode)}, interceptor);
                } else {
                    sqlEx = new MySQLQueryInterruptedException(message, sqlState, vendorErrorCode);
                }
            } catch (SQLException sqlEx2) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unable to create correct SQLException class instance, error class/codes may be incorrect. Reason: ");
                stringBuilder.append(Util.stackTraceToString(sqlEx2));
                return runThroughExceptionInterceptor(interceptor, new SQLException(stringBuilder.toString(), SQL_STATE_GENERAL_ERROR), conn);
            }
        }
        sqlEx2 = new SQLException(message, sqlState, vendorErrorCode);
        return runThroughExceptionInterceptor(interceptor, sqlEx2, conn);
    }

    public static SQLException createCommunicationsException(MySQLConnection conn, long lastPacketSentTimeMs, long lastPacketReceivedTimeMs, Exception underlyingException, ExceptionInterceptor interceptor) {
        SQLException exToReturn;
        if (Util.isJdbc4()) {
            try {
                exToReturn = (SQLException) Util.handleNewInstance(JDBC_4_COMMUNICATIONS_EXCEPTION_CTOR, new Object[]{conn, Long.valueOf(lastPacketSentTimeMs), Long.valueOf(lastPacketReceivedTimeMs), underlyingException}, interceptor);
            } catch (SQLException sqlEx) {
                return sqlEx;
            }
        }
        exToReturn = new CommunicationsException(conn, lastPacketSentTimeMs, lastPacketReceivedTimeMs, underlyingException);
        return runThroughExceptionInterceptor(interceptor, exToReturn, conn);
    }

    public static String createLinkFailureMessageBasedOnHeuristics(MySQLConnection conn, long lastPacketSentTimeMs, long lastPacketReceivedTimeMs, Exception underlyingException) {
        long lastPacketSentTimeMs2;
        int dueToTimeout;
        int dueToTimeout2;
        StringBuilder timeoutMessageBuf;
        Exception exception;
        int i;
        Object[] objArr;
        long j;
        MySQLConnection mySQLConnection = conn;
        long serverTimeoutSeconds = 0;
        boolean isInteractiveClient = false;
        if (mySQLConnection != null) {
            String serverTimeoutSecondsStr;
            isInteractiveClient = conn.getInteractiveClient();
            if (isInteractiveClient) {
                serverTimeoutSecondsStr = mySQLConnection.getServerVariable("interactive_timeout");
            } else {
                serverTimeoutSecondsStr = mySQLConnection.getServerVariable("wait_timeout");
            }
            if (serverTimeoutSecondsStr != null) {
                try {
                    serverTimeoutSeconds = Long.parseLong(serverTimeoutSecondsStr);
                } catch (NumberFormatException e) {
                    NumberFormatException nfe = e;
                    serverTimeoutSeconds = 0;
                }
            }
        }
        StringBuilder exceptionMessageBuf = new StringBuilder();
        long nowMs = System.currentTimeMillis();
        if (lastPacketSentTimeMs == 0) {
            lastPacketSentTimeMs2 = nowMs;
        } else {
            lastPacketSentTimeMs2 = lastPacketSentTimeMs;
        }
        long timeSinceLastPacketSentMs = nowMs - lastPacketSentTimeMs2;
        long timeSinceLastPacketSeconds = timeSinceLastPacketSentMs / 1000;
        long timeSinceLastPacketReceivedMs = nowMs - lastPacketReceivedTimeMs;
        if (serverTimeoutSeconds == 0) {
            if (timeSinceLastPacketSeconds > DEFAULT_WAIT_TIMEOUT_SECONDS) {
                dueToTimeout = 2;
                dueToTimeout2 = new StringBuilder();
                dueToTimeout2.append(Messages.getString("CommunicationsException.5"));
                dueToTimeout2.append(Messages.getString("CommunicationsException.6"));
                dueToTimeout2.append(Messages.getString("CommunicationsException.7"));
                dueToTimeout2.append(Messages.getString("CommunicationsException.8"));
            }
            dueToTimeout2 = 0;
            timeoutMessageBuf = null;
            if (dueToTimeout2 != 1) {
                exception = underlyingException;
                i = dueToTimeout2;
            } else if (dueToTimeout2 == 2) {
                if ((underlyingException instanceof BindException) != 0) {
                    if (conn.getLocalSocketAddress() != null) {
                    }
                    exceptionMessageBuf.append(Messages.getString("CommunicationsException.TooManyClientConnections"));
                }
                if (exceptionMessageBuf.length() == 0) {
                    exceptionMessageBuf.append(Messages.getString("CommunicationsException.20"));
                    exceptionMessageBuf.append("\n\n");
                    if (lastPacketReceivedTimeMs == 0) {
                        exceptionMessageBuf.append(Messages.getString("CommunicationsException.ServerPacketTimingInfo", new Object[]{Long.valueOf(timeSinceLastPacketReceivedMs), Long.valueOf(timeSinceLastPacketSentMs)}));
                    } else {
                        exceptionMessageBuf.append(Messages.getString("CommunicationsException.ServerPacketTimingInfoNoRecv", new Object[]{Long.valueOf(timeSinceLastPacketSentMs)}));
                    }
                }
                return exceptionMessageBuf.toString();
            } else {
                exception = underlyingException;
                i = dueToTimeout2;
            }
            if (lastPacketReceivedTimeMs != 0) {
                exceptionMessageBuf.append(Messages.getString("CommunicationsException.ServerPacketTimingInfoNoRecv", new Object[]{Long.valueOf(timeSinceLastPacketSentMs)}));
            } else {
                objArr = new Object[2];
                objArr[0] = Long.valueOf(timeSinceLastPacketReceivedMs);
                j = nowMs;
                objArr[1] = Long.valueOf(timeSinceLastPacketSentMs);
                exceptionMessageBuf.append(Messages.getString("CommunicationsException.ServerPacketTimingInfo", objArr));
            }
            if (timeoutMessageBuf != null) {
                exceptionMessageBuf.append(timeoutMessageBuf);
            }
            exceptionMessageBuf.append(Messages.getString("CommunicationsException.11"));
            exceptionMessageBuf.append(Messages.getString("CommunicationsException.12"));
            exceptionMessageBuf.append(Messages.getString("CommunicationsException.13"));
            if (exceptionMessageBuf.length() == 0) {
                exceptionMessageBuf.append(Messages.getString("CommunicationsException.20"));
                exceptionMessageBuf.append("\n\n");
                if (lastPacketReceivedTimeMs == 0) {
                    exceptionMessageBuf.append(Messages.getString("CommunicationsException.ServerPacketTimingInfoNoRecv", new Object[]{Long.valueOf(timeSinceLastPacketSentMs)}));
                } else {
                    exceptionMessageBuf.append(Messages.getString("CommunicationsException.ServerPacketTimingInfo", new Object[]{Long.valueOf(timeSinceLastPacketReceivedMs), Long.valueOf(timeSinceLastPacketSentMs)}));
                }
            }
            return exceptionMessageBuf.toString();
        } else if (timeSinceLastPacketSeconds > serverTimeoutSeconds) {
            dueToTimeout = 1;
            dueToTimeout2 = new StringBuilder();
            dueToTimeout2.append(Messages.getString("CommunicationsException.2"));
            if (isInteractiveClient) {
                dueToTimeout2.append(Messages.getString("CommunicationsException.4"));
            } else {
                dueToTimeout2.append(Messages.getString("CommunicationsException.3"));
            }
        } else {
            dueToTimeout2 = 0;
            timeoutMessageBuf = null;
            if (dueToTimeout2 != 1) {
                exception = underlyingException;
                i = dueToTimeout2;
            } else if (dueToTimeout2 == 2) {
                exception = underlyingException;
                i = dueToTimeout2;
            } else {
                if ((underlyingException instanceof BindException) != 0) {
                    if (conn.getLocalSocketAddress() != null || Util.interfaceExists(conn.getLocalSocketAddress())) {
                        exceptionMessageBuf.append(Messages.getString("CommunicationsException.TooManyClientConnections"));
                    } else {
                        exceptionMessageBuf.append(Messages.getString("CommunicationsException.LocalSocketAddressNotAvailable"));
                    }
                }
                if (exceptionMessageBuf.length() == 0) {
                    exceptionMessageBuf.append(Messages.getString("CommunicationsException.20"));
                    if (!(mySQLConnection == null || !conn.getMaintainTimeStats() || conn.getParanoid())) {
                        exceptionMessageBuf.append("\n\n");
                        if (lastPacketReceivedTimeMs == 0) {
                            exceptionMessageBuf.append(Messages.getString("CommunicationsException.ServerPacketTimingInfo", new Object[]{Long.valueOf(timeSinceLastPacketReceivedMs), Long.valueOf(timeSinceLastPacketSentMs)}));
                        } else {
                            exceptionMessageBuf.append(Messages.getString("CommunicationsException.ServerPacketTimingInfoNoRecv", new Object[]{Long.valueOf(timeSinceLastPacketSentMs)}));
                        }
                    }
                }
                return exceptionMessageBuf.toString();
            }
            if (lastPacketReceivedTimeMs != 0) {
                objArr = new Object[2];
                objArr[0] = Long.valueOf(timeSinceLastPacketReceivedMs);
                j = nowMs;
                objArr[1] = Long.valueOf(timeSinceLastPacketSentMs);
                exceptionMessageBuf.append(Messages.getString("CommunicationsException.ServerPacketTimingInfo", objArr));
            } else {
                exceptionMessageBuf.append(Messages.getString("CommunicationsException.ServerPacketTimingInfoNoRecv", new Object[]{Long.valueOf(timeSinceLastPacketSentMs)}));
            }
            if (timeoutMessageBuf != null) {
                exceptionMessageBuf.append(timeoutMessageBuf);
            }
            exceptionMessageBuf.append(Messages.getString("CommunicationsException.11"));
            exceptionMessageBuf.append(Messages.getString("CommunicationsException.12"));
            exceptionMessageBuf.append(Messages.getString("CommunicationsException.13"));
            if (exceptionMessageBuf.length() == 0) {
                exceptionMessageBuf.append(Messages.getString("CommunicationsException.20"));
                exceptionMessageBuf.append("\n\n");
                if (lastPacketReceivedTimeMs == 0) {
                    exceptionMessageBuf.append(Messages.getString("CommunicationsException.ServerPacketTimingInfoNoRecv", new Object[]{Long.valueOf(timeSinceLastPacketSentMs)}));
                } else {
                    exceptionMessageBuf.append(Messages.getString("CommunicationsException.ServerPacketTimingInfo", new Object[]{Long.valueOf(timeSinceLastPacketReceivedMs), Long.valueOf(timeSinceLastPacketSentMs)}));
                }
            }
            return exceptionMessageBuf.toString();
        }
        timeoutMessageBuf = dueToTimeout2;
        dueToTimeout2 = dueToTimeout;
        if (dueToTimeout2 != 1) {
            exception = underlyingException;
            i = dueToTimeout2;
        } else if (dueToTimeout2 == 2) {
            exception = underlyingException;
            i = dueToTimeout2;
        } else {
            if ((underlyingException instanceof BindException) != 0) {
                if (conn.getLocalSocketAddress() != null) {
                }
                exceptionMessageBuf.append(Messages.getString("CommunicationsException.TooManyClientConnections"));
            }
            if (exceptionMessageBuf.length() == 0) {
                exceptionMessageBuf.append(Messages.getString("CommunicationsException.20"));
                exceptionMessageBuf.append("\n\n");
                if (lastPacketReceivedTimeMs == 0) {
                    exceptionMessageBuf.append(Messages.getString("CommunicationsException.ServerPacketTimingInfo", new Object[]{Long.valueOf(timeSinceLastPacketReceivedMs), Long.valueOf(timeSinceLastPacketSentMs)}));
                } else {
                    exceptionMessageBuf.append(Messages.getString("CommunicationsException.ServerPacketTimingInfoNoRecv", new Object[]{Long.valueOf(timeSinceLastPacketSentMs)}));
                }
            }
            return exceptionMessageBuf.toString();
        }
        if (lastPacketReceivedTimeMs != 0) {
            objArr = new Object[2];
            objArr[0] = Long.valueOf(timeSinceLastPacketReceivedMs);
            j = nowMs;
            objArr[1] = Long.valueOf(timeSinceLastPacketSentMs);
            exceptionMessageBuf.append(Messages.getString("CommunicationsException.ServerPacketTimingInfo", objArr));
        } else {
            exceptionMessageBuf.append(Messages.getString("CommunicationsException.ServerPacketTimingInfoNoRecv", new Object[]{Long.valueOf(timeSinceLastPacketSentMs)}));
        }
        if (timeoutMessageBuf != null) {
            exceptionMessageBuf.append(timeoutMessageBuf);
        }
        exceptionMessageBuf.append(Messages.getString("CommunicationsException.11"));
        exceptionMessageBuf.append(Messages.getString("CommunicationsException.12"));
        exceptionMessageBuf.append(Messages.getString("CommunicationsException.13"));
        if (exceptionMessageBuf.length() == 0) {
            exceptionMessageBuf.append(Messages.getString("CommunicationsException.20"));
            exceptionMessageBuf.append("\n\n");
            if (lastPacketReceivedTimeMs == 0) {
                exceptionMessageBuf.append(Messages.getString("CommunicationsException.ServerPacketTimingInfoNoRecv", new Object[]{Long.valueOf(timeSinceLastPacketSentMs)}));
            } else {
                exceptionMessageBuf.append(Messages.getString("CommunicationsException.ServerPacketTimingInfo", new Object[]{Long.valueOf(timeSinceLastPacketReceivedMs), Long.valueOf(timeSinceLastPacketSentMs)}));
            }
        }
        return exceptionMessageBuf.toString();
    }

    private static SQLException runThroughExceptionInterceptor(ExceptionInterceptor exInterceptor, SQLException sqlEx, Connection conn) {
        if (exInterceptor != null) {
            SQLException interceptedEx = exInterceptor.interceptException(sqlEx, conn);
            if (interceptedEx != null) {
                return interceptedEx;
            }
        }
        return sqlEx;
    }

    public static SQLException createBatchUpdateException(SQLException underlyingEx, long[] updateCounts, ExceptionInterceptor interceptor) throws SQLException {
        SQLException newEx;
        if (Util.isJdbc42()) {
            newEx = (SQLException) Util.getInstance("java.sql.BatchUpdateException", new Class[]{String.class, String.class, Integer.TYPE, long[].class, Throwable.class}, new Object[]{underlyingEx.getMessage(), underlyingEx.getSQLState(), Integer.valueOf(underlyingEx.getErrorCode()), updateCounts, underlyingEx}, interceptor);
        } else {
            newEx = new BatchUpdateException(underlyingEx.getMessage(), underlyingEx.getSQLState(), underlyingEx.getErrorCode(), Util.truncateAndConvertToInt(updateCounts));
            newEx.initCause(underlyingEx);
        }
        return runThroughExceptionInterceptor(interceptor, newEx, null);
    }

    public static SQLException createSQLFeatureNotSupportedException() throws SQLException {
        if (Util.isJdbc4()) {
            return (SQLException) Util.getInstance("java.sql.SQLFeatureNotSupportedException", null, null, null);
        }
        return new NotImplemented();
    }

    public static SQLException createSQLFeatureNotSupportedException(String message, String sqlState, ExceptionInterceptor interceptor) throws SQLException {
        SQLException newEx;
        if (Util.isJdbc4()) {
            newEx = (SQLException) Util.getInstance("java.sql.SQLFeatureNotSupportedException", new Class[]{String.class, String.class}, new Object[]{message, sqlState}, interceptor);
        } else {
            newEx = new NotImplemented();
        }
        return runThroughExceptionInterceptor(interceptor, newEx, null);
    }
}
