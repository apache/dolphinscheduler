package org.apache.dolphinscheduler.plugin.trigger.simple;

import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.plugin.trigger.api.AbstractTrigger;
import org.apache.dolphinscheduler.plugin.trigger.api.TriggerCallBack;
import org.apache.dolphinscheduler.plugin.trigger.api.TriggerException;
import org.apache.dolphinscheduler.plugin.trigger.api.TriggerExecutionContext;
import org.apache.dolphinscheduler.plugin.trigger.api.parameters.AbstractParameters;

@Slf4j
public class SimpleTrigger extends AbstractTrigger {

    private SimpleTriggerPatameters simpleTriggerPatameters;

    private TriggerExecutionContext triggerExecutionContext;

    public SimpleTrigger(TriggerExecutionContext triggerExecutionContext) {
        super(triggerExecutionContext);

        this.triggerExecutionContext = triggerExecutionContext;
    }

    @Override
    public void init() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handle(TriggerCallBack triggerCallBack) throws TriggerException {
        try {

        } catch (Exception e) {
            log.error("shell task error", e);
            throw new TriggerException("Execute shell task error", e);
        }
    }

    @Override
    public void cancel() throws TriggerException {
        // cancel process
        try {
        } catch (Exception e) {
            throw new TriggerException("cancel application error", e);
        }
    }

    public AbstractParameters getParameters() {
        return simpleTriggerPatameters;
    }

}
