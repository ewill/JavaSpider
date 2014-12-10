package org.Kagan.core;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.Kagan.config.Configure;
import org.Kagan.util.Db;

import com.alibaba.druid.pool.DruidPooledConnection;

public class DbWriter implements Runnable {
    
    private volatile static boolean closed = true;
    private static final short BUFF_SIZE = 10;
    private static final int THREAD_SLEEP_TIME = 500;
    
    private final List<PageInfo> buff;
    private final BlockingQueue<PageInfo> queue;
    
    public DbWriter(BlockingQueue<PageInfo> queue) {
        this.queue = queue;
        this.buff = new ArrayList<PageInfo>(BUFF_SIZE);
    }
    
    @Override
    public String toString() {
        return String.format("Buff Size : %d", buff.size());
    }
    
    @Override
    public void run() {
        if (closed) {
            closed = false;
            try {
                writeData();
            } catch (InterruptedException e) {
                System.out.println("Thread sleep...");
                System.out.println("Before " + Thread.currentThread().isInterrupted());
                Thread.currentThread().interrupt();
                System.out.println("After " + Thread.currentThread().isInterrupted());
            }
        }
    }
    
    private void writeData() throws InterruptedException {
        while (!closed) {
            if (buff.size() == BUFF_SIZE) {
                writeToDb();
            }
            
            buff.add(queue.take());
            Thread.sleep(THREAD_SLEEP_TIME);
        }
        
        if (buff.size() > 0) {
            writeToDb();
        }
    }
    
    private boolean writeToDb() {
        boolean status = false;
        PreparedStatement pstmt = null;
        DruidPooledConnection conn = null;
        
        try {
            conn = Db.getConnection();
            pstmt = conn.prepareStatement(String.format("INSERT INTO %s ( title, link, pageContent, comeFrom ) VALUES ( ?, ?, ?, ? ) ", Configure.dataTable));
            
            for (PageInfo pageInfo : buff) {
                pstmt.setString(1, pageInfo.getTitle());
                pstmt.setString(2, pageInfo.getLink());
                pstmt.setString(3, pageInfo.getPageContent().length() > 0 ? pageInfo.getPageContent() : "");
                pstmt.setString(4, pageInfo.getComeFrom());
                pstmt.addBatch();
            }
            
            pstmt.executeBatch();
            status = true;
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Db.close(conn, pstmt);
        }
        
        buff.clear();
        return status;
    }
    
    public void shutdown() {
        if (closed) { return; }
        closed = true;
    }
}
