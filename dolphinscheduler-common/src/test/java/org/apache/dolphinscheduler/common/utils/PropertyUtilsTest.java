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

import static com.google.common.truth.Truth.assertThat;

import org.apache.dolphinscheduler.common.constants.Constants;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Sets;

public class PropertyUtilsTest {

    @Test
    public void getString() {
        Assertions.assertNotNull(PropertyUtils.getString(Constants.FS_DEFAULT_FS));
    }

    @Test
    public void getSet() {
        Set<String> networkInterface = PropertyUtils.getSet("networkInterface", value -> {
            if (StringUtils.isEmpty(value)) {
                return Collections.emptySet();
            }
            return Arrays.stream(value.split(",")).map(String::trim).collect(Collectors.toSet());
        }, Sets.newHashSet("docker0"));
        Assertions.assertEquals(Sets.newHashSet("docker0"), networkInterface);
    }

    @Test
    void getByPrefix() {
        Map<String, String> awsProperties = PropertyUtils.getByPrefix("resource.aws.", "");
        assertThat(awsProperties).containsEntry("access.key.id", "minioadmin");
        assertThat(awsProperties).containsEntry("secret.access.key", "minioadmin");
        assertThat(awsProperties).containsEntry("region", "cn-north-1");
        assertThat(awsProperties).containsEntry("s3.bucket.name", "dolphinscheduler");
        assertThat(awsProperties).containsEntry("endpoint", "http://localhost:9000");
    }
}
