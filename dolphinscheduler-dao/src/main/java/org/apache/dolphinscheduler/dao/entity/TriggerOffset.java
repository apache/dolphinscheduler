package org.apache.dolphinscheduler.dao.entity;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName("t_ds_trigger_offset")
public class TriggerOffset {
    /**
     * id
     */
    private Integer id;

    /**
     * code
     */
    private long code;

    public TriggerOffset() {
    }

    public TriggerOffset(long code) {
        this.code = code;
    }
}
