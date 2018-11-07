package com.mysql.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DatabaseMetaDataUsingInfoSchema extends DatabaseMetaData {
    private final boolean hasParametersView;
    private boolean hasReferentialConstraintsView = this.conn.versionMeetsMinimum(5, 1, 10);

    protected enum JDBC4FunctionConstant {
        FUNCTION_COLUMN_UNKNOWN,
        FUNCTION_COLUMN_IN,
        FUNCTION_COLUMN_INOUT,
        FUNCTION_COLUMN_OUT,
        FUNCTION_COLUMN_RETURN,
        FUNCTION_COLUMN_RESULT,
        FUNCTION_NO_NULLS,
        FUNCTION_NULLABLE,
        FUNCTION_NULLABLE_UNKNOWN
    }

    protected DatabaseMetaDataUsingInfoSchema(MySQLConnection connToSet, String databaseToSet) throws SQLException {
        super(connToSet, databaseToSet);
        ResultSet rs = null;
        try {
            rs = super.getTables("INFORMATION_SCHEMA", null, "PARAMETERS", new String[0]);
            this.hasParametersView = rs.next();
            if (rs != null) {
                rs.close();
            }
        } catch (Throwable th) {
            if (rs != null) {
                rs.close();
            }
        }
    }

    protected ResultSet executeMetadataQuery(PreparedStatement pStmt) throws SQLException {
        ResultSet rs = pStmt.executeQuery();
        ((ResultSetInternalMethods) rs).setOwningStatement(null);
        return rs;
    }

    public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) throws SQLException {
        String columnNamePattern2;
        String catalog2;
        Throwable th;
        PreparedStatement pStmt;
        Throwable th2;
        DatabaseMetaDataUsingInfoSchema this;
        DatabaseMetaDataUsingInfoSchema databaseMetaDataUsingInfoSchema = this;
        if (columnNamePattern != null) {
            columnNamePattern2 = columnNamePattern;
        } else if (databaseMetaDataUsingInfoSchema.conn.getNullNamePatternMatchesAll()) {
            columnNamePattern2 = "%";
        } else {
            throw SQLError.createSQLException("Column name pattern can not be NULL or empty.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
        if (catalog == null && databaseMetaDataUsingInfoSchema.conn.getNullCatalogMeansCurrent()) {
            catalog2 = databaseMetaDataUsingInfoSchema.database;
        } else {
            catalog2 = catalog;
        }
        PreparedStatement pStmt2 = null;
        try {
            pStmt2 = prepareMetaDataSafeStatement("SELECT TABLE_SCHEMA AS TABLE_CAT, NULL AS TABLE_SCHEM, TABLE_NAME,COLUMN_NAME, NULL AS GRANTOR, GRANTEE, PRIVILEGE_TYPE AS PRIVILEGE, IS_GRANTABLE FROM INFORMATION_SCHEMA.COLUMN_PRIVILEGES WHERE TABLE_SCHEMA LIKE ? AND TABLE_NAME =? AND COLUMN_NAME LIKE ? ORDER BY COLUMN_NAME, PRIVILEGE_TYPE");
            if (catalog2 != null) {
                pStmt2.setString(1, catalog2);
            } else {
                pStmt2.setString(1, "%");
            }
            try {
                pStmt2.setString(2, table);
                pStmt2.setString(3, columnNamePattern2);
                ResultSet rs = executeMetadataQuery(pStmt2);
                ((ResultSetInternalMethods) rs).redefineFieldsForDBMD(new Field[]{new Field("", "TABLE_CAT", 1, 64), new Field("", "TABLE_SCHEM", 1, 1), new Field("", "TABLE_NAME", 1, 64), new Field("", "COLUMN_NAME", 1, 64), new Field("", "GRANTOR", 1, 77), new Field("", "GRANTEE", 1, 77), new Field("", "PRIVILEGE", 1, 64), new Field("", "IS_GRANTABLE", 1, 3)});
                if (pStmt2 != null) {
                    pStmt2.close();
                }
                return rs;
            } catch (Throwable th3) {
                th = th3;
                pStmt = pStmt2;
                th2 = th;
                this = databaseMetaDataUsingInfoSchema;
                if (pStmt != null) {
                    pStmt.close();
                }
                throw th2;
            }
        } catch (Throwable th4) {
            th = th4;
            pStmt = pStmt2;
            th2 = th;
            this = databaseMetaDataUsingInfoSchema;
            if (pStmt != null) {
                pStmt.close();
            }
            throw th2;
        }
    }

    public ResultSet getColumns(String catalog, String schemaPattern, String tableName, String columnNamePattern) throws SQLException {
        if (columnNamePattern == null) {
            if (this.conn.getNullNamePatternMatchesAll()) {
                columnNamePattern = "%";
            } else {
                throw SQLError.createSQLException("Column name pattern can not be NULL or empty.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            }
        }
        if (catalog == null && this.conn.getNullCatalogMeansCurrent()) {
            catalog = this.database;
        }
        StringBuilder sqlBuf = new StringBuilder("SELECT TABLE_SCHEMA AS TABLE_CAT, NULL AS TABLE_SCHEM, TABLE_NAME, COLUMN_NAME,");
        MysqlDefs.appendJdbcTypeMappingQuery(sqlBuf, "DATA_TYPE");
        sqlBuf.append(" AS DATA_TYPE, ");
        if (this.conn.getCapitalizeTypeNames()) {
            sqlBuf.append("UPPER(CASE WHEN LOCATE('unsigned', COLUMN_TYPE) != 0 AND LOCATE('unsigned', DATA_TYPE) = 0 AND LOCATE('set', DATA_TYPE) <> 1 AND LOCATE('enum', DATA_TYPE) <> 1 THEN CONCAT(DATA_TYPE, ' unsigned') ELSE DATA_TYPE END) AS TYPE_NAME,");
        } else {
            sqlBuf.append("CASE WHEN LOCATE('unsigned', COLUMN_TYPE) != 0 AND LOCATE('unsigned', DATA_TYPE) = 0 AND LOCATE('set', DATA_TYPE) <> 1 AND LOCATE('enum', DATA_TYPE) <> 1 THEN CONCAT(DATA_TYPE, ' unsigned') ELSE DATA_TYPE END AS TYPE_NAME,");
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("CASE WHEN LCASE(DATA_TYPE)='date' THEN 10 WHEN LCASE(DATA_TYPE)='time' THEN 8 WHEN LCASE(DATA_TYPE)='datetime' THEN 19 WHEN LCASE(DATA_TYPE)='timestamp' THEN 19 WHEN CHARACTER_MAXIMUM_LENGTH IS NULL THEN NUMERIC_PRECISION WHEN CHARACTER_MAXIMUM_LENGTH > 2147483647 THEN 2147483647 ELSE CHARACTER_MAXIMUM_LENGTH END AS COLUMN_SIZE, ");
        stringBuilder.append(MysqlIO.getMaxBuf());
        stringBuilder.append(" AS BUFFER_LENGTH,");
        stringBuilder.append("NUMERIC_SCALE AS DECIMAL_DIGITS,");
        stringBuilder.append("10 AS NUM_PREC_RADIX,");
        stringBuilder.append("CASE WHEN IS_NULLABLE='NO' THEN ");
        stringBuilder.append(0);
        stringBuilder.append(" ELSE CASE WHEN IS_NULLABLE='YES' THEN ");
        stringBuilder.append(1);
        stringBuilder.append(" ELSE ");
        stringBuilder.append(2);
        stringBuilder.append(" END END AS NULLABLE,");
        stringBuilder.append("COLUMN_COMMENT AS REMARKS,");
        stringBuilder.append("COLUMN_DEFAULT AS COLUMN_DEF,");
        stringBuilder.append("0 AS SQL_DATA_TYPE,");
        stringBuilder.append("0 AS SQL_DATETIME_SUB,");
        stringBuilder.append("CASE WHEN CHARACTER_OCTET_LENGTH > ");
        stringBuilder.append(ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED);
        stringBuilder.append(" THEN ");
        stringBuilder.append(ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED);
        stringBuilder.append(" ELSE CHARACTER_OCTET_LENGTH END AS CHAR_OCTET_LENGTH,");
        stringBuilder.append("ORDINAL_POSITION,");
        stringBuilder.append("IS_NULLABLE,");
        stringBuilder.append("NULL AS SCOPE_CATALOG,");
        stringBuilder.append("NULL AS SCOPE_SCHEMA,");
        stringBuilder.append("NULL AS SCOPE_TABLE,");
        stringBuilder.append("NULL AS SOURCE_DATA_TYPE,");
        stringBuilder.append("IF (EXTRA LIKE '%auto_increment%','YES','NO') AS IS_AUTOINCREMENT, ");
        stringBuilder.append("IF (EXTRA LIKE '%GENERATED%','YES','NO') AS IS_GENERATEDCOLUMN FROM INFORMATION_SCHEMA.COLUMNS WHERE ");
        sqlBuf.append(stringBuilder.toString());
        boolean operatingOnInformationSchema = "information_schema".equalsIgnoreCase(catalog);
        if (catalog != null) {
            if (!operatingOnInformationSchema) {
                if (StringUtils.indexOfIgnoreCase(0, catalog, "%") != -1 || StringUtils.indexOfIgnoreCase(0, catalog, "_") != -1) {
                    sqlBuf.append("TABLE_SCHEMA LIKE ? AND ");
                }
            }
            sqlBuf.append("TABLE_SCHEMA = ? AND ");
        } else {
            sqlBuf.append("TABLE_SCHEMA LIKE ? AND ");
        }
        if (tableName == null) {
            sqlBuf.append("TABLE_NAME LIKE ? AND ");
        } else if (StringUtils.indexOfIgnoreCase(0, tableName, "%") == -1 && StringUtils.indexOfIgnoreCase(0, tableName, "_") == -1) {
            sqlBuf.append("TABLE_NAME = ? AND ");
        } else {
            sqlBuf.append("TABLE_NAME LIKE ? AND ");
        }
        if (StringUtils.indexOfIgnoreCase(0, columnNamePattern, "%") == -1 && StringUtils.indexOfIgnoreCase(0, columnNamePattern, "_") == -1) {
            sqlBuf.append("COLUMN_NAME = ? ");
        } else {
            sqlBuf.append("COLUMN_NAME LIKE ? ");
        }
        sqlBuf.append("ORDER BY TABLE_SCHEMA, TABLE_NAME, ORDINAL_POSITION");
        PreparedStatement pStmt = null;
        try {
            pStmt = prepareMetaDataSafeStatement(sqlBuf.toString());
            if (catalog != null) {
                pStmt.setString(1, catalog);
            } else {
                pStmt.setString(1, "%");
            }
            pStmt.setString(2, tableName);
            pStmt.setString(3, columnNamePattern);
            ResultSet rs = executeMetadataQuery(pStmt);
            ((ResultSetInternalMethods) rs).redefineFieldsForDBMD(createColumnsFields());
            if (pStmt != null) {
                pStmt.close();
            }
            return rs;
        } catch (Throwable th) {
            if (pStmt != null) {
                pStmt.close();
            }
        }
    }

    public ResultSet getCrossReference(String primaryCatalog, String primarySchema, String primaryTable, String foreignCatalog, String foreignSchema, String foreignTable) throws SQLException {
        if (primaryTable == null) {
            throw SQLError.createSQLException("Table not specified.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
        if (primaryCatalog == null && this.conn.getNullCatalogMeansCurrent()) {
            primaryCatalog = this.database;
        }
        if (foreignCatalog == null && this.conn.getNullCatalogMeansCurrent()) {
            foreignCatalog = this.database;
        }
        String sql = new StringBuilder();
        sql.append("SELECT A.REFERENCED_TABLE_SCHEMA AS PKTABLE_CAT,NULL AS PKTABLE_SCHEM, A.REFERENCED_TABLE_NAME AS PKTABLE_NAME,A.REFERENCED_COLUMN_NAME AS PKCOLUMN_NAME, A.TABLE_SCHEMA AS FKTABLE_CAT, NULL AS FKTABLE_SCHEM, A.TABLE_NAME AS FKTABLE_NAME, A.COLUMN_NAME AS FKCOLUMN_NAME, A.ORDINAL_POSITION AS KEY_SEQ,");
        sql.append(generateUpdateRuleClause());
        sql.append(" AS UPDATE_RULE,");
        sql.append(generateDeleteRuleClause());
        sql.append(" AS DELETE_RULE,");
        sql.append("A.CONSTRAINT_NAME AS FK_NAME,");
        sql.append("(SELECT CONSTRAINT_NAME FROM");
        sql.append(" INFORMATION_SCHEMA.TABLE_CONSTRAINTS");
        sql.append(" WHERE TABLE_SCHEMA = A.REFERENCED_TABLE_SCHEMA AND");
        sql.append(" TABLE_NAME = A.REFERENCED_TABLE_NAME AND");
        sql.append(" CONSTRAINT_TYPE IN ('UNIQUE','PRIMARY KEY') LIMIT 1)");
        sql.append(" AS PK_NAME,");
        sql.append(7);
        sql.append(" AS DEFERRABILITY ");
        sql.append("FROM ");
        sql.append("INFORMATION_SCHEMA.KEY_COLUMN_USAGE A JOIN ");
        sql.append("INFORMATION_SCHEMA.TABLE_CONSTRAINTS B ");
        sql.append("USING (TABLE_SCHEMA, TABLE_NAME, CONSTRAINT_NAME) ");
        sql.append(generateOptionalRefContraintsJoin());
        sql.append("WHERE ");
        sql.append("B.CONSTRAINT_TYPE = 'FOREIGN KEY' ");
        sql.append("AND A.REFERENCED_TABLE_SCHEMA LIKE ? AND A.REFERENCED_TABLE_NAME=? ");
        sql.append("AND A.TABLE_SCHEMA LIKE ? AND A.TABLE_NAME=? ORDER BY A.TABLE_SCHEMA, A.TABLE_NAME, A.ORDINAL_POSITION");
        PreparedStatement pStmt = null;
        try {
            pStmt = prepareMetaDataSafeStatement(sql.toString());
            if (primaryCatalog != null) {
                pStmt.setString(1, primaryCatalog);
            } else {
                pStmt.setString(1, "%");
            }
            pStmt.setString(2, primaryTable);
            if (foreignCatalog != null) {
                pStmt.setString(3, foreignCatalog);
            } else {
                pStmt.setString(3, "%");
            }
            pStmt.setString(4, foreignTable);
            ResultSet rs = executeMetadataQuery(pStmt);
            ((ResultSetInternalMethods) rs).redefineFieldsForDBMD(createFkMetadataFields());
            if (pStmt != null) {
                pStmt.close();
            }
            return rs;
        } catch (Throwable th) {
            if (pStmt != null) {
                pStmt.close();
            }
        }
    }

    public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
        if (table == null) {
            throw SQLError.createSQLException("Table not specified.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
        if (catalog == null && this.conn.getNullCatalogMeansCurrent()) {
            catalog = this.database;
        }
        String sql = new StringBuilder();
        sql.append("SELECT A.REFERENCED_TABLE_SCHEMA AS PKTABLE_CAT, NULL AS PKTABLE_SCHEM, A.REFERENCED_TABLE_NAME AS PKTABLE_NAME, A.REFERENCED_COLUMN_NAME AS PKCOLUMN_NAME, A.TABLE_SCHEMA AS FKTABLE_CAT, NULL AS FKTABLE_SCHEM, A.TABLE_NAME AS FKTABLE_NAME,A.COLUMN_NAME AS FKCOLUMN_NAME, A.ORDINAL_POSITION AS KEY_SEQ,");
        sql.append(generateUpdateRuleClause());
        sql.append(" AS UPDATE_RULE,");
        sql.append(generateDeleteRuleClause());
        sql.append(" AS DELETE_RULE,");
        sql.append("A.CONSTRAINT_NAME AS FK_NAME,");
        sql.append("(SELECT CONSTRAINT_NAME FROM");
        sql.append(" INFORMATION_SCHEMA.TABLE_CONSTRAINTS");
        sql.append(" WHERE TABLE_SCHEMA = A.REFERENCED_TABLE_SCHEMA AND");
        sql.append(" TABLE_NAME = A.REFERENCED_TABLE_NAME AND");
        sql.append(" CONSTRAINT_TYPE IN ('UNIQUE','PRIMARY KEY') LIMIT 1)");
        sql.append(" AS PK_NAME,");
        sql.append(7);
        sql.append(" AS DEFERRABILITY ");
        sql.append("FROM ");
        sql.append("INFORMATION_SCHEMA.KEY_COLUMN_USAGE A JOIN ");
        sql.append("INFORMATION_SCHEMA.TABLE_CONSTRAINTS B ");
        sql.append("USING (TABLE_SCHEMA, TABLE_NAME, CONSTRAINT_NAME) ");
        sql.append(generateOptionalRefContraintsJoin());
        sql.append("WHERE ");
        sql.append("B.CONSTRAINT_TYPE = 'FOREIGN KEY' ");
        sql.append("AND A.REFERENCED_TABLE_SCHEMA LIKE ? AND A.REFERENCED_TABLE_NAME=? ");
        sql.append("ORDER BY A.TABLE_SCHEMA, A.TABLE_NAME, A.ORDINAL_POSITION");
        PreparedStatement pStmt = null;
        try {
            pStmt = prepareMetaDataSafeStatement(sql.toString());
            if (catalog != null) {
                pStmt.setString(1, catalog);
            } else {
                pStmt.setString(1, "%");
            }
            pStmt.setString(2, table);
            ResultSet rs = executeMetadataQuery(pStmt);
            ((ResultSetInternalMethods) rs).redefineFieldsForDBMD(createFkMetadataFields());
            if (pStmt != null) {
                pStmt.close();
            }
            return rs;
        } catch (Throwable th) {
            if (pStmt != null) {
                pStmt.close();
            }
        }
    }

    private String generateOptionalRefContraintsJoin() {
        return this.hasReferentialConstraintsView ? "JOIN INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS R ON (R.CONSTRAINT_NAME = B.CONSTRAINT_NAME AND R.TABLE_NAME = B.TABLE_NAME AND R.CONSTRAINT_SCHEMA = B.TABLE_SCHEMA) " : "";
    }

    private String generateDeleteRuleClause() {
        if (!this.hasReferentialConstraintsView) {
            return String.valueOf(1);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("CASE WHEN R.DELETE_RULE='CASCADE' THEN ");
        stringBuilder.append(String.valueOf(0));
        stringBuilder.append(" WHEN R.DELETE_RULE='SET NULL' THEN ");
        stringBuilder.append(String.valueOf(2));
        stringBuilder.append(" WHEN R.DELETE_RULE='SET DEFAULT' THEN ");
        stringBuilder.append(String.valueOf(4));
        stringBuilder.append(" WHEN R.DELETE_RULE='RESTRICT' THEN ");
        stringBuilder.append(String.valueOf(1));
        stringBuilder.append(" WHEN R.DELETE_RULE='NO ACTION' THEN ");
        stringBuilder.append(String.valueOf(3));
        stringBuilder.append(" ELSE ");
        stringBuilder.append(String.valueOf(3));
        stringBuilder.append(" END ");
        return stringBuilder.toString();
    }

    private String generateUpdateRuleClause() {
        if (!this.hasReferentialConstraintsView) {
            return String.valueOf(1);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("CASE WHEN R.UPDATE_RULE='CASCADE' THEN ");
        stringBuilder.append(String.valueOf(0));
        stringBuilder.append(" WHEN R.UPDATE_RULE='SET NULL' THEN ");
        stringBuilder.append(String.valueOf(2));
        stringBuilder.append(" WHEN R.UPDATE_RULE='SET DEFAULT' THEN ");
        stringBuilder.append(String.valueOf(4));
        stringBuilder.append(" WHEN R.UPDATE_RULE='RESTRICT' THEN ");
        stringBuilder.append(String.valueOf(1));
        stringBuilder.append(" WHEN R.UPDATE_RULE='NO ACTION' THEN ");
        stringBuilder.append(String.valueOf(3));
        stringBuilder.append(" ELSE ");
        stringBuilder.append(String.valueOf(3));
        stringBuilder.append(" END ");
        return stringBuilder.toString();
    }

    public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException {
        if (table == null) {
            throw SQLError.createSQLException("Table not specified.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
        if (catalog == null && this.conn.getNullCatalogMeansCurrent()) {
            catalog = this.database;
        }
        String sql = new StringBuilder();
        sql.append("SELECT A.REFERENCED_TABLE_SCHEMA AS PKTABLE_CAT, NULL AS PKTABLE_SCHEM, A.REFERENCED_TABLE_NAME AS PKTABLE_NAME,A.REFERENCED_COLUMN_NAME AS PKCOLUMN_NAME, A.TABLE_SCHEMA AS FKTABLE_CAT, NULL AS FKTABLE_SCHEM, A.TABLE_NAME AS FKTABLE_NAME, A.COLUMN_NAME AS FKCOLUMN_NAME, A.ORDINAL_POSITION AS KEY_SEQ,");
        sql.append(generateUpdateRuleClause());
        sql.append(" AS UPDATE_RULE,");
        sql.append(generateDeleteRuleClause());
        sql.append(" AS DELETE_RULE,");
        sql.append("A.CONSTRAINT_NAME AS FK_NAME,");
        sql.append("(SELECT CONSTRAINT_NAME FROM");
        sql.append(" INFORMATION_SCHEMA.TABLE_CONSTRAINTS");
        sql.append(" WHERE TABLE_SCHEMA = A.REFERENCED_TABLE_SCHEMA AND");
        sql.append(" TABLE_NAME = A.REFERENCED_TABLE_NAME AND");
        sql.append(" CONSTRAINT_TYPE IN ('UNIQUE','PRIMARY KEY') LIMIT 1)");
        sql.append(" AS PK_NAME,");
        sql.append(7);
        sql.append(" AS DEFERRABILITY ");
        sql.append("FROM ");
        sql.append("INFORMATION_SCHEMA.KEY_COLUMN_USAGE A ");
        sql.append("JOIN INFORMATION_SCHEMA.TABLE_CONSTRAINTS B USING ");
        sql.append("(CONSTRAINT_NAME, TABLE_NAME) ");
        sql.append(generateOptionalRefContraintsJoin());
        sql.append("WHERE ");
        sql.append("B.CONSTRAINT_TYPE = 'FOREIGN KEY' ");
        sql.append("AND A.TABLE_SCHEMA LIKE ? ");
        sql.append("AND A.TABLE_NAME=? ");
        sql.append("AND A.REFERENCED_TABLE_SCHEMA IS NOT NULL ");
        sql.append("ORDER BY A.REFERENCED_TABLE_SCHEMA, A.REFERENCED_TABLE_NAME, A.ORDINAL_POSITION");
        PreparedStatement pStmt = null;
        try {
            pStmt = prepareMetaDataSafeStatement(sql.toString());
            if (catalog != null) {
                pStmt.setString(1, catalog);
            } else {
                pStmt.setString(1, "%");
            }
            pStmt.setString(2, table);
            ResultSet rs = executeMetadataQuery(pStmt);
            ((ResultSetInternalMethods) rs).redefineFieldsForDBMD(createFkMetadataFields());
            if (pStmt != null) {
                pStmt.close();
            }
            return rs;
        } catch (Throwable th) {
            if (pStmt != null) {
                pStmt.close();
            }
        }
    }

    public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) throws SQLException {
        StringBuilder sqlBuf = new StringBuilder("SELECT TABLE_SCHEMA AS TABLE_CAT, NULL AS TABLE_SCHEM, TABLE_NAME, NON_UNIQUE,");
        sqlBuf.append("TABLE_SCHEMA AS INDEX_QUALIFIER, INDEX_NAME,3 AS TYPE, SEQ_IN_INDEX AS ORDINAL_POSITION, COLUMN_NAME,");
        sqlBuf.append("COLLATION AS ASC_OR_DESC, CARDINALITY, NULL AS PAGES, NULL AS FILTER_CONDITION FROM INFORMATION_SCHEMA.STATISTICS WHERE ");
        sqlBuf.append("TABLE_SCHEMA LIKE ? AND TABLE_NAME LIKE ?");
        if (unique) {
            sqlBuf.append(" AND NON_UNIQUE=0 ");
        }
        sqlBuf.append("ORDER BY NON_UNIQUE, INDEX_NAME, SEQ_IN_INDEX");
        PreparedStatement pStmt = null;
        if (catalog == null) {
            try {
                if (this.conn.getNullCatalogMeansCurrent()) {
                    catalog = this.database;
                }
            } catch (Throwable th) {
                if (pStmt != null) {
                    pStmt.close();
                }
            }
        }
        pStmt = prepareMetaDataSafeStatement(sqlBuf.toString());
        if (catalog != null) {
            pStmt.setString(1, catalog);
        } else {
            pStmt.setString(1, "%");
        }
        pStmt.setString(2, table);
        ResultSet rs = executeMetadataQuery(pStmt);
        ((ResultSetInternalMethods) rs).redefineFieldsForDBMD(createIndexInfoFields());
        if (pStmt != null) {
            pStmt.close();
        }
        return rs;
    }

    public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException {
        if (catalog == null && this.conn.getNullCatalogMeansCurrent()) {
            catalog = this.database;
        }
        if (table == null) {
            throw SQLError.createSQLException("Table not specified.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
        PreparedStatement pStmt = null;
        try {
            pStmt = prepareMetaDataSafeStatement("SELECT TABLE_SCHEMA AS TABLE_CAT, NULL AS TABLE_SCHEM, TABLE_NAME, COLUMN_NAME, SEQ_IN_INDEX AS KEY_SEQ, 'PRIMARY' AS PK_NAME FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA LIKE ? AND TABLE_NAME LIKE ? AND INDEX_NAME='PRIMARY' ORDER BY TABLE_SCHEMA, TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX");
            if (catalog != null) {
                pStmt.setString(1, catalog);
            } else {
                pStmt.setString(1, "%");
            }
            pStmt.setString(2, table);
            ResultSet rs = executeMetadataQuery(pStmt);
            ((ResultSetInternalMethods) rs).redefineFieldsForDBMD(new Field[]{new Field("", "TABLE_CAT", 1, 255), new Field("", "TABLE_SCHEM", 1, 0), new Field("", "TABLE_NAME", 1, 255), new Field("", "COLUMN_NAME", 1, 32), new Field("", "KEY_SEQ", 5, 5), new Field("", "PK_NAME", 1, 32)});
            if (pStmt != null) {
                pStmt.close();
            }
            return rs;
        } catch (Throwable th) {
            if (pStmt != null) {
                pStmt.close();
            }
        }
    }

    public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) throws SQLException {
        if (procedureNamePattern == null || procedureNamePattern.length() == 0) {
            if (this.conn.getNullNamePatternMatchesAll()) {
                procedureNamePattern = "%";
            } else {
                throw SQLError.createSQLException("Procedure name pattern can not be NULL or empty.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            }
        }
        String db = null;
        if (catalog != null) {
            db = catalog;
        } else if (this.conn.getNullCatalogMeansCurrent()) {
            db = this.database;
        }
        String sql = new StringBuilder();
        sql.append("SELECT ROUTINE_SCHEMA AS PROCEDURE_CAT, NULL AS PROCEDURE_SCHEM, ROUTINE_NAME AS PROCEDURE_NAME, NULL AS RESERVED_1, NULL AS RESERVED_2, NULL AS RESERVED_3, ROUTINE_COMMENT AS REMARKS, CASE WHEN ROUTINE_TYPE = 'PROCEDURE' THEN 1 WHEN ROUTINE_TYPE='FUNCTION' THEN 2 ELSE 0 END AS PROCEDURE_TYPE, ROUTINE_NAME AS SPECIFIC_NAME FROM INFORMATION_SCHEMA.ROUTINES WHERE ");
        sql.append(getRoutineTypeConditionForGetProcedures());
        sql.append("ROUTINE_SCHEMA LIKE ? AND ROUTINE_NAME LIKE ? ORDER BY ROUTINE_SCHEMA, ROUTINE_NAME, ROUTINE_TYPE");
        PreparedStatement pStmt = null;
        try {
            pStmt = prepareMetaDataSafeStatement(sql.toString());
            if (db != null) {
                pStmt.setString(1, db);
            } else {
                pStmt.setString(1, "%");
            }
            pStmt.setString(2, procedureNamePattern);
            ResultSet rs = executeMetadataQuery(pStmt);
            ((ResultSetInternalMethods) rs).redefineFieldsForDBMD(createFieldMetadataForGetProcedures());
            if (pStmt != null) {
                pStmt.close();
            }
            return rs;
        } catch (Throwable th) {
            if (pStmt != null) {
                pStmt.close();
            }
        }
    }

    protected String getRoutineTypeConditionForGetProcedures() {
        return "";
    }

    public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern) throws SQLException {
        if (!this.hasParametersView) {
            return getProcedureColumnsNoISParametersView(catalog, schemaPattern, procedureNamePattern, columnNamePattern);
        }
        if (procedureNamePattern == null || procedureNamePattern.length() == 0) {
            if (this.conn.getNullNamePatternMatchesAll()) {
                procedureNamePattern = "%";
            } else {
                throw SQLError.createSQLException("Procedure name pattern can not be NULL or empty.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            }
        }
        String db = null;
        if (catalog != null) {
            db = catalog;
        } else if (this.conn.getNullCatalogMeansCurrent()) {
            db = this.database;
        }
        StringBuilder sqlBuf = new StringBuilder("SELECT SPECIFIC_SCHEMA AS PROCEDURE_CAT, NULL AS `PROCEDURE_SCHEM`, SPECIFIC_NAME AS `PROCEDURE_NAME`, IFNULL(PARAMETER_NAME, '') AS `COLUMN_NAME`, CASE WHEN PARAMETER_MODE = 'IN' THEN 1 WHEN PARAMETER_MODE = 'OUT' THEN 4 WHEN PARAMETER_MODE = 'INOUT' THEN 2 WHEN ORDINAL_POSITION = 0 THEN 5 ELSE 0 END AS `COLUMN_TYPE`, ");
        MysqlDefs.appendJdbcTypeMappingQuery(sqlBuf, "DATA_TYPE");
        sqlBuf.append(" AS `DATA_TYPE`, ");
        if (this.conn.getCapitalizeTypeNames()) {
            sqlBuf.append("UPPER(CASE WHEN LOCATE('unsigned', DATA_TYPE) != 0 AND LOCATE('unsigned', DATA_TYPE) = 0 THEN CONCAT(DATA_TYPE, ' unsigned') ELSE DATA_TYPE END) AS `TYPE_NAME`,");
        } else {
            sqlBuf.append("CASE WHEN LOCATE('unsigned', DATA_TYPE) != 0 AND LOCATE('unsigned', DATA_TYPE) = 0 THEN CONCAT(DATA_TYPE, ' unsigned') ELSE DATA_TYPE END AS `TYPE_NAME`,");
        }
        sqlBuf.append("NUMERIC_PRECISION AS `PRECISION`, ");
        sqlBuf.append("CASE WHEN LCASE(DATA_TYPE)='date' THEN 10 WHEN LCASE(DATA_TYPE)='time' THEN 8 WHEN LCASE(DATA_TYPE)='datetime' THEN 19 WHEN LCASE(DATA_TYPE)='timestamp' THEN 19 WHEN CHARACTER_MAXIMUM_LENGTH IS NULL THEN NUMERIC_PRECISION WHEN CHARACTER_MAXIMUM_LENGTH > 2147483647 THEN 2147483647 ELSE CHARACTER_MAXIMUM_LENGTH END AS LENGTH, ");
        sqlBuf.append("NUMERIC_SCALE AS `SCALE`, ");
        sqlBuf.append("10 AS RADIX,");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("1 AS `NULLABLE`, NULL AS `REMARKS`, NULL AS `COLUMN_DEF`, NULL AS `SQL_DATA_TYPE`, NULL AS `SQL_DATETIME_SUB`, CHARACTER_OCTET_LENGTH AS `CHAR_OCTET_LENGTH`, ORDINAL_POSITION, 'YES' AS `IS_NULLABLE`, SPECIFIC_NAME FROM INFORMATION_SCHEMA.PARAMETERS WHERE ");
        stringBuilder.append(getRoutineTypeConditionForGetProcedureColumns());
        stringBuilder.append("SPECIFIC_SCHEMA LIKE ? AND SPECIFIC_NAME LIKE ? AND (PARAMETER_NAME LIKE ? OR PARAMETER_NAME IS NULL) ");
        stringBuilder.append("ORDER BY SPECIFIC_SCHEMA, SPECIFIC_NAME, ROUTINE_TYPE, ORDINAL_POSITION");
        sqlBuf.append(stringBuilder.toString());
        PreparedStatement pStmt = null;
        try {
            pStmt = prepareMetaDataSafeStatement(sqlBuf.toString());
            if (db != null) {
                pStmt.setString(1, db);
            } else {
                pStmt.setString(1, "%");
            }
            pStmt.setString(2, procedureNamePattern);
            pStmt.setString(3, columnNamePattern);
            ResultSet rs = executeMetadataQuery(pStmt);
            ((ResultSetInternalMethods) rs).redefineFieldsForDBMD(createProcedureColumnsFields());
            if (pStmt != null) {
                pStmt.close();
            }
            return rs;
        } catch (Throwable th) {
            if (pStmt != null) {
                pStmt.close();
            }
        }
    }

    protected ResultSet getProcedureColumnsNoISParametersView(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern) throws SQLException {
        return super.getProcedureColumns(catalog, schemaPattern, procedureNamePattern, columnNamePattern);
    }

    protected String getRoutineTypeConditionForGetProcedureColumns() {
        return "";
    }

    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException {
        String catalog2;
        String tableNamePattern2;
        List<String> parseList;
        String tableNamePat;
        PreparedStatement pStmt;
        String sql;
        boolean operatingOnInformationSchema;
        String sql2;
        StringBuilder stringBuilder;
        int i;
        int i2;
        int i3;
        int i4;
        TableType tableType;
        int idx;
        ResultSet rs;
        DatabaseMetaDataUsingInfoSchema this;
        TableType[] tableTypes;
        DatabaseMetaDataUsingInfoSchema databaseMetaDataUsingInfoSchema = this;
        String[] types2 = types;
        if (catalog == null && databaseMetaDataUsingInfoSchema.conn.getNullCatalogMeansCurrent()) {
            catalog2 = databaseMetaDataUsingInfoSchema.database;
        } else {
            catalog2 = catalog;
        }
        if (tableNamePattern != null) {
            tableNamePattern2 = tableNamePattern;
        } else if (databaseMetaDataUsingInfoSchema.conn.getNullNamePatternMatchesAll()) {
            tableNamePattern2 = "%";
        } else {
            throw SQLError.createSQLException("Table name pattern can not be NULL or empty.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
        String tmpCat = "";
        if (catalog2 != null) {
            if (catalog2.length() != 0) {
                tmpCat = catalog2;
                parseList = StringUtils.splitDBdotName(tableNamePattern2, tmpCat, databaseMetaDataUsingInfoSchema.quotedId, databaseMetaDataUsingInfoSchema.conn.isNoBackslashEscapesSet());
                if (parseList.size() != 2) {
                    tableNamePat = (String) parseList.get(1);
                } else {
                    tableNamePat = tableNamePattern2;
                }
                pStmt = null;
                sql = "SELECT TABLE_SCHEMA AS TABLE_CAT, NULL AS TABLE_SCHEM, TABLE_NAME, CASE WHEN TABLE_TYPE='BASE TABLE' THEN CASE WHEN TABLE_SCHEMA = 'mysql' OR TABLE_SCHEMA = 'performance_schema' THEN 'SYSTEM TABLE' ELSE 'TABLE' END WHEN TABLE_TYPE='TEMPORARY' THEN 'LOCAL_TEMPORARY' ELSE TABLE_TYPE END AS TABLE_TYPE, TABLE_COMMENT AS REMARKS, NULL AS TYPE_CAT, NULL AS TYPE_SCHEM, NULL AS TYPE_NAME, NULL AS SELF_REFERENCING_COL_NAME, NULL AS REF_GENERATION FROM INFORMATION_SCHEMA.TABLES WHERE ";
                operatingOnInformationSchema = "information_schema".equalsIgnoreCase(catalog2);
                if (catalog2 == null) {
                    if (!operatingOnInformationSchema) {
                        if (StringUtils.indexOfIgnoreCase(0, catalog2, "%") == -1 || StringUtils.indexOfIgnoreCase(0, catalog2, "_") != -1) {
                            StringBuilder stringBuilder2 = new StringBuilder();
                            stringBuilder2.append(sql);
                            stringBuilder2.append("TABLE_SCHEMA LIKE ? ");
                            sql2 = stringBuilder2.toString();
                        }
                    }
                    StringBuilder stringBuilder3 = new StringBuilder();
                    stringBuilder3.append(sql);
                    stringBuilder3.append("TABLE_SCHEMA = ? ");
                    sql2 = stringBuilder3.toString();
                } else {
                    sql2 = new StringBuilder();
                    sql2.append(sql);
                    sql2.append("TABLE_SCHEMA LIKE ? ");
                    sql2 = sql2.toString();
                }
                if (tableNamePat != null) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append(sql2);
                    stringBuilder.append("AND TABLE_NAME LIKE ? ");
                    sql2 = stringBuilder.toString();
                } else if (StringUtils.indexOfIgnoreCase(0, tableNamePat, "%") == -1 || StringUtils.indexOfIgnoreCase(0, tableNamePat, "_") != -1) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append(sql2);
                    stringBuilder.append("AND TABLE_NAME LIKE ? ");
                    sql2 = stringBuilder.toString();
                } else {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append(sql2);
                    stringBuilder.append("AND TABLE_NAME = ? ");
                    sql2 = stringBuilder.toString();
                }
                stringBuilder = new StringBuilder();
                stringBuilder.append(sql2);
                stringBuilder.append("HAVING TABLE_TYPE IN (?,?,?,?,?) ");
                sql2 = stringBuilder.toString();
                stringBuilder = new StringBuilder();
                stringBuilder.append(sql2);
                stringBuilder.append("ORDER BY TABLE_TYPE, TABLE_SCHEMA, TABLE_NAME");
                pStmt = prepareMetaDataSafeStatement(stringBuilder.toString());
                if (catalog2 == null) {
                    pStmt.setString(1, catalog2);
                } else {
                    pStmt.setString(1, "%");
                }
                pStmt.setString(2, tableNamePat);
                i = 3;
                if (types2 != null) {
                    if (types2.length == 0) {
                        for (i2 = 0; i2 < 5; i2++) {
                            pStmt.setNull(3 + i2, 12);
                        }
                        i = 3;
                        i3 = 0;
                        while (true) {
                            i4 = i3;
                            if (i4 < types2.length) {
                                break;
                            }
                            tableType = TableType.getTableTypeEqualTo(types2[i4]);
                            if (tableType != TableType.UNKNOWN) {
                                idx = i + 1;
                                pStmt.setString(i, tableType.getName());
                                i = idx;
                            }
                            i3 = i4 + 1;
                        }
                        rs = executeMetadataQuery(pStmt);
                        ((ResultSetInternalMethods) rs).redefineFieldsForDBMD(createTablesFields());
                        this = databaseMetaDataUsingInfoSchema;
                        if (pStmt != null) {
                            pStmt.close();
                        }
                        return rs;
                    }
                }
                tableTypes = TableType.values();
                i3 = 0;
                while (true) {
                    idx = i3;
                    if (idx < 5) {
                        break;
                    }
                    pStmt.setString(i + idx, tableTypes[idx].getName());
                    i3 = idx + 1;
                    i = 3;
                }
                rs = executeMetadataQuery(pStmt);
                ((ResultSetInternalMethods) rs).redefineFieldsForDBMD(createTablesFields());
                this = databaseMetaDataUsingInfoSchema;
                if (pStmt != null) {
                    pStmt.close();
                }
                return rs;
            }
        }
        if (databaseMetaDataUsingInfoSchema.conn.getNullCatalogMeansCurrent()) {
            tmpCat = databaseMetaDataUsingInfoSchema.database;
        }
        parseList = StringUtils.splitDBdotName(tableNamePattern2, tmpCat, databaseMetaDataUsingInfoSchema.quotedId, databaseMetaDataUsingInfoSchema.conn.isNoBackslashEscapesSet());
        if (parseList.size() != 2) {
            tableNamePat = tableNamePattern2;
        } else {
            tableNamePat = (String) parseList.get(1);
        }
        pStmt = null;
        sql = "SELECT TABLE_SCHEMA AS TABLE_CAT, NULL AS TABLE_SCHEM, TABLE_NAME, CASE WHEN TABLE_TYPE='BASE TABLE' THEN CASE WHEN TABLE_SCHEMA = 'mysql' OR TABLE_SCHEMA = 'performance_schema' THEN 'SYSTEM TABLE' ELSE 'TABLE' END WHEN TABLE_TYPE='TEMPORARY' THEN 'LOCAL_TEMPORARY' ELSE TABLE_TYPE END AS TABLE_TYPE, TABLE_COMMENT AS REMARKS, NULL AS TYPE_CAT, NULL AS TYPE_SCHEM, NULL AS TYPE_NAME, NULL AS SELF_REFERENCING_COL_NAME, NULL AS REF_GENERATION FROM INFORMATION_SCHEMA.TABLES WHERE ";
        operatingOnInformationSchema = "information_schema".equalsIgnoreCase(catalog2);
        if (catalog2 == null) {
            sql2 = new StringBuilder();
            sql2.append(sql);
            sql2.append("TABLE_SCHEMA LIKE ? ");
            sql2 = sql2.toString();
        } else {
            if (operatingOnInformationSchema) {
                if (StringUtils.indexOfIgnoreCase(0, catalog2, "%") == -1) {
                }
                StringBuilder stringBuilder22 = new StringBuilder();
                stringBuilder22.append(sql);
                stringBuilder22.append("TABLE_SCHEMA LIKE ? ");
                sql2 = stringBuilder22.toString();
            }
            StringBuilder stringBuilder32 = new StringBuilder();
            stringBuilder32.append(sql);
            stringBuilder32.append("TABLE_SCHEMA = ? ");
            sql2 = stringBuilder32.toString();
        }
        if (tableNamePat != null) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(sql2);
            stringBuilder.append("AND TABLE_NAME LIKE ? ");
            sql2 = stringBuilder.toString();
        } else {
            if (StringUtils.indexOfIgnoreCase(0, tableNamePat, "%") == -1) {
            }
            stringBuilder = new StringBuilder();
            stringBuilder.append(sql2);
            stringBuilder.append("AND TABLE_NAME LIKE ? ");
            sql2 = stringBuilder.toString();
        }
        stringBuilder = new StringBuilder();
        stringBuilder.append(sql2);
        stringBuilder.append("HAVING TABLE_TYPE IN (?,?,?,?,?) ");
        sql2 = stringBuilder.toString();
        stringBuilder = new StringBuilder();
        stringBuilder.append(sql2);
        stringBuilder.append("ORDER BY TABLE_TYPE, TABLE_SCHEMA, TABLE_NAME");
        try {
            pStmt = prepareMetaDataSafeStatement(stringBuilder.toString());
            if (catalog2 == null) {
                pStmt.setString(1, "%");
            } else {
                pStmt.setString(1, catalog2);
            }
            pStmt.setString(2, tableNamePat);
            i = 3;
            if (types2 != null) {
                if (types2.length == 0) {
                    for (i2 = 0; i2 < 5; i2++) {
                        pStmt.setNull(3 + i2, 12);
                    }
                    i = 3;
                    i3 = 0;
                    while (true) {
                        i4 = i3;
                        if (i4 < types2.length) {
                            break;
                        }
                        tableType = TableType.getTableTypeEqualTo(types2[i4]);
                        if (tableType != TableType.UNKNOWN) {
                            idx = i + 1;
                            pStmt.setString(i, tableType.getName());
                            i = idx;
                        }
                        i3 = i4 + 1;
                    }
                    rs = executeMetadataQuery(pStmt);
                    ((ResultSetInternalMethods) rs).redefineFieldsForDBMD(createTablesFields());
                    this = databaseMetaDataUsingInfoSchema;
                    if (pStmt != null) {
                        pStmt.close();
                    }
                    return rs;
                }
            }
            tableTypes = TableType.values();
            i3 = 0;
            while (true) {
                idx = i3;
                if (idx < 5) {
                    break;
                }
                pStmt.setString(i + idx, tableTypes[idx].getName());
                i3 = idx + 1;
                i = 3;
            }
            rs = executeMetadataQuery(pStmt);
            ((ResultSetInternalMethods) rs).redefineFieldsForDBMD(createTablesFields());
            this = databaseMetaDataUsingInfoSchema;
            if (pStmt != null) {
                pStmt.close();
            }
            return rs;
        } catch (Throwable th) {
            Throwable th2 = th;
            this = databaseMetaDataUsingInfoSchema;
            String str = schemaPattern;
            if (pStmt != null) {
                pStmt.close();
            }
        }
    }

    public boolean gethasParametersView() {
        return this.hasParametersView;
    }

    public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException {
        String catalog2;
        DatabaseMetaDataUsingInfoSchema databaseMetaDataUsingInfoSchema = this;
        String table2 = table;
        if (catalog == null && databaseMetaDataUsingInfoSchema.conn.getNullCatalogMeansCurrent()) {
            catalog2 = databaseMetaDataUsingInfoSchema.database;
        } else {
            catalog2 = catalog;
        }
        if (table2 == null) {
            throw SQLError.createSQLException("Table not specified.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
        }
        StringBuilder sqlBuf = new StringBuilder("SELECT NULL AS SCOPE, COLUMN_NAME, ");
        MysqlDefs.appendJdbcTypeMappingQuery(sqlBuf, "DATA_TYPE");
        sqlBuf.append(" AS DATA_TYPE, ");
        sqlBuf.append("COLUMN_TYPE AS TYPE_NAME, ");
        sqlBuf.append("CASE WHEN LCASE(DATA_TYPE)='date' THEN 10 WHEN LCASE(DATA_TYPE)='time' THEN 8 WHEN LCASE(DATA_TYPE)='datetime' THEN 19 WHEN LCASE(DATA_TYPE)='timestamp' THEN 19 WHEN CHARACTER_MAXIMUM_LENGTH IS NULL THEN NUMERIC_PRECISION WHEN CHARACTER_MAXIMUM_LENGTH > 2147483647 THEN 2147483647 ELSE CHARACTER_MAXIMUM_LENGTH END AS COLUMN_SIZE, ");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(MysqlIO.getMaxBuf());
        stringBuilder.append(" AS BUFFER_LENGTH,NUMERIC_SCALE AS DECIMAL_DIGITS, ");
        stringBuilder.append(Integer.toString(1));
        stringBuilder.append(" AS PSEUDO_COLUMN FROM INFORMATION_SCHEMA.COLUMNS ");
        stringBuilder.append("WHERE TABLE_SCHEMA LIKE ? AND TABLE_NAME LIKE ? AND EXTRA LIKE '%on update CURRENT_TIMESTAMP%'");
        sqlBuf.append(stringBuilder.toString());
        try {
            PreparedStatement pStmt = prepareMetaDataSafeStatement(sqlBuf.toString());
            if (catalog2 != null) {
                pStmt.setString(1, catalog2);
            } else {
                pStmt.setString(1, "%");
            }
            pStmt.setString(2, table2);
            ResultSet rs = executeMetadataQuery(pStmt);
            ((ResultSetInternalMethods) rs).redefineFieldsForDBMD(new Field[]{new Field("", "SCOPE", 5, 5), new Field("", "COLUMN_NAME", 1, 32), new Field("", "DATA_TYPE", 4, 5), new Field("", "TYPE_NAME", 1, 16), new Field("", "COLUMN_SIZE", 4, 16), new Field("", "BUFFER_LENGTH", 4, 16), new Field("", "DECIMAL_DIGITS", 5, 16), new Field("", "PSEUDO_COLUMN", 5, 5)});
            if (pStmt != null) {
                pStmt.close();
            }
            return rs;
        } catch (Throwable th) {
            PreparedStatement pStmt2 = null;
            Throwable pStmt3 = th;
            DatabaseMetaDataUsingInfoSchema this = databaseMetaDataUsingInfoSchema;
            if (pStmt2 != null) {
                pStmt2.close();
            }
        }
    }

    public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern) throws SQLException {
        if (!this.hasParametersView) {
            return super.getFunctionColumns(catalog, schemaPattern, functionNamePattern, columnNamePattern);
        }
        if (functionNamePattern == null || functionNamePattern.length() == 0) {
            if (this.conn.getNullNamePatternMatchesAll()) {
                functionNamePattern = "%";
            } else {
                throw SQLError.createSQLException("Procedure name pattern can not be NULL or empty.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
            }
        }
        String db = null;
        if (catalog != null) {
            db = catalog;
        } else if (this.conn.getNullCatalogMeansCurrent()) {
            db = this.database;
        }
        StringBuilder sqlBuf = new StringBuilder("SELECT SPECIFIC_SCHEMA AS FUNCTION_CAT, NULL AS `FUNCTION_SCHEM`, SPECIFIC_NAME AS `FUNCTION_NAME`, ");
        sqlBuf.append("IFNULL(PARAMETER_NAME, '') AS `COLUMN_NAME`, CASE WHEN PARAMETER_MODE = 'IN' THEN ");
        sqlBuf.append(getJDBC4FunctionConstant(JDBC4FunctionConstant.FUNCTION_COLUMN_IN));
        sqlBuf.append(" WHEN PARAMETER_MODE = 'OUT' THEN ");
        sqlBuf.append(getJDBC4FunctionConstant(JDBC4FunctionConstant.FUNCTION_COLUMN_OUT));
        sqlBuf.append(" WHEN PARAMETER_MODE = 'INOUT' THEN ");
        sqlBuf.append(getJDBC4FunctionConstant(JDBC4FunctionConstant.FUNCTION_COLUMN_INOUT));
        sqlBuf.append(" WHEN ORDINAL_POSITION = 0 THEN ");
        sqlBuf.append(getJDBC4FunctionConstant(JDBC4FunctionConstant.FUNCTION_COLUMN_RETURN));
        sqlBuf.append(" ELSE ");
        sqlBuf.append(getJDBC4FunctionConstant(JDBC4FunctionConstant.FUNCTION_COLUMN_UNKNOWN));
        sqlBuf.append(" END AS `COLUMN_TYPE`, ");
        MysqlDefs.appendJdbcTypeMappingQuery(sqlBuf, "DATA_TYPE");
        sqlBuf.append(" AS `DATA_TYPE`, ");
        if (this.conn.getCapitalizeTypeNames()) {
            sqlBuf.append("UPPER(CASE WHEN LOCATE('unsigned', DATA_TYPE) != 0 AND LOCATE('unsigned', DATA_TYPE) = 0 THEN CONCAT(DATA_TYPE, ' unsigned') ELSE DATA_TYPE END) AS `TYPE_NAME`,");
        } else {
            sqlBuf.append("CASE WHEN LOCATE('unsigned', DATA_TYPE) != 0 AND LOCATE('unsigned', DATA_TYPE) = 0 THEN CONCAT(DATA_TYPE, ' unsigned') ELSE DATA_TYPE END AS `TYPE_NAME`,");
        }
        sqlBuf.append("NUMERIC_PRECISION AS `PRECISION`, ");
        sqlBuf.append("CASE WHEN LCASE(DATA_TYPE)='date' THEN 10 WHEN LCASE(DATA_TYPE)='time' THEN 8 WHEN LCASE(DATA_TYPE)='datetime' THEN 19 WHEN LCASE(DATA_TYPE)='timestamp' THEN 19 WHEN CHARACTER_MAXIMUM_LENGTH IS NULL THEN NUMERIC_PRECISION WHEN CHARACTER_MAXIMUM_LENGTH > 2147483647 THEN 2147483647 ELSE CHARACTER_MAXIMUM_LENGTH END AS LENGTH, ");
        sqlBuf.append("NUMERIC_SCALE AS `SCALE`, ");
        sqlBuf.append("10 AS RADIX,");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getJDBC4FunctionConstant(JDBC4FunctionConstant.FUNCTION_NULLABLE));
        stringBuilder.append(" AS `NULLABLE`,  NULL AS `REMARKS`, ");
        stringBuilder.append("CHARACTER_OCTET_LENGTH AS `CHAR_OCTET_LENGTH`,  ORDINAL_POSITION, 'YES' AS `IS_NULLABLE`, SPECIFIC_NAME ");
        stringBuilder.append("FROM INFORMATION_SCHEMA.PARAMETERS WHERE ");
        stringBuilder.append("SPECIFIC_SCHEMA LIKE ? AND SPECIFIC_NAME LIKE ? AND (PARAMETER_NAME LIKE ? OR PARAMETER_NAME IS NULL) ");
        stringBuilder.append("AND ROUTINE_TYPE='FUNCTION' ORDER BY SPECIFIC_SCHEMA, SPECIFIC_NAME, ORDINAL_POSITION");
        sqlBuf.append(stringBuilder.toString());
        PreparedStatement pStmt = null;
        try {
            pStmt = prepareMetaDataSafeStatement(sqlBuf.toString());
            if (db != null) {
                pStmt.setString(1, db);
            } else {
                pStmt.setString(1, "%");
            }
            pStmt.setString(2, functionNamePattern);
            pStmt.setString(3, columnNamePattern);
            ResultSet rs = executeMetadataQuery(pStmt);
            ((ResultSetInternalMethods) rs).redefineFieldsForDBMD(createFunctionColumnsFields());
            if (pStmt != null) {
                pStmt.close();
            }
            return rs;
        } catch (Throwable th) {
            if (pStmt != null) {
                pStmt.close();
            }
        }
    }

    protected int getJDBC4FunctionConstant(JDBC4FunctionConstant constant) {
        return 0;
    }

    public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) throws SQLException {
        String functionNamePattern2;
        String db;
        String sql;
        PreparedStatement pStmt;
        DatabaseMetaDataUsingInfoSchema databaseMetaDataUsingInfoSchema = this;
        if (functionNamePattern != null) {
            if (functionNamePattern.length() != 0) {
                functionNamePattern2 = functionNamePattern;
                db = null;
                if (catalog == null) {
                    db = catalog;
                } else if (databaseMetaDataUsingInfoSchema.conn.getNullCatalogMeansCurrent()) {
                    db = databaseMetaDataUsingInfoSchema.database;
                }
                sql = new StringBuilder();
                sql.append("SELECT ROUTINE_SCHEMA AS FUNCTION_CAT, NULL AS FUNCTION_SCHEM, ROUTINE_NAME AS FUNCTION_NAME, ROUTINE_COMMENT AS REMARKS, ");
                sql.append(getJDBC4FunctionNoTableConstant());
                sql.append(" AS FUNCTION_TYPE, ROUTINE_NAME AS SPECIFIC_NAME FROM INFORMATION_SCHEMA.ROUTINES ");
                sql.append("WHERE ROUTINE_TYPE LIKE 'FUNCTION' AND ROUTINE_SCHEMA LIKE ? AND ");
                sql.append("ROUTINE_NAME LIKE ? ORDER BY FUNCTION_CAT, FUNCTION_SCHEM, FUNCTION_NAME, SPECIFIC_NAME");
                try {
                    pStmt = prepareMetaDataSafeStatement(sql.toString());
                    pStmt.setString(1, db == null ? db : "%");
                    pStmt.setString(2, functionNamePattern2);
                    ResultSet rs = executeMetadataQuery(pStmt);
                    ((ResultSetInternalMethods) rs).redefineFieldsForDBMD(new Field[]{new Field("", "FUNCTION_CAT", 1, 255), new Field("", "FUNCTION_SCHEM", 1, 255), new Field("", "FUNCTION_NAME", 1, 255), new Field("", "REMARKS", 1, 255), new Field("", "FUNCTION_TYPE", 5, 6), new Field("", "SPECIFIC_NAME", 1, 255)});
                    if (pStmt != null) {
                        pStmt.close();
                    }
                    return rs;
                } catch (Throwable th) {
                    pStmt = null;
                    Throwable pStmt2 = th;
                    DatabaseMetaDataUsingInfoSchema this = databaseMetaDataUsingInfoSchema;
                    PreparedStatement pStmt3;
                    if (pStmt3 != null) {
                        pStmt3.close();
                    }
                }
            }
        }
        if (databaseMetaDataUsingInfoSchema.conn.getNullNamePatternMatchesAll()) {
            functionNamePattern2 = "%";
            db = null;
            if (catalog == null) {
                db = catalog;
            } else if (databaseMetaDataUsingInfoSchema.conn.getNullCatalogMeansCurrent()) {
                db = databaseMetaDataUsingInfoSchema.database;
            }
            sql = new StringBuilder();
            sql.append("SELECT ROUTINE_SCHEMA AS FUNCTION_CAT, NULL AS FUNCTION_SCHEM, ROUTINE_NAME AS FUNCTION_NAME, ROUTINE_COMMENT AS REMARKS, ");
            sql.append(getJDBC4FunctionNoTableConstant());
            sql.append(" AS FUNCTION_TYPE, ROUTINE_NAME AS SPECIFIC_NAME FROM INFORMATION_SCHEMA.ROUTINES ");
            sql.append("WHERE ROUTINE_TYPE LIKE 'FUNCTION' AND ROUTINE_SCHEMA LIKE ? AND ");
            sql.append("ROUTINE_NAME LIKE ? ORDER BY FUNCTION_CAT, FUNCTION_SCHEM, FUNCTION_NAME, SPECIFIC_NAME");
            pStmt = prepareMetaDataSafeStatement(sql.toString());
            if (db == null) {
            }
            pStmt.setString(1, db == null ? db : "%");
            pStmt.setString(2, functionNamePattern2);
            ResultSet rs2 = executeMetadataQuery(pStmt);
            ((ResultSetInternalMethods) rs2).redefineFieldsForDBMD(new Field[]{new Field("", "FUNCTION_CAT", 1, 255), new Field("", "FUNCTION_SCHEM", 1, 255), new Field("", "FUNCTION_NAME", 1, 255), new Field("", "REMARKS", 1, 255), new Field("", "FUNCTION_TYPE", 5, 6), new Field("", "SPECIFIC_NAME", 1, 255)});
            if (pStmt != null) {
                pStmt.close();
            }
            return rs2;
        }
        throw SQLError.createSQLException("Function name pattern can not be NULL or empty.", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, getExceptionInterceptor());
    }

    protected int getJDBC4FunctionNoTableConstant() {
        return 0;
    }
}
