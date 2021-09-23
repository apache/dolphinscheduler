package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.AuditModuleType;
import org.apache.dolphinscheduler.common.enums.AuditOperationType;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Map;

/**
 * audit information service
 */
public interface AuditService {

    /**
     *
     * @param user login user
     * @param module module type
     * @param operation operation type
     * @param projectName project name
     * @param processName process name
     */
    void addAudit(User user, AuditModuleType module, AuditOperationType operation,
                  String projectName, String processName);

    /**
     * query audit log list
     *
     * @param loginUser         login user
     * @param moduleType        module type
     * @param operationType     operation type
     * @param startTime         start time
     * @param endTime           end time
     * @param userName          query user name
     * @param projectName
     * @param processName
     * @param pageNo            page number
     * @param pageSize          page size
     * @return                  audit log string
     */
    Result queryLogListPaging(User loginUser, AuditModuleType moduleType,
                              AuditOperationType operationType, String startTime,
                              String endTime, String userName,
                              String projectName, String processName,
                              Integer pageNo, Integer pageSize);
}
