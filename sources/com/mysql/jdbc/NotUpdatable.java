package com.mysql.jdbc;

import java.sql.SQLException;

public class NotUpdatable extends SQLException {
    public static final String NOT_UPDATEABLE_MESSAGE;
    private static final long serialVersionUID = 8084742846039782258L;

    static {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Messages.getString("NotUpdatable.0"));
        stringBuilder.append(Messages.getString("NotUpdatable.1"));
        stringBuilder.append(Messages.getString("NotUpdatable.2"));
        stringBuilder.append(Messages.getString("NotUpdatable.3"));
        stringBuilder.append(Messages.getString("NotUpdatable.4"));
        stringBuilder.append(Messages.getString("NotUpdatable.5"));
        NOT_UPDATEABLE_MESSAGE = stringBuilder.toString();
    }

    public NotUpdatable() {
        this(NOT_UPDATEABLE_MESSAGE);
    }

    public NotUpdatable(String reason) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(reason);
        stringBuilder.append(Messages.getString("NotUpdatable.1"));
        stringBuilder.append(Messages.getString("NotUpdatable.2"));
        stringBuilder.append(Messages.getString("NotUpdatable.3"));
        stringBuilder.append(Messages.getString("NotUpdatable.4"));
        stringBuilder.append(Messages.getString("NotUpdatable.5"));
        super(stringBuilder.toString(), SQLError.SQL_STATE_GENERAL_ERROR);
    }
}
