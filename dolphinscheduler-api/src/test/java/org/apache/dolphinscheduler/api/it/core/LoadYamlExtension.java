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

package org.apache.dolphinscheduler.api.it.core;

import java.util.Arrays;

import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * 在 @BeforeEach（基类清表+加载 base fixture）之后、测试方法之前，
 * 加载方法上 @LoadYaml 指定的 YAML 文件。
 *
 * 之所以选择 BeforeTestExecutionCallback：
 *   JUnit5 生命周期 = @BeforeEach -> BeforeTestExecutionCallback -> @Test -> AfterTestExecutionCallback -> @AfterEach
 * 这样基类 setUpData() 先执行清表+base fixture，再加载方法专属 YAML，叠加正确。
 */
public class LoadYamlExtension implements BeforeTestExecutionCallback {

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        LoadYaml ann = context.getRequiredTestMethod().getAnnotation(LoadYaml.class);
        if (ann == null || ann.value().length == 0) {
            return;
        }

        YamlDataLoader loader = SpringExtension
                .getApplicationContext(context)
                .getBean(YamlDataLoader.class);

        loader.load(Arrays.asList(ann.value()));
    }
}
