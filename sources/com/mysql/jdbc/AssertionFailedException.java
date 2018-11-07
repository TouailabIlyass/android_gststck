package com.mysql.jdbc;

public class AssertionFailedException extends RuntimeException {
    private static final long serialVersionUID = 1;

    public static void shouldNotHappen(Exception ex) throws AssertionFailedException {
        throw new AssertionFailedException(ex);
    }

    public AssertionFailedException(Exception ex) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Messages.getString("AssertionFailedException.0"));
        stringBuilder.append(ex.toString());
        stringBuilder.append(Messages.getString("AssertionFailedException.1"));
        super(stringBuilder.toString());
    }
}
