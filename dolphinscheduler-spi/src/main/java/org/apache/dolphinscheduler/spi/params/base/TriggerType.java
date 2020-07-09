package org.apache.dolphinscheduler.spi.params.base;

public enum TriggerType {

    BLUR("blur"),

    CHANGE("change");

    private String triggerType;

    TriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public String getTriggerType() {
        return this.triggerType;
    }

}
