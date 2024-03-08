package org.apache.dolphinscheduler.api.audit.operator.impl;

import org.apache.dolphinscheduler.api.audit.operator.BaseOperator;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TenantOperatorImpl extends BaseOperator {

    @Autowired
    private TenantMapper tenantMapper;

    @Override
    public String getObjectNameFromReturnIdentity(Object identity) {
        Tenant obj = tenantMapper.selectById(Long.parseLong(identity.toString()));
        return obj == null ? "" : obj.getTenantCode();
    }
}
