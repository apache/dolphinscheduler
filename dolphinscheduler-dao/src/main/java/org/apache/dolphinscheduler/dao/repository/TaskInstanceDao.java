package org.apache.dolphinscheduler.dao.repository;

import lombok.NonNull;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.exception.RepositoryException;

public interface TaskInstanceDao {

    /**
     * Update the taskInstance, if update failed will throw exception.
     *
     * @param taskInstance need to update
     */
    void updateTaskInstance(@NonNull TaskInstance taskInstance) throws RepositoryException;

    /**
     * Update the taskInstance, if update success will return true, else return true.
     * <p>
     * This method will never throw exception.
     *
     * @param taskInstance need to update
     */
    boolean updateTaskInstanceSafely(@NonNull TaskInstance taskInstance);
}
