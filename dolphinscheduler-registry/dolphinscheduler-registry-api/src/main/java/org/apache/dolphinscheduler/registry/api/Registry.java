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
 * The SPI interface for registry center, each registry plugin should implement this interface.
 */
public interface Registry extends Closeable {

    /**
     * Start the registry, once started, the registry will connect to the registry center.
     */
    void start();

    /**
     * Whether the registry is connected
     *
     * @return true if connected, false otherwise.
     */
    boolean isConnected();

    /**
     * Connect to the registry, will wait in the given timeout
     *
     * @param timeout max timeout, if timeout <= 0 will wait indefinitely.
     * @throws RegistryException cannot connect in the given timeout
     */
    void connectUntilTimeout(@NonNull Duration timeout) throws RegistryException;

    /**
     * Subscribe the path, when the path has expose {@link Event}, the listener will be triggered.
     *
     * @param path     the path to subscribe
     * @param listener the listener to be triggered
     */
    void subscribe(String path, SubscribeListener listener);

    /**
     * Remove the path from the subscribe list.
     */
    void unsubscribe(String path);

    /**
     * Add a connection listener to collection.
     */
    void addConnectionStateListener(ConnectionListener listener);

    /**
     * Get the value of the key, if key not exist will throw {@link RegistryException}
     */
    String get(String key) throws RegistryException;

    /**
     * Put the key-value pair into the registry
     *
     * @param key                the key, cannot be null
     * @param value              the value, cannot be null
     * @param deleteOnDisconnect if true, when the connection state is disconnected, the key will be deleted
     */
    void put(String key, String value, boolean deleteOnDisconnect);

    /**
     * Delete the key from the registry
     */
    void delete(String key);

    /**
     * Return the children of the key
     */
    Collection<String> children(String key);

    /**
     * Check if the key exists
     *
     * @param key the key to check
     * @return true if the key exists
     */
    boolean exists(String key);

    /**
     * Acquire the lock of the prefix {@param key}
     */
    boolean acquireLock(String key);

    /**
     * Acquire the lock of the prefix {@param key}, if acquire in the given timeout return true, else return false.
     */
    boolean acquireLock(String key, long timeout);

    /**
     * Release the lock of the prefix {@param key}
     */
    boolean releaseLock(String key);
}
