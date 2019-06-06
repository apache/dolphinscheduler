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

import cn.escheduler.dao.datasource.ConnectionFactory;
import cn.escheduler.dao.mapper.ResourceMapper;
import cn.escheduler.dao.model.Resource;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class ResourceViewDtoTest {
    private static final Logger logger = LoggerFactory.getLogger(ResourceViewDtoTest.class);
    ResourceMapper resourceMapper;

    @Before
    public void before(){

        resourceMapper = ConnectionFactory.getSqlSession().getMapper(ResourceMapper.class);
    }

    @Test
    public void resourceViewTest(){
        List<Resource> resources = resourceMapper.queryResourceCreatedByUser(2,0);
        List<Resource> allResourceList = resourceMapper.queryAllResourceListPaging(0, "", 0, 10);

        ResourceTreeBuilder treeBuilder = new ResourceTreeBuilder(allResourceList,resources);
        logger.info(treeBuilder.buildJSONTree());
    }

}