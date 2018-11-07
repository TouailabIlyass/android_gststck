package com.mysql.fabric.xmlrpc.base;

import java.util.ArrayList;
import java.util.List;

public class Params {
    protected List<Param> param;

    public List<Param> getParam() {
        if (this.param == null) {
            this.param = new ArrayList();
        }
        return this.param;
    }

    public void addParam(Param p) {
        getParam().add(p);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.param != null) {
            sb.append("<params>");
            for (int i = 0; i < this.param.size(); i++) {
                sb.append(((Param) this.param.get(i)).toString());
            }
            sb.append("</params>");
        }
        return sb.toString();
    }
}
