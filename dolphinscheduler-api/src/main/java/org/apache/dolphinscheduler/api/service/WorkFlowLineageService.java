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
package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.mapper.WorkFlowLineageMapper;
import org.apache.dolphinscheduler.dao.entity.WorkFlowLineage;
import org.apache.dolphinscheduler.dao.entity.WorkFlowRelation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WorkFlowLineageService extends BaseService {

    @Autowired
    private WorkFlowLineageMapper workFlowLineageMapper;

    public Map<String, Object> queryWorkFlowLineageByName(String workFlowName, int projectId) {
        Map<String, Object> result = new HashMap<>(5);
        List<WorkFlowLineage> workFlowLineageList = workFlowLineageMapper.queryByName(workFlowName, projectId);
        result.put(Constants.DATA_LIST, workFlowLineageList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    private List<WorkFlowRelation> getWorkFlowRelationRecursion(Set<Integer> ids, List<WorkFlowRelation> workFlowRelations) {
        for(int id : ids) {
            List<WorkFlowRelation> workFlowRelationsTmp = workFlowLineageMapper.querySourceTarget(id);

            if(workFlowRelationsTmp != null && !workFlowRelationsTmp.isEmpty()) {
                Set<Integer> idsTmp = new HashSet<>();
                for(WorkFlowRelation workFlowRelation:workFlowRelationsTmp) {
                    idsTmp.add(workFlowRelation.getTargetWorkFlowId());
                }
                workFlowRelations.addAll(workFlowRelationsTmp);
                getWorkFlowRelationRecursion(idsTmp, workFlowRelations);
            }
        }
        return workFlowRelations;
    }

    public Map<String, Object> queryWorkFlowLineageByIds(Set<Integer> ids,int projectId) {
        Map<String, Object> result = new HashMap<>(5);
        List<WorkFlowLineage> workFlowLineageList = workFlowLineageMapper.queryByIds(ids, projectId);
        Map<String, Object> workFlowLists = new HashMap<>(5);
        Set<Integer> idsV = new HashSet<>();
        if(ids == null || ids.isEmpty()){
            for(WorkFlowLineage workFlowLineage:workFlowLineageList) {
                idsV.add(workFlowLineage.getWorkFlowId());
            }
        } else {
            idsV = ids;
        }
        List<WorkFlowRelation> workFlowRelations = new ArrayList<>();
        getWorkFlowRelationRecursion(idsV, workFlowRelations);

        Set<Integer> idSet = new HashSet<>();
        //如果传入参数不为空，则需要补充下游工作流明细属性
        if(ids != null && !ids.isEmpty()) {
            for(WorkFlowRelation workFlowRelation : workFlowRelations) {
                idSet.add(workFlowRelation.getTargetWorkFlowId());
            }
            for(int id : ids){
                idSet.remove(id);
            }
            if(!idSet.isEmpty()) {
                workFlowLineageList.addAll(workFlowLineageMapper.queryByIds(idSet, projectId));
            }
        }

        workFlowLists.put("workFlowList",workFlowLineageList);
        workFlowLists.put("workFlowRelationList",workFlowRelations);
        result.put(Constants.DATA_LIST, workFlowLists);
        putMsg(result, Status.SUCCESS);
        return result;
    }
}
