package org.kagan.interfaces;

import java.sql.ResultSet;
import java.util.Map;

public interface IResultSet {
    /**
     * @param index the first index is 1, second is 2, ...
     */
    public Map<String, Object> handle(int index, ResultSet rs);
}
