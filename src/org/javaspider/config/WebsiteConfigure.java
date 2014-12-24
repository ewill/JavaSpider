package org.javaspider.config;

import java.util.regex.Pattern;

import org.javaspider.interfaces.IPageHandler;

public final class WebsiteConfigure {
    private String url;
    private Pattern regex;
    private String charset;
    private String websiteName;
    private String indexerClass;
    private String writerClass;
    private IPageHandler handler;
    
    public String getIndexerClass() {
        return indexerClass;
    }

    public void setIndexerClass(String indexerClass) {
        this.indexerClass = indexerClass;
    }

    public String getWriterClass() {
        return writerClass;
    }

    public void setWriterClass(String writerClass) {
        this.writerClass = writerClass;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public IPageHandler getHandler() {
        return handler;
    }

    public void setHandler(IPageHandler handler) {
        this.handler = handler;
    }

    public String getWebsiteName() {
        return websiteName;
    }
    
    public void setWebsiteName(String websiteName) {
        this.websiteName = websiteName;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public Pattern getRegex() {
        return regex;
    }
    
    public void setRegex(Pattern regex) {
        this.regex = regex;
    }
    
}
