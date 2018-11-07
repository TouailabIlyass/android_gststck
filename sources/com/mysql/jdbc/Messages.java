package com.mysql.jdbc;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
    private static final String BUNDLE_NAME = "com.mysql.jdbc.LocalizedErrorMessages";
    private static final ResourceBundle RESOURCE_BUNDLE;

    static {
        ResourceBundle temp;
        try {
            temp = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault(), Messages.class.getClassLoader());
        } catch (Throwable t2) {
            try {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Can't load resource bundle due to underlying exception ");
                stringBuilder.append(t.toString());
                new RuntimeException(stringBuilder.toString()).initCause(t2);
            } catch (Throwable th) {
                RESOURCE_BUNDLE = null;
            }
        }
        RESOURCE_BUNDLE = temp;
    }

    public static String getString(String key) {
        if (RESOURCE_BUNDLE == null) {
            throw new RuntimeException("Localized messages from resource bundle 'com.mysql.jdbc.LocalizedErrorMessages' not loaded during initialization of driver.");
        } else if (key == null) {
            try {
                throw new IllegalArgumentException("Message key can not be null");
            } catch (MissingResourceException e) {
                r1 = new StringBuilder();
                r1.append('!');
                r1.append(key);
                r1.append('!');
                return r1.toString();
            }
        } else {
            MissingResourceException e2 = RESOURCE_BUNDLE.getString(key);
            if (e2 == null) {
                r1 = new StringBuilder();
                r1.append("Missing error message for key '");
                r1.append(key);
                r1.append("'");
                e2 = r1.toString();
            }
            return e2;
        }
    }

    public static String getString(String key, Object[] args) {
        return MessageFormat.format(getString(key), args);
    }

    private Messages() {
    }
}
