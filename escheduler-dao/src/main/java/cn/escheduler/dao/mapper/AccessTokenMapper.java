package cn.escheduler.dao.mapper;

import cn.escheduler.dao.entity.AccessToken;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

public interface AccessTokenMapper extends BaseMapper<AccessToken> {



    IPage<AccessToken> selectAccessTokenPage(Page page,
                                             @Param("userName") String userName,
                                             @Param("userId") int userId
    );
}
