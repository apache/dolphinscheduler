package org.apache.dolphinscheduler.dao.repository;

import org.apache.dolphinscheduler.dao.entity.DqRuleExecuteSql;

import java.util.List;

public interface DqRuleExecuteSqlDao {

    List<DqRuleExecuteSql> getDqExecuteSql(int ruleId);
}
