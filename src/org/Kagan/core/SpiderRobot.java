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
    private final Thread[] dbWriterThreads;
    private final Indexer[] indexers;
    private final DbWriter[] dbWriters;
    private static boolean closed = true;
    private final BlockingQueue<PageInfo> queue;
    
    public SpiderRobot(Configure conf) {
        this.conf = conf;
        this.queue = new LinkedBlockingQueue<PageInfo>(Configure.queueSize);
        this.indexers = new Indexer[conf.getWebsites().size()];
        this.dbWriters = new DbWriter[Configure.writeDbThreads];
        this.indexerThreads = new Thread[conf.getWebsites().size()];
        this.dbWriterThreads = new Thread[Configure.writeDbThreads];
    }
    
    @Override
    public String toString() {
        final String SPLIT = "--------------------------------------------------------------------------------";
        StringBuilder sb = new StringBuilder();
        sb.append("Queue Size : ").append(queue.size()).append("\n").append(SPLIT).append("\n");
        for (Indexer indexer : indexers) {
            sb.append(indexer).append("\n");
        }
        sb.append(SPLIT).append("\n");
        for (DbWriter writer : dbWriters) {
            sb.append(writer).append("\n");
        }
        return sb.toString();
    }
    
    public final void start() {
        if (!closed) { return; }
        
        int i = 0;
        for (Map.Entry<String, WebsiteConfigure> entry : conf.getWebsites().entrySet()) {
            indexers[i] = new Indexer(entry.getValue(),
                new LinkedBlockingDeque<String>(Configure.dequeSize),
                queue,
                new PageInfoHandler()
            );
            indexerThreads[i] = new Thread(indexers[i]);
            indexerThreads[i++].start();
        }
        
        for (i = 0; i < Configure.writeDbThreads; i++) {
            dbWriters[i] = new DbWriter(queue);
            dbWriterThreads[i] = new Thread(dbWriters[i]);
            dbWriterThreads[i].start();
        }
        
        closed = false;
    }
    
    public final void shutdown() {
        if (closed) { return; }
        closed = true;
        for (Indexer idxer : indexers) {
            idxer.shutdown();
        }
        
        for (DbWriter writer : dbWriters) {
            writer.shutdown();
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
