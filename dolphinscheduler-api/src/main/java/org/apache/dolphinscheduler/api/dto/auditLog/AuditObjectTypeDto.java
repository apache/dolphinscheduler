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

    private List<AuditObjectTypeDto> child = null;

    public static List<AuditObjectTypeDto> getObjectTypeDtoList() {
        List<AuditObjectTypeDto> dtoList = new ArrayList<>();
        transFromEnumListToDto(dtoList, AuditObjectType.getAuditObjectTreeList());
        return dtoList;
    }

    public static List<AuditObjectTypeDto> transFromEnumListToDto(List<AuditObjectTypeDto> dtoList, List<AuditObjectType> objectTypeList) {
        for (AuditObjectType operationType: objectTypeList) {
            dtoList.add(transFromEnumToDto(operationType));
        }

        return dtoList;
    }

    public static AuditObjectTypeDto transFromEnumToDto(AuditObjectType operationType) {
        AuditObjectTypeDto dto = new AuditObjectTypeDto();
        dto.setName(operationType.getName());
        dto.setCode(operationType.getCode());

        if(!operationType.getChild().isEmpty()) {
            dto.setChild(transFromEnumListToDto(new ArrayList<>(), operationType.getChild()));
        }

        return dto;
    }
}
