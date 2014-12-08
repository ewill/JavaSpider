package org.Kagan.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.Kagan.config.Configure;
import org.Kagan.util.Db;
import org.Kagan.util.StringKit;


public class SpiderRobot {
    
    private static Configure conf;
    private static boolean closed = true;
    
    public static final void init(Configure c) {
        conf = c;
    }
    
    public final SpiderRobot start() {
        closed = false;
        return this;
    }
    
    public static final void shutdown() {
        if (closed) { return; }
        
        // todo
        
        closed = true;
    }
    
    public final int countRecords() {
        return Db.count(conf.getDataTable());
    }
    
    public final List<Map<String, Object>> checkRepeatData() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT hashKey ")
           .append("FROM ").append(conf.getIndexTable()).append(" ")
           .append("GROUP BY hashKey ")
           .append("HAVING COUNT(hashKey) > 1");
        return Db.query(sql.toString());
    }
    
    public final String formatRepeatData(List<Map<String, Object>> data) {
        List<String> result = new ArrayList<String>();
        for (Map<String, Object> map : data) {
            result.add(String.valueOf(map.get("hashKey")));
        }
        return String.format("[\n    %s\n]", StringKit.join(result, ",\n    "));
    }
    
}
