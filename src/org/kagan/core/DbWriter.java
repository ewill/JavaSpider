package org.kagan.core;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.kagan.config.Configure;
import org.kagan.util.Db;

import com.alibaba.druid.pool.DruidPooledConnection;

public class DbWriter extends Thread {
    
    private volatile static boolean closed = false;
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
        try {
            writeData();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void writeData() throws InterruptedException {
        while (!closed) {
            if (buff.size() == BUFF_SIZE) {
                writeToDb();
            }
            
            PageInfo pageInfo = queue.poll(5L, TimeUnit.SECONDS);
            if (pageInfo != null) {
                buff.add(pageInfo);
            } else if (buff.size() > 0) {
                writeToDb();
            }
            
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
            pstmt = conn.prepareStatement(String.format("INSERT INTO %s ( title, link, pageContent, comeFrom, postTime ) VALUES ( ?, ?, ?, ?, ? ) ", Configure.dataTable));
            
            for (PageInfo pageInfo : buff) {
                pstmt.setString(1, pageInfo.getTitle());
                pstmt.setString(2, pageInfo.getLink());
                pstmt.setString(3, pageInfo.getPageContent() != null ? pageInfo.getPageContent() : "");
                pstmt.setString(4, pageInfo.getComeFrom());
                pstmt.setDate(5, new Date(pageInfo.getPostTime().getTime()));
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
        closed = true;
        while (true) {
            if (buff.size() == 0) {
                break;
            } else {
                writeToDb();
            }
        }
    }
}
