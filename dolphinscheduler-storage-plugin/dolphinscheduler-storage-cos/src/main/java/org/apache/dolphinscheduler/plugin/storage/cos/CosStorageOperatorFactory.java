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

package org.apache.dolphinscheduler.plugin.storage.cos;

import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperator;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperatorFactory;
import org.apache.dolphinscheduler.plugin.storage.api.StorageType;
import org.apache.dolphinscheduler.plugin.storage.api.constants.StorageConstants;

import java.util.Map;

import com.google.auto.service.AutoService;

@AutoService(StorageOperatorFactory.class)
public class CosStorageOperatorFactory implements StorageOperatorFactory {

    @Override
    public StorageOperator createStorageOperate() {
        final CosStorageProperties cosStorageProperties = getCosStorageProperties();
        return new CosStorageOperator(cosStorageProperties);
    }

    @Override
    public StorageType getStorageOperate() {
        return StorageType.COS;
    }

    private CosStorageProperties getCosStorageProperties() {

        Map<String, String> cosPropertiesMap =
                PropertyUtils.getByPrefix(CosStorageConstants.TENCENT_CLOUD_COS_PROPERTY_PREFIX);

        return CosStorageProperties.builder()
                .region(cosPropertiesMap.get(CosStorageConstants.TENCENT_CLOUD_COS_REGION))
                .accessKeyId(cosPropertiesMap.get(CosStorageConstants.TENCENT_CLOUD_ACCESS_KEY_ID))
                .accessKeySecret(cosPropertiesMap.get(CosStorageConstants.TENCENT_CLOUD_ACCESS_KEY_SECRET))
                .bucketName(cosPropertiesMap.get(CosStorageConstants.TENCENT_CLOUD_COS_BUCKET_NAME))
                .resourceUploadPath(
                        cosPropertiesMap.getOrDefault(StorageConstants.RESOURCE_UPLOAD_PATH,
                                CosStorageConstants.DEFAULT_COS_RESOURCE_UPLOAD_PATH))
                .build();
    }
}
