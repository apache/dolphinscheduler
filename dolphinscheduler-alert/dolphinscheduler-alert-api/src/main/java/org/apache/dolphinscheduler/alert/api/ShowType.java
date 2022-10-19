/*
 * Licensed to Apache Software Foundation (ASF) under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Apache Software Foundation (ASF) licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.dolphinscheduler.alert.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ShowType {

    /**
     * 0 TABLE;
     * 1 TEXT;
     * 2 attachment;
     * 3 TABLE+attachment;
     * 4 MARKDOWN;
     */
    TABLE(0, "table"),
    TEXT(1, "text"),
    ATTACHMENT(2, "attachment"),
    TABLE_ATTACHMENT(3, "table attachment"),
    MARKDOWN(4, "markdown"),
    ;

    private final int code;

    private final String descp;

}
