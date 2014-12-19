package org.kagan.config;

import java.util.LinkedHashMap;
import java.util.Map;


public final class Configure {
    public static String indexTable;
    public static String dataTable;
    public static int queueSize;
    public static int dequeSize;
    public static int readThreads;
    public static int writeDbThreads;
    public static int websiteNum;
    public static int retryTimes;
    private Map<String, WebsiteConfigure> websites = new LinkedHashMap<String, WebsiteConfigure>();
    
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
        return String.format("\nKagan System Configure:\nIndexTable : %s\nDataTable  : %s\nDequeSize  : %d\nQueueSize  : %d\nR-Threads  : %d\nW-Threads  : %d\nRetryTimes : %d\n\nWebsite List Below\n%s",
            indexTable,
            dataTable,
            dequeSize,
            queueSize,
            readThreads,
            writeDbThreads,
            retryTimes,
            sb.toString()
        );
    }
}
