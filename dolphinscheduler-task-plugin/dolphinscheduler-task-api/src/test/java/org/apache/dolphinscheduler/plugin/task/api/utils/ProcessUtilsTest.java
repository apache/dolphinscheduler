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

package org.apache.dolphinscheduler.plugin.task.api.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ProcessUtilsTest {

    private static final Pattern LINUXPATTERN = Pattern.compile("\\((\\d+)\\)");

    private String getPidStr(String pids) {
        Matcher mat = null;
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(pids)) {
            mat = LINUXPATTERN.matcher(pids);
        }
        if (null != mat) {
            while (mat.find()) {
                sb.append(mat.group(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    @Test
    public void testGetPidsStr() {
        String pids = "sudo(6279)---558_1497.sh(6282)---sleep(6354)";
        String exceptPidsStr = "6279 6282 6354";
        String actualPidsStr = getPidStr(pids);
        Assertions.assertEquals(exceptPidsStr, actualPidsStr);

        String pids2 = "init(1)---systemd(1000)---(sd-pam)(1001)";
        String exceptPidsStr2 = "1 1000 1001";
        String actualPidsStr2 = getPidStr(pids2);
        Assertions.assertEquals(exceptPidsStr2, actualPidsStr2);

        String pids3 = "sshd(5000)---sshd(6000)---bash(7000)---python(7100)";
        String exceptPidsStr3 = "5000 6000 7000 7100";
        String actualPidsStr3 = getPidStr(pids3);
        Assertions.assertEquals(exceptPidsStr3, actualPidsStr3);

        String pids4 = "apache2(2000)---apache2-submit_task.py(2100)---apache2(2101)";
        String exceptPidsStr4 = "2000 2100 2101";
        String actualPidsStr4 = getPidStr(pids4);
        Assertions.assertEquals(exceptPidsStr4, actualPidsStr4);
    }

}
