package org.apache.dolphinscheduler.plugin.trigger.api;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.plugin.trigger.api.enums.TriggerExecutionStatus;
import org.apache.dolphinscheduler.plugin.trigger.api.parameters.AbstractParameters;

import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public abstract class AbstractTrigger {
    @Getter
    @Setter
    protected Map<String, String> taskOutputParams;

    /**
     * taskExecutionContext
     **/
    protected TriggerExecutionContext triggerExecutionContext;

    protected AbstractTrigger(TriggerExecutionContext triggerExecutionContext) {
        this.triggerExecutionContext = triggerExecutionContext;
    }

    public void init() {
    }

    public abstract void handle(TriggerCallBack taskCallBack) throws TriggerException;

    public abstract void cancel() throws TriggerException;

    /**
     * get task parameters
     *
     * @return AbstractParameters
     */
    public abstract AbstractParameters getParameters();


}
