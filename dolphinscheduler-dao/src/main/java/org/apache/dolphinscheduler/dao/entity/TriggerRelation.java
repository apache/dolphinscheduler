package org.apache.dolphinscheduler.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import lombok.Data;

@Data
@TableName("t_ds_trigger_relation")
public class TriggerRelation {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * trigger code
     */
    private long triggerCode;

    /**
     * triggerType
     */
    private int triggerType;

    /**
     * jobId
     */
    private Integer jobId;

    /**
     * create time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Date createTime;

    /**
     * update time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Date updateTime;

    @Override
    public String toString() {
        return "TriggerRelation{"
            + "id=" + id
            + ", triggerCode='" + triggerCode + '\''
            + ", triggerType=" + triggerType
            + ", jobId=" + jobId
            + ", createTime=" + createTime
            + ", updateTime=" + updateTime
            + '}';
    }
}
