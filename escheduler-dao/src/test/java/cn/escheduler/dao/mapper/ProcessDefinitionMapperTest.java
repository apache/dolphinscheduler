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

import cn.escheduler.common.enums.Flag;
import cn.escheduler.dao.datasource.ConnectionFactory;
import cn.escheduler.dao.model.ProcessDefinition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

public class ProcessDefinitionMapperTest {


    ProcessDefinitionMapper processDefinitionMapper;


    @Before
    public void before(){
        processDefinitionMapper = ConnectionFactory.getSqlSession().getMapper(ProcessDefinitionMapper.class);
    }
    @Test
    public void testMapper() {
        ProcessDefinition processDefinition = new ProcessDefinition();

        processDefinition.setProcessDefinitionJson("json field");

        processDefinition.setName("test");
        processDefinition.setConnects("[]");
        processDefinition.setLocations("[]");
        processDefinition.setFlag(Flag.YES);
        processDefinition.setDesc("test");
        processDefinition.setProjectId(1024);
        processDefinition.setUpdateTime(new Date());
        processDefinition.setCreateTime(new Date());

        processDefinitionMapper.insert(processDefinition);
        Assert.assertNotEquals(processDefinition.getId(), 0);
        processDefinition.setName("test definition");
        int update = processDefinitionMapper.update(processDefinition);
        Assert.assertEquals(update, 1);

        ProcessDefinition findProcess = null;
        List<ProcessDefinition> processDefinitionList = processDefinitionMapper.queryAllDefinitionList(1024);
        for(ProcessDefinition processDefinition1 : processDefinitionList){
            if(processDefinition1.getId() == processDefinition.getId()){
                findProcess = processDefinition1;
                break;
            }
        }
        Assert.assertNotEquals(findProcess, null);

        int delete = processDefinitionMapper.delete(processDefinition.getId());
        Assert.assertEquals(delete, 1);


    }

}