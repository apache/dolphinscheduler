package org.apache.dolphinscheduler.plugin.task.switchtask;

import java.util.List;

public class SwitchResult {
    private List<SwitchCondition> dependTaskList;

    private Long nextNode;

    public List<SwitchCondition> getDependTaskList() {
        return dependTaskList;
    }

    public void setDependTaskList(List<SwitchCondition> dependTaskList) {
        this.dependTaskList = dependTaskList;
    }

    public Long getNextNode() {
        return nextNode;
    }

    public void setNextNode(Long nextNode) {
        this.nextNode = nextNode;
    }
}
