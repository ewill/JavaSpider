package org.kagan.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import org.kagan.config.Configure;
import org.kagan.config.WebsiteConfigure;
import org.kagan.util.ConfigKit;
import org.kagan.util.Db;
import org.kagan.util.StringKit;

public class SpiderRobot {
    
    private final Configure conf;
    private final Indexer[] indexers;
    private final DbWriter[] dbWriters;
    private final BlockingQueue<PageInfo> queue;
    private volatile static boolean closed = true;
    
    public SpiderRobot() throws Exception {
        this.conf = ConfigKit.loadKaganXml("KaganConfig.xml");
        this.queue = new LinkedBlockingQueue<PageInfo>(Configure.queueSize);
        this.indexers = new Indexer[conf.getWebsites().size()];
        this.dbWriters = new DbWriter[Configure.writeDbThreads];
        
        Db.Init(ConfigKit.loadProperties("druid.properties"));
    }
    
    public Configure getConfigure() {
        return conf;
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
                queue
            );
            indexers[i++].start();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        for (i = 0; i < dbWriters.length; i++) {
            dbWriters[i] = new DbWriter(queue);
            dbWriters[i].start();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        closed = false;
    }
    
    public final void shutdown() {
        try {
            for (Indexer idxer : indexers) {
                if (idxer != null) {
                    idxer.shutdown();
                    idxer.join();
                    System.out.println(idxer);
                }
            }
            for (DbWriter writer : dbWriters) {
                if (writer != null) {
                    writer.shutdown();
                    writer.join();
                    System.out.println(writer);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        closed = true;
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
