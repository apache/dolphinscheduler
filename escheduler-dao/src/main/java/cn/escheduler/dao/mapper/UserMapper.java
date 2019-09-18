package cn.escheduler.dao.mapper;

import cn.escheduler.dao.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper extends BaseMapper<User> {

    List<User> queryAllGeneralUser();

    User queryByUserNameAccurately(@Param("userName") String userName);

    User queryUserByNamePassword(@Param("userName") String userName, @Param("password") String password);


    IPage<User> queryUserPaging(Page page,
                                @Param("userName") String userName);

    User getDetailsById(@Param("userId") String userId);

    List<User> queryUserListByAlertGroupId(@Param("alertgroupId") int alertgroupId);


    User queryTenantCodeByUserId(@Param("userId") int userId);

    User queryUserByToken(@Param("token") String token);

}
