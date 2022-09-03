package org.apache.dolphinscheduler.plugin.task.api;

import java.util.Set;

public abstract class AbstractRemoteTask extends AbstractTask {



    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
    protected AbstractRemoteTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
    }

    @Override
    public void cancel() throws TaskException {
        this.cancelApplication();
    }

    public abstract Set<String> getApplicationIds() throws TaskException;

    public abstract void cancelApplication() throws TaskException;
}
