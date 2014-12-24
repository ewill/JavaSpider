package org.javaspider.core;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.javaspider.config.Config;
import org.javaspider.config.WebsiteConfigure;
import org.javaspider.kit.Db;
import org.javaspider.kit.StringKit;
import org.jsoup.nodes.Document;

import com.alibaba.druid.pool.DruidPooledConnection;

public final class DefaultIndexerThread extends AbstractIndexerThread {
    
    private volatile boolean closed = true;
    private final IndexHandler[] indexHandlers;
    private static final int THREAD_SLEEP_TIME  = 4000;
    private static final Logger log = Logger.getLogger(DefaultIndexerThread.class);
    
    public DefaultIndexerThread(Config conf, HttpClient httpClient, WebsiteConfigure wc, BlockingDeque<String> deque, BlockingQueue<PageInfo> queue) {
        super(conf, httpClient, wc, deque, queue);
        this.indexHandlers = new IndexHandler[this.conf.getConfigure().getReadThreads()];
        for (int i = 0; i < indexHandlers.length; i++) {
            indexHandlers[i] = new IndexHandler(this);
        }
    }
    
    @Override
    public void close() {
        if (!closed) {
            try {
                for (IndexHandler h : indexHandlers) {
                    h.close();
                    h.join();
                }
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
            closed = true;
        }
    }
    
    @Override
    public void run() {
        try {
            if (closed) {
                closed = false;
                indexPage();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void indexPage() throws InterruptedException {
        String[] hashKeys;
        Map<String, String> map;
        
        for (IndexHandler h : indexHandlers) {
            h.start();
            Thread.sleep(1000);
        }
        
        Document doc = httpClient.parse(wc.getUrl(), wc.getCharset());
        while (!closed) {
            if (doc == null) {
                doc = httpClient.parse(wc.getUrl(), wc.getCharset());
                Thread.sleep(THREAD_SLEEP_TIME);
            } else {
                map = splitUrl(doc.html());
                hashKeys = getValidHashKey(map.keySet());
                if (hashKeys != null) {
                    addIndex(hashKeys);
                    for (int i = 1; i < hashKeys.length; i++) {
                        deque.putFirst(map.get(hashKeys[i]));
                    }
                    
                    String url = map.get(hashKeys[0]);
                    if (url != null && (doc = httpClient.parse(url, wc.getCharset())) != null) {
                        // Get page info and then add in queue
                        PageInfo pageInfo = getPageInfo(wc.getWebsiteName(), StringKit.sha1(url), url, doc, this.handler);
                        if (!StringKit.isEmpty(pageInfo.getTitle())) {
                            queue.put(pageInfo);
                        }
                    }
                } else {
                    doc = null;
                }
            }
            
        }
        
    }
    
    private boolean addIndex(String[] hashKeys) {
        boolean status = false;
        PreparedStatement pstmt = null;
        DruidPooledConnection conn = null;
        String sql = String.format("INSERT INTO %s ( hashKey ) VALUES ( ? )", conf.getConfigure().getIndexTable());
        
        try {
            conn = Db.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            for (String key : hashKeys) {
                pstmt.setString(1, key);
                pstmt.addBatch();
            }
            
            pstmt.executeBatch();
            status = true;
            
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            Db.close(conn, pstmt);
        }
        
        return status;
    }
    
    private String[] getValidHashKey(Set<String> keySet) {
        
        if (keySet.size() == 0) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        for (String key : keySet) {
            String k = String.format("'%s'", key);
            if (sb.length() == 0) {
                sb.append(k);
            } else {
                sb.append(",").append(k);
            }
        }
        
        try {
            String sql = String.format("SELECT hashKey FROM %s WHERE hashKey IN ( %s ) ORDER BY id DESC", conf.getConfigure().getIndexTable(), sb.toString());
            List<Map<String, Object>> result = Db.query(sql);
            
            List<String> urls = new ArrayList<String>();
            for (Map<String, Object> r : result) {
                urls.add(String.valueOf(r.get("hashKey")));
            }
            keySet.removeAll(urls);
        } catch (Exception e) {
            return null;
        }
        
        return keySet.size() > 0 ? keySet.toArray(new String[keySet.size()]) : null;
    }
    
    static class IndexHandler extends Thread {
        
        private final AbstractIndexerThread indexer;
        private volatile boolean closed = true;
        
        public IndexHandler(AbstractIndexerThread indexer) {
            this.indexer = indexer;
        }
        
        @Override
        public void run() {
            if (closed) {
                closed = false;
                try {
                    Document doc;
                    while (indexer.deque.size() != 0 || !closed) {
                        String url = indexer.deque.pollLast(2L, TimeUnit.SECONDS);
                        if (url != null && (doc = indexer.httpClient.parse(url, indexer.wc.getCharset())) != null) {
                            PageInfo pageInfo = indexer.getPageInfo(indexer.wc.getWebsiteName(), StringKit.sha1(url), url, doc, indexer.handler);
                            if (!StringKit.isEmpty(pageInfo.getTitle())) {
                                indexer.queue.put(pageInfo);
                            }
                        }
                        Thread.sleep(THREAD_SLEEP_TIME);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        public void close() {
            closed = true;
        }
        
    }

}
