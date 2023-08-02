package org.apache.dolphinscheduler.listener.plugin;

import org.apache.dolphinscheduler.listener.event.*;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;

import java.util.List;

/**
 * @author wxn
 * @date 2023/5/15
 */
public interface ListenerPlugin {

    String name();

    List<PluginParams> params();

    /**
     * api: master/worker 失连/超时
     */
    default void onMasterDown(DsListenerMasterDownEvent masterDownEvent) {
    }

    default void onMasterTimeout(DsListenerMasterTimeoutEvent masterTimeoutEvent) {
    }

    /**
     * api：工作流创建/修改/删除等
     */
    default void onWorkflowAdded(DsListenerWorkflowAddedEvent workflowAddedEvent) {
    }

    default void onWorkflowUpdate(DsListenerWorkflowUpdateEvent workflowUpdateEvent) {
    }

    default void onWorkflowRemoved(DsListenerWorkflowRemovedEvent workflowRemovedEvent) {
    }

    /**
     * master：工作流开始/结束/失败等
     */
    default void onWorkflowStart(DsListenerWorkflowStartEvent workflowStartEvent) {
    }

    default void onWorkflowEnd(DsListenerWorkflowEndEvent workflowEndEvent) {
    }

    default void onWorkflowFail(DsListenerWorkflowFailEvent workflowErrorEvent) {
    }

    /**
     * api：任务创建/修改/删除等
     */
    default void onTaskAdded(DsListenerTaskAddedEvent taskAddedEvent) {
    }

    default void onTaskUpdate(DsListenerTaskUpdateEvent taskUpdateEvent) {
    }

    default void onTaskRemoved(DsListenerTaskRemovedEvent taskRemovedEvent) {
    }

    /**
     * worker：任务开始/结束/失败等
     */
    default void onTaskStart(DsListenerTaskStartEvent taskStartEvent) {
    }

    default void onTaskEnd(DsListenerTaskEndEvent taskEndEvent) {
    }

    default void onTaskFail(DsListenerTaskFailEvent taskErrorEvent) {
    }
}
