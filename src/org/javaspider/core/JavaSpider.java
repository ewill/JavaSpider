package org.javaspider.core;

import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.javaspider.config.Config;
import org.javaspider.config.WebsiteConfigure;
import org.javaspider.kit.ConfigKit;

public final class JavaSpider {
    
    private final Config conf;
    private final HttpClient httpClient;
    private volatile boolean closed = true;
    private final AbstractWriterThread[] dbWriters;
    private final AbstractIndexerThread[] indexers;
    private static final Log log = LogFactory.getLog(JavaSpider.class);
    
    public JavaSpider(Config conf) {
        this.conf = conf;
        this.httpClient = new HttpClient(conf);
        this.indexers = new AbstractIndexerThread[conf.getConfigure().getWebsites().size()];
        this.dbWriters = new AbstractWriterThread[conf.getConfigure().getWriteDbThreads() * conf.getConfigure().getWebsites().size()];
    }
    
    public final void start() {
        if (!closed) { return; }
        
        int i = 0, k = 0;
        for (Map.Entry<String, WebsiteConfigure> entry : conf.getConfigure().getWebsites().entrySet()) {
            try {
                BlockingQueue<PageInfo> queue = new LinkedBlockingQueue<PageInfo>(conf.getConfigure().getQueueSize());
                Class<? extends AbstractIndexerThread> indexerClass = entry.getValue().getIndexerClass();
                if (indexerClass == null) {
                    indexers[i] = new DefaultIndexerThread(
                        conf,
                        httpClient,
                        entry.getValue(),
                        new LinkedBlockingDeque<String>(conf.getConfigure().getDequeSize()),
                        queue
                    );
                } else {
                    indexers[i] = ConfigKit.newConstructorInstance(
                        ConfigKit.newConstructor(
                            indexerClass,
                            Config.class,
                            HttpClient.class,
                            WebsiteConfigure.class,
                            BlockingDeque.class,
                            BlockingQueue.class
                        ),
                        conf,
                        httpClient,
                        entry.getValue(),
                        new LinkedBlockingDeque<String>(conf.getConfigure().getDequeSize()),
                        queue
                    );
                }
                indexers[i++].start();
                
                Class<? extends AbstractWriterThread> writer = entry.getValue().getWriterClass();
<<<<<<< HEAD
                for (int j = 0; j < conf.getConfigure().getWriteDbThreads(); j++) {
                    if (writer == null) {
                        dbWriters[j + k] = new DefaultDbWriterThread(conf, queue);
                    } else {
                        dbWriters[j + k] = ConfigKit.newConstructorInstance(
                            ConfigKit.newConstructor(writer, Config.class, BlockingQueue.class),
                            conf,
                            queue
                        );
                    }
                    dbWriters[j + k].start();
=======
                for (i = 0; i < conf.getConfigure().getWriteDbThreads(); i++) {
                    if (writer == null) {
                        dbWriters[i] = new DefaultDbWriterThread(conf, queue);
                    } else {
                        dbWriters[i] = ConfigKit.newConstructorInstance(
                            ConfigKit.newConstructor(writer, Config.class, BlockingQueue.class),
                            conf,
                            queue
                        );
                    }
                    dbWriters[i].start();
>>>>>>> branch 'master' of https://github.com/ewill/KaganSpider.git
                }
                
                k += conf.getConfigure().getWriteDbThreads();
                
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                shutdown();
            }
        }
        
        closed = false;
    }
    
    public final void shutdown() {
        try {
            for (AbstractIndexerThread idxer : indexers) {
                if (idxer != null) {
                    idxer.close();
                    idxer.join();
                }
            }
            for (AbstractWriterThread writer : dbWriters) {
                if (writer != null) {
                    writer.close();
                    writer.join();
                }
            }
        } catch (InterruptedException e) {
            log.error("JavaSpider shutdown fail!", e);
        }
        closed = true;
        
        httpClient.shutdown();
    }
    
}
