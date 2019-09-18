package cn.escheduler.dao.mapper;

import cn.escheduler.dao.entity.ProcessInstanceMap;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

public interface ProcessInstanceMapMapper extends BaseMapper<ProcessInstanceMap> {



    ProcessInstanceMap queryByParentId(@Param("parentProcessId") int parentProcessId, @Param("parentTaskId") int parentTaskId);


    ProcessInstanceMap queryBySubProcessId(@Param("subProcessId") Integer subProcessId);

    int deleteByParentProcessId(@Param("parentProcessId") int parentProcessId);

}
