package org.apache.dolphinscheduler.api.audit.operator.impl;

import org.apache.dolphinscheduler.api.audit.enums.AuditType;
import org.apache.dolphinscheduler.api.audit.operator.BaseOperator;
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
public class TokenOperatorImpl extends BaseOperator {

    @Autowired
    private AccessTokenMapper accessTokenMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void modifyAuditOperationType(AuditType auditType, Map<String, Object> paramsMap,
                                         List<AuditLog> auditLogList) {
        if (paramsMap.get("userId") != null) {
            User user = userMapper.selectById(paramsMap.get("userId").toString());
            auditLogList.forEach(auditLog -> {
                auditLog.setObjectName(user.getUserName());
                auditLog.setObjectId(Long.valueOf(user.getId()));
            });
        }
    }

    @Override
    public String getObjectNameFromReturnIdentity(Object identity) {
        AccessToken obj = accessTokenMapper.selectById(Long.parseLong(identity.toString()));
        return obj == null ? "" : obj.getUserName();
    }
}
