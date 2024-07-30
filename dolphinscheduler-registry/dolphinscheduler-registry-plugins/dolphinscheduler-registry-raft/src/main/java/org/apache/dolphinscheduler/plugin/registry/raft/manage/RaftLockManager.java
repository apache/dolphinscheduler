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

package org.apache.dolphinscheduler.plugin.registry.raft.manage;

import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.plugin.registry.raft.RaftRegistryProperties;
import org.apache.dolphinscheduler.plugin.registry.raft.model.RaftLockEntry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.util.concurrent.DistributedLock;
import com.alipay.sofa.jraft.util.ExecutorServiceHelper;

public class RaftLockManager implements IRaftLockManager {

    private final Map<String, RaftLockEntry> distributedLockMap = new ConcurrentHashMap<>();
    private final RheaKVStore rheaKvStore;
    private final RaftRegistryProperties raftRegistryProperties;
    private static final ScheduledExecutorService WATCH_DOG = Executors.newSingleThreadScheduledExecutor();
    private static final String LOCK_OWNER_PREFIX = NetUtils.getHost() + "_" + OSUtils.getProcessID() + "_";

    public RaftLockManager(RheaKVStore rheaKVStore, RaftRegistryProperties raftRegistryProperties) {
        this.rheaKvStore = rheaKVStore;
        this.raftRegistryProperties = raftRegistryProperties;
    }

    @Override
    public boolean acquireLock(String lockKey) {
        final String lockOwner = getLockOwnerPrefix();
        if (isThreadReentrant(lockKey, lockOwner)) {
            return true;
        }

        final DistributedLock<byte[]> distributedLock = rheaKvStore.getDistributedLock(lockKey,
                raftRegistryProperties.getDistributedLockTimeout().toMillis(), TimeUnit.MILLISECONDS, WATCH_DOG);

        while (true) {
            if (distributedLock.tryLock()) {
                distributedLockMap.put(lockKey, RaftLockEntry.builder().distributedLock(distributedLock)
                        .lockOwner(lockOwner)
                        .build());
                return true;
            } else {
                // fail to acquire lock
                ThreadUtils.sleep(raftRegistryProperties.getDistributedLockRetryInterval().toMillis());
            }
        }
    }

    @Override
    public boolean acquireLock(String lockKey, long timeout) {
        final String lockOwner = getLockOwnerPrefix();
        if (isThreadReentrant(lockKey, lockOwner)) {
            return true;
        }
        final long endTime = System.currentTimeMillis() + timeout;
        final DistributedLock<byte[]> distributedLock = rheaKvStore.getDistributedLock(lockKey,
                raftRegistryProperties.getDistributedLockTimeout().toMillis(), TimeUnit.MILLISECONDS, WATCH_DOG);

        while (System.currentTimeMillis() < endTime) {
            if (distributedLock.tryLock()) {
                distributedLockMap.put(lockKey, RaftLockEntry.builder().distributedLock(distributedLock)
                        .lockOwner(lockOwner)
                        .build());
                return true;
            } else {
                // fail to acquire lock
                ThreadUtils.sleep(raftRegistryProperties.getDistributedLockRetryInterval().toMillis());
            }
        }

        return false;
    }

    private boolean isThreadReentrant(String lockKey, String lockOwner) {
        final RaftLockEntry lockEntry = distributedLockMap.get(lockKey);
        return lockEntry != null && lockOwner.equals(lockEntry.getLockOwner());
    }

    @Override
    public boolean releaseLock(String lockKey) {
        final String lockOwner = getLockOwnerPrefix();
        final RaftLockEntry lockEntry = distributedLockMap.get(lockKey);
        if (lockEntry == null || !lockOwner.equals(lockEntry.getLockOwner())) {
            return false;
        }

        final DistributedLock<byte[]> distributedLock = distributedLockMap.get(lockKey).getDistributedLock();
        if (distributedLock != null) {
            distributedLock.unlock();
        }
        distributedLockMap.remove(lockKey);
        return true;
    }

    public static String getLockOwnerPrefix() {
        return LOCK_OWNER_PREFIX + Thread.currentThread().getName();
    }

    @Override
    public void close() throws Exception {
        ExecutorServiceHelper.shutdownAndAwaitTermination(WATCH_DOG);
    }
}
