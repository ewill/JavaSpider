package org.kagan.config;

import java.util.regex.Pattern;

import org.kagan.interfaces.IPageInfo;

public final class WebsiteConfigure {
    private String url;
    private Pattern regex;
    private String charset;
    private String websiteName;
    private IPageInfo handler;
    
    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public IPageInfo getHandler() {
        return handler;
    }

    public void setHandler(IPageInfo handler) {
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

    @Override
    public String toString() {
        return String.format("%s\nWebsite Name : %s\n         Url : %s\n       Regex : %s\n     Charset : %s\n     Handler : %s\n",
            "--------------------------------------------------------------------------------",
            websiteName,
            url,
            regex.toString(),
            charset,
            handler.getClass().getName()
        );
    }
    
}
