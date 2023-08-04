package org.apache.dolphinscheduler.listener.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author wxn
 * @date 2023/7/29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class DsListenerWorkflowAddedEvent extends DsListenerEvent {

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
