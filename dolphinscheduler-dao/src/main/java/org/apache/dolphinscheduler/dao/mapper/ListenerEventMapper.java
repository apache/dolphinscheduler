package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.dao.entity.ListenerEvent;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wxn
 * @date 2023/7/10
 */
public interface ListenerEventMapper extends BaseMapper<ListenerEvent> {
    int batchInsert(@Param("listenerEvents") List<ListenerEvent> listenerEvents);
}
