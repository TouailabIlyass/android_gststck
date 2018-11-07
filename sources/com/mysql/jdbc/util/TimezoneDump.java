package com.mysql.jdbc.util;

import com.mysql.jdbc.TimeUtil;
import java.io.PrintStream;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class TimezoneDump {
    private static final String DEFAULT_URL = "jdbc:mysql:///test";

    public static void main(String[] args) throws Exception {
        String jdbcUrl = DEFAULT_URL;
        if (args.length == 1 && args[0] != null) {
            jdbcUrl = args[0];
        }
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        ResultSet resultSet = null;
        ResultSet resultSet2 = null;
        try {
            resultSet2 = DriverManager.getConnection(jdbcUrl).createStatement().executeQuery("SHOW VARIABLES LIKE 'timezone'");
            while (resultSet2.next()) {
                String timezoneFromServer = resultSet2.getString(2);
                PrintStream printStream = System.out;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("MySQL timezone name: ");
                stringBuilder.append(timezoneFromServer);
                printStream.println(stringBuilder.toString());
                String canonicalTimezone = TimeUtil.getCanonicalTimezone(timezoneFromServer, null);
                PrintStream printStream2 = System.out;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Java timezone name: ");
                stringBuilder2.append(canonicalTimezone);
                printStream2.println(stringBuilder2.toString());
            }
            resultSet = resultSet2;
        } finally {
            resultSet2 = 
/*
Method generation error in method: com.mysql.jdbc.util.TimezoneDump.main(java.lang.String[]):void, dex: classes.dex
jadx.core.utils.exceptions.CodegenException: Error generate insn: ?: MERGE  (r2_5 'resultSet2' java.sql.ResultSet) = (r2_4 'resultSet2' java.sql.ResultSet), (r1_6 'resultSet' java.sql.ResultSet) in method: com.mysql.jdbc.util.TimezoneDump.main(java.lang.String[]):void, dex: classes.dex
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:226)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:203)
	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:100)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:50)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeTryCatch(RegionGen.java:299)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:63)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:187)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:320)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:257)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:220)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:110)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:75)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:12)
	at jadx.core.ProcessClass.process(ProcessClass.java:40)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:282)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
	at jadx.api.JadxDecompiler$$Lambda$8/1205406622.run(Unknown Source)
Caused by: jadx.core.utils.exceptions.CodegenException: MERGE can be used only in fallback mode
	at jadx.core.codegen.InsnGen.fallbackOnlyInsn(InsnGen.java:537)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:509)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
	... 22 more

*/
        }
