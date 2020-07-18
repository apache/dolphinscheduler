package org.apache.dolphinscheduler.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PluginDefineMapper extends BaseMapper<PluginDefine> {

    /**
     * query all plugin define
     * @return PluginDefine list
     */
    List<PluginDefine> queryAllPluginDefineList();

    /**
     * query by plugin type
     * @param pluginType pluginType
     * @return PluginDefine list
     */
    List<PluginDefine> queryByPluginType(@Param("pluginType") String pluginType);

    /**
     * query by name and type
     * @param pluginName
     * @param pluginType
     * @return
     */
    List<PluginDefine> queryByNameAndType(@Param("pluginName") String pluginName, @Param("pluginType") String pluginType);
}
