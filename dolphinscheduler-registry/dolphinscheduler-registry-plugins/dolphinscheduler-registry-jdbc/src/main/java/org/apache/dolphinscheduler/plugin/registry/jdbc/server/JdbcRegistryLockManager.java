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

package org.apache.dolphinscheduler.plugin.registry.jdbc.server;

import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.plugin.registry.jdbc.JdbcRegistryProperties;
import org.apache.dolphinscheduler.plugin.registry.jdbc.LockUtils;
import org.apache.dolphinscheduler.plugin.registry.jdbc.model.DTO.JdbcRegistryLockDTO;
import org.apache.dolphinscheduler.plugin.registry.jdbc.repository.JdbcRegistryLockRepository;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DuplicateKeyException;

@Slf4j
public class JdbcRegistryLockManager implements IJdbcRegistryLockManager {

    private final JdbcRegistryProperties jdbcRegistryProperties;
    private final JdbcRegistryLockRepository jdbcRegistryLockRepository;

    // lockKey -> LockEntry
    private final Map<String, LockEntry> jdbcRegistryLockHolderMap = new ConcurrentHashMap<>();

    private final Object lockHolderMonitor = new Object();

    public JdbcRegistryLockManager(JdbcRegistryProperties jdbcRegistryProperties,
                                   JdbcRegistryLockRepository jdbcRegistryLockRepository) {
        this.jdbcRegistryProperties = jdbcRegistryProperties;
        this.jdbcRegistryLockRepository = jdbcRegistryLockRepository;
    }

    @Override
    public void acquireJdbcRegistryLock(Long clientId, String lockKey) {
        String lockOwner = LockUtils.getLockOwner();
        while (true) {
            if (tryReenterLock(clientId, lockKey, lockOwner)) {
                return;
            }
            JdbcRegistryLockDTO jdbcRegistryLock = JdbcRegistryLockDTO.builder()
                    .lockKey(lockKey)
                    .clientId(clientId)
                    .lockOwner(lockOwner)
                    .createTime(new Date())
                    .build();
            try {
                synchronized (lockHolderMonitor) {
                    jdbcRegistryLockRepository.insert(jdbcRegistryLock);
                    jdbcRegistryLockHolderMap.put(lockKey, LockEntry.builder()
                            .lockKey(lockKey)
                            .lockOwner(lockOwner)
                            .jdbcRegistryLock(jdbcRegistryLock)
                            .build());
                }
                log.debug("{} acquire the lock {} success", lockOwner, lockKey);
                return;
            } catch (DuplicateKeyException duplicateKeyException) {
                // The lock is already exist, wait it release.
                log.debug("{} failed to acquire the lock {}, it is held by another owner", lockOwner, lockKey);
            }
            log.debug("{} acquire the lock {} failed try again", lockOwner, lockKey);
            // acquire failed, wait and try again
            if (!sleepBeforeRetry(jdbcRegistryProperties.getHeartbeatRefreshInterval().toMillis())) {
                throw new IllegalStateException("Interrupted while acquiring the lock: " + lockKey);
            }
        }
    }

    private boolean tryReenterLock(Long clientId, String lockKey, String lockAcquirer) {
        synchronized (lockHolderMonitor) {
            LockEntry lockEntry = jdbcRegistryLockHolderMap.get(lockKey);
            if (lockEntry != null && lockAcquirer.equals(lockEntry.getLockOwner())) {
                if (!clientId.equals(lockEntry.getJdbcRegistryLock().getClientId())) {
                    throw new UnsupportedOperationException(
                            "The client " + clientId + " is not the lock owner of the lock: " + lockKey);
                }
                lockEntry.lockCount.incrementAndGet();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean acquireJdbcRegistryLock(Long clientId, String lockKey, long timeout) {
        String lockOwner = LockUtils.getLockOwner();
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start <= timeout) {
            if (tryReenterLock(clientId, lockKey, lockOwner)) {
                return true;
            }
            JdbcRegistryLockDTO jdbcRegistryLock = JdbcRegistryLockDTO.builder()
                    .lockKey(lockKey)
                    .clientId(clientId)
                    .lockOwner(lockOwner)
                    .createTime(new Date())
                    .build();
            try {
                synchronized (lockHolderMonitor) {
                    jdbcRegistryLockRepository.insert(jdbcRegistryLock);
                    jdbcRegistryLockHolderMap.put(lockKey, LockEntry.builder()
                            .lockKey(lockKey)
                            .lockOwner(lockOwner)
                            .jdbcRegistryLock(jdbcRegistryLock)
                            .build());
                }
                log.debug("{} acquire the lock {} success", lockOwner, lockKey);
                return true;
            } catch (DuplicateKeyException duplicateKeyException) {
                // The lock is already exist, wait it release.
                log.debug("{} failed to acquire the lock {}, it is held by another owner", lockOwner, lockKey);
            }
            log.debug("{} acquire the lock {} failed try again", lockOwner, lockKey);
            // acquire failed, wait and try again
            long remaining = timeout - (System.currentTimeMillis() - start);
            if (remaining <= 0 || !sleepBeforeRetry(Math.min(
                    remaining, jdbcRegistryProperties.getHeartbeatRefreshInterval().toMillis()))) {
                return false;
            }
        }
        return false;
    }

    private boolean sleepBeforeRetry(long millis) {
        if (millis <= 0 || Thread.currentThread().isInterrupted()) {
            return false;
        }
        ThreadUtils.sleep(millis);
        return !Thread.currentThread().isInterrupted();
    }

    @Override
    public void releaseJdbcRegistryLock(Long clientId, String lockKey) {
        String lockOwner = LockUtils.getLockOwner();
        LockEntry lockEntry;
        synchronized (lockHolderMonitor) {
            lockEntry = jdbcRegistryLockHolderMap.get(lockKey);
            if (lockEntry == null || !lockOwner.equals(lockEntry.getLockOwner())) {
                return;
            }
            if (!clientId.equals(lockEntry.getJdbcRegistryLock().getClientId())) {
                throw new UnsupportedOperationException(
                        "The client " + clientId + " is not the lock owner of the lock: " + lockKey);
            }
            int newLockCount = lockEntry.lockCount.decrementAndGet();
            if (newLockCount > 0) {
                return;
            }
            if (newLockCount < 0) {
                lockEntry.lockCount.incrementAndGet();
                throw new IllegalMonitorStateException("Jdbc lock count has gone negative for lock: " + lockKey);
            }
            // Remove before deleting from the database so a replacement entry cannot be removed by this release.
            jdbcRegistryLockHolderMap.remove(lockKey, lockEntry);
        }
        jdbcRegistryLockRepository.deleteById(lockEntry.getJdbcRegistryLock().getId());
    }

    /**
     * Delete an inactive lock while serializing it with local acquire/release operations.
     */
    boolean deleteIfInactive(JdbcRegistryLockDTO jdbcRegistryLock) {
        synchronized (lockHolderMonitor) {
            boolean deleted = jdbcRegistryLockRepository.deleteByIdAndInactiveClient(
                    jdbcRegistryLock.getId(), jdbcRegistryLock.getClientId());
            if (!deleted) {
                return false;
            }
            LockEntry lockEntry = jdbcRegistryLockHolderMap.get(jdbcRegistryLock.getLockKey());
            if (lockEntry != null
                    && jdbcRegistryLock.getId().equals(lockEntry.getJdbcRegistryLock().getId())) {
                jdbcRegistryLockHolderMap.remove(jdbcRegistryLock.getLockKey(), lockEntry);
            }
            return true;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LockEntry {

        private String lockKey;
        private String lockOwner;
        final AtomicInteger lockCount = new AtomicInteger(1);
        private JdbcRegistryLockDTO jdbcRegistryLock;
    }
}
