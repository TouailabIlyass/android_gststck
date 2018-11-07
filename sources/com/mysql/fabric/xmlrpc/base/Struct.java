package com.mysql.fabric.xmlrpc.base;

import java.util.ArrayList;
import java.util.List;

public class Struct {
    protected List<Member> member;

    public List<Member> getMember() {
        if (this.member == null) {
            this.member = new ArrayList();
        }
        return this.member;
    }

    public void addMember(Member m) {
        getMember().add(m);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.member != null) {
            sb.append("<struct>");
            for (int i = 0; i < this.member.size(); i++) {
                sb.append(((Member) this.member.get(i)).toString());
            }
            sb.append("</struct>");
        }
        return sb.toString();
    }
}
