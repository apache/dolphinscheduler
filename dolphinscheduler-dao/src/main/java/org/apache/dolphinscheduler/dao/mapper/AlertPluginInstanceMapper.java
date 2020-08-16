package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public interface AlertPluginInstanceMapper extends BaseMapper<AlertPluginInstance> {

    /**
     * query all alert plugin instance
     *
     * @return AlertPluginInstance list
     */
    List<AlertPluginInstance> queryAllAlertPluginInstanceList();

    /**
     * query by alert group id
     *
     * @param alertGroupId
     * @return AlertPluginInstance list
     */
    List<AlertPluginInstance> queryByAlertGroupId(@Param("alertGroupId") int alertGroupId);
}
