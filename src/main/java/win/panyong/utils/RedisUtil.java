package win.panyong.utils;

import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisUtil {
    public static final String OK = "OK";
    public static final String SUCCESS = "1";
    public static final Long SUCC = 1L;
    public static final String FAIL = "0";
    /**
     * 防止重复就生成的分布式锁key
     */
    private static final String REDIS_WRITELOCK_KEY_PREFIX = "recreation:writelock:key:";
    private static Integer defaultDb;
    private static JedisPool jedisPool;

    public RedisUtil(Integer defaultDb, JedisPool jedisPool) {
        RedisUtil.defaultDb = defaultDb;
        RedisUtil.jedisPool = jedisPool;
    }

    private boolean success(String sta) {
        return SUCCESS.equals(sta);
    }

    private JedisPool getJedisPool() {
        return jedisPool;
    }

    private synchronized Jedis getJedis() {
        return getJedisPool().getResource();
    }

    private String getWriteLockKey(String key) {
        if (!StringUtil.invalid(key)) {
            return REDIS_WRITELOCK_KEY_PREFIX + key;
        } else {
            throw new AppException("key can not be null!");
        }

    }

    /**
     * 尝试获得写操作分布式锁
     *
     * @param key        根据记录数据计算出来的MD5值
     * @param lockExpire 过期时间
     * @return 是否获取到了操作锁
     */
    public boolean tryWriteLock(final String key, final int lockExpire) {
        final String lockKey = getWriteLockKey(key);
        Callable<Boolean> callable = jedis -> {
            jedis.select(0);
            try {
                Long count = jedis.setnx(lockKey, lockKey);
                if (count == 1) {
                    jedis.expire(lockKey, lockExpire);
                    return Boolean.TRUE;
                } else {
                    if (-1 == jedis.ttl(lockKey)) {
                        jedis.expire(lockKey, lockExpire);
                    }
                    return Boolean.FALSE;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Boolean.FALSE;
        };
        return op(callable);
    }

    /**
     * 释放锁
     *
     * @param key
     */
    public void unWriteLock(final String key) {
        final String lockKey = getWriteLockKey(key);
        Runnable runnable = jedis -> {
            jedis.select(0);
            try {
                jedis.del(lockKey);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        op(runnable);
    }

    public boolean canGetWriteLock(final String key) {
        return op(jedis -> {
            jedis.select(0);
            return !jedis.exists(getWriteLockKey(key));
        });
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void set(final String key, final String value) {
        setAndExpire(defaultDb, key, value, -1);
    }

    public void setAndExpire(final String key, final String value, final long seconds) {
        setAndExpire(defaultDb, key, value, seconds);
    }

    public void set(Integer db, final String key, final String value) {
        setAndExpire(db, key, value, -1L);
    }

    public void setAndExpire(Integer db, final String key, final String value, final long seconds) {
        op(jedis -> {
            jedis.select(db);
            jedis.set(key, value);
            jedis.expire(key, seconds);
        });
    }

    public void setByLock(final String key, final String value) {
        if (tryWriteLock(key, 5)) {
            set(key, value);
            unWriteLock(key);
        } else {
            System.out.println("没获取到操作锁，操作终止");
        }
    }

    public void setByLock(final String key, java.lang.Runnable runnable) {
        if (tryWriteLock(key, 5)) {
            runnable.run();
            unWriteLock(key);
        } else {
            System.out.println("没获取到操作锁，操作终止");
        }
    }

    //获取key对应的String，
    //若key对应的value不是string抛异常，若key不存在，return null
    public String get(final String key) {
        return get(defaultDb, key);
    }

    public String get(Integer db, final String key) {
        return op(jedis -> {
            jedis.select(db);
            return jedis.get(key);
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    interface Callable<T> {
        T call(Jedis jedis) throws Exception;
    }

    private <T> T op(Callable<T> callable) {
        T res = null;
        try (Jedis jedis = getJedis()) {
            res = callable.call(jedis);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    interface Runnable {
        void run(Jedis jedis) throws Exception;
    }

    private void op(Runnable runnable) {
        try (Jedis jedis = getJedis()) {
            runnable.run(jedis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
