package org.apache.dolphinscheduler.api.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author wxn
 * @date 2023/8/4
 */
@Data
public class ListenerInstanceVO {
    /**
     * id
     */
    private Integer id;

    /**
     * plugin_define_id
     */
    private int pluginDefineId;

    /**
     * listener_plugin_name
     */
    private String listenerPluginName;

    /**
     * alert plugin instance name
     */
    private String instanceName;

    /**
     * plugin_instance_params
     */
    private String pluginInstanceParams;

    /**
     * listener_event_type
     */
    private List<String> listenerEventTypes;

    /**
     * create_time
     */
    private Date createTime;

    /**
     * update_time
     */
    private Date updateTime;
}
