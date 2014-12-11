package org.kagan.interfaces;

import org.jsoup.nodes.Document;
import org.kagan.core.PageInfo;

public interface IPageInfo {
    public PageInfo getPageInfo(Document doc);
}
