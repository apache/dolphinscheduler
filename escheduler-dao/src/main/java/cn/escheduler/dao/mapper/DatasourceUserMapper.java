package cn.escheduler.dao.mapper;

import cn.escheduler.dao.entity.DatasourceUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

public interface DataSourceUserMapper extends BaseMapper<DatasourceUser> {


    int deleteByUserId(@Param("userId") int userId);

    int deleteByDatasourceId(@Param("datasourceId") int datasourceId);


}
