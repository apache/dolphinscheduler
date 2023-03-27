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

package org.apache.dolphinscheduler.plugin.task.flink;

import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FlinkParametersTest {

    @Test
    public void getResourceFilesList() {
        FlinkStreamParameters flinkParameters = new FlinkStreamParameters();
        Assertions.assertTrue(flinkParameters.getResourceFilesList().isEmpty());

        ResourceInfo mainResource = new ResourceInfo();
        mainResource.setResourceName("/testFlinkMain-1.0.0-SNAPSHOT.jar");
        flinkParameters.setMainJar(mainResource);

        List<ResourceInfo> resourceInfos = new LinkedList<>();
        ResourceInfo resourceInfo1 = new ResourceInfo();
        resourceInfo1.setResourceName("/testFlinkParameters1.jar");
        resourceInfos.add(resourceInfo1);

        flinkParameters.setResourceList(resourceInfos);
        List<ResourceInfo> resourceFilesList = flinkParameters.getResourceFilesList();
        Assertions.assertNotNull(resourceFilesList);
        Assertions.assertEquals(2, resourceFilesList.size());

        ResourceInfo resourceInfo2 = new ResourceInfo();
        resourceInfo2.setResourceName("/testFlinkParameters2.jar");
        resourceInfos.add(resourceInfo2);

        flinkParameters.setResourceList(resourceInfos);
        resourceFilesList = flinkParameters.getResourceFilesList();
        Assertions.assertNotNull(resourceFilesList);
        Assertions.assertEquals(3, resourceFilesList.size());
    }
}
