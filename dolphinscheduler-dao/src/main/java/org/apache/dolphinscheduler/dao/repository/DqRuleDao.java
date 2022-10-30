package org.apache.dolphinscheduler.dao.repository;

import org.apache.dolphinscheduler.dao.entity.DqRule;

public interface DqRuleDao {

    DqRule findRuleById(Integer ruleId);
}
