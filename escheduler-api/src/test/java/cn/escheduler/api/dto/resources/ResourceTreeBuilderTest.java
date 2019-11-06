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
package cn.escheduler.api.dto.resources;

import cn.escheduler.common.enums.ResourceType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResourceTreeBuilderTest {
    private static final Logger logger = LoggerFactory.getLogger(ResourceTreeBuilderTest.class);

    @Test
    public void resourceTreeBuilderTest(){
        List<ResourceViewDto> nodes = new ArrayList<ResourceViewDto>();

        ResourceViewDto p1 = new ResourceViewDto(1,-1,"home", ResourceType.FILEDIR);

        ResourceViewDto p2 = new ResourceViewDto(2, -1,"home1", ResourceType.FILEDIR);

        ResourceViewDto p3 = new ResourceViewDto(5, 1,"quality", ResourceType.FILEDIR);

        ResourceViewDto p4 = new ResourceViewDto(6, 1,"home1", ResourceType.FILEDIR);

        ResourceViewDto p5 = new ResourceViewDto(3, -1,"1.txt", ResourceType.FILE);

        ResourceViewDto p6 = new ResourceViewDto(4, -1,"2.txt", ResourceType.FILE);

        ResourceViewDto p7 = new ResourceViewDto(7, 5,"/quality/quality01.txt", ResourceType.FILE);

        ResourceViewDto p8 = new ResourceViewDto(8, 6,"/quality/quality02.txt", ResourceType.FILE);

        ResourceViewDto p9 = new ResourceViewDto(9, 2,"/home1/01.txt", ResourceType.FILE);

        ResourceViewDto p10 = new ResourceViewDto(10, 2,"/home1/02.txt", ResourceType.FILE);



        nodes.add(p3);

        nodes.add(p4);

        nodes.add(p5);

        nodes.add(p6);

        nodes.add(p7);

        nodes.add(p8);
        nodes.add(p9);
        nodes.add(p10);
        nodes.add(p1);

        nodes.add(p2);

        ResourceTreeBuilder treeBuilder = new ResourceTreeBuilder(nodes);

        logger.info(treeBuilder.buildJSONTree());
    }

    @Test
    public void resourceTreeBuilderPermissionTest(){
        List<ResourceViewDto> nodes = new ArrayList<ResourceViewDto>();

        ResourceViewDto p1 = new ResourceViewDto(1,-1,"home", ResourceType.FILEDIR,1);

        ResourceViewDto p2 = new ResourceViewDto(2, -1,"home1", ResourceType.FILEDIR,1);

        ResourceViewDto p3 = new ResourceViewDto(5, 1,"quality", ResourceType.FILEDIR,1);

        ResourceViewDto p4 = new ResourceViewDto(6, 1,"home1", ResourceType.FILEDIR,1);

        ResourceViewDto p5 = new ResourceViewDto(3, -1,"1.txt", ResourceType.FILE,1);

        ResourceViewDto p6 = new ResourceViewDto(4, -1,"2.txt", ResourceType.FILE,1);

        ResourceViewDto p7 = new ResourceViewDto(7, 5,"/quality/quality01.txt", ResourceType.FILE,1);

        ResourceViewDto p8 = new ResourceViewDto(8, 6,"/quality/quality02.txt", ResourceType.FILE,1);

        ResourceViewDto p9 = new ResourceViewDto(9, 2,"/home1/01.txt", ResourceType.FILE,1);

        ResourceViewDto p10 = new ResourceViewDto(10, 2,"/home1/02.txt", ResourceType.FILE,1);

        nodes.add(p1);

        nodes.add(p2);

        nodes.add(p3);

        nodes.add(p4);

        nodes.add(p5);

        nodes.add(p6);

        nodes.add(p7);

        nodes.add(p8);
        nodes.add(p9);
        nodes.add(p10);


        ResourceTreeBuilder treeBuilder = new ResourceTreeBuilder(nodes);

        logger.info(treeBuilder.buildJSONTree());
    }

}