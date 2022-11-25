package org.apache.dolphinscheduler.api.dto.workflowInstance;

import org.apache.dolphinscheduler.api.dto.PageQueryDto;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;

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

    public ProcessInstance convert2ProcessInstance() {
        ProcessInstance processInstance = new ProcessInstance();
        if (this.workflowName != null) {
            processInstance.setName(this.workflowName);
        }
        return processInstance;
    }
}
