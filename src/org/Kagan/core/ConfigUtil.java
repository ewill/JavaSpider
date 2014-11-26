package org.Kagan.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public final class ConfigUtil {
    
    public static final String RootPath = ConfigUtil.class.getClassLoader().getResource(".").getPath().replace("bin/", "configure/");
    
    public static final Properties loadProperties(String fileName) throws FileNotFoundException, IOException {
        String path = String.format("%s%s", RootPath, fileName);
        Properties properties = new Properties();
        properties.load(new FileInputStream(new File(path)));
        return properties;
    }
    
    public static final Configure loadKaganXml(String fileName) throws ParserConfigurationException, SAXException, IOException {
        String path = String.format("%s%s", RootPath, fileName);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = factory.newDocumentBuilder();
        Document document = db.parse(new File(path));
        NodeList rootList = document.getElementsByTagName("KaganRoot");
        Configure bean = new Configure();
        

        if (rootList.getLength() > 0) {
            Element root = (Element)rootList.item(0);
            
            // KeyTable
            bean.SetKeyTable(((Element)root.getElementsByTagName("KeyTable").item(0)).getAttribute("name"));
            
            // DataTable
            bean.SetDataTable(((Element)root.getElementsByTagName("DataTable").item(0)).getAttribute("name"));
            
            // WriteDbThreads
            bean.SetWriteDbThreads(Integer.valueOf(((Element)root.getElementsByTagName("WriteDbThreads").item(0)).getAttribute("value")));
            
            // QueueSize
            bean.SetQueueSize(Integer.valueOf(((Element)root.getElementsByTagName("QueueSize").item(0)).getAttribute("value")));
            
            // WebsiteList
            NodeList websiteList = root.getElementsByTagName("WebsiteList");
            if (websiteList.getLength() > 0) {
                NodeList websites = ((Element)websiteList.item(0)).getElementsByTagName("website");
                for (int i = 0; i < websites.getLength(); i++) {
                    Element website = (Element)websites.item(i);
                    WebsiteConfigure wc = new WebsiteConfigure();
                    String name = website.getAttribute("name");
                    wc.setWebsiteName(name);
                    wc.setUrl(website.getElementsByTagName("url").item(0).getFirstChild().getNodeValue());
                    wc.setRegex(Pattern.compile(website.getElementsByTagName("regex").item(0).getFirstChild().getNodeValue()));
                    wc.setDailySize(Integer.valueOf(website.getElementsByTagName("DailySize").item(0).getFirstChild().getNodeValue()));
                    bean.SetWebsite(name, wc);
                }
            }
            
        }
        
        System.out.println(bean.toString());
        
        return bean;
    }
    
}
