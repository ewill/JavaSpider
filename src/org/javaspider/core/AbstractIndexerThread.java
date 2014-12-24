package org.javaspider.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;

import org.javaspider.config.Config;
import org.javaspider.config.WebsiteConfigure;
import org.javaspider.handler.PageInfoHandler;
import org.javaspider.interfaces.IPageHandler;
import org.javaspider.kit.ConfigKit;
import org.javaspider.kit.StringKit;
import org.jsoup.nodes.Document;

public abstract class AbstractIndexerThread extends Thread {
    
    protected final Config conf;
    protected final WebsiteConfigure wc;
    protected final IPageHandler handler;
    protected final HttpClient httpClient;
    protected final BlockingDeque<String> deque;
    protected final BlockingQueue<PageInfo> queue;
    
    public AbstractIndexerThread(Config conf, HttpClient httpClient, WebsiteConfigure wc, BlockingDeque<String> deque, BlockingQueue<PageInfo> queue) {
        this.wc         = wc;
        this.conf       = conf;
        this.queue      = queue;
        this.deque      = deque;
        this.httpClient = httpClient;
        
        Class<? extends IPageHandler> handlerClass = wc.getHandlerClass();
        if (handlerClass == null) {
            this.handler = new PageInfoHandler();
        } else {
            this.handler = (IPageHandler)ConfigKit.newInstance(handlerClass);
        }
    }
    
    public abstract void run();
    public abstract void close();
    
    public final PageInfo getPageInfo(String websiteName, String hashKey, String url, Document doc, IPageHandler handler) {
        PageInfo pageInfo = handler.getPageInfo(doc);
        pageInfo.setLink(url);
        pageInfo.setHashKey(hashKey);
        pageInfo.setComeFrom(websiteName);
        return pageInfo;
    }
    
    public Map<String, String> splitUrl(String html) {
        String url;
        Map<String, String> map = new LinkedHashMap<String, String>();
        Matcher matcher = wc.getRegex().matcher(html);
        while (matcher.find()) {
            url = matcher.group(1);
            map.put(StringKit.sha1(url), url);
        }
        return map;
    }
}
