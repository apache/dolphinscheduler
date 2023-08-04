package org.apache.dolphinscheduler.listener.event;

import org.apache.dolphinscheduler.common.enums.AlertEvent;
import org.apache.dolphinscheduler.common.enums.AlertWarnLevel;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.spi.enums.CommandType;
import org.apache.dolphinscheduler.spi.enums.Flag;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author wxn
 * @date 2023/7/29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class DsListenerWorkflowUpdateEvent extends DsListenerEvent {

    @JsonProperty("projectId")
    private Integer projectId;
    @JsonProperty("projectCode")
    private Long projectCode;
    @JsonProperty("projectName")
    private String projectName;
    @JsonProperty("owner")
    private String owner;
    @JsonProperty("processId")
    private Integer processId;
    @JsonProperty("processDefinitionCode")
    private Long processDefinitionCode;
    @JsonProperty("processName")
    private String processName;
}
