package org.apache.dolphinscheduler.common.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.core.spi.FilterReply;
import org.apache.dolphinscheduler.common.Constants;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Marker;

import java.util.Map;


public class WorkerLogFilterTest {

    @Test
    public void decide() {
        WorkerLogFilter workerLogFilter = new WorkerLogFilter();


        FilterReply filterReply = workerLogFilter.decide(new ILoggingEvent() {
            @Override
            public String getThreadName() {
                return Constants.THREAD_NAME_WORKER_SERVER;
            }

            @Override
            public Level getLevel() {
                return Level.INFO;
            }

            @Override
            public String getMessage() {
                return "consume tasks: [2_177_2_704_-1],there still have 0 tasks need to be executed";
            }

            @Override
            public Object[] getArgumentArray() {
                return new Object[0];
            }

            @Override
            public String getFormattedMessage() {
                return "consume tasks: [2_177_2_704_-1],there still have 0 tasks need to be executed";
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