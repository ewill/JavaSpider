package org.Kagan.core;

public class PageInfo {
    
    private String id;
    private String title;
    private String link;
    private String hashKey;
    private String pageContent;
    private String comeFrom;
    
    public String getHashKey() {
        return hashKey;
    }

    public void setHashKey(String hashKey) {
        this.hashKey = hashKey;
    }

    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getLink() {
        return link;
    }
    
    public void setLink(String link) {
        this.link = link;
    }
    
    public String getPageContent() {
        return pageContent;
    }
    
    public void setPageContent(String pageContent) {
        this.pageContent = pageContent;
    }
    
    public String getComeFrom() {
        return comeFrom;
    }
    
    public void setComeFrom(String comeFrom) {
        this.comeFrom = comeFrom;
    }
    
}
