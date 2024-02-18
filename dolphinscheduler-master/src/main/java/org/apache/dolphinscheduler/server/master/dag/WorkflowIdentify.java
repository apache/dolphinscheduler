package org.apache.dolphinscheduler.server.master.dag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowIdentify {

    private long workflowCode;

    private int workflowVersion;

}
