package org.apache.dolphinscheduler.plugin.task.datax.content;

import org.apache.dolphinscheduler.plugin.task.datax.content.reader.AbstractBuildDataxJobContentJsonReader;
import org.apache.dolphinscheduler.plugin.task.datax.content.reader.BuildDataxJobContentJsonDefaultReader;
import org.apache.dolphinscheduler.plugin.task.datax.content.writer.AbstractBuildDataxJobContentJsonWriter;
import org.apache.dolphinscheduler.plugin.task.datax.content.writer.BuildDataxJobContentJsonDefaultWriter;
import org.apache.dolphinscheduler.spi.enums.DbType;

public class BuildDataxJobContentJsonFactory {

    public AbstractBuildDataxJobContentJsonReader getReader(DbType dbType) {
        switch (dbType) {
            default:
                return new BuildDataxJobContentJsonDefaultReader();
        }
    }

    public AbstractBuildDataxJobContentJsonWriter getWriter(DbType dbType) {
        switch (dbType) {
            default:
                return new BuildDataxJobContentJsonDefaultWriter();
        }
    }

}
