package org.apache.dolphinscheduler.dao.repository.impl;

import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.TASK_INSTANCE_ID;

import org.apache.dolphinscheduler.dao.entity.DqTaskStatisticsValue;
import org.apache.dolphinscheduler.dao.mapper.DqTaskStatisticsValueMapper;
import org.apache.dolphinscheduler.dao.repository.DqTaskStatisticsValueDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@Component
public class DqTaskStatisticsValueDaoImpl implements DqTaskStatisticsValueDao {

    @Autowired
    DqTaskStatisticsValueMapper dqTaskStatisticsValueMapper;

    @Override
    public int deleteTaskStatisticsValueByTaskInstanceId(int taskInstanceId) {
        return dqTaskStatisticsValueMapper.delete(
                new QueryWrapper<DqTaskStatisticsValue>()
                        .eq(TASK_INSTANCE_ID, taskInstanceId));
    }
}
