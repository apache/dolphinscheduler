package cn.escheduler.dao.mapper;

import cn.escheduler.dao.entity.DataSource;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataSourceMapper extends BaseMapper<DataSource> {

    List<DataSource> queryDataSourceByType(@Param("userId") int userId, @Param("type") Integer type);

    IPage<DataSource> selectPaging(IPage<DataSource> page,
                                   @Param("userId") int userId,
                                   @Param("name") String name);

    DataSource queryDataSourceByName(@Param("name") String name);


    List<DataSource> queryAuthedDatasource(@Param("userId") int userId);

    List<DataSource> queryDatasourceExceptUserId(@Param("userId") int userId);

    List<DataSource> listAllDataSourceByType(@Param("type") Integer type);


}
