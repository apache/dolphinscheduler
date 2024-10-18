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

package org.apache.dolphinscheduler.plugin.datasource.adbspark;

import static com.google.common.base.Preconditions.checkNotNull;

import lombok.extern.slf4j.Slf4j;

import com.aliyun.adb20211201.Client;
import com.aliyun.teaopenapi.models.Config;

@Slf4j
public class AdbSparkClientWrapper implements AutoCloseable {

    private Client adbSparkClient;

    private static final String ADB_ENDPOINT_TEMPLATE = "adb.%s.aliyuncs.com";

    /**
     * Constructs an AdbSparkClientWrapper instance.
     * This constructor initializes the Aliyun ADB Spark client
     * by setting the access key ID, access key secret, and region ID.
     *
     * @param accessKeyId The Access Key ID provided by Alibaba Cloud.
     * @param accessKeySecret The Access Key Secret provided by Alibaba Cloud.
     * @param regionId The region ID of the Alibaba Cloud resource.
     * @throws Exception If the access key ID, access key secret, or region ID is null, an exception is thrown.
     */
    public AdbSparkClientWrapper(String accessKeyId, String accessKeySecret, String regionId) throws Exception {
        checkNotNull(accessKeyId, "accessKeyId is null");
        checkNotNull(accessKeySecret, "accessKeySecret is null");
        checkNotNull(regionId, "regionId is null");

        String endpoint = String.format(ADB_ENDPOINT_TEMPLATE, regionId);

        Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret)
                .setEndpoint(endpoint);

        adbSparkClient = new Client(config);
    }

    @Override
    public void close() throws Exception {
        // No need implement
    }

    public Client getAdbSparkClient() {
        return adbSparkClient;
    }
}
