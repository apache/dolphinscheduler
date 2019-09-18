package cn.escheduler.dao.mapper;

import cn.escheduler.dao.entity.ProjectUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

public interface ProjectUserMapper extends BaseMapper<ProjectUser> {

    int deleteProjectRelation(@Param("projectId") int projectId,
                              @Param("userId") int userId);

    ProjectUser queryProjectRelation(@Param("projectId") int projectId,
                                     @Param("userId") int userId);
}
