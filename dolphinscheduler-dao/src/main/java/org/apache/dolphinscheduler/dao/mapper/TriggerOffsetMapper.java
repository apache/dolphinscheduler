package org.apache.dolphinscheduler.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.dolphinscheduler.dao.entity.*;
import org.apache.ibatis.annotations.Param;

public interface TriggerOffsetMapper extends BaseMapper<TriggerOffset> {

    /**
     * query trigger definition by code
     *
     * @param code taskDefinitionCode
     * @return trigger definition
     */
    TriggerOffset queryByCode(@Param("code") long code);
}
