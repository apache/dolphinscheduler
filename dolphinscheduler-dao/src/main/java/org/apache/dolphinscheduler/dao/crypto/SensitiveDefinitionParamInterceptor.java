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

package org.apache.dolphinscheduler.dao.crypto;

import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Transparent at-rest protection for definition sensitive params.
 * <p>
 * MyBatis-Plus 3.5.2 {@code InnerInterceptor} only has before-query / before-update hooks,
 * so this uses a MyBatis {@link Executor} plugin: encrypt parameters before write, restore
 * plaintext afterwards, and decrypt query results. Upper layers always see plaintext.
 * Instance tables are not intercepted.
 */
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
})
public class SensitiveDefinitionParamInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (!SensitiveDefinitionParamCodec.isEncryptionEnabled()) {
            return invocation.proceed();
        }
        if ("update".equals(invocation.getMethod().getName())) {
            Object parameter = invocation.getArgs()[1];
            apply(parameter, true);
            try {
                return invocation.proceed();
            } finally {
                apply(parameter, false);
            }
        }
        Object result = invocation.proceed();
        apply(result, false);
        return result;
    }

    void apply(Object value, boolean encrypt) {
        visit(value, encrypt, Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    private void visit(Object value, boolean encrypt, Set<Object> seen) {
        if (value == null || !seen.add(value)) {
            return;
        }
        if (value instanceof WorkflowInstance) {
            return;
        }
        if (value instanceof WorkflowDefinition) {
            WorkflowDefinition definition = (WorkflowDefinition) value;
            definition.setGlobalParams(encrypt
                    ? SensitiveDefinitionParamCodec.encryptGlobalParams(definition.getGlobalParams())
                    : SensitiveDefinitionParamCodec.decryptGlobalParams(definition.getGlobalParams()));
            return;
        }
        if (value instanceof TaskDefinition) {
            TaskDefinition definition = (TaskDefinition) value;
            definition.setTaskParams(encrypt
                    ? SensitiveDefinitionParamCodec.encryptTaskParams(definition.getTaskParams())
                    : SensitiveDefinitionParamCodec.decryptTaskParams(definition.getTaskParams()));
            return;
        }
        if (value instanceof Map<?, ?>) {
            for (Object entryValue : ((Map<?, ?>) value).values()) {
                visit(entryValue, encrypt, seen);
            }
            return;
        }
        if (value instanceof Collection<?>) {
            for (Object item : (Collection<?>) value) {
                visit(item, encrypt, seen);
            }
            return;
        }
        if (value.getClass().isArray() && !value.getClass().getComponentType().isPrimitive()) {
            for (Object item : (Object[]) value) {
                visit(item, encrypt, seen);
            }
        }
    }
}
