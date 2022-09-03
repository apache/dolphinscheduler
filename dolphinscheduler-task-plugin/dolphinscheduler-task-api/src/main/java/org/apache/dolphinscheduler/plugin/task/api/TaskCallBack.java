package org.apache.dolphinscheduler.plugin.task.api;

public interface TaskCallBack {

    public void updateRemoteApplicationInfo(int taskInstanceId, String appIds);
}
