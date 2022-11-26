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

import io.netty.util.internal.ReflectionUtil;
import org.apache.dolphinscheduler.common.enums.ProcessExecutionTypeEnum;
import org.apache.dolphinscheduler.common.utils.OkHttpUtils;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.util.ReflectionUtils;

import static org.apache.dolphinscheduler.common.enums.ProcessExecutionTypeEnum.PARALLEL;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ProcessDefinitionDemoTest {
    
    @Mock
    ProjectMapper projectMapper;
    
    @Mock
    UserMapper userMapper;
    
    @Mock
    ProxyProcessDefinitionController proxyProcessDefinitionController;
    
    @InjectMocks
    ProcessDefinitionDemo processDefinitionDemo;
    
    @Test
    void swicthDemo() {
        Mockito.doReturn(ProxyResult.success("ok"))
                .when(proxyProcessDefinitionController)
                .createProcessDefinition(
                        Mockito.anyString(),
                        Mockito.anyLong(),
                        Mockito.anyString(),
                        Mockito.anyString(),
                        Mockito.anyString(),
                        eq(null),
                        Mockito.anyInt(),
                        Mockito.anyString(),
                        Mockito.anyString(),
                        Mockito.anyString(),
                        Mockito.any()
                );
        ProxyResult proxyResult = processDefinitionDemo.swicthDemo("test", 1L, "test");
        Assertions.assertNotNull(proxyResult);
        Assertions.assertEquals("ok", proxyResult.getData());
    }
    
    @Test
    void parameterContextDemo() {
        Mockito.doReturn(ProxyResult.success("ok"))
                .when(proxyProcessDefinitionController)
                .createProcessDefinition(
                        Mockito.anyString(),
                        Mockito.anyLong(),
                        Mockito.anyString(),
                        Mockito.anyString(),
                        Mockito.anyString(),
                        eq(null),
                        Mockito.anyInt(),
                        Mockito.anyString(),
                        Mockito.anyString(),
                        Mockito.anyString(),
                        Mockito.any()
                );
        ProxyResult proxyResult = processDefinitionDemo.parameterContextDemo("test", 1L, "test");
        Assertions.assertNotNull(proxyResult);
        Assertions.assertEquals("ok", proxyResult.getData());
    }
    
    @Test
    void shellDemo() {
        Mockito.doReturn(ProxyResult.success("ok"))
                .when(proxyProcessDefinitionController)
                .createProcessDefinition(
                        Mockito.anyString(),
                        Mockito.anyLong(),
                        Mockito.anyString(),
                        Mockito.anyString(),
                        Mockito.anyString(),
                        eq(null),
                        Mockito.anyInt(),
                        Mockito.anyString(),
                        Mockito.anyString(),
                        Mockito.anyString(),
                        Mockito.any()
                );
        ProxyResult proxyResult = processDefinitionDemo.shellDemo("test", 1L, "test");
        Assertions.assertNotNull(proxyResult);
        Assertions.assertEquals("ok", proxyResult.getData());
    }
}