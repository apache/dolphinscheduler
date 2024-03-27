package org.apache.dolphinscheduler.plugin.trigger.api;

import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractTrigger {
    public void init() {
    }

    public abstract void trigger(TriggerCallback triggerCallback) throws TriggerException;

    public abstract void cancel() throws TriggerException;
}
