package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.dao.entity.ListenerPluginInstance;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author wxn
 * @date 2023/7/10
 */
public interface ListenerPluginInstanceMapper extends BaseMapper<ListenerPluginInstance> {
    Boolean existInstanceName(@Param("instanceName") String instanceName);
}
