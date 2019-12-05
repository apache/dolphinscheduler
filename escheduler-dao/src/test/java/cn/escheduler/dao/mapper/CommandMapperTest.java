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

import cn.escheduler.common.enums.CommandType;
import cn.escheduler.common.enums.FailureStrategy;
import cn.escheduler.common.enums.TaskDependType;
import cn.escheduler.common.enums.WarningType;
import cn.escheduler.dao.datasource.ConnectionFactory;
import cn.escheduler.dao.model.Command;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * command test
 */
public class CommandMapperTest {

    CommandMapper commandMapper;


    @Before
    public void before(){
        commandMapper = ConnectionFactory.getSqlSession().getMapper(CommandMapper.class);
    }

    @Test
    public void testMapper(){

        Command command = new Command();
        command.setCommandType(CommandType.START_PROCESS);
        command.setProcessDefinitionId(1);
        command.setExecutorId(10);
        command.setFailureStrategy(FailureStrategy.CONTINUE);
        command.setWarningType(WarningType.NONE);
        command.setWarningGroupId(1);
        command.setTaskDependType(TaskDependType.TASK_POST);
        commandMapper.insert(command);
        Assert.assertNotEquals(command.getId(), 0);

        command.setCommandParam("command parameter test");
        int update = commandMapper.update(command);
        Assert.assertEquals(update, 1);

        int delete = commandMapper.delete(command.getId());
        Assert.assertEquals(delete, 1);
    }

    @Test
    public void testQuery(){
        List<Command> commandList = commandMapper.queryAllCommand();
        Assert.assertNotEquals(commandList, null);
    }


}
