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

package org.apache.dolphinscheduler.plugin.registry.raft;

/**
 * Interface for managing locks in a raft registry client.
 */
public interface IRaftLockManager extends AutoCloseable {

    /**
     * Acquires a lock with the specified key.
     * This method blocks until the lock is acquired.
     *
     * @param lockKey the key for the lock
     * @return true if the lock was successfully acquired, false otherwise
     */
    boolean acquireLock(String lockKey);

    /**
     * Acquires a lock with the specified key, with a timeout.
     * This method blocks until the lock is acquired or the timeout is reached.
     *
     * @param lockKey the key for the lock
     * @param timeout the maximum time to wait for the lock in milliseconds
     * @return true if the lock was successfully acquired within the timeout, false otherwise
     */
    boolean acquireLock(String lockKey, long timeout);

    /**
     * Releases the lock with the specified key.
     *
     * @param lockKey the key for the lock
     * @return true if the lock was successfully released, false otherwise
     */
    boolean releaseLock(String lockKey);

    /**
     * Closes the lock manager and releases any resources held by it.
     *
     * @throws Exception if an error occurs while closing the lock manager
     */
    @Override
    void close() throws Exception;
}
