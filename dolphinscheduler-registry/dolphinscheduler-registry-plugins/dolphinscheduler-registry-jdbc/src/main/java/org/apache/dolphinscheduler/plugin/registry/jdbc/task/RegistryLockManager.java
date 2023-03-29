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

package org.apache.dolphinscheduler.plugin.registry.jdbc.task;

import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.plugin.registry.jdbc.JdbcOperator;
import org.apache.dolphinscheduler.plugin.registry.jdbc.JdbcRegistryConstant;
import org.apache.dolphinscheduler.plugin.registry.jdbc.JdbcRegistryProperties;
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.JdbcRegistryLock;
import org.apache.dolphinscheduler.registry.api.RegistryException;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

@Slf4j
public class RegistryLockManager implements AutoCloseable {

    private final JdbcOperator jdbcOperator;
    private final JdbcRegistryProperties registryProperties;
    private final Map<String, JdbcRegistryLock> lockHoldMap;
    private final ScheduledExecutorService lockTermUpdateThreadPool;

    public RegistryLockManager(JdbcRegistryProperties registryProperties, JdbcOperator jdbcOperator) {
        this.registryProperties = registryProperties;
        this.jdbcOperator = jdbcOperator;
        this.lockHoldMap = new ConcurrentHashMap<>();
        this.lockTermUpdateThreadPool = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder().setNameFormat("JdbcRegistryLockTermRefreshThread").setDaemon(true).build());
    }

    public void start() {
        lockTermUpdateThreadPool.scheduleWithFixedDelay(
                new LockTermRefreshTask(lockHoldMap, jdbcOperator),
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
            JdbcRegistryLock jdbcRegistryLock;
            try {
                while ((jdbcRegistryLock = jdbcOperator.tryToAcquireLock(lockKey)) == null) {
                    log.debug("Acquire the lock {} failed try again", key);
                    // acquire failed, wait and try again
                    ThreadUtils.sleep(JdbcRegistryConstant.LOCK_ACQUIRE_INTERVAL);
                }
            } catch (SQLException e) {
                throw new RegistryException("Acquire the lock error", e);
            }
            return jdbcRegistryLock;
        });
    }

    public void releaseLock(String lockKey) {
        JdbcRegistryLock jdbcRegistryLock = lockHoldMap.get(lockKey);
        if (jdbcRegistryLock != null) {
            try {
                // the lock is unExit
                jdbcOperator.releaseLock(jdbcRegistryLock.getId());
                lockHoldMap.remove(lockKey);
            } catch (SQLException e) {
                throw new RegistryException(String.format("Release lock: %s error", lockKey), e);
            }
        }
    }

    @Override
    public void close() {
        lockTermUpdateThreadPool.shutdownNow();
        for (Map.Entry<String, JdbcRegistryLock> lockEntry : lockHoldMap.entrySet()) {
            releaseLock(lockEntry.getKey());
        }
    }

    /**
     * This task is used to refresh the lock held by the current server.
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    static class LockTermRefreshTask implements Runnable {

        private final Map<String, JdbcRegistryLock> lockHoldMap;
        private final JdbcOperator jdbcOperator;

        public void run() {
            try {
                if (lockHoldMap.isEmpty()) {
                    return;
                }
                List<Long> lockIds = lockHoldMap.values()
                        .stream()
                        .map(JdbcRegistryLock::getId)
                        .collect(Collectors.toList());
                if (!jdbcOperator.updateLockTerm(lockIds)) {
                    log.warn("Update the lock: {} term failed.", lockIds);
                }
                jdbcOperator.clearExpireLock();
            } catch (Exception e) {
                log.error("Update lock term error", e);
            }
        }
    }
}
