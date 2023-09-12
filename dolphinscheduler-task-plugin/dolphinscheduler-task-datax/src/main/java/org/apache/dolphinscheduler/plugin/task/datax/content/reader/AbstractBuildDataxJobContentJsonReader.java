package org.apache.dolphinscheduler.plugin.task.datax.content.reader;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.dolphinscheduler.plugin.task.datax.DataxParameters;
import org.apache.dolphinscheduler.plugin.task.datax.DataxTaskExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBuildDataxJobContentJsonReader {
    protected final Logger log = LoggerFactory.getLogger(AbstractBuildDataxJobContentJsonReader.class);

    protected DataxTaskExecutionContext dataxTaskExecutionContext;
    protected DataxParameters dataXParameters;

    public abstract void init(DataxTaskExecutionContext dataxTaskExecutionContext, DataxParameters dataXParameters);

    public abstract ObjectNode reader();
}
