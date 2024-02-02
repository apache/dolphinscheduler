package org.apache.dolphinscheduler.api.dto.auditLog;

import lombok.Data;
import org.apache.dolphinscheduler.common.enums.AuditObjectType;
import org.apache.dolphinscheduler.common.enums.AuditOperationType;
import org.apache.dolphinscheduler.dao.model.WorkflowDefinitionCountDto;

import java.util.ArrayList;
import java.util.List;

@Data
public class AuditObjectTypeDto {
    private int code;

    private String name;

    private List<AuditObjectTypeDto> child = new ArrayList<>();

    public static List<AuditObjectTypeDto> getlist() {
        List<AuditObjectTypeDto> dtoList = new ArrayList<>();
        trans(dtoList, AuditObjectType.getAuditObjectTreeList());
        return dtoList;
    }

    public static List<AuditObjectTypeDto> trans(List<AuditObjectTypeDto> dtoList, List<AuditObjectType> objectTypeList) {
        for (AuditObjectType operationType: objectTypeList) {
            dtoList.add(transToDto(operationType));
        }

        return dtoList;
    }

    public static AuditObjectTypeDto transToDto(AuditObjectType operationType) {
        AuditObjectTypeDto dto = new AuditObjectTypeDto();
        dto.setName(operationType.getName());
        dto.setCode(operationType.getCode());
        if(!operationType.getChild().isEmpty()) {
            dto.setChild(trans(new ArrayList<>(), operationType.getChild()));
        }
        return dto;
    }
}
