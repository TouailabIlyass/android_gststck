package com.mysql.jdbc;

import java.sql.SQLException;

public class PacketTooBigException extends SQLException {
    static final long serialVersionUID = 7248633977685452174L;

    public PacketTooBigException(long packetSize, long maximumPacketSize) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Messages.getString("PacketTooBigException.0"));
        stringBuilder.append(packetSize);
        stringBuilder.append(Messages.getString("PacketTooBigException.1"));
        stringBuilder.append(maximumPacketSize);
        stringBuilder.append(Messages.getString("PacketTooBigException.2"));
        stringBuilder.append(Messages.getString("PacketTooBigException.3"));
        stringBuilder.append(Messages.getString("PacketTooBigException.4"));
        super(stringBuilder.toString(), SQLError.SQL_STATE_GENERAL_ERROR);
    }
}
