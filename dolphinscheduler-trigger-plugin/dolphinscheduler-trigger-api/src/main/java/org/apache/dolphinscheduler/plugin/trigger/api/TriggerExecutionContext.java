package org.apache.dolphinscheduler.plugin.trigger.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * to master/worker task transport
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TriggerExecutionContext implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * trigger id
     */
    private int triggerInstanceId;

    /**
     * trigger name
     */
    private String triggerName;

    /**
     * trigger first submit time.
     */
    private long firstSubmitTime;

    /**
     * trigger start time
     */
    private long startTime;

    /**
     * trigger type
     */
    private String triggerType;

    /**
     * trigger json
     */
    private String triggerJson;

    /**
     * processId
     */
    private int processId;

    /**
     * processCode
     */
    private Long processDefineCode;

    /**
     * processVersion
     */
    private int processDefineVersion;

    /**
     * appIds
     */
    private String appIds;

    /**
     * process instance id
     */
    private int processInstanceId;

    /**
     * process instance schedule time
     */
    private long scheduleTime;

    /**
     * process instance global parameters
     */
    private String globalParams;

    /**
     * execute user id
     */
    private int executorId;

    /**
     * command type if complement
     */
    private int cmdTypeIfComplement;

    /**
     * tenant code
     */
    private String tenantCode;

    /**
     * process define id
     */
    private int processDefineId;

    /**
     * project id
     */
    private int projectId;

    /**
     * project code
     */
    private long projectCode;

    /**
     * taskParams
     */
    private String taskParams;

    /**
     * environmentConfig
     */
    private String environmentConfig;
}

