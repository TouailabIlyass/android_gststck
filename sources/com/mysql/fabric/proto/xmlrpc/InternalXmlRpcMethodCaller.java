package com.mysql.fabric.proto.xmlrpc;

import com.mysql.fabric.FabricCommunicationException;
import com.mysql.fabric.xmlrpc.Client;
import com.mysql.fabric.xmlrpc.base.Array;
import com.mysql.fabric.xmlrpc.base.Member;
import com.mysql.fabric.xmlrpc.base.MethodCall;
import com.mysql.fabric.xmlrpc.base.Param;
import com.mysql.fabric.xmlrpc.base.Params;
import com.mysql.fabric.xmlrpc.base.Struct;
import com.mysql.fabric.xmlrpc.base.Value;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InternalXmlRpcMethodCaller implements XmlRpcMethodCaller {
    private Client xmlRpcClient;

    public InternalXmlRpcMethodCaller(String url) throws FabricCommunicationException {
        try {
            this.xmlRpcClient = new Client(url);
        } catch (Throwable ex) {
            throw new FabricCommunicationException(ex);
        }
    }

    private Object unwrapValue(Value v) {
        if (v.getType() == (byte) 8) {
            return methodResponseArrayToList((Array) v.getValue());
        }
        if (v.getType() != (byte) 7) {
            return v.getValue();
        }
        Map<String, Object> s = new HashMap();
        for (Member m : ((Struct) v.getValue()).getMember()) {
            s.put(m.getName(), unwrapValue(m.getValue()));
        }
        return s;
    }

    private List<Object> methodResponseArrayToList(Array array) {
        List<Object> result = new ArrayList();
        for (Value v : array.getData().getValue()) {
            result.add(unwrapValue(v));
        }
        return result;
    }

    public void setHeader(String name, String value) {
        this.xmlRpcClient.setHeader(name, value);
    }

    public void clearHeader(String name) {
        this.xmlRpcClient.clearHeader(name);
    }

    public List<Object> call(String methodName, Object[] args) throws FabricCommunicationException {
        MethodCall methodCall = new MethodCall();
        Params p = new Params();
        if (args == null) {
            args = new Object[0];
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                throw new NullPointerException("nil args unsupported");
            }
            if (String.class.isAssignableFrom(args[i].getClass())) {
                p.addParam(new Param(new Value((String) args[i])));
            } else if (Double.class.isAssignableFrom(args[i].getClass())) {
                p.addParam(new Param(new Value(((Double) args[i]).doubleValue())));
            } else if (Integer.class.isAssignableFrom(args[i].getClass())) {
                p.addParam(new Param(new Value(((Integer) args[i]).intValue())));
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unknown argument type: ");
                stringBuilder.append(args[i].getClass());
                throw new IllegalArgumentException(stringBuilder.toString());
            }
        }
        methodCall.setMethodName(methodName);
        methodCall.setParams(p);
        try {
            return methodResponseArrayToList((Array) ((Param) this.xmlRpcClient.execute(methodCall).getParams().getParam().get(0)).getValue().getValue());
        } catch (Exception ex) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("Error during call to `");
            stringBuilder.append(methodName);
            stringBuilder.append("' (args=");
            stringBuilder.append(Arrays.toString(args));
            stringBuilder.append(")");
            throw new FabricCommunicationException(stringBuilder.toString(), ex);
        }
    }
}
