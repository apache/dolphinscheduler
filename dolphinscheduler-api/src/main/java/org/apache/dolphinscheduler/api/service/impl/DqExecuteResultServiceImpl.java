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

package org.apache.dolphinscheduler.api.service.impl;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.DqExecuteResultService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.DqExecuteResult;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.DqExecuteResultMapper;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * DqExecuteResultServiceImpl
 */
@Service
public class DqExecuteResultServiceImpl extends BaseServiceImpl implements DqExecuteResultService {

    @Autowired
    private DqExecuteResultMapper dqExecuteResultMapper;

    @Override
    public Result queryResultListPaging(User loginUser,
                                        String searchVal,
                                        Integer state,
                                        Integer ruleType,
                                        String startTime,
                                        String endTime,
                                        Integer pageNo,
                                        Integer pageSize) {

        Result result = new Result();
        int[] statusArray = null;
        // filter by state
        if (state != null) {
            statusArray = new int[]{state};
        }

        Date start = null;
        Date end = null;
        try {
            if (StringUtils.isNotEmpty(startTime)) {
                start = DateUtils.getScheduleDate(startTime);
            }
            if (StringUtils.isNotEmpty(endTime)) {
                end = DateUtils.getScheduleDate(endTime);
            }
        } catch (Exception e) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, "startTime,endTime");
            return result;
        }

        Page<DqExecuteResult> page = new Page<>(pageNo, pageSize);
        PageInfo<DqExecuteResult> pageInfo = new PageInfo<>(pageNo, pageSize);

        if (ruleType == null) {
            ruleType = -1;
        }

        IPage<DqExecuteResult> dqsResultPage =
                dqExecuteResultMapper.queryResultListPaging(
                        page,
                        searchVal,
                        loginUser.getId(),
                        statusArray,
                        ruleType,
                        start,
                        end);

        pageInfo.setTotal((int) dqsResultPage.getTotal());
        pageInfo.setTotalList(dqsResultPage.getRecords());
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }
}
