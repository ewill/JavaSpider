package org.kagan.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class StringKit {
    private StringKit() {}
    
    public static final boolean isEmpty(String str) {
        return str != null && str.length() > 0 ? false : true;
    }
    
    public static final String digest(String content, String type) {
        StringBuffer str = new StringBuffer();
        byte[] digest = null;
        MessageDigest alg = null;
        try {
            alg = MessageDigest.getInstance(type);
            alg.update(content.getBytes());
            digest = alg.digest();
            
            for (byte b : digest) {
                str.append(String.format("%02x", b));
            }
            
        } catch (NoSuchAlgorithmException e) {
            digest = null;
        }
        
        return str.toString();
    }
    
    public static final String md5(String str) {
        return digest(str, "md5");
    }
    
    public static final String sha1(String str) {
        return digest(str, "sha1");
    }
    
    public static final String joinMap(Map<String, Object> data, String mid, String right, String split) {
        List<String> result = new ArrayList<String>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            result.add(String.format("%s %s %s", entry.getKey(), mid, isEmpty(right) ? String.valueOf(entry.getValue()) : right));
        }
        return join(result, split);
    }
    
    public static final String join(String data, String split) {
        String[] buff = new String[data.length()];
        for (int i = 0; i < data.length(); i++) {
            buff[i] = String.valueOf(data.charAt(i));
        }
        return join(Arrays.asList(buff), split);
    }
    
    public static final <TData> String join(Collection<TData> data, String split) {
        StringBuilder sb = new StringBuilder();
        Iterator<TData> iter = data.iterator();
        while (iter.hasNext()) {
            if (sb.length() == 0) {
                sb.append(String.valueOf(iter.next()));
            } else {
                sb.append(split).append(String.valueOf(iter.next()));
            }
        }
        return sb.toString();
    }
    
    public static final String repeat(String str, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
}
