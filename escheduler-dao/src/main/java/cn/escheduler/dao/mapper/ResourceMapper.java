package cn.escheduler.dao.mapper;

import cn.escheduler.dao.entity.Resource;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ResourceMapper extends BaseMapper<Resource> {

    /**
     *
     * @param alias query all if null
     * @param userId query all if -1
     * @param type query all type if -1
     * @return
     */
    List<Resource> queryResourceList(@Param("alias") String alias,
                                     @Param("userId") int userId,
                                     @Param("type") int type);


    /**
     *
     * @param page
     * @param userId query all if -1, then query the authed resources
     * @param type
     * @param searchVal
     * @return
     */
    IPage<Resource> queryResourcePaging(IPage<Resource> page,
                                        @Param("userId") int userId,
                                        @Param("type") int type,
                                        @Param("searchVal") String searchVal);

    /**
     *
     * @param userId
     * @param type
     * @return
     */
    List<Resource> queryResourceListAuthored(@Param("userId") int userId, @Param("type") int type);

    /**
     *
     * @param userId
     * @return
     */
    List<Resource> queryAuthorizedResourceList(@Param("userId") int userId);

    List<Resource> queryResourceExceptUserId(@Param("userId") int userId);


    String queryTenantCodeByResourceName(@Param("resName") String resName);
}
