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
package org.apache.dolphinscheduler.server.master.dispatch.host.assign;

import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * random selector
 */
public class RandomSelectorTest {

    @Test(expected = IllegalArgumentException.class)
    public void testSelectWithIllegalArgumentException(){
        RandomSelector selector = new RandomSelector();
        selector.select(Collections.EMPTY_LIST);
    }

    @Test
    public void testSelect1(){
        RandomSelector<String> selector = new RandomSelector();
        String result = selector.select(Arrays.asList("1"));
        Assert.assertTrue(StringUtils.isNotEmpty(result));
        Assert.assertTrue(result.equalsIgnoreCase("1"));
    }

    @Test
    public void testSelect(){
        RandomSelector<Integer> selector = new RandomSelector();
        int result = selector.select(Arrays.asList(1,2,3,4,5,6,7));
        Assert.assertTrue(result >= 1 && result <= 7);
    }
}
