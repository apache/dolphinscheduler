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

package org.apache.dolphinscheduler.dao.repository.impl;

import org.apache.dolphinscheduler.dao.entity.SerialCommand;
import org.apache.dolphinscheduler.dao.mapper.SerialCommandMapper;
import org.apache.dolphinscheduler.dao.model.SerialCommandDto;
import org.apache.dolphinscheduler.dao.repository.BaseDao;
import org.apache.dolphinscheduler.dao.repository.SerialCommandDao;

import java.util.List;
import java.util.stream.Collectors;

import lombok.NonNull;

import org.springframework.stereotype.Repository;

@Repository
public class SerialCommandDaoImpl extends BaseDao<SerialCommand, SerialCommandMapper> implements SerialCommandDao {

    public SerialCommandDaoImpl(@NonNull SerialCommandMapper serialCommandMapper) {
        super(serialCommandMapper);
    }

    @Override
    public List<SerialCommandDto> fetchSerialCommands(int fetchSize) {
        return mybatisMapper.fetchSerialCommands(fetchSize).stream().map(SerialCommandDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public int deleteByWorkflowInstanceId(Integer workflowInstanceId) {
        return mybatisMapper.deleteByWorkflowInstanceId(workflowInstanceId);
    }
}
