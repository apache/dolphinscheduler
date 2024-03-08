package org.apache.dolphinscheduler.api.audit.operator.impl;

import org.apache.dolphinscheduler.api.audit.operator.BaseOperator;
import org.apache.dolphinscheduler.dao.entity.K8sNamespace;
import org.apache.dolphinscheduler.dao.mapper.K8sNamespaceMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class K8sNamespaceOperatorImpl extends BaseOperator {

    @Autowired
    private K8sNamespaceMapper k8sNamespaceMapper;

    @Override
    public String getObjectNameFromReturnIdentity(Object identity) {
        K8sNamespace obj = k8sNamespaceMapper.selectById(Long.parseLong(identity.toString()));
        return obj == null ? "" : obj.getNamespace();
    }
}
