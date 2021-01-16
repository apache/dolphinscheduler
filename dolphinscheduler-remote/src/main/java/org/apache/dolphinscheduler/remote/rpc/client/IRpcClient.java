package org.apache.dolphinscheduler.remote.rpc.client;

/**
 * IRpcClient
 */
public interface IRpcClient {


    <T> T create(Class<T> clazz) throws Exception;

}
