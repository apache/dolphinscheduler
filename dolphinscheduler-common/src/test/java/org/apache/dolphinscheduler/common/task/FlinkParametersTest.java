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
package org.apache.dolphinscheduler.common.task;

import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.flink.FlinkParameters;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public class FlinkParametersTest {
    @Test
    public void getResourceFilesList() {
        FlinkParameters flinkParameters = new FlinkParameters();
        Assert.assertTrue(CollectionUtils.isEmpty(flinkParameters.getResourceFilesList()));

        ResourceInfo mainResource = new ResourceInfo();
        mainResource.setRes("testFlinkMain-1.0.0-SNAPSHOT.jar");
        flinkParameters.setMainJar(mainResource);

        List<ResourceInfo> resourceInfos = new LinkedList<>();
        ResourceInfo resourceInfo1 = new ResourceInfo();
        resourceInfo1.setRes("testFlinkParameters1.jar");
        resourceInfos.add(resourceInfo1);

        flinkParameters.setResourceList(resourceInfos);
        List<ResourceInfo> resourceFilesList = flinkParameters.getResourceFilesList();
        Assert.assertNotNull(resourceFilesList);
        Assert.assertEquals(2, resourceFilesList.size());

        ResourceInfo resourceInfo2 = new ResourceInfo();
        resourceInfo2.setRes("testFlinkParameters2.jar");
        resourceInfos.add(resourceInfo2);

        flinkParameters.setResourceList(resourceInfos);
        resourceFilesList = flinkParameters.getResourceFilesList();
        Assert.assertNotNull(resourceFilesList);
        Assert.assertEquals(3, resourceFilesList.size());
    }
}
