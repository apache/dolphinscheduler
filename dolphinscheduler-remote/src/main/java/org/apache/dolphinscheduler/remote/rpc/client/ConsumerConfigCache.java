package org.apache.dolphinscheduler.remote.rpc.client;

import java.util.concurrent.ConcurrentHashMap;

/**
 * ConsumerConfigCache
 */
public class ConsumerConfigCache {

    private static ConcurrentHashMap<String, ConsumerConfig> consumerMap=new ConcurrentHashMap<>();

    public static ConsumerConfig getConfigByServersName(String serviceName){
        return consumerMap.get(serviceName);
    }

    public static void putConfig(String serviceName,ConsumerConfig consumerConfig){
        consumerMap.putIfAbsent(serviceName,consumerConfig);
    }
}
