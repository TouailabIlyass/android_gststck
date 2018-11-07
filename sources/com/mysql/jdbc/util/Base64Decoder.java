package com.mysql.jdbc.util;

public class Base64Decoder {
    private static byte[] decoderMap = new byte[]{(byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) 62, (byte) -1, (byte) -1, (byte) -1, (byte) 63, (byte) 52, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57, (byte) 58, (byte) 59, (byte) 60, (byte) 61, (byte) -1, (byte) -1, (byte) -1, (byte) -2, (byte) -1, (byte) -1, (byte) -1, (byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 7, (byte) 8, (byte) 9, (byte) 10, (byte) 11, (byte) 12, (byte) 13, (byte) 14, (byte) 15, (byte) 16, (byte) 17, (byte) 18, (byte) 19, (byte) 20, (byte) 21, (byte) 22, (byte) 23, (byte) 24, (byte) 25, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) 26, (byte) 27, (byte) 28, (byte) 29, (byte) 30, (byte) 31, (byte) 32, (byte) 33, (byte) 34, (byte) 35, (byte) 36, (byte) 37, (byte) 38, (byte) 39, (byte) 40, (byte) 41, (byte) 42, (byte) 43, (byte) 44, (byte) 45, (byte) 46, (byte) 47, (byte) 48, (byte) 49, (byte) 50, (byte) 51, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1};

    public static class IntWrapper {
        public int value;

        public IntWrapper(int value) {
            this.value = value;
        }
    }

    private static byte getNextValidByte(byte[] in, IntWrapper pos, int maxPos) {
        while (pos.value <= maxPos) {
            if (in[pos.value] < (byte) 0 || decoderMap[in[pos.value]] < (byte) 0) {
                pos.value++;
            } else {
                int i = pos.value;
                pos.value = i + 1;
                return in[i];
            }
        }
        return (byte) 61;
    }

    public static byte[] decode(byte[] in, int pos, int length) {
        byte[] bArr = in;
        IntWrapper offset = new IntWrapper(pos);
        byte[] sestet = new byte[4];
        byte[] octet = new byte[((length * 3) / 4)];
        int octetId = 0;
        int maxPos = (offset.value + length) - 1;
        while (offset.value <= maxPos) {
            int octetId2;
            sestet[0] = decoderMap[getNextValidByte(bArr, offset, maxPos)];
            sestet[1] = decoderMap[getNextValidByte(bArr, offset, maxPos)];
            sestet[2] = decoderMap[getNextValidByte(bArr, offset, maxPos)];
            sestet[3] = decoderMap[getNextValidByte(bArr, offset, maxPos)];
            if (sestet[1] != (byte) -2) {
                octetId2 = octetId + 1;
                octet[octetId] = (byte) ((sestet[0] << 2) | (sestet[1] >>> 4));
            } else {
                octetId2 = octetId;
            }
            if (sestet[2] != (byte) -2) {
                octetId = octetId2 + 1;
                octet[octetId2] = (byte) (((sestet[1] & 15) << 4) | (sestet[2] >>> 2));
            } else {
                octetId = octetId2;
            }
            if (sestet[3] != (byte) -2) {
                octetId2 = octetId + 1;
                octet[octetId] = (byte) (((sestet[2] & 3) << 6) | sestet[3]);
                octetId = octetId2;
            }
        }
        byte[] out = new byte[octetId];
        System.arraycopy(octet, 0, out, 0, octetId);
        return out;
    }
}
