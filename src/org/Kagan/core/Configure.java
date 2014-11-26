package org.Kagan.core;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Configure {
    private String keyTable;
    private String dataTable;
    private int queueSize;
    private int writeDbThreads;
    private Map<String, WebsiteConfigure> websiteList = new LinkedHashMap<String, WebsiteConfigure>();
    
    public int GetQueueSize() {
        return queueSize;
    }

    public void SetQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public int GetWriteDbThreads() {
        return writeDbThreads;
    }

    public void SetWriteDbThreads(int writeDbThreads) {
        this.writeDbThreads = writeDbThreads;
    }

    public String GetKeyTable() {
        return keyTable;
    }
    
    public void SetKeyTable(String keyTable) {
        this.keyTable = keyTable;
    }
    
    public String GetDataTable() {
        return dataTable;
    }
    
    public void SetDataTable(String dataTable) {
        this.dataTable = dataTable;
    }
    
    public void SetWebsite(String name, WebsiteConfigure wc) {
        websiteList.put(name, wc);
    }
    
    public WebsiteConfigure GetWebsite(String name) {
        return websiteList.get(name);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, WebsiteConfigure> wc : websiteList.entrySet()) {
            sb.append(wc.getValue().toString());
        }
        return String.format("Kagan System Configure:\n\n KeyTable : %s\nDataTable : %s\nQueueSize : %d\nW-Threads : %d\n\nWebsite List Below\n%s",
            keyTable,
            dataTable,
            queueSize,
            writeDbThreads,
            sb.toString()
        );
    }
}
