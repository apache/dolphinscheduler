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

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.DestroyMode;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ACP SSH Session factory
 */
public class PooledSSHSessionFactory extends BaseKeyedPooledObjectFactory<SSHSessionHost, SSHSessionHolder> {

    private static final Logger logger = LoggerFactory.getLogger(PooledSSHSessionFactory.class);

    @Override
    public SSHSessionHolder create(SSHSessionHost sshSessionHost) throws Exception {
        SSHSessionHolder pooledObject = new SSHSessionHolder(sshSessionHost);
        pooledObject.connect();
        return pooledObject;
    }

    @Override
    public PooledObject<SSHSessionHolder> wrap(SSHSessionHolder sshSessionHolder) {
        return new DefaultPooledObject<>(sshSessionHolder);
    }

    @Override
    public void destroyObject(SSHSessionHost key, PooledObject<SSHSessionHolder> p,
                              DestroyMode destroyMode) throws Exception {
        logger.info("destroy session {}", p.getObject().toString());
        p.getObject().disconnect();
    }

    @Override
    public boolean validateObject(SSHSessionHost key, PooledObject<SSHSessionHolder> p) {
        if (p.getObject().isConnected()) {
            try {
                p.getObject().keepAlive();
                return true;
            } catch (Exception e) {
                logger.error("Cannot send alive msg to session of {}", p.getObject().toString(), e);
            }
        }
        return false;
    }
}
