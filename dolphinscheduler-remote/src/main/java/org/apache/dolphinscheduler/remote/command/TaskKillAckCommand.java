package org.apache.dolphinscheduler.remote.command;

import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.io.Serializable;

public class TaskKillAckCommand implements Serializable {

    private int taskInstanceId;
    private int status;

    public TaskKillAckCommand() {
        super();
    }

    public TaskKillAckCommand(int status, int taskInstanceId) {
        this.status = status;
        this.taskInstanceId = taskInstanceId;
    }

    public int getTaskInstanceId() {
        return taskInstanceId;
    }

    public void setTaskInstanceId(int taskInstanceId) {
        this.taskInstanceId = taskInstanceId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * package response command
     *
     * @return command
     */
    public Command convert2Command() {
        Command command = new Command();
        command.setType(CommandType.TASK_KILL_RESPONSE_ACK);
        byte[] body = JSONUtils.toJsonByteArray(this);
        command.setBody(body);
        return command;
    }

    @Override
    public String toString() {
        return "KillTaskAckCommand{" + "taskInstanceId=" + taskInstanceId + ", status=" + status + '}';
    }
}
