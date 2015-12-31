package me.cache.local;

import javafx.beans.binding.Bindings;
import me.cache.local.exceptions.CacheFullException;
import me.cache.local.exceptions.CacheNotFoundException;

/**
 * Created by OurEDA on 2015/12/31.
 */
public interface LocalCache {

    long size();

    void put(String key, Object val) throws CacheFullException;

    void put(String key, Object val, long expire) throws CacheFullException;

    Object get(String key) throws CacheNotFoundException;
}
