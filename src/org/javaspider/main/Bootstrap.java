package org.javaspider.main;

import java.util.Properties;

import org.javaspider.config.Config;
import org.javaspider.config.JavaSpiderConfig;
import org.javaspider.core.JavaSpider;
import org.javaspider.kit.ConfigKit;
import org.javaspider.kit.Db;
import org.javaspider.kit.PathKit;

public class Bootstrap {
    
    protected final Config c;
    protected final Properties pro;
    protected final JavaSpider spider;
    protected final JavaSpiderConfig config;
    
    public Bootstrap() {
        this.c = new Config();
        this.pro = ConfigKit.loadProperty(PathKit.getRootPath() + "javaspider.properties");
        this.config = (JavaSpiderConfig)(ConfigKit.newInstance(ConfigKit.loadClass(pro.getProperty("javaspider.config.class"))));
        this.config.config(this.c.getConfigure());
        this.config.configHttpClient(this.c.getHttpClientConfig());
        this.spider = new JavaSpider(this.c);
    }
    
    public void start() {
        config.beforeStart();
        Db.Init(ConfigKit.loadProperty(PathKit.getRootPath() + "druid.properties"));
        spider.start();
    }
    
    public void stop() {
        config.beforeStop();
        spider.shutdown();
        Db.shutdown();
    }

}
