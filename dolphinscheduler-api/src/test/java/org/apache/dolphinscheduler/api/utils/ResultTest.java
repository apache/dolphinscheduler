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
package org.apache.dolphinscheduler.api.utils;

import org.apache.dolphinscheduler.api.enums.Status;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class ResultTest {

    @Test
    public void success() {
        HashMap<String, String> map = new HashMap<>();
        map.put("testdata", "test");
        Result ret = Result.success(map);
        Assert.assertEquals(Status.SUCCESS.getCode(), ret.getCode().intValue());
    }

    @Test
    public void error() {
        Result ret = Result.error(Status.ACCESS_TOKEN_NOT_EXIST);
        Assert.assertEquals(Status.ACCESS_TOKEN_NOT_EXIST.getCode(), ret.getCode().intValue());
    }

    @Test
    public void errorWithArgs() {
        Result ret = Result.errorWithArgs(Status.INTERNAL_SERVER_ERROR_ARGS, "test internal server error");
        Assert.assertEquals(Status.INTERNAL_SERVER_ERROR_ARGS.getCode(), ret.getCode().intValue());
    }
}