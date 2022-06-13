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

package org.apache.dolphinscheduler.plugin.registry.mysql.task;

import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.plugin.registry.mysql.MysqlOperator;
import org.apache.dolphinscheduler.plugin.registry.mysql.MysqlRegistryConstant;
import org.apache.dolphinscheduler.plugin.registry.mysql.MysqlRegistryProperties;
import org.apache.dolphinscheduler.plugin.registry.mysql.model.MysqlRegistryLock;
import org.apache.dolphinscheduler.registry.api.RegistryException;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

public class RegistryLockManager implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(RegistryLockManager.class);

    private final MysqlOperator mysqlOperator;
    private final MysqlRegistryProperties registryProperties;
    private final Map<String, MysqlRegistryLock> lockHoldMap;
    private final ScheduledExecutorService lockTermUpdateThreadPool;

    public RegistryLockManager(MysqlRegistryProperties registryProperties, MysqlOperator mysqlOperator) {
        this.registryProperties = registryProperties;
        this.mysqlOperator = mysqlOperator;
        this.lockHoldMap = new ConcurrentHashMap<>();
        this.lockTermUpdateThreadPool = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder().setNameFormat("MysqlRegistryLockTermRefreshThread").setDaemon(true).build());
    }

    public void start() {
        lockTermUpdateThreadPool.scheduleWithFixedDelay(
                new LockTermRefreshTask(lockHoldMap, mysqlOperator),
                registryProperties.getTermRefreshInterval().toMillis(),
                registryProperties.getTermRefreshInterval().toMillis(),
                TimeUnit.MILLISECONDS);
    }

    /**
     * Acquire the lock, if cannot get the lock will await.
     */
    public void acquireLock(String lockKey) throws RegistryException {
        // maybe we can use the computeIf absent
        lockHoldMap.computeIfAbsent(lockKey, key -> {
            MysqlRegistryLock mysqlRegistryLock;
            try {
                while ((mysqlRegistryLock = mysqlOperator.tryToAcquireLock(lockKey)) == null) {
                    logger.debug("Acquire the lock {} failed try again", key);
                    // acquire failed, wait and try again
                    ThreadUtils.sleep(MysqlRegistryConstant.LOCK_ACQUIRE_INTERVAL);
                }
            } catch (SQLException e) {
                throw new RegistryException("Acquire the lock error", e);
            }
            return mysqlRegistryLock;
        });
    }

    public void releaseLock(String lockKey) {
        MysqlRegistryLock mysqlRegistryLock = lockHoldMap.get(lockKey);
        if (mysqlRegistryLock != null) {
            try {
                // the lock is unExit
                mysqlOperator.releaseLock(mysqlRegistryLock.getId());
                lockHoldMap.remove(lockKey);
            } catch (SQLException e) {
                throw new RegistryException(String.format("Release lock: %s error", lockKey), e);
            }
        }
    }

    @Override
    public void close() {
        lockTermUpdateThreadPool.shutdownNow();
        for (Map.Entry<String, MysqlRegistryLock> lockEntry : lockHoldMap.entrySet()) {
            releaseLock(lockEntry.getKey());
        }
    }

    /**
     * This task is used to refresh the lock held by the current server.
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    static class LockTermRefreshTask implements Runnable {
        private final Map<String, MysqlRegistryLock> lockHoldMap;
        private final MysqlOperator mysqlOperator;

        public void run() {
            try {
                if (lockHoldMap.isEmpty()) {
                    return;
                }
                List<Long> lockIds = lockHoldMap.values()
                        .stream()
                        .map(MysqlRegistryLock::getId)
                        .collect(Collectors.toList());
                if (!mysqlOperator.updateLockTerm(lockIds)) {
                    logger.warn("Update the lock: {} term failed.", lockIds);
                }
                mysqlOperator.clearExpireLock();
            } catch (Exception e) {
                logger.error("Update lock term error", e);
            }
        }
    }
}

