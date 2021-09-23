package org.apache.dolphinscheduler.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.dolphinscheduler.dao.entity.AuditLog;
import org.apache.ibatis.annotations.Param;
import java.util.Date;

/**
 * auditlog mapper interface
 */
public interface AuditLogMapper extends BaseMapper<AuditLog> {
    IPage<AuditLog> queryAuditLog(IPage<AuditLog> page,
                                  @Param("moduleType") int[] moduleType,
                                  @Param("operationType") int[] operationType,
                                  @Param("userName") String userName,
                                  @Param("projectName") String projectName,
                                  @Param("processName") String processName,
                                  @Param("startDate") Date startDate,
                                  @Param("endDate") Date endDate);
}
