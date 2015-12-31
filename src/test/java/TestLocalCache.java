import me.cache.local.LocalCache;
import me.cache.local.LocalCacheFactory;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertTrue;

/**
 * Created by OurEDA on 2015/12/31.
 */
public class TestLocalCache {

    static LocalCache localCache = LocalCacheFactory.createLruLocalCache(3);
    static {
        localCache.put("key1", 1, 1000);
        localCache.put("key2", 2, -1);
        localCache.put("key3", 3, -1);
    }

    @Test
    public void testSingleThread() throws InterruptedException {

        localCache.put("key4", 4, -1);

        assertTrue(localCache.size()==3);
        assertTrue(localCache.get("key1").equals(1));
        assertTrue(localCache.get("key2")==null);
        assertTrue(localCache.get("key3").equals(3));
        assertTrue(localCache.get("key4").equals(4));

        sleep(1002L);

        assertTrue(localCache.size()==3);
        assertTrue(localCache.get("key1").equals(1));
        assertTrue(localCache.size()==2);
        assertTrue(localCache.get("key1")==null);
    }

    @Test
    public void testConcurrent () throws InterruptedException {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(100, 100, 1000, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(10));
        executor.execute(new Worker(localCache, "1"));
        executor.execute(new Worker(localCache, "2"));
        executor.execute(new Worker(localCache, "3"));
        Thread.currentThread().join();
    }


    class Worker implements Runnable{

        final String name;
        LocalCache cache;
        Worker(LocalCache cache, String name) {
            this.cache = cache;
            this.name = name;
        }

        public void run() {
            while(true) {
                int randVal = RandomUtils.nextInt(0, 10);
                cache.put(randVal+"", randVal, 5000);
                randVal = RandomUtils.nextInt(0, 10);
                System.out.println("Thread "+name+": "+randVal+": "+cache.get(randVal+""));
                try {
                    sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
