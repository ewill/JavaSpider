package org.javaspider.kit;

public final class PathKit {
    private PathKit() {}
    
    private static final String ROOT_PATH = ConfigKit.class.getClassLoader().getResource(".").getPath().replaceAll("bin/", "");
    
    public static final String getRootPath() {
        return ROOT_PATH;
    }
}
