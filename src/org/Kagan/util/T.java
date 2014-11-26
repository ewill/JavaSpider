package org.Kagan.util;

public final class T {
    private T() {}
    
    public static final <TMsg> void print(TMsg msg) {
        System.out.print(msg);
    }
    
    public static final <TMsg> void println(TMsg msg) {
        System.out.println(msg);
    }
}
