package org.apache.dolphinscheduler.remote.rpc.client;

import org.apache.dolphinscheduler.remote.rpc.future.RpcFuture;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jiangli
 * @date 2021-01-14 10:42
 */
public class RpcRequestTable {

    // key: requestId     value: RpcFuture
    private static ConcurrentHashMap<String, RpcFuture> processingRpc = new ConcurrentHashMap<>();

    public static void put(String requestId,RpcFuture rpcFuture){
        processingRpc.put(requestId,rpcFuture);
    }

    public static RpcFuture get(String requestId){
        return processingRpc.get(requestId);
    }

    public static void remove(String requestId){
        processingRpc.remove(requestId);
    }

}
