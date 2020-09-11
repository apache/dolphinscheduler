package org.apache.dolphinscheduler.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.dolphinscheduler.dao.entity.Tag;
import org.apache.ibatis.annotations.Param;



public interface TagMapper extends BaseMapper<Tag> {

    /**
     * query tag by name
     * @param tagName tagName
     * @return tag
     */
    Tag queryByName(@Param("tagName")String tagName);

    /**
     * tag page
     * @param page page
     * @param userId userId
     * @param projectId projectId
     * @param searchName searchName
     * @return tag Ipage
     */
    IPage<Tag> queryTagListPaging(IPage<Tag> page,
                                          @Param("userId") int userId,
                                          @Param("projectId") int projectId,
                                          @Param("searchName") String searchName);
}
