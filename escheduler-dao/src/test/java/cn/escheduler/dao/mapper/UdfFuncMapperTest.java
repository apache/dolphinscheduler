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
package cn.escheduler.dao.mapper;

import cn.escheduler.dao.datasource.ConnectionFactory;
import cn.escheduler.dao.model.Resource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class UdfFuncMapperTest {

    private final Logger logger = LoggerFactory.getLogger(UdfFuncMapperTest.class);

    ResourceMapper resourceMapper;
    UdfFuncMapper udfFuncMapper;

    @Before
    public void before(){

        resourceMapper = ConnectionFactory.getSqlSession().getMapper(ResourceMapper.class);
        udfFuncMapper = ConnectionFactory.getSqlSession().getMapper(UdfFuncMapper.class);
    }

    @Test
    public void testMapper(){
        Resource resource  = new Resource();
        resource.setAlias("aa");
        resource.setFileName("aa");
        resource.setDesc("aa");
        resource.setUserId(2);
        resource.setCreateTime(new Date());
        resource.setUpdateTime(new Date());
        resourceMapper.insert(resource);
        Assert.assertNotEquals(resource.getId(), 0);


        resource.setAlias("aa123");
        resourceMapper.update(resource);
        resource = resourceMapper.queryResourceById(resource.getId());
        Assert.assertEquals(resource.getAlias(), "aa123");


        int delete = resourceMapper.delete(resource.getId());
        Assert.assertEquals(delete, 1);
    }
}