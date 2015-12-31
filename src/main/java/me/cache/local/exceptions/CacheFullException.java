package me.cache.local.exceptions;

/**
 * Created by OurEDA on 2015/12/31.
 */
public class CacheFullException extends Exception{

    public static final CacheFullException instance = new CacheFullException();

    public CacheFullException() {
        super();
    }

    public CacheFullException(String msg) {
        super(msg);
    }

    public CacheFullException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public CacheFullException(Throwable cause) {
        super(cause);
    }
}
