package org.apache.dolphinscheduler.server.master.dag;

public interface TaskTriggerConditionChecker {

    boolean taskCanTrigger(String taskName);

}
