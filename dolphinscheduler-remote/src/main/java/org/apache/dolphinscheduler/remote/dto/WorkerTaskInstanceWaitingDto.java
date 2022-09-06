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
public class WorkerTaskInstanceWaitingDto {

    private int taskInstanceId;
    private String taskInstanceName;
    private long WaitingTime;
    private Date submitTime;
}
