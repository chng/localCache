package me.cache.local;

import javafx.beans.binding.Bindings;

/**
 * Created by OurEDA on 2015/12/31.
 */
public interface LocalCache {

    void put(String key, Object val);

    void put(String key, Object val, long expire);

    long size();

    Object get(String key);
}
