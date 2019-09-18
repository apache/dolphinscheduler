package cn.escheduler.dao.mapper;

import cn.escheduler.common.enums.AlertType;
import cn.escheduler.dao.entity.AlertGroup;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface AlertGroupMapper extends BaseMapper<AlertGroup> {


    IPage<AlertGroup> queryAlertGroupPage(Page page,
                                          @Param("groupName") String groupName);


    List<AlertGroup> queryByGroupName(@Param("groupName") String groupName);


    List<AlertGroup> queryByUserId(@Param("userId") int userId);


    List<AlertGroup> queryByAlertType(@Param("alertType") AlertType alertType);

    List<AlertGroup> queryAllGroupList();
}
