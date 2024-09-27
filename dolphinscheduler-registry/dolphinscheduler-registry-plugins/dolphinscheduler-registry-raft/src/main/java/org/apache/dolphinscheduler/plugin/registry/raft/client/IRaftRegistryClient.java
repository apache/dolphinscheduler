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

package org.apache.dolphinscheduler.plugin.registry.raft.client;

import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;

import java.util.Collection;

public interface IRaftRegistryClient extends AutoCloseable {

    /**
     * Start the raft registry client. Once started, the client will connect to the raft registry server and then it can be used.
     */
    void start();

    /**
     * Check the connectivity of the client.
     *
     * @return true if the client is connected, false otherwise
     */
    boolean isConnectivity();

    /**
     * Subscribe to the raft registry connection state change event.
     *
     * @param connectionStateListener the listener to handle connection state changes
     */
    void subscribeConnectionStateChange(ConnectionListener connectionStateListener);

    /**
     * Subscribe to the register data change event.
     *
     * @param path     the path to subscribe to
     * @param listener the listener to handle data changes
     */
    void subscribeRaftRegistryDataChange(String path, SubscribeListener listener);

    /**
     * Get the raft register data by key.
     *
     * @param key the key of the register data
     * @return the value associated with the key
     */
    String getRegistryDataByKey(String key);

    /**
     * Put the register data to the raft registry server.
     * <p>
     * If the key already exists, then update the value. If the key does not exist, then insert a new key-value pair.
     *
     * @param key                the key of the register data
     * @param value              the value to be associated with the key
     * @param deleteOnDisconnect if true, the data will be deleted when the client disconnects
     */
    void putRegistryData(String key, String value, boolean deleteOnDisconnect);

    /**
     * Delete the register data by key.
     *
     * @param key the key of the register data to be deleted
     */
    void deleteRegistryDataByKey(String key);

    /**
     * List all the children of the given key.
     * <p>
     * e.g. key = "/dolphinscheduler/master", and data exists in db as "/dolphinscheduler/master/master1", "/dolphinscheduler/master/master2"
     * <p>
     * then the return value will be ["master1", "master2"]
     *
     * @param key the key whose children are to be listed
     * @return a collection of children keys
     */
    Collection<String> getRegistryDataChildren(String key);

    /**
     * Check if the key exists in the raft registry server.
     *
     * @param key the key to check
     * @return true if the key exists, false otherwise
     */
    boolean existRaftRegistryDataKey(String key);

    /**
     * Acquire the raft registry lock by key. This is a blocking method. If you want to stop the blocking, you can interrupt the thread.
     *
     * @param lockKey the key of the lock to be acquired
     * @return true if the lock was successfully acquired, false otherwise
     */
    boolean acquireRaftRegistryLock(String lockKey);

    /**
     * Acquire the raft registry lock by key until timeout.
     *
     * @param lockKey the key of the lock to be acquired
     * @param timeout the maximum time to wait for the lock
     * @return true if the lock was successfully acquired, false otherwise
     */
    boolean acquireRaftRegistryLock(String lockKey, long timeout);

    /**
     * Release the raft registry lock by key. If the lockKey does not exist, this method will do nothing.
     *
     * @param lockKey the key of the lock to be released
     * @return true if the lock was successfully released, false otherwise
     */
    boolean releaseRaftRegistryLock(String lockKey);

    /**
     * Close the raft registry client. Once the client is closed, it cannot work anymore.
     */
    @Override
    void close();
}
