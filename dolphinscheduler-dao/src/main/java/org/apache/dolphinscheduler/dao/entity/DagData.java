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

package org.apache.dolphinscheduler.dao.entity;

import java.util.List;

/**
 * DagData
 */
public class DagData {

    /**
     * processDefinition
     */
    private ProcessDefinition processDefinition;

    /**
     * processTaskRelationList
     */
    private List<ProcessTaskRelation> processTaskRelationList;

    /**
     * processTaskRelationList
     */
    private List<TaskDefinition> taskDefinitionList;

    public DagData(ProcessDefinition processDefinition, List<ProcessTaskRelation> processTaskRelationList,
                   List<TaskDefinition> taskDefinitionList) {
        this.processDefinition = processDefinition;
        this.processTaskRelationList = processTaskRelationList;
        this.taskDefinitionList = taskDefinitionList;
    }

    public DagData() {
    }

    public ProcessDefinition getProcessDefinition() {
        return processDefinition;
    }

    public void setProcessDefinition(ProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
    }

    public List<ProcessTaskRelation> getProcessTaskRelationList() {
        return processTaskRelationList;
    }

    public void setProcessTaskRelationList(List<ProcessTaskRelation> processTaskRelationList) {
        this.processTaskRelationList = processTaskRelationList;
    }

    public List<TaskDefinition> getTaskDefinitionList() {
        return taskDefinitionList;
    }

    public void setTaskDefinitionList(List<TaskDefinition> taskDefinitionList) {
        this.taskDefinitionList = taskDefinitionList;
    }
}
