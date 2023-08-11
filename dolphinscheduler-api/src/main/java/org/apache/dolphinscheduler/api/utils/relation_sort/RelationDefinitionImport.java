package org.apache.dolphinscheduler.api.utils.relation_sort;

import org.apache.dolphinscheduler.dao.entity.TaskDefinition;

import java.util.HashMap;


import org.apache.dolphinscheduler.api.dto.DagDataSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;


public class RelationDefinitionImport {
    private static final Logger logger = LoggerFactory.getLogger(RelationDefinitionImport.class);
    Map<Long, List<TaskDefinition>> subTaskRelationDict = new HashMap<>();
    private RelationDefinitionAnalyze relationDefinitionAnalyze;

    public RelationDefinitionImport(List<DagDataSchedule> dagDataScheduleList) {
        relationDefinitionAnalyze = new RelationDefinitionAnalyze(dagDataScheduleList);

        dagDataScheduleList.forEach(dagDataSchedule -> {
            dagDataSchedule.getTaskDefinitionList().forEach(taskDefinition -> {
                if ("SUB_PROCESS".equals(taskDefinition.getTaskType())) {
                    addRelationDict(taskDefinition);
                }
            });
        });
    }

    private void addSubTaskRelationDict(TaskDefinition taskDefinition, Long preProcessDefinitionCode) {
        if (!subTaskRelationDict.containsKey(preProcessDefinitionCode)) {
            subTaskRelationDict.put(preProcessDefinitionCode, new LinkedList());
        }

        if (preProcessDefinitionCode != null) {
            subTaskRelationDict.get(preProcessDefinitionCode).add(taskDefinition);
        }
    }

    private void addRelationDict(TaskDefinition taskDefinition) {
        Long preProcessDefinitionCode = SubRelationDefinitionUtils.getProcessDefinitionCode(taskDefinition.getTaskParams());
        addSubTaskRelationDict(taskDefinition, preProcessDefinitionCode);
    }

    public void doImport(ImportFunc importFunc) {
        this.relationDefinitionAnalyze.sort().forEach(dagDataSchedule -> {
            Long originCode = dagDataSchedule.getProcessDefinition().getCode();
            logger.info("### doImport start: {} ,code: {} ", dagDataSchedule.getProcessDefinition().getName(), originCode);
            boolean success = importFunc.doImport(dagDataSchedule);
            if (!success) {
                return;
            }
            Long newCode = dagDataSchedule.getProcessDefinition().getCode();
            logger.info("### doImport end: {} ,originCode: {} ,newCode: {}", dagDataSchedule.getProcessDefinition().getName(), originCode, newCode);
            updateProcessDefinitionCode(originCode, newCode);
        });
    }

    private void updateProcessDefinitionCode(Long originCode, Long newCode) {
        if (!this.subTaskRelationDict.containsKey(originCode)) {
            return;
        }
        for (TaskDefinition taskDefinition : this.subTaskRelationDict.get(originCode)) {
            String taskParams = SubRelationDefinitionUtils.updateProcessDefinitionCode(taskDefinition.getTaskParams(), newCode);
            taskDefinition.setTaskParams(taskParams);
            logger.info("###updateProcessDefinitionCode taskName {} ,origin: {} ,new: {}", taskDefinition.getName(), originCode, newCode);
        }

    }


    public interface ImportFunc {
        boolean doImport(DagDataSchedule dagDataSchedule);
    }

}
