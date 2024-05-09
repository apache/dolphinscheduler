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

package org.apache.dolphinscheduler.extract.base.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class ServerMethodInvokerImpl implements ServerMethodInvoker {

    private final Object serviceBean;

    private final Method method;

    private final String methodIdentify;

    ServerMethodInvokerImpl(Object serviceBean, Method method) {
        this.serviceBean = serviceBean;
        this.method = method;
        this.methodIdentify = method.toGenericString();
    }

    @Override
    public Object invoke(Object... args) throws Throwable {
        // todo: check the request param when register
        try {
            return method.invoke(serviceBean, args);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }

    @Override
    public String getMethodIdentify() {
        return methodIdentify;
    }

    @Override
    public String getMethodProviderIdentify() {
        return serviceBean.getClass().getName();
    }
}
