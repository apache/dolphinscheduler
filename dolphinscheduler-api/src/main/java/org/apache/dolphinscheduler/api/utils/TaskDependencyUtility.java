package org.apache.dolphinscheduler.api.utils;

import lombok.Data;

import java.util.List;

@Data
public class TaskDependencyUtility {
    private String taskName;
    private List<String> upStreams;
    private String dataSource;
}
