package org.javaspider.core;

import java.util.concurrent.BlockingQueue;

import org.javaspider.config.Config;

public abstract class AbstractWriterThread extends Thread {
    
    protected final Config conf;
    protected final BlockingQueue<PageInfo> queue;
    
    public AbstractWriterThread(Config conf, BlockingQueue<PageInfo> queue) {
        this.conf = conf;
        this.queue = queue;
    }
    
    public abstract void run();
    public abstract void close();
}
