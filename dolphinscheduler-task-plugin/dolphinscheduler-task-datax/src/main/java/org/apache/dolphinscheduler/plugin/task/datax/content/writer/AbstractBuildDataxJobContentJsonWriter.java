package org.apache.dolphinscheduler.plugin.task.datax.content.writer;

import org.apache.dolphinscheduler.plugin.task.datax.DataxParameters;
import org.apache.dolphinscheduler.plugin.task.datax.DataxTaskExecutionContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author xinxing
 * @description 功能描述
 * @create 2023/8/30 15:06
 */
public abstract class AbstractBuildDataxJobContentJsonWriter {

    protected final Logger log = LoggerFactory.getLogger(AbstractBuildDataxJobContentJsonWriter.class);

    protected DataxTaskExecutionContext dataxTaskExecutionContext;
    protected DataxParameters dataXParameters;

    public abstract void init(DataxTaskExecutionContext dataxTaskExecutionContext, DataxParameters dataXParameters);

    public abstract ObjectNode writer();
}
