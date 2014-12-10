package org.Kagan.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.Kagan.interfaces.IResultSet;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.druid.pool.DruidPooledConnection;

public final class Db {
    
    private static DruidDataSource ds = null;
    
    private Db() {}
    
    public static final Map<String, Object> data(Object... args) {
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        for (int i = 0; i < args.length; i += 2) {
            data.put(String.valueOf(args[i]), args[i + 1]);
        }
        return data;
    }
    
    public synchronized static final void Init(Properties p) throws Exception {
        if (ds == null) {
            ds = (DruidDataSource)DruidDataSourceFactory.createDataSource(p);
        }
    }
    
    public synchronized static final DruidPooledConnection getConnection() throws SQLException {
        return ds != null ? ds.getConnection() : null;
    }
    
    public static final boolean insert(String table, Map<String, Object> data) {
        return insert(table, data, false) > 0;
    }
    
    public static final int insert(String table, Map<String, Object> data, boolean retValue) {
        int newId = -1;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        DruidPooledConnection conn = null;
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(table).append(" ")
           .append("( ").append(StringKit.join(data.keySet(), ",")).append(" ) ")
           .append("VALUES ( ").append(StringKit.join(StringKit.repeat("?", data.size()), ",")).append(" ) ")
           .append("SELECT @@IDENTITY AS newId ");
        
        try {
            int i = 1;
            conn = getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            
            for (Map.Entry<String, Object> d : data.entrySet()) {
                pstmt.setObject(i++, d.getValue());
            }
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                newId = rs.getInt("newId");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(conn, pstmt, rs);
        }
        
        return newId;
    }
    
    public static final boolean update(String table, Map<String, Object> data) {
        return update(table, data, null);
    }
    
    public static final boolean update(String table, Map<String, Object> data, String where, Object... args) {
        boolean status = false;
        PreparedStatement pstmt = null;
        DruidPooledConnection conn = null;
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(table).append(" ")
           .append("SET ").append(StringKit.joinMap(data, "=", "?", ",")).append(" ");
        
        if (where != null && where.length() > 0) {
            sql.append("WHERE ").append(where);
        }
        
        try {
            int i = 1;
            conn = getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            
            for (Map.Entry<String, Object> d : data.entrySet()) {
                pstmt.setObject(i++, d.getValue());
            }
            
            for (Object o : args) {
                pstmt.setObject(i++, o);
            }
            
            status = pstmt.executeUpdate() > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(conn, pstmt);
        }
        
        return status;
    }
    
    public static final List<Map<String, Object>> query(String sql, Object... args) {
        return query(sql, null, args);
    }
    
    public static final List<Map<String, Object>> query(String sql, IResultSet handler, Object... args) {
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        DruidPooledConnection conn = null;
        Map<String, Object> map = null;
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        
        try {
            int i = 1;
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            
            for (Object o : args) {
                pstmt.setObject(i++, o);
            }
            
            rs = pstmt.executeQuery();
            
            if (handler != null) {
                i = 1;
                while (rs.next()) {
                    result.add(handler.handle(i++, rs));
                }
            } else {
                while (rs.next()) {
                    map = new HashMap<String, Object>();
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    for (int k = 1; k <= columnCount; k++) {
                        String label = metaData.getColumnLabel(k);
                        map.put(label, rs.getObject(label));
                    }
                    result.add(map);
                }
            }
            
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(conn, pstmt, rs);
        }
        
        return result;
    }
    
    public static final int count(String table) {
        return count(table, null);
    }
    
    public static final int count(String table, String where, Object... args) {
        int cnt = -1;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        DruidPooledConnection conn = null;
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(1) AS cnt FROM ").append(table).append(" ");
        if (!StringKit.isEmpty(where)) {
            sql.append("WHERE ").append(where);
        }
        
        try {
            int i = 1;
            conn = getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            
            for (Object o : args) {
                pstmt.setObject(i++, o);
            }
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                cnt = rs.getInt("cnt");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(conn, pstmt, rs);
        }
        
        return cnt;
    }
    
    public static final void close(DruidPooledConnection conn, PreparedStatement pstmt) {
        close(conn, pstmt, null);
    }
    
    public static final void close(DruidPooledConnection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            
            if (pstmt != null) {
                pstmt.close();
            }
            
            if (conn != null) {
                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static final void shutdown() {
        if (ds != null) {
            ds.close();
            ds = null;
        }
    }
}
