package com.mysql.jdbc;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Security {
    private static final char PVERSION41_CHAR = '*';
    private static final int SHA1_HASH_SIZE = 20;

    private static int charVal(char c) {
        if (c >= '0' && c <= '9') {
            return c - 48;
        }
        int i = (c < 'A' || c > 'Z') ? c - 97 : c - 65;
        return i + 10;
    }

    static byte[] createKeyFromOldPassword(String passwd) throws NoSuchAlgorithmException {
        return getBinaryPassword(getSaltFromPassword(makeScrambledPassword(passwd)), false);
    }

    static byte[] getBinaryPassword(int[] salt, boolean usingNewPasswords) throws NoSuchAlgorithmException {
        byte[] binaryPassword = new byte[20];
        int i = 0;
        int pos;
        int val;
        int t;
        if (usingNewPasswords) {
            pos = 0;
            while (i < 4) {
                val = salt[i];
                t = 3;
                while (t >= 0) {
                    int pos2 = pos + 1;
                    binaryPassword[pos] = (byte) (val & 255);
                    val >>= 8;
                    t--;
                    pos = pos2;
                }
                i++;
            }
            return binaryPassword;
        }
        val = 0;
        pos = 0;
        for (t = 0; t < 2; t++) {
            pos2 = salt[t];
            for (pos = 3; pos >= 0; pos--) {
                binaryPassword[pos + val] = (byte) (pos2 % 256);
                pos2 >>= 8;
            }
            val += 4;
        }
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(binaryPassword, 0, 8);
        return md.digest();
    }

    private static int[] getSaltFromPassword(String password) {
        int[] result = new int[6];
        if (password != null) {
            if (password.length() != 0) {
                int i = 0;
                int val;
                if (password.charAt(0) == PVERSION41_CHAR) {
                    String saltInHex = password.substring(1, 5);
                    val = 0;
                    while (i < 4) {
                        val = (val << 4) + charVal(saltInHex.charAt(i));
                        i++;
                    }
                    return result;
                }
                int resultPos = 0;
                int length = password.length();
                int pos;
                for (val = 0; val < length; val = pos) {
                    int val2 = 0;
                    pos = val;
                    val = 0;
                    while (val < 8) {
                        val2 = (val2 << 4) + charVal(password.charAt(pos));
                        val++;
                        pos++;
                    }
                    val = resultPos + 1;
                    result[resultPos] = val2;
                    resultPos = val;
                }
                return result;
            }
        }
        return result;
    }

    private static String longToHex(long val) {
        String longHex = Long.toHexString(val);
        int length = longHex.length();
        int i = 0;
        if (length >= 8) {
            return longHex.substring(0, 8);
        }
        int padding = 8 - length;
        StringBuilder buf = new StringBuilder();
        while (i < padding) {
            buf.append("0");
            i++;
        }
        buf.append(longHex);
        return buf.toString();
    }

    static String makeScrambledPassword(String password) throws NoSuchAlgorithmException {
        long[] passwordHash = Util.hashPre41Password(password);
        StringBuilder scramble = new StringBuilder();
        scramble.append(longToHex(passwordHash[0]));
        scramble.append(longToHex(passwordHash[1]));
        return scramble.toString();
    }

    public static void xorString(byte[] from, byte[] to, byte[] scramble, int length) {
        int scrambleLength = scramble.length;
        for (int pos = 0; pos < length; pos++) {
            to[pos] = (byte) (from[pos] ^ scramble[pos % scrambleLength]);
        }
    }

    static byte[] passwordHashStage1(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        StringBuilder cleansedPassword = new StringBuilder();
        int passwordLength = password.length();
        for (int i = 0; i < passwordLength; i++) {
            char c = password.charAt(i);
            if (c != ' ') {
                if (c != '\t') {
                    cleansedPassword.append(c);
                }
            }
        }
        return md.digest(StringUtils.getBytes(cleansedPassword.toString()));
    }

    static byte[] passwordHashStage2(byte[] hashedPassword, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(salt, 0, 4);
        md.update(hashedPassword, 0, 20);
        return md.digest();
    }

    public static byte[] scramble411(String password, String seed, String passwordEncoding) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        byte[] passwordHashStage1;
        byte[] passwordHashStage2;
        byte[] toBeXord;
        int numToXor;
        int i;
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        if (passwordEncoding != null) {
            if (passwordEncoding.length() != 0) {
                passwordHashStage1 = StringUtils.getBytes(password, passwordEncoding);
                passwordHashStage1 = md.digest(passwordHashStage1);
                md.reset();
                passwordHashStage2 = md.digest(passwordHashStage1);
                md.reset();
                md.update(StringUtils.getBytes(seed, "ASCII"));
                md.update(passwordHashStage2);
                toBeXord = md.digest();
                numToXor = toBeXord.length;
                for (i = 0; i < numToXor; i++) {
                    toBeXord[i] = (byte) (toBeXord[i] ^ passwordHashStage1[i]);
                }
                return toBeXord;
            }
        }
        passwordHashStage1 = StringUtils.getBytes(password);
        passwordHashStage1 = md.digest(passwordHashStage1);
        md.reset();
        passwordHashStage2 = md.digest(passwordHashStage1);
        md.reset();
        md.update(StringUtils.getBytes(seed, "ASCII"));
        md.update(passwordHashStage2);
        toBeXord = md.digest();
        numToXor = toBeXord.length;
        for (i = 0; i < numToXor; i++) {
            toBeXord[i] = (byte) (toBeXord[i] ^ passwordHashStage1[i]);
        }
        return toBeXord;
    }

    private Security() {
    }
}
