package org.javaspider.interfaces;

import org.javaspider.core.PageInfo;
import org.jsoup.nodes.Document;

public interface IPageHandler {
    public PageInfo getPageInfo(Document doc);
}
