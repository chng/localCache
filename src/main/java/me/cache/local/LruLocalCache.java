package me.cache.local;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.cache.local.exceptions.CacheFullException;
import me.cache.local.exceptions.CacheNotFoundException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by OurEDA on 2015/12/31.
 */
public class LruLocalCache implements LocalCache {

    Lock lock = new ReentrantLock(true);

    private final long capacity;
    private AtomicLong size = new AtomicLong(0L);

    HashMap<LruKey, Object> cache = Maps.newLinkedHashMap();//new HashMap<LruKey, Object>();
    LinkedList<LruKey> keys = Lists.newLinkedList(); //new LinkedList<LruKey>();

    private void moveToFirst(LruKey lruKey) {
        keys.remove(lruKey);
        keys.addFirst(lruKey);
    }

    public LruLocalCache(long i) {
        capacity = i;
    }

    public void put(String key, Object val) throws CacheFullException {
        put(key, val, Expire.NEVER);
    }
    public void put(String key, Object val, long expire) throws CacheFullException {

        LruKey lruKey = new LruKey(key, expire);

        lock.lock();

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
                if (it.getExpire() == Expire.NEVER || it.isExpired()) {
                    oldKey = it;
                    break;
                }
            }
            if (oldKey == null) {
                throw CacheFullException.instance;
            }
            cache.remove(oldKey);
            cache.put(lruKey, val);
            keys.remove(oldKey);
            keys.addFirst(lruKey);
        }

        lock.unlock();
    }

    public long size() {
        return size.get();
    }

    // 会修改keys，如果key过期，还会修改cache，因此是同步块
    public Object get(String key) throws CacheNotFoundException {

        LruKey lruKey = new LruKey(key);

        lock.lock();

        Object val = cache.get(lruKey);
        if (val == null) {
            throw CacheNotFoundException.instance;
        }

        lruKey = findLruKey(lruKey);
        // 如果已过期，从key和cache中删掉。第一次过期的key仍能返回value
        if (lruKey.isExpired()) {
            cache.remove(lruKey);
            keys.remove(lruKey);
            size.decrementAndGet();
        } else {
            moveToFirst(new LruKey(key));
        }
        lock.unlock();

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
