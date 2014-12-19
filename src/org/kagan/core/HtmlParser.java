package org.kagan.core;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.kagan.config.Configure;

public final class HtmlParser {
    
    private final IdleConnectionMonitorThread monitor;
    private final CloseableHttpClient httpClient;
    private static final HtmlParser me = new HtmlParser();
    private final PoolingHttpClientConnectionManager connMgr;
    private static final int CONNECT_TIMEOUT = 8000;
    
    private HtmlParser() {
        this.connMgr = new PoolingHttpClientConnectionManager();
        this.connMgr.setDefaultMaxPerRoute(10);
        this.connMgr.setMaxTotal(Configure.websiteNum * 50);
        this.httpClient = HttpClients.custom().setDefaultRequestConfig(
            RequestConfig.custom().setSocketTimeout(CONNECT_TIMEOUT).setConnectTimeout(CONNECT_TIMEOUT).build()
        ).setRedirectStrategy(new RedirectStrategy()).setConnectionManager(this.connMgr).build();
        
        monitor = new IdleConnectionMonitorThread(this.connMgr);
        monitor.start();
    }
    
    public static Document parse(String url, String charset) {
        try {
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36 SE 2.X MetaSr 1.0");
            CloseableHttpResponse response = me.httpClient.execute(httpGet, HttpClientContext.create());
            try {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    HttpEntity entity = response.getEntity();
                    return Jsoup.parse(EntityUtils.toString(entity, charset));
                } else {
                    System.out.println(String.format("[%s] - HtmlParser: %s - Status Code : %d.", new Date(), url, statusCode));
                }
            } finally {
                response.close();
            }
        } catch (Exception e) {
            System.out.println(String.format("[%s] - HtmlParser: %s %s.", new Date(), url, e.getMessage()));
        }
        
        return null;
    }
    
    public static final void shutdown() {
        try {
            me.monitor.shutdown();
            me.httpClient.close();
            me.connMgr.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static class RedirectStrategy extends DefaultRedirectStrategy {

        @Override
        public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
            boolean isRedirect = false;
            try {
                isRedirect = super.isRedirected(request, response, context);
            } catch (ProtocolException e) {
                e.printStackTrace();
            }
            if (!isRedirect) {
                int responseCode = response.getStatusLine().getStatusCode();
                if (responseCode == 301 || responseCode == 302) {
                    return true;
                }
            }
            return isRedirect;
        }
        
    }
    
    public static class IdleConnectionMonitorThread extends Thread {
        
        private volatile boolean shutdown;
        private final PoolingHttpClientConnectionManager connMgr;
        private static final int WAIT_TIMEOUT = 8000;
        
        public IdleConnectionMonitorThread(PoolingHttpClientConnectionManager connMgr) {
            this.connMgr = connMgr;
        }

        @Override
        public void run() {
            try {
                while (!shutdown) {
                    synchronized (this) {
                        wait(WAIT_TIMEOUT);
                        connMgr.closeExpiredConnections();
                        connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        public void shutdown() {
            shutdown = true;
            synchronized(this) {
                notifyAll();
            }
        }
        
    }
}
