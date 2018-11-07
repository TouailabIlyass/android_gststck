package com.mysql.fabric.xmlrpc.base;

public class Fault {
    protected Value value;

    public Value getValue() {
        return this.value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.value != null) {
            sb.append("<fault>");
            sb.append(this.value.toString());
            sb.append("</fault>");
        }
        return sb.toString();
    }
}
