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

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.plugin.registry.mysql.MysqlOperator;
import org.apache.dolphinscheduler.plugin.registry.mysql.MysqlRegistryConstant;
import org.apache.dolphinscheduler.plugin.registry.mysql.model.MysqlRegistryLock;
import org.apache.dolphinscheduler.registry.api.RegistryException;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class RegistryLockManager implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(RegistryLockManager.class);

    private final MysqlOperator mysqlOperator;
    private final Map<String, MysqlRegistryLock> lockHoldMap;
    private final ScheduledExecutorService lockTermUpdateThreadPool;

    public RegistryLockManager(MysqlOperator mysqlOperator) {
        this.mysqlOperator = mysqlOperator;
        mysqlOperator.clearExpireLock();
        this.lockHoldMap = new ConcurrentHashMap<>();
        this.lockTermUpdateThreadPool = Executors.newScheduledThreadPool(
                1,
                new ThreadFactoryBuilder().setNameFormat("MysqlRegistryLockTermRefreshThread").setDaemon(true).build());
    }

    public void start() {
        lockTermUpdateThreadPool.scheduleWithFixedDelay(
                new LockTermRefreshTask(lockHoldMap, mysqlOperator),
                MysqlRegistryConstant.TERM_REFRESH_INTERVAL,
                MysqlRegistryConstant.TERM_REFRESH_INTERVAL,
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
                    ThreadUtils.sleep(1_000L);
                }
            } catch (SQLException e) {
                // We clear the interrupt status
                throw new RegistryException("Acquire the lock error", e);
            }
            return mysqlRegistryLock;
        });
    }

    public void releaseLock(String lockKey) {
        MysqlRegistryLock mysqlRegistryLock = lockHoldMap.get(lockKey);
        if (mysqlRegistryLock != null) {
            // the lock is unExit
            try {
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
        lockHoldMap.clear();
    }

    /**
     * This task is used to refresh the lock held by the current server.
     */
    static class LockTermRefreshTask implements Runnable {
        private final Map<String, MysqlRegistryLock> lockHoldMap;
        private final MysqlOperator mysqlOperator;

        private LockTermRefreshTask(Map<String, MysqlRegistryLock> lockHoldMap, MysqlOperator mysqlOperator) {
            this.lockHoldMap = checkNotNull(lockHoldMap);
            this.mysqlOperator = checkNotNull(mysqlOperator);
        }

        public void run() {
            try {
                for (Map.Entry<String, MysqlRegistryLock> entry : lockHoldMap.entrySet()) {
                    // update the lock term
                    mysqlOperator.updateLockTerm(entry.getValue());
                }
                mysqlOperator.clearExpireLock();
            } catch (Exception e) {
                logger.debug("Update lock term error", e);
            }
        }
    }
}

