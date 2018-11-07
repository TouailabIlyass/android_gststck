package com.mysql.fabric.xmlrpc.base;

import java.util.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class ResponseParser extends DefaultHandler {
    Stack<Object> elNames = new Stack();
    Stack<Object> objects = new Stack();
    private MethodResponse resp = null;

    public MethodResponse getMethodResponse() {
        return this.resp;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        String thisElement = qName;
        if (thisElement != null) {
            this.elNames.push(thisElement);
            if (thisElement.equals("methodResponse")) {
                this.objects.push(new MethodResponse());
            } else if (thisElement.equals("params")) {
                this.objects.push(new Params());
            } else if (thisElement.equals("param")) {
                this.objects.push(new Param());
            } else if (thisElement.equals("value")) {
                this.objects.push(new Value());
            } else if (thisElement.equals("array")) {
                this.objects.push(new Array());
            } else if (thisElement.equals("data")) {
                this.objects.push(new Data());
            } else if (thisElement.equals("struct")) {
                this.objects.push(new Struct());
            } else if (thisElement.equals("member")) {
                this.objects.push(new Member());
            } else if (thisElement.equals("fault")) {
                this.objects.push(new Fault());
            }
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        String thisElement = (String) this.elNames.pop();
        if (thisElement == null) {
            return;
        }
        if (thisElement.equals("methodResponse")) {
            this.resp = (MethodResponse) this.objects.pop();
        } else if (thisElement.equals("params")) {
            ((MethodResponse) this.objects.peek()).setParams((Params) this.objects.pop());
        } else if (thisElement.equals("param")) {
            ((Params) this.objects.peek()).addParam((Param) this.objects.pop());
        } else if (thisElement.equals("value")) {
            Value v = (Value) this.objects.pop();
            Object parent = this.objects.peek();
            if (parent instanceof Data) {
                ((Data) parent).addValue(v);
            } else if (parent instanceof Param) {
                ((Param) parent).setValue(v);
            } else if (parent instanceof Member) {
                ((Member) parent).setValue(v);
            } else if (parent instanceof Fault) {
                ((Fault) parent).setValue(v);
            }
        } else if (thisElement.equals("array")) {
            ((Value) this.objects.peek()).setArray((Array) this.objects.pop());
        } else if (thisElement.equals("data")) {
            ((Array) this.objects.peek()).setData((Data) this.objects.pop());
        } else if (thisElement.equals("struct")) {
            ((Value) this.objects.peek()).setStruct((Struct) this.objects.pop());
        } else if (thisElement.equals("member")) {
            ((Struct) this.objects.peek()).addMember((Member) this.objects.pop());
        } else if (thisElement.equals("fault")) {
            ((MethodResponse) this.objects.peek()).setFault((Fault) this.objects.pop());
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        try {
            String thisElement = (String) this.elNames.peek();
            if (thisElement != null) {
                if (thisElement.equals("name")) {
                    ((Member) this.objects.peek()).setName(new String(ch, start, length));
                } else if (thisElement.equals("value")) {
                    ((Value) this.objects.peek()).appendString(new String(ch, start, length));
                } else {
                    if (!thisElement.equals("i4")) {
                        if (!thisElement.equals("int")) {
                            if (thisElement.equals("boolean")) {
                                ((Value) this.objects.peek()).setBoolean(new String(ch, start, length));
                            } else if (thisElement.equals("string")) {
                                ((Value) this.objects.peek()).appendString(new String(ch, start, length));
                            } else if (thisElement.equals("double")) {
                                ((Value) this.objects.peek()).setDouble(new String(ch, start, length));
                            } else if (thisElement.equals("dateTime.iso8601")) {
                                ((Value) this.objects.peek()).setDateTime(new String(ch, start, length));
                            } else if (thisElement.equals("base64")) {
                                ((Value) this.objects.peek()).setBase64(new String(ch, start, length).getBytes());
                            }
                        }
                    }
                    ((Value) this.objects.peek()).setInt(new String(ch, start, length));
                }
            }
        } catch (Exception e) {
            throw new SAXParseException(e.getMessage(), null, e);
        }
    }
}
