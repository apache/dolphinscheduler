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

package org.apache.dolphinscheduler.service.storage;

import static org.apache.dolphinscheduler.common.Constants.RESOURCE_STORAGE_TYPE;
import static org.apache.dolphinscheduler.common.Constants.STORAGE_HDFS;
import static org.apache.dolphinscheduler.common.Constants.STORAGE_OSS;
import static org.apache.dolphinscheduler.common.Constants.STORAGE_S3;

import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.service.storage.impl.HadoopUtils;
import org.apache.dolphinscheduler.service.storage.impl.OssOperator;
import org.apache.dolphinscheduler.service.storage.impl.S3Utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * choose the impl of storage by RESOURCE_STORAGE_TYPE
 */

@Component
@Configuration
public class StoreConfiguration {

    @Bean
    public StorageOperate storageOperate() {
        switch (PropertyUtils.getUpperCaseString(RESOURCE_STORAGE_TYPE)) {
            case STORAGE_OSS:
                OssOperator ossOperator = new OssOperator();
                // TODO: change to use ossOperator.init(ossConnection) after DS supports Configuration / Connection
                // Center
                ossOperator.init();
                return ossOperator;
            case STORAGE_S3:
                return S3Utils.getInstance();
            case STORAGE_HDFS:
                return HadoopUtils.getInstance();
            default:
                return null;
        }
    }

}
