package hu.organum.lilypondwave.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class Settings {

    private static final Logger LOG = Logger.getLogger(Settings.class.getName());

    private Properties defaultSettings = new Properties();

    private Properties customSettings = new Properties();

    public Settings() {
        try {
            defaultSettings.load(getClass().getResourceAsStream("/default_settings.properties"));
        } catch (IOException e) {
            LOG.severe("Couldn't load default settings");
        }
        InputStream customProperties = getClass().getResourceAsStream("/settings.properties");
        if (customProperties != null) {
            try {
                customSettings.load(customProperties);
            } catch (IOException e) {
                LOG.severe("Couldn't load custom settings");
            }
        }
    }

    public String get(String key) {
        String value;
        if (customSettings.containsKey(key)) {
            value = customSettings.getProperty(key);
        } else if (defaultSettings.containsKey(key)) {
            value = defaultSettings.getProperty(key);
        } else {
            LOG.warning(key + " not found in any configuration");
            value = null;
        }
        return value;
    }

    public Integer getInteger(String key) {
        return Integer.valueOf(get(key));
    }

}
