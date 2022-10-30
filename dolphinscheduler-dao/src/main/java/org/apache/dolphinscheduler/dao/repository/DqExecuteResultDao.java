package org.apache.dolphinscheduler.dao.repository;

import org.apache.dolphinscheduler.dao.entity.DqExecuteResult;

public interface DqExecuteResultDao {

    DqExecuteResult getDqExecuteResultByTaskInstanceId(int taskInstanceId);

    int updateById(DqExecuteResult dqExecuteResult);

    int deleteDqExecuteResultByTaskInstanceId(int taskInstanceId);

    int updateDqExecuteResultUserId(int taskInstanceId);
}
