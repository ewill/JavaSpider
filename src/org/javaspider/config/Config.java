package org.javaspider.config;

import java.nio.charset.Charset;

public final class Config {
    
    private final Configure c;
    private final HttpClientConfig hcConfig;
    public static final String DEFAULT_CHARSET = Charset.forName("UTF-8").toString();
    
    public Config() {
        this.c = new Configure();
        this.hcConfig = new HttpClientConfig();
    }
    
    public Configure getConfigure() {
        return c;
    }
    
    public HttpClientConfig getHttpClientConfig() {
        return hcConfig;
    }
}
