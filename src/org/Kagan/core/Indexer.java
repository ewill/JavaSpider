package org.Kagan.core;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;

import org.Kagan.config.Configure;
import org.Kagan.config.WebsiteConfigure;
import org.Kagan.interfaces.IPageInfo;
import org.Kagan.util.Db;
import org.Kagan.util.StringKit;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.alibaba.druid.pool.DruidPooledConnection;

public class Indexer implements Runnable {
    
    private IPageInfo handler;
    private WebsiteConfigure wc;
    private BlockingDeque<String> deque;
    private BlockingQueue<PageInfo> queue;
    private volatile static boolean closed = true;
    
    public Indexer(WebsiteConfigure wc, BlockingDeque<String> deque, BlockingQueue<PageInfo> queue, IPageInfo handler) {
        this.wc    = wc;
        this.deque = deque;
        this.queue = queue;
        this.handler = handler;
    }
    
    public void shutdown() {
        if (closed) { return; }
        closed = true;
    }
    
    @Override
    public void run() {
        try {
            if (closed) {
                closed = false;
                indexPage();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void indexPage() throws IOException, InterruptedException {
        String[] hashKeys;
        String url = wc.getUrl();
        Map<String, String> map;
        boolean first = true;
        
        do {
            
            Document doc = parseHtml(url);
            
            if (!first) {
                // Get page info and then add in queue
                queue.put(getPageInfo(StringKit.sha1(url), url, doc));
            } else {
                first = false;
            }
            
            map = splitUrl(doc.html());
            hashKeys = getValidHashKey(map.keySet());
            if (null != hashKeys) {
                for (String k : hashKeys) {
                    deque.putFirst(map.get(k));
                }
                addHashKeyToDb(hashKeys);
            }
            
            map = null;
            
            try {
                url = deque.takeLast();
            } catch (InterruptedException e) {
                first = true;
                url = wc.getUrl();
                System.out.println("Set url to: " + wc.getUrl());
                Thread.currentThread().interrupt();
            }
            
        } while (!closed);
        
    }
    
    private Document parseHtml(String url) throws MalformedURLException, IOException {
        Document doc = Jsoup.connect(url).timeout(10000).get();
        return doc;
    }
    
    private Map<String, String> splitUrl(String html) {
        String url;
        Map<String, String> map = new HashMap<String, String>();
        Matcher matcher = wc.getRegex().matcher(html);
        while (matcher.find()) {
            url = matcher.group(1);
            map.put(StringKit.sha1(url), url);
        }
        return map;
    }
    
    private PageInfo getPageInfo(String hashKey, String url, Document doc) {
        return handler.getPageInfo(hashKey, url, doc);
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
        
        return keySet.toArray(new String[keySet.size()]);
    }

}
