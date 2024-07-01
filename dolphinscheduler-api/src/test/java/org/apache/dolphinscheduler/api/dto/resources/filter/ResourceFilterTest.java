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
package org.apache.dolphinscheduler.api.dto.resources.filter;

import org.apache.dolphinscheduler.plugin.storage.api.StorageEntity;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * resource filter test
 */
public class ResourceFilterTest {

    private static Logger logger = LoggerFactory.getLogger(ResourceFilterTest.class);
    @Test
    public void filterTest() {
        List<StorageEntity> allList = new ArrayList<>();

        StorageEntity resource1 = new StorageEntity();
        resource1.setFullName("a1.txt");
        StorageEntity resource2 = new StorageEntity();
        resource2.setFullName("b1.txt");
        StorageEntity resource3 = new StorageEntity();
        resource3.setFullName("b2.jar");
        StorageEntity resource4 = new StorageEntity();
        resource4.setFullName("c2.jar");
        allList.add(resource1);
        allList.add(resource2);
        allList.add(resource3);
        allList.add(resource4);

        ResourceFilter resourceFilter = new ResourceFilter(".jar", allList);
        List<StorageEntity> resourceList = resourceFilter.filter();
        Assertions.assertNotNull(resourceList);
        resourceList.forEach(t -> logger.info(t.toString()));
    }
}
