package org.mhb.parser;

public enum PropertyName {
    SEARCH_DIRECTORY("searching-directory"),
    INCLUDE_PATTERN("include-file-pattern"),
    EXCLUDE_PATTERN("exclude-file-pattern"),
    PROPERTY_FILENAME("application.properties"),
    LOGGING_PROPERTY_FILENAME("logging.properties"),
    DB_PASSWORD("database-password"),
    DB_USERNAME("database-username"),
    DB_SERVERNAME("database-servername"),
    DB_PORTNUMBER("database-portnumber"),
    TEST_FILENAME("test.properties"),
    DB_NAME("database-name");

    private final String propertyName;

    PropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getName() {
        return propertyName;
    }

}
