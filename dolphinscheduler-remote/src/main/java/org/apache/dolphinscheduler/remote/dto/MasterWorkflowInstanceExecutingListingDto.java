package org.apache.dolphinscheduler.remote.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MasterWorkflowInstanceExecutingListingDto {

    private int workflowInstanceId;

    private String workflowInstanceName;

    private Date startTime;

    private long costTime;

}
