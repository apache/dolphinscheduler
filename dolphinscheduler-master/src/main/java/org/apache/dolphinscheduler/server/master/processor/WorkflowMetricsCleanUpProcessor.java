package org.apache.dolphinscheduler.server.master.processor;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.WorkflowMetricsCleanUpCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.server.master.metrics.ProcessInstanceMetrics;

import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;

@Component
public class WorkflowMetricsCleanUpProcessor implements NettyRequestProcessor {

    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.WORKFLOW_METRICS_CLEANUP == command.getType(),
                String.format("invalid command type: %s", command.getType()));

        WorkflowMetricsCleanUpCommand workflowMetricsCleanUpCommand =
                JSONUtils.parseObject(command.getBody(), WorkflowMetricsCleanUpCommand.class);

        ProcessInstanceMetrics.cleanUpProcessInstanceCountMetricsByDefinitionCode(
                workflowMetricsCleanUpCommand.getProcessDefinitionCode());
    }

}
