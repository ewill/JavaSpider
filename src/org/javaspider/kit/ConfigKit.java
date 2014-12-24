package org.javaspider.kit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class ConfigKit {
    
    private static final Log log = LogFactory.getLog(ConfigKit.class);
    
    private ConfigKit() {}
    
    public static final Properties loadProperty(String fullPath) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(new File(fullPath)));
        } catch (IOException e) {
            log.error(String.format("%s%s", "Load properties fail! path = ", fullPath), e);
        }
        return properties;
    }
    
    @SuppressWarnings("rawtypes")
    public static final Class loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            log.error(String.format("%s%s", "Load class fail! Class name = ", className), e);
        }
        return null;
    }
    
    @SuppressWarnings("rawtypes")
    public static final Object newInstance(Class clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            log.error("New class instance fail!", e);
        }
        return null;
    }
    
    @SuppressWarnings("rawtypes")
    public static final <T> Constructor newConstructor(Class<T> clazz, Class ...classTypes) {
        try {
            return clazz.getConstructor(classTypes);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static final <T> T newConstructorInstance(Constructor constructor, Object ...params) {
        try {
            return (T)constructor.newInstance(params);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

}
