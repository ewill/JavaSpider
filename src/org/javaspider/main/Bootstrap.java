package org.javaspider.main;

import java.util.Properties;

import org.javaspider.config.Config;
import org.javaspider.config.JavaSpiderConfig;
import org.javaspider.core.JavaSpider;
import org.javaspider.kit.ConfigKit;
import org.javaspider.kit.PathKit;

public class Bootstrap {
    
    private final Config c;
    private final Properties pro;
    private final JavaSpider spider;
    private final JavaSpiderConfig config;
    
    public Bootstrap() {
        this.c = new Config();
        this.pro = ConfigKit.loadProperty(PathKit.getRootPath() + "/configure/javaspider.properties");
        this.config = (JavaSpiderConfig)(ConfigKit.newInstance(ConfigKit.loadClass(pro.getProperty("javaspider.config.class"))));
        this.config.config(this.c.getConfigure());
        this.config.configHttpClient(this.c.getHttpClientConfig());
        this.spider = new JavaSpider(this.c);
    }
    
    public void start() {
        config.beforeStart();
        spider.start();
    }
    
    public void stop() {
        config.beforeStop();
        spider.shutdown();
    }
    
    public static void main(String[] args) {
        new Bootstrap().start();
    }
}
