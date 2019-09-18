package cn.escheduler.dao.mapper;

import cn.escheduler.dao.entity.Project;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProjectMapper extends BaseMapper<Project> {

    Project queryDetailById(@Param("projectId") int projectId);

    Project queryProjectByName(@Param("projectName") String projectName);

    IPage<Project> queryProjectListPaging(IPage<Project> page,
                                          @Param("userId") int userId,
                                          @Param("searchName") String searchName);

    IPage<Project> queryAllProjectListPaging(IPage<Project> page,
                                             @Param("searchName") String searchName);

    List<Integer> queryProjectCreatedByUser(@Param("userId") int userId);

    List<Project> queryAuthedProjectListByUserId(@Param("userId") int userId);

    List<Project> queryProjectExceptUserId(@Param("userId") int userId);

}
