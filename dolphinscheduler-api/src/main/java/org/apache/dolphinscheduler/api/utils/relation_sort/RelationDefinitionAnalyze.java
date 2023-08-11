package org.apache.dolphinscheduler.api.utils.relation_sort;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.dolphinscheduler.api.dto.DagDataSchedule;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RelationDefinitionAnalyze {
    private static final Logger logger = LoggerFactory.getLogger(RelationDefinitionAnalyze.class);
    Map<Long, DagDataSchedule> processDict = new HashMap<>();

    Map<Long, List<Long>> subProcessRelationDict = new HashMap<>();


    public RelationDefinitionAnalyze(List<DagDataSchedule> dagDataScheduleList) {
        dagDataScheduleList.forEach(dagDataSchedule -> {
            processDict.put(dagDataSchedule.getProcessDefinition().getCode(), dagDataSchedule);
            addDefinitionNode(dagDataSchedule.getProcessDefinition().getCode(), null);
            dagDataSchedule.getTaskDefinitionList().forEach(taskDefinition -> {
                if ("SUB_PROCESS".equals(taskDefinition.getTaskType())) {
                    Long preProcessDefinitionCode = SubRelationDefinitionUtils.getProcessDefinitionCode(taskDefinition.getTaskParams());
                    addDefinitionNode(dagDataSchedule.getProcessDefinition().getCode(), preProcessDefinitionCode);
                }
            });
        });
    }

    public List<DagDataSchedule> sort() {
        Set<Long> sort = new LinkedHashSet<>();


        while (true) {
            List<Long> collect = subProcessRelationDict.entrySet().stream().filter((entry) -> {
                if (entry.getValue().isEmpty()) {
                    return true;
                }
                return sort.containsAll(entry.getValue());
            }).map((entry -> entry.getKey())).collect(Collectors.toList());
            sort.addAll(collect);
            collect.forEach(code -> subProcessRelationDict.remove(code));
            if (collect.size() == 0) {
                break;
            }
        }

        List<DagDataSchedule> result = new LinkedList<>();
        sort.forEach(code -> result.add(processDict.get(code)));
        if (!subProcessRelationDict.isEmpty()) {
            logger.warn("ProcessRelationAnalyze failed size: {} ,subProcessRelationDict: {}", subProcessRelationDict.size(), JSONUtils.toJsonString(subProcessRelationDict));

        }
        return result;
    }


    private void addDefinitionNode(Long processDefinitionCode, Long preProcessDefinitionCode) {

        if (!subProcessRelationDict.containsKey(processDefinitionCode)) {
            subProcessRelationDict.put(processDefinitionCode, new LinkedList());
        }

        if (preProcessDefinitionCode != null) {
            subProcessRelationDict.get(processDefinitionCode).add(preProcessDefinitionCode);
        }

    }


}