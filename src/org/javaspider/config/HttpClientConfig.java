package org.javaspider.config;

public final class HttpClientConfig {
    private int retryTimes;
    private int maxPerRoute;
    private int socketTimeout;
    private int requestTimeout;
    private int connectionTimeout;
    private boolean redirectEnabled;
    private String userAgent = "JavaSpider/HttpClient";
    
    public int getMaxPerRoute() {
        return maxPerRoute;
    }

    public void setMaxPerRoute(int maxPerRoute) {
        this.maxPerRoute = maxPerRoute;
    }

    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public int getRetryTimes() {
        return retryTimes;
    }
    
    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }
    
    public int getSocketTimeout() {
        return socketTimeout;
    }
    
    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }
    
    public int getConnectionTimeout() {
        return connectionTimeout;
    }
    
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
    
    public int getRequestTimeout() {
        return requestTimeout;
    }
    
    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }
    
    public boolean isRedirectEnabled() {
        return redirectEnabled;
    }
    
    public void setRedirectEnabled(boolean redirectEnabled) {
        this.redirectEnabled = redirectEnabled;
    }
    
}
