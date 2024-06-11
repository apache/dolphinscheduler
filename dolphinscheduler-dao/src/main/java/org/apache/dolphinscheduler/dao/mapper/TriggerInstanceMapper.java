package org.apache.dolphinscheduler.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.dao.entity.*;
import org.apache.ibatis.annotations.Param;

import java.util.*;

public interface TriggerInstanceMapper extends BaseMapper<TriggerInstance> {

    /**
     * query trigger definition by code
     *
     * @param code taskDefinitionCode
     * @return trigger definition
     */
    TriggerInstance queryByCode(@Param("code") long code);
}
