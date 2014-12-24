package org.javaspider.config;

import java.util.regex.Pattern;

import org.javaspider.core.AbstractIndexerThread;
import org.javaspider.core.AbstractWriterThread;
import org.javaspider.interfaces.IPageHandler;

public final class WebsiteConfigure {
    
    private String url;
    private Pattern regex;
    private String websiteName;
    private int catchPageTimeout = 3000;
    private String charset = Config.DEFAULT_CHARSET;
    private Class<? extends IPageHandler> handlerClass;
    private Class<? extends AbstractWriterThread> writerClass;
    private Class<? extends AbstractIndexerThread> indexerClass;
    
    public int getCatchPageTimeout() {
        return catchPageTimeout;
    }

    public void setCatchPageTimeout(int catchPageTimeout) {
        this.catchPageTimeout = catchPageTimeout;
    }

    public Class<? extends AbstractIndexerThread> getIndexerClass() {
        return indexerClass;
    }

    public void setIndexerClass(Class<? extends AbstractIndexerThread> indexerClass) {
        this.indexerClass = indexerClass;
    }

    public Class<? extends AbstractWriterThread> getWriterClass() {
        return writerClass;
    }

    public void setWriterClass(Class<? extends AbstractWriterThread> writerClass) {
        this.writerClass = writerClass;
    }

    public Class<? extends IPageHandler> getHandlerClass() {
        return handlerClass;
    }

    public void setHandlerClass(Class<? extends IPageHandler> handlerClass) {
        this.handlerClass = handlerClass;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
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
    
    public void setRegex(String regex) {
        this.regex = Pattern.compile(String.format("(%s)", regex));
    }
    
}
