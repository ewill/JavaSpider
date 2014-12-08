package org.Kagan.config;

import java.util.LinkedHashMap;
import java.util.Map;


public final class Configure {
    private String indexTable;
    private String dataTable;
    private int queueSize;
    private int dequeSize;
    private int writeDbThreads;
    private Map<String, WebsiteConfigure> websites = new LinkedHashMap<String, WebsiteConfigure>();
    
    public String getIndexTable() {
        return indexTable;
    }

    public void setIndexTable(String keyTable) {
        this.indexTable = keyTable;
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

    public int getWriteDbThreads() {
        return writeDbThreads;
    }

    public void setWriteDbThreads(int writeDbThreads) {
        this.writeDbThreads = writeDbThreads;
    }

    public Map<String, WebsiteConfigure> getWebsites() {
        return websites;
    }

    public void setWebsites(String key, WebsiteConfigure wc) {
        websites.put(key, wc);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, WebsiteConfigure> entry : websites.entrySet()) {
            sb.append(entry.getValue().toString());
        }
        return String.format("Kagan System Configure:\n\nIndexTable : %s\nDataTable  : %s\nDequeSize  : %d\nQueueSize  : %d\nW-Threads  : %d\n\nWebsite List Below\n%s",
            indexTable,
            dataTable,
            dequeSize,
            queueSize,
            writeDbThreads,
            sb.toString()
        );
    }
}
