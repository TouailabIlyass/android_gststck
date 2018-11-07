package com.mysql.jdbc;

import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/* compiled from: CharsetMapping */
class MysqlCharset {
    public final String charsetName;
    public final List<String> javaEncodingsUc;
    public int major;
    public final int mblen;
    public int minor;
    public final int priority;
    public int subminor;

    public MysqlCharset(String charsetName, int mblen, int priority, String[] javaEncodings) {
        this.javaEncodingsUc = new ArrayList();
        this.major = 4;
        this.minor = 1;
        int i = 0;
        this.subminor = 0;
        this.charsetName = charsetName;
        this.mblen = mblen;
        this.priority = priority;
        while (i < javaEncodings.length) {
            String encoding = javaEncodings[i];
            try {
                Charset cs = Charset.forName(encoding);
                addEncodingMapping(cs.name());
                for (String addEncodingMapping : cs.aliases()) {
                    addEncodingMapping(addEncodingMapping);
                }
            } catch (Exception e) {
                if (mblen == 1) {
                    addEncodingMapping(encoding);
                }
            }
            i++;
        }
        if (this.javaEncodingsUc.size() != 0) {
            return;
        }
        if (mblen > 1) {
            addEncodingMapping("UTF-8");
        } else {
            addEncodingMapping("Cp1252");
        }
    }

    private void addEncodingMapping(String encoding) {
        String encodingUc = encoding.toUpperCase(Locale.ENGLISH);
        if (!this.javaEncodingsUc.contains(encodingUc)) {
            this.javaEncodingsUc.add(encodingUc);
        }
    }

    public MysqlCharset(String charsetName, int mblen, int priority, String[] javaEncodings, int major, int minor) {
        this(charsetName, mblen, priority, javaEncodings);
        this.major = major;
        this.minor = minor;
    }

    public MysqlCharset(String charsetName, int mblen, int priority, String[] javaEncodings, int major, int minor, int subminor) {
        this(charsetName, mblen, priority, javaEncodings);
        this.major = major;
        this.minor = minor;
        this.subminor = subminor;
    }

    public String toString() {
        StringBuilder asString = new StringBuilder();
        asString.append("[");
        asString.append("charsetName=");
        asString.append(this.charsetName);
        asString.append(",mblen=");
        asString.append(this.mblen);
        asString.append("]");
        return asString.toString();
    }

    boolean isOkayForVersion(Connection conn) throws SQLException {
        return conn.versionMeetsMinimum(this.major, this.minor, this.subminor);
    }

    String getMatchingJavaEncoding(String javaEncoding) {
        if (javaEncoding == null || !this.javaEncodingsUc.contains(javaEncoding.toUpperCase(Locale.ENGLISH))) {
            return (String) this.javaEncodingsUc.get(0);
        }
        return javaEncoding;
    }
}
