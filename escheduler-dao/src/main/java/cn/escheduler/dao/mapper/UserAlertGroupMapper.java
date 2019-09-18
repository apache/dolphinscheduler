package cn.escheduler.dao.mapper;

import cn.escheduler.dao.entity.User;
import cn.escheduler.dao.entity.UserAlertGroup;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserAlertGroupMapper extends BaseMapper<UserAlertGroup> {

    List<User> queryForUser(@Param("alertgroupId") int alertgroupId);

    int deleteByAlertgroupId(@Param("alertgroupId") int alertgroupId);

    List<User> listUserByAlertgroupId(@Param("alertgroupId") int alertgroupId);

}
