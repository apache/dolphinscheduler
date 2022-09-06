package org.apache.dolphinscheduler.api.dto;

import org.apache.dolphinscheduler.dao.dto.WorkerGroupDto;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class WorkerGroupHandleDto {

    private WorkerGroupDto workerGroup;
    private String workerGroupName;
    private Map<String, WorkerGroupDto> workerGroupsMap;
    private Collection<String> childrenNodes;
    private List<WorkerGroupDto> workerGroups;
}
