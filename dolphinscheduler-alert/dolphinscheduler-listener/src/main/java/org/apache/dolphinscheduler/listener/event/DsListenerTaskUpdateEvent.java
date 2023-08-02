package org.apache.dolphinscheduler.listener.event;


import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

/**
 * @author wxn
 * @date 2023/7/29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class DsListenerTaskUpdateEvent extends DsListenerEvent {

    @JsonProperty("taskInstanceId")
    private int taskInstanceId;
    @JsonProperty("taskName")
    private String taskName;
    @JsonProperty("taskType")
    private String taskType;
    @JsonProperty("processDefinitionId")
    private int processDefinitionId;
    @JsonProperty("processDefinitionName")
    private String processDefinitionName;
    @JsonProperty("processInstanceId")
    private int processInstanceId;
    @JsonProperty("processInstanceName")
    private String processInstanceName;
    @JsonProperty("state")
    private TaskExecutionStatus state;
    @JsonProperty("startTime")
    private Date startTime;
    @JsonProperty("endTime")
    private Date endTime;
    @JsonProperty("host")
    private String host;
    @JsonProperty("logPath")
    private String logPath;
}
