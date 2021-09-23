package org.apache.dolphinscheduler.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * (TaskGroup)实体类
 *
 * @author makejava
 * @since 2021-08-07 14:27:07
 */
@TableName("t_ds_task_group")
public class TaskGroup implements Serializable {
    /**
     * key
     */
    @TableId(value="id", type= IdType.AUTO)
    private Integer id;
    /**
     * task_group name
     */
    private String name;

    private String description;
    /**
     * 作业组大小
     */
    private Integer groupSize;
    /**
     * 已使用作业组大小
     */
    private Integer useSize;
    /**
     * creator id
     */
    private Integer userId;
    /**
     * 0 not available, 1 available
     */
    private Integer status;
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getGroupSize() {
        return groupSize;
    }

    public void setGroupSize(Integer groupSize) {
        this.groupSize = groupSize;
    }

    public Integer getUseSize() {
        return useSize;
    }

    public void setUseSize(Integer useSize) {
        this.useSize = useSize;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
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
        return "TaskGroup{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", groupSize=" + groupSize +
                ", useSize=" + useSize +
                ", userId=" + userId +
                ", status=" + status +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }

    @Override
    public int hashCode() {
        Integer result = id;
        result = 31 * result + userId;
        result = 31 * result + status;
        result = 31 * result + useSize;
        result = 31 * result + groupSize;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (updateTime != null ? updateTime.hashCode() : 0);
        return result;
    }

    public TaskGroup(Integer id, String name, String description, Integer groupSize, Integer userId) {
        this.name = name;
        this.description = description;
        this.groupSize = groupSize;
        this.userId = userId;
        init();

    }

    public TaskGroup() {
        init();
    }

    public void init() {
        this.status = 1;
        this.useSize = 0;
    }
}
