package org.gradle.wrapper;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class BootstrapMainStarter {
    public void start(String[] args, File gradleHome) throws Exception {
        URLClassLoader contextClassLoader = new URLClassLoader(new URL[]{findLauncherJar(gradleHome).toURI().toURL()}, ClassLoader.getSystemClassLoader().getParent());
        Thread.currentThread().setContextClassLoader(contextClassLoader);
        contextClassLoader.loadClass("org.gradle.launcher.GradleMain").getMethod("main", new Class[]{String[].class}).invoke(null, new Object[]{args});
    }

    private File findLauncherJar(File gradleHome) {
        for (File file : new File(gradleHome, "lib").listFiles()) {
            if (file.getName().matches("gradle-launcher-.*\\.jar")) {
                return file;
            }
        }
        throw new RuntimeException(String.format("Could not locate the Gradle launcher JAR in Gradle distribution '%s'.", new Object[]{gradleHome}));
    }
}
