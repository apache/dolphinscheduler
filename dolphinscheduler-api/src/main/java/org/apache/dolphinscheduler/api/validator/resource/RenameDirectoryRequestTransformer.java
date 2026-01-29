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

package org.apache.dolphinscheduler.api.validator.resource;

import org.apache.dolphinscheduler.api.dto.resources.RenameDirectoryDto;
import org.apache.dolphinscheduler.api.dto.resources.RenameDirectoryRequest;
import org.apache.dolphinscheduler.api.validator.ITransformer;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RenameDirectoryRequestTransformer implements ITransformer<RenameDirectoryRequest, RenameDirectoryDto> {

    @Override
    public RenameDirectoryDto transform(RenameDirectoryRequest renameDirectoryRequest) {
        String originDirectoryAbsolutePath = renameDirectoryRequest.getDirectoryAbsolutePath();
        String targetDirectoryName = renameDirectoryRequest.getNewDirectoryName();

        String targetDirectoryAbsolutePath =
                getTargetDirectoryAbsolutePath(originDirectoryAbsolutePath, targetDirectoryName);

        return RenameDirectoryDto.builder()
                .loginUser(renameDirectoryRequest.getLoginUser())
                .originDirectoryAbsolutePath(originDirectoryAbsolutePath)
                .targetDirectoryAbsolutePath(targetDirectoryAbsolutePath)
                .build();
    }

    private String getTargetDirectoryAbsolutePath(String originDirectoryAbsolutePath, String targetDirectoryName) {
        String originDirectoryParentAbsolutePath = StringUtils.substringBeforeLast(
                originDirectoryAbsolutePath, File.separator);
        return originDirectoryParentAbsolutePath + File.separator + targetDirectoryName;
    }
}
