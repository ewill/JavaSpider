package org.kagan.core;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import org.jsoup.nodes.Document;
import org.kagan.config.Configure;
import org.kagan.config.WebsiteConfigure;
import org.kagan.interfaces.IPageInfo;
import org.kagan.util.Db;
import org.kagan.util.StringKit;

import com.alibaba.druid.pool.DruidPooledConnection;

public class Indexer extends Thread {
    
    private final String indexName;
    private final IPageInfo handler;
    private final WebsiteConfigure wc;
    private final IndexHandler[] indexHandlers;
    private final BlockingDeque<String> deque;
    private final BlockingQueue<PageInfo> queue;
    private volatile static boolean closed = false;
    private static final int THREAD_SLEEP_TIME  = 4000;
    
    public Indexer(WebsiteConfigure wc, BlockingDeque<String> deque, BlockingQueue<PageInfo> queue) {
        this.wc      = wc;
        this.queue   = queue;
        this.deque   = deque;
        this.handler = wc.getHandler();
        this.indexName = wc.getWebsiteName();
        this.indexHandlers = new IndexHandler[Configure.readThreads];
        for (int i = 0; i < indexHandlers.length; i++) {
            indexHandlers[i] = new IndexHandler(this.wc, this.deque, this.queue, this.handler);
        }
    }
    
    @Override
    public String toString() {
        return String.format("%s Deque Size : %d", indexName, deque.size());
    }
    
    public void shutdown() {
        closed = true;
        
        try {
            for (IndexHandler h : indexHandlers) {
                h.shutdown();
                h.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
    }
    
    @Override
    public void run() {
        try {
            indexPage();
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
        
        Document doc = HtmlParser.parse(wc.getUrl(), wc.getCharset());
        while (!closed) {
            if (doc == null) {
                doc = HtmlParser.parse(wc.getUrl(), wc.getCharset());
                Thread.sleep(THREAD_SLEEP_TIME);
            } else {
                map = splitUrl(doc.html());
                hashKeys = getValidHashKey(map.keySet());
                if (hashKeys != null) {
                    addHashKeyToDb(hashKeys);
                    for (int i = 1; i < hashKeys.length; i++) {
                        deque.putFirst(map.get(hashKeys[i]));
                    }
                    
                    String url = map.get(hashKeys[0]);
                    if (url != null && (doc = HtmlParser.parse(url, wc.getCharset())) != null) {
                        // Get page info and then add in queue
                        PageInfo pageInfo = getPageInfo(wc.getWebsiteName(), StringKit.sha1(url), url, doc, handler);
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
    
    private Map<String, String> splitUrl(String html) {
        String url;
        Map<String, String> map = new LinkedHashMap<String, String>();
        Matcher matcher = wc.getRegex().matcher(html);
        while (matcher.find()) {
            url = matcher.group(1);
            map.put(StringKit.sha1(url), url);
        }
        return map;
    }
    
    public static PageInfo getPageInfo(String websiteName, String hashKey, String url, Document doc, IPageInfo handler) {
        PageInfo pageInfo = handler.getPageInfo(doc);
        pageInfo.setLink(url);
        pageInfo.setHashKey(hashKey);
        pageInfo.setComeFrom(websiteName);
        return pageInfo;
    }
    
    private boolean addHashKeyToDb(String[] hashKeys) {
        boolean status = false;
        PreparedStatement pstmt = null;
        DruidPooledConnection conn = null;
        String sql = String.format("INSERT INTO %s ( hashKey ) VALUES ( ? )", Configure.indexTable);
        
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
            e.printStackTrace();
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
            String sql = String.format("SELECT hashKey FROM %s WHERE hashKey IN ( %s ) ORDER BY id DESC", Configure.indexTable, sb.toString());
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
        
        private IPageInfo handler;
        private final WebsiteConfigure wc;
        private BlockingDeque<String> deque;
        private BlockingQueue<PageInfo> queue;
        private volatile static boolean closed = false;
        
        public IndexHandler(WebsiteConfigure wc, BlockingDeque<String> deque, BlockingQueue<PageInfo> queue, IPageInfo handler) {
            this.wc      = wc;
            this.deque   = deque;
            this.queue   = queue;
            this.handler = handler;
        }
        
        @Override
        public void run() {
            try {
                Document doc;
                while (deque.size() != 0 || !closed) {
                    String url = deque.pollLast(2L, TimeUnit.SECONDS);
                    if (url != null && (doc = HtmlParser.parse(url, wc.getCharset())) != null) {
                        PageInfo pageInfo = getPageInfo(wc.getWebsiteName(), StringKit.sha1(url), url, doc, handler);
                        if (!StringKit.isEmpty(pageInfo.getTitle())) {
                            queue.put(pageInfo);
                        }
                    }
                    Thread.sleep(THREAD_SLEEP_TIME);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        public void shutdown() {
            closed = true;
        }
        
    }

}
