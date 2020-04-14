package org.apache.dolphinscheduler.dao.entity;

public class WorkFlowRelation {
    private int sourceWorkFlowId;
    private int targetWorkFlowId;

    public int getSourceWorkFlowId() {
        return sourceWorkFlowId;
    }

    public void setSourceWorkFlowId(int sourceWorkFlowId) {
        this.sourceWorkFlowId = sourceWorkFlowId;
    }

    public int getTargetWorkFlowId() {
        return targetWorkFlowId;
    }

    public void setTargetWorkFlowId(int targetWorkFlowId) {
        this.targetWorkFlowId = targetWorkFlowId;
    }
}
