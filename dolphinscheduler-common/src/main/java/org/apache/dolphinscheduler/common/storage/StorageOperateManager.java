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
package org.apache.dolphinscheduler.common.storage;

import org.apache.dolphinscheduler.common.enums.ResUploadType;

import java.util.EnumMap;
import java.util.Objects;
import java.util.ServiceLoader;
/**
 * @author Storage Operate Manager
 */
public class StorageOperateManager {

    public static final EnumMap<ResUploadType, StorageOperate> OPERATE_MAP = new EnumMap<>(ResUploadType.class);

    static {
        ServiceLoader<StorageOperate> load = ServiceLoader.load(StorageOperate.class);
        for (StorageOperate storageOperate : load) {
            OPERATE_MAP.put(storageOperate.returnStorageType(), storageOperate);
        }
    }

    public static StorageOperate getStorageOperate(ResUploadType resUploadType) {
        if (Objects.isNull(resUploadType)){
            resUploadType = ResUploadType.HDFS;
        }
        StorageOperate storageOperate = OPERATE_MAP.get(resUploadType);
        if (Objects.isNull(storageOperate)){
            storageOperate = OPERATE_MAP.get(ResUploadType.HDFS);
        }
        return storageOperate;
    }
}
