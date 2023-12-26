package org.apache.dolphinscheduler.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.WarningType;

import java.util.Date;

@Data
@TableName("t_ds_triggers")
public class Trigger {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * process definition code
     */
    private long processDefinitionCode;

    /**
     * process definition name
     */
    @TableField(exist = false)
    private String processDefinitionName;

    /**
     * project name
     */
    @TableField(exist = false)
    private String projectName;

    /**
     * schedule description
     */
    @TableField(exist = false)
    private String definitionDescription;

    /**
     * trigger type
     */
    private String triggerType;

    /**
     * schedule start time
     */
    private Date startTime;

    /**
     * schedule end time
     */
    private Date endTime;

    /**
     * timezoneId
     * <p>see {@link java.util.TimeZone#getTimeZone(String)}
     */
    private String timezoneId;

    /**
     * crontab expression
     */
    private String crontab;

    /**
     * failure strategy
     */
    private FailureStrategy failureStrategy;

    /**
     * warning type
     */
    private WarningType warningType;

    /**
     * create time
     */
    private Date createTime;

    /**
     * update time
     */
    private Date updateTime;

    /**
     * created user id
     */
    private int userId;

    /**
     * created user name
     */
    @TableField(exist = false)
    private String userName;

    /**
     * release state
     */
    private ReleaseState releaseState;

    /**
     * warning group id
     */
    private int warningGroupId;

    /**
     * process instance priority
     */
    private Priority processInstancePriority;

    /**
     *  worker group
     */
    private String workerGroup;

    /**
     * tenant code
     */
    private String tenantCode;

    /**
     * environment code
     */
    private Long environmentCode;

    /**
     * environment name
     */
    @TableField(exist = false)
    private String environmentName;
}
