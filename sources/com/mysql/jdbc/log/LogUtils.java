package com.mysql.jdbc.log;

import com.mysql.jdbc.Util;
import com.mysql.jdbc.profiler.ProfilerEvent;

public class LogUtils {
    public static final String CALLER_INFORMATION_NOT_AVAILABLE = "Caller information not available";
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final int LINE_SEPARATOR_LENGTH = LINE_SEPARATOR.length();

    public static Object expandProfilerEventIfNecessary(Object possibleProfilerEvent) {
        if (!(possibleProfilerEvent instanceof ProfilerEvent)) {
            return possibleProfilerEvent;
        }
        StringBuilder msgBuf = new StringBuilder();
        ProfilerEvent evt = (ProfilerEvent) possibleProfilerEvent;
        String locationInformation = evt.getEventCreationPointAsString();
        if (locationInformation == null) {
            locationInformation = Util.stackTraceToString(new Throwable());
        }
        msgBuf.append("Profiler Event: [");
        switch (evt.getEventType()) {
            case (byte) 0:
                msgBuf.append("WARN");
                break;
            case (byte) 1:
                msgBuf.append("CONSTRUCT");
                break;
            case (byte) 2:
                msgBuf.append("PREPARE");
                break;
            case (byte) 3:
                msgBuf.append("QUERY");
                break;
            case (byte) 4:
                msgBuf.append("EXECUTE");
                break;
            case (byte) 5:
                msgBuf.append("FETCH");
                break;
            case (byte) 6:
                msgBuf.append("SLOW QUERY");
                break;
            default:
                msgBuf.append("UNKNOWN");
                break;
        }
        msgBuf.append("] ");
        msgBuf.append(locationInformation);
        msgBuf.append(" duration: ");
        msgBuf.append(evt.getEventDuration());
        msgBuf.append(" ");
        msgBuf.append(evt.getDurationUnits());
        msgBuf.append(", connection-id: ");
        msgBuf.append(evt.getConnectionId());
        msgBuf.append(", statement-id: ");
        msgBuf.append(evt.getStatementId());
        msgBuf.append(", resultset-id: ");
        msgBuf.append(evt.getResultSetId());
        String evtMessage = evt.getMessage();
        if (evtMessage != null) {
            msgBuf.append(", message: ");
            msgBuf.append(evtMessage);
        }
        return msgBuf;
    }

    public static String findCallingClassAndMethod(Throwable t) {
        String stackTraceAsString = Util.stackTraceToString(t);
        String callingClassAndMethod = CALLER_INFORMATION_NOT_AVAILABLE;
        int endInternalMethods = stackTraceAsString.lastIndexOf("com.mysql.jdbc");
        if (endInternalMethods != -1) {
            int endOfLine;
            int compliancePackage = stackTraceAsString.indexOf("com.mysql.jdbc.compliance", endInternalMethods);
            if (compliancePackage != -1) {
                endOfLine = compliancePackage - LINE_SEPARATOR_LENGTH;
            } else {
                endOfLine = stackTraceAsString.indexOf(LINE_SEPARATOR, endInternalMethods);
            }
            if (endOfLine != -1) {
                int nextEndOfLine = stackTraceAsString.indexOf(LINE_SEPARATOR, LINE_SEPARATOR_LENGTH + endOfLine);
                callingClassAndMethod = nextEndOfLine != -1 ? stackTraceAsString.substring(LINE_SEPARATOR_LENGTH + endOfLine, nextEndOfLine) : stackTraceAsString.substring(LINE_SEPARATOR_LENGTH + endOfLine);
            }
        }
        if (callingClassAndMethod.startsWith("\tat ") || callingClassAndMethod.startsWith("at ")) {
            return callingClassAndMethod;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("at ");
        stringBuilder.append(callingClassAndMethod);
        return stringBuilder.toString();
    }
}
