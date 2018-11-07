package com.mysql.fabric.xmlrpc.base;

public class Param {
    protected Value value;

    public Param(Value value) {
        this.value = value;
    }

    public Value getValue() {
        return this.value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("<param>");
        sb.append(this.value.toString());
        sb.append("</param>");
        return sb.toString();
    }
}
