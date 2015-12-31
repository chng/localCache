package me.cache.local.exceptions;

/**
 * Created by OurEDA on 2015/12/31.
 */
public class CacheNotFoundException extends Exception {

    public static final CacheNotFoundException instance = new CacheNotFoundException();

    public CacheNotFoundException() {
        super();
    }

    public CacheNotFoundException(String msg) {
        super(msg);
    }

    public CacheNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public CacheNotFoundException(Throwable cause) {
        super(cause);
    }
}
