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

package org.apache.dolphinscheduler.plugin.datasource.zeppelin;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.zeppelin.client.ClientConfig;
import org.apache.zeppelin.client.ZeppelinClient;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class ZeppelinClientWrapper implements AutoCloseable {

    private ZeppelinClient zeppelinClient;

    public ZeppelinClientWrapper(String restEndpoint)
                                                      throws Exception {
        checkNotNull(restEndpoint);
        ClientConfig clientConfig = new ClientConfig(restEndpoint);
        zeppelinClient = new ZeppelinClient(clientConfig);
    }

    public boolean checkConnect(String username, String password) {
        try {
            // If the login fails, an exception will be thrown directly
            zeppelinClient.login(username, password);
            String version = zeppelinClient.getVersion();
            log.info("zeppelin client connects to server successfully, version is {}", version);
            return true;
        } catch (Exception e) {
            log.info("zeppelin client failed to connect to the server");
            return false;
        }
    }

    @Override
    public void close() throws Exception {

    }
}
