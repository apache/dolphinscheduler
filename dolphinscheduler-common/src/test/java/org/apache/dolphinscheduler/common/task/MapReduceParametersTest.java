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
import org.apache.dolphinscheduler.common.task.mr.MapReduceParameters;

import org.apache.commons.collections.CollectionUtils;

import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class MapReduceParametersTest {

    @Test
    public void getResourceList() {
        MapReduceParameters mapReduceParameters = new MapReduceParameters();
        Assert.assertTrue(CollectionUtils.isEmpty(mapReduceParameters.getResourceFilesList()));

        ResourceInfo mainResource = new ResourceInfo();
        mainResource.setRes("testMapReduce-1.0.0-SNAPSHOT.jar");
        mapReduceParameters.setMainJar(mainResource);

        LinkedList<ResourceInfo> resourceInfos = new LinkedList<>();
        ResourceInfo resourceInfo1 = new ResourceInfo();
        resourceInfo1.setRes("testMapReduceParameters1.jar");
        resourceInfos.add(resourceInfo1);

        mapReduceParameters.setResourceList(resourceInfos);
        List<ResourceInfo> resourceFilesList = mapReduceParameters.getResourceFilesList();
        Assert.assertNotNull(resourceFilesList);
        Assert.assertEquals(2, resourceFilesList.size());

        ResourceInfo resourceInfo2 = new ResourceInfo();
        resourceInfo2.setRes("testMapReduceParameters2.jar");
        resourceInfos.add(resourceInfo2);

        mapReduceParameters.setResourceList(resourceInfos);
        resourceFilesList = mapReduceParameters.getResourceFilesList();
        Assert.assertNotNull(resourceFilesList);
        Assert.assertEquals(3, resourceFilesList.size());

    }
}
