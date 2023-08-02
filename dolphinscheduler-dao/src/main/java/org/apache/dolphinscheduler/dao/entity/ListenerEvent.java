package org.apache.dolphinscheduler.dao.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.apache.dolphinscheduler.listener.enums.ListenerEventPostStatus;
import org.apache.dolphinscheduler.listener.enums.ListenerEventType;

/**
 * @author wxn
 * @date 2023/7/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_ds_listener_event")
public class ListenerEvent {

    /**
     * primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * title
     */
    @TableField(value = "title")
    private String title;

    /**
     * sign
     */
    @TableField(value = "sign")
    private String sign;

    /**
     * content
     */
    @TableField(value = "content")
    private String content;

    /**
     * alert_status
     */
    @TableField(value = "event_type")
    private ListenerEventType eventType;

    /**
     * log
     */
    @TableField(value = "log")
    private String log;

    /**
     * alertgroup_id
     */
    @TableField("plugin_instance_id")
    private Integer pluginInstanceId;

    /**
     * post_status
     */
    @TableField("post_status")
    private ListenerEventPostStatus postStatus;

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

    @TableField(exist = false)
    private Map<String, String> params = new HashMap<>();
}
