package org.Kagan.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import org.Kagan.config.Configure;
import org.Kagan.config.WebsiteConfigure;
import org.Kagan.util.Db;
import org.Kagan.util.StringKit;

public class SpiderRobot {
    
    private final Configure conf;
    private Thread[] indexerThreads;
    private final Indexer[] indexer;
    private static boolean closed = true;
    private final BlockingQueue<PageInfo> queue;
    
    public SpiderRobot(Configure conf) {
        this.conf = conf;
        this.indexer = new Indexer[conf.getWebsites().size()];
        this.queue = new LinkedBlockingQueue<PageInfo>(Configure.queueSize);
        
    }
    
    public final void start() {
        if (!closed) { return; }
        
        int i = 0;
        indexerThreads = new Thread[conf.getWebsites().size()];
        for (Map.Entry<String, WebsiteConfigure> entry : conf.getWebsites().entrySet()) {
            indexer[i] = new Indexer(entry.getValue(),
                new LinkedBlockingDeque<String>(Configure.dequeSize),
                queue,
                new PageInfoHandler()
            );
            indexerThreads[i] = new Thread(indexer[i]);
            indexerThreads[i++].start();
        }
        
        closed = false;
    }
    
    public final void shutdown() {
        if (closed) { return; }
        closed = true;
        for (Indexer idxer : indexer) {
            idxer.shutdown();
        }
    }
    
    public final int countRecords() {
        return Db.count(Configure.dataTable);
    }
    
    public final List<Map<String, Object>> checkRepeatData() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT hashKey ")
           .append("FROM ").append(Configure.indexTable).append(" ")
           .append("GROUP BY hashKey ")
           .append("HAVING COUNT(hashKey) > 1");
        return Db.query(sql.toString());
    }
    
    public final String formatRepeatData(List<Map<String, Object>> data) {
        List<String> result = new ArrayList<String>();
        for (Map<String, Object> map : data) {
            result.add(String.valueOf(map.get("hashKey")));
        }
        if (result.size() > 0) {
            return String.format("[\n    %s\n]", StringKit.join(result, ",\n    "));
        } else {
            return null;
        }
    }
    
}
