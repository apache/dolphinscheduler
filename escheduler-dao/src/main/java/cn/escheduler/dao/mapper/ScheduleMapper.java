package cn.escheduler.dao.mapper;

import cn.escheduler.dao.entity.Schedule;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ScheduleMapper extends BaseMapper<Schedule> {


    IPage<Schedule> queryByProcessDefineIdPaging(IPage<Schedule> page,
                                                 @Param("processDefinitionId") int processDefinitionId,
                                                 @Param("searchVal") String searchVal);
    List<Schedule> querySchedulerListByProjectName(@Param("projectName") String projectName);


    List<Schedule> selectAllByProcessDefineArray(@Param("processDefineIds") String processDefineIds);

    List<Schedule> queryByProcessDefinitionId(@Param("processDefinitionId") int processDefinitionId);

}
