package com.mysql.fabric.xmlrpc.base;

public class MethodCall {
    protected String methodName;
    protected Params params;

    public String getMethodName() {
        return this.methodName;
    }

    public void setMethodName(String value) {
        this.methodName = value;
    }

    public Params getParams() {
        return this.params;
    }

    public void setParams(Params value) {
        this.params = value;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<methodCall>");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\t<methodName>");
        stringBuilder.append(this.methodName);
        stringBuilder.append("</methodName>");
        sb.append(stringBuilder.toString());
        if (this.params != null) {
            sb.append(this.params.toString());
        }
        sb.append("</methodCall>");
        return sb.toString();
    }
}
