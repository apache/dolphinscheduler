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

package org.apache.dolphinscheduler.dao.upgrade;

import org.apache.dolphinscheduler.common.utils.FileUtils;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * dolphinshceduler manager test
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ FileUtils.class })
public class DolphinSchedulerManagerTest {

    @Test
    public void testUpgradeDolphinScheduler() throws Exception {
        String rootDir = System.getProperty("user.dir") + "/../";
        PowerMockito.mockStatic(FileUtils.class);
        File[] files = new File[4];

        files[0] = new File(rootDir + "sql/upgrade/1.0.1_schema");
        files[1] = new File(rootDir + "sql/upgrade/1.0.2_schema");
        files[2] = new File(rootDir + "sql/upgrade/1.1.0_schema");
        files[3] = new File(rootDir + "sql/upgrade/1.2.0_schema");
        files[4] = new File(rootDir + "sql/upgrade/1.3.0_schema");
        files[5] = new File(rootDir + "sql/upgrade/1.3.2_schema");
        files[6] = new File(rootDir + "sql/upgrade/1.3.4_schema");
        PowerMockito.when(FileUtils.getAllDir("sql/upgrade")).thenReturn(files);

        DolphinSchedulerManager dolphinSchedulerManager = new DolphinSchedulerManager();
        dolphinSchedulerManager.upgradeDolphinScheduler();
        Assert.assertNotNull(dolphinSchedulerManager.upgradeDao);
    }
}
