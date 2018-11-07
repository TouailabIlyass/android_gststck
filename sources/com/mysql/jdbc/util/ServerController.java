package com.mysql.jdbc.util;

import com.mysql.jdbc.StringUtils;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

public class ServerController {
    public static final String BASEDIR_KEY = "basedir";
    public static final String DATADIR_KEY = "datadir";
    public static final String DEFAULTS_FILE_KEY = "defaults-file";
    public static final String EXECUTABLE_NAME_KEY = "executable";
    public static final String EXECUTABLE_PATH_KEY = "executablePath";
    private Process serverProcess = null;
    private Properties serverProps = null;
    private Properties systemProps = null;

    public ServerController(String baseDir) {
        setBaseDir(baseDir);
    }

    public ServerController(String basedir, String datadir) {
    }

    public void setBaseDir(String baseDir) {
        getServerProps().setProperty(BASEDIR_KEY, baseDir);
    }

    public void setDataDir(String dataDir) {
        getServerProps().setProperty(DATADIR_KEY, dataDir);
    }

    public Process start() throws IOException {
        if (this.serverProcess != null) {
            throw new IllegalArgumentException("Server already started");
        }
        this.serverProcess = Runtime.getRuntime().exec(getCommandLine());
        return this.serverProcess;
    }

    public void stop(boolean forceIfNecessary) throws IOException {
        if (this.serverProcess != null) {
            String basedir = getServerProps().getProperty(BASEDIR_KEY);
            StringBuilder pathBuf = new StringBuilder(basedir);
            if (!basedir.endsWith(File.separator)) {
                pathBuf.append(File.separator);
            }
            pathBuf.append("bin");
            pathBuf.append(File.separator);
            pathBuf.append("mysqladmin shutdown");
            System.out.println(pathBuf.toString());
            int exitStatus = -1;
            try {
                exitStatus = Runtime.getRuntime().exec(pathBuf.toString()).waitFor();
            } catch (InterruptedException e) {
            }
            if (exitStatus != 0 && forceIfNecessary) {
                forceStop();
            }
        }
    }

    public void forceStop() {
        if (this.serverProcess != null) {
            this.serverProcess.destroy();
            this.serverProcess = null;
        }
    }

    public synchronized Properties getServerProps() {
        if (this.serverProps == null) {
            this.serverProps = new Properties();
        }
        return this.serverProps;
    }

    private String getCommandLine() {
        StringBuilder commandLine = new StringBuilder(getFullExecutablePath());
        commandLine.append(buildOptionalCommandLine());
        return commandLine.toString();
    }

    private String getFullExecutablePath() {
        StringBuilder pathBuf = new StringBuilder();
        String optionalExecutablePath = getServerProps().getProperty(EXECUTABLE_PATH_KEY);
        if (optionalExecutablePath == null) {
            String basedir = getServerProps().getProperty(BASEDIR_KEY);
            pathBuf.append(basedir);
            if (!basedir.endsWith(File.separator)) {
                pathBuf.append(File.separatorChar);
            }
            if (runningOnWindows()) {
                pathBuf.append("bin");
            } else {
                pathBuf.append("libexec");
            }
            pathBuf.append(File.separatorChar);
        } else {
            pathBuf.append(optionalExecutablePath);
            if (!optionalExecutablePath.endsWith(File.separator)) {
                pathBuf.append(File.separatorChar);
            }
        }
        pathBuf.append(getServerProps().getProperty(EXECUTABLE_NAME_KEY, "mysqld"));
        return pathBuf.toString();
    }

    private String buildOptionalCommandLine() {
        StringBuilder commandLineBuf = new StringBuilder();
        if (this.serverProps != null) {
            Iterator<Object> iter = this.serverProps.keySet().iterator();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                String value = this.serverProps.getProperty(key);
                if (!isNonCommandLineArgument(key)) {
                    if (value == null || value.length() <= 0) {
                        commandLineBuf.append(" --");
                        commandLineBuf.append(key);
                    } else {
                        commandLineBuf.append(" \"");
                        commandLineBuf.append("--");
                        commandLineBuf.append(key);
                        commandLineBuf.append("=");
                        commandLineBuf.append(value);
                        commandLineBuf.append("\"");
                    }
                }
            }
        }
        return commandLineBuf.toString();
    }

    private boolean isNonCommandLineArgument(String propName) {
        if (!propName.equals(EXECUTABLE_NAME_KEY)) {
            if (!propName.equals(EXECUTABLE_PATH_KEY)) {
                return false;
            }
        }
        return true;
    }

    private synchronized Properties getSystemProperties() {
        if (this.systemProps == null) {
            this.systemProps = System.getProperties();
        }
        return this.systemProps;
    }

    private boolean runningOnWindows() {
        return StringUtils.indexOfIgnoreCase(getSystemProperties().getProperty("os.name"), "WINDOWS") != -1;
    }
}
