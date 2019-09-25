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

import cn.escheduler.common.enums.*;
import cn.escheduler.dao.entity.Command;
import cn.escheduler.dao.entity.CommandCount;
import cn.escheduler.dao.entity.ProcessDefinition;
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

    @Test
    public void testDelete(){

        Command Command = insertOne();
        int delete = commandMapper.deleteById(Command.getId());
        Assert.assertEquals(delete, 1);
    }

    @Test
    public void testQuery() {
        Command command = insertOne();
        //query
        List<Command> commands = commandMapper.selectList(null);
        Assert.assertNotEquals(commands.size(), 0);
        commandMapper.deleteById(command.getId());
    }
    @Test
    public void testGetAll() {
        Command command = insertOne();
        List<Command> commands = commandMapper.selectList(null);
        Assert.assertNotEquals(commands.size(), 0);
        commandMapper.deleteById(command.getId());
    }

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

    @Test
    public void testCountCommandState() {
        Command command = insertOne();

        List<CommandCount> commandCounts = commandMapper.countCommandState(
                4, null, null, null
        );
        Assert.assertNotEquals(commandCounts.size(), 0);
        commandMapper.deleteById(command.getId());
    }
}