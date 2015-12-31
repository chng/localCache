package me.cache.local;

/**
 * Created by OurEDA on 2015/12/31.
 */
public class LocalCacheFactory {
    public static LocalCache createLruLocalCache(int i) {
        return new LruLocalCache(i);
    }
}
