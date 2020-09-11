package org.apache.dolphinscheduler.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * ProcessTag
 */
@TableName("t_ds_relation_process_tag")
public class ProcessTag {

    /**
     * id
     */
    @TableId(value="id", type= IdType.AUTO)
    private int id;

    @TableField("process_id")
    private int processID;

    @TableField("tag_id")
    private int tagID;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int gettagID() {
        return tagID;
    }

    public void settagID(int userId) {
        this.tagID = tagID;
    }

    public int getProcessID() {
        return processID;
    }

    public void setProcessID(int processID) {
        this.processID = processID;
    }
    @Override
    public String toString() {
        return "ProcessTag{" +
                "id=" + id +
                ", processID=" + processID +
                ", tagID=" + tagID +
                '}';
    }
}
