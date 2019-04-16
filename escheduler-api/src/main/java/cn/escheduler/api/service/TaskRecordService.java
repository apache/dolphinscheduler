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
package cn.escheduler.api.service;

import cn.escheduler.api.enums.Status;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.api.utils.PageInfo;
import cn.escheduler.dao.TaskRecordDao;
import cn.escheduler.dao.model.TaskRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.escheduler.common.Constants.*;

/**
 * task record service
 */
@Service
public class TaskRecordService extends BaseService{

    private static final Logger logger = LoggerFactory.getLogger(TaskRecordService.class);

    /**
     * query task record list paging
     *
     * @param taskName
     * @param startDate
     * @param taskDate
     * @param sourceTable
     * @param destTable
     * @param endDate
     * @param state
     * @param pageNo
     * @param pageSize
     * @return
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
