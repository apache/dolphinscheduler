package org.apache.dolphinscheduler.dao.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * @author wxn
 * @date 2023/7/10
 */
@Data
@TableName("t_ds_listener_plugin_instance")
public class ListenerPluginInstance {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * plugin_define_id
     */
    @TableField(value = "plugin_define_id", updateStrategy = FieldStrategy.NEVER)
    private int pluginDefineId;

    /**
     * alert plugin instance name
     */
    @TableField("instance_name")
    private String instanceName;

    /**
     * plugin_instance_params
     */
    @TableField("plugin_instance_params")
    private String pluginInstanceParams;

    /**
     * listener_event_type
     */
    @TableField("listener_event_types")
    private String listenerEventTypes;

    /**
     * create_time
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * update_time
     */
    @TableField("update_time")
    private Date updateTime;

}
