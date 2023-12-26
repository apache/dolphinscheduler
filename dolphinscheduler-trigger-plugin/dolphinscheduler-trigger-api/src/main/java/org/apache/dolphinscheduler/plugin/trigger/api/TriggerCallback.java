package org.apache.dolphinscheduler.plugin.trigger.api;

public interface TriggerCallback {
    public void updateRemoteApplicationInfo(int projectId, int schedulerId);
}
