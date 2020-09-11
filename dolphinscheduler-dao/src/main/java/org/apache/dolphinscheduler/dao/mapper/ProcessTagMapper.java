package org.apache.dolphinscheduler.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.dolphinscheduler.dao.entity.ProcessTag;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProcessTagMapper extends BaseMapper<ProcessTag> {
    /**
     * delte process tag relation
     * @param processID processId
     * @param tagID tagId
     * @return delete result
     */
    int deleteProcessRelation(@Param("processID") int processID,
                              @Param("tagID") int tagID);
}
