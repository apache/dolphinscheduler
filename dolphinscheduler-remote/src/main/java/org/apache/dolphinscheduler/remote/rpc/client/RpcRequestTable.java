package org.apache.dolphinscheduler.remote.rpc.client;

import java.util.concurrent.ConcurrentHashMap;

/**
 * RpcRequestTable
 */
public class RpcRequestTable {


    private static ConcurrentHashMap<String, RpcRequestCache> requestMap = new ConcurrentHashMap<>();

    public static void put(String requestId,RpcRequestCache rpcRequestCache){
        requestMap.put(requestId,rpcRequestCache);
    }

    public static RpcRequestCache get(String requestId){
        return requestMap.get(requestId);
    }

    public static void remove(String requestId){
        requestMap.remove(requestId);
    }

}
