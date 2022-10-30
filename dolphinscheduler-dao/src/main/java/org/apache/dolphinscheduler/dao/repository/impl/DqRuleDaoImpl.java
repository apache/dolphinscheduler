package org.apache.dolphinscheduler.dao.repository.impl;

import org.apache.dolphinscheduler.dao.entity.DqRule;
import org.apache.dolphinscheduler.dao.mapper.DqRuleMapper;
import org.apache.dolphinscheduler.dao.repository.DqRuleDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class DqRuleDaoImpl implements DqRuleDao {

    @Autowired
    DqRuleMapper dqRuleMapper;

    @Override
    public DqRule findRuleById(Integer ruleId) {
        return dqRuleMapper.selectById(ruleId);
    }
}
