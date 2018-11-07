package com.mysql.fabric.xmlrpc.base;

import java.util.Arrays;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class Value {
    public static final byte TYPE_array = (byte) 8;
    public static final byte TYPE_base64 = (byte) 6;
    public static final byte TYPE_boolean = (byte) 2;
    public static final byte TYPE_dateTime_iso8601 = (byte) 5;
    public static final byte TYPE_double = (byte) 4;
    public static final byte TYPE_i4 = (byte) 0;
    public static final byte TYPE_int = (byte) 1;
    public static final byte TYPE_string = (byte) 3;
    public static final byte TYPE_struct = (byte) 7;
    private DatatypeFactory dtf = null;
    protected byte objType = (byte) 3;
    protected Object objValue = "";

    public Value(int value) {
        setInt(value);
    }

    public Value(String value) {
        setString(value);
    }

    public Value(boolean value) {
        setBoolean(value);
    }

    public Value(double value) {
        setDouble(value);
    }

    public Value(GregorianCalendar value) throws DatatypeConfigurationException {
        setDateTime(value);
    }

    public Value(byte[] value) {
        setBase64(value);
    }

    public Value(Struct value) {
        setStruct(value);
    }

    public Value(Array value) {
        setArray(value);
    }

    public Object getValue() {
        return this.objValue;
    }

    public byte getType() {
        return this.objType;
    }

    public void setInt(int value) {
        this.objValue = Integer.valueOf(value);
        this.objType = (byte) 1;
    }

    public void setInt(String value) {
        this.objValue = Integer.valueOf(value);
        this.objType = (byte) 1;
    }

    public void setString(String value) {
        this.objValue = value;
        this.objType = (byte) 3;
    }

    public void appendString(String value) {
        Object stringBuilder;
        if (this.objValue != null) {
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append(this.objValue);
            stringBuilder2.append(value);
            stringBuilder = stringBuilder2.toString();
        } else {
            stringBuilder = value;
        }
        this.objValue = stringBuilder;
        this.objType = (byte) 3;
    }

    public void setBoolean(boolean value) {
        this.objValue = Boolean.valueOf(value);
        this.objType = (byte) 2;
    }

    public void setBoolean(String value) {
        if (!value.trim().equals("1")) {
            if (!value.trim().equalsIgnoreCase("true")) {
                this.objValue = Boolean.valueOf(false);
                this.objType = (byte) 2;
            }
        }
        this.objValue = Boolean.valueOf(true);
        this.objType = (byte) 2;
    }

    public void setDouble(double value) {
        this.objValue = Double.valueOf(value);
        this.objType = (byte) 4;
    }

    public void setDouble(String value) {
        this.objValue = Double.valueOf(value);
        this.objType = (byte) 4;
    }

    public void setDateTime(GregorianCalendar value) throws DatatypeConfigurationException {
        if (this.dtf == null) {
            this.dtf = DatatypeFactory.newInstance();
        }
        this.objValue = this.dtf.newXMLGregorianCalendar(value);
        this.objType = (byte) 5;
    }

    public void setDateTime(String value) throws DatatypeConfigurationException {
        if (this.dtf == null) {
            this.dtf = DatatypeFactory.newInstance();
        }
        this.objValue = this.dtf.newXMLGregorianCalendar(value);
        this.objType = (byte) 5;
    }

    public void setBase64(byte[] value) {
        this.objValue = value;
        this.objType = (byte) 6;
    }

    public void setStruct(Struct value) {
        this.objValue = value;
        this.objType = (byte) 7;
    }

    public void setArray(Array value) {
        this.objValue = value;
        this.objType = (byte) 8;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("<value>");
        StringBuilder stringBuilder;
        switch (this.objType) {
            case (byte) 0:
                stringBuilder = new StringBuilder();
                stringBuilder.append("<i4>");
                stringBuilder.append(((Integer) this.objValue).toString());
                stringBuilder.append("</i4>");
                sb.append(stringBuilder.toString());
                break;
            case (byte) 1:
                stringBuilder = new StringBuilder();
                stringBuilder.append("<int>");
                stringBuilder.append(((Integer) this.objValue).toString());
                stringBuilder.append("</int>");
                sb.append(stringBuilder.toString());
                break;
            case (byte) 2:
                stringBuilder = new StringBuilder();
                stringBuilder.append("<boolean>");
                stringBuilder.append(((Boolean) this.objValue).booleanValue());
                stringBuilder.append("</boolean>");
                sb.append(stringBuilder.toString());
                break;
            case (byte) 4:
                stringBuilder = new StringBuilder();
                stringBuilder.append("<double>");
                stringBuilder.append(((Double) this.objValue).toString());
                stringBuilder.append("</double>");
                sb.append(stringBuilder.toString());
                break;
            case (byte) 5:
                stringBuilder = new StringBuilder();
                stringBuilder.append("<dateTime.iso8601>");
                stringBuilder.append(((XMLGregorianCalendar) this.objValue).toString());
                stringBuilder.append("</dateTime.iso8601>");
                sb.append(stringBuilder.toString());
                break;
            case (byte) 6:
                stringBuilder = new StringBuilder();
                stringBuilder.append("<base64>");
                stringBuilder.append(Arrays.toString((byte[]) this.objValue));
                stringBuilder.append("</base64>");
                sb.append(stringBuilder.toString());
                break;
            case (byte) 7:
                sb.append(((Struct) this.objValue).toString());
                break;
            case (byte) 8:
                sb.append(((Array) this.objValue).toString());
                break;
            default:
                stringBuilder = new StringBuilder();
                stringBuilder.append("<string>");
                stringBuilder.append(escapeXMLChars(this.objValue.toString()));
                stringBuilder.append("</string>");
                sb.append(stringBuilder.toString());
                break;
        }
        sb.append("</value>");
        return sb.toString();
    }

    private String escapeXMLChars(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '&') {
                sb.append("&amp;");
            } else if (c == '<') {
                sb.append("&lt;");
            } else if (c != '>') {
                sb.append(c);
            } else {
                sb.append("&gt;");
            }
        }
        return sb.toString();
    }
}
