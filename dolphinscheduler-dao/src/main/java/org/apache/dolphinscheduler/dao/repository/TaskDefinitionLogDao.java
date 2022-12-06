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

<<<<<<< HEAD:dolphinscheduler-task-plugin/dolphinscheduler-task-spark/src/main/java/org/apache/dolphinscheduler/plugin/task/spark/SparkVersion.java
public enum SparkVersion {
    SPARK1, SPARK2
=======
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;

import java.util.List;

/**
 * Task Definition Log DAO
 */
public interface TaskDefinitionLogDao {

    /**
     * Get task definition log list
     * @param processTaskRelations list of process task relation
     * @return list of task definition
     */
    List<TaskDefinitionLog> getTaskDefineLogList(List<ProcessTaskRelation> processTaskRelations);

    /**
     * Query task definition log list by process task relation list
     * @param processTaskRelations list of task relation
     * @return list of task definition log
     */
    List<TaskDefinitionLog> getTaskDefineLogListByRelation(List<ProcessTaskRelation> processTaskRelations);

>>>>>>> refs/remotes/origin/3.1.1-release:dolphinscheduler-dao/src/main/java/org/apache/dolphinscheduler/dao/repository/TaskDefinitionLogDao.java
}
