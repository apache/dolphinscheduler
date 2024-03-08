package org.apache.dolphinscheduler.api.audit.operator.impl;

import org.apache.dolphinscheduler.api.audit.operator.BaseOperator;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import org.apache.dolphinscheduler.dao.mapper.UdfFuncMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UdfFunctionOperatorImpl extends BaseOperator {

    @Autowired
    private UdfFuncMapper udfFuncMapper;

    @Override
    protected String getObjectNameFromReturnIdentity(Object identity) {
        UdfFunc obj = udfFuncMapper.selectUdfById((int) identity);
        return obj == null ? "" : obj.getFuncName();
    }

}
