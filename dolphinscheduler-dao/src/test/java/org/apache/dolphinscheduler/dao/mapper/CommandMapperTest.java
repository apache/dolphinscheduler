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

import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.CommandCount;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.common.enums.*;
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
public class CommandMapperTest {


    @Autowired
    CommandMapper commandMapper;

    @Autowired
    ProcessDefinitionMapper processDefinitionMapper;

    /**
     * insert
     * @return Command
     */
    private Command insertOne(){
        //insertOne
        Command command = new Command();
        command.setCommandType(CommandType.START_PROCESS);
        command.setProcessDefinitionId(1);
        command.setExecutorId(4);
        command.setProcessInstancePriority(Priority.MEDIUM);
        command.setFailureStrategy(FailureStrategy.CONTINUE);
        command.setWorkerGroupId(-1);
        command.setWarningGroupId(1);
        command.setUpdateTime(new Date());
        commandMapper.insert(command);
        return command;
    }

    /**
     * test update
     */
    @Test
    public void testUpdate(){
        //insertOne
        Command command = insertOne();
        //update
        command.setStartTime(new Date());
        int update = commandMapper.updateById(command);
        Assert.assertEquals(update, 1);
        commandMapper.deleteById(command.getId());
    }

    /**
     * test delete
     */
    @Test
    public void testDelete(){

        Command Command = insertOne();
        int delete = commandMapper.deleteById(Command.getId());
        Assert.assertEquals(delete, 1);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        Command command = insertOne();
        //query
        List<Command> commands = commandMapper.selectList(null);
        Assert.assertNotEquals(commands.size(), 0);
        commandMapper.deleteById(command.getId());
    }

    /**
     * test query all
     */
    @Test
    public void testGetAll() {
        Command command = insertOne();
        List<Command> commands = commandMapper.selectList(null);
        Assert.assertNotEquals(commands.size(), 0);
        commandMapper.deleteById(command.getId());
    }

    /**
     * test get on command to run
     */
    @Test
    public void testGetOneToRun() {
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setReleaseState(ReleaseState.ONLINE);
        processDefinition.setName("ut test");
        processDefinition.setProjectId(1);
        processDefinition.setFlag(Flag.YES);
        processDefinitionMapper.insert(processDefinition);

        Command command = new Command();
        command.setCommandType(CommandType.START_PROCESS);
        command.setProcessDefinitionId(processDefinition.getId());
        command.setExecutorId(4);
        command.setProcessInstancePriority(Priority.MEDIUM);
        command.setFailureStrategy(FailureStrategy.CONTINUE);
        command.setWorkerGroupId(-1);
        command.setWarningGroupId(1);
        command.setUpdateTime(new Date());
        commandMapper.insert(command);

        Command command2 = commandMapper.getOneToRun();
        Assert.assertNotEquals(command2, null);
        commandMapper.deleteById(command.getId());
        processDefinitionMapper.deleteById(processDefinition.getId());
    }

    /**
     * test count command state
     */
    @Test
    public void testCountCommandState() {
        Command command = insertOne();

        //insertOne
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setName("def 1");
        processDefinition.setProjectId(1010);
        processDefinition.setUserId(101);
        processDefinition.setUpdateTime(new Date());
        processDefinition.setCreateTime(new Date());
        processDefinitionMapper.insert(processDefinition);

        command.setProcessDefinitionId(processDefinition.getId());
        commandMapper.updateById(command);


        List<CommandCount> commandCounts = commandMapper.countCommandState(
                4, null, null, new Integer[0]
        );

        Integer[] projectIdArray = new Integer[2];
        projectIdArray[0] = processDefinition.getProjectId();
        projectIdArray[1] = 200;
        List<CommandCount> commandCounts2 = commandMapper.countCommandState(
                4, null, null, projectIdArray
        );

        commandMapper.deleteById(command.getId());
        processDefinitionMapper.deleteById(processDefinition.getId());
        Assert.assertNotEquals(commandCounts.size(), 0);
        Assert.assertNotEquals(commandCounts2.size(), 0);
    }
}