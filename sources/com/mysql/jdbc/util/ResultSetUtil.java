package com.mysql.jdbc.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class ResultSetUtil {
    public static StringBuilder appendResultSetSlashGStyle(StringBuilder appendTo, ResultSet rs) throws SQLException {
        int i;
        ResultSetMetaData rsmd = rs.getMetaData();
        int numFields = rsmd.getColumnCount();
        String[] fieldNames = new String[numFields];
        int maxWidth = 0;
        for (i = 0; i < numFields; i++) {
            fieldNames[i] = rsmd.getColumnLabel(i + 1);
            if (fieldNames[i].length() > maxWidth) {
                maxWidth = fieldNames[i].length();
            }
        }
        i = 1;
        while (rs.next()) {
            appendTo.append("*************************** ");
            int rowCount = i + 1;
            appendTo.append(i);
            appendTo.append(". row ***************************\n");
            for (i = 0; i < numFields; i++) {
                int leftPad = maxWidth - fieldNames[i].length();
                for (int j = 0; j < leftPad; j++) {
                    appendTo.append(" ");
                }
                appendTo.append(fieldNames[i]);
                appendTo.append(": ");
                String stringVal = rs.getString(i + 1);
                if (stringVal != null) {
                    appendTo.append(stringVal);
                } else {
                    appendTo.append("NULL");
                }
                appendTo.append("\n");
            }
            appendTo.append("\n");
            i = rowCount;
        }
        return appendTo;
    }
}
