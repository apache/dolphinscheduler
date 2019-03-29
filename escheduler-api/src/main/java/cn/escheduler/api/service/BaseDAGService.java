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
package cn.escheduler.api.service;

import cn.escheduler.common.graph.DAG;
import cn.escheduler.common.model.TaskNode;
import cn.escheduler.common.model.TaskNodeRelation;
import cn.escheduler.common.process.ProcessDag;
import cn.escheduler.common.utils.CollectionUtils;
import cn.escheduler.common.utils.JSONUtils;
import cn.escheduler.dao.model.ProcessData;
import cn.escheduler.dao.model.ProcessInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * base DAG service
 */
public class BaseDAGService extends BaseService{


    /**
     * process instance to DAG
     *
     * @param processInstance
     * @return
     * @throws Exception
     */
    public static DAG<String, TaskNode, TaskNodeRelation> processInstance2DAG(ProcessInstance processInstance) throws Exception {

        String processDefinitionJson = processInstance.getProcessInstanceJson();

        ProcessData processData = JSONUtils.parseObject(processDefinitionJson, ProcessData.class);

        List<TaskNode> taskNodeList = processData.getTasks();

        List<TaskNodeRelation> taskNodeRelations = new ArrayList<>();

        //Traversing node information and building relationships
        for (TaskNode taskNode : taskNodeList) {
            String preTasks = taskNode.getPreTasks();
            List<String> preTasksList = JSONUtils.toList(preTasks, String.class);

            //if previous tasks not empty
            if (preTasksList != null) {
                for (String depNode : preTasksList) {
                    taskNodeRelations.add(new TaskNodeRelation(depNode, taskNode.getName()));
                }
            }
        }

        ProcessDag processDag = new ProcessDag();
        processDag.setEdges(taskNodeRelations);
        processDag.setNodes(taskNodeList);


        // generate detail Dag, to be executed
        DAG<String, TaskNode, TaskNodeRelation> dag = new DAG<>();

        if (CollectionUtils.isNotEmpty(processDag.getNodes())) {
            for (TaskNode node : processDag.getNodes()) {
                dag.addNode(node.getName(), node);
            }
        }

        if (CollectionUtils.isNotEmpty(processDag.getEdges())) {
            for (TaskNodeRelation edge : processDag.getEdges()) {
                dag.addEdge(edge.getStartNode(), edge.getEndNode());
            }
        }

        return dag;
    }
}
