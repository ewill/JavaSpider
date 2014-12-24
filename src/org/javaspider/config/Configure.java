package org.javaspider.config;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Configure {
    
    private String indexTable;
    private String dataTable;
    private int queueSize = 50;
    private int dequeSize = 100;
    private int readThreads = 3;
    private int writeDbThreads = 3;
    private Map<String, WebsiteConfigure> websites = new LinkedHashMap<String, WebsiteConfigure>();
    
    public String getIndexTable() {
        return indexTable;
    }

    public void setIndexTable(String indexTable) {
        this.indexTable = indexTable;
    }

    public String getDataTable() {
        return dataTable;
    }

    public void setDataTable(String dataTable) {
        this.dataTable = dataTable;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public int getDequeSize() {
        return dequeSize;
    }

    public void setDequeSize(int dequeSize) {
        this.dequeSize = dequeSize;
    }

    public int getReadThreads() {
        return readThreads;
    }

    public void setReadThreads(int readThreads) {
        this.readThreads = readThreads;
    }

    public int getWriteDbThreads() {
        return writeDbThreads;
    }

    public void setWriteDbThreads(int writeDbThreads) {
        this.writeDbThreads = writeDbThreads;
    }

    public Map<String, WebsiteConfigure> getWebsites() {
        return websites;
    }

    public void setWebsites(String name, WebsiteConfigure wc) {
        this.websites.put(name, wc);
    }

}
