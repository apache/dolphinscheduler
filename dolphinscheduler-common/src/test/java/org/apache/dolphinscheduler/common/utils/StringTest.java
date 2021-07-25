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
package org.apache.dolphinscheduler.common.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class StringTest {

    @Test
    public void stringCompareTest(){

        for(int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            int size = 10000;

            List<String> taskList = new ArrayList<>(size);

            //init
            for (int i = 0; i < size; i++) {
                taskList.add(String.format("%d_%010d_%010d", 1, i, i + 1));
            }

            String origin = taskList.get(0);
            for (int i = 1; i < taskList.size(); i++) {
                String str = taskList.get(i);
                int result = str.compareTo(origin);
                if (result < 0) {
                    origin = str;
                }
            }
            double during = (System.currentTimeMillis() - start) / 1000.0;
            System.out.println(during);
            Assert.assertEquals("1_0000000000_0000000001", origin);
        }
    }
}
