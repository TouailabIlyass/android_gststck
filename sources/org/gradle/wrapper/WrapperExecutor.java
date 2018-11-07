package org.gradle.wrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Formatter;
import java.util.Properties;

public class WrapperExecutor {
    public static final String DISTRIBUTION_BASE_PROPERTY = "distributionBase";
    public static final String DISTRIBUTION_PATH_PROPERTY = "distributionPath";
    public static final String DISTRIBUTION_URL_PROPERTY = "distributionUrl";
    public static final String ZIP_STORE_BASE_PROPERTY = "zipStoreBase";
    public static final String ZIP_STORE_PATH_PROPERTY = "zipStorePath";
    private final WrapperConfiguration config = new WrapperConfiguration();
    private final Properties properties;
    private final File propertiesFile;
    private final Appendable warningOutput;

    public static WrapperExecutor forProjectDirectory(File projectDir, Appendable warningOutput) {
        return new WrapperExecutor(new File(projectDir, "gradle/wrapper/gradle-wrapper.properties"), new Properties(), warningOutput);
    }

    public static WrapperExecutor forWrapperPropertiesFile(File propertiesFile, Appendable warningOutput) {
        if (propertiesFile.exists()) {
            return new WrapperExecutor(propertiesFile, new Properties(), warningOutput);
        }
        throw new RuntimeException(String.format("Wrapper properties file '%s' does not exist.", new Object[]{propertiesFile}));
    }

    WrapperExecutor(File propertiesFile, Properties properties, Appendable warningOutput) {
        this.properties = properties;
        this.propertiesFile = propertiesFile;
        this.warningOutput = warningOutput;
        if (propertiesFile.exists()) {
            try {
                loadProperties(propertiesFile, properties);
                this.config.setDistribution(prepareDistributionUri());
                this.config.setDistributionBase(getProperty(DISTRIBUTION_BASE_PROPERTY, this.config.getDistributionBase()));
                this.config.setDistributionPath(getProperty(DISTRIBUTION_PATH_PROPERTY, this.config.getDistributionPath()));
                this.config.setZipBase(getProperty(ZIP_STORE_BASE_PROPERTY, this.config.getZipBase()));
                this.config.setZipPath(getProperty(ZIP_STORE_PATH_PROPERTY, this.config.getZipPath()));
            } catch (Exception e) {
                throw new RuntimeException(String.format("Could not load wrapper properties from '%s'.", new Object[]{propertiesFile}), e);
            }
        }
    }

    private URI prepareDistributionUri() throws URISyntaxException {
        URI source = readDistroUrl();
        if (source.getScheme() == null) {
            return new File(this.propertiesFile.getParentFile(), source.getSchemeSpecificPart()).toURI();
        }
        return source;
    }

    private URI readDistroUrl() throws URISyntaxException {
        if (this.properties.getProperty(DISTRIBUTION_URL_PROPERTY) != null) {
            return new URI(getProperty(DISTRIBUTION_URL_PROPERTY));
        }
        return readDistroUrlDeprecatedWay();
    }

    private URI readDistroUrlDeprecatedWay() throws URISyntaxException {
        String distroUrl = null;
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(getProperty("urlRoot"));
            stringBuilder.append("/");
            stringBuilder.append(getProperty("distributionName"));
            stringBuilder.append("-");
            stringBuilder.append(getProperty("distributionVersion"));
            stringBuilder.append("-");
            stringBuilder.append(getProperty("distributionClassifier"));
            stringBuilder.append(".zip");
            distroUrl = stringBuilder.toString();
            Formatter formatter = new Formatter();
            formatter.format("Wrapper properties file '%s' contains deprecated entries 'urlRoot', 'distributionName', 'distributionVersion' and 'distributionClassifier'. These will be removed soon. Please use '%s' instead.%n", new Object[]{this.propertiesFile, DISTRIBUTION_URL_PROPERTY});
            this.warningOutput.append(formatter.toString());
        } catch (Exception e) {
            reportMissingProperty(DISTRIBUTION_URL_PROPERTY);
        }
        return new URI(distroUrl);
    }

    private static void loadProperties(File propertiesFile, Properties properties) throws IOException {
        InputStream inStream = new FileInputStream(propertiesFile);
        try {
            properties.load(inStream);
        } finally {
            inStream.close();
        }
    }

    public URI getDistribution() {
        return this.config.getDistribution();
    }

    public WrapperConfiguration getConfiguration() {
        return this.config;
    }

    public void execute(String[] args, Install install, BootstrapMainStarter bootstrapMainStarter) throws Exception {
        bootstrapMainStarter.start(args, install.createDist(this.config));
    }

    private String getProperty(String propertyName) {
        return getProperty(propertyName, null);
    }

    private String getProperty(String propertyName, String defaultValue) {
        String value = this.properties.getProperty(propertyName);
        if (value != null) {
            return value;
        }
        if (defaultValue != null) {
            return defaultValue;
        }
        return reportMissingProperty(propertyName);
    }

    private String reportMissingProperty(String propertyName) {
        throw new RuntimeException(String.format("No value with key '%s' specified in wrapper properties file '%s'.", new Object[]{propertyName, this.propertiesFile}));
    }
}
