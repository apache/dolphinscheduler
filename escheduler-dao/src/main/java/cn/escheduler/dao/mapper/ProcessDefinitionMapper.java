package cn.escheduler.dao.mapper;

import cn.escheduler.common.enums.UserType;
import cn.escheduler.dao.entity.DefinitionGroupByUser;
import cn.escheduler.dao.entity.ProcessDefinition;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProcessDefinitionMapper extends BaseMapper<ProcessDefinition> {


    ProcessDefinition queryByDefineName(@Param("projectId") int projectId,
                                        @Param("processDefinitionName") String name);

    IPage<ProcessDefinition> queryDefineListPaging(IPage<ProcessDefinition> page,
                                                   @Param("searchVal") String searchVal,
                                                   @Param("userId") int userId,
                                                   @Param("projectId") int projectId);

    List<ProcessDefinition> queryAllDefinitionList(@Param("projectId") int projectId);

    List<ProcessDefinition> queryDefinitionListByIdList(@Param("ids") String ids);

    List<DefinitionGroupByUser> countDefinitionGroupByUser(
            @Param("userId") Integer userId,
            @Param("userType") UserType userType,
            @Param("projectIds") String projectIds);

}
