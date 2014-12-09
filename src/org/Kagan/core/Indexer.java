package org.Kagan.core;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;

import org.Kagan.config.Configure;

public class Indexer implements Runnable {
    
    private Configure conf;
    private BlockingDeque<PageInfo> deque;
    private BlockingQueue<PageInfo> queue;
    
    public Indexer(Configure conf, BlockingDeque<PageInfo> deque, BlockingQueue<PageInfo> queue) {
        this.conf  = conf;
        this.deque = deque;
        this.queue = queue;
    }
    
    @Override
    public void run() {
        indexPage();
    }
    
    private void indexPage() {
        
    }
    
}
