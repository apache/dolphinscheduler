package org.apache.dolphinscheduler.api.dto;

import org.apache.dolphinscheduler.dao.entity.WorkerGroup;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class WorkerGroupHandleDto {
    private WorkerGroup workerGroup;
    private   String workerGroupName;
    private Map<String, WorkerGroup> workerGroupsMap;
    private Collection<String> childrenNodes;
    private List<WorkerGroup> workerGroups;
}
