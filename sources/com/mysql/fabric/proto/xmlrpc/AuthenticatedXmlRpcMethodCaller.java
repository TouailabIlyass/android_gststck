package com.mysql.fabric.proto.xmlrpc;

import com.mysql.fabric.FabricCommunicationException;
import java.io.IOException;
import java.util.List;

public class AuthenticatedXmlRpcMethodCaller implements XmlRpcMethodCaller {
    private String password;
    private XmlRpcMethodCaller underlyingCaller;
    private String url;
    private String username;

    public AuthenticatedXmlRpcMethodCaller(XmlRpcMethodCaller underlyingCaller, String url, String username, String password) {
        this.underlyingCaller = underlyingCaller;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public void setHeader(String name, String value) {
        this.underlyingCaller.setHeader(name, value);
    }

    public void clearHeader(String name) {
        this.underlyingCaller.clearHeader(name);
    }

    public List<?> call(String methodName, Object[] args) throws FabricCommunicationException {
        try {
            this.underlyingCaller.setHeader("Authorization", DigestAuthentication.generateAuthorizationHeader(DigestAuthentication.parseDigestChallenge(DigestAuthentication.getChallengeHeader(this.url)), this.username, this.password));
            return this.underlyingCaller.call(methodName, args);
        } catch (IOException ex) {
            throw new FabricCommunicationException("Unable to obtain challenge header for authentication", ex);
        }
    }
}
