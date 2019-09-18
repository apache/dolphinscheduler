package cn.escheduler.dao.mapper;

import cn.escheduler.dao.entity.Session;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SessionMapper extends BaseMapper<Session> {

    List<Session> queryByUserId(@Param("userId") int userId);

}
