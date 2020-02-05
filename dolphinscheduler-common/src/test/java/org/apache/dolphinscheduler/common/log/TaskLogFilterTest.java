package org.apache.dolphinscheduler.common.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.core.spi.FilterReply;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Marker;

import java.util.Map;


public class TaskLogFilterTest {

    @Test
    public void decide() {
        TaskLogFilter taskLogFilter = new TaskLogFilter();


        FilterReply filterReply = taskLogFilter.decide(new ILoggingEvent() {
            @Override
            public String getThreadName() {
                return LoggerUtils.TASK_LOGGER_THREAD_NAME;
            }

            @Override
            public Level getLevel() {
                return Level.INFO;
            }

            @Override
            public String getMessage() {
                return "raw script : echo 222";
            }

            @Override
            public Object[] getArgumentArray() {
                return new Object[0];
            }

            @Override
            public String getFormattedMessage() {
                return "raw script : echo 222";
            }

            @Override
            public String getLoggerName() {
                return null;
            }

            @Override
            public LoggerContextVO getLoggerContextVO() {
                return null;
            }

            @Override
            public IThrowableProxy getThrowableProxy() {
                return null;
            }

            @Override
            public StackTraceElement[] getCallerData() {
                return new StackTraceElement[0];
            }

            @Override
            public boolean hasCallerData() {
                return false;
            }

            @Override
            public Marker getMarker() {
                return null;
            }

            @Override
            public Map<String, String> getMDCPropertyMap() {
                return null;
            }

            @Override
            public Map<String, String> getMdc() {
                return null;
            }

            @Override
            public long getTimeStamp() {
                return 0;
            }

            @Override
            public void prepareForDeferredProcessing() {

            }
        });

        Assert.assertEquals(FilterReply.ACCEPT, filterReply);

    }
}