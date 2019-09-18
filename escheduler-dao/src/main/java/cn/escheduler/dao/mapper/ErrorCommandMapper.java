package cn.escheduler.dao.mapper;

import cn.escheduler.common.enums.UserType;
import cn.escheduler.dao.entity.CommandCount;
import cn.escheduler.dao.entity.ErrorCommand;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface ErrorCommandMapper extends BaseMapper<ErrorCommand> {

    List<CommandCount> countCommandState(
            @Param("userId") int userId,
            @Param("userType") UserType userType,
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("projectId") int projectId);
}
