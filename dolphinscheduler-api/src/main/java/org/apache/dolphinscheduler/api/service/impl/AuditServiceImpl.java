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

import org.apache.dolphinscheduler.api.audit.AuditMessage;
import org.apache.dolphinscheduler.api.audit.AuditPublishService;
import org.apache.dolphinscheduler.api.dto.AuditDto;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.AuditService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.AuditModuleType;
import org.apache.dolphinscheduler.common.enums.AuditOperationType;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.dao.entity.AuditLog;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AuditLogMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Service
public class AuditServiceImpl extends BaseServiceImpl implements AuditService {

    @Autowired
    private AuditLogMapper auditLogMapper;

    @Autowired
    private AuditPublishService publishService;

    /**
     * add new audit log
     *
     * @param user          login user
     * @param module        module type
     * @param operation     operation type
     */
    @Override
    public void addAudit(User user, AuditModuleType module, AuditOperationType operation, String projectName, String processName) {
        publishService.publish(new AuditMessage(user, new Date(), module, operation, projectName, processName));
    }

    /**
     * query audit log paging
     *
     * @param loginUser         login user
     * @param moduleType        module type
     * @param operationType     operation type
     * @param startDate         start time
     * @param endDate           end time
     * @param userName          query user name
     * @param projectName       project name
     * @param processName       process name
     * @param pageNo            page number
     * @param pageSize          page size
     * @return  audit log string data
     */
    @Override
    public Result queryLogListPaging(User loginUser, AuditModuleType moduleType,
                                     AuditOperationType operationType, String startDate,
                                     String endDate, String userName,
                                     String projectName, String processName,
                                     Integer pageNo, Integer pageSize) {
        Result result = new Result();

        Map<String, Object> checkAndParseDateResult = checkAndParseDateParameters(startDate, endDate);
        Status resultEnum = (Status) checkAndParseDateResult.get(Constants.STATUS);
        if (resultEnum != Status.SUCCESS) {
            putMsg(result,resultEnum);
            return result;
        }

        int[] moduleArray = null;
        if (moduleType != null) {
            moduleArray = new int[]{moduleType.getCode()};
        }

        int[] opsArray = null;
        if (operationType != null) {
            opsArray = new int[]{operationType.getCode()};
        }

        Date start = (Date) checkAndParseDateResult.get(Constants.START_TIME);
        Date end = (Date) checkAndParseDateResult.get(Constants.END_TIME);

        Page<AuditLog> page = new Page<>(pageNo, pageSize);
        IPage<AuditLog> logIPage = auditLogMapper.queryAuditLog(page, moduleArray, opsArray, userName, projectName, processName, start, end);
        List<AuditLog> logList = logIPage.getRecords();
        PageInfo<AuditDto> pageInfo = new PageInfo<>(pageNo, pageSize);
        if (CollectionUtils.isEmpty(logList)) {
            pageInfo.setTotal(0);
            pageInfo.setTotalList(new ArrayList<>());
            result.setData(pageInfo);
            putMsg(result, Status.SUCCESS);
            return result;
        }
        List<AuditDto> auditDtos = CollectionUtils.transformToList(logList,
            auditLog -> transformAuditLog(auditLog));
        pageInfo.setTotal(auditDtos.size());
        pageInfo.setTotalList(auditDtos);
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * transform AuditLog to AuditDto
     *
     * @param auditLog audit log
     * @return audit dto
     */
    private AuditDto transformAuditLog(AuditLog auditLog) {
        AuditDto auditDto = new AuditDto();
        auditDto.setUserName(auditLog.getUserName());
        auditDto.setModule(AuditModuleType.of(auditLog.getModule()).getMsg());
        auditDto.setOperation(AuditOperationType.of(auditLog.getOperation()).getMsg());
        auditDto.setTime(auditLog.getTime());
        auditDto.setProcessName(auditLog.getProcessName());
        auditDto.setProjectName(auditLog.getProjectName());
        return auditDto;
    }
}
