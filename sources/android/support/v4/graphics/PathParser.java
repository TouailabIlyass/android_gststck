package android.support.v4.graphics;

import android.graphics.Path;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.util.Log;
import java.util.ArrayList;

@RestrictTo({Scope.LIBRARY_GROUP})
public class PathParser {
    private static final String LOGTAG = "PathParser";

    private static class ExtractFloatResult {
        int mEndPosition;
        boolean mEndWithNegOrDot;

        ExtractFloatResult() {
        }
    }

    public static class PathDataNode {
        @RestrictTo({Scope.LIBRARY_GROUP})
        public float[] mParams;
        @RestrictTo({Scope.LIBRARY_GROUP})
        public char mType;

        PathDataNode(char type, float[] params) {
            this.mType = type;
            this.mParams = params;
        }

        PathDataNode(PathDataNode n) {
            this.mType = n.mType;
            this.mParams = PathParser.copyOfRange(n.mParams, 0, n.mParams.length);
        }

        public static void nodesToPath(PathDataNode[] node, Path path) {
            float[] current = new float[6];
            char previousCommand = 'm';
            for (int i = 0; i < node.length; i++) {
                addCommand(path, current, previousCommand, node[i].mType, node[i].mParams);
                previousCommand = node[i].mType;
            }
        }

        public void interpolatePathDataNode(PathDataNode nodeFrom, PathDataNode nodeTo, float fraction) {
            for (int i = 0; i < nodeFrom.mParams.length; i++) {
                this.mParams[i] = (nodeFrom.mParams[i] * (1.0f - fraction)) + (nodeTo.mParams[i] * fraction);
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private static void addCommand(android.graphics.Path r29, float[] r30, char r31, char r32, float[] r33) {
            /*
            r10 = r29;
            r13 = r33;
            r0 = 2;
            r14 = 0;
            r1 = r30[r14];
            r15 = 1;
            r2 = r30[r15];
            r16 = 2;
            r3 = r30[r16];
            r17 = 3;
            r4 = r30[r17];
            r18 = 4;
            r5 = r30[r18];
            r19 = 5;
            r6 = r30[r19];
            switch(r32) {
                case 65: goto L_0x0034;
                case 67: goto L_0x0032;
                case 72: goto L_0x0030;
                case 76: goto L_0x002e;
                case 77: goto L_0x002e;
                case 81: goto L_0x002c;
                case 83: goto L_0x002c;
                case 84: goto L_0x002e;
                case 86: goto L_0x0030;
                case 90: goto L_0x0021;
                case 97: goto L_0x0034;
                case 99: goto L_0x0032;
                case 104: goto L_0x0030;
                case 108: goto L_0x002e;
                case 109: goto L_0x002e;
                case 113: goto L_0x002c;
                case 115: goto L_0x002c;
                case 116: goto L_0x002e;
                case 118: goto L_0x0030;
                case 122: goto L_0x0021;
                default: goto L_0x001e;
            };
        L_0x001e:
            r20 = r0;
            goto L_0x0036;
        L_0x0021:
            r29.close();
            r1 = r5;
            r2 = r6;
            r3 = r5;
            r4 = r6;
            r10.moveTo(r1, r2);
            goto L_0x001e;
        L_0x002c:
            r0 = 4;
            goto L_0x001e;
        L_0x002e:
            r0 = 2;
            goto L_0x001e;
        L_0x0030:
            r0 = 1;
            goto L_0x001e;
        L_0x0032:
            r0 = 6;
            goto L_0x001e;
        L_0x0034:
            r0 = 7;
            goto L_0x001e;
        L_0x0036:
            r7 = r31;
            r9 = r1;
            r8 = r2;
            r21 = r3;
            r22 = r4;
            r23 = r5;
            r24 = r6;
            r0 = r14;
        L_0x0043:
            r6 = r0;
            r0 = r13.length;
            if (r6 >= r0) goto L_0x038c;
        L_0x0047:
            r0 = 115; // 0x73 float:1.61E-43 double:5.7E-322;
            r1 = 99;
            r2 = 84;
            r3 = 81;
            r4 = 116; // 0x74 float:1.63E-43 double:5.73E-322;
            r5 = 113; // 0x71 float:1.58E-43 double:5.6E-322;
            r25 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
            r15 = 0;
            switch(r32) {
                case 65: goto L_0x0344;
                case 67: goto L_0x030d;
                case 72: goto L_0x02fb;
                case 76: goto L_0x02e1;
                case 77: goto L_0x02b1;
                case 81: goto L_0x0287;
                case 83: goto L_0x0238;
                case 84: goto L_0x0205;
                case 86: goto L_0x01f3;
                case 97: goto L_0x01a3;
                case 99: goto L_0x0169;
                case 104: goto L_0x015a;
                case 108: goto L_0x0142;
                case 109: goto L_0x0114;
                case 113: goto L_0x00e9;
                case 115: goto L_0x009e;
                case 116: goto L_0x0071;
                case 118: goto L_0x0060;
                default: goto L_0x0059;
            };
        L_0x0059:
            r27 = r6;
            r14 = r7;
            r11 = r8;
            r12 = r9;
            goto L_0x0384;
        L_0x0060:
            r0 = r6 + 0;
            r0 = r13[r0];
            r10.rLineTo(r15, r0);
            r0 = r6 + 0;
            r0 = r13[r0];
            r8 = r8 + r0;
        L_0x006d:
            r27 = r6;
            goto L_0x01a0;
        L_0x0071:
            r0 = 0;
            r1 = 0;
            if (r7 == r5) goto L_0x007b;
        L_0x0075:
            if (r7 == r4) goto L_0x007b;
        L_0x0077:
            if (r7 == r3) goto L_0x007b;
        L_0x0079:
            if (r7 != r2) goto L_0x007f;
        L_0x007b:
            r0 = r9 - r21;
            r1 = r8 - r22;
        L_0x007f:
            r2 = r6 + 0;
            r2 = r13[r2];
            r3 = r6 + 1;
            r3 = r13[r3];
            r10.rQuadTo(r0, r1, r2, r3);
            r2 = r9 + r0;
            r3 = r8 + r1;
            r4 = r6 + 0;
            r4 = r13[r4];
            r9 = r9 + r4;
            r4 = r6 + 1;
            r4 = r13[r4];
            r8 = r8 + r4;
            r21 = r2;
            r22 = r3;
            goto L_0x006d;
        L_0x009e:
            r2 = 0;
            r3 = 0;
            if (r7 == r1) goto L_0x00b1;
        L_0x00a2:
            if (r7 == r0) goto L_0x00b1;
        L_0x00a4:
            r0 = 67;
            if (r7 == r0) goto L_0x00b1;
        L_0x00a8:
            r0 = 83;
            if (r7 != r0) goto L_0x00ad;
        L_0x00ac:
            goto L_0x00b1;
        L_0x00ad:
            r15 = r2;
            r25 = r3;
            goto L_0x00b8;
        L_0x00b1:
            r0 = r9 - r21;
            r1 = r8 - r22;
            r15 = r0;
            r25 = r1;
        L_0x00b8:
            r0 = r6 + 0;
            r3 = r13[r0];
            r0 = r6 + 1;
            r4 = r13[r0];
            r0 = r6 + 2;
            r5 = r13[r0];
            r0 = r6 + 3;
            r26 = r13[r0];
            r0 = r10;
            r1 = r15;
            r2 = r25;
            r27 = r6;
            r6 = r26;
            r0.rCubicTo(r1, r2, r3, r4, r5, r6);
            r6 = r27 + 0;
            r0 = r13[r6];
            r0 = r0 + r9;
            r6 = r27 + 1;
            r1 = r13[r6];
            r1 = r1 + r8;
            r6 = r27 + 2;
            r2 = r13[r6];
            r9 = r9 + r2;
            r6 = r27 + 3;
            r2 = r13[r6];
            r8 = r8 + r2;
            goto L_0x019c;
        L_0x00e9:
            r27 = r6;
            r6 = r27 + 0;
            r0 = r13[r6];
            r6 = r27 + 1;
            r1 = r13[r6];
            r6 = r27 + 2;
            r2 = r13[r6];
            r6 = r27 + 3;
            r3 = r13[r6];
            r10.rQuadTo(r0, r1, r2, r3);
            r6 = r27 + 0;
            r0 = r13[r6];
            r0 = r0 + r9;
            r6 = r27 + 1;
            r1 = r13[r6];
            r1 = r1 + r8;
            r6 = r27 + 2;
            r2 = r13[r6];
            r9 = r9 + r2;
            r6 = r27 + 3;
            r2 = r13[r6];
            r8 = r8 + r2;
            goto L_0x019c;
        L_0x0114:
            r27 = r6;
            r6 = r27 + 0;
            r0 = r13[r6];
            r9 = r9 + r0;
            r6 = r27 + 1;
            r0 = r13[r6];
            r8 = r8 + r0;
            if (r27 <= 0) goto L_0x012f;
        L_0x0122:
            r6 = r27 + 0;
            r0 = r13[r6];
            r6 = r27 + 1;
            r1 = r13[r6];
            r10.rLineTo(r0, r1);
            goto L_0x01a0;
        L_0x012f:
            r6 = r27 + 0;
            r0 = r13[r6];
            r6 = r27 + 1;
            r1 = r13[r6];
            r10.rMoveTo(r0, r1);
            r0 = r9;
            r1 = r8;
            r23 = r0;
            r24 = r1;
            goto L_0x01a0;
        L_0x0142:
            r27 = r6;
            r6 = r27 + 0;
            r0 = r13[r6];
            r6 = r27 + 1;
            r1 = r13[r6];
            r10.rLineTo(r0, r1);
            r6 = r27 + 0;
            r0 = r13[r6];
            r9 = r9 + r0;
            r6 = r27 + 1;
            r0 = r13[r6];
            r8 = r8 + r0;
            goto L_0x01a0;
        L_0x015a:
            r27 = r6;
            r6 = r27 + 0;
            r0 = r13[r6];
            r10.rLineTo(r0, r15);
            r6 = r27 + 0;
            r0 = r13[r6];
            r9 = r9 + r0;
            goto L_0x01a0;
        L_0x0169:
            r27 = r6;
            r6 = r27 + 0;
            r1 = r13[r6];
            r6 = r27 + 1;
            r2 = r13[r6];
            r6 = r27 + 2;
            r3 = r13[r6];
            r6 = r27 + 3;
            r4 = r13[r6];
            r6 = r27 + 4;
            r5 = r13[r6];
            r6 = r27 + 5;
            r6 = r13[r6];
            r0 = r10;
            r0.rCubicTo(r1, r2, r3, r4, r5, r6);
            r6 = r27 + 2;
            r0 = r13[r6];
            r0 = r0 + r9;
            r6 = r27 + 3;
            r1 = r13[r6];
            r1 = r1 + r8;
            r6 = r27 + 4;
            r2 = r13[r6];
            r9 = r9 + r2;
            r6 = r27 + 5;
            r2 = r13[r6];
            r8 = r8 + r2;
        L_0x019c:
            r21 = r0;
            r22 = r1;
        L_0x01a0:
            r14 = r7;
            goto L_0x0384;
        L_0x01a3:
            r27 = r6;
            r6 = r27 + 5;
            r0 = r13[r6];
            r3 = r0 + r9;
            r6 = r27 + 6;
            r0 = r13[r6];
            r4 = r0 + r8;
            r6 = r27 + 0;
            r5 = r13[r6];
            r6 = r27 + 1;
            r6 = r13[r6];
            r0 = r27 + 2;
            r25 = r13[r0];
            r0 = r27 + 3;
            r0 = r13[r0];
            r0 = (r0 > r15 ? 1 : (r0 == r15 ? 0 : -1));
            if (r0 == 0) goto L_0x01c8;
        L_0x01c5:
            r26 = 1;
            goto L_0x01ca;
        L_0x01c8:
            r26 = r14;
        L_0x01ca:
            r0 = r27 + 4;
            r0 = r13[r0];
            r0 = (r0 > r15 ? 1 : (r0 == r15 ? 0 : -1));
            if (r0 == 0) goto L_0x01d4;
        L_0x01d2:
            r15 = 1;
            goto L_0x01d5;
        L_0x01d4:
            r15 = r14;
        L_0x01d5:
            r0 = r10;
            r1 = r9;
            r2 = r8;
            r14 = r7;
            r7 = r25;
            r11 = r8;
            r8 = r26;
            r12 = r9;
            r9 = r15;
            drawArc(r0, r1, r2, r3, r4, r5, r6, r7, r8, r9);
            r6 = r27 + 5;
            r0 = r13[r6];
            r9 = r12 + r0;
            r6 = r27 + 6;
            r0 = r13[r6];
            r8 = r11 + r0;
            r0 = r9;
            r1 = r8;
            goto L_0x033f;
        L_0x01f3:
            r27 = r6;
            r14 = r7;
            r11 = r8;
            r12 = r9;
            r6 = r27 + 0;
            r0 = r13[r6];
            r10.lineTo(r12, r0);
            r6 = r27 + 0;
            r8 = r13[r6];
            goto L_0x0384;
        L_0x0205:
            r27 = r6;
            r14 = r7;
            r11 = r8;
            r12 = r9;
            r0 = r12;
            r1 = r11;
            if (r14 == r5) goto L_0x0214;
        L_0x020e:
            if (r14 == r4) goto L_0x0214;
        L_0x0210:
            if (r14 == r3) goto L_0x0214;
        L_0x0212:
            if (r14 != r2) goto L_0x021c;
        L_0x0214:
            r9 = r25 * r12;
            r0 = r9 - r21;
            r25 = r25 * r11;
            r1 = r25 - r22;
        L_0x021c:
            r6 = r27 + 0;
            r2 = r13[r6];
            r6 = r27 + 1;
            r3 = r13[r6];
            r10.quadTo(r0, r1, r2, r3);
            r2 = r0;
            r3 = r1;
            r6 = r27 + 0;
            r9 = r13[r6];
            r6 = r27 + 1;
            r8 = r13[r6];
            r21 = r2;
            r22 = r3;
            goto L_0x0384;
        L_0x0238:
            r27 = r6;
            r14 = r7;
            r11 = r8;
            r12 = r9;
            r2 = r12;
            r3 = r11;
            if (r14 == r1) goto L_0x0250;
        L_0x0241:
            if (r14 == r0) goto L_0x0250;
        L_0x0243:
            r0 = 67;
            if (r14 == r0) goto L_0x0250;
        L_0x0247:
            r0 = 83;
            if (r14 != r0) goto L_0x024c;
        L_0x024b:
            goto L_0x0250;
        L_0x024c:
            r9 = r2;
            r25 = r3;
            goto L_0x0258;
        L_0x0250:
            r9 = r25 * r12;
            r9 = r9 - r21;
            r25 = r25 * r11;
            r25 = r25 - r22;
        L_0x0258:
            r6 = r27 + 0;
            r3 = r13[r6];
            r6 = r27 + 1;
            r4 = r13[r6];
            r6 = r27 + 2;
            r5 = r13[r6];
            r6 = r27 + 3;
            r6 = r13[r6];
            r0 = r10;
            r1 = r9;
            r2 = r25;
            r0.cubicTo(r1, r2, r3, r4, r5, r6);
            r6 = r27 + 0;
            r0 = r13[r6];
            r6 = r27 + 1;
            r1 = r13[r6];
            r6 = r27 + 2;
            r2 = r13[r6];
            r6 = r27 + 3;
            r8 = r13[r6];
            r21 = r0;
            r22 = r1;
            r9 = r2;
            goto L_0x0384;
        L_0x0287:
            r27 = r6;
            r14 = r7;
            r11 = r8;
            r12 = r9;
            r6 = r27 + 0;
            r0 = r13[r6];
            r6 = r27 + 1;
            r1 = r13[r6];
            r6 = r27 + 2;
            r2 = r13[r6];
            r6 = r27 + 3;
            r3 = r13[r6];
            r10.quadTo(r0, r1, r2, r3);
            r6 = r27 + 0;
            r0 = r13[r6];
            r6 = r27 + 1;
            r1 = r13[r6];
            r6 = r27 + 2;
            r9 = r13[r6];
            r6 = r27 + 3;
            r8 = r13[r6];
            goto L_0x033f;
        L_0x02b1:
            r27 = r6;
            r14 = r7;
            r11 = r8;
            r12 = r9;
            r6 = r27 + 0;
            r9 = r13[r6];
            r6 = r27 + 1;
            r8 = r13[r6];
            if (r27 <= 0) goto L_0x02cd;
        L_0x02c0:
            r6 = r27 + 0;
            r0 = r13[r6];
            r6 = r27 + 1;
            r1 = r13[r6];
            r10.lineTo(r0, r1);
            goto L_0x0384;
        L_0x02cd:
            r6 = r27 + 0;
            r0 = r13[r6];
            r6 = r27 + 1;
            r1 = r13[r6];
            r10.moveTo(r0, r1);
            r0 = r9;
            r1 = r8;
            r23 = r0;
            r24 = r1;
            goto L_0x0384;
        L_0x02e1:
            r27 = r6;
            r14 = r7;
            r11 = r8;
            r12 = r9;
            r6 = r27 + 0;
            r0 = r13[r6];
            r6 = r27 + 1;
            r1 = r13[r6];
            r10.lineTo(r0, r1);
            r6 = r27 + 0;
            r9 = r13[r6];
            r6 = r27 + 1;
            r8 = r13[r6];
            goto L_0x0384;
        L_0x02fb:
            r27 = r6;
            r14 = r7;
            r11 = r8;
            r12 = r9;
            r6 = r27 + 0;
            r0 = r13[r6];
            r10.lineTo(r0, r11);
            r6 = r27 + 0;
            r9 = r13[r6];
            goto L_0x0384;
        L_0x030d:
            r27 = r6;
            r14 = r7;
            r11 = r8;
            r12 = r9;
            r6 = r27 + 0;
            r1 = r13[r6];
            r6 = r27 + 1;
            r2 = r13[r6];
            r6 = r27 + 2;
            r3 = r13[r6];
            r6 = r27 + 3;
            r4 = r13[r6];
            r6 = r27 + 4;
            r5 = r13[r6];
            r6 = r27 + 5;
            r6 = r13[r6];
            r0 = r10;
            r0.cubicTo(r1, r2, r3, r4, r5, r6);
            r6 = r27 + 4;
            r9 = r13[r6];
            r6 = r27 + 5;
            r8 = r13[r6];
            r6 = r27 + 2;
            r0 = r13[r6];
            r6 = r27 + 3;
            r1 = r13[r6];
        L_0x033f:
            r21 = r0;
            r22 = r1;
            goto L_0x0384;
        L_0x0344:
            r27 = r6;
            r14 = r7;
            r11 = r8;
            r12 = r9;
            r6 = r27 + 5;
            r3 = r13[r6];
            r6 = r27 + 6;
            r4 = r13[r6];
            r6 = r27 + 0;
            r5 = r13[r6];
            r6 = r27 + 1;
            r6 = r13[r6];
            r0 = r27 + 2;
            r7 = r13[r0];
            r0 = r27 + 3;
            r0 = r13[r0];
            r0 = (r0 > r15 ? 1 : (r0 == r15 ? 0 : -1));
            if (r0 == 0) goto L_0x0367;
        L_0x0365:
            r8 = 1;
            goto L_0x0368;
        L_0x0367:
            r8 = 0;
        L_0x0368:
            r0 = r27 + 4;
            r0 = r13[r0];
            r0 = (r0 > r15 ? 1 : (r0 == r15 ? 0 : -1));
            if (r0 == 0) goto L_0x0372;
        L_0x0370:
            r9 = 1;
            goto L_0x0373;
        L_0x0372:
            r9 = 0;
        L_0x0373:
            r0 = r10;
            r1 = r12;
            r2 = r11;
            drawArc(r0, r1, r2, r3, r4, r5, r6, r7, r8, r9);
            r6 = r27 + 5;
            r9 = r13[r6];
            r6 = r27 + 6;
            r8 = r13[r6];
            r0 = r9;
            r1 = r8;
            goto L_0x033f;
        L_0x0384:
            r7 = r32;
            r0 = r27 + r20;
            r14 = 0;
            r15 = 1;
            goto L_0x0043;
        L_0x038c:
            r14 = r7;
            r11 = r8;
            r2 = r11;
            r1 = 0;
            r30[r1] = r9;
            r1 = 1;
            r30[r1] = r2;
            r30[r16] = r21;
            r30[r17] = r22;
            r30[r18] = r23;
            r30[r19] = r24;
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: android.support.v4.graphics.PathParser.PathDataNode.addCommand(android.graphics.Path, float[], char, char, float[]):void");
        }

        private static void drawArc(Path p, float x0, float y0, float x1, float y1, float a, float b, float theta, boolean isMoreThanHalf, boolean isPositiveArc) {
            float f = x0;
            float f2 = y0;
            float f3 = x1;
            float f4 = y1;
            float f5 = a;
            float f6 = b;
            boolean z = isPositiveArc;
            float f7 = theta;
            double thetaD = Math.toRadians((double) f7);
            double cosTheta = Math.cos(thetaD);
            double sinTheta = Math.sin(thetaD);
            double x0p = ((((double) f) * cosTheta) + (((double) f2) * sinTheta)) / ((double) f5);
            double y0p = ((((double) (-f)) * sinTheta) + (((double) f2) * cosTheta)) / ((double) f6);
            double x1p = ((((double) f3) * cosTheta) + (((double) f4) * sinTheta)) / ((double) f5);
            double y1p = ((((double) (-f3)) * sinTheta) + (((double) f4) * cosTheta)) / ((double) f6);
            double dx = x0p - x1p;
            double dy = y0p - y1p;
            double xm = (x0p + x1p) / 2.0d;
            double ym = (y0p + y1p) / 2.0d;
            double dsq = (dx * dx) + (dy * dy);
            if (dsq == 0.0d) {
                Log.w(PathParser.LOGTAG, " Points are coincident");
                return;
            }
            double disc = (1.0d / dsq) - 0.25d;
            if (disc < 0.0d) {
                String str = PathParser.LOGTAG;
                StringBuilder stringBuilder = new StringBuilder();
                double disc2 = disc;
                stringBuilder.append("Points are too far apart ");
                stringBuilder.append(dsq);
                Log.w(str, stringBuilder.toString());
                float adjust = (float) (Math.sqrt(dsq) / 1.99999d);
                drawArc(p, f, f2, f3, f4, f5 * adjust, f6 * adjust, f7, isMoreThanHalf, isPositiveArc);
                return;
            }
            double cx;
            double cy;
            disc2 = dsq;
            double thetaD2 = thetaD;
            disc = Math.sqrt(disc);
            dsq = disc * dx;
            thetaD = disc * dy;
            z = isPositiveArc;
            if (isMoreThanHalf == z) {
                cx = xm - thetaD;
                cy = ym + dsq;
            } else {
                cx = xm + thetaD;
                cy = ym - dsq;
            }
            double eta0 = Math.atan2(y0p - cy, x0p - cx);
            disc = Math.atan2(y1p - cy, x1p - cx);
            dsq = disc - eta0;
            if (z != (dsq >= 0.0d ? 1 : null)) {
                if (dsq > 0.0d) {
                    dsq -= 6.283185307179586d;
                } else {
                    dsq += 6.283185307179586d;
                }
            }
            float f8 = a;
            cx *= (double) f8;
            adjust = b;
            cy *= (double) adjust;
            thetaD = cx;
            arcToBezier(p, (cx * cosTheta) - (cy * sinTheta), (thetaD * sinTheta) + (cy * cosTheta), (double) f8, (double) adjust, (double) f, (double) f2, thetaD2, eta0, dsq);
        }

        private static void arcToBezier(Path p, double cx, double cy, double a, double b, double e1x, double e1y, double theta, double start, double sweep) {
            double d = a;
            int numSegments = (int) Math.ceil(Math.abs((sweep * 4.0d) / 3.141592653589793d));
            double eta1 = start;
            double cosTheta = Math.cos(theta);
            double sinTheta = Math.sin(theta);
            double cosEta1 = Math.cos(eta1);
            double sinEta1 = Math.sin(eta1);
            double anglePerSegment = sweep / ((double) numSegments);
            int i = 0;
            double ep1y = (((-d) * sinTheta) * sinEta1) + ((b * cosTheta) * cosEta1);
            double e1y2 = e1y;
            double ep1x = (((-d) * cosTheta) * sinEta1) - ((b * sinTheta) * cosEta1);
            double e1x2 = e1x;
            while (true) {
                int i2 = i;
                int numSegments2;
                double cosTheta2;
                double sinTheta2;
                if (i2 < numSegments) {
                    int i3 = i2;
                    double eta2 = eta1 + anglePerSegment;
                    double sinEta2 = Math.sin(eta2);
                    double cosEta2 = Math.cos(eta2);
                    double anglePerSegment2 = anglePerSegment;
                    double e2x = (cx + ((d * cosTheta) * cosEta2)) - ((b * sinTheta) * sinEta2);
                    double e2y = (cy + ((d * sinTheta) * cosEta2)) + ((b * cosTheta) * sinEta2);
                    double ep2x = (((-d) * cosTheta) * sinEta2) - ((b * sinTheta) * cosEta2);
                    anglePerSegment = (((-d) * sinTheta) * sinEta2) + ((b * cosTheta) * cosEta2);
                    d = Math.tan((eta2 - eta1) / 2.0d);
                    double alpha = (Math.sin(eta2 - eta1) * (Math.sqrt(4.0d + ((3.0d * d) * d)) - 1.0d)) / 3.0d;
                    d = e1x2 + (alpha * ep1x);
                    numSegments2 = numSegments;
                    double q1y = e1y2 + (alpha * ep1y);
                    cosTheta2 = cosTheta;
                    double q2x = e2x - (alpha * ep2x);
                    alpha = e2y - (alpha * anglePerSegment);
                    sinTheta2 = sinTheta;
                    Path path = p;
                    path.rLineTo(0.0f, 0.0f);
                    float f = (float) alpha;
                    alpha = e2x;
                    eta1 = e2y;
                    path.cubicTo((float) d, (float) q1y, (float) q2x, f, (float) alpha, (float) eta1);
                    e1x2 = alpha;
                    e1y2 = eta1;
                    ep1x = ep2x;
                    ep1y = anglePerSegment;
                    i = i3 + 1;
                    eta1 = eta2;
                    anglePerSegment = anglePerSegment2;
                    numSegments = numSegments2;
                    cosTheta = cosTheta2;
                    sinTheta = sinTheta2;
                    d = a;
                } else {
                    numSegments2 = numSegments;
                    double d2 = eta1;
                    cosTheta2 = cosTheta;
                    sinTheta2 = sinTheta;
                    sinTheta = p;
                    return;
                }
            }
        }
    }

    static float[] copyOfRange(float[] original, int start, int end) {
        if (start > end) {
            throw new IllegalArgumentException();
        }
        int originalLength = original.length;
        if (start >= 0) {
            if (start <= originalLength) {
                int resultLength = end - start;
                float[] result = new float[resultLength];
                System.arraycopy(original, start, result, 0, Math.min(resultLength, originalLength - start));
                return result;
            }
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public static Path createPathFromPathData(String pathData) {
        Path path = new Path();
        PathDataNode[] nodes = createNodesFromPathData(pathData);
        if (nodes == null) {
            return null;
        }
        try {
            PathDataNode.nodesToPath(nodes, path);
            return path;
        } catch (RuntimeException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Error in parsing ");
            stringBuilder.append(pathData);
            throw new RuntimeException(stringBuilder.toString(), e);
        }
    }

    public static PathDataNode[] createNodesFromPathData(String pathData) {
        if (pathData == null) {
            return null;
        }
        int start = 0;
        int end = 1;
        ArrayList<PathDataNode> list = new ArrayList();
        while (end < pathData.length()) {
            end = nextStart(pathData, end);
            String s = pathData.substring(start, end).trim();
            if (s.length() > 0) {
                addNode(list, s.charAt(0), getFloats(s));
            }
            start = end;
            end++;
        }
        if (end - start == 1 && start < pathData.length()) {
            addNode(list, pathData.charAt(start), new float[0]);
        }
        return (PathDataNode[]) list.toArray(new PathDataNode[list.size()]);
    }

    public static PathDataNode[] deepCopyNodes(PathDataNode[] source) {
        if (source == null) {
            return null;
        }
        PathDataNode[] copy = new PathDataNode[source.length];
        for (int i = 0; i < source.length; i++) {
            copy[i] = new PathDataNode(source[i]);
        }
        return copy;
    }

    public static boolean canMorph(PathDataNode[] nodesFrom, PathDataNode[] nodesTo) {
        if (nodesFrom != null) {
            if (nodesTo != null) {
                if (nodesFrom.length != nodesTo.length) {
                    return false;
                }
                int i = 0;
                while (i < nodesFrom.length) {
                    if (nodesFrom[i].mType == nodesTo[i].mType) {
                        if (nodesFrom[i].mParams.length == nodesTo[i].mParams.length) {
                            i++;
                        }
                    }
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public static void updateNodes(PathDataNode[] target, PathDataNode[] source) {
        for (int i = 0; i < source.length; i++) {
            target[i].mType = source[i].mType;
            for (int j = 0; j < source[i].mParams.length; j++) {
                target[i].mParams[j] = source[i].mParams[j];
            }
        }
    }

    private static int nextStart(String s, int end) {
        while (end < s.length()) {
            char c = s.charAt(end);
            if (((c - 65) * (c - 90) <= 0 || (c - 97) * (c - 122) <= 0) && c != 'e' && c != 'E') {
                return end;
            }
            end++;
        }
        return end;
    }

    private static void addNode(ArrayList<PathDataNode> list, char cmd, float[] val) {
        list.add(new PathDataNode(cmd, val));
    }

    private static float[] getFloats(String s) {
        if (s.charAt(0) != 'z') {
            if (s.charAt(0) != 'Z') {
                try {
                    float[] results = new float[s.length()];
                    int count = 0;
                    int startPosition = 1;
                    ExtractFloatResult result = new ExtractFloatResult();
                    int totalLength = s.length();
                    while (startPosition < totalLength) {
                        extract(s, startPosition, result);
                        int endPosition = result.mEndPosition;
                        if (startPosition < endPosition) {
                            int count2 = count + 1;
                            results[count] = Float.parseFloat(s.substring(startPosition, endPosition));
                            count = count2;
                        }
                        if (result.mEndWithNegOrDot) {
                            startPosition = endPosition;
                        } else {
                            startPosition = endPosition + 1;
                        }
                    }
                    return copyOfRange(results, 0, count);
                } catch (NumberFormatException e) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("error in parsing \"");
                    stringBuilder.append(s);
                    stringBuilder.append("\"");
                    throw new RuntimeException(stringBuilder.toString(), e);
                }
            }
        }
        return new float[0];
    }

    private static void extract(String s, int start, ExtractFloatResult result) {
        int currentIndex = start;
        boolean foundSeparator = false;
        boolean isExponential = false;
        result.mEndWithNegOrDot = false;
        boolean secondDot = false;
        while (currentIndex < s.length()) {
            boolean isPrevExponential = isExponential;
            isExponential = false;
            char currentChar = s.charAt(currentIndex);
            if (currentChar != ' ') {
                if (currentChar != 'E' && currentChar != 'e') {
                    switch (currentChar) {
                        case ',':
                            break;
                        case '-':
                            if (!(currentIndex == start || isPrevExponential)) {
                                foundSeparator = true;
                                result.mEndWithNegOrDot = true;
                                break;
                            }
                        case '.':
                            if (!secondDot) {
                                secondDot = true;
                                break;
                            }
                            foundSeparator = true;
                            result.mEndWithNegOrDot = true;
                            break;
                        default:
                            break;
                    }
                }
                isExponential = true;
                if (foundSeparator) {
                    currentIndex++;
                } else {
                    result.mEndPosition = currentIndex;
                }
            }
            foundSeparator = true;
            if (foundSeparator) {
                currentIndex++;
            } else {
                result.mEndPosition = currentIndex;
            }
        }
        result.mEndPosition = currentIndex;
    }
}
