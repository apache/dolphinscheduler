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

package org.apache.dolphinscheduler.api.audit.operator.impl;

import org.apache.dolphinscheduler.api.audit.constants.AuditLogConstants;
import org.apache.dolphinscheduler.api.audit.enums.AuditType;
import org.apache.dolphinscheduler.api.audit.operator.BaseAuditOperator;
import org.apache.dolphinscheduler.dao.entity.AccessToken;
import org.apache.dolphinscheduler.dao.entity.AuditLog;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AccessTokenMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TokenAuditOperatorImpl extends BaseAuditOperator {

    @Autowired
    private AccessTokenMapper accessTokenMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void modifyAuditOperationType(AuditType auditType, Map<String, Object> paramsMap,
                                         List<AuditLog> auditLogList) {
        if (paramsMap.get(AuditLogConstants.USER_ID) != null) {
            User user = userMapper.selectById(paramsMap.get(AuditLogConstants.USER_ID).toString());
            auditLogList.forEach(auditLog -> {
                auditLog.setModelName(user.getUserName());
                auditLog.setModelId(Long.valueOf(user.getId()));
            });
        }
    }

    @Override
    public String getObjectNameFromIdentity(Object identity) {
        Long objId = toLong(identity);
        if (objId == -1) {
            return "";
        }

        AccessToken obj = accessTokenMapper.selectById(objId);
        return obj == null ? "" : obj.getUserName();
    }
}
