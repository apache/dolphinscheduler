package org.apache.dolphinscheduler.remote.rpc.client;

import org.apache.dolphinscheduler.remote.utils.Host;

/**
 * IRpcClient
 */
public interface IRpcClient {


    <T> T create(Class<T> clazz, Host host) throws Exception;

}
