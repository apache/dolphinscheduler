package org.apache.dolphinscheduler.dao.repository.impl;

import lombok.NonNull;
import org.apache.dolphinscheduler.dao.dto.WorkerGroupDto;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.mapper.WorkerGroupMapper;
import org.apache.dolphinscheduler.dao.repository.WorkerGroupDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class WorkerGroupDaoImpl implements WorkerGroupDao {

    @Autowired
    private WorkerGroupMapper workerGroupMapper;

    @Override
    public List<WorkerGroupDto> queryAllWorkerGroup() {
        return workerGroupMapper.queryAllWorkerGroup()
                .stream()
                .map(WorkerGroupDto::transformFromDo)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkerGroupDto> queryWorkerGroupByName(@NonNull String workerGroupName) {
        return workerGroupMapper.queryWorkerGroupByName(workerGroupName)
                .stream()
                .map(WorkerGroupDto::transformFromDo)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkerGroupDto> queryByIds(@NonNull List<Integer> workerGroupIds) {
        return workerGroupMapper.selectBatchIds(workerGroupIds)
                .stream()
                .map(WorkerGroupDto::transformFromDo)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<WorkerGroupDto> queryById(int workerGroupId) {
        WorkerGroup workerGroupDo = workerGroupMapper.selectById(workerGroupId);
        if (workerGroupDo == null) {
            return Optional.empty();
        }
        return Optional.of(WorkerGroupDto.transformFromDo(workerGroupDo));
    }

    @Override
    public int insertOrUpdate(@NonNull WorkerGroupDto workerGroupDto) {
        if (workerGroupDto.getId() == 0) {
            return insert(workerGroupDto);
        } else {
            return update(workerGroupDto);
        }
    }

    @Override
    public int insert(@NonNull WorkerGroupDto workerGroupDto) {
        WorkerGroup workerGroup = workerGroupDto.transformToDo();
        return workerGroupMapper.insert(workerGroup);
    }

    @Override
    public int update(@NonNull WorkerGroupDto workerGroupDto) {
        WorkerGroup workerGroupDo = workerGroupDto.transformToDo();
        return workerGroupMapper.updateById(workerGroupDo);
    }

    @Override
    public int deleteById(@NonNull Integer workerGroupId) {
        return workerGroupMapper.deleteById(workerGroupId);
    }

}
