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

    private static GenericKeyedObjectPool<SSHSessionHost, SSHSessionHolder> sessionPool;

    private static SftpConfig sftpConfig;

    private SSHSessionPool() {
    }

    public static void init(GenericKeyedObjectPoolConfig<SSHSessionHolder> poolConfig,
                            AbandonedConfig abandonedConfig) {
        init(poolConfig, abandonedConfig, null);
    }

    public static void init(GenericKeyedObjectPoolConfig<SSHSessionHolder> poolConfig, AbandonedConfig abandonedConfig,
                            SftpConfig sftpConfig) {
        sessionPool = new GenericKeyedObjectPool<>(new PooledSSHSessionFactory(), poolConfig, abandonedConfig);
        SSHSessionPool.sftpConfig = sftpConfig;
    }

    public static SSHSessionHolder getSessionHolder(SSHSessionHost sessionHost) throws Exception {
        logger.info("try to borrow a session:{}", sessionHost);
        SSHSessionHolder sessionHolder = sessionPool.borrowObject(sessionHost);
        sessionHolder.setSftpConfig(sftpConfig);
        return sessionHolder;
    }

    public static void returnSSHSessionHolder(SSHSessionHost sessionHost, SSHSessionHolder sessionHolder) {
        logger.info("return session:{}", sessionHost);
        sessionPool.returnObject(sessionHost, sessionHolder);
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

}
