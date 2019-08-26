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
package cn.escheduler.server.master;

import cn.escheduler.common.enums.CommandType;
import cn.escheduler.common.enums.FailureStrategy;
import cn.escheduler.common.enums.TaskDependType;
import cn.escheduler.common.enums.WarningType;
import cn.escheduler.common.graph.DAG;
import cn.escheduler.common.model.TaskNode;
import cn.escheduler.common.model.TaskNodeRelation;
import cn.escheduler.common.process.ProcessDag;
import cn.escheduler.dao.datasource.ConnectionFactory;
import cn.escheduler.dao.mapper.CommandMapper;
import cn.escheduler.dao.mapper.ProcessDefinitionMapper;
import cn.escheduler.dao.model.Command;
import cn.escheduler.dao.model.ProcessDefinition;
import cn.escheduler.dao.utils.DagHelper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 *  master test
 */
@Ignore
public class MasterCommandTest {

    private final Logger logger = LoggerFactory.getLogger(MasterCommandTest.class);

    private CommandMapper commandMapper;

    private ProcessDefinitionMapper processDefinitionMapper;


    @Before
    public void before(){

        commandMapper = ConnectionFactory.getSqlSession().getMapper(CommandMapper.class);
        processDefinitionMapper = ConnectionFactory.getSqlSession().getMapper(ProcessDefinitionMapper.class);
    }




    @Test
    public void StartFromFailedCommand(){
        Command cmd = new Command();
        cmd.setCommandType(CommandType.START_FAILURE_TASK_PROCESS);
        cmd.setCommandParam("{\"ProcessInstanceId\":325}");
        cmd.setProcessDefinitionId(63);

        commandMapper.insert(cmd);

    }

    @Test
    public void RecoverSuspendCommand(){

        Command cmd = new Command();
        cmd.setProcessDefinitionId(44);
        cmd.setCommandParam("{\"ProcessInstanceId\":290}");
        cmd.setCommandType(CommandType.RECOVER_SUSPENDED_PROCESS);

        commandMapper.insert(cmd);
    }




    @Test
    public void startNewProcessCommand(){
        Command cmd = new Command();
        cmd.setCommandType(CommandType.START_PROCESS);
        cmd.setProcessDefinitionId(167);
        cmd.setFailureStrategy(FailureStrategy.CONTINUE);
        cmd.setWarningType(WarningType.NONE);
        cmd.setWarningGroupId(4);
        cmd.setExecutorId(19);

        commandMapper.insert(cmd);
    }

    @Test
    public void ToleranceCommand(){
        Command cmd = new Command();
        cmd.setCommandType(CommandType.RECOVER_TOLERANCE_FAULT_PROCESS);
        cmd.setCommandParam("{\"ProcessInstanceId\":816}");
        cmd.setProcessDefinitionId(15);

        commandMapper.insert(cmd);
    }

    @Test
    public void insertCommand(){
        Command cmd = new Command();
        cmd.setCommandType(CommandType.START_PROCESS);
        cmd.setFailureStrategy(FailureStrategy.CONTINUE);
        cmd.setWarningType(WarningType.ALL);
        cmd.setProcessDefinitionId(72);
        cmd.setExecutorId(10);
        commandMapper.insert(cmd);
    }


    @Test
    public void testDagHelper(){

        ProcessDefinition processDefinition = processDefinitionMapper.queryByDefineId(19);

        try {
            ProcessDag processDag = DagHelper.generateFlowDag(processDefinition.getProcessDefinitionJson(),
                    new ArrayList<>(), new ArrayList<>(), TaskDependType.TASK_POST);

            DAG<String,TaskNode,TaskNodeRelation> dag = DagHelper.buildDagGraph(processDag);
            Collection<String> start = DagHelper.getStartVertex("1", dag, null);

            System.out.println(start.toString());

            Map<String, TaskNode> forbidden = DagHelper.getForbiddenTaskNodeMaps(processDefinition.getProcessDefinitionJson());
            System.out.println(forbidden);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }




}
