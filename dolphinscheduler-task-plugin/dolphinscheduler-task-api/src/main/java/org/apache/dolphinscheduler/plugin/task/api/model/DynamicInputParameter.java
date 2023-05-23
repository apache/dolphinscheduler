package org.apache.dolphinscheduler.plugin.task.api.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class DynamicInputParameter {

    @NonNull
    private String name;
    @NonNull
    private String value;
    private String separator = ",";
}
