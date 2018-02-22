package win.panyong.utils;

import com.easyond.utils.ObjectUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class RedisCache {
    public static final String OK = "OK";
    public static final String SUCCESS = "1";
    public static final Long SUCC = 1L;
    public static final String FAIL = "0";
    @Autowired
    private JedisPool jedisPool;

    private JedisPool getJedisPool() {
        return jedisPool;
    }

    private synchronized Jedis getJedis() {
        return getJedisPool().getResource();
    }

    //Redis 操作operate
    private <T> T op(Callable<T> callable) {
        Jedis jedis = null;
        T res = null;
        try {
            //外部接口
            res = callable.call(jedis = getJedis());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedisPool.returnResourceObject(jedis);
            return res;
        }
    }

    private boolean success(String sta) {
        return SUCCESS.equals(sta);
    }

    private boolean success(Long sta) {
        return SUCC.equals(sta);
    }

    private boolean ok(String sta) {
        return OK.equals(sta);
    }

    //======================================Key===========================================
    //判断key是否存在
    public boolean exists(final String key) {
        return op(jedis -> jedis.exists(key));
    }

    //删除声明的keys，返回成功删除key的数量，若为0，则key不存在
    public Long del(final String... keys) {
        return op(jedis -> jedis.del(keys));
    }

    //返回key对应的value类型：none string list set zset hash
    public String type(final String key) {
        return op(jedis -> jedis.type(key));
    }

    //为指定key设置过期时间
    public boolean expire(final String key, final int seconds) {
        return success(op((Callable<Long>) jedis -> jedis.expire(key, seconds)));
    }

    //return -1表示没有设置过期时间，-2表示key不存在，单位秒
    public Long ttl(final String key) {
        return op(jedis -> jedis.ttl(key));
    }

    //设置key对应的值为string类型的value,true -->操作成功
    public boolean set(final String key, final String value) {
        return success(op((Callable<String>) jedis -> jedis.set(key, value)));
    }


    //======================================String===========================================

    public boolean set(final byte[] key, final byte[] value) {
        return success(op((Callable<String>) jedis -> jedis.set(key, value)));
    }

    //SET if Not Exists
    public boolean setNx(final String key, final String value) {
        return success(op((Callable<Long>) jedis -> jedis.setnx(key, value)));
    }

    //milliseconds 过期时间，毫秒值，true -->成功
    public boolean set(final String key, final String value, final long milliseconds) {
        return ok(op(jedis -> jedis.psetex(key, milliseconds, value)));
    }

    public boolean set(final byte[] key, final byte[] value, final long milliseconds) {
        return ok(op(jedis -> jedis.psetex(key, milliseconds, value)));
    }

    //获取key对应的String，
    //若key对应的value不是string抛异常，若key不存在，return null
    public String get(final String key) {
        return op(jedis -> jedis.get(key));
    }

    public byte[] get(final byte[] key) {
        return op(jedis -> jedis.get(key));
    }

    /**
     * 设置key对应的value，并且返回oldValue
     *
     * @param key
     * @param value
     * @return
     */
    public String getSet(final String key, final String value) {
        return op(jedis -> jedis.getSet(key, value));
    }

    //======================================Object=========================================
    //存储对象
    public boolean set(final String key, final Object obj) {
        return set(key.getBytes(), ObjectUtil.serialize(obj));
    }

    //存储对象 ，过期时间毫秒单位
    public boolean set(String key, Object obj, long milliseconds) {
        return set(key.getBytes(), ObjectUtil.serialize(obj), milliseconds);
    }

    //获取对象
    public <T> T get(final String key, T t) {
        return ObjectUtil.unSerialize(get(key.getBytes()), t);
    }

    //存对象 key:{field1:value1, field2:value2, .....};value是字符串
    public boolean hmSet(final String key, final Map<String, String> map) {
        return ok(op(jedis -> jedis.hmset(key, map)));
    }
    //======================================Hash===========================================

    //返回与fields顺序相同的value列表，若field不存在，则对应的value是null
    public List<String> hmGet(final String key, final String... fields) {
        return op(jedis -> jedis.hmget(key, fields));
    }

    //设置 key的field对应的value。return 0是更新,1是创建。
    public Long hSet(final String key, final String field, final String value) {
        return op(jedis -> jedis.hset(key, field, value));
    }

    //获取key对应field的value,key 或者 field不存在，return null
    public String hGet(final String key, final String field) {
        return op(jedis -> jedis.hget(key, field));
    }

    //删除key对应fields，返回成功删除的个数
    public Long hDel(final String key, final String... fields) {
        return op(jedis -> jedis.hdel(key, fields));
    }

    //返回指定hash的fields数量，若key不存在，return 0
    public Long hLen(final String key) {
        return op(jedis -> jedis.hlen(key));
    }

    //返回对应key的所有fields
    public Set<String> hKeys(final String key) {
        return op(jedis -> jedis.hkeys(key));
    }

    //返回hash的所有filed和value
    public Map<String, String> hGetAll(final String key) {
        return op(jedis -> jedis.hgetAll(key));
    }

    //新增
    //key不存在则新建不报错，对应value type非set, 则return error。
    //return true表示操作成功
    public boolean sAdd(final String key, final String... members) {
        return success(op((Callable<Long>) jedis -> jedis.sadd(key, members)));
    }

    //======================================SET===========================================

    //新增
    public boolean sAdd(String key, List<String> members) {
        return sAdd(key, members.toArray(new String[members.size()]));
    }

    //删除
    //如果member在集合中不存在或者key不存在返回0，如果key对应的不是set类型的值返回错误
    //return true表示操作成功
    public boolean srem(final String key, final String... members) {
        return success(op((Callable<Long>) jedis -> jedis.srem(key, members)));
    }

    //成员存在性
    public boolean isMember(final String key, final String member) {
        return op(jedis -> jedis.sismember(key, member));
    }

    //返回key对应set的所有元素，结果是无序的
    public Set<String> sMembers(final String key) {
        return op(jedis -> jedis.smembers(key));
    }

    //使用Redis管道进行操作
    public void pipe(final PCallBack cb) {
        op((Callable<Void>) jedis -> {
            cb.call(jedis.pipelined());
            return null;
        });
    }


    interface Callable<T> {
        T call(Jedis jedis);
    }

    //======================================PipeLine===========================================
    public interface PCallBack {
        void call(Pipeline p);
    }
}
