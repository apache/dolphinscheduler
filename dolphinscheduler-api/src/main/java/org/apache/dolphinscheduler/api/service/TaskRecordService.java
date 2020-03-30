/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.TaskRecordDao;
import org.apache.dolphinscheduler.dao.entity.TaskRecord;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.dolphinscheduler.common.Constants.*;

/**
 * task record service
 */
@Service
public class TaskRecordService extends BaseService{

    /**
     * query task record list paging
     *
     * @param taskName task name
     * @param state state
     * @param sourceTable source table
     * @param destTable destination table
     * @param taskDate task date
     * @param startDate start time
     * @param endDate end time
     * @param pageNo page numbere
     * @param pageSize page size
     * @param isHistory is history
     * @return task record list
     */
    public Map<String,Object> queryTaskRecordListPaging(boolean isHistory, String taskName, String startDate,
                                                        String taskDate, String sourceTable,
                                                        String destTable, String endDate,
                                                        String state, Integer pageNo, Integer pageSize) {
        Map<String, Object> result = new HashMap<>(10);
        PageInfo pageInfo = new PageInfo<TaskRecord>(pageNo, pageSize);

        Map<String, String> map = new HashMap<>(10);
        map.put("taskName", taskName);
        map.put("taskDate", taskDate);
        map.put("state", state);
        map.put("sourceTable", sourceTable);
        map.put("targetTable", destTable);
        map.put("startTime", startDate);
        map.put("endTime", endDate);
        map.put("offset", pageInfo.getStart().toString());
        map.put("pageSize", pageInfo.getPageSize().toString());

        String table = isHistory ? TASK_RECORD_TABLE_HISTORY_HIVE_LOG : TASK_RECORD_TABLE_HIVE_LOG;
        int count = TaskRecordDao.countTaskRecord(map, table);
        List<TaskRecord> recordList = TaskRecordDao.queryAllTaskRecord(map, table);
        pageInfo.setTotalCount(count);
        pageInfo.setLists(recordList);
        result.put(Constants.DATA_LIST, pageInfo);
        putMsg(result, Status.SUCCESS);

        return result;

    }
}
