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
package org.apache.dolphinscheduler.api.dto.resources.visitor;

import org.apache.dolphinscheduler.api.dto.resources.ResourceComponent;
import org.apache.dolphinscheduler.plugin.storage.api.StorageEntity;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * resource tree visitor test
 */
public class ResourceTreeVisitorTest {

    @Test
    public void visit() {
        List<StorageEntity> resourceList = new ArrayList<>();

        StorageEntity resource1 = new StorageEntity();
        resource1.setFullName("/default/a");
        resource1.setPfullName("/default");

        StorageEntity resource2 = new StorageEntity();
        resource1.setFullName("/default/a/a1.txt");
        resource1.setPfullName("/default/a");

        resourceList.add(resource1);
        resourceList.add(resource2);

        ResourceTreeVisitor resourceTreeVisitor = new ResourceTreeVisitor(resourceList);
        ResourceComponent resourceComponent = resourceTreeVisitor.visit("/default");
        Assertions.assertNotNull(resourceComponent.getChildren());
    }

    @Test
    public void rootNode() {
        List<StorageEntity> resourceList = new ArrayList<>();

        StorageEntity resource1 = new StorageEntity();
        resource1.setFullName("/default/a");
        resource1.setPfullName("/default");

        StorageEntity resource2 = new StorageEntity();
        resource1.setFullName("/default/a/a1.txt");
        resource1.setPfullName("/default/a");

        resourceList.add(resource1);
        resourceList.add(resource2);

        ResourceTreeVisitor resourceTreeVisitor = new ResourceTreeVisitor(resourceList);
        Assertions.assertTrue(resourceTreeVisitor.rootNode(resource1, "/default"));
        Assertions.assertFalse(resourceTreeVisitor.rootNode(resource2, "/default"));
    }

}
