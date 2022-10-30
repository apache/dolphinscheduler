package org.apache.dolphinscheduler.dao.repository;

import org.apache.dolphinscheduler.dao.entity.DqRuleInputEntry;

import java.util.List;

public interface DqRuleInputEntryDao {

    List<DqRuleInputEntry> getRuleInputEntry(int ruleId);
}
