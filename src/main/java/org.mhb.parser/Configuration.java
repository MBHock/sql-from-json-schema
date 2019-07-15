package org.mhb.parser;

import java.util.Objects;

public class Configuration {

    public String getSearchDirectory() {
        return PropertyResolver.INSTANCE.get(PropertyName.SEARCH_DIRECTORY.getName(), System.getProperty("user.dir"));
    }

    public String[] getIncludePattern() {
        String fileNamePattern = PropertyResolver.INSTANCE.get(PropertyName.INCLUDE_PATTERN.getName());
        if (Objects.isNull(fileNamePattern) || fileNamePattern.isEmpty()) {
            return new String[]{".*\\.json"};
        }

        return fileNamePattern.split(",");
    }

    public String getExcludePattern() {
        return PropertyResolver.INSTANCE.get(PropertyName.EXCLUDE_PATTERN.getName());
    }

    public String getDatabasePassword() {
        return PropertyResolver.INSTANCE.get(PropertyName.DB_PASSWORD.getName());
    }

    public String getDatabaseUsername() {
        return PropertyResolver.INSTANCE.get(PropertyName.DB_USERNAME.getName());
    }

    public String getDatabaseServername() {
        return PropertyResolver.INSTANCE.get(PropertyName.DB_SERVERNAME.getName());
    }

    public Integer getDatabasePortnumber() {
        return Integer.parseInt(PropertyResolver.INSTANCE.get(PropertyName.DB_PORTNUMBER.getName()));
    }

    public String getDatabaseName() {
        return PropertyResolver.INSTANCE.get(PropertyName.DB_NAME.getName());
    }
}
