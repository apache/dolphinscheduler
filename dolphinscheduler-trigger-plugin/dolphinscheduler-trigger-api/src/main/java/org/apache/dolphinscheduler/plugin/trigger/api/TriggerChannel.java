package org.apache.dolphinscheduler.plugin.trigger.api;

public interface TriggerChannel {
    void cancelApplication(boolean status);

    AbstractTrigger createTrigger(TriggerExecutionContext taskRequest);
}
