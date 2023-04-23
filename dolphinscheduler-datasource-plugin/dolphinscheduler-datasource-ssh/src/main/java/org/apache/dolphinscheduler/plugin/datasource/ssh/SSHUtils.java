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

package org.apache.dolphinscheduler.plugin.datasource.ssh;

import org.apache.dolphinscheduler.plugin.datasource.ssh.param.SSHConnectionParam;

import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.config.keys.loader.KeyPairResourceLoader;
import org.apache.sshd.common.util.security.SecurityUtils;

import java.security.KeyPair;
import java.util.Collection;

public class SSHUtils {

    private SSHUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static ClientSession getSession(SshClient client, SSHConnectionParam connectionParam) throws Exception {
        ClientSession session;
        session = client.connect(connectionParam.getUser(), connectionParam.getHost(), connectionParam.getPort())
                .verify(5000).getSession();
        // add password identity
        String password = connectionParam.getPassword();
        if (StringUtils.isNotEmpty(password)) {
            session.addPasswordIdentity(password);
        }

        // add public key identity
        String publicKey = connectionParam.getPublicKey();
        if (StringUtils.isNotEmpty(publicKey)) {
            try {
                KeyPairResourceLoader loader = SecurityUtils.getKeyPairResourceParser();
                Collection<KeyPair> keyPairCollection = loader.loadKeyPairs(null, null, null, publicKey);
                for (KeyPair keyPair : keyPairCollection) {
                    session.addPublicKeyIdentity(keyPair);
                }
            } catch (Exception e) {
                throw new Exception("Failed to add public key identity", e);
            }
        }
        return session;
    }
}
