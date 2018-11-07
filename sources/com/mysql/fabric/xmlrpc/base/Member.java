package com.mysql.fabric.xmlrpc.base;

public class Member {
    protected String name;
    protected Value value;

    public Member(String name, Value value) {
        setName(name);
        setValue(value);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public Value getValue() {
        return this.value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<member>");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<name>");
        stringBuilder.append(this.name);
        stringBuilder.append("</name>");
        sb.append(stringBuilder.toString());
        sb.append(this.value.toString());
        sb.append("</member>");
        return sb.toString();
    }
}
