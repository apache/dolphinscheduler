package org.apache.dolphinscheduler.plugin.task.switchtask;

public class SwitchCondition {

    private String condition;
    private Long nextNode;

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Long getNextNode() {
        return nextNode;
    }

    public void setNextNode(Long nextNode) {
        this.nextNode = nextNode;
    }
}
