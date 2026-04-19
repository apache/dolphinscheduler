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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

@Component
public class TableEntityRegistry {

    private static final String ENTITY_BASE_PACKAGE = "org.apache.dolphinscheduler.dao.entity";

    private final Map<String, Class<?>> tableToEntity = new HashMap<>();
    private final Map<Class<?>, BaseMapper<?>> entityToMapper = new HashMap<>();

    private final ApplicationContext applicationContext;

    public TableEntityRegistry(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    void init() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(TableName.class));

        scanner.findCandidateComponents(ENTITY_BASE_PACKAGE).forEach(bd -> {
            try {
                Class<?> entityClass = Class.forName(bd.getBeanClassName());
                TableName tn = AnnotationUtils.findAnnotation(entityClass, TableName.class);
                if (tn == null) {
                    return;
                }
                tableToEntity.put(tn.value(), entityClass);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        });

        applicationContext.getBeansOfType(BaseMapper.class).values().forEach(mapper -> {
            Class<?> entityClass = resolveMapperEntity(mapper.getClass());
            if (entityClass != null) {
                entityToMapper.put(entityClass, mapper);
            }
        });
    }

    public Class<?> entityClassFor(String tableName) {
        Class<?> c = tableToEntity.get(tableName);
        if (c == null) {
            throw new IllegalArgumentException(
                    "No entity class registered for table: " + tableName);
        }
        return c;
    }

    @SuppressWarnings("unchecked")
    public <T> BaseMapper<T> mapperFor(Class<T> entityClass) {
        BaseMapper<?> mapper = entityToMapper.get(entityClass);
        if (mapper == null) {
            throw new IllegalArgumentException(
                    "No BaseMapper bean found for entity: " + entityClass);
        }
        return (BaseMapper<T>) mapper;
    }

    private Class<?> resolveMapperEntity(Class<?> mapperClass) {
        for (Type t : mapperClass.getGenericInterfaces()) {
            if (t instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) t;
                if (pt.getRawType() == BaseMapper.class) {
                    Type typeArg = pt.getActualTypeArguments()[0];
                    return typeArg instanceof Class ? (Class<?>) typeArg : null;
                }
            }
        }
        // fallback: walk parent interfaces recursively
        for (Class<?> i : mapperClass.getInterfaces()) {
            Class<?> r = resolveMapperEntity(i);
            if (r != null) {
                return r;
            }
        }
        return null;
    }
}
