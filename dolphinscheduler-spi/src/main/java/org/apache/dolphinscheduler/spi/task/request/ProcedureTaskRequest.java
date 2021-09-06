package org.apache.dolphinscheduler.spi.task.request;

/**
 * Procedure Task ExecutionContext
 * to master/worker task transport
 */
public class ProcedureTaskRequest extends TaskRequest {

    /**
     * connectionParams
     */
    private String connectionParams;

    public String getConnectionParams() {
        return connectionParams;
    }

    public void setConnectionParams(String connectionParams) {
        this.connectionParams = connectionParams;
    }

    @Override
    public String toString() {
        return "ProcedureTaskExecutionContext{"
                + "connectionParams='" + connectionParams + '\''
                + '}';
    }
}
