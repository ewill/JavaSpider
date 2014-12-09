package org.Kagan.interfaces;

import org.Kagan.core.PageInfo;
import org.jsoup.nodes.Document;

public interface IPageInfo {
    public PageInfo getPageInfo(String hashKey, String url, Document doc);
}
