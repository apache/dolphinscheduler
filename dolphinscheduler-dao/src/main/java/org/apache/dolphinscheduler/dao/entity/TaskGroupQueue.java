package org.apache.dolphinscheduler.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * (TaskGroupQueue)实体类
 *
 * @author makejava
 * @since 2021-08-07 14:27:13
 */
@TableName("t_ds_task_group_queue")
public class TaskGroupQueue implements Serializable {
    /**
     * key
     */
    @TableId(value="id", type= IdType.AUTO)
    private int id;
    /**
     * taskIntanceid
     */
    private int taskId;
    /**
     * TaskInstance name
     */
    private String taskName;
    /**
     * taskGroup id
     */
    private int groupId;
    /**
     * processInstace id
     */
    private int processId;
    /**
     * the priority of task instance
     */
    private int priority;
    /**
     * -1: waiting  1: running  6: failed  7: finished
     */
    private int status;
    /**
     * create time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createTime;
    /**
     * update time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date updateTime;

    public TaskGroupQueue() {

    }


    public TaskGroupQueue(int id, int taskId, String taskName, int groupId, int processId, int priority, int status) {
        this.id = id;
        this.taskId = taskId;
        this.taskName = taskName;
        this.groupId = groupId;
        this.processId = processId;
        this.priority = priority;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }


    @Override
    public String toString() {
        return "TaskGroupQueue{" +
                "id=" + id +
                ", taskId=" + taskId +
                ", taskName='" + taskName + '\'' +
                ", groupId=" + groupId +
                ", processId=" + processId +
                ", priority=" + priority +
                ", status=" + status +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + taskId;
        result = 31 * result + groupId;
        result = 31 * result + processId;
        result = 31 * result + status;
        result = 31 * result + priority;
        result = 31 * result + (taskName != null ? taskName.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (updateTime != null ? updateTime.hashCode() : 0);
        return result;
    }


}
