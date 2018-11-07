package com.mysql.fabric;

import com.mysql.fabric.proto.xmlrpc.ResultSetParser;
import java.util.List;
import java.util.Map;

public class Response {
    private String errorMessage;
    private String fabricUuid;
    private int protocolVersion;
    private List<Map<String, ?>> resultSet;
    private int ttl;

    public Response(List<?> responseData) throws FabricCommunicationException {
        this.protocolVersion = ((Integer) responseData.get(0)).intValue();
        if (this.protocolVersion != 1) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Unknown protocol version: ");
            stringBuilder.append(this.protocolVersion);
            throw new FabricCommunicationException(stringBuilder.toString());
        }
        this.fabricUuid = (String) responseData.get(1);
        this.ttl = ((Integer) responseData.get(2)).intValue();
        this.errorMessage = (String) responseData.get(3);
        if ("".equals(this.errorMessage)) {
            this.errorMessage = null;
        }
        List<Map<String, ?>> resultSets = (List) responseData.get(4);
        if (resultSets.size() > 0) {
            Map<String, ?> resultData = (Map) resultSets.get(0);
            this.resultSet = new ResultSetParser().parse((Map) resultData.get("info"), (List) resultData.get("rows"));
        }
    }

    public int getProtocolVersion() {
        return this.protocolVersion;
    }

    public String getFabricUuid() {
        return this.fabricUuid;
    }

    public int getTtl() {
        return this.ttl;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public List<Map<String, ?>> getResultSet() {
        return this.resultSet;
    }
}
