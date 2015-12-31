package me.cache.local;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by OurEDA on 2015/12/31.
 */
public class LruLocalCache implements LocalCache {

    private final long capacity;
    private AtomicLong size = new AtomicLong(0L);

    HashMap<LruKey, Object> cache = Maps.newLinkedHashMap();//new HashMap<LruKey, Object>();
    LinkedList<LruKey> keys = Lists.newLinkedList(); //new LinkedList<LruKey>();

    private synchronized void moveToFirst(LruKey lruKey) {
        keys.remove(lruKey);
        keys.addFirst(lruKey);
    }

    public LruLocalCache(long i) {
        capacity = i;
    }

    public synchronized void put(String key, Object val) {
        put(key, val, Expire.Never);
    }
    public synchronized void put(String key, Object val, long expire) {

        LruKey lruKey = new LruKey(key, expire);

        //如果还有空间，直接保存
        if (size() < capacity) {
            keys.add(lruKey);
            cache.put(lruKey, val);
            size.incrementAndGet();
            return;
        }
        // 如果空间不足
        if (cache.containsKey(lruKey)) {
            // 如果存在，将key从keys移到First
            moveToFirst(lruKey);
            cache.put(lruKey, val);
        } else {
            // 如果不存在，找到淘汰key
            // 第一个超时的或者永不超时的key作为被淘汰的key，
            // 在keys中移除淘汰key，替换成新的key，同时移除cache中淘汰key
            LruKey oldKey = null;
            for (LruKey it : keys) {
                if (it.getExpire() == Expire.Never || it.isExpired()) {
                    oldKey = it;
                    break;
                }
            }
            if (oldKey == null) {
                //failed
                return;
            }
            cache.remove(oldKey);
            cache.put(lruKey, val);
            keys.remove(oldKey);
            keys.addFirst(lruKey);
        }
    }

    public long size() {
        return size.get();
    }

    // 会修改keys，如果key过期，还会修改cache，因此是同步块
    public synchronized Object get(String key) {

        LruKey lruKey = new LruKey(key);

        Object val = cache.get(lruKey);
        if (val == null) {
            return null;
        }

        lruKey = findLruKey(lruKey);
        // 如果已过期，从key和cache中删掉，
        if (lruKey.isExpired()) {
            cache.remove(lruKey);
            keys.remove(lruKey);
            size.decrementAndGet();
        } else {
            moveToFirst(new LruKey(key));
        }
        return val;
    }

    private LruKey findLruKey(LruKey lruKey) {
        for(Object it: cache.keySet()) {
            LruKey lk = (LruKey) it;
            if(lk.equals(lruKey)) {
                lruKey = lk;
                break;
            }
        }
        return lruKey;
    }

}
