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

package org.apache.dolphinscheduler.dao.entity;

import org.apache.dolphinscheduler.dao.entity.UdfFunc.UdfFuncDeserializer;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UdfFuncTest {

    /**
     * test UdfFuncDeserializer.deserializeKey
     *
     * @throws IOException
     */
    @Test
    public void testUdfFuncDeserializer() throws IOException {

        // UdfFuncDeserializer.deserializeKey key is null
        UdfFuncDeserializer udfFuncDeserializer = new UdfFuncDeserializer();
        Assertions.assertNull(udfFuncDeserializer.deserializeKey(null, null));

        //
        UdfFunc udfFunc = new UdfFunc();
        udfFunc.setResourceName("dolphin_resource_update");
        udfFunc.setResourceId(2);
        udfFunc.setClassName("org.apache.dolphinscheduler.test.mrUpdate");

        Assertions.assertNotNull(udfFuncDeserializer.deserializeKey(udfFunc.toString(), null));
    }

}
