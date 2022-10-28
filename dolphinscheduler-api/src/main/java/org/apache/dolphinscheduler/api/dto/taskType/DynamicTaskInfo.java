package org.apache.dolphinscheduler.api.dto.taskType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class DynamicTaskInfo {

    private String hover;
    private String icon;
    private String json;

}
