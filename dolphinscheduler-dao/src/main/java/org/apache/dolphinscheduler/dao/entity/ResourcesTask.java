package org.apache.dolphinscheduler.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

@TableName("t_ds_relation_resources_task")

public class ResourcesTask {

    @TableId(value = "id", type = IdType.AUTO)
    private int id;

    private String fullName;

    private ResourceType type;

    public void setId(int id) {
        this.id = id;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setType(ResourceType type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public ResourceType getType() {
        return type;
    }

    public ResourcesTask(String fullName, ResourceType type) {
//        this.id = id;
        this.fullName = fullName;
        this.type = type;
    }

    @Override
    public String toString() {
        return "ResourcesTask{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", type=" + type +
                '}';
    }
}
