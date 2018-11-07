package com.mysql.jdbc;

public class Constants {
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    public static final String MILLIS_I18N = Messages.getString("Milliseconds");
    public static final byte[] SLASH_STAR_SPACE_AS_BYTES = new byte[]{(byte) 47, (byte) 42, (byte) 32};
    public static final byte[] SPACE_STAR_SLASH_SPACE_AS_BYTES = new byte[]{(byte) 32, (byte) 42, (byte) 47, (byte) 32};

    private Constants() {
    }
}
