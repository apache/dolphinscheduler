package cn.escheduler.dao.mapper;

import cn.escheduler.dao.entity.WorkerGroup;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WorkerGroupMapper extends BaseMapper<WorkerGroup> {

    List<WorkerGroup> queryAllWorkerGroup();

    List<WorkerGroup> queryWorkerGroupByName(@Param("name") String name);

    IPage<WorkerGroup> queryListPaging(IPage<WorkerGroup> page,
                                       @Param("searchVal") String searchVal);

}

