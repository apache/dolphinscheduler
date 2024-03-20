package org.apache.dolphinscheduler.api.utils;

import java.util.List;

import lombok.Data;

@Data
public class TaskDependencyUtility {

    private String taskName;
    private List<String> upStreams;
    private String dataSource;
}
