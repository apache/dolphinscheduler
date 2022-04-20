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
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.CommandCount;
import org.apache.dolphinscheduler.dao.entity.ErrorCommand;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ErrorCommandMapperTest extends BaseDaoTest {

    @Autowired
    private ErrorCommandMapper errorCommandMapper;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    /**
     * insert
     * @return ErrorCommand
     */
    private ErrorCommand insertOne() {
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
     * test query
     */
    @Test
    public void testQuery() {
        ErrorCommand errorCommand = insertOne();

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setCode(1L);
        processDefinition.setName("def 1");
        processDefinition.setProjectCode(1010L);
        processDefinition.setUserId(101);
        processDefinition.setUpdateTime(new Date());
        processDefinition.setCreateTime(new Date());
        processDefinitionMapper.insert(processDefinition);

        errorCommand.setProcessDefinitionCode(processDefinition.getCode());
        errorCommandMapper.updateById(errorCommand);

        List<CommandCount> commandCounts = errorCommandMapper.countCommandState(
                0,
                null,
                null,
                new Long[0]
        );

        Long[] projectCodeArray = new Long[2];
        projectCodeArray[0] = processDefinition.getProjectCode();
        projectCodeArray[1] = 200L;
        List<CommandCount> commandCounts2 = errorCommandMapper.countCommandState(
                0,
                null,
                null,
                projectCodeArray
        );

        Assert.assertNotEquals(commandCounts.size(), 0);
        Assert.assertNotEquals(commandCounts2.size(), 0);
    }
}
