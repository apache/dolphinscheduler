/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.api.dto.auditLog;

import org.apache.dolphinscheduler.common.enums.AuditModelType;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class AuditModelTypeDto {

    private String name;

    private List<AuditModelTypeDto> child = null;

    public static List<AuditModelTypeDto> getModelTypeDtoList() {
        List<AuditModelTypeDto> dtoList = new ArrayList<>();
        transFromEnumListToDto(dtoList, AuditModelType.getAuditModelTreeList());
        return dtoList;
    }

    public static List<AuditModelTypeDto> transFromEnumListToDto(List<AuditModelTypeDto> dtoList,
                                                                 List<AuditModelType> objectTypeList) {
        for (AuditModelType operationType : objectTypeList) {
            dtoList.add(transFromEnumToDto(operationType));
        }

        return dtoList;
    }

    public static AuditModelTypeDto transFromEnumToDto(AuditModelType operationType) {
        AuditModelTypeDto dto = new AuditModelTypeDto();
        dto.setName(operationType.getName());

        if (!operationType.getChild().isEmpty()) {
            dto.setChild(transFromEnumListToDto(new ArrayList<>(), operationType.getChild()));
        }

        return dto;
    }
}
