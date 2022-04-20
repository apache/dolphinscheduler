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

package org.apache.dolphinscheduler.server.worker.verify;

import org.apache.dolphinscheduler.common.storage.StorageOperate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Server start verify
 */
@Component
public class WorkerServerStartVerify {

    /**
     * Storage
     */
    @Autowired(required = false)
    private StorageOperate storageOperate;

    /**
     * verify
     */
    public void verify() {
        if(Objects.isNull(storageOperate)){
            throw new RuntimeException("Worker startup verifies that StorageOperate is empty!");
        }
    }
}
