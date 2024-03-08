package org.apache.dolphinscheduler.api.audit.operator.impl;

import org.apache.dolphinscheduler.api.audit.operator.BaseOperator;
import org.apache.dolphinscheduler.dao.entity.Cluster;
import org.apache.dolphinscheduler.dao.mapper.ClusterMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClusterOperatorImpl extends BaseOperator {

    @Autowired
    private ClusterMapper clusterMapper;

    @Override
    public String getObjectNameFromReturnIdentity(Object identity) {
        Cluster obj = clusterMapper.queryByClusterCode((long) identity);
        return obj == null ? "" : obj.getName();
    }
}
