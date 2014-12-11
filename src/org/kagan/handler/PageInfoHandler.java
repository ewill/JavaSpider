package org.kagan.handler;

import org.jsoup.nodes.Document;
import org.kagan.core.PageInfo;
import org.kagan.interfaces.IPageInfo;

public class PageInfoHandler implements IPageInfo {
    
    @Override
    public PageInfo getPageInfo(Document doc) {
        PageInfo pageInfo = new PageInfo();
        pageInfo.setTitle(doc.title());
        return pageInfo;
    }
    
}
