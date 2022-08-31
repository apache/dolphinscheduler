package org.apache.dolphinscheduler.dao.repository;

import lombok.NonNull;
import org.apache.dolphinscheduler.dao.dto.WorkerGroupDto;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public interface WorkerGroupDao {

    List<WorkerGroupDto> queryAllWorkerGroup();

    List<WorkerGroupDto> queryWorkerGroupByName(@NonNull String workerGroupName);

    List<WorkerGroupDto> queryByIds(@NonNull List<Integer> workerGroupIds);

    Optional<WorkerGroupDto> queryById(int workerGroupId);

    int insertOrUpdate(@NonNull WorkerGroupDto workerGroupDto);

    int insert(@NonNull WorkerGroupDto workerGroupDto);

    int update(@NonNull WorkerGroupDto workerGroupDto);

    int deleteById(@NonNull Integer workerGroupId);

}
