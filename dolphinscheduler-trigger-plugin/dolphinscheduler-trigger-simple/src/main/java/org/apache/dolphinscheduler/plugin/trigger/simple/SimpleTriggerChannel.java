package org.apache.dolphinscheduler.plugin.trigger.simple;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.trigger.api.TriggerChannel;
import org.apache.dolphinscheduler.plugin.trigger.api.TriggerExecutionContext;
import org.apache.dolphinscheduler.plugin.trigger.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.trigger.api.parameters.ParametersNode;

public class SimpleTriggerChannel implements TriggerChannel {

    @Override
    public void cancelApplication(boolean status) {

    }

    @Override
    public SimpleTrigger createTrigger(TriggerExecutionContext taskRequest) {
        return new SimpleTrigger(taskRequest);
    }
}
