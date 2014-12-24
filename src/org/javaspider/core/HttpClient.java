package org.javaspider.core;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
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
import org.javaspider.config.Config;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public final class HttpClient {
    
    private final Config conf;
    private final CloseableHttpClient httpClient;
    private final IdleConnectionMonitorThread monitor;
    private final PoolingHttpClientConnectionManager connMgr;
    private static final Log log = LogFactory.getLog(HttpClient.class);
    
    HttpClient(Config conf) {
        this.conf = conf;
        this.connMgr = new PoolingHttpClientConnectionManager();
        this.connMgr.setDefaultMaxPerRoute(this.conf.getHttpClientConfig().getMaxPerRoute());
        this.connMgr.setMaxTotal(this.conf.getConfigure().getWebsites().size() * this.conf.getHttpClientConfig().getMaxPerRoute());
        this.httpClient = HttpClients.custom().setDefaultRequestConfig(
            RequestConfig.custom()
                         .setCircularRedirectsAllowed(false)
                         .setRedirectsEnabled(this.conf.getHttpClientConfig().isRedirectEnabled())
                         .setRelativeRedirectsAllowed(this.conf.getHttpClientConfig().isRedirectEnabled())
                         .setSocketTimeout(this.conf.getHttpClientConfig().getSocketTimeout())
                         .setConnectTimeout(this.conf.getHttpClientConfig().getConnectionTimeout())
                         .setConnectionRequestTimeout(this.conf.getHttpClientConfig().getRequestTimeout())
                         .build()
        ).setRedirectStrategy(new RedirectStrategy())
         .setConnectionManager(this.connMgr)
         .setRetryHandler(new RequestRetryHandler(this.conf.getHttpClientConfig().getRetryTimes()))
         .build();
        
        this.monitor = new IdleConnectionMonitorThread(this.connMgr);
        this.monitor.start();
    }
    
    public final Document parse(String url) {
        return parse(url, Config.DEFAULT_CHARSET);
    }
    
    public final Document parse(String url, String charset) {
        try {
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader(HttpHeaders.USER_AGENT, conf.getHttpClientConfig().getUserAgent());
            CloseableHttpResponse response = httpClient.execute(httpGet, HttpClientContext.create());
            try {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    HttpEntity entity = response.getEntity();
                    return Jsoup.parse(EntityUtils.toString(entity, charset));
                } else {
                    log.error(String.format("HttpClient: %s - Status Code : %d.", url, statusCode));
                }
            } finally {
                response.close();
            }
        } catch (Exception e) {
            log.error(String.format("HttpClient: %s %s.", url, e.getMessage()), e);
        }
        
        return null;
    }
    
    public final void shutdown() {
        try {
            monitor.shutdown();
            httpClient.close();
            connMgr.shutdown();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
    
    public static class RequestRetryHandler implements HttpRequestRetryHandler {
        
        private final int retryTimes;
        
        public RequestRetryHandler(int retryTimes) {
            this.retryTimes = retryTimes;
        }

        @Override
        public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
            boolean isRetry = true;
            if (executionCount >= retryTimes) {
                isRetry = false;
            }
            return isRetry;
        }
        
    }
    
    public static class RedirectStrategy extends DefaultRedirectStrategy {

        @Override
        public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
            boolean isRedirect = false;
            try {
                isRedirect = super.isRedirected(request, response, context);
            } catch (ProtocolException e) {
                log.error(e.getMessage(), e);
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
                Thread.currentThread().interrupt();
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
