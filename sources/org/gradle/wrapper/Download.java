package org.gradle.wrapper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

public class Download implements IDownload {
    private static final int BUFFER_SIZE = 10000;
    private static final int PROGRESS_CHUNK = 20000;
    private final String applicationName;
    private final String applicationVersion;

    private static class SystemPropertiesProxyAuthenticator extends Authenticator {
        private SystemPropertiesProxyAuthenticator() {
        }

        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(System.getProperty("http.proxyUser"), System.getProperty("http.proxyPassword", "").toCharArray());
        }
    }

    public Download(String applicationName, String applicationVersion) {
        this.applicationName = applicationName;
        this.applicationVersion = applicationVersion;
        configureProxyAuthentication();
    }

    private void configureProxyAuthentication() {
        if (System.getProperty("http.proxyUser") != null) {
            Authenticator.setDefault(new SystemPropertiesProxyAuthenticator());
        }
    }

    public void download(URI address, File destination) throws Exception {
        destination.getParentFile().mkdirs();
        downloadInternal(address, destination);
    }

    private void downloadInternal(URI address, File destination) throws Exception {
        Throwable th;
        URLConnection conn;
        OutputStream out;
        Throwable th2;
        InputStream in = null;
        try {
            URL url = address.toURL();
            try {
                OutputStream out2 = new BufferedOutputStream(new FileOutputStream(destination));
                URLConnection out3 = url.openConnection();
                out3.setRequestProperty("User-Agent", calculateUserAgent());
                in = out3.getInputStream();
                byte[] buffer = new byte[10000];
                long progressCounter = 0;
                while (true) {
                    int read = in.read(buffer);
                    int numRead = read;
                    if (read == -1) {
                        break;
                    }
                    long progressCounter2 = progressCounter + ((long) numRead);
                    if (progressCounter2 / 20000 > 0) {
                        System.out.print(".");
                        progressCounter = progressCounter2 - 20000;
                    } else {
                        progressCounter = progressCounter2;
                    }
                    out2.write(buffer, 0, numRead);
                }
                System.out.println("");
                if (in != null) {
                    in.close();
                }
                if (out2 != null) {
                    out2.close();
                }
            } catch (Throwable th3) {
                th = th3;
                conn = null;
                out = null;
                th2 = th;
                System.out.println("");
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                throw th2;
            }
        } catch (Throwable th4) {
            th = th4;
            File file = destination;
            conn = null;
            out = null;
            th2 = th;
            System.out.println("");
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            throw th2;
        }
    }

    private String calculateUserAgent() {
        String appVersion = this.applicationVersion;
        String javaVendor = System.getProperty("java.vendor");
        String javaVersion = System.getProperty("java.version");
        String javaVendorVersion = System.getProperty("java.vm.version");
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String osArch = System.getProperty("os.arch");
        return String.format("%s/%s (%s;%s;%s) (%s;%s;%s)", new Object[]{this.applicationName, appVersion, osName, osVersion, osArch, javaVendor, javaVersion, javaVendorVersion});
    }
}
