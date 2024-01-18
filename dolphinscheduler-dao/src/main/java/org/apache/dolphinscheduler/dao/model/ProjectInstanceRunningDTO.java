package org.apache.dolphinscheduler.dao.model;

import lombok.Data;

/**
 * @program: dolphinscheduler
 * @description:
 * @author: chenlc
 * @create: 2024-01-18 10:26
 */
@Data
public class ProjectInstanceRunningDTO {
    private Integer instRunningCount;
    private Long projectCode;
}
