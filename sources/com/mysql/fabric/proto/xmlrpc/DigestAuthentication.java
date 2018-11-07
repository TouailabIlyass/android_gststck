package com.mysql.fabric.proto.xmlrpc;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class DigestAuthentication {
    private static Random random = new Random();

    public static String getChallengeHeader(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setDoOutput(true);
        conn.getOutputStream().close();
        try {
            conn.getInputStream().close();
        } catch (IOException ex) {
            if (401 == conn.getResponseCode()) {
                String hdr = conn.getHeaderField("WWW-Authenticate");
                if (hdr != null && !"".equals(hdr)) {
                    return hdr;
                }
            } else if (400 == conn.getResponseCode()) {
                throw new IOException("Fabric returns status 400. If authentication is disabled on the Fabric node, omit the `fabricUsername' and `fabricPassword' properties from your connection.");
            } else {
                throw ex;
            }
        }
        return null;
    }

    public static String calculateMD5RequestDigest(String uri, String username, String password, String realm, String nonce, String nc, String cnonce, String qop) {
        String reqA1 = new StringBuilder();
        reqA1.append(username);
        reqA1.append(":");
        reqA1.append(realm);
        reqA1.append(":");
        reqA1.append(password);
        reqA1 = reqA1.toString();
        String reqA2 = new StringBuilder();
        reqA2.append("POST:");
        reqA2.append(uri);
        reqA2 = reqA2.toString();
        String hashA1 = checksumMD5(reqA1);
        String hashA2 = checksumMD5(reqA2);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(nonce);
        stringBuilder.append(":");
        stringBuilder.append(nc);
        stringBuilder.append(":");
        stringBuilder.append(cnonce);
        stringBuilder.append(":");
        stringBuilder.append(qop);
        stringBuilder.append(":");
        stringBuilder.append(hashA2);
        return digestMD5(hashA1, stringBuilder.toString());
    }

    private static String checksumMD5(String data) {
        try {
            return hexEncode(MessageDigest.getInstance("MD5").digest(data.getBytes()));
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("Unable to create MD5 instance", ex);
        }
    }

    private static String digestMD5(String secret, String data) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(secret);
        stringBuilder.append(":");
        stringBuilder.append(data);
        return checksumMD5(stringBuilder.toString());
    }

    private static String hexEncode(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            sb.append(String.format("%02x", new Object[]{Byte.valueOf(data[i])}));
        }
        return sb.toString();
    }

    public static String serializeDigestResponse(Map<String, String> paramMap) {
        StringBuilder sb = new StringBuilder("Digest ");
        boolean prefixComma = false;
        for (Entry<String, String> entry : paramMap.entrySet()) {
            if (prefixComma) {
                sb.append(", ");
            } else {
                prefixComma = true;
            }
            sb.append((String) entry.getKey());
            sb.append("=");
            sb.append((String) entry.getValue());
        }
        return sb.toString();
    }

    public static Map<String, String> parseDigestChallenge(String headerValue) {
        if (headerValue.startsWith("Digest ")) {
            String params = headerValue.substring(7);
            Map<String, String> paramMap = new HashMap();
            for (String param : params.split(",\\s*")) {
                String[] pieces = param.split("=");
                paramMap.put(pieces[0], pieces[1].replaceAll("^\"(.*)\"$", "$1"));
            }
            return paramMap;
        }
        throw new IllegalArgumentException("Header is not a digest challenge");
    }

    public static String generateCnonce(String nonce, String nc) {
        byte[] buf = new byte[8];
        random.nextBytes(buf);
        for (int i = 0; i < 8; i++) {
            buf[i] = (byte) (32 + (buf[i] % 95));
        }
        try {
            return hexEncode(MessageDigest.getInstance("SHA-1").digest(String.format("%s:%s:%s:%s", new Object[]{nonce, nc, new Date().toGMTString(), new String(buf)}).getBytes()));
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("Unable to create SHA-1 instance", ex);
        }
    }

    private static String quoteParam(String param) {
        if (!param.contains("\"")) {
            if (!param.contains("'")) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("\"");
                stringBuilder.append(param);
                stringBuilder.append("\"");
                return stringBuilder.toString();
            }
        }
        throw new IllegalArgumentException("Invalid character in parameter");
    }

    public static String generateAuthorizationHeader(Map<String, String> digestChallenge, String username, String password) {
        Map<String, String> map = digestChallenge;
        String nonce = (String) map.get("nonce");
        String nc = "00000001";
        String cnonce = generateCnonce(nonce, nc);
        String qop = "auth";
        String uri = "/RPC2";
        String realm = (String) map.get("realm");
        String opaque = (String) map.get("opaque");
        String requestDigest = calculateMD5RequestDigest(uri, username, password, realm, nonce, nc, cnonce, qop);
        Map<String, String> digestResponseMap = new HashMap();
        digestResponseMap.put("algorithm", "MD5");
        digestResponseMap.put("username", quoteParam(username));
        digestResponseMap.put("realm", quoteParam(realm));
        digestResponseMap.put("nonce", quoteParam(nonce));
        digestResponseMap.put("uri", quoteParam(uri));
        digestResponseMap.put("qop", qop);
        digestResponseMap.put("nc", nc);
        digestResponseMap.put("cnonce", quoteParam(cnonce));
        digestResponseMap.put("response", quoteParam(requestDigest));
        digestResponseMap.put("opaque", quoteParam(opaque));
        return serializeDigestResponse(digestResponseMap);
    }
}
