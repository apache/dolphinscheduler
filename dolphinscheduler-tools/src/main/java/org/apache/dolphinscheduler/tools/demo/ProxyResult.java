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

package org.apache.dolphinscheduler.tools.demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProxyResult<T> {

    private Integer code;

    private String msg;

    private T data;

    public static <T> ProxyResult<T> success(T data) {
        return new ProxyResult<>(0, "success", data);
    }

    public static ProxyResult success() {
        return success(null);
    }

    public boolean isSuccess() {
        if (code == 0) {
            return true;
        }
        return false;
    }

    public boolean isFailed() {
        return !this.isSuccess();
    }

}
