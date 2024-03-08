package org.apache.dolphinscheduler.api.audit.operator.impl;

import org.apache.dolphinscheduler.api.audit.operator.BaseOperator;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DatasourceOperatorImpl extends BaseOperator {

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Override
    public String getObjectNameFromReturnIdentity(Object identity) {
        DataSource obj = dataSourceMapper.selectById(Long.parseLong(identity.toString()));
        return obj == null ? "" : obj.getName();
    }
}
