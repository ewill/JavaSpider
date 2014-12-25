package org.javaspider.core;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.javaspider.config.Config;
import org.javaspider.kit.Db;

import com.alibaba.druid.pool.DruidPooledConnection;

public class DefaultDbWriterThread extends AbstractWriterThread {
    
    private final List<PageInfo> buff;
    private volatile boolean closed = false;
    private static final short BUFF_SIZE = 10;
    private static final int THREAD_SLEEP_TIME = 500;
    private static final Log log = LogFactory.getLog(DefaultDbWriterThread.class);
    
    public DefaultDbWriterThread(Config conf, BlockingQueue<PageInfo> queue) {
        super(conf, queue);
        this.buff = new ArrayList<PageInfo>(BUFF_SIZE);
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
            if (buff.size() >= BUFF_SIZE) {
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
            pstmt = conn.prepareStatement(String.format("INSERT INTO %s ( title, link, pageContent, comeFrom, postTime ) VALUES ( ?, ?, ?, ?, ? ) ", conf.getConfigure().getDataTable()));
            
            for (PageInfo pageInfo : buff) {
                pstmt.setString(1, pageInfo.getTitle());
                pstmt.setString(2, pageInfo.getLink());
                pstmt.setString(3, pageInfo.getPageContent() != null ? pageInfo.getPageContent() : "");
                pstmt.setString(4, pageInfo.getComeFrom());
                try {
                    pstmt.setDate(5, new Date(pageInfo.getPostTime().getTime()));
                } catch (Exception e) {
                    pstmt.setDate(5, new Date(System.currentTimeMillis()));
                }
                pstmt.addBatch();
            }
            
            pstmt.executeBatch();
            status = true;
            
        } catch (Exception e) {
            log.error("Write data to db fail!", e);
        } finally {
            Db.close(conn, pstmt);
        }
        
        buff.clear();
        return status;
    }
    
    @Override
    public void close() {
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
