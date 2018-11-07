package com.mysql.jdbc;

/* compiled from: CharsetMapping */
class Collation {
    public final String collationName;
    public final int index;
    public final MysqlCharset mysqlCharset;
    public final int priority;

    public Collation(int index, String collationName, int priority, String charsetName) {
        this.index = index;
        this.collationName = collationName;
        this.priority = priority;
        this.mysqlCharset = (MysqlCharset) CharsetMapping.CHARSET_NAME_TO_CHARSET.get(charsetName);
    }

    public String toString() {
        StringBuilder asString = new StringBuilder();
        asString.append("[");
        asString.append("index=");
        asString.append(this.index);
        asString.append(",collationName=");
        asString.append(this.collationName);
        asString.append(",charsetName=");
        asString.append(this.mysqlCharset.charsetName);
        asString.append(",javaCharsetName=");
        asString.append(this.mysqlCharset.getMatchingJavaEncoding(null));
        asString.append("]");
        return asString.toString();
    }
}
