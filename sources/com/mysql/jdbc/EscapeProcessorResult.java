package com.mysql.jdbc;

class EscapeProcessorResult {
    boolean callingStoredFunction = false;
    String escapedSql;
    byte usesVariables = (byte) 0;

    EscapeProcessorResult() {
    }
}
