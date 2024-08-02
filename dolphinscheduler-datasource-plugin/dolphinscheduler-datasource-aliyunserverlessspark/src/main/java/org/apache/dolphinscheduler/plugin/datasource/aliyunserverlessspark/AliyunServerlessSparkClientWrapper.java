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

package org.apache.dolphinscheduler.plugin.datasource.aliyunserverlessspark;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

import com.aliyun.emr_serverless_spark20230808.Client;
import com.aliyun.teaopenapi.models.Config;

@Slf4j
public class AliyunServerlessSparkClientWrapper implements AutoCloseable {

    private Client aliyunServerlessSparkClient;

    public AliyunServerlessSparkClientWrapper(
                                              String accessKeyId,
                                              String accessKeySecret,
                                              String regionId,
                                              String endpoint)
                                                               throws Exception {

        checkNotNull(accessKeyId, accessKeySecret, regionId);

        if (StringUtils.isEmpty(endpoint)) {
            endpoint = String.format(AliyunServerlessSparkConstants.ENDPOINT_TEMPLATE, regionId);
        }

        Config config = new Config()
                .setEndpoint(endpoint)
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret);
        aliyunServerlessSparkClient = new Client(config);
    }

    // TODO: update checkConnect when aliyun serverless spark service support the service connection check
    public boolean checkConnect(String accessKeyId, String accessKeySecret, String regionId) {
        try {
            // If the login fails, an exception will be thrown directly
            return true;
        } catch (Exception e) {
            log.info("spark client failed to connect to the server", e);
            return false;
        }
    }

    @Override
    public void close() throws Exception {

    }
}
