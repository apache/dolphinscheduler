package org.apache.dolphinscheduler.server.utils;

import org.apache.dolphinscheduler.common.enums.CycleEnum;
import org.apache.dolphinscheduler.common.enums.DependentRelation;
import org.apache.dolphinscheduler.common.model.DependentItem;
import org.apache.dolphinscheduler.common.model.DependentTaskModel;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.task.dependent.DependentParameters;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DependAnalyseUtils {

    public static DependentItem buildDependentItem(int processDefinitionId, String nodeName) {
        DependentItem dependentItem = new DependentItem();
        dependentItem.setDefinitionId(processDefinitionId);
        dependentItem.setDepTasks(nodeName);
        dependentItem.setCycle(CycleEnum.DAY.toString().toLowerCase());
        dependentItem.setDateValue("today");
        return dependentItem;
    }

    public static void addNodeDependentItem(TaskNode taskNode, int dependProcessId, String dependNodeName) {
        addNodeDependentItem(taskNode, buildDependentItem(dependProcessId, dependNodeName));
    }

    public static void addNodeDependentItem(TaskNode taskNode, DependentItem dependentItem) {
        DependentParameters dependentParameters;
        if (StringUtils.isEmpty(taskNode.getDependence())) {
            dependentParameters = new DependentParameters();
        } else {
            dependentParameters = JSONUtils.parseObject(taskNode.getDependence(), DependentParameters.class);
        }

        List<DependentTaskModel> dependTaskList;
        if (CollectionUtils.isEmpty(dependentParameters.getDependTaskList())) {
            dependTaskList = new ArrayList<>();
        } else {
            dependTaskList = dependentParameters.getDependTaskList();
        }

        DependentTaskModel dependentTaskModel = new DependentTaskModel();
        List<DependentItem> dependItemList = new ArrayList<>();
        dependItemList.add(dependentItem);
        dependentTaskModel.setDependItemList(dependItemList);
        dependentTaskModel.setRelation(DependentRelation.AND);
        dependTaskList.add(dependentTaskModel);
        dependentParameters.setDependTaskList(dependTaskList);
        dependentParameters.setRelation(DependentRelation.AND);
        taskNode.setDependence(JSONUtils.writeValueAsString(dependentParameters));
    }

    public static void addNodeDepList(TaskNode taskNode, TaskNode dependNode) {
        if (CollectionUtils.isEmpty(taskNode.getDepList())) {
            taskNode.setDepList(Arrays.asList(dependNode.getName()));
        } else {
            if (!taskNode.getDepList().contains(dependNode.getName())) {
                taskNode.getDepList().add(dependNode.getName());
            }
        }
    }

}
