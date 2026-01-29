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

import org.apache.dolphinscheduler.plugin.datasource.aliyunserverlessspark.param.AliyunServerlessSparkConnectionParam;

import com.aliyun.emr_serverless_spark20230808.Client;
import com.aliyun.teaopenapi.models.Config;

public class AliyunServerlessSparkUtils {

    private AliyunServerlessSparkUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static Client getAliyunServerlessSparkClient(AliyunServerlessSparkConnectionParam connectionParam) throws Exception {
        String endpoint =
                String.format(AliyunServerlessSparkConstants.ENDPOINT_TEMPLATE, connectionParam.getRegionId());
        Config config = new Config()
                .setEndpoint(endpoint)
                .setAccessKeyId(connectionParam.getAccessKeyId())
                .setAccessKeySecret(connectionParam.getAccessKeySecret());
        return new Client(config);
    }

}
