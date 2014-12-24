package org.javaspider.core;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.javaspider.config.Config;
import org.javaspider.config.WebsiteConfigure;
import org.javaspider.kit.ConfigKit;

public class JavaSpider {
    
    private final Config conf;
    private final HttpClient httpClient;
    private volatile boolean closed = true;
    private final AbstractWriterThread[] dbWriters;
    private final AbstractIndexerThread[] indexers;
    private static final Logger log = Logger.getLogger(JavaSpider.class);
    
    public JavaSpider(Config conf) {
        this.conf = conf;
        this.httpClient = new HttpClient(conf);
        this.dbWriters = new AbstractWriterThread[conf.getConfigure().getWriteDbThreads()];
        this.indexers = new AbstractIndexerThread[conf.getConfigure().getWebsites().size()];
    }
    
    @SuppressWarnings("unchecked")
    public final void start() {
        if (!closed) { return; }
        
        int i = 0;
        for (Map.Entry<String, WebsiteConfigure> entry : conf.getConfigure().getWebsites().entrySet()) {
            try {
                BlockingQueue<PageInfo> queue = new LinkedBlockingQueue<PageInfo>(conf.getConfigure().getQueueSize());
                Class<? extends AbstractIndexerThread> indexerClazz = ConfigKit.loadClass(entry.getValue().getIndexerClass());
                indexers[i] = ConfigKit.newConstructorInstance(
                    ConfigKit.newConstructor(
                        indexerClazz,
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
                indexers[i++].start();
                
                Class<? extends AbstractWriterThread> writerClazz = ConfigKit.loadClass(entry.getValue().getWriterClass());
                Constructor<? extends AbstractWriterThread> writerConstructor = ConfigKit.newConstructor(writerClazz, Config.class, BlockingQueue.class);
                for (i = 0; i < conf.getConfigure().getWriteDbThreads(); i++) {
                    dbWriters[i] = ConfigKit.newConstructorInstance(writerConstructor, conf, queue);
                    dbWriters[i].start();
                }
                
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
