package org.apache.dolphinscheduler.remote.rpc.client;

/**
 * @author jiangli
 * @date 2021-01-09 10:58
 */
public interface IRpcClient {


    <T> T create(Class<T> clazz) throws Exception;

}
