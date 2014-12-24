package org.javaspider.handler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javaspider.core.PageInfo;
import org.javaspider.interfaces.IPageHandler;
import org.jsoup.nodes.Document;

public class PageInfoHandler implements IPageHandler {
    
    private static final Pattern DATE_PATTERN = Pattern.compile("(((\\d{4})年(\\d{2})月(\\d{2}))日|((\\d{4})-(\\d{2})-(\\d{2})))");
    
    @Override
    public PageInfo getPageInfo(Document doc) {
        PageInfo pageInfo = new PageInfo();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Matcher matcher = DATE_PATTERN.matcher(doc.body().html());
        try {
            if (matcher.find()) {
                pageInfo.setPostTime(format.parse(matcher.group(1).replaceAll("年|月|日", "-")));
            }
        } catch (ParseException e) {
        }
        
        String title = doc.title();
        title = title.contains("404") || title.contains("页面不存在") || title.contains("页面找不到") ? "" : title;
        pageInfo.setTitle(title);
        return pageInfo;
    }
    
}
