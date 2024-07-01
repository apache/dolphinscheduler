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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;

class CodeGenerateUtilsTest {

    @Test
    void testNoGenerateDuplicateCode() {
        int codeNum = 10000000;
        List<Long> existsCode = new ArrayList<>();
        for (int i = 0; i < codeNum; i++) {
            Long currentCode = CodeGenerateUtils.genCode();
            existsCode.add(currentCode);
        }
        Set<Long> existsCodeSet = new HashSet<>(existsCode);
        // Disallow duplicate code
        assertEquals(existsCode.size(), existsCodeSet.size());
    }

    @Test
    void testNoGenerateDuplicateCodeWithDifferentAppName() throws UnknownHostException, InterruptedException {
        int threadNum = 10;
        int codeNum = 1000000;

        final String hostName = InetAddress.getLocalHost().getHostName();
        Map<String, List<Long>> machineCodes = new ConcurrentHashMap<>();
        CountDownLatch countDownLatch = new CountDownLatch(threadNum);

        for (int i = 0; i < threadNum; i++) {
            final int c = i;
            new Thread(() -> {
                List<Long> codes = new ArrayList<>(codeNum);
                CodeGenerateUtils.CodeGenerator codeGenerator = new CodeGenerateUtils.CodeGenerator(hostName + "-" + c);
                for (int j = 0; j < codeNum; j++) {
                    codes.add(codeGenerator.genCode());
                }
                machineCodes.put(Thread.currentThread().getName(), codes);
                countDownLatch.countDown();
            }).start();
        }
        countDownLatch.await();
        Set<Long> totalCodes = new HashSet<>();
        machineCodes.values().forEach(totalCodes::addAll);
        assertEquals(codeNum * threadNum, totalCodes.size());
    }
}
