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
package org.apache.dolphinscheduler.dao.mapper;


import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.dao.entity.CommandCount;
import org.apache.dolphinscheduler.dao.entity.ErrorCommand;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ErrorCommandMapperTest {

    @Autowired
    ErrorCommandMapper errorCommandMapper;

    @Autowired
    ProcessDefinitionMapper processDefinitionMapper;


    /**
     * insert
     * @return ErrorCommand
     */
    private ErrorCommand insertOne(){
        //insertOne
        ErrorCommand errorCommand = new ErrorCommand();
        errorCommand.setId(10101);
        errorCommand.setCommandType(CommandType.START_PROCESS);
        errorCommand.setUpdateTime(new Date());
        errorCommand.setStartTime(new Date());
        errorCommandMapper.insert(errorCommand);
        return errorCommand;
    }

    /**
     * test update
     */
    @Test
    public void testUpdate(){
        //insertOne
        ErrorCommand errorCommand = insertOne();
        //update
        errorCommand.setUpdateTime(new Date());
        int update = errorCommandMapper.updateById(errorCommand);
        Assert.assertEquals(update, 1);
        errorCommandMapper.deleteById(errorCommand.getId());
    }

    /**
     * test delete
     */
    @Test
    public void testDelete(){

        ErrorCommand errorCommand = insertOne();
        int delete = errorCommandMapper.deleteById(errorCommand.getId());
        Assert.assertEquals(delete, 1);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        ErrorCommand errorCommand = insertOne();

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setName("def 1");
        processDefinition.setProjectId(1010);
        processDefinition.setUserId(101);
        processDefinition.setUpdateTime(new Date());
        processDefinition.setCreateTime(new Date());
        processDefinitionMapper.insert(processDefinition);

        errorCommand.setProcessDefinitionId(processDefinition.getId());
        errorCommandMapper.updateById(errorCommand);


        List<CommandCount> commandCounts = errorCommandMapper.countCommandState(
                null,
                 null,
                          new Integer[0]
        );

        Integer[] projectIdArray = new Integer[2];
        projectIdArray[0] = processDefinition.getProjectId();
        projectIdArray[1] = 200;
        List<CommandCount> commandCounts2 = errorCommandMapper.countCommandState(
                null,
                null,
                projectIdArray
        );

        errorCommandMapper.deleteById(errorCommand.getId());
        processDefinitionMapper.deleteById(processDefinition.getId());
        Assert.assertNotEquals(commandCounts.size(), 0);
        Assert.assertNotEquals(commandCounts2.size(), 0);
    }
}