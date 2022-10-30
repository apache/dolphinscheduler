package org.apache.dolphinscheduler.dao.repository.impl;

import org.apache.dolphinscheduler.dao.entity.DqRuleExecuteSql;
import org.apache.dolphinscheduler.dao.mapper.DqRuleExecuteSqlMapper;
import org.apache.dolphinscheduler.dao.repository.DqRuleExecuteSqlDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DqRuleExecuteSqlDaoImpl implements DqRuleExecuteSqlDao{

    @Autowired
    DqRuleExecuteSqlMapper dqRuleExecuteSqlMapper;

    @Override
    public List<DqRuleExecuteSql> getDqExecuteSql(int ruleId) {
        return dqRuleExecuteSqlMapper.getExecuteSqlList(ruleId);
    }
}
