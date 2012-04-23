package rediswing;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Transaction;


/**
 * Redis类
 * @author liuyufei
 *
 */
public class Redis
{
	/**
	 * 连接池
	 */
	private static JedisPool  pool=null;
	/**
	 * 初始化
	 */
	public static void init(String server , int port , String psw)
	{
        if(pool!=null)
        {
                destory();
        }
         // 连接池配置 TODO 连接池配置
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxActive(100);
        config.setMaxIdle(50);
        config.setMaxWait(1000);
        config.setTestOnBorrow(true);
        // 初始化连接池
        pool = new JedisPool(config, server, port, 30, psw);
	}
	/**
	 * 销毁
	 */
	public static void destory()
	{
        pool.destroy();
	}
	/**
	 * 添加KEY 如果key已经存在则返回false TODO 加锁操作 考虑是否集群
	 */
	public static boolean setIfNotExists(String key,String value,int expire)
	{
        Jedis jedis=pool.getResource();
        boolean result = false;
        try
        {
            result = (jedis.setnx(key, value)==1)?true:false;
            if(result && expire>0)
            {
                    jedis.expire(key, expire);
            }
        } catch (Exception e) {
            throw new CoreException(CoreException.SYSTEM_REDIS_ERROR);
        }
        finally{
            pool.returnResource(jedis);
        }
        return result;
    }
    /**
     * 获取KEY 不反序列化
     */
    public static String get(String key)
    {
        Jedis jedis=pool.getResource();
        String result = null;
        try
        {
            result =  jedis.get(key);
        } catch (Exception e) {
            throw new CoreException(CoreException.SYSTEM_REDIS_ERROR);
        }
        finally{
            pool.returnResource(jedis);
        }
        return result;
	}
	/**
	 * 删除KEY(s)
	 */
	public static void delete(String ... keys)
	{
		Jedis jedis=pool.getResource();
        try
        {
        	jedis.del(keys);
        } catch (Exception e) {
            throw new CoreException(CoreException.SYSTEM_REDIS_ERROR);
        }
        finally{
            pool.returnResource(jedis);
        }
	}
	/**
	 * 设置单个会过期的key
	 */
	public static void setExpireKey(String key,String value,int expire)
	{
		 Jedis jedis=pool.getResource();
        try
        {
            jedis.setex(key, expire, value);
        } catch (Exception e) {
            throw new CoreException(CoreException.SYSTEM_REDIS_ERROR);
        }
        finally{
            pool.returnResource(jedis);
        }
	}
	/**
	 * 自增
	 */
	public static Long increment(String key)
	{
		Jedis jedis=pool.getResource();
		Long no = null;
        try
        {
        	no = jedis.incr(key);
        } catch (Exception e) {
            throw new CoreException(CoreException.SYSTEM_REDIS_ERROR);
        }
        finally{
            pool.returnResource(jedis);
        }
        return no;
	}
	/**
	 * 设置HASH表的值 不反序列化
	 */
	public static void setHash(String key,String field,String value)
	{
		Jedis jedis=pool.getResource();
        try
        {
        	jedis.hset(key, field, value);
        } catch (Exception e) {
        	throw new CoreException(CoreException.SYSTEM_REDIS_ERROR);
        }
        finally{
            pool.returnResource(jedis);
        }
	}
	/**
	 * 获取HASH表的值 不反序列化
	 */
	public static String getHash(String key,String field)
	{
		Jedis jedis=pool.getResource();
		String result = null;
        try
        {
        	result =  jedis.hget(key, field);
        } catch (Exception e) {
        	throw new CoreException(CoreException.SYSTEM_REDIS_ERROR);
        }
        finally{
            pool.returnResource(jedis);
        }
        return result;
	}
	/**
	 * 存储单个对象
	 */
	public static void setObject(String key,Object value)
    {
        Jedis jedis=pool.getResource();
        try
        {
            jedis.set(key, serialize(value));
        } catch (Exception e) {
        	throw new CoreException(CoreException.SYSTEM_REDIS_ERROR);
        }
        finally{
            pool.returnResource(jedis);
        }

    }

    /**
	 * 获取单个对象
	 */
	public static <T> T getObject(String key,Class<T> clas)
    {
        Jedis jedis=pool.getResource();
        T obj=null;
        try
        {
        	obj = unserialize( jedis.get(key),clas);
        } catch (Exception e) {
        	throw new CoreException(CoreException.SYSTEM_REDIS_ERROR);
        }
        finally{
            pool.returnResource(jedis);
        }
        return obj;
    }
    
	/**
	 * 存储多个对象 事务
	 */
	public static void setObjectMap(HashMap<String,Object>  objectMap)
    {
		if(!objectMap.isEmpty())
		{
	        Jedis jedis=pool.getResource();
	        try
	        {
	        	String[] keySet = objectMap.keySet().toArray(new String[objectMap.size()]);
	        	jedis.watch(keySet);
	        	Transaction t = jedis.multi();
	        	LinkedList<String> keysValues = new LinkedList<String>();
	        	for(Entry<String, Object> entry:objectMap.entrySet())
	        	{
	        		keysValues.add(entry.getKey());
	        		keysValues.add( serialize(entry.getValue()) );
	        	}
	        	t.mset(keysValues.toArray( new String[keysValues.size()] ));
	        	t.exec();
	        } catch (Exception e) {
	        	throw new CoreException(CoreException.SYSTEM_REDIS_ERROR);
	        }
	        finally{
	            pool.returnResource(jedis);
	        }
		}
    }
    /**
	 * 获取服务器状态
	 */
	public static String getServerInfo()
    {
        Jedis jedis=pool.getResource();
        String info=null;
        try
        {
            info = jedis.info();
        } catch (Exception e) {
        	throw new CoreException(CoreException.SYSTEM_REDIS_ERROR);
        }
        finally{
            pool.returnResource(jedis);
        }
        return info;
    }
    /**
	 * 获取keys
	 */
	public static Set<String> keys(String pattern)
    {
        Jedis jedis=pool.getResource();
        Set<String> keys=null;
        try
        {
            keys = jedis.keys(pattern);
        } catch (Exception e) {
        	throw new CoreException(CoreException.SYSTEM_REDIS_ERROR);
        }
        finally{
            pool.returnResource(jedis);
        }
        return keys;
    }
     /**
	 * 获取key的类型
	 */
	public static String type(String key)
    {
        Jedis jedis=pool.getResource();
        String type=null;
        try
        {
            type = jedis.type(key);
        } catch (Exception e) {
        	throw new CoreException(CoreException.SYSTEM_REDIS_ERROR);
        }
        finally{
            pool.returnResource(jedis);
        }
        return type;
    }
	/**
	 * 序列化对象
	 */
	public static String serialize(Object obj)
    {
		return JSON.toJSONString(obj, SerializerFeature.WriteClassName);
    }

	/**
	 * 反序列化对象
	 */
    @SuppressWarnings("unchecked")
    public static <T> T unserialize (String objString,Class<T> clas)
    {
    	if(objString == null) return null;
    	return (T) JSON.parseObject(objString,clas);
    }
  
}