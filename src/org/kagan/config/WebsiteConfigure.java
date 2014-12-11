package org.kagan.config;

import java.util.regex.Pattern;

public final class WebsiteConfigure {
    private int dailySize;
    private String websiteName;
    private String url;
    private Pattern regex;
    
    public int getDailySize() {
        return dailySize;
    }
    
    public void setDailySize(int dailySize) {
        this.dailySize = dailySize;
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
        return String.format("%s\nWebsite Name : %s\n   DailySize : %d\n         Url : %s\n       Regex : %s\n",
            "--------------------------------------------------------------------------------",
            websiteName,
            dailySize,
            url,
            regex.toString()
        );
    }
    
}
