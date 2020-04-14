package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.dao.entity.WorkFlowLineage;
import org.apache.dolphinscheduler.dao.entity.WorkFlowRelation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.type.JdbcType;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

public interface WorkFlowLineageMapper {

    public List<WorkFlowLineage> queryByName(@Param("searchVal") String searchVal, @Param("projectId") int projectId);

    public List<WorkFlowLineage> queryByIds(@Param("ids") Set<Integer> ids, @Param("projectId") int projectId);

    public List<WorkFlowRelation> querySourceTarget(@Param("id") int id);
}
