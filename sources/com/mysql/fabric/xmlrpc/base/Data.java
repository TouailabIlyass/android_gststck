package com.mysql.fabric.xmlrpc.base;

import java.util.ArrayList;
import java.util.List;

public class Data {
    protected List<Value> value;

    public List<Value> getValue() {
        if (this.value == null) {
            this.value = new ArrayList();
        }
        return this.value;
    }

    public void addValue(Value v) {
        getValue().add(v);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.value != null) {
            sb.append("<data>");
            for (int i = 0; i < this.value.size(); i++) {
                sb.append(((Value) this.value.get(i)).toString());
            }
            sb.append("</data>");
        }
        return sb.toString();
    }
}
