package cn.escheduler.dao.mapper;

import cn.escheduler.dao.entity.UdfFunc;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UdfFuncMapper extends BaseMapper<UdfFunc> {


    List<UdfFunc> queryUdfByIdStr(@Param("ids") String ids,
                                  @Param("funcNames") String funcNames);

    IPage<UdfFunc> queryUdfFuncPaging(IPage<UdfFunc> page,
                                      @Param("userId") int userId,
                                      @Param("searchVal") String searchVal);

    List<UdfFunc> getUdfFuncByType(@Param("userId") int userId,
                                   @Param("type") Integer type);

    List<UdfFunc> queryUdfFuncExceptUserId(@Param("userId") int userId);

    List<UdfFunc> queryAuthedUdfFunc(@Param("userId") int userId);


}
