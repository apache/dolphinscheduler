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

import org.apache.dolphinscheduler.api.dto.AuditDto;
import org.apache.dolphinscheduler.api.service.AuditService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.enums.Audit.AuditObjectType;
import org.apache.dolphinscheduler.common.enums.Audit.AuditOperationType;
import org.apache.dolphinscheduler.dao.entity.AuditLog;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AuditLogMapper;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Service
public class AuditServiceImpl extends BaseServiceImpl implements AuditService {

    @Autowired
    private AuditLogMapper auditLogMapper;

    @Override
    public void addAudit(AuditLog auditLog) {
        auditLogMapper.insert(auditLog);
    }

    @Override
    public void addQuartzLog(int processId) {
        AuditLog auditLog = new AuditLog();
        auditLog.setObjectId(processId);
        auditLog.setObjectType(AuditObjectType.WORKFLOW.getCode());
        auditLog.setOperationType(AuditOperationType.SCHEDULE_RUN.getCode());
        auditLog.setTime(new Date());
        auditLog.setUserId(-1);
        auditLogMapper.insert(auditLog);
    }

    /**
     * query audit log paging
     *
     * @param loginUser         login user
     * @param resourceType      resource type
     * @param operationType     operation type
     * @param startDate         start time
     * @param endDate           end time
     * @param userName          query user name
     * @param pageNo            page number
     * @param pageSize          page size
     * @return audit log string data
     */
    @Override
    public PageInfo<AuditDto> queryLogListPaging(User loginUser,
                                                 AuditObjectType resourceType,
                                                 AuditOperationType operationType,
                                                 String startDate,
                                                 String endDate,
                                                 String userName,
                                                 Integer pageNo,
                                                 Integer pageSize) {

        int[] resourceArray = null;
        if (resourceType != null) {
            resourceArray = new int[]{resourceType.getCode()};
        }

        int[] opsArray = null;
        if (operationType != null) {
            opsArray = new int[]{operationType.getCode()};
        }

        Date start = checkAndParseDateParameters(startDate);
        Date end = checkAndParseDateParameters(endDate);

        IPage<AuditLog> logIPage = auditLogMapper.queryAuditLog(new Page<>(pageNo, pageSize), resourceArray, opsArray,
                userName, start, end);
        List<AuditDto> auditDtos =
                logIPage.getRecords().stream().map(this::transformAuditLog).collect(Collectors.toList());

        PageInfo<AuditDto> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotal((int) auditDtos.size());
        pageInfo.setTotalList(auditDtos);
        return pageInfo;
    }

    /**
     * transform AuditLog to AuditDto
     *
     * @param auditLog audit log
     * @return audit dto
     */
    private AuditDto transformAuditLog(AuditLog auditLog) {
        AuditDto auditDto = new AuditDto();
        String resourceType = AuditObjectType.of(auditLog.getObjectType()).getName();
        auditDto.setResource(resourceType);
        auditDto.setOperation(AuditOperationType.of(auditLog.getOperationType()).getName());
        auditDto.setUserName(auditLog.getUserName());
        auditDto.setResourceName(auditLogMapper.queryResourceNameByType(resourceType, auditLog.getObjectId()));
        auditDto.setTime(auditLog.getTime());
        return auditDto;
    }
}
