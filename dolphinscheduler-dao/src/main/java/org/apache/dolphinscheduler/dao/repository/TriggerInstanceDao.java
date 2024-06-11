package org.apache.dolphinscheduler.dao.repository;

import org.apache.dolphinscheduler.dao.entity.TriggerInstance;

/**
 * Trigger Instance DAO
 */
public interface TriggerInstanceDao extends IDao<TriggerInstance> {
    TriggerInstance queryByCode(long triggerCode);
}
