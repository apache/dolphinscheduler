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

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;

import static net.bytebuddy.matcher.ElementMatchers.takesArgument;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static org.apache.skywalking.apm.agent.core.plugin.match.NameMatch.byName;

/**
 * Enhance {@link org.apache.dolphinscheduler.server.worker.runner.TaskExecuteThread} instance and intercept `run` method,
 * this class provides an isolated thread for running task.
 *
 * @see org.apache.dolphinscheduler.skywalking.plugin.TaskExecuteThreadConstructorInterceptor
 * @see org.apache.dolphinscheduler.skywalking.plugin.TaskExecuteThreadMethodInterceptor
 */
public class TaskExecuteThreadInterceptorInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {
    private static final String CONSTRUCTOR_INTERCEPTOR_CLASS = "org.apache.dolphinscheduler.skywalking.plugin.TaskExecuteThreadConstructorInterceptor";
    private static final String METHOD_INTERCEPTOR_CLASS = "org.apache.dolphinscheduler.skywalking.plugin.TaskExecuteThreadMethodInterceptor";
    private static final String ENHANC_CLASS = "org.apache.dolphinscheduler.server.worker.runner.TaskExecuteThread";

    @Override
    protected ClassMatch enhanceClass() {
        return byName(ENHANC_CLASS);
    }

    @Override
    public ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return new ConstructorInterceptPoint[] {
                new ConstructorInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getConstructorMatcher() {
                        return takesArgument(0, named("org.apache.dolphinscheduler.server.entity.TaskExecutionContext"));
                    }

                    @Override
                    public String getConstructorInterceptor() {
                        return CONSTRUCTOR_INTERCEPTOR_CLASS;
                    }
                }
        };
    }

    @Override
    public InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[] {
                new InstanceMethodsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return named("run")
                                .and(isPublic())
                                .and(takesArguments(0));
                    }

                    @Override
                    public String getMethodsInterceptor() {
                        return METHOD_INTERCEPTOR_CLASS;
                    }

                    @Override
                    public boolean isOverrideArgs() {
                        return false;
                    }
                }
        };
    }
}
