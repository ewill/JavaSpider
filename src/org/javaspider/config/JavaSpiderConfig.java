package org.javaspider.config;

import java.util.Properties;

import org.javaspider.kit.ConfigKit;

public abstract class JavaSpiderConfig {
    
    public final Properties loadProperty(String fullPath) {
        return ConfigKit.loadProperty(fullPath);
    }
    
    public abstract void beforeStop();
    public abstract void beforeStart();
    public abstract void config(Configure me);
    public abstract void configHttpClient(HttpClientConfig me);
}
