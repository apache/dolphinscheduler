package cn.escheduler.dao.mapper;

import cn.escheduler.dao.entity.Command;
import cn.escheduler.dao.entity.CommandCount;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

public interface CommandMapper extends BaseMapper<Command> {



    @Select("select * from t_escheduler_command ${ew.customSqlSegment}")
    List<Command> getAll(@Param(Constants.WRAPPER) Wrapper wrapper);

    Command getOneToRun();

    List<CommandCount> countCommandState(
            @Param("userId") int userId,
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("projectIdString") String projectIdString);



}
