package com.mysql.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.TimeZone;

public abstract class ResultSetRow {
    protected ExceptionInterceptor exceptionInterceptor;
    protected Field[] metadata;

    public abstract void closeOpenStreams();

    public abstract InputStream getBinaryInputStream(int i) throws SQLException;

    public abstract int getBytesSize();

    public abstract byte[] getColumnValue(int i) throws SQLException;

    public abstract Date getDateFast(int i, MySQLConnection mySQLConnection, ResultSetImpl resultSetImpl, Calendar calendar) throws SQLException;

    public abstract int getInt(int i) throws SQLException;

    public abstract long getLong(int i) throws SQLException;

    public abstract Date getNativeDate(int i, MySQLConnection mySQLConnection, ResultSetImpl resultSetImpl, Calendar calendar) throws SQLException;

    public abstract Object getNativeDateTimeValue(int i, Calendar calendar, int i2, int i3, TimeZone timeZone, boolean z, MySQLConnection mySQLConnection, ResultSetImpl resultSetImpl) throws SQLException;

    public abstract double getNativeDouble(int i) throws SQLException;

    public abstract float getNativeFloat(int i) throws SQLException;

    public abstract int getNativeInt(int i) throws SQLException;

    public abstract long getNativeLong(int i) throws SQLException;

    public abstract short getNativeShort(int i) throws SQLException;

    public abstract Time getNativeTime(int i, Calendar calendar, TimeZone timeZone, boolean z, MySQLConnection mySQLConnection, ResultSetImpl resultSetImpl) throws SQLException;

    public abstract Timestamp getNativeTimestamp(int i, Calendar calendar, TimeZone timeZone, boolean z, MySQLConnection mySQLConnection, ResultSetImpl resultSetImpl) throws SQLException;

    public abstract Reader getReader(int i) throws SQLException;

    public abstract String getString(int i, String str, MySQLConnection mySQLConnection) throws SQLException;

    public abstract Time getTimeFast(int i, Calendar calendar, TimeZone timeZone, boolean z, MySQLConnection mySQLConnection, ResultSetImpl resultSetImpl) throws SQLException;

    public abstract Timestamp getTimestampFast(int i, Calendar calendar, TimeZone timeZone, boolean z, MySQLConnection mySQLConnection, ResultSetImpl resultSetImpl) throws SQLException;

    public abstract boolean isFloatingPointNumber(int i) throws SQLException;

    public abstract boolean isNull(int i) throws SQLException;

    public abstract long length(int i) throws SQLException;

    public abstract void setColumnValue(int i, byte[] bArr) throws SQLException;

    protected ResultSetRow(ExceptionInterceptor exceptionInterceptor) {
        this.exceptionInterceptor = exceptionInterceptor;
    }

    protected final Date getDateFast(int columnIndex, byte[] dateAsBytes, int offset, int length, MySQLConnection conn, ResultSetImpl rs, Calendar targetCalendar) throws SQLException {
        SQLException e;
        int i;
        ResultSetRow resultSetRow = this;
        byte[] bArr = dateAsBytes;
        int i2 = offset;
        int i3 = length;
        ResultSetImpl resultSetImpl = rs;
        Calendar calendar = targetCalendar;
        int year = 0;
        if (bArr == null) {
            return null;
        }
        int i4;
        int i5;
        boolean allZeroDate = true;
        boolean z = false;
        int i6 = 0;
        while (i6 < i3) {
            try {
                if (bArr[i2 + i6] == (byte) 58) {
                    z = true;
                    break;
                }
                i6++;
            } catch (SQLException e2) {
                e = e2;
                i = i3;
                i4 = year;
            } catch (Exception e3) {
                e = e3;
                i = i3;
            }
        }
        for (i5 = 0; i5 < i3; i5++) {
            byte b = bArr[i2 + i5];
            if (b == (byte) 32 || b == (byte) 45 || b == (byte) 47) {
                z = false;
            }
            if (b != (byte) 48 && b != (byte) 32 && b != (byte) 58 && b != (byte) 45 && b != (byte) 47 && b != (byte) 46) {
                allZeroDate = false;
                break;
            }
        }
        i5 = -1;
        int i7 = 0;
        while (i7 < i3) {
            try {
                if (bArr[i2 + i7] == (byte) 46) {
                    i5 = i7;
                    break;
                }
                i7++;
                i3 = length;
            } catch (SQLException e4) {
                SQLException sqlEx = e4;
                i4 = 0;
                i = length;
            } catch (Exception e5) {
                Exception e52;
                i3 = e52;
                i4 = 0;
                i = length;
            }
        }
        if (i5 > -1) {
            i3 = i5;
        } else {
            i3 = length;
        }
        if (z || !allZeroDate) {
            i4 = 0;
            if (resultSetRow.metadata[columnIndex].getMysqlType() == 7) {
                if (i3 == 2) {
                    year = StringUtils.getInt(bArr, i2 + 0, i2 + 2);
                    if (year <= 69) {
                        year += 100;
                    }
                    return resultSetImpl.fastDateCreate(calendar, year + MysqlErrorNumbers.ER_SLAVE_SQL_THREAD_MUST_STOP, 1, 1);
                } else if (i3 != 4) {
                    if (i3 != 6) {
                        if (i3 != 8) {
                            if (!(i3 == 10 || i3 == 12)) {
                                if (i3 != 14) {
                                    if (i3 == 19 || i3 == 21 || i3 == 29) {
                                        year = StringUtils.getInt(bArr, i2 + 0, i2 + 4);
                                        return resultSetImpl.fastDateCreate(calendar, year, StringUtils.getInt(bArr, i2 + 5, i2 + 7), StringUtils.getInt(bArr, i2 + 8, i2 + 10));
                                    }
                                    throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_Date", new Object[]{StringUtils.toString(dateAsBytes), Integer.valueOf(columnIndex + 1)}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, resultSetRow.exceptionInterceptor);
                                }
                            }
                        }
                        year = StringUtils.getInt(bArr, i2 + 0, i2 + 4);
                        return resultSetImpl.fastDateCreate(calendar, year, StringUtils.getInt(bArr, i2 + 4, i2 + 6), StringUtils.getInt(bArr, i2 + 6, i2 + 8));
                    }
                    year = StringUtils.getInt(bArr, i2 + 0, i2 + 2);
                    if (year <= 69) {
                        year += 100;
                    }
                    return resultSetImpl.fastDateCreate(calendar, year + MysqlErrorNumbers.ER_SLAVE_SQL_THREAD_MUST_STOP, StringUtils.getInt(bArr, i2 + 2, i2 + 4), StringUtils.getInt(bArr, i2 + 4, i2 + 6));
                } else {
                    year = StringUtils.getInt(bArr, i2 + 0, i2 + 4);
                    if (year <= 69) {
                        year += 100;
                    }
                    return resultSetImpl.fastDateCreate(calendar, year + MysqlErrorNumbers.ER_SLAVE_SQL_THREAD_MUST_STOP, StringUtils.getInt(bArr, i2 + 2, i2 + 4), 1);
                }
            } else if (resultSetRow.metadata[columnIndex].getMysqlType() == 13) {
                if (i3 != 2) {
                    if (i3 != 1) {
                        year = StringUtils.getInt(bArr, i2 + 0, i2 + 4);
                        return resultSetImpl.fastDateCreate(calendar, year, 1, 1);
                    }
                }
                year = StringUtils.getInt(bArr, i2, i2 + i3);
                if (year <= 69) {
                    year += 100;
                }
                year += MysqlErrorNumbers.ER_SLAVE_SQL_THREAD_MUST_STOP;
                return resultSetImpl.fastDateCreate(calendar, year, 1, 1);
            } else if (resultSetRow.metadata[columnIndex].getMysqlType() == 11) {
                return resultSetImpl.fastDateCreate(calendar, 1970, 1, 1);
            } else {
                if (i3 >= 10) {
                    int month;
                    int day;
                    if (i3 != 18) {
                        year = StringUtils.getInt(bArr, i2 + 0, i2 + 4);
                        month = StringUtils.getInt(bArr, i2 + 5, i2 + 7);
                        day = StringUtils.getInt(bArr, i2 + 8, i2 + 10);
                    } else {
                        StringTokenizer st = new StringTokenizer(StringUtils.toString(bArr, i2, i3, "ISO8859_1"), "- ");
                        i7 = Integer.parseInt(st.nextToken());
                        try {
                            month = Integer.parseInt(st.nextToken());
                            day = Integer.parseInt(st.nextToken());
                            year = i7;
                        } catch (SQLException e6) {
                            e4 = e6;
                            i = i3;
                            i4 = i7;
                            sqlEx = e4;
                            throw sqlEx;
                        } catch (Exception e7) {
                            e52 = e7;
                            i = i3;
                            i4 = i7;
                            i3 = e52;
                            SQLException sqlEx2 = SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_Date", new Object[]{StringUtils.toString(dateAsBytes), Integer.valueOf(columnIndex + 1)}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, resultSetRow.exceptionInterceptor);
                            sqlEx2.initCause(i3);
                            throw sqlEx2;
                        }
                    }
                    return resultSetImpl.fastDateCreate(calendar, year, month, day);
                } else if (i3 == 8) {
                    return resultSetImpl.fastDateCreate(calendar, 1970, 1, 1);
                } else {
                    throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_Date", new Object[]{StringUtils.toString(dateAsBytes), Integer.valueOf(columnIndex + 1)}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, resultSetRow.exceptionInterceptor);
                }
            }
        }
        try {
            if ("convertToNull".equals(conn.getZeroDateTimeBehavior())) {
                return null;
            }
            if ("exception".equals(conn.getZeroDateTimeBehavior())) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Value '");
                stringBuilder.append(StringUtils.toString(dateAsBytes));
                stringBuilder.append("' can not be represented as java.sql.Date");
                i4 = 0;
                try {
                    throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, resultSetRow.exceptionInterceptor);
                } catch (SQLException e8) {
                    e4 = e8;
                    i = i3;
                    sqlEx = e4;
                    throw sqlEx;
                } catch (Exception e9) {
                    e52 = e9;
                    i = i3;
                    i3 = e52;
                    SQLException sqlEx22 = SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_Date", new Object[]{StringUtils.toString(dateAsBytes), Integer.valueOf(columnIndex + 1)}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, resultSetRow.exceptionInterceptor);
                    sqlEx22.initCause(i3);
                    throw sqlEx22;
                }
            }
            i4 = 0;
            return resultSetImpl.fastDateCreate(calendar, 1, 1, 1);
        } catch (SQLException e42) {
            i4 = 0;
            i = i3;
            sqlEx = e42;
            throw sqlEx;
        } catch (Exception e522) {
            i4 = 0;
            i = i3;
            i3 = e522;
            SQLException sqlEx222 = SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_Date", new Object[]{StringUtils.toString(dateAsBytes), Integer.valueOf(columnIndex + 1)}), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, resultSetRow.exceptionInterceptor);
            sqlEx222.initCause(i3);
            throw sqlEx222;
        }
    }

    protected Date getNativeDate(int columnIndex, byte[] bits, int offset, int length, MySQLConnection conn, ResultSetImpl rs, Calendar cal) throws SQLException {
        int year = 0;
        int month = 0;
        int day = 0;
        if (length != 0) {
            year = (bits[offset + 0] & 255) | ((bits[offset + 1] & 255) << 8);
            month = bits[offset + 2];
            day = bits[offset + 3];
        }
        if (length == 0 || (year == 0 && month == 0 && day == 0)) {
            if ("convertToNull".equals(conn.getZeroDateTimeBehavior())) {
                return null;
            }
            if ("exception".equals(conn.getZeroDateTimeBehavior())) {
                throw SQLError.createSQLException("Value '0000-00-00' can not be represented as java.sql.Date", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, this.exceptionInterceptor);
            }
            year = 1;
            month = 1;
            day = 1;
        }
        if (!rs.useLegacyDatetimeCode) {
            return TimeUtil.fastDateCreate(year, month, day, cal);
        }
        return rs.fastDateCreate(cal == null ? rs.getCalendarInstanceForSessionOrNew() : cal, year, month, day);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected java.lang.Object getNativeDateTimeValue(int r33, byte[] r34, int r35, int r36, java.util.Calendar r37, int r38, int r39, java.util.TimeZone r40, boolean r41, com.mysql.jdbc.MySQLConnection r42, com.mysql.jdbc.ResultSetImpl r43) throws java.sql.SQLException {
        /*
        r32 = this;
        r0 = r32;
        r3 = r36;
        r13 = r37;
        r14 = r39;
        r12 = r40;
        r11 = r41;
        r10 = r43;
        r4 = 0;
        r6 = 0;
        r7 = 0;
        r8 = 0;
        r9 = 0;
        r15 = 0;
        r16 = 0;
        r17 = 0;
        if (r34 != 0) goto L_0x001b;
    L_0x001a:
        return r17;
    L_0x001b:
        r5 = r42.getUseJDBCCompliantTimezoneShift();
        if (r5 == 0) goto L_0x0026;
    L_0x0021:
        r5 = r42.getUtcCalendar();
        goto L_0x002a;
    L_0x0026:
        r5 = r43.getCalendarInstanceForSessionOrNew();
    L_0x002a:
        r18 = 0;
        r24 = r4;
        r4 = 7;
        if (r14 == r4) goto L_0x009f;
    L_0x0031:
        switch(r14) {
            case 10: goto L_0x0067;
            case 11: goto L_0x0048;
            case 12: goto L_0x0044;
            default: goto L_0x0034;
        };
    L_0x0034:
        r4 = 0;
        r27 = r4;
        r26 = r6;
        r3 = r7;
        r7 = r15;
    L_0x003b:
        r25 = r16;
    L_0x003d:
        r31 = r9;
        r9 = r8;
        r8 = r31;
        goto L_0x0110;
    L_0x0044:
        r26 = r6;
        goto L_0x00a1;
    L_0x0048:
        r4 = 1;
        if (r3 == 0) goto L_0x0057;
    L_0x004b:
        r18 = r35 + 5;
        r8 = r34[r18];
        r18 = r35 + 6;
        r9 = r34[r18];
        r18 = r35 + 7;
        r15 = r34[r18];
    L_0x0057:
        r18 = 1970; // 0x7b2 float:2.76E-42 double:9.733E-321;
        r6 = 1;
        r7 = 1;
        r27 = r4;
        r26 = r6;
        r3 = r7;
        r7 = r15;
        r25 = r16;
        r24 = r18;
        goto L_0x003d;
    L_0x0067:
        r4 = 1;
        if (r3 == 0) goto L_0x008e;
    L_0x006a:
        r18 = r35 + 0;
        r25 = r4;
        r4 = r34[r18];
        r4 = r4 & 255;
        r18 = r35 + 1;
        r26 = r6;
        r6 = r34[r18];
        r6 = r6 & 255;
        r6 = r6 << 8;
        r4 = r4 | r6;
        r6 = r35 + 2;
        r6 = r34[r6];
        r18 = r35 + 3;
        r7 = r34[r18];
        r24 = r4;
        r26 = r6;
        r3 = r7;
        r7 = r15;
        r27 = r25;
        goto L_0x003b;
    L_0x008e:
        r25 = r4;
        r26 = r6;
        r3 = r7;
        r7 = r15;
        r27 = r25;
        r25 = r16;
        r31 = r9;
        r9 = r8;
        r8 = r31;
        goto L_0x0110;
    L_0x009f:
        r26 = r6;
    L_0x00a1:
        r6 = 1;
        if (r3 == 0) goto L_0x0105;
    L_0x00a4:
        r18 = r35 + 0;
        r4 = r34[r18];
        r4 = r4 & 255;
        r18 = r35 + 1;
        r27 = r6;
        r6 = r34[r18];
        r6 = r6 & 255;
        r6 = r6 << 8;
        r4 = r4 | r6;
        r6 = r35 + 2;
        r6 = r34[r6];
        r18 = r35 + 3;
        r7 = r34[r18];
        r28 = r4;
        r4 = 4;
        if (r3 <= r4) goto L_0x00ce;
    L_0x00c2:
        r4 = r35 + 4;
        r8 = r34[r4];
        r4 = r35 + 5;
        r9 = r34[r4];
        r4 = r35 + 6;
        r15 = r34[r4];
    L_0x00ce:
        r4 = 7;
        if (r3 <= r4) goto L_0x00fb;
    L_0x00d1:
        r4 = r35 + 7;
        r4 = r34[r4];
        r4 = r4 & 255;
        r18 = r35 + 8;
        r3 = r34[r18];
        r3 = r3 & 255;
        r3 = r3 << 8;
        r3 = r3 | r4;
        r4 = r35 + 9;
        r4 = r34[r4];
        r4 = r4 & 255;
        r4 = r4 << 16;
        r3 = r3 | r4;
        r4 = r35 + 10;
        r4 = r34[r4];
        r4 = r4 & 255;
        r4 = r4 << 24;
        r3 = r3 | r4;
        r3 = r3 * 1000;
        r25 = r3;
        r26 = r6;
        r3 = r7;
        r7 = r15;
        goto L_0x0101;
    L_0x00fb:
        r26 = r6;
        r3 = r7;
        r7 = r15;
        r25 = r16;
    L_0x0101:
        r24 = r28;
        goto L_0x003d;
    L_0x0105:
        r27 = r6;
        r3 = r7;
        r7 = r15;
        r25 = r16;
        r31 = r9;
        r9 = r8;
        r8 = r31;
    L_0x0110:
        switch(r38) {
            case 91: goto L_0x01e3;
            case 92: goto L_0x01ab;
            case 93: goto L_0x0124;
            default: goto L_0x0113;
        };
    L_0x0113:
        r4 = r3;
        r1 = r7;
        r2 = r8;
        r14 = r9;
        r3 = r10;
        r7 = r11;
        r0 = r12;
        r6 = new java.sql.SQLException;
        r8 = "Internal error - conversion method doesn't support this type";
        r9 = "S1000";
        r6.<init>(r8, r9);
        throw r6;
    L_0x0124:
        if (r27 == 0) goto L_0x019f;
    L_0x0126:
        if (r24 != 0) goto L_0x0155;
    L_0x0128:
        if (r26 != 0) goto L_0x0155;
    L_0x012a:
        if (r3 != 0) goto L_0x0155;
    L_0x012c:
        r4 = "convertToNull";
        r6 = r42.getZeroDateTimeBehavior();
        r4 = r4.equals(r6);
        if (r4 == 0) goto L_0x0139;
    L_0x0138:
        return r17;
    L_0x0139:
        r4 = "exception";
        r6 = r42.getZeroDateTimeBehavior();
        r4 = r4.equals(r6);
        if (r4 == 0) goto L_0x014f;
    L_0x0145:
        r4 = new java.sql.SQLException;
        r6 = "Value '0000-00-00' can not be represented as java.sql.Timestamp";
        r1 = "S1009";
        r4.<init>(r6, r1);
        throw r4;
    L_0x014f:
        r1 = 1;
        r4 = 1;
        r3 = 1;
        r26 = r4;
        goto L_0x0157;
    L_0x0155:
        r1 = r24;
    L_0x0157:
        r4 = r10.useLegacyDatetimeCode;
        if (r4 != 0) goto L_0x016f;
    L_0x015b:
        r15 = r12;
        r16 = r1;
        r17 = r26;
        r18 = r3;
        r19 = r9;
        r20 = r8;
        r21 = r7;
        r22 = r25;
        r4 = com.mysql.jdbc.TimeUtil.fastTimestampCreate(r15, r16, r17, r18, r19, r20, r21, r22);
        return r4;
    L_0x016f:
        r16 = r43.getCalendarInstanceForSessionOrNew();
        r15 = r10;
        r17 = r1;
        r18 = r26;
        r19 = r3;
        r20 = r9;
        r21 = r8;
        r22 = r7;
        r23 = r25;
        r15 = r15.fastTimestampCreate(r16, r17, r18, r19, r20, r21, r22, r23);
        r16 = r42.getServerTimezoneTZ();
        r4 = r42;
        r6 = r13;
        r29 = r1;
        r1 = r7;
        r7 = r15;
        r2 = r8;
        r8 = r16;
        r14 = r9;
        r9 = r12;
        r30 = r3;
        r3 = r10;
        r10 = r11;
        r4 = com.mysql.jdbc.TimeUtil.changeTimezone(r4, r5, r6, r7, r8, r9, r10);
        return r4;
    L_0x019f:
        r4 = r3;
        r1 = r7;
        r2 = r8;
        r14 = r9;
        r3 = r10;
        r6 = r33 + 1;
        r6 = r3.getNativeTimestampViaParseConversion(r6, r13, r12, r11);
        return r6;
    L_0x01ab:
        r4 = r3;
        r1 = r7;
        r2 = r8;
        r14 = r9;
        r3 = r10;
        if (r27 == 0) goto L_0x01d9;
    L_0x01b2:
        r6 = r3.useLegacyDatetimeCode;
        if (r6 != 0) goto L_0x01bd;
    L_0x01b6:
        r6 = r0.exceptionInterceptor;
        r6 = com.mysql.jdbc.TimeUtil.fastTimeCreate(r14, r2, r1, r13, r6);
        return r6;
    L_0x01bd:
        r6 = r43.getCalendarInstanceForSessionOrNew();
        r7 = r0.exceptionInterceptor;
        r15 = com.mysql.jdbc.TimeUtil.fastTimeCreate(r6, r14, r2, r1, r7);
        r10 = r42.getServerTimezoneTZ();
        r6 = r42;
        r7 = r5;
        r8 = r13;
        r9 = r15;
        r11 = r12;
        r0 = r12;
        r12 = r41;
        r6 = com.mysql.jdbc.TimeUtil.changeTimezone(r6, r7, r8, r9, r10, r11, r12);
        return r6;
    L_0x01d9:
        r0 = r12;
        r6 = r33 + 1;
        r7 = r41;
        r6 = r3.getNativeTimeViaParseConversion(r6, r13, r0, r7);
        return r6;
    L_0x01e3:
        r4 = r3;
        r1 = r7;
        r2 = r8;
        r14 = r9;
        r3 = r10;
        r7 = r11;
        r0 = r12;
        if (r27 == 0) goto L_0x0231;
    L_0x01ec:
        if (r24 != 0) goto L_0x021a;
    L_0x01ee:
        if (r26 != 0) goto L_0x021a;
    L_0x01f0:
        if (r4 != 0) goto L_0x021a;
    L_0x01f2:
        r6 = "convertToNull";
        r8 = r42.getZeroDateTimeBehavior();
        r6 = r6.equals(r8);
        if (r6 == 0) goto L_0x01ff;
    L_0x01fe:
        return r17;
    L_0x01ff:
        r6 = "exception";
        r8 = r42.getZeroDateTimeBehavior();
        r6 = r6.equals(r8);
        if (r6 == 0) goto L_0x0215;
    L_0x020b:
        r6 = new java.sql.SQLException;
        r8 = "Value '0000-00-00' can not be represented as java.sql.Date";
        r9 = "S1009";
        r6.<init>(r8, r9);
        throw r6;
    L_0x0215:
        r24 = 1;
        r26 = 1;
        r4 = 1;
    L_0x021a:
        r8 = r4;
        r4 = r24;
        r6 = r26;
        r9 = r3.useLegacyDatetimeCode;
        if (r9 != 0) goto L_0x0228;
    L_0x0223:
        r9 = com.mysql.jdbc.TimeUtil.fastDateCreate(r4, r6, r8, r13);
        return r9;
    L_0x0228:
        r9 = r43.getCalendarInstanceForSessionOrNew();
        r9 = r3.fastDateCreate(r9, r4, r6, r8);
        return r9;
    L_0x0231:
        r6 = r33 + 1;
        r6 = r3.getNativeDateViaParseConversion(r6);
        return r6;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.ResultSetRow.getNativeDateTimeValue(int, byte[], int, int, java.util.Calendar, int, int, java.util.TimeZone, boolean, com.mysql.jdbc.MySQLConnection, com.mysql.jdbc.ResultSetImpl):java.lang.Object");
    }

    protected double getNativeDouble(byte[] bits, int offset) {
        return Double.longBitsToDouble(((((((((long) (bits[offset + 0] & 255)) | (((long) (bits[offset + 1] & 255)) << 8)) | (((long) (bits[offset + 2] & 255)) << 16)) | (((long) (bits[offset + 3] & 255)) << 24)) | (((long) (bits[offset + 4] & 255)) << 32)) | (((long) (bits[offset + 5] & 255)) << 40)) | (((long) (bits[offset + 6] & 255)) << 48)) | (((long) (bits[offset + 7] & 255)) << 56));
    }

    protected float getNativeFloat(byte[] bits, int offset) {
        return Float.intBitsToFloat((((bits[offset + 0] & 255) | ((bits[offset + 1] & 255) << 8)) | ((bits[offset + 2] & 255) << 16)) | ((bits[offset + 3] & 255) << 24));
    }

    protected int getNativeInt(byte[] bits, int offset) {
        return (((bits[offset + 0] & 255) | ((bits[offset + 1] & 255) << 8)) | ((bits[offset + 2] & 255) << 16)) | ((bits[offset + 3] & 255) << 24);
    }

    protected long getNativeLong(byte[] bits, int offset) {
        return ((((((((long) (bits[offset + 0] & 255)) | (((long) (bits[offset + 1] & 255)) << 8)) | (((long) (bits[offset + 2] & 255)) << 16)) | (((long) (bits[offset + 3] & 255)) << 24)) | (((long) (bits[offset + 4] & 255)) << 32)) | (((long) (bits[offset + 5] & 255)) << 40)) | (((long) (bits[offset + 6] & 255)) << 48)) | (((long) (bits[offset + 7] & 255)) << 56);
    }

    protected short getNativeShort(byte[] bits, int offset) {
        return (short) ((bits[offset + 0] & 255) | ((bits[offset + 1] & 255) << 8));
    }

    protected Time getNativeTime(int columnIndex, byte[] bits, int offset, int length, Calendar targetCalendar, TimeZone tz, boolean rollForward, MySQLConnection conn, ResultSetImpl rs) throws SQLException {
        ResultSetRow resultSetRow = this;
        int hour = 0;
        int minute = 0;
        int seconds = 0;
        if (length != 0) {
            hour = bits[offset + 5];
            minute = bits[offset + 6];
            seconds = bits[offset + 7];
        }
        if (rs.useLegacyDatetimeCode) {
            Calendar calendar = targetCalendar;
            Calendar sessionCalendar = rs.getCalendarInstanceForSessionOrNew();
            return TimeUtil.changeTimezone(conn, sessionCalendar, calendar, TimeUtil.fastTimeCreate(sessionCalendar, hour, minute, seconds, resultSetRow.exceptionInterceptor), conn.getServerTimezoneTZ(), tz, rollForward);
        }
        return TimeUtil.fastTimeCreate(hour, minute, seconds, targetCalendar, resultSetRow.exceptionInterceptor);
    }

    protected Timestamp getNativeTimestamp(byte[] bits, int offset, int length, Calendar targetCalendar, TimeZone tz, boolean rollForward, MySQLConnection conn, ResultSetImpl rs) throws SQLException {
        int i = length;
        int year = 0;
        int month = 0;
        int day = 0;
        int hour = 0;
        int minute = 0;
        int seconds = 0;
        int nanos = 0;
        if (i != 0) {
            year = (bits[offset + 0] & 255) | ((bits[offset + 1] & 255) << 8);
            month = bits[offset + 2];
            day = bits[offset + 3];
            if (i > 4) {
                hour = bits[offset + 4];
                minute = bits[offset + 5];
                seconds = bits[offset + 6];
            }
            if (i > 7) {
                nanos = ((((bits[offset + 7] & 255) | ((bits[offset + 8] & 255) << 8)) | ((bits[offset + 9] & 255) << 16)) | ((bits[offset + 10] & 255) << 24)) * 1000;
            }
        }
        int hour2 = hour;
        int minute2 = minute;
        int seconds2 = seconds;
        int nanos2 = nanos;
        if (i != 0) {
            if (year != 0 || month != 0 || day != 0) {
                ResultSetRow resultSetRow = this;
                if (!rs.useLegacyDatetimeCode) {
                    return TimeUtil.fastTimestampCreate(tz, year, month, day, hour2, minute2, seconds2, nanos2);
                }
                Calendar sessionCalendar = conn.getUseJDBCCompliantTimezoneShift() ? conn.getUtcCalendar() : rs.getCalendarInstanceForSessionOrNew();
                return TimeUtil.changeTimezone(conn, sessionCalendar, targetCalendar, rs.fastTimestampCreate(sessionCalendar, year, month, day, hour2, minute2, seconds2, nanos2), conn.getServerTimezoneTZ(), tz, rollForward);
            }
        }
        if ("convertToNull".equals(conn.getZeroDateTimeBehavior())) {
            return null;
        }
        if ("exception".equals(conn.getZeroDateTimeBehavior())) {
            throw SQLError.createSQLException("Value '0000-00-00' can not be represented as java.sql.Timestamp", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, this.exceptionInterceptor);
        }
        resultSetRow = this;
        year = 1;
        month = 1;
        day = 1;
        if (!rs.useLegacyDatetimeCode) {
            return TimeUtil.fastTimestampCreate(tz, year, month, day, hour2, minute2, seconds2, nanos2);
        }
        if (conn.getUseJDBCCompliantTimezoneShift()) {
        }
        Calendar sessionCalendar2 = conn.getUseJDBCCompliantTimezoneShift() ? conn.getUtcCalendar() : rs.getCalendarInstanceForSessionOrNew();
        return TimeUtil.changeTimezone(conn, sessionCalendar2, targetCalendar, rs.fastTimestampCreate(sessionCalendar2, year, month, day, hour2, minute2, seconds2, nanos2), conn.getServerTimezoneTZ(), tz, rollForward);
    }

    protected String getString(String encoding, MySQLConnection conn, byte[] value, int offset, int length) throws SQLException {
        if (conn == null || !conn.getUseUnicode()) {
            return StringUtils.toAsciiString(value, offset, length);
        }
        String stringVal;
        if (encoding == null) {
            try {
                stringVal = StringUtils.toString(value);
            } catch (UnsupportedEncodingException e) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(Messages.getString("ResultSet.Unsupported_character_encoding____101"));
                stringBuilder.append(encoding);
                stringBuilder.append("'.");
                throw SQLError.createSQLException(stringBuilder.toString(), "0S100", this.exceptionInterceptor);
            }
        }
        UnsupportedEncodingException E = conn.getCharsetConverter(encoding);
        if (E != null) {
            stringVal = E.toString(value, offset, length);
        } else {
            stringVal = StringUtils.toString(value, offset, length, encoding);
        }
        return stringVal;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected java.sql.Time getTimeFast(int r28, byte[] r29, int r30, int r31, java.util.Calendar r32, java.util.TimeZone r33, boolean r34, com.mysql.jdbc.MySQLConnection r35, com.mysql.jdbc.ResultSetImpl r36) throws java.sql.SQLException {
        /*
        r27 = this;
        r1 = r27;
        r2 = r28;
        r3 = r29;
        r4 = r31;
        r12 = r32;
        r13 = r36;
        r5 = 0;
        r6 = 0;
        r7 = 0;
        r8 = 0;
        r9 = -1;
        r10 = 0;
        if (r3 != 0) goto L_0x0015;
    L_0x0014:
        return r10;
    L_0x0015:
        r11 = 1;
        r14 = 0;
        r16 = 0;
    L_0x0019:
        r17 = r16;
        r10 = r17;
        if (r10 >= r4) goto L_0x003f;
    L_0x001f:
        r16 = r30 + r10;
        r20 = r5;
        r5 = r3[r16];	 Catch:{ RuntimeException -> 0x0034 }
        r21 = r6;
        r6 = 58;
        if (r5 != r6) goto L_0x002d;
    L_0x002b:
        r14 = 1;
        goto L_0x0043;
    L_0x002d:
        r16 = r10 + 1;
        r5 = r20;
        r6 = r21;
        goto L_0x0019;
    L_0x0034:
        r0 = move-exception;
        r21 = r6;
        r4 = r0;
        r23 = r7;
        r17 = r8;
        r10 = r9;
        goto L_0x0367;
    L_0x003f:
        r20 = r5;
        r21 = r6;
    L_0x0043:
        r5 = 0;
    L_0x0044:
        r6 = 46;
        if (r5 >= r4) goto L_0x005c;
    L_0x0048:
        r10 = r30 + r5;
        r10 = r3[r10];	 Catch:{ RuntimeException -> 0x0053 }
        if (r10 != r6) goto L_0x0050;
    L_0x004e:
        r9 = r5;
        goto L_0x005c;
    L_0x0050:
        r5 = r5 + 1;
        goto L_0x0044;
    L_0x0053:
        r0 = move-exception;
        r4 = r0;
        r23 = r7;
        r17 = r8;
        r10 = r9;
        goto L_0x0367;
    L_0x005c:
        r10 = r9;
        r5 = 0;
    L_0x005e:
        if (r5 >= r4) goto L_0x0099;
    L_0x0060:
        r9 = r30 + r5;
        r9 = r3[r9];	 Catch:{ RuntimeException -> 0x0091 }
        r6 = 32;
        if (r9 == r6) goto L_0x0070;
    L_0x0068:
        r6 = 45;
        if (r9 == r6) goto L_0x0070;
    L_0x006c:
        r6 = 47;
        if (r9 != r6) goto L_0x0072;
    L_0x0070:
        r6 = 0;
        r14 = r6;
    L_0x0072:
        r6 = 48;
        if (r9 == r6) goto L_0x008c;
    L_0x0076:
        r6 = 32;
        if (r9 == r6) goto L_0x008c;
    L_0x007a:
        r6 = 58;
        if (r9 == r6) goto L_0x008c;
    L_0x007e:
        r6 = 45;
        if (r9 == r6) goto L_0x008c;
    L_0x0082:
        r6 = 47;
        if (r9 == r6) goto L_0x008c;
    L_0x0086:
        r6 = 46;
        if (r9 == r6) goto L_0x008e;
    L_0x008a:
        r11 = 0;
        goto L_0x0099;
    L_0x008c:
        r6 = 46;
    L_0x008e:
        r5 = r5 + 1;
        goto L_0x005e;
    L_0x0091:
        r0 = move-exception;
        r4 = r0;
        r23 = r7;
    L_0x0095:
        r17 = r8;
        goto L_0x0367;
    L_0x0099:
        r16 = r11;
        if (r14 != 0) goto L_0x00e2;
    L_0x009d:
        if (r16 == 0) goto L_0x00e2;
    L_0x009f:
        r5 = "convertToNull";
        r6 = r35.getZeroDateTimeBehavior();	 Catch:{ RuntimeException -> 0x0091 }
        r5 = r5.equals(r6);	 Catch:{ RuntimeException -> 0x0091 }
        if (r5 == 0) goto L_0x00ad;
    L_0x00ab:
        r5 = 0;
        return r5;
    L_0x00ad:
        r5 = "exception";
        r6 = r35.getZeroDateTimeBehavior();	 Catch:{ RuntimeException -> 0x0091 }
        r5 = r5.equals(r6);	 Catch:{ RuntimeException -> 0x0091 }
        if (r5 == 0) goto L_0x00dc;
    L_0x00b9:
        r5 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0091 }
        r5.<init>();	 Catch:{ RuntimeException -> 0x0091 }
        r6 = "Value '";
        r5.append(r6);	 Catch:{ RuntimeException -> 0x0091 }
        r6 = com.mysql.jdbc.StringUtils.toString(r29);	 Catch:{ RuntimeException -> 0x0091 }
        r5.append(r6);	 Catch:{ RuntimeException -> 0x0091 }
        r6 = "' can not be represented as java.sql.Time";
        r5.append(r6);	 Catch:{ RuntimeException -> 0x0091 }
        r5 = r5.toString();	 Catch:{ RuntimeException -> 0x0091 }
        r6 = "S1009";
        r9 = r1.exceptionInterceptor;	 Catch:{ RuntimeException -> 0x0091 }
        r5 = com.mysql.jdbc.SQLError.createSQLException(r5, r6, r9);	 Catch:{ RuntimeException -> 0x0091 }
        throw r5;	 Catch:{ RuntimeException -> 0x0091 }
    L_0x00dc:
        r5 = 0;
        r5 = r13.fastTimeCreate(r12, r5, r5, r5);	 Catch:{ RuntimeException -> 0x0091 }
        return r5;
    L_0x00e2:
        r5 = r1.metadata;	 Catch:{ RuntimeException -> 0x0360 }
        r5 = r5[r2];	 Catch:{ RuntimeException -> 0x0360 }
        r11 = r5;
        r5 = r4;
        r6 = -1;
        if (r10 == r6) goto L_0x0133;
    L_0x00eb:
        r5 = r10;
        r6 = r10 + 2;
        if (r6 > r4) goto L_0x0125;
    L_0x00f0:
        r6 = r30 + r10;
        r6 = r6 + 1;
        r9 = r30 + r4;
        r6 = com.mysql.jdbc.StringUtils.getInt(r3, r6, r9);	 Catch:{ RuntimeException -> 0x011d }
        r8 = r6;
        r6 = r10 + 1;
        r6 = r4 - r6;
        r9 = 9;
        if (r6 >= r9) goto L_0x0114;
    L_0x0103:
        r22 = r5;
        r4 = 4621819117588971520; // 0x4024000000000000 float:0.0 double:10.0;
        r9 = r9 - r6;
        r24 = r6;
        r23 = r7;
        r6 = (double) r9;
        r4 = java.lang.Math.pow(r4, r6);	 Catch:{ RuntimeException -> 0x012f }
        r4 = (int) r4;	 Catch:{ RuntimeException -> 0x012f }
        r8 = r8 * r4;
        goto L_0x0118;
    L_0x0114:
        r22 = r5;
        r23 = r7;
    L_0x0118:
        r17 = r8;
        r4 = r22;
        goto L_0x0138;
    L_0x011d:
        r0 = move-exception;
        r23 = r7;
        r4 = r0;
        r17 = r8;
        goto L_0x0367;
    L_0x0125:
        r22 = r5;
        r23 = r7;
        r4 = new java.lang.IllegalArgumentException;	 Catch:{ RuntimeException -> 0x012f }
        r4.<init>();	 Catch:{ RuntimeException -> 0x012f }
        throw r4;	 Catch:{ RuntimeException -> 0x012f }
    L_0x012f:
        r0 = move-exception;
        r4 = r0;
        goto L_0x0095;
    L_0x0133:
        r23 = r7;
        r4 = r5;
        r17 = r8;
    L_0x0138:
        r5 = r11.getMysqlType();	 Catch:{ RuntimeException -> 0x035c }
        r6 = 7;
        r7 = 12;
        r8 = 10;
        r9 = 8;
        if (r5 != r6) goto L_0x022c;
    L_0x0145:
        if (r4 == r8) goto L_0x01cf;
    L_0x0147:
        if (r4 == r7) goto L_0x01a4;
    L_0x0149:
        r5 = 14;
        if (r4 == r5) goto L_0x01a4;
    L_0x014d:
        r5 = 19;
        if (r4 == r5) goto L_0x0182;
    L_0x0151:
        r5 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x017e }
        r5.<init>();	 Catch:{ RuntimeException -> 0x017e }
        r6 = "ResultSet.Timestamp_too_small_to_convert_to_Time_value_in_column__257";
        r6 = com.mysql.jdbc.Messages.getString(r6);	 Catch:{ RuntimeException -> 0x017e }
        r5.append(r6);	 Catch:{ RuntimeException -> 0x017e }
        r6 = r2 + 1;
        r5.append(r6);	 Catch:{ RuntimeException -> 0x017e }
        r6 = "(";
        r5.append(r6);	 Catch:{ RuntimeException -> 0x017e }
        r5.append(r11);	 Catch:{ RuntimeException -> 0x017e }
        r6 = ").";
        r5.append(r6);	 Catch:{ RuntimeException -> 0x017e }
        r5 = r5.toString();	 Catch:{ RuntimeException -> 0x017e }
        r6 = "S1009";
        r7 = r1.exceptionInterceptor;	 Catch:{ RuntimeException -> 0x017e }
        r5 = com.mysql.jdbc.SQLError.createSQLException(r5, r6, r7);	 Catch:{ RuntimeException -> 0x017e }
        throw r5;	 Catch:{ RuntimeException -> 0x017e }
    L_0x017e:
        r0 = move-exception;
        r4 = r0;
        goto L_0x0367;
    L_0x0182:
        r5 = r30 + r4;
        r5 = r5 - r9;
        r6 = r30 + r4;
        r6 = r6 + -6;
        r5 = com.mysql.jdbc.StringUtils.getInt(r3, r5, r6);	 Catch:{ RuntimeException -> 0x017e }
        r6 = r30 + r4;
        r7 = 5;
        r6 = r6 - r7;
        r7 = r30 + r4;
        r7 = r7 + -3;
        r6 = com.mysql.jdbc.StringUtils.getInt(r3, r6, r7);	 Catch:{ RuntimeException -> 0x0226 }
        r7 = r30 + r4;
        r7 = r7 + -2;
        r8 = r30 + r4;
        r7 = com.mysql.jdbc.StringUtils.getInt(r3, r7, r8);	 Catch:{ RuntimeException -> 0x01c7 }
        goto L_0x01e1;
    L_0x01a4:
        r5 = r30 + r4;
        r5 = r5 + -6;
        r6 = r30 + r4;
        r6 = r6 + -4;
        r5 = com.mysql.jdbc.StringUtils.getInt(r3, r5, r6);	 Catch:{ RuntimeException -> 0x017e }
        r6 = r30 + r4;
        r6 = r6 + -4;
        r7 = r30 + r4;
        r7 = r7 + -2;
        r6 = com.mysql.jdbc.StringUtils.getInt(r3, r6, r7);	 Catch:{ RuntimeException -> 0x0226 }
        r7 = r30 + r4;
        r7 = r7 + -2;
        r8 = r30 + r4;
        r7 = com.mysql.jdbc.StringUtils.getInt(r3, r7, r8);	 Catch:{ RuntimeException -> 0x01c7 }
        goto L_0x01e1;
    L_0x01c7:
        r0 = move-exception;
        r4 = r0;
        r20 = r5;
        r21 = r6;
        goto L_0x0367;
    L_0x01cf:
        r5 = r30 + 6;
        r6 = r30 + 8;
        r5 = com.mysql.jdbc.StringUtils.getInt(r3, r5, r6);	 Catch:{ RuntimeException -> 0x017e }
        r6 = r30 + 8;
        r7 = r30 + 10;
        r6 = com.mysql.jdbc.StringUtils.getInt(r3, r6, r7);	 Catch:{ RuntimeException -> 0x0226 }
        r7 = 0;
    L_0x01e1:
        r8 = new java.sql.SQLWarning;	 Catch:{ RuntimeException -> 0x021a }
        r9 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x021a }
        r9.<init>();	 Catch:{ RuntimeException -> 0x021a }
        r25 = r5;
        r5 = "ResultSet.Precision_lost_converting_TIMESTAMP_to_Time_with_getTime()_on_column__261";
        r5 = com.mysql.jdbc.Messages.getString(r5);	 Catch:{ RuntimeException -> 0x0210 }
        r9.append(r5);	 Catch:{ RuntimeException -> 0x0210 }
        r9.append(r2);	 Catch:{ RuntimeException -> 0x0210 }
        r5 = "(";
        r9.append(r5);	 Catch:{ RuntimeException -> 0x0210 }
        r9.append(r11);	 Catch:{ RuntimeException -> 0x0210 }
        r5 = ").";
        r9.append(r5);	 Catch:{ RuntimeException -> 0x0210 }
        r5 = r9.toString();	 Catch:{ RuntimeException -> 0x0210 }
        r8.<init>(r5);	 Catch:{ RuntimeException -> 0x0210 }
        r8 = r6;
        r9 = r25;
        goto L_0x02ff;
    L_0x0210:
        r0 = move-exception;
        r4 = r0;
        r21 = r6;
        r23 = r7;
    L_0x0216:
        r20 = r25;
        goto L_0x0367;
    L_0x021a:
        r0 = move-exception;
        r25 = r5;
        r4 = r0;
        r21 = r6;
        r23 = r7;
        r20 = r25;
        goto L_0x0367;
    L_0x0226:
        r0 = move-exception;
        r4 = r0;
        r20 = r5;
        goto L_0x0367;
    L_0x022c:
        r5 = r11.getMysqlType();	 Catch:{ RuntimeException -> 0x035c }
        if (r5 != r7) goto L_0x029d;
    L_0x0232:
        r5 = r30 + 11;
        r6 = r30 + 13;
        r5 = com.mysql.jdbc.StringUtils.getInt(r3, r5, r6);	 Catch:{ RuntimeException -> 0x017e }
        r6 = r30 + 14;
        r7 = r30 + 16;
        r6 = com.mysql.jdbc.StringUtils.getInt(r3, r6, r7);	 Catch:{ RuntimeException -> 0x0295 }
        r7 = r30 + 17;
        r8 = r30 + 19;
        r7 = com.mysql.jdbc.StringUtils.getInt(r3, r7, r8);	 Catch:{ RuntimeException -> 0x028e }
        r8 = new java.sql.SQLWarning;	 Catch:{ RuntimeException -> 0x0285 }
        r9 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0285 }
        r9.<init>();	 Catch:{ RuntimeException -> 0x0285 }
        r26 = r5;
        r5 = "ResultSet.Precision_lost_converting_DATETIME_to_Time_with_getTime()_on_column__264";
        r5 = com.mysql.jdbc.Messages.getString(r5);	 Catch:{ RuntimeException -> 0x027b }
        r9.append(r5);	 Catch:{ RuntimeException -> 0x027b }
        r5 = r2 + 1;
        r9.append(r5);	 Catch:{ RuntimeException -> 0x027b }
        r5 = "(";
        r9.append(r5);	 Catch:{ RuntimeException -> 0x027b }
        r9.append(r11);	 Catch:{ RuntimeException -> 0x027b }
        r5 = ").";
        r9.append(r5);	 Catch:{ RuntimeException -> 0x027b }
        r5 = r9.toString();	 Catch:{ RuntimeException -> 0x027b }
        r8.<init>(r5);	 Catch:{ RuntimeException -> 0x027b }
        r8 = r6;
        r9 = r26;
        goto L_0x02ff;
    L_0x027b:
        r0 = move-exception;
        r4 = r0;
        r21 = r6;
        r23 = r7;
        r20 = r26;
        goto L_0x0367;
    L_0x0285:
        r0 = move-exception;
        r26 = r5;
        r4 = r0;
        r21 = r6;
        r23 = r7;
        goto L_0x0299;
    L_0x028e:
        r0 = move-exception;
        r26 = r5;
        r4 = r0;
        r21 = r6;
        goto L_0x0299;
    L_0x0295:
        r0 = move-exception;
        r26 = r5;
        r4 = r0;
    L_0x0299:
        r20 = r26;
        goto L_0x0367;
    L_0x029d:
        r5 = r11.getMysqlType();	 Catch:{ RuntimeException -> 0x035c }
        if (r5 != r8) goto L_0x02aa;
    L_0x02a3:
        r5 = 0;
        r6 = 0;
        r5 = r13.fastTimeCreate(r6, r5, r5, r5);	 Catch:{ RuntimeException -> 0x017e }
        return r5;
    L_0x02aa:
        r5 = 0;
        r6 = 5;
        if (r4 == r6) goto L_0x02e0;
    L_0x02ae:
        if (r4 == r9) goto L_0x02e0;
    L_0x02b0:
        r5 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x017e }
        r5.<init>();	 Catch:{ RuntimeException -> 0x017e }
        r6 = "ResultSet.Bad_format_for_Time____267";
        r6 = com.mysql.jdbc.Messages.getString(r6);	 Catch:{ RuntimeException -> 0x017e }
        r5.append(r6);	 Catch:{ RuntimeException -> 0x017e }
        r6 = com.mysql.jdbc.StringUtils.toString(r29);	 Catch:{ RuntimeException -> 0x017e }
        r5.append(r6);	 Catch:{ RuntimeException -> 0x017e }
        r6 = "ResultSet.___in_column__268";
        r6 = com.mysql.jdbc.Messages.getString(r6);	 Catch:{ RuntimeException -> 0x017e }
        r5.append(r6);	 Catch:{ RuntimeException -> 0x017e }
        r6 = r2 + 1;
        r5.append(r6);	 Catch:{ RuntimeException -> 0x017e }
        r5 = r5.toString();	 Catch:{ RuntimeException -> 0x017e }
        r6 = "S1009";
        r7 = r1.exceptionInterceptor;	 Catch:{ RuntimeException -> 0x017e }
        r5 = com.mysql.jdbc.SQLError.createSQLException(r5, r6, r7);	 Catch:{ RuntimeException -> 0x017e }
        throw r5;	 Catch:{ RuntimeException -> 0x017e }
    L_0x02e0:
        r6 = r30 + 0;
        r7 = r30 + 2;
        r6 = com.mysql.jdbc.StringUtils.getInt(r3, r6, r7);	 Catch:{ RuntimeException -> 0x035c }
        r7 = r30 + 3;
        r8 = r30 + 5;
        r7 = com.mysql.jdbc.StringUtils.getInt(r3, r7, r8);	 Catch:{ RuntimeException -> 0x0356 }
        r8 = 5;
        if (r4 != r8) goto L_0x02f4;
    L_0x02f3:
        goto L_0x02fc;
    L_0x02f4:
        r5 = r30 + 6;
        r8 = r30 + 8;
        r5 = com.mysql.jdbc.StringUtils.getInt(r3, r5, r8);	 Catch:{ RuntimeException -> 0x034e }
    L_0x02fc:
        r9 = r6;
        r8 = r7;
        r7 = r5;
    L_0x02ff:
        r5 = r36.getCalendarInstanceForSessionOrNew();	 Catch:{ RuntimeException -> 0x0340 }
        r6 = r5;
        r5 = r13.useLegacyDatetimeCode;	 Catch:{ RuntimeException -> 0x0340 }
        if (r5 != 0) goto L_0x0317;
    L_0x0308:
        r5 = r13.fastTimeCreate(r12, r9, r8, r7);	 Catch:{ RuntimeException -> 0x030d }
        return r5;
    L_0x030d:
        r0 = move-exception;
        r4 = r0;
        r23 = r7;
        r21 = r8;
        r20 = r9;
        goto L_0x0367;
    L_0x0317:
        r15 = r13.fastTimeCreate(r6, r9, r8, r7);	 Catch:{ RuntimeException -> 0x0340 }
        r18 = r35.getServerTimezoneTZ();	 Catch:{ RuntimeException -> 0x0340 }
        r5 = r35;
        r19 = r6;
        r20 = r7;
        r7 = r12;
        r21 = r8;
        r8 = r15;
        r25 = r9;
        r9 = r18;
        r15 = r10;
        r10 = r33;
        r18 = r11;
        r11 = r34;
        r5 = com.mysql.jdbc.TimeUtil.changeTimezone(r5, r6, r7, r8, r9, r10, r11);	 Catch:{ RuntimeException -> 0x0339 }
        return r5;
    L_0x0339:
        r0 = move-exception;
        r4 = r0;
        r10 = r15;
        r23 = r20;
        goto L_0x0216;
    L_0x0340:
        r0 = move-exception;
        r20 = r7;
        r21 = r8;
        r25 = r9;
        r15 = r10;
        r4 = r0;
        r23 = r20;
        r20 = r25;
        goto L_0x0367;
    L_0x034e:
        r0 = move-exception;
        r15 = r10;
        r4 = r0;
        r20 = r6;
        r21 = r7;
        goto L_0x035f;
    L_0x0356:
        r0 = move-exception;
        r15 = r10;
        r4 = r0;
        r20 = r6;
        goto L_0x035f;
    L_0x035c:
        r0 = move-exception;
        r15 = r10;
        r4 = r0;
    L_0x035f:
        goto L_0x0367;
    L_0x0360:
        r0 = move-exception;
        r23 = r7;
        r15 = r10;
        r4 = r0;
        r17 = r8;
    L_0x0367:
        r5 = r4.toString();
        r6 = "S1009";
        r7 = r1.exceptionInterceptor;
        r5 = com.mysql.jdbc.SQLError.createSQLException(r5, r6, r7);
        r5.initCause(r4);
        throw r5;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.ResultSetRow.getTimeFast(int, byte[], int, int, java.util.Calendar, java.util.TimeZone, boolean, com.mysql.jdbc.MySQLConnection, com.mysql.jdbc.ResultSetImpl):java.sql.Time");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected java.sql.Timestamp getTimestampFast(int r41, byte[] r42, int r43, int r44, java.util.Calendar r45, java.util.TimeZone r46, boolean r47, com.mysql.jdbc.MySQLConnection r48, com.mysql.jdbc.ResultSetImpl r49) throws java.sql.SQLException {
        /*
        r40 = this;
        r1 = r40;
        r2 = r41;
        r3 = r42;
        r4 = r43;
        r5 = r44;
        r15 = r49;
        r6 = r48.getUseJDBCCompliantTimezoneShift();	 Catch:{ RuntimeException -> 0x0419 }
        if (r6 == 0) goto L_0x001d;
    L_0x0012:
        r6 = r48.getUtcCalendar();	 Catch:{ RuntimeException -> 0x0017 }
        goto L_0x0021;
    L_0x0017:
        r0 = move-exception;
        r10 = r5;
        r14 = r15;
    L_0x001a:
        r5 = r0;
        goto L_0x041e;
    L_0x001d:
        r6 = r49.getCalendarInstanceForSessionOrNew();	 Catch:{ RuntimeException -> 0x0419 }
    L_0x0021:
        r16 = r6;
        r6 = 1;
        r7 = 0;
        r9 = 0;
    L_0x0026:
        r10 = 58;
        if (r9 >= r5) goto L_0x0035;
    L_0x002a:
        r11 = r4 + r9;
        r11 = r3[r11];	 Catch:{ RuntimeException -> 0x0017 }
        if (r11 != r10) goto L_0x0032;
    L_0x0030:
        r7 = 1;
        goto L_0x0035;
    L_0x0032:
        r9 = r9 + 1;
        goto L_0x0026;
    L_0x0035:
        r9 = r7;
        r7 = 0;
    L_0x0037:
        r11 = 45;
        if (r7 >= r5) goto L_0x0063;
    L_0x003b:
        r12 = r4 + r7;
        r12 = r3[r12];	 Catch:{ RuntimeException -> 0x0017 }
        r13 = 32;
        if (r12 == r13) goto L_0x0049;
    L_0x0043:
        if (r12 == r11) goto L_0x0049;
    L_0x0045:
        r13 = 47;
        if (r12 != r13) goto L_0x004a;
    L_0x0049:
        r9 = 0;
    L_0x004a:
        r13 = 48;
        if (r12 == r13) goto L_0x0060;
    L_0x004e:
        r13 = 32;
        if (r12 == r13) goto L_0x0060;
    L_0x0052:
        if (r12 == r10) goto L_0x0060;
    L_0x0054:
        if (r12 == r11) goto L_0x0060;
    L_0x0056:
        r13 = 47;
        if (r12 == r13) goto L_0x0060;
    L_0x005a:
        r13 = 46;
        if (r12 == r13) goto L_0x0060;
    L_0x005e:
        r6 = 0;
        goto L_0x0063;
    L_0x0060:
        r7 = r7 + 1;
        goto L_0x0037;
    L_0x0063:
        r25 = r6;
        r24 = r9;
        if (r24 != 0) goto L_0x00c8;
    L_0x0069:
        if (r25 == 0) goto L_0x00c8;
    L_0x006b:
        r6 = "convertToNull";
        r7 = r48.getZeroDateTimeBehavior();	 Catch:{ RuntimeException -> 0x0017 }
        r6 = r6.equals(r7);	 Catch:{ RuntimeException -> 0x0017 }
        if (r6 == 0) goto L_0x0079;
    L_0x0077:
        r6 = 0;
        return r6;
    L_0x0079:
        r6 = "exception";
        r7 = r48.getZeroDateTimeBehavior();	 Catch:{ RuntimeException -> 0x0017 }
        r6 = r6.equals(r7);	 Catch:{ RuntimeException -> 0x0017 }
        if (r6 == 0) goto L_0x00a8;
    L_0x0085:
        r6 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0017 }
        r6.<init>();	 Catch:{ RuntimeException -> 0x0017 }
        r7 = "Value '";
        r6.append(r7);	 Catch:{ RuntimeException -> 0x0017 }
        r7 = com.mysql.jdbc.StringUtils.toString(r42);	 Catch:{ RuntimeException -> 0x0017 }
        r6.append(r7);	 Catch:{ RuntimeException -> 0x0017 }
        r7 = "' can not be represented as java.sql.Timestamp";
        r6.append(r7);	 Catch:{ RuntimeException -> 0x0017 }
        r6 = r6.toString();	 Catch:{ RuntimeException -> 0x0017 }
        r7 = "S1009";
        r8 = r1.exceptionInterceptor;	 Catch:{ RuntimeException -> 0x0017 }
        r6 = com.mysql.jdbc.SQLError.createSQLException(r6, r7, r8);	 Catch:{ RuntimeException -> 0x0017 }
        throw r6;	 Catch:{ RuntimeException -> 0x0017 }
    L_0x00a8:
        r6 = r15.useLegacyDatetimeCode;	 Catch:{ RuntimeException -> 0x0017 }
        if (r6 != 0) goto L_0x00ba;
    L_0x00ac:
        r8 = 1;
        r9 = 1;
        r10 = 1;
        r11 = 0;
        r12 = 0;
        r13 = 0;
        r14 = 0;
        r7 = r46;
        r6 = com.mysql.jdbc.TimeUtil.fastTimestampCreate(r7, r8, r9, r10, r11, r12, r13, r14);	 Catch:{ RuntimeException -> 0x0017 }
        return r6;
    L_0x00ba:
        r7 = 0;
        r8 = 1;
        r9 = 1;
        r10 = 1;
        r11 = 0;
        r12 = 0;
        r13 = 0;
        r14 = 0;
        r6 = r15;
        r6 = r6.fastTimestampCreate(r7, r8, r9, r10, r11, r12, r13, r14);	 Catch:{ RuntimeException -> 0x0017 }
        return r6;
    L_0x00c8:
        r6 = r1.metadata;	 Catch:{ RuntimeException -> 0x0419 }
        r6 = r6[r2];	 Catch:{ RuntimeException -> 0x0419 }
        r6 = r6.getMysqlType();	 Catch:{ RuntimeException -> 0x0419 }
        r7 = 13;
        r9 = 4;
        if (r6 != r7) goto L_0x011d;
    L_0x00d5:
        r6 = r15.useLegacyDatetimeCode;	 Catch:{ RuntimeException -> 0x0118 }
        if (r6 != 0) goto L_0x00f0;
    L_0x00d9:
        r27 = com.mysql.jdbc.StringUtils.getInt(r3, r4, r9);	 Catch:{ RuntimeException -> 0x0017 }
        r28 = 1;
        r29 = 1;
        r30 = 0;
        r31 = 0;
        r32 = 0;
        r33 = 0;
        r26 = r46;
        r6 = com.mysql.jdbc.TimeUtil.fastTimestampCreate(r26, r27, r28, r29, r30, r31, r32, r33);	 Catch:{ RuntimeException -> 0x0017 }
        return r6;
    L_0x00f0:
        r17 = com.mysql.jdbc.StringUtils.getInt(r3, r4, r9);	 Catch:{ RuntimeException -> 0x0118 }
        r18 = 1;
        r19 = 1;
        r20 = 0;
        r21 = 0;
        r22 = 0;
        r23 = 0;
        r14 = r15;
        r9 = r15.fastTimestampCreate(r16, r17, r18, r19, r20, r21, r22, r23);	 Catch:{ RuntimeException -> 0x0143 }
        r10 = r48.getServerTimezoneTZ();	 Catch:{ RuntimeException -> 0x0143 }
        r6 = r48;
        r7 = r16;
        r8 = r45;
        r11 = r46;
        r12 = r47;
        r6 = com.mysql.jdbc.TimeUtil.changeTimezone(r6, r7, r8, r9, r10, r11, r12);	 Catch:{ RuntimeException -> 0x0143 }
        return r6;
    L_0x0118:
        r0 = move-exception;
        r14 = r15;
    L_0x011a:
        r10 = r5;
        goto L_0x001a;
    L_0x011d:
        r14 = r15;
        r6 = 0;
        r7 = 0;
        r12 = 0;
        r13 = 0;
        r15 = 0;
        r17 = 0;
        r18 = 0;
        r19 = -1;
        r20 = 0;
    L_0x012b:
        r34 = r20;
        r8 = r34;
        if (r8 >= r5) goto L_0x0145;
    L_0x0131:
        r20 = r4 + r8;
        r10 = r3[r20];	 Catch:{ RuntimeException -> 0x0143 }
        r11 = 46;
        if (r10 != r11) goto L_0x013c;
    L_0x0139:
        r19 = r8;
        goto L_0x0145;
    L_0x013c:
        r20 = r8 + 1;
        r10 = 58;
        r11 = 45;
        goto L_0x012b;
    L_0x0143:
        r0 = move-exception;
        goto L_0x011a;
    L_0x0145:
        r11 = r19;
        r8 = r4 + r5;
        r8 = r8 + -1;
        r10 = -1;
        if (r11 != r8) goto L_0x0154;
    L_0x014e:
        r5 = r5 + -1;
        r10 = r5;
        r36 = r6;
        goto L_0x0192;
    L_0x0154:
        if (r11 == r10) goto L_0x018e;
    L_0x0156:
        r8 = r11 + 2;
        if (r8 > r5) goto L_0x0186;
    L_0x015a:
        r8 = r4 + r11;
        r8 = r8 + 1;
        r10 = r4 + r5;
        r8 = com.mysql.jdbc.StringUtils.getInt(r3, r8, r10);	 Catch:{ RuntimeException -> 0x0183 }
        r10 = r11 + 1;
        r10 = r5 - r10;
        r9 = 9;
        if (r10 >= r9) goto L_0x017c;
    L_0x016c:
        r36 = r6;
        r5 = 4621819117588971520; // 0x4024000000000000 float:0.0 double:10.0;
        r9 = 9 - r10;
        r38 = r10;
        r9 = (double) r9;	 Catch:{ RuntimeException -> 0x0183 }
        r5 = java.lang.Math.pow(r5, r9);	 Catch:{ RuntimeException -> 0x0183 }
        r5 = (int) r5;	 Catch:{ RuntimeException -> 0x0183 }
        r8 = r8 * r5;
        goto L_0x017e;
    L_0x017c:
        r36 = r6;
    L_0x017e:
        r5 = r11;
        r10 = r5;
        r18 = r8;
        goto L_0x0192;
    L_0x0183:
        r0 = move-exception;
        goto L_0x041b;
    L_0x0186:
        r36 = r6;
        r5 = new java.lang.IllegalArgumentException;	 Catch:{ RuntimeException -> 0x0183 }
        r5.<init>();	 Catch:{ RuntimeException -> 0x0183 }
        throw r5;	 Catch:{ RuntimeException -> 0x0183 }
    L_0x018e:
        r36 = r6;
        r10 = r44;
    L_0x0192:
        r5 = 69;
        r6 = 2;
        if (r10 == r6) goto L_0x03ad;
    L_0x0197:
        r6 = 4;
        if (r10 == r6) goto L_0x0395;
    L_0x019a:
        r6 = 6;
        if (r10 == r6) goto L_0x0374;
    L_0x019d:
        r6 = 8;
        if (r10 == r6) goto L_0x030a;
    L_0x01a1:
        r6 = 10;
        if (r10 == r6) goto L_0x028d;
    L_0x01a5:
        r6 = 12;
        if (r10 == r6) goto L_0x0252;
    L_0x01a9:
        r5 = 14;
        if (r10 == r5) goto L_0x0214;
    L_0x01ad:
        r5 = 29;
        if (r10 == r5) goto L_0x01e3;
    L_0x01b1:
        switch(r10) {
            case 19: goto L_0x01e3;
            case 20: goto L_0x01e3;
            case 21: goto L_0x01e3;
            case 22: goto L_0x01e3;
            case 23: goto L_0x01e3;
            case 24: goto L_0x01e3;
            case 25: goto L_0x01e3;
            case 26: goto L_0x01e3;
            default: goto L_0x01b4;
        };
    L_0x01b4:
        r5 = new java.sql.SQLException;	 Catch:{ RuntimeException -> 0x01e0 }
        r6 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x01e0 }
        r6.<init>();	 Catch:{ RuntimeException -> 0x01e0 }
        r8 = "Bad format for Timestamp '";
        r6.append(r8);	 Catch:{ RuntimeException -> 0x01e0 }
        r8 = com.mysql.jdbc.StringUtils.toString(r42);	 Catch:{ RuntimeException -> 0x01e0 }
        r6.append(r8);	 Catch:{ RuntimeException -> 0x01e0 }
        r8 = "' in column ";
        r6.append(r8);	 Catch:{ RuntimeException -> 0x01e0 }
        r8 = r2 + 1;
        r6.append(r8);	 Catch:{ RuntimeException -> 0x01e0 }
        r8 = ".";
        r6.append(r8);	 Catch:{ RuntimeException -> 0x01e0 }
        r6 = r6.toString();	 Catch:{ RuntimeException -> 0x01e0 }
        r8 = "S1009";
        r5.<init>(r6, r8);	 Catch:{ RuntimeException -> 0x01e0 }
        throw r5;	 Catch:{ RuntimeException -> 0x01e0 }
    L_0x01e0:
        r0 = move-exception;
        goto L_0x001a;
    L_0x01e3:
        r5 = r4 + 0;
        r6 = r4 + 4;
        r5 = com.mysql.jdbc.StringUtils.getInt(r3, r5, r6);	 Catch:{ RuntimeException -> 0x01e0 }
        r6 = r4 + 5;
        r8 = r4 + 7;
        r6 = com.mysql.jdbc.StringUtils.getInt(r3, r6, r8);	 Catch:{ RuntimeException -> 0x01e0 }
        r7 = r4 + 8;
        r8 = r4 + 10;
        r7 = com.mysql.jdbc.StringUtils.getInt(r3, r7, r8);	 Catch:{ RuntimeException -> 0x01e0 }
        r8 = r4 + 11;
        r9 = r4 + 13;
        r8 = com.mysql.jdbc.StringUtils.getInt(r3, r8, r9);	 Catch:{ RuntimeException -> 0x01e0 }
        r9 = r4 + 14;
        r12 = r4 + 16;
        r9 = com.mysql.jdbc.StringUtils.getInt(r3, r9, r12);	 Catch:{ RuntimeException -> 0x01e0 }
        r12 = r4 + 17;
        r13 = r4 + 19;
        r12 = com.mysql.jdbc.StringUtils.getInt(r3, r12, r13);	 Catch:{ RuntimeException -> 0x01e0 }
        goto L_0x0245;
    L_0x0214:
        r5 = r4 + 0;
        r6 = r4 + 4;
        r5 = com.mysql.jdbc.StringUtils.getInt(r3, r5, r6);	 Catch:{ RuntimeException -> 0x01e0 }
        r6 = r4 + 4;
        r8 = r4 + 6;
        r6 = com.mysql.jdbc.StringUtils.getInt(r3, r6, r8);	 Catch:{ RuntimeException -> 0x01e0 }
        r7 = r4 + 6;
        r8 = r4 + 8;
        r7 = com.mysql.jdbc.StringUtils.getInt(r3, r7, r8);	 Catch:{ RuntimeException -> 0x01e0 }
        r8 = r4 + 8;
        r9 = r4 + 10;
        r8 = com.mysql.jdbc.StringUtils.getInt(r3, r8, r9);	 Catch:{ RuntimeException -> 0x01e0 }
        r9 = r4 + 10;
        r12 = r4 + 12;
        r9 = com.mysql.jdbc.StringUtils.getInt(r3, r9, r12);	 Catch:{ RuntimeException -> 0x01e0 }
        r12 = r4 + 12;
        r13 = r4 + 14;
        r12 = com.mysql.jdbc.StringUtils.getInt(r3, r12, r13);	 Catch:{ RuntimeException -> 0x01e0 }
    L_0x0245:
        r15 = r5;
        r17 = r6;
    L_0x0248:
        r19 = r7;
        r20 = r8;
        r21 = r9;
        r22 = r12;
        goto L_0x03cb;
    L_0x0252:
        r6 = r4 + 0;
        r8 = r4 + 2;
        r6 = com.mysql.jdbc.StringUtils.getInt(r3, r6, r8);	 Catch:{ RuntimeException -> 0x01e0 }
        if (r6 > r5) goto L_0x025e;
    L_0x025c:
        r6 = r6 + 100;
    L_0x025e:
        r6 = r6 + 1900;
        r5 = r4 + 2;
        r8 = r4 + 4;
        r5 = com.mysql.jdbc.StringUtils.getInt(r3, r5, r8);	 Catch:{ RuntimeException -> 0x01e0 }
        r7 = r4 + 4;
        r8 = r4 + 6;
        r7 = com.mysql.jdbc.StringUtils.getInt(r3, r7, r8);	 Catch:{ RuntimeException -> 0x01e0 }
        r8 = r4 + 6;
        r9 = r4 + 8;
        r8 = com.mysql.jdbc.StringUtils.getInt(r3, r8, r9);	 Catch:{ RuntimeException -> 0x01e0 }
        r9 = r4 + 8;
        r12 = r4 + 10;
        r9 = com.mysql.jdbc.StringUtils.getInt(r3, r9, r12);	 Catch:{ RuntimeException -> 0x01e0 }
        r12 = r4 + 10;
        r13 = r4 + 12;
        r12 = com.mysql.jdbc.StringUtils.getInt(r3, r12, r13);	 Catch:{ RuntimeException -> 0x01e0 }
        r17 = r5;
        r15 = r6;
        goto L_0x0248;
    L_0x028d:
        r8 = 0;
        r35 = 0;
    L_0x0290:
        r9 = r35;
        if (r9 >= r10) goto L_0x02a5;
    L_0x0294:
        r19 = r4 + r9;
        r5 = r3[r19];	 Catch:{ RuntimeException -> 0x01e0 }
        r6 = 45;
        if (r5 != r6) goto L_0x029e;
    L_0x029c:
        r8 = 1;
        goto L_0x02a5;
    L_0x029e:
        r35 = r9 + 1;
        r5 = 69;
        r6 = 10;
        goto L_0x0290;
    L_0x02a5:
        r5 = r1.metadata;	 Catch:{ RuntimeException -> 0x01e0 }
        r5 = r5[r2];	 Catch:{ RuntimeException -> 0x01e0 }
        r5 = r5.getMysqlType();	 Catch:{ RuntimeException -> 0x01e0 }
        r6 = 10;
        if (r5 == r6) goto L_0x02e5;
    L_0x02b1:
        if (r8 == 0) goto L_0x02b4;
    L_0x02b3:
        goto L_0x02e5;
    L_0x02b4:
        r5 = r4 + 0;
        r6 = r4 + 2;
        r5 = com.mysql.jdbc.StringUtils.getInt(r3, r5, r6);	 Catch:{ RuntimeException -> 0x01e0 }
        r6 = 69;
        if (r5 > r6) goto L_0x02c2;
    L_0x02c0:
        r5 = r5 + 100;
    L_0x02c2:
        r6 = r4 + 2;
        r9 = r4 + 4;
        r6 = com.mysql.jdbc.StringUtils.getInt(r3, r6, r9);	 Catch:{ RuntimeException -> 0x01e0 }
        r7 = r4 + 4;
        r9 = r4 + 6;
        r7 = com.mysql.jdbc.StringUtils.getInt(r3, r7, r9);	 Catch:{ RuntimeException -> 0x01e0 }
        r9 = r4 + 6;
        r12 = r4 + 8;
        r9 = com.mysql.jdbc.StringUtils.getInt(r3, r9, r12);	 Catch:{ RuntimeException -> 0x01e0 }
        r12 = r4 + 8;
        r13 = r4 + 10;
        r12 = com.mysql.jdbc.StringUtils.getInt(r3, r12, r13);	 Catch:{ RuntimeException -> 0x01e0 }
        r5 = r5 + 1900;
        goto L_0x02ff;
    L_0x02e5:
        r5 = r4 + 0;
        r6 = r4 + 4;
        r5 = com.mysql.jdbc.StringUtils.getInt(r3, r5, r6);	 Catch:{ RuntimeException -> 0x01e0 }
        r6 = r4 + 5;
        r9 = r4 + 7;
        r6 = com.mysql.jdbc.StringUtils.getInt(r3, r6, r9);	 Catch:{ RuntimeException -> 0x01e0 }
        r7 = r4 + 8;
        r9 = r4 + 10;
        r7 = com.mysql.jdbc.StringUtils.getInt(r3, r7, r9);	 Catch:{ RuntimeException -> 0x01e0 }
        r9 = 0;
        r12 = 0;
    L_0x02ff:
        r15 = r5;
        r19 = r7;
        r20 = r9;
        r21 = r12;
        r22 = r17;
        goto L_0x03c9;
    L_0x030a:
        r5 = 0;
        r35 = 0;
    L_0x030d:
        r6 = r35;
        if (r6 >= r10) goto L_0x031e;
    L_0x0311:
        r8 = r4 + r6;
        r8 = r3[r8];	 Catch:{ RuntimeException -> 0x01e0 }
        r9 = 58;
        if (r8 != r9) goto L_0x031b;
    L_0x0319:
        r5 = 1;
        goto L_0x031e;
    L_0x031b:
        r35 = r6 + 1;
        goto L_0x030d;
    L_0x031e:
        if (r5 == 0) goto L_0x034a;
    L_0x0320:
        r6 = r4 + 0;
        r8 = r4 + 2;
        r6 = com.mysql.jdbc.StringUtils.getInt(r3, r6, r8);	 Catch:{ RuntimeException -> 0x01e0 }
        r8 = r4 + 3;
        r9 = r4 + 5;
        r8 = com.mysql.jdbc.StringUtils.getInt(r3, r8, r9);	 Catch:{ RuntimeException -> 0x01e0 }
        r9 = r4 + 6;
        r13 = r4 + 8;
        r9 = com.mysql.jdbc.StringUtils.getInt(r3, r9, r13);	 Catch:{ RuntimeException -> 0x01e0 }
        r13 = 1970; // 0x7b2 float:2.76E-42 double:9.733E-321;
        r7 = 1;
        r12 = 1;
        r20 = r6;
        r17 = r7;
        r21 = r8;
        r22 = r9;
        r19 = r12;
        r15 = r13;
        goto L_0x03cb;
    L_0x034a:
        r6 = r4 + 0;
        r8 = r4 + 4;
        r6 = com.mysql.jdbc.StringUtils.getInt(r3, r6, r8);	 Catch:{ RuntimeException -> 0x01e0 }
        r8 = r4 + 4;
        r9 = r4 + 6;
        r8 = com.mysql.jdbc.StringUtils.getInt(r3, r8, r9);	 Catch:{ RuntimeException -> 0x01e0 }
        r7 = r8;
        r8 = r4 + 6;
        r9 = r4 + 8;
        r8 = com.mysql.jdbc.StringUtils.getInt(r3, r8, r9);	 Catch:{ RuntimeException -> 0x01e0 }
        r6 = r6 + -1900;
        r9 = -1;
        r7 = r7 + r9;
        r19 = r8;
        r20 = r13;
        r21 = r15;
        r22 = r17;
        r15 = r6;
        r17 = r7;
        goto L_0x03cb;
    L_0x0374:
        r5 = r4 + 0;
        r6 = r4 + 2;
        r5 = com.mysql.jdbc.StringUtils.getInt(r3, r5, r6);	 Catch:{ RuntimeException -> 0x01e0 }
        r6 = 69;
        if (r5 > r6) goto L_0x0382;
    L_0x0380:
        r5 = r5 + 100;
    L_0x0382:
        r5 = r5 + 1900;
        r6 = r4 + 2;
        r8 = r4 + 4;
        r6 = com.mysql.jdbc.StringUtils.getInt(r3, r6, r8);	 Catch:{ RuntimeException -> 0x01e0 }
        r7 = r4 + 4;
        r8 = r4 + 6;
        r7 = com.mysql.jdbc.StringUtils.getInt(r3, r7, r8);	 Catch:{ RuntimeException -> 0x01e0 }
        goto L_0x03c0;
    L_0x0395:
        r5 = r4 + 0;
        r6 = r4 + 2;
        r5 = com.mysql.jdbc.StringUtils.getInt(r3, r5, r6);	 Catch:{ RuntimeException -> 0x01e0 }
        r6 = 69;
        if (r5 > r6) goto L_0x03a3;
    L_0x03a1:
        r5 = r5 + 100;
    L_0x03a3:
        r6 = r4 + 2;
        r8 = r4 + 4;
        r6 = com.mysql.jdbc.StringUtils.getInt(r3, r6, r8);	 Catch:{ RuntimeException -> 0x01e0 }
        r7 = 1;
        goto L_0x03c0;
    L_0x03ad:
        r5 = r4 + 0;
        r6 = r4 + 2;
        r5 = com.mysql.jdbc.StringUtils.getInt(r3, r5, r6);	 Catch:{ RuntimeException -> 0x0414 }
        r6 = 69;
        if (r5 > r6) goto L_0x03bb;
    L_0x03b9:
        r5 = r5 + 100;
    L_0x03bb:
        r5 = r5 + 1900;
        r6 = 1;
        r7 = 1;
    L_0x03c0:
        r19 = r7;
        r20 = r13;
        r21 = r15;
        r22 = r17;
        r15 = r5;
    L_0x03c9:
        r17 = r6;
    L_0x03cb:
        r5 = r14.useLegacyDatetimeCode;	 Catch:{ RuntimeException -> 0x0414 }
        if (r5 != 0) goto L_0x03e4;
    L_0x03cf:
        r26 = r46;
        r27 = r15;
        r28 = r17;
        r29 = r19;
        r30 = r20;
        r31 = r21;
        r32 = r22;
        r33 = r18;
        r5 = com.mysql.jdbc.TimeUtil.fastTimestampCreate(r26, r27, r28, r29, r30, r31, r32, r33);	 Catch:{ RuntimeException -> 0x01e0 }
        return r5;
    L_0x03e4:
        r5 = r14;
        r6 = r16;
        r7 = r15;
        r8 = r17;
        r9 = r19;
        r37 = r10;
        r10 = r20;
        r23 = r11;
        r11 = r21;
        r12 = r22;
        r13 = r18;
        r29 = r5.fastTimestampCreate(r6, r7, r8, r9, r10, r11, r12, r13);	 Catch:{ RuntimeException -> 0x040f }
        r30 = r48.getServerTimezoneTZ();	 Catch:{ RuntimeException -> 0x040f }
        r26 = r48;
        r27 = r16;
        r28 = r45;
        r31 = r46;
        r32 = r47;
        r5 = com.mysql.jdbc.TimeUtil.changeTimezone(r26, r27, r28, r29, r30, r31, r32);	 Catch:{ RuntimeException -> 0x040f }
        return r5;
    L_0x040f:
        r0 = move-exception;
        r5 = r0;
        r10 = r37;
        goto L_0x041e;
    L_0x0414:
        r0 = move-exception;
        r37 = r10;
        r5 = r0;
        goto L_0x041e;
    L_0x0419:
        r0 = move-exception;
        r14 = r15;
    L_0x041b:
        r5 = r0;
        r10 = r44;
    L_0x041e:
        r6 = new java.lang.StringBuilder;
        r6.<init>();
        r7 = "Cannot convert value '";
        r6.append(r7);
        r7 = "ISO8859_1";
        r8 = r48;
        r7 = r1.getString(r2, r7, r8);
        r6.append(r7);
        r7 = "' from column ";
        r6.append(r7);
        r7 = r2 + 1;
        r6.append(r7);
        r7 = " to TIMESTAMP.";
        r6.append(r7);
        r6 = r6.toString();
        r7 = "S1009";
        r9 = r1.exceptionInterceptor;
        r6 = com.mysql.jdbc.SQLError.createSQLException(r6, r7, r9);
        r6.initCause(r5);
        throw r6;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.ResultSetRow.getTimestampFast(int, byte[], int, int, java.util.Calendar, java.util.TimeZone, boolean, com.mysql.jdbc.MySQLConnection, com.mysql.jdbc.ResultSetImpl):java.sql.Timestamp");
    }

    public ResultSetRow setMetadata(Field[] f) throws SQLException {
        this.metadata = f;
        return this;
    }
}
