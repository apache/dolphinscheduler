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
import org.apache.dolphinscheduler.common.enums.AuditObjectType;
import org.apache.dolphinscheduler.common.enums.AuditOperationType;
import org.apache.dolphinscheduler.dao.entity.AuditLog;
import org.apache.dolphinscheduler.dao.mapper.AuditLogMapper;

import org.apache.parquet.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Service
@Slf4j
public class AuditServiceImpl extends BaseServiceImpl implements AuditService {

    @Autowired
    private AuditLogMapper auditLogMapper;

    @Override
    public void addAudit(AuditLog auditLog) {
        if (auditLog.getObjectId() == null || auditLog.getObjectName() == null) {
            return;
        }

        auditLogMapper.insert(auditLog);
    }

    @Override
    public void addAudit(List<AuditLog> auditLogList, long latency) {
        auditLogList.forEach(auditLog -> {
            auditLog.setLatency(latency);
            addAudit(auditLog);
        });
    }

    /**
     * query audit log paging
     *
     * @param objectTypes         object types
     * @param operationTypes      operation types
     * @param startDate           start time
     * @param endDate             end time
     * @param userName            query user name
     * @param objectName          query object name
     * @param pageNo              page number
     * @param pageSize            page size
     * @return audit log string data
     */
    @Override
    public PageInfo<AuditDto> queryLogListPaging(String objectTypes,
                                                 String operationTypes,
                                                 String startDate,
                                                 String endDate,
                                                 String userName,
                                                 String objectName,
                                                 Integer pageNo,
                                                 Integer pageSize) {
        List<String> objectTypeCodeList = convertStringToList(objectTypes);
        List<String> operationTypeCodeList = convertStringToList(operationTypes);

        Date start = checkAndParseDateParameters(startDate);
        Date end = checkAndParseDateParameters(endDate);

        IPage<AuditLog> logIPage =
                auditLogMapper.queryAuditLog(new Page<>(pageNo, pageSize), objectTypeCodeList, operationTypeCodeList,
                        userName, objectName, start, end);
        List<AuditDto> auditDtos =
                logIPage.getRecords().stream().map(this::transformAuditLog).collect(Collectors.toList());

        PageInfo<AuditDto> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotal((int) logIPage.getTotal());
        pageInfo.setTotalList(auditDtos);
        return pageInfo;
    }

    private List<String> convertStringToList(String string) {
        if (Strings.isNullOrEmpty(string)) {
            return new ArrayList<>();
        }

        return Arrays.stream(string.split(",")).collect(Collectors.toList());
    }

    /**
     * transform AuditLog to AuditDto
     *
     * @param auditLog audit log
     * @return audit dto
     */
    private AuditDto transformAuditLog(AuditLog auditLog) {
        AuditDto auditDto = new AuditDto();
        AuditObjectType objectType = AuditObjectType.of(auditLog.getObjectType());
        auditDto.setObjectType(objectType.getName());
        auditDto.setObjectName(auditLog.getObjectName());
        auditDto.setOperation(AuditOperationType.of(auditLog.getOperationType()).getName());
        auditDto.setUserName(auditLog.getUserName());
        auditDto.setLatency(String.format("%.2f", (double) auditLog.getLatency() / 1000));
        auditDto.setDetail(auditLog.getDetail());
        auditDto.setDescription(auditLog.getDescription());
        auditDto.setTime(auditLog.getTime());
        return auditDto;
    }
}
