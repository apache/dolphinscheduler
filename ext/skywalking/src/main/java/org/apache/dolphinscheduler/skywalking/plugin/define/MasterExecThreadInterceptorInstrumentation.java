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

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.returns;
import static org.apache.skywalking.apm.agent.core.plugin.match.NameMatch.byName;

/**
 * Enhance {@link org.apache.dolphinscheduler.server.master.runner.MasterExecThread} instance and intercept `run`  and `getProcessInstanceState` methods,
 * the `run` method is a unified entrance of scheduled job.
 *
 * @see org.apache.dolphinscheduler.skywalking.plugin.MasterExecThreadConstructorInterceptor
 * @see org.apache.dolphinscheduler.skywalking.plugin.MasterExecThreadMethodInterceptor
 * @see org.apache.dolphinscheduler.skywalking.plugin.MasterExecThreadStateCacheInterceptor
 */
public class MasterExecThreadInterceptorInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {
    private static final String CONSTRUCTOR_INTERCEPTOR_CLASS = "org.apache.dolphinscheduler.skywalking.plugin.MasterExecThreadConstructorInterceptor";
    private static final String EXEC_PROCESS_METHOD_INTERCEPTOR_CLASS = "org.apache.dolphinscheduler.skywalking.plugin.MasterExecThreadMethodInterceptor";
    private static final String CACHE_STATE_METHOD_INTERCEPTOR_CLASS = "org.apache.dolphinscheduler.skywalking.plugin.MasterExecThreadStateCacheInterceptor";
    private static final String ENHANC_CLASS = "org.apache.dolphinscheduler.server.master.runner.MasterExecThread";

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
                        return takesArgument(0, named("org.apache.dolphinscheduler.dao.entity.ProcessInstance"));
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
                        return EXEC_PROCESS_METHOD_INTERCEPTOR_CLASS;
                    }

                    @Override
                    public boolean isOverrideArgs() {
                        return false;
                    }
                },
                new InstanceMethodsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return named("getProcessInstanceState")
                                .and(returns(named("org.apache.dolphinscheduler.common.enums.ExecutionStatus")));
                    }

                    @Override
                    public String getMethodsInterceptor() {
                        return CACHE_STATE_METHOD_INTERCEPTOR_CLASS;
                    }

                    @Override
                    public boolean isOverrideArgs() {
                        return false;
                    }
                }
        };
    }
}
