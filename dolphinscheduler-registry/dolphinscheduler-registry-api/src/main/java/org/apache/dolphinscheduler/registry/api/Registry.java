/*
 * Licensed to Apache Software Foundation (ASF) under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Apache Software Foundation (ASF) licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.dolphinscheduler.registry.api;

import java.io.Closeable;
import java.time.Duration;
import java.util.Collection;

import lombok.NonNull;

/**
 * Registry
 */
public interface Registry extends Closeable {

    /**
     * Connect to the registry, will wait in the given timeout
     *
     * @param timeout max timeout, if timeout <= 0 will wait indefinitely.
     * @throws RegistryException cannot connect in the given timeout
     */
    void connectUntilTimeout(@NonNull Duration timeout) throws RegistryException;

    boolean subscribe(String path, SubscribeListener listener);

    /**
     * Remove the path from the subscribe list.
     */
    void unsubscribe(String path);

    /**
     * Add a connection listener to collection.
     */
    void addConnectionStateListener(ConnectionListener listener);

    /**
     * @return the value
     */
    String get(String key);

    /**
     *
     * @param key
     * @param value
     * @param deleteOnDisconnect if true, when the connection state is disconnected, the key will be deleted
     */
    void put(String key, String value, boolean deleteOnDisconnect);

    /**
     * This function will delete the keys whose prefix is {@param key}
     * @param key the prefix of deleted key
     * @throws if the key not exists, there is a registryException
     */
    void delete(String key);

    /**
     * @return {@code true} if key exists.
     * E.g: registry contains  the following keys:[/test/test1/test2,]
     * if the key: /test
     * Return: test1
     */
    Collection<String> children(String key);

    /**
     * @return if key exists,return true
     */
    boolean exists(String key);

    /**
     * Acquire the lock of the prefix {@param key}
     */
    boolean acquireLock(String key);

    /**
     * Release the lock of the prefix {@param key}
     */
    boolean releaseLock(String key);
}
