package org.apache.dolphinscheduler.api.audit.operator.impl;

import org.apache.dolphinscheduler.api.audit.operator.BaseOperator;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProjectOperatorImpl extends BaseOperator {

    @Autowired
    private ProjectMapper projectMapper;

    @Override
    protected String getObjectNameFromReturnIdentity(Object identity) {
        Project obj = projectMapper.queryByCode((Long) identity);
        return obj == null ? "" : obj.getName();
    }
}
