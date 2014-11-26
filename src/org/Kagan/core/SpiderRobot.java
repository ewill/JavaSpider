package org.Kagan.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.Kagan.interfaces.IQueue;
import org.Kagan.interfaces.ISearchHandler;
import org.Kagan.interfaces.IWriteDataHandler;
import org.Kagan.util.Db;
import org.Kagan.util.StringKit;


public class SpiderRobot {
    
    private static IQueue queue;
    private static Configure conf;
    private static boolean closed = true;
    private static ISearchHandler searchHandler;
    private static IWriteDataHandler writeHandler;
    
    public static final void Init(Configure c, IQueue q) {
        conf = c;
        queue = q;
    }
    
    public final SpiderRobot Start() {
        closed = false;
        return this;
    }
    
    /**
     * How to search website page
     */
    public final SpiderRobot SetSearchHandler(ISearchHandler handler) {
        searchHandler = handler;
        return this;
    }
    
    /**
     * How to save website data
     */
    public final SpiderRobot SetWriteDataHandler(IWriteDataHandler handler) {
        writeHandler = handler;
        return this;
    }
    
    public static final void Shutdown() {
        if (closed) { return; }
        
        // todo
        
        closed = true;
    }
    
    public final int CountRecords() {
        return Db.count(conf.GetDataTable());
    }
    
    public final List<Map<String, Object>> CheckRepeatData() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT hashKey ")
           .append("FROM ").append(conf.GetKeyTable()).append(" ")
           .append("GROUP BY hashKey ")
           .append("HAVING COUNT(hashKey) > 1");
        return Db.query(sql.toString());
    }
    
    public final String FormatRepeatData(List<Map<String, Object>> data) {
        List<String> result = new ArrayList<String>();
        for (Map<String, Object> map : data) {
            result.add(String.valueOf(map.get("hashKey")));
        }
        return String.format("[\n    %s\n]", StringKit.join(result, ",\n    "));
    }
    
}
