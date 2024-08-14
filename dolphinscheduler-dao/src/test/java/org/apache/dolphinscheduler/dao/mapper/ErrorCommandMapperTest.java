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

import static com.google.common.truth.Truth.assertThat;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.CommandCount;
import org.apache.dolphinscheduler.dao.entity.ErrorCommand;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;

import java.util.Date;
import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

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
        // insertOne
        ErrorCommand errorCommand = new ErrorCommand();
        errorCommand.setId(10101);
        errorCommand.setCommandType(CommandType.START_PROCESS);
        errorCommand.setUpdateTime(new Date());
        errorCommand.setStartTime(new Date());
        errorCommandMapper.insert(errorCommand);
        return errorCommand;
    }

    @Test
    public void testQueryCommandPageByIds() {
        ErrorCommand expectedCommand = insertOne();
        Page<ErrorCommand> page = new Page<>(1, 10);
        IPage<ErrorCommand> commandIPage = errorCommandMapper.queryErrorCommandPageByIds(page,
                Lists.newArrayList(expectedCommand.getProcessDefinitionCode()));
        List<ErrorCommand> commandList = commandIPage.getRecords();
        assertThat(commandList).isNotEmpty();
        assertThat(commandIPage.getTotal()).isEqualTo(1);
        assertThat(commandList.get(0).getId()).isEqualTo(expectedCommand.getId());
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
                null,
                null,
                Lists.newArrayList(0L));

        List<CommandCount> commandCounts2 = errorCommandMapper.countCommandState(
                null,
                null,
                Lists.newArrayList(processDefinition.getProjectCode(), 200L));

        Assertions.assertEquals(0, commandCounts.size());
        Assertions.assertNotEquals(0, commandCounts2.size());
    }
}
