package org.apache.dolphinscheduler.dao.repository;

import org.apache.dolphinscheduler.dao.entity.DqComparisonType;

public interface DqComparisonTypeDao {

    DqComparisonType selectById(int id);
}
