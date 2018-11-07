package org.gradle.wrapper;

import java.io.File;
import java.math.BigInteger;
import java.net.URI;
import java.security.MessageDigest;

public class PathAssembler {
    public static final String GRADLE_USER_HOME_STRING = "GRADLE_USER_HOME";
    public static final String PROJECT_STRING = "PROJECT";
    private File gradleUserHome;

    public class LocalDistribution {
        private final File distDir;
        private final File distZip;

        public LocalDistribution(File distDir, File distZip) {
            this.distDir = distDir;
            this.distZip = distZip;
        }

        public File getDistributionDir() {
            return this.distDir;
        }

        public File getZipFile() {
            return this.distZip;
        }
    }

    public PathAssembler(File gradleUserHome) {
        this.gradleUserHome = gradleUserHome;
    }

    public LocalDistribution getDistribution(WrapperConfiguration configuration) {
        String baseName = getDistName(configuration.getDistribution());
        String rootDirName = rootDirName(removeExtension(baseName), configuration);
        File baseDir = getBaseDir(configuration.getDistributionBase());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(configuration.getDistributionPath());
        stringBuilder.append("/");
        stringBuilder.append(rootDirName);
        File distDir = new File(baseDir, stringBuilder.toString());
        File baseDir2 = getBaseDir(configuration.getZipBase());
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append(configuration.getZipPath());
        stringBuilder2.append("/");
        stringBuilder2.append(rootDirName);
        stringBuilder2.append("/");
        stringBuilder2.append(baseName);
        return new LocalDistribution(distDir, new File(baseDir2, stringBuilder2.toString()));
    }

    private String rootDirName(String distName, WrapperConfiguration configuration) {
        String urlHash = getMd5Hash(configuration.getDistribution().toString());
        return String.format("%s/%s", new Object[]{distName, urlHash});
    }

    private String getMd5Hash(String string) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(string.getBytes());
            return new BigInteger(1, messageDigest.digest()).toString(32);
        } catch (Exception e) {
            throw new RuntimeException("Could not hash input string.", e);
        }
    }

    private String removeExtension(String name) {
        int p = name.lastIndexOf(".");
        if (p < 0) {
            return name;
        }
        return name.substring(0, p);
    }

    private String getDistName(URI distUrl) {
        String path = distUrl.getPath();
        int p = path.lastIndexOf("/");
        if (p < 0) {
            return path;
        }
        return path.substring(p + 1);
    }

    private File getBaseDir(String base) {
        if (base.equals("GRADLE_USER_HOME")) {
            return this.gradleUserHome;
        }
        if (base.equals(PROJECT_STRING)) {
            return new File(System.getProperty("user.dir"));
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Base: ");
        stringBuilder.append(base);
        stringBuilder.append(" is unknown");
        throw new RuntimeException(stringBuilder.toString());
    }
}
