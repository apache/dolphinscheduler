package org.apache.dolphinscheduler.service.subworkflow;

import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.RelationSubWorkflow;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public interface SubWorkflowService {

    List<ProcessInstance> getAllDynamicSubWorkflow(long processInstanceId, long taskCode);

    int batchInsertRelationSubWorkflow(List<RelationSubWorkflow> relationSubWorkflowList);

    List<ProcessInstance> filterFinishProcessInstances(List<ProcessInstance> processInstanceList);

    List<ProcessInstance> filterSuccessProcessInstances(List<ProcessInstance> processInstanceList);

    List<ProcessInstance> filterRunningProcessInstances(List<ProcessInstance> processInstanceList);

    List<ProcessInstance> filterWaitToRunProcessInstances(List<ProcessInstance> processInstanceList);

    List<ProcessInstance> filterFailedProcessInstances(List<ProcessInstance> processInstanceList);

    List<Property> getWorkflowOutputParameters(ProcessInstance processInstance);
}
