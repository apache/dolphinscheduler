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
import org.apache.dolphinscheduler.common.utils.HadoopUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.EnumMap;

/**
 * @author StorageOperateManagerTest
 */
@RunWith(MockitoJUnitRunner.class)
public class StorageOperateManagerTest {

    @Mock
    private HadoopUtils hadoopUtils;

    @Test
    public void testManager() {
        StorageOperateManager mock = Mockito.mock(StorageOperateManager.class);
        Assert.assertNotNull(mock);

        EnumMap<ResUploadType, StorageOperate> storageOperateMap = StorageOperateManager.OPERATE_MAP;
        storageOperateMap.put(ResUploadType.HDFS, hadoopUtils);

        StorageOperate storageOperate = StorageOperateManager.getStorageOperate(ResUploadType.HDFS);
        Assert.assertNotNull(storageOperate);
    }
}
