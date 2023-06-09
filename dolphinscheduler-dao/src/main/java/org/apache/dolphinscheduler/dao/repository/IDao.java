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

package org.apache.dolphinscheduler.dao.repository;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import lombok.NonNull;

public interface IDao<Entity> {

    /**
     * Query the entity by primary key.
     */
    Entity queryById(@NonNull Serializable id);

    /**
     * Same with {@link #queryById(Serializable)} but return {@link Optional} instead of null.
     */
    Optional<Entity> queryOptionalById(@NonNull Serializable id);

    /**
     * Query the entity by primary keys.
     */
    List<Entity> queryByIds(Collection<? extends Serializable> ids);

    /**
     * Insert the entity.
     */
    int insert(@NonNull Entity model);

    /**
     * Insert the entities.
     */
    void insertBatch(Collection<Entity> models);

    /**
     * Update the entity by primary key.
     */
    boolean updateById(@NonNull Entity model);

    /**
     * Delete the entity by primary key.
     */
    boolean deleteById(@NonNull Serializable id);

    /**
     * Delete the entities by primary keys.
     */
    boolean deleteByIds(Collection<? extends Serializable> ids);

}
