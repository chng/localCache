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

        //������пռ䣬ֱ�ӱ���
        if (size() < capacity) {
            keys.add(lruKey);
            cache.put(lruKey, val);
            size.incrementAndGet();
            return;
        }
        // ����ռ䲻��
        if (cache.containsKey(lruKey)) {
            // ������ڣ���key��keys�Ƶ�First
            moveToFirst(lruKey);
            cache.put(lruKey, val);
        } else {
            // ��������ڣ��ҵ���̭key
            // ��һ����ʱ�Ļ���������ʱ��key��Ϊ����̭��key��
            // ��keys���Ƴ���̭key���滻���µ�key��ͬʱ�Ƴ�cache����̭key
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

    // ���޸�keys�����key���ڣ������޸�cache�������ͬ����
    public Object get(String key) throws CacheNotFoundException {

        LruKey lruKey = new LruKey(key);

        lock.lock();

        Object val = cache.get(lruKey);
        if (val == null) {
            throw CacheNotFoundException.instance;
        }

        lruKey = findLruKey(lruKey);
        // ����ѹ��ڣ���key��cache��ɾ������һ�ι��ڵ�key���ܷ���value
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
