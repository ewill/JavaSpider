package org.Kagan.interfaces;

public interface IQueue<TKey, TData> {
    public TData front();
    public TData tail();
    public void add(TKey key, TData value);
}
