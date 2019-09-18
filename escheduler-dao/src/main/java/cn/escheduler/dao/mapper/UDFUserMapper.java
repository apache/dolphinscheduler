package cn.escheduler.dao.mapper;

import cn.escheduler.dao.entity.UDFUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

public interface UDFUserMapper extends BaseMapper<UDFUser> {

    int deleteByUserId(@Param("userId") int userId);

    int deleteByUdfFuncId(@Param("udfFuncId") int udfFuncId);

}

