package org.gradle.wrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SystemPropertiesHandler {
    public static Map<String, String> getSystemProperties(File propertiesFile) {
        Map<String, String> propertyMap = new HashMap();
        if (!propertiesFile.isFile()) {
            return propertyMap;
        }
        Properties properties = new Properties();
        FileInputStream inStream;
        try {
            inStream = new FileInputStream(propertiesFile);
            properties.load(inStream);
            inStream.close();
            inStream = Pattern.compile("systemProp\\.(.*)");
            for (Object argument : properties.keySet()) {
                Matcher matcher = inStream.matcher(argument.toString());
                if (matcher.find()) {
                    String key = matcher.group(1);
                    if (key.length() > 0) {
                        propertyMap.put(key, properties.get(argument).toString());
                    }
                }
            }
            return propertyMap;
        } catch (IOException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Error when loading properties file=");
            stringBuilder.append(propertiesFile);
            throw new RuntimeException(stringBuilder.toString(), e);
        } catch (Throwable th) {
            inStream.close();
        }
    }
}
