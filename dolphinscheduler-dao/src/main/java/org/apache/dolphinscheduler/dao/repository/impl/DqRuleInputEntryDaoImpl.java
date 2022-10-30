package org.apache.dolphinscheduler.dao.repository.impl;

import org.apache.dolphinscheduler.dao.entity.DqRuleInputEntry;
import org.apache.dolphinscheduler.dao.mapper.DqRuleInputEntryMapper;
import org.apache.dolphinscheduler.dao.repository.DqRuleInputEntryDao;
import org.apache.dolphinscheduler.dao.utils.DqRuleUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class DqRuleInputEntryDaoImpl implements DqRuleInputEntryDao {

    @Autowired
    private DqRuleInputEntryMapper dqRuleInputEntryMapper;
    
    @Override
    public List<DqRuleInputEntry> getRuleInputEntry(int ruleId) {
        return DqRuleUtils.transformInputEntry(dqRuleInputEntryMapper.getRuleInputEntryList(ruleId));
    }
}
