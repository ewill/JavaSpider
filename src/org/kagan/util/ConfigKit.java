package org.kagan.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.kagan.config.Configure;
import org.kagan.config.WebsiteConfigure;
import org.kagan.interfaces.IPageInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public final class ConfigKit {
public static final String RootPath = ConfigKit.class.getClassLoader().getResource(".").getPath().replace("bin/", "configure/");
    
    public static final Properties loadProperties(String fileName) throws FileNotFoundException, IOException {
        String path = String.format("%s%s", RootPath, fileName);
        Properties properties = new Properties();
        properties.load(new FileInputStream(new File(path)));
        return properties;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static final Class<? extends IPageInfo> loadClass(String className) {
        Class clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return clazz;
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
            
            // IndexTable
            Configure.indexTable = ((Element)root.getElementsByTagName("IndexTable").item(0)).getAttribute("name");
            
            // DataTable
            Configure.dataTable = ((Element)root.getElementsByTagName("DataTable").item(0)).getAttribute("name");
            
            // ReadThreads
            Configure.readThreads = Integer.valueOf(((Element)root.getElementsByTagName("ReadThreads").item(0)).getAttribute("value"));
            
            // WriteDbThreads
            Configure.writeDbThreads = Integer.valueOf(((Element)root.getElementsByTagName("WriteDbThreads").item(0)).getAttribute("value"));
            
            // QueueSize
            Configure.queueSize = Integer.valueOf(((Element)root.getElementsByTagName("QueueSize").item(0)).getAttribute("value"));
            
            // DequeSize
            Configure.dequeSize = Integer.valueOf(((Element)root.getElementsByTagName("DequeSize").item(0)).getAttribute("value"));
            
            // RetryTimes
            Configure.retryTimes = Integer.valueOf(((Element)root.getElementsByTagName("RetryTimes").item(0)).getAttribute("value"));
            
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
                    wc.setCharset(website.getElementsByTagName("charset").item(0).getFirstChild().getNodeValue());
                    wc.setRegex(Pattern.compile(String.format("(%s)", website.getElementsByTagName("regex").item(0).getFirstChild().getNodeValue())));
                    
                    Class<? extends IPageInfo> clazz = ConfigKit.loadClass(website.getElementsByTagName("handler").item(0).getFirstChild().getNodeValue());
                    
                    try {
                        wc.setHandler(clazz.newInstance());
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    
                    bean.setWebsites(name, wc);
                }
            }
            
            Configure.websiteNum = bean.getWebsites().size();
            
        }
        
        return bean;
    }
}
