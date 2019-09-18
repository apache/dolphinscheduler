package cn.escheduler.dao.mapper;

import cn.escheduler.common.enums.AlertStatus;
import cn.escheduler.dao.entity.Alert;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AlertMapper extends BaseMapper<Alert> {


    List<Alert> listAlertByStatus(@Param("alertStatus") AlertStatus alertStatus);

}
