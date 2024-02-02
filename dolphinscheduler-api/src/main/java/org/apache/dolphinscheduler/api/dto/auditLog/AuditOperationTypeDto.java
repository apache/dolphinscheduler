package org.apache.dolphinscheduler.api.dto.auditLog;

import lombok.Data;
import org.apache.dolphinscheduler.common.enums.AuditObjectType;
import org.apache.dolphinscheduler.common.enums.AuditOperationType;

import java.util.ArrayList;
import java.util.List;

@Data
public class AuditOperationTypeDto {
    private int code;

    private String name;

    public static List<AuditOperationTypeDto> getOperationTypeDtoList() {
        List<AuditOperationTypeDto> dtoList = new ArrayList<>();
        for (AuditOperationType operationType: AuditOperationType.getOperationList()) {
            AuditOperationTypeDto dto = new AuditOperationTypeDto();
            dto.setCode(operationType.getCode());
            dto.setName(operationType.getName());
            dtoList.add(dto);
        }

        return dtoList;
    }
}
