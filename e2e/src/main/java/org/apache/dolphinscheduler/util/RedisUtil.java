/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dolphinscheduler.util;

import org.apache.dolphinscheduler.constant.TestConstant;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * redis util
 */
public class RedisUtil {
    /**
     * redis  ip
     */
    private static String redisIp;

    /**
     * redis port
     */
    private static int redisPort;

    /**
     * redis password
     */
    private static String redisPwd;

    /**
     * redis pool config
     */
    private static JedisPoolConfig jedisPoolConfig;

    /**
     * redis pool
     */
    private static JedisPool jedisPool;

    /**
     * jedis connection
     */
    private Jedis jedis;

    /**
     * jedis expire time
     */
    private int jedisExpireTime;

    /**
     * jedis max total
     */
    private static int jedisPoolMaxTotal;

    /**
     * jedis max idle
     */
    private static int jedisPoolMaxIdle;

    /**
     * jedis max wait time
     */
    private static int jedisPoolMaxWaitMillis;

    /**
     * Whether to perform a valid check when calling the borrowObject method
     */
    private static boolean jedisPoolTestOnBorrow;

    /**
     * Whether to perform a valid check when calling the returnObject method
     */
    private static boolean jedisPoolTestOnReturn;

    /**
     * storage local thread
     */
    public static ThreadLocal<Jedis> threadLocal = new ThreadLocal<>();

    /*
     * redis init
     */
    static {
        // redis properties
        redisIp = PropertiesReader.getKey("redis.ip");
        redisPort = Integer.valueOf(PropertiesReader.getKey("redis.port"));
        redisPwd = PropertiesReader.getKey("redis.pwd");
        //redis pool  properties
        jedisPoolMaxTotal = Integer.valueOf(PropertiesReader.getKey("jedis.pool.maxTotal"));
        jedisPoolMaxIdle = Integer.valueOf(PropertiesReader.getKey("jedis.pool.maxIdle"));
        jedisPoolMaxWaitMillis = Integer.valueOf(PropertiesReader.getKey("jedis.pool.maxWaitMillis"));
        jedisPoolTestOnBorrow = Boolean.valueOf(PropertiesReader.getKey("jedis.pool.testOnBorrow"));
        jedisPoolTestOnReturn = Boolean.valueOf(PropertiesReader.getKey("jedis.pool.testOnReturn"));
        // redis pool start properties
        jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(jedisPoolMaxTotal);
        jedisPoolConfig.setMaxIdle(jedisPoolMaxIdle);
        jedisPoolConfig.setMaxWaitMillis(jedisPoolMaxWaitMillis);
        jedisPoolConfig.setTestOnBorrow(jedisPoolTestOnBorrow);
        jedisPoolConfig.setTestOnReturn(jedisPoolTestOnReturn);
        // connect redis
        try {
            System.out.println("redis init");
            if (redisPwd.isEmpty())
                jedisPool = new JedisPool(jedisPoolConfig, redisIp, redisPort, TestConstant.THREE_THOUSAND);
            else {
                jedisPool = new JedisPool(jedisPoolConfig, redisIp, redisPort, TestConstant.TEN_THOUSAND, redisPwd);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("redis connect fail");
        }
    }

    /**
     * get redis pool
     *
     * @return redis pool
     */
    public static JedisPool getJedisPool() {
        return jedisPool;
    }

    /**
     * get jedis connection
     *
     * @return jedis connection
     */
    public Jedis getNewJedis() {
        Jedis newJedis = null;
        try {
            newJedis = jedisPool.getResource();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("redis connection fail");
        }
        System.out.println("redis connection success");
        return newJedis;
    }

    /**
     * get jedis connection
     *
     * @return jedis connection
     */
    public Jedis getJedis() {
        return jedis;
    }

    public void setJedisAndExpire(Jedis jedis) {
        this.jedis = jedis;
        threadLocal.set(jedis);
        // jedis expire time(s)
        jedisExpireTime = Integer.valueOf(PropertiesReader.getKey("jedis.expireTime"));
        System.out.println("redisUtil sets up a redis connection");
    }

    /**
     * set key
     *
     * @param key key
     * @param value value
     *
     */

    public void setKey(String key, String value) {
        jedis.set(key, value);
        // set expire time 1h
        jedis.expire(key, jedisExpireTime);
    }

    /**
     * get key
     *
     * @param key key
     * @return value
     */
    public String getKey(String key) {
        return jedis.get(key);
    }

    /**
     * Return jedis connection
     */
    public void returnJedis() {
        if (jedis != null) {
            jedis.close();
        }
        System.out.println("jedis has been returned");
    }
}
