package org.Kagan.core;

import org.Kagan.interfaces.IPageInfo;
import org.jsoup.nodes.Document;

public class PageInfoHandler implements IPageInfo {
    
    @Override
    public PageInfo getPageInfo(String hashKey, String url, Document doc) {
        PageInfo pageInfo = new PageInfo();
        System.out.println(doc.title());
        pageInfo.setTitle(doc.title());
        pageInfo.setLink(url);
        pageInfo.setHashKey(hashKey);
        pageInfo.setComeFrom("jobcn");
        return pageInfo;
    }
    
}
