package me.cache.local;

import java.sql.Time;
import java.util.concurrent.TimeUnit;

/**
 * Created by OurEDA on 2015/12/31.
 */
public class Expire {
    public static final long NEVER = -1;
    public static final long SECOND = 1000;
    public static final long MINUTE = 60 * SECOND;
    public static final long HOUR = 60 * MINUTE;
    public static final long DAILY = 24 * HOUR;
    public static final long WEEK = 7 * DAILY;
    public static final long MONTH = 30 * DAILY;
}
