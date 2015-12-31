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

    // ���޸�keys�����key���ڣ������޸�cache�������ͬ����
    public synchronized Object get(String key) {

        LruKey lruKey = new LruKey(key);

        Object val = cache.get(lruKey);
        if (val == null) {
            return null;
        }

        lruKey = findLruKey(lruKey);
        // ����ѹ��ڣ���key��cache��ɾ����
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
