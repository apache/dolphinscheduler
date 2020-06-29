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
package org.apache.dolphinscheduler.service.zk;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * zookeeper conf
 */
@Component
@PropertySource("classpath:zookeeper.properties")
public class ZookeeperConfig {

    //zk connect config
    @Value("${zookeeper.quorum}")
    private String serverList;

    @Value("${zookeeper.retry.base.sleep:100}")
    private int baseSleepTimeMs;

    @Value("${zookeeper.retry.max.sleep:30000}")
    private int maxSleepMs;

    @Value("${zookeeper.retry.maxtime:10}")
    private int maxRetries;

    @Value("${zookeeper.session.timeout:60000}")
    private int sessionTimeoutMs;

    @Value("${zookeeper.connection.timeout:30000}")
    private int connectionTimeoutMs;

    @Value("${zookeeper.connection.digest: }")
    private String digest;

    @Value("${zookeeper.dolphinscheduler.root:/dolphinscheduler}")
    private String dsRoot;

    public String getServerList() {
        return serverList;
    }

    public void setServerList(String serverList) {
        this.serverList = serverList;
    }

    public int getBaseSleepTimeMs() {
        return baseSleepTimeMs;
    }

    public void setBaseSleepTimeMs(int baseSleepTimeMs) {
        this.baseSleepTimeMs = baseSleepTimeMs;
    }

    public int getMaxSleepMs() {
        return maxSleepMs;
    }

    public void setMaxSleepMs(int maxSleepMs) {
        this.maxSleepMs = maxSleepMs;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getSessionTimeoutMs() {
        return sessionTimeoutMs;
    }

    public void setSessionTimeoutMs(int sessionTimeoutMs) {
        this.sessionTimeoutMs = sessionTimeoutMs;
    }

    public int getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    public void setConnectionTimeoutMs(int connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getDsRoot() {
        return dsRoot;
    }

    public void setDsRoot(String dsRoot) {
        this.dsRoot = dsRoot;
    }
}