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

import org.apache.dolphinscheduler.dao.entity.Resource;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * resource filter test
 */
public class ResourceFilterTest {
    private static Logger logger = LoggerFactory.getLogger(ResourceFilterTest.class);
    @Test
    public void filterTest(){
        List<Resource> allList = new ArrayList<>();

        Resource resource1 = new Resource(3,-1,"b","/b",true);
        Resource resource2 = new Resource(4,2,"a1.txt","/a/a1.txt",false);
        Resource resource3 = new Resource(5,3,"b1.txt","/b/b1.txt",false);
        Resource resource4 = new Resource(6,3,"b2.jar","/b/b2.jar",false);
        Resource resource5 = new Resource(7,-1,"b2","/b2",true);
        Resource resource6 = new Resource(8,-1,"b2","/b/b2",true);
        Resource resource7 = new Resource(9,8,"c2.jar","/b/b2/c2.jar",false);
        allList.add(resource1);
        allList.add(resource2);
        allList.add(resource3);
        allList.add(resource4);
        allList.add(resource5);
        allList.add(resource6);
        allList.add(resource7);
        Set<String> suffixSet = new HashSet<>();
        suffixSet.add(".jar");
        suffixSet.add(".txt");

        ResourceFilter resourceFilter = new ResourceFilter(suffixSet,allList);
        List<Resource> resourceList = resourceFilter.filter();
        Assert.assertNotNull(resourceList);
        resourceList.stream().forEach(t-> logger.info(t.toString()));
    }
}