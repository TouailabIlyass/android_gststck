package com.mysql.jdbc;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.TimeZone;

public class TimeUtil {
    private static final TimeZone DEFAULT_TIMEZONE = TimeZone.getDefault();
    static final TimeZone GMT_TIMEZONE = TimeZone.getTimeZone("GMT");
    private static final String TIME_ZONE_MAPPINGS_RESOURCE = "/com/mysql/jdbc/TimeZoneMapping.properties";
    protected static final Method systemNanoTimeMethod;
    private static Properties timeZoneMappings = null;

    static {
        Method aMethod;
        try {
            aMethod = System.class.getMethod("nanoTime", (Class[]) null);
        } catch (SecurityException e) {
            aMethod = null;
        } catch (NoSuchMethodException e2) {
            aMethod = null;
        }
        systemNanoTimeMethod = aMethod;
    }

    public static boolean nanoTimeAvailable() {
        return systemNanoTimeMethod != null;
    }

    public static final TimeZone getDefaultTimeZone(boolean useCache) {
        return (TimeZone) (useCache ? DEFAULT_TIMEZONE : TimeZone.getDefault()).clone();
    }

    public static long getCurrentTimeNanosOrMillis() {
        if (systemNanoTimeMethod != null) {
            try {
                return ((Long) systemNanoTimeMethod.invoke(null, (Object[]) null)).longValue();
            } catch (IllegalArgumentException e) {
                return System.currentTimeMillis();
            } catch (IllegalAccessException e2) {
                return System.currentTimeMillis();
            } catch (InvocationTargetException e3) {
            }
        }
        return System.currentTimeMillis();
    }

    public static Time changeTimezone(MySQLConnection conn, Calendar sessionCalendar, Calendar targetCalendar, Time t, TimeZone fromTz, TimeZone toTz, boolean rollForward) {
        Date date = t;
        if (conn != null) {
            if (conn.getUseTimezone() && !conn.getNoTimezoneConversionForTimeType()) {
                long toTime;
                Calendar fromCal = Calendar.getInstance(fromTz);
                fromCal.setTime(date);
                int fromOffset = fromCal.get(15) + fromCal.get(16);
                Calendar toCal = Calendar.getInstance(toTz);
                toCal.setTime(date);
                int offsetDiff = fromOffset - (toCal.get(15) + toCal.get(16));
                long toTime2 = toCal.getTime().getTime();
                if (rollForward) {
                    toTime = toTime2 + ((long) offsetDiff);
                } else {
                    toTime = toTime2 - ((long) offsetDiff);
                }
                return new Time(toTime);
            } else if (conn.getUseJDBCCompliantTimezoneShift() && targetCalendar != null) {
                return new Time(jdbcCompliantZoneShift(sessionCalendar, targetCalendar, date));
            }
        }
        return date;
    }

    public static Timestamp changeTimezone(MySQLConnection conn, Calendar sessionCalendar, Calendar targetCalendar, Timestamp tstamp, TimeZone fromTz, TimeZone toTz, boolean rollForward) {
        Date date = tstamp;
        if (conn != null) {
            if (conn.getUseTimezone()) {
                long toTime;
                Calendar fromCal = Calendar.getInstance(fromTz);
                fromCal.setTime(date);
                int fromOffset = fromCal.get(15) + fromCal.get(16);
                Calendar toCal = Calendar.getInstance(toTz);
                toCal.setTime(date);
                int offsetDiff = fromOffset - (toCal.get(15) + toCal.get(16));
                long toTime2 = toCal.getTime().getTime();
                if (rollForward) {
                    toTime = toTime2 + ((long) offsetDiff);
                } else {
                    toTime = toTime2 - ((long) offsetDiff);
                }
                return new Timestamp(toTime);
            } else if (conn.getUseJDBCCompliantTimezoneShift() && targetCalendar != null) {
                Timestamp adjustedTimestamp = new Timestamp(jdbcCompliantZoneShift(sessionCalendar, targetCalendar, date));
                adjustedTimestamp.setNanos(date.getNanos());
                return adjustedTimestamp;
            }
        }
        return date;
    }

    private static long jdbcCompliantZoneShift(Calendar sessionCalendar, Calendar targetCalendar, Date dt) {
        Throwable th;
        Calendar calendar;
        if (sessionCalendar == null) {
            sessionCalendar = new GregorianCalendar();
        }
        synchronized (sessionCalendar) {
            try {
                long time;
                Calendar sessionCalendar2;
                Date origCalDate = targetCalendar.getTime();
                Date origSessionDate = sessionCalendar.getTime();
                try {
                    sessionCalendar.setTime(dt);
                    targetCalendar.set(1, sessionCalendar.get(1));
                    targetCalendar.set(2, sessionCalendar.get(2));
                    targetCalendar.set(5, sessionCalendar.get(5));
                    targetCalendar.set(11, sessionCalendar.get(11));
                    targetCalendar.set(12, sessionCalendar.get(12));
                    targetCalendar.set(13, sessionCalendar.get(13));
                    targetCalendar.set(14, sessionCalendar.get(14));
                    time = targetCalendar.getTime().getTime();
                    sessionCalendar2 = sessionCalendar;
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
                try {
                    sessionCalendar2.setTime(origSessionDate);
                    targetCalendar.setTime(origCalDate);
                    return time;
                } catch (Throwable th3) {
                    th = th3;
                    calendar = sessionCalendar2;
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                calendar = sessionCalendar;
                throw th;
            }
        }
    }

    static final java.sql.Date fastDateCreate(boolean useGmtConversion, Calendar gmtCalIfNeeded, Calendar cal, int year, int month, int day) {
        Calendar dateCal;
        Throwable th;
        Calendar calendar;
        Calendar dateCal2 = cal;
        if (useGmtConversion) {
            if (gmtCalIfNeeded == null) {
                gmtCalIfNeeded = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            }
            dateCal2 = gmtCalIfNeeded;
        }
        synchronized (dateCal2) {
            try {
                java.sql.Date date;
                Date origCalDate = dateCal2.getTime();
                try {
                    dateCal2.clear();
                    dateCal2.set(14, 0);
                    dateCal2.set(year, month - 1, day, 0, 0, 0);
                    date = new java.sql.Date(dateCal2.getTimeInMillis());
                    dateCal = dateCal2;
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
                try {
                    dateCal.setTime(origCalDate);
                    return date;
                } catch (Throwable th3) {
                    th = th3;
                    calendar = dateCal;
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                calendar = dateCal2;
                throw th;
            }
        }
    }

    static final java.sql.Date fastDateCreate(int year, int month, int day, Calendar targetCalendar) {
        Throwable th;
        Calendar dateCal = targetCalendar == null ? new GregorianCalendar() : targetCalendar;
        synchronized (dateCal) {
            try {
                Date origCalDate = dateCal.getTime();
                try {
                    dateCal.clear();
                    dateCal.set(year, month - 1, day, 0, 0, 0);
                    dateCal.set(14, 0);
                    java.sql.Date date = new java.sql.Date(dateCal.getTimeInMillis());
                    dateCal.setTime(origCalDate);
                    return date;
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                Calendar calendar = dateCal;
                throw th;
            }
        }
    }

    static final Time fastTimeCreate(Calendar cal, int hour, int minute, int second, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        Throwable th;
        StringBuilder stringBuilder;
        if (hour >= 0) {
            if (hour <= 24) {
                if (minute >= 0) {
                    if (minute <= 59) {
                        if (second >= 0) {
                            if (second <= 59) {
                                synchronized (cal) {
                                    try {
                                        Date origCalDate = cal.getTime();
                                        try {
                                            cal.clear();
                                            cal.set(1970, 0, 1, hour, minute, second);
                                            Time time = new Time(cal.getTimeInMillis());
                                            cal.setTime(origCalDate);
                                            return time;
                                        } catch (Throwable th2) {
                                            th = th2;
                                            throw th;
                                        }
                                    } catch (Throwable th3) {
                                        th = th3;
                                        Calendar calendar = cal;
                                        throw th;
                                    }
                                }
                            }
                        }
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("Illegal minute value '");
                        stringBuilder.append(second);
                        stringBuilder.append("' for java.sql.Time type in value '");
                        stringBuilder.append(timeFormattedString(hour, minute, second));
                        stringBuilder.append(".");
                        throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, exceptionInterceptor);
                    }
                }
                stringBuilder = new StringBuilder();
                stringBuilder.append("Illegal minute value '");
                stringBuilder.append(minute);
                stringBuilder.append("' for java.sql.Time type in value '");
                stringBuilder.append(timeFormattedString(hour, minute, second));
                stringBuilder.append(".");
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, exceptionInterceptor);
            }
        }
        stringBuilder = new StringBuilder();
        stringBuilder.append("Illegal hour value '");
        stringBuilder.append(hour);
        stringBuilder.append("' for java.sql.Time type in value '");
        stringBuilder.append(timeFormattedString(hour, minute, second));
        stringBuilder.append(".");
        throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, exceptionInterceptor);
    }

    static final Time fastTimeCreate(int hour, int minute, int second, Calendar targetCalendar, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        Throwable th;
        StringBuilder stringBuilder;
        if (hour >= 0) {
            if (hour <= 23) {
                if (minute >= 0) {
                    if (minute <= 59) {
                        if (second >= 0) {
                            if (second <= 59) {
                                Calendar cal = targetCalendar == null ? new GregorianCalendar() : targetCalendar;
                                synchronized (cal) {
                                    try {
                                        Date origCalDate = cal.getTime();
                                        try {
                                            cal.clear();
                                            cal.set(1970, 0, 1, hour, minute, second);
                                            Time time = new Time(cal.getTimeInMillis());
                                            cal.setTime(origCalDate);
                                            return time;
                                        } catch (Throwable th2) {
                                            th = th2;
                                            throw th;
                                        }
                                    } catch (Throwable th3) {
                                        th = th3;
                                        Calendar calendar = cal;
                                        throw th;
                                    }
                                }
                            }
                        }
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("Illegal minute value '");
                        stringBuilder.append(second);
                        stringBuilder.append("' for java.sql.Time type in value '");
                        stringBuilder.append(timeFormattedString(hour, minute, second));
                        stringBuilder.append(".");
                        throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, exceptionInterceptor);
                    }
                }
                stringBuilder = new StringBuilder();
                stringBuilder.append("Illegal minute value '");
                stringBuilder.append(minute);
                stringBuilder.append("' for java.sql.Time type in value '");
                stringBuilder.append(timeFormattedString(hour, minute, second));
                stringBuilder.append(".");
                throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, exceptionInterceptor);
            }
        }
        stringBuilder = new StringBuilder();
        stringBuilder.append("Illegal hour value '");
        stringBuilder.append(hour);
        stringBuilder.append("' for java.sql.Time type in value '");
        stringBuilder.append(timeFormattedString(hour, minute, second));
        stringBuilder.append(".");
        throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, exceptionInterceptor);
    }

    static final Timestamp fastTimestampCreate(boolean useGmtConversion, Calendar gmtCalIfNeeded, Calendar cal, int year, int month, int day, int hour, int minute, int seconds, int secondsPart) {
        Throwable th;
        Throwable th2;
        Date origCalDate;
        boolean z;
        Calendar gmtCalIfNeeded2;
        int year2;
        int month2;
        int hour2;
        int minute2;
        int i;
        int i2;
        int day2;
        Calendar calendar;
        Calendar calendar2 = cal;
        int secondsPart2 = secondsPart;
        synchronized (cal) {
            try {
                Date origCalDate2 = cal.getTime();
                Calendar instance;
                try {
                    cal.clear();
                    calendar2.set(year, month - 1, day, hour, minute, seconds);
                    int offsetDiff = 0;
                    if (useGmtConversion) {
                        int fromOffset = calendar2.get(15) + calendar2.get(16);
                        if (gmtCalIfNeeded == null) {
                            instance = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                        } else {
                            instance = gmtCalIfNeeded;
                        }
                        try {
                            instance.clear();
                            instance.setTimeInMillis(cal.getTimeInMillis());
                            offsetDiff = fromOffset - (instance.get(15) + instance.get(16));
                        } catch (Throwable th3) {
                            th = th3;
                            th2 = th;
                            origCalDate = origCalDate2;
                            z = useGmtConversion;
                            gmtCalIfNeeded2 = instance;
                            year2 = year;
                            month2 = month;
                            hour2 = hour;
                            minute2 = minute;
                            try {
                                calendar2.setTime(origCalDate);
                                throw th2;
                            } catch (Throwable th4) {
                                th2 = th4;
                                i = hour2;
                                i2 = minute2;
                                minute2 = year2;
                                hour2 = month2;
                                year2 = gmtCalIfNeeded2;
                                while (true) {
                                    try {
                                        break;
                                    } catch (Throwable th42) {
                                        th2 = th42;
                                    }
                                }
                                throw th2;
                            }
                        }
                    }
                    if (secondsPart2 != 0) {
                        calendar2.set(14, secondsPart2 / 1000000);
                    }
                    Timestamp ts = new Timestamp(cal.getTimeInMillis() + ((long) offsetDiff));
                    ts.setNanos(secondsPart2);
                    Date origCalDate3 = origCalDate2;
                    Calendar cal2 = calendar2;
                    day2 = day;
                    int seconds2 = seconds;
                    try {
                        cal2.setTime(origCalDate3);
                        return ts;
                    } catch (Throwable th422) {
                        th2 = th422;
                        calendar = cal2;
                        origCalDate2 = day2;
                        day2 = seconds2;
                        while (true) {
                            break;
                        }
                        throw th2;
                    }
                } catch (Throwable th5) {
                    th422 = th5;
                    instance = gmtCalIfNeeded;
                    th2 = th422;
                    origCalDate = origCalDate2;
                    z = useGmtConversion;
                    gmtCalIfNeeded2 = instance;
                    year2 = year;
                    month2 = month;
                    hour2 = hour;
                    minute2 = minute;
                    calendar2.setTime(origCalDate);
                    throw th2;
                }
            } catch (Throwable th4222) {
                z = useGmtConversion;
                int i3 = day;
                i = hour;
                i2 = minute;
                day2 = seconds;
                th2 = th4222;
                calendar = calendar2;
                while (true) {
                    break;
                }
                throw th2;
            }
        }
    }

    static final Timestamp fastTimestampCreate(TimeZone tz, int year, int month, int day, int hour, int minute, int seconds, int secondsPart) {
        Calendar cal = tz == null ? new GregorianCalendar() : new GregorianCalendar(tz);
        cal.clear();
        cal.set(year, month - 1, day, hour, minute, seconds);
        Timestamp ts = new Timestamp(cal.getTimeInMillis());
        ts.setNanos(secondsPart);
        return ts;
    }

    public static String getCanonicalTimezone(String timezoneStr, ExceptionInterceptor exceptionInterceptor) throws SQLException {
        if (timezoneStr == null) {
            return null;
        }
        timezoneStr = timezoneStr.trim();
        if (timezoneStr.length() <= 2 || !((timezoneStr.charAt(0) == '+' || timezoneStr.charAt(0) == '-') && Character.isDigit(timezoneStr.charAt(1)))) {
            synchronized (TimeUtil.class) {
                if (timeZoneMappings == null) {
                    loadTimeZoneMappings(exceptionInterceptor);
                }
            }
            String property = timeZoneMappings.getProperty(timezoneStr);
            String canonicalTz = property;
            if (property != null) {
                return canonicalTz;
            }
            throw SQLError.createSQLException(Messages.getString("TimeUtil.UnrecognizedTimezoneId", new Object[]{timezoneStr}), SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE, exceptionInterceptor);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("GMT");
        stringBuilder.append(timezoneStr);
        return stringBuilder.toString();
    }

    private static String timeFormattedString(int hours, int minutes, int seconds) {
        StringBuilder buf = new StringBuilder(8);
        if (hours < 10) {
            buf.append("0");
        }
        buf.append(hours);
        buf.append(":");
        if (minutes < 10) {
            buf.append("0");
        }
        buf.append(minutes);
        buf.append(":");
        if (seconds < 10) {
            buf.append("0");
        }
        buf.append(seconds);
        return buf.toString();
    }

    public static String formatNanos(int nanos, boolean serverSupportsFracSecs, boolean usingMicros) {
        if (nanos > 999999999) {
            nanos %= 100000000;
        }
        if (usingMicros) {
            nanos /= 1000;
        }
        if (serverSupportsFracSecs) {
            if (nanos != 0) {
                int digitCount = usingMicros ? 6 : 9;
                String nanosString = Integer.toString(nanos);
                String zeroPadding = usingMicros ? "000000" : "000000000";
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(zeroPadding.substring(0, digitCount - nanosString.length()));
                stringBuilder.append(nanosString);
                nanosString = stringBuilder.toString();
                int pos = digitCount - 1;
                while (nanosString.charAt(pos) == '0') {
                    pos--;
                }
                return nanosString.substring(0, pos + 1);
            }
        }
        return "0";
    }

    private static void loadTimeZoneMappings(ExceptionInterceptor exceptionInterceptor) throws SQLException {
        timeZoneMappings = new Properties();
        try {
            timeZoneMappings.load(TimeUtil.class.getResourceAsStream(TIME_ZONE_MAPPINGS_RESOURCE));
            for (String tz : TimeZone.getAvailableIDs()) {
                if (!timeZoneMappings.containsKey(tz)) {
                    timeZoneMappings.put(tz, tz);
                }
            }
        } catch (IOException e) {
            throw SQLError.createSQLException(Messages.getString("TimeUtil.LoadTimeZoneMappingError"), SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE, exceptionInterceptor);
        }
    }

    public static Timestamp truncateFractionalSeconds(Timestamp timestamp) {
        Timestamp truncatedTimestamp = new Timestamp(timestamp.getTime());
        truncatedTimestamp.setNanos(0);
        return truncatedTimestamp;
    }
}
