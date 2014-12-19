package org.kagan.handler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.kagan.core.PageInfo;
import org.kagan.interfaces.IPageInfo;

public class PageInfoHandler implements IPageInfo {
    
    private static final Pattern DATE_PATTERN = Pattern.compile("(((\\d{4})年(\\d{2})月(\\d{2}))日|((\\d{4})-(\\d{2})-(\\d{2})))");
    
    @Override
    public PageInfo getPageInfo(Document doc) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        PageInfo pageInfo = new PageInfo();
        Matcher matcher = DATE_PATTERN.matcher(doc.body().html());
        try {
            if (matcher.find()) {
                pageInfo.setPostTime(format.parse(matcher.group(1).replaceAll("年|月|日", "-")));
            }
        } catch (ParseException e) {
        }
        
        String title = doc.title();
        title = title.contains("404") || title.contains("页面不存在") ? "" : title;
        pageInfo.setTitle(title);
        return pageInfo;
    }
    
}
