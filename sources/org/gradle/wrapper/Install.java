package org.gradle.wrapper;

import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.gradle.wrapper.PathAssembler.LocalDistribution;

public class Install {
    public static final String DEFAULT_DISTRIBUTION_PATH = "wrapper/dists";
    private final IDownload download;
    private final ExclusiveFileAccessManager exclusiveFileAccessManager = new ExclusiveFileAccessManager(120000, Callback.DEFAULT_DRAG_ANIMATION_DURATION);
    private final PathAssembler pathAssembler;

    public Install(IDownload download, PathAssembler pathAssembler) {
        this.download = download;
        this.pathAssembler = pathAssembler;
    }

    public File createDist(WrapperConfiguration configuration) throws Exception {
        final URI distributionUrl = configuration.getDistribution();
        LocalDistribution localDistribution = this.pathAssembler.getDistribution(configuration);
        final File distDir = localDistribution.getDistributionDir();
        final File localZipFile = localDistribution.getZipFile();
        return (File) this.exclusiveFileAccessManager.access(localZipFile, new Callable<File>() {
            public File call() throws Exception {
                File parentFile = localZipFile.getParentFile();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(localZipFile.getName());
                stringBuilder.append(".ok");
                File markerFile = new File(parentFile, stringBuilder.toString());
                if (distDir.isDirectory() && markerFile.isFile()) {
                    return Install.this.getDistributionRoot(distDir, distDir.getAbsolutePath());
                }
                File parentFile2;
                StringBuilder stringBuilder2;
                PrintStream printStream;
                if (localZipFile.isFile() ^ 1) {
                    parentFile2 = localZipFile.getParentFile();
                    stringBuilder2 = new StringBuilder();
                    stringBuilder2.append(localZipFile.getName());
                    stringBuilder2.append(".part");
                    File tmpZipFile = new File(parentFile2, stringBuilder2.toString());
                    tmpZipFile.delete();
                    printStream = System.out;
                    stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("Downloading ");
                    stringBuilder2.append(distributionUrl);
                    printStream.println(stringBuilder2.toString());
                    Install.this.download.download(distributionUrl, tmpZipFile);
                    tmpZipFile.renameTo(localZipFile);
                }
                for (File dir : Install.this.listDirs(distDir)) {
                    PrintStream printStream2 = System.out;
                    StringBuilder stringBuilder3 = new StringBuilder();
                    stringBuilder3.append("Deleting directory ");
                    stringBuilder3.append(dir.getAbsolutePath());
                    printStream2.println(stringBuilder3.toString());
                    Install.this.deleteDir(dir);
                }
                printStream = System.out;
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Unzipping ");
                stringBuilder2.append(localZipFile.getAbsolutePath());
                stringBuilder2.append(" to ");
                stringBuilder2.append(distDir.getAbsolutePath());
                printStream.println(stringBuilder2.toString());
                Install.this.unzip(localZipFile, distDir);
                parentFile2 = Install.this.getDistributionRoot(distDir, distributionUrl.toString());
                Install.this.setExecutablePermissions(parentFile2);
                markerFile.createNewFile();
                return parentFile2;
            }
        });
    }

    private File getDistributionRoot(File distDir, String distributionDescription) {
        List<File> dirs = listDirs(distDir);
        if (dirs.isEmpty()) {
            throw new RuntimeException(String.format("Gradle distribution '%s' does not contain any directories. Expected to find exactly 1 directory.", new Object[]{distributionDescription}));
        } else if (dirs.size() == 1) {
            return (File) dirs.get(0);
        } else {
            throw new RuntimeException(String.format("Gradle distribution '%s' contains too many directories. Expected to find exactly 1 directory.", new Object[]{distributionDescription}));
        }
    }

    private List<File> listDirs(File distDir) {
        List<File> dirs = new ArrayList();
        if (distDir.exists()) {
            for (File file : distDir.listFiles()) {
                if (file.isDirectory()) {
                    dirs.add(file);
                }
            }
        }
        return dirs;
    }

    private void setExecutablePermissions(File gradleHome) {
        if (!isWindows()) {
            File gradleCommand = new File(gradleHome, "bin/gradle");
            String errorMessage = null;
            try {
                Process p = new ProcessBuilder(new String[]{"chmod", "755", gradleCommand.getCanonicalPath()}).start();
                if (p.waitFor() == 0) {
                    PrintStream printStream = System.out;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Set executable permissions for: ");
                    stringBuilder.append(gradleCommand.getAbsolutePath());
                    printStream.println(stringBuilder.toString());
                } else {
                    BufferedReader is = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    Formatter stdout = new Formatter();
                    while (true) {
                        String readLine = is.readLine();
                        String line = readLine;
                        if (readLine == null) {
                            break;
                        }
                        stdout.format("%s%n", new Object[]{line});
                    }
                    errorMessage = stdout.toString();
                }
            } catch (IOException e) {
                errorMessage = e.getMessage();
            } catch (InterruptedException e2) {
                errorMessage = e2.getMessage();
            }
            if (errorMessage != null) {
                PrintStream printStream2 = System.out;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Could not set executable permissions for: ");
                stringBuilder2.append(gradleCommand.getAbsolutePath());
                printStream2.println(stringBuilder2.toString());
                System.out.println("Please do this manually if you want to use the Gradle UI.");
            }
        }
    }

    private boolean isWindows() {
        if (System.getProperty("os.name").toLowerCase(Locale.US).indexOf("windows") > -1) {
            return true;
        }
        return false;
    }

    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String file : children) {
                if (!deleteDir(new File(dir, file))) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    private void unzip(File zip, File dest) throws IOException {
        Throwable th;
        ZipFile zipFile = new ZipFile(zip);
        try {
            Enumeration entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                if (entry.isDirectory()) {
                    new File(dest, entry.getName()).mkdirs();
                } else {
                    OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(new File(dest, entry.getName())));
                    try {
                        copyInputStream(zipFile.getInputStream(entry), outputStream);
                        outputStream.close();
                    } catch (Throwable th2) {
                        th = th2;
                    }
                }
            }
            zipFile.close();
        } catch (Throwable th3) {
            th = th3;
            zipFile.close();
            throw th;
        }
    }

    private void copyInputStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        while (true) {
            int read = in.read(buffer);
            int len = read;
            if (read >= 0) {
                out.write(buffer, 0, len);
            } else {
                in.close();
                out.close();
                return;
            }
        }
    }
}
