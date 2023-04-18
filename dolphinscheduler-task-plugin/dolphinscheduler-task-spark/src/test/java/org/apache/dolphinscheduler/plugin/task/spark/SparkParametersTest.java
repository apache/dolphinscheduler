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

package org.apache.dolphinscheduler.plugin.task.spark;

import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SparkParametersTest {

    @Test
    public void getResourceFilesList() {
        SparkParameters sparkParameters = new SparkParameters();
        Assertions.assertTrue(sparkParameters.getResourceFilesList().isEmpty());

        ResourceInfo mainResource = new ResourceInfo();
        mainResource.setResourceName("testSparkMain-1.0.0-SNAPSHOT.jar\"");
        sparkParameters.setMainJar(mainResource);

        LinkedList<ResourceInfo> resourceInfos = new LinkedList<>();
        ResourceInfo resourceInfo1 = new ResourceInfo();
        resourceInfo1.setResourceName("testSparkParameters1.jar");
        resourceInfos.add(resourceInfo1);

        sparkParameters.setResourceList(resourceInfos);
        List<ResourceInfo> resourceFilesList = sparkParameters.getResourceFilesList();
        Assertions.assertNotNull(resourceFilesList);
        Assertions.assertEquals(2, resourceFilesList.size());

        ResourceInfo resourceInfo2 = new ResourceInfo();
        resourceInfo2.setResourceName("testSparkParameters2.jar");
        resourceInfos.add(resourceInfo2);

        sparkParameters.setResourceList(resourceInfos);
        resourceFilesList = sparkParameters.getResourceFilesList();
        Assertions.assertNotNull(resourceFilesList);
        Assertions.assertEquals(3, resourceFilesList.size());

    }
}
