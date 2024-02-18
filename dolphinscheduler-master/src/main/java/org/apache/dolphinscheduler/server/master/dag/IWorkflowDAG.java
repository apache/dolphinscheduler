package org.apache.dolphinscheduler.server.master.dag;

import java.util.List;

public interface IWorkflowDAG {

    /**
     * Return the post task name of given parentTaskName.
     *
     * @param parentTaskName parent task name, can be null.
     * @return post task name list, sort by priority.
     */
    List<String> getPostNodeNames(String parentTaskName);

    /**
     * Get the pre task name of given taskName
     *
     * @param taskName task name can be null.
     * @return parent task name list.
     */
    List<String> getParentNodeNames(String taskName);

}
