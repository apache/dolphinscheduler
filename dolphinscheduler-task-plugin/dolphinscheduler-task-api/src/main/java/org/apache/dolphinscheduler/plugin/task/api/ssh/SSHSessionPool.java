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

package org.apache.dolphinscheduler.plugin.task.api.ssh;

import org.apache.dolphinscheduler.plugin.task.api.model.SSHSessionHost;

import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ACP SSH Session Pool
 */
public final class SSHSessionPool {

    private static final Logger logger = LoggerFactory.getLogger(SSHSessionPool.class);

    private static volatile GenericKeyedObjectPool<SSHSessionHost, SSHSessionHolder> sessionPool = null;

    private static GenericKeyedObjectPoolConfig<SSHSessionHolder> poolConfig;

    private static AbandonedConfig abandonedConfig;

    private static SftpConfig sftpConfig;

    public static GenericKeyedObjectPool<SSHSessionHost, SSHSessionHolder> getSessionPool() {
        if (sessionPool == null) {
            synchronized (SSHSessionPool.class) {
                if (sessionPool == null) {
                    sessionPool =
                            new GenericKeyedObjectPool<>(new PooledSSHSessionFactory(), poolConfig, abandonedConfig);
                }
            }
        }
        return sessionPool;
    }

    public static SSHSessionHolder getSessionHolder(SSHSessionHost sessionHost) throws Exception {
        logger.info("try to borrow a session:{}", sessionHost.toString());
        return getSessionPool().borrowObject(sessionHost);
    }

    public static void returnSSHSessionHolder(SSHSessionHost sessionHost, SSHSessionHolder sessionHolder) {
        logger.info("return session:{}", sessionHost.toString());
        getSessionPool().returnObject(sessionHost, sessionHolder);
    }

    public static void printPoolStatus() {
        Map<String, Integer> activeKeyMap = sessionPool.getNumActivePerKey();
        for (Map.Entry<String, Integer> key : activeKeyMap.entrySet()) {
            String hostName = key.getKey();
            logger.info("Session Pool Stat: Key :{}, Active session count: {}, Total: {}", hostName, key.getValue(),
                    sessionPool.getMaxTotalPerKey());
        }
        logger.info("Session Pool Stat: Active session count: {}, Idle session : {}, Wait session: {} , Total: {}",
                sessionPool.getNumActive(), sessionPool.getNumIdle(), sessionPool.getNumWaiters(),
                sessionPool.getMaxTotal());
    }

    public static void setPoolConfig(GenericKeyedObjectPoolConfig<SSHSessionHolder> poolConfig) {
        SSHSessionPool.poolConfig = poolConfig;
    }

    public static void setAbandonedConfig(AbandonedConfig abandonedConfig) {
        SSHSessionPool.abandonedConfig = abandonedConfig;
    }

    public static SftpConfig getSftpConfig() {
        return sftpConfig;
    }

    public static void setSftpConfig(SftpConfig sftpConfig) {
        SSHSessionPool.sftpConfig = sftpConfig;
    }

}
