
package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.dao.entity.DefinedParam;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public interface DefinedParamMapper extends BaseMapper<DefinedParam> {
    List<DefinedParam> queryDefinedParambyKeys(@Param("keys") List<String> var1);
}
