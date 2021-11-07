/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.dolphinscheduler.skywalking.plugin.define;

import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;
import org.apache.skywalking.apm.agent.core.plugin.match.HierarchyMatch;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * Enhance {@link org.apache.dolphinscheduler.spi.task.AbstractTask} instance and intercept {@code init} and
 * {@code handle} methods, the implementation class execute task through these methods.
 *
 * @see org.apache.dolphinscheduler.skywalking.plugin.TaskExecuteConstructorInterceptor
 */
public class TaskExecuteInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {

    @Override
    protected ClassMatch enhanceClass() {
        return HierarchyMatch.byHierarchyMatch("org.apache.dolphinscheduler.spi.task.AbstractTask");
    }

    @Override
    public ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return new ConstructorInterceptPoint[]{
            new ConstructorInterceptPoint() {
                @Override
                public ElementMatcher<MethodDescription> getConstructorMatcher() {
                    return takesArgument(0, named("org.apache.dolphinscheduler.spi.task.request.TaskRequest"));
                }

                @Override
                public String getConstructorInterceptor() {
                    return "org.apache.dolphinscheduler.skywalking.plugin.TaskExecuteConstructorInterceptor";
                }
            }
        };
    }

    @Override
    public InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[]{
            new InstanceMethodsInterceptPoint() {
                @Override
                public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return named("handle")
                        .or(named("init"))
                        .and(isPublic())
                        .and(takesArguments(0));
                }

                @Override
                public String getMethodsInterceptor() {
                    return "org.apache.dolphinscheduler.skywalking.plugin.TaskExecuteMethodInterceptor";
                }

                @Override
                public boolean isOverrideArgs() {
                    return false;
                }
            }
        };
    }
}
