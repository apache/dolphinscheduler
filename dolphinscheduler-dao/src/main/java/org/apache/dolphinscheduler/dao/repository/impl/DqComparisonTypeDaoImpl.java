package org.apache.dolphinscheduler.dao.repository.impl;

import org.apache.dolphinscheduler.dao.entity.DqComparisonType;
import org.apache.dolphinscheduler.dao.mapper.DqComparisonTypeMapper;
import org.apache.dolphinscheduler.dao.repository.DqComparisonTypeDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DqComparisonTypeDaoImpl implements DqComparisonTypeDao {

    @Autowired
    DqComparisonTypeMapper dqComparisonTypeMapper;

    @Override
    public DqComparisonType selectById(int id) {
        return dqComparisonTypeMapper.selectById(id);
    }
}
