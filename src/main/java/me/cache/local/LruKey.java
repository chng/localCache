package me.cache.local;

/**
 * Created by OurEDA on 2015/12/31.
 */
public class LruKey {

    private String key;
    public String getKey() {
        return key;
    }

    private long expire = Expire.Never;
    public long getExpire() {
        return expire;
    }

    public final long gmtCreate = System.currentTimeMillis();

    public LruKey(String key) {
        this.key = key;
    }
    public LruKey(String key, long expire) {
        this.key = key;
        this.expire = expire;
    }

    public boolean isExpired() {
        return getExpire()!=Expire.Never && gmtCreate+getExpire()<System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof LruKey)) {
            return false;
        }
        String key = ((LruKey) obj).getKey();
        return getKey().equals(key);
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }
}
