package org.apache.dolphinscheduler.api.dto;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;

import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DynamicSubWorkflowDto {

    private long processInstanceId;

    private String name;

    private long index;

    private Map<String, String> parameters;

    private WorkflowExecutionStatus state;

}
