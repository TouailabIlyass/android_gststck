package org.gradle.wrapper;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;
import org.gradle.cli.CommandLineParser;
import org.gradle.cli.ParsedCommandLine;
import org.gradle.cli.SystemPropertiesCommandLineConverter;

public class GradleWrapperMain {
    public static final String DEFAULT_GRADLE_USER_HOME;
    public static final String GRADLE_USER_HOME_DETAILED_OPTION = "gradle-user-home";
    public static final String GRADLE_USER_HOME_ENV_KEY = "GRADLE_USER_HOME";
    public static final String GRADLE_USER_HOME_OPTION = "g";
    public static final String GRADLE_USER_HOME_PROPERTY_KEY = "gradle.user.home";

    static {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(System.getProperty("user.home"));
        stringBuilder.append("/.gradle");
        DEFAULT_GRADLE_USER_HOME = stringBuilder.toString();
    }

    public static void main(String[] args) throws Exception {
        File wrapperJar = wrapperJar();
        File propertiesFile = wrapperProperties(wrapperJar);
        File rootDir = rootDir(wrapperJar);
        CommandLineParser parser = new CommandLineParser();
        parser.allowUnknownOptions();
        parser.option(GRADLE_USER_HOME_OPTION, GRADLE_USER_HOME_DETAILED_OPTION).hasArgument();
        SystemPropertiesCommandLineConverter converter = new SystemPropertiesCommandLineConverter();
        converter.configure(parser);
        ParsedCommandLine options = parser.parse(args);
        System.getProperties().putAll((Map) converter.convert(options));
        File gradleUserHome = gradleUserHome(options);
        addSystemProperties(gradleUserHome, rootDir);
        WrapperExecutor.forWrapperPropertiesFile(propertiesFile, System.out).execute(args, new Install(new Download("gradlew", wrapperVersion()), new PathAssembler(gradleUserHome)), new BootstrapMainStarter());
    }

    private static void addSystemProperties(File gradleHome, File rootDir) {
        System.getProperties().putAll(SystemPropertiesHandler.getSystemProperties(new File(gradleHome, "gradle.properties")));
        System.getProperties().putAll(SystemPropertiesHandler.getSystemProperties(new File(rootDir, "gradle.properties")));
    }

    private static File rootDir(File wrapperJar) {
        return wrapperJar.getParentFile().getParentFile().getParentFile();
    }

    private static File wrapperProperties(File wrapperJar) {
        return new File(wrapperJar.getParent(), wrapperJar.getName().replaceFirst("\\.jar$", ".properties"));
    }

    private static File wrapperJar() {
        try {
            URI location = GradleWrapperMain.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            if (location.getScheme().equals("file")) {
                return new File(location.getPath());
            }
            throw new RuntimeException(String.format("Cannot determine classpath for wrapper Jar from codebase '%s'.", new Object[]{location}));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    static String wrapperVersion() {
        InputStream resourceAsStream;
        try {
            resourceAsStream = GradleWrapperMain.class.getResourceAsStream("/build-receipt.properties");
            if (resourceAsStream == null) {
                throw new RuntimeException("No build receipt resource found.");
            }
            Properties buildReceipt = new Properties();
            buildReceipt.load(resourceAsStream);
            String versionNumber = buildReceipt.getProperty("versionNumber");
            if (versionNumber == null) {
                throw new RuntimeException("No version number specified in build receipt resource.");
            }
            resourceAsStream.close();
            return versionNumber;
        } catch (Exception e) {
            throw new RuntimeException("Could not determine wrapper version.", e);
        } catch (Throwable th) {
            resourceAsStream.close();
        }
    }

    private static File gradleUserHome(ParsedCommandLine options) {
        if (options.hasOption(GRADLE_USER_HOME_OPTION)) {
            return new File(options.option(GRADLE_USER_HOME_OPTION).getValue());
        }
        String property = System.getProperty(GRADLE_USER_HOME_PROPERTY_KEY);
        String gradleUserHome = property;
        if (property != null) {
            return new File(gradleUserHome);
        }
        property = System.getenv("GRADLE_USER_HOME");
        gradleUserHome = property;
        if (property != null) {
            return new File(gradleUserHome);
        }
        return new File(DEFAULT_GRADLE_USER_HOME);
    }
}
