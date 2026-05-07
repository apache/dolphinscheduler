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

package org.apache.dolphinscheduler.registry.api;

/**
 * A handle to an acquired registry lock that releases the lock on {@link #close()},
 * intended for use with try-with-resources.
 * <p>
 * This handle is bound to the thread that acquired the lock — registry backends (e.g.
 * Zookeeper) keep per-thread state, so the handle must be closed on the same thread.
 * <p>
 * If {@link #close()} fails (the underlying release throws), the handle is left in
 * an un-closed state so the caller may retry releasing.
 */
public final class RegistryLock implements AutoCloseable {

    private final Registry registry;
    private final String lockKey;
    private boolean closed = false;

    RegistryLock(Registry registry, String lockKey) {
        this.registry = registry;
        this.lockKey = lockKey;
    }

    public String getLockKey() {
        return lockKey;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        registry.releaseLock(lockKey);
        closed = true;
    }
}
