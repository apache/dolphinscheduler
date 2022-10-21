package org.apache.dolphinscheduler.plugin.task.datax.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ElasticSearchDataxParams {
    private String index;
    private String type;
    private boolean clearnUp;
    private String splitter;
    private int tyrSize;
    private int timeout;
    private boolean discovery;
    private boolean compression;
    private boolean multiThread;
    private boolean ignoreWriteError;
    private boolean ignoreParseError;
    private String alias;
    private int aliasMode;
    private String settings;
    private boolean dynamic;
}
