package org.kagan.core;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.kagan.config.Configure;

public final class HtmlParser {
    
    private final CloseableHttpClient httpClient;
    private static final HtmlParser me = new HtmlParser();
    private final PoolingHttpClientConnectionManager connMgr;
    
    private HtmlParser() {
        this.connMgr = new PoolingHttpClientConnectionManager(5, TimeUnit.SECONDS);
        this.connMgr.setDefaultMaxPerRoute(5);
        this.connMgr.setMaxTotal(Configure.websiteNum * 50);
        this.httpClient = HttpClients.custom().setConnectionManager(this.connMgr).build();
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
                    System.out.println(String.format("HtmlParser: %s - Status Code : %d.", url, statusCode));
                }
            } catch (Exception e) {
                System.out.println(String.format("HtmlParser: %s %s.", url, e.getMessage()));
            } finally {
                response.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static final void shutdown() {
        try {
            me.httpClient.close();
            me.connMgr.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
