package org.apache.dolphinscheduler.server.master.runner.task.dynamic;

import java.util.Map;

import lombok.Data;

@Data
public class DynamicOutput {

    private Map<String, String> dynParams;

    private Map<String, String> outputValue;

    private int mappedTimes;

}
