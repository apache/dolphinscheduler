package org.apache.dolphinscheduler.api.dto.workflowInstance;

import org.apache.dolphinscheduler.api.dto.PageQueryDto;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * workflow instance request
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class WorkflowInstanceQueryRequest extends PageQueryDto {

    @Schema(name = "projectName", example = "PROJECT-NAME")
    String projectName;

    @Schema(name = "workflowName", example = "WORKFLOW-NAME")
    String workflowName;

    @Schema(name = "searchVal", example = "SEARCH-VAL")
    String searchVal;

    @Schema(name = "executorName", example = "EXECUTOR-NAME")
    String executorName;

    @Schema(name = "stateType", example = "STATE-TYPE")
    WorkflowExecutionStatus stateType;

    @Schema(name = "host", example = "HOST")
    String host;

    @Schema(name = "startDate", example = "START-TIME")
    String startTime;

    @Schema(name = "endDate", example = "END-DATE")
    String endTime;

    @Schema(name = "taskExecuteType", example = "EXECUTE-TYPE", defaultValue = "BATCH")
    TaskExecuteType taskExecuteType;
}
