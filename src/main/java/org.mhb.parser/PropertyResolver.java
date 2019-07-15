package org.mhb.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public enum PropertyResolver {

    INSTANCE;

    private final Logger logger = Logger.getLogger(PropertyResolver.class.getSimpleName());

    private final Properties properties = new Properties();

    private final Properties testProps = new Properties();

    PropertyResolver() {

        readConfiguration(PropertyName.PROPERTY_FILENAME.getName(), properties);

        readConfiguration(PropertyName.TEST_FILENAME.getName(), testProps);

        properties.stringPropertyNames().stream().map(key -> String.format("%s:     '%s'", key, properties.getProperty(key))).forEach(logger::info);

        setLogLevel();
    }

    private void setLogLevel() {
        try {
            LogManager.getLogManager().readConfiguration(getResourceAsStream(PropertyName.LOGGING_PROPERTY_FILENAME.getName()));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while reading logging property from {0}", PropertyName.LOGGING_PROPERTY_FILENAME.getName());
            throw new RuntimeException(e);
        }
    }


    private void readConfiguration(String propertyFileName, Properties properties) {
        InputStream is = getResourceAsStream(propertyFileName);
        try {
            properties.load(is);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while reading property from {0}", propertyFileName);
            throw new RuntimeException(e);
        }
    }

    private InputStream getResourceAsStream(String propertyFileName) {
        InputStream is = PropertyResolver.class.getClassLoader().getResourceAsStream(propertyFileName);
        Objects.requireNonNull(is, () -> String.format("The property file %s expected in the %s directory, but is missing", propertyFileName, "src/main/resources"));
        return is;
    }

    public String get(String propName) {
        return properties.getProperty(propName);
    }

    public String get(String propName, String defaultValue) {
        return properties.getProperty(propName, defaultValue);
    }

    public Properties getTestProps() {
        Properties prop = new Properties();
        prop.putAll(testProps);
        return prop;
    }
}
