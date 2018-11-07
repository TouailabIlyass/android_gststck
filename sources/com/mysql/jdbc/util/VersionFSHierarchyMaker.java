package com.mysql.jdbc.util;

import com.mysql.jdbc.NonRegisteringDriver;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.util.Properties;

public class VersionFSHierarchyMaker {
    public static void main(String[] args) throws Exception {
        Throwable th;
        File osVersionDir;
        FileOutputStream pathOut;
        File jvmVersionDir;
        Throwable th2;
        FileOutputStream pathOut2;
        String[] args2 = args;
        if (args2.length < 3) {
            usage();
            System.exit(1);
        }
        String jvmVersion = removeWhitespaceChars(System.getProperty("java.version"));
        String jvmVendor = removeWhitespaceChars(System.getProperty("java.vendor"));
        String osName = removeWhitespaceChars(System.getProperty("os.name"));
        String osArch = removeWhitespaceChars(System.getProperty("os.arch"));
        String osVersion = removeWhitespaceChars(System.getProperty("os.version"));
        String jdbcUrl = System.getProperty("com.mysql.jdbc.testsuite.url");
        String mysqlVersion = new StringBuilder();
        mysqlVersion.append("MySQL");
        mysqlVersion.append(args2[2]);
        mysqlVersion.append("_");
        mysqlVersion = mysqlVersion.toString();
        try {
            Properties props = new Properties();
            props.setProperty("allowPublicKeyRetrieval", "true");
            ResultSet rs = new NonRegisteringDriver().connect(jdbcUrl, props).createStatement().executeQuery("SELECT VERSION()");
            rs.next();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(mysqlVersion);
            stringBuilder.append(removeWhitespaceChars(rs.getString(1)));
            mysqlVersion = stringBuilder.toString();
        } catch (Throwable th3) {
            Throwable t = th3;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append(mysqlVersion);
            stringBuilder2.append("no-server-running-on-");
            stringBuilder2.append(removeWhitespaceChars(jdbcUrl));
            mysqlVersion = stringBuilder2.toString();
        }
        String jvmSubdirName = new StringBuilder();
        jvmSubdirName.append(jvmVendor);
        jvmSubdirName.append("-");
        jvmSubdirName.append(jvmVersion);
        jvmSubdirName = jvmSubdirName.toString();
        String osSubdirName = new StringBuilder();
        osSubdirName.append(osName);
        osSubdirName.append("-");
        osSubdirName.append(osArch);
        osSubdirName.append("-");
        osSubdirName.append(osVersion);
        osSubdirName = osSubdirName.toString();
        File baseDir = new File(args2[0]);
        File osVersionDir2 = new File(new File(baseDir, mysqlVersion), osSubdirName);
        File jvmVersionDir2 = new File(osVersionDir2, jvmSubdirName);
        jvmVersionDir2.mkdirs();
        FileOutputStream pathOut3 = null;
        try {
            osVersionDir = osVersionDir2;
            String propsOutputPath = args2[1];
            try {
                pathOut = new FileOutputStream(propsOutputPath);
                try {
                    jvmVersionDir = jvmVersionDir2;
                    propsOutputPath = baseDir.getAbsolutePath();
                    jvmVersionDir2 = jvmVersionDir2.getAbsolutePath();
                } catch (Throwable th4) {
                    th3 = th4;
                    jvmVersionDir = jvmVersionDir2;
                    pathOut3 = pathOut;
                    th2 = th3;
                    osVersionDir2 = osVersionDir;
                    jvmVersionDir2 = jvmVersionDir;
                    pathOut2 = pathOut3;
                    if (pathOut2 != null) {
                        pathOut2.flush();
                        pathOut2.close();
                    }
                    throw th2;
                }
            } catch (Throwable th5) {
                th3 = th5;
                jvmVersionDir = jvmVersionDir2;
                th2 = th3;
                osVersionDir2 = osVersionDir;
                jvmVersionDir2 = jvmVersionDir;
                pathOut2 = pathOut3;
                if (pathOut2 != null) {
                    pathOut2.flush();
                    pathOut2.close();
                }
                throw th2;
            }
            try {
                if (jvmVersionDir2.startsWith(propsOutputPath)) {
                    jvmVersionDir2 = jvmVersionDir2.substring(propsOutputPath.length() + 1);
                }
                pathOut.write(jvmVersionDir2.getBytes());
                if (pathOut != null) {
                    pathOut.flush();
                    pathOut.close();
                }
            } catch (Throwable th32) {
                pathOut3 = pathOut;
                th2 = th32;
                osVersionDir2 = osVersionDir;
                jvmVersionDir2 = jvmVersionDir;
                pathOut2 = pathOut3;
                if (pathOut2 != null) {
                    pathOut2.flush();
                    pathOut2.close();
                }
                throw th2;
            }
        } catch (Throwable th322) {
            osVersionDir = osVersionDir2;
            jvmVersionDir = jvmVersionDir2;
            th2 = th322;
            osVersionDir2 = osVersionDir;
            jvmVersionDir2 = jvmVersionDir;
            pathOut2 = pathOut3;
            if (pathOut2 != null) {
                pathOut2.flush();
                pathOut2.close();
            }
            throw th2;
        }
    }

    public static String removeWhitespaceChars(String input) {
        if (input == null) {
            return input;
        }
        int strLen = input.length();
        StringBuilder output = new StringBuilder(strLen);
        for (int i = 0; i < strLen; i++) {
            char c = input.charAt(i);
            if (Character.isDigit(c) || Character.isLetter(c)) {
                output.append(c);
            } else if (Character.isWhitespace(c)) {
                output.append("_");
            } else {
                output.append(".");
            }
        }
        return output.toString();
    }

    private static void usage() {
        System.err.println("Creates a fs hierarchy representing MySQL version, OS version and JVM version.");
        System.err.println("Stores the full path as 'outputDirectory' property in file 'directoryPropPath'");
        System.err.println();
        System.err.println("Usage: java VersionFSHierarchyMaker baseDirectory directoryPropPath jdbcUrlIter");
    }
}
