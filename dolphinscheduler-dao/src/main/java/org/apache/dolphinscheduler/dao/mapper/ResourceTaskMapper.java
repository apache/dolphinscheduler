package org.apache.dolphinscheduler.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.dolphinscheduler.dao.entity.ResourcesTask;
import org.apache.dolphinscheduler.spi.enums.ResourceType;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * resource task relation mapper interface
 */
public interface ResourceTaskMapper extends BaseMapper<ResourcesTask> {
    Integer existResourceByFullName(@Param("fullName") String fullName,
                                          @Param("type")ResourceType type);

    int deleteIds(@Param("resIds")Integer[] resIds);

    List<Integer> selectBatchFullNames(@Param("fullNameArr") String[] fullNameArr,
                                             @Param("type")ResourceType type);
}
