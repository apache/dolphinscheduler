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

package org.apache.dolphinscheduler.dao.repository;

import org.apache.dolphinscheduler.dao.entity.WorkFlowRelationDetail;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskLineage;

import java.util.List;

public interface WorkflowTaskLineageDao extends IDao<WorkflowTaskLineage> {

    int batchDeleteByWorkflowDefinitionCode(List<Long> workflowDefinitionCodes);

    int batchInsert(List<WorkflowTaskLineage> workflowTaskLineages);

    List<WorkflowTaskLineage> queryByProjectCode(long projectCode);

    List<WorkFlowRelationDetail> queryWorkFlowLineageByCode(long workflowDefinitionCode);

    List<WorkFlowRelationDetail> queryWorkFlowLineageByName(long projectCode,
                                                            String workflowDefinitionName);

    List<WorkflowTaskLineage> queryWorkFlowLineageByDept(long deptProjectCode,
                                                         long deptWorkflowDefinitionCode,
                                                         long deptTaskDefinitionCode);

    List<WorkflowTaskLineage> queryByWorkflowDefinitionCode(long workflowDefinitionCode);

    int updateWorkflowTaskLineage(List<WorkflowTaskLineage> workflowTaskLineages);
}
