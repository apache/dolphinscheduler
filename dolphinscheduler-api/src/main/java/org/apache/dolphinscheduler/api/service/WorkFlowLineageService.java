package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.mapper.WorkFlowLineageMapper;
import org.apache.dolphinscheduler.dao.entity.WorkFlowLineage;
import org.apache.dolphinscheduler.dao.entity.WorkFlowRelation;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WorkFlowLineageService extends BaseService {
    private static final Logger logger = LoggerFactory.getLogger(WorkFlowLineageService.class);

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

            if(workFlowRelationsTmp != null && workFlowRelationsTmp.size()>0) {
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
        Set<Integer> idsV = ids;
        if(ids == null || ids.size() == 0){
            for(WorkFlowLineage workFlowLineage:workFlowLineageList) {
                idsV.add(workFlowLineage.getWorkFlowId());
            }
        }
        List<WorkFlowRelation> workFlowRelations = new ArrayList<>();
        getWorkFlowRelationRecursion(idsV, workFlowRelations);

        Set<Integer> idSet = new HashSet<>();
        //如果传入参数不为空，则需要补充下游工作流明细属性
        if(ids != null && ids.size() > 0) {
            for(WorkFlowRelation workFlowRelation : workFlowRelations) {
                idSet.add(workFlowRelation.getTargetWorkFlowId());
            }
            for(int id : ids){
                idSet.remove(id);
            }
            if(idSet.size()>0) {
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
