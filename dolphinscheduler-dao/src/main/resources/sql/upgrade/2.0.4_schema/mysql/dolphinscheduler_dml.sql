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

DELETE FROM t_ds_process_task_relation_log WHERE id IN
(
 SELECT
     x.id
 FROM
     (
         SELECT
             aa.id
         FROM
             t_ds_process_task_relation_log aa
                 JOIN
             (
                 SELECT
                     a.process_definition_code
                      ,MAX(a.id) as min_id
                      ,a.pre_task_code
                      ,a.pre_task_version
                      ,a.post_task_code
                      ,a.post_task_version
                      ,a.process_definition_version
                      ,COUNT(*) cnt
                 FROM
                     t_ds_process_task_relation_log a
                         JOIN (
                         SELECT
                             code
                         FROM
                             t_ds_process_definition
                         GROUP BY code
                     )b ON b.code = a.process_definition_code
                 WHERE 1=1
                 GROUP BY a.pre_task_code
                        ,a.post_task_code
                        ,a.pre_task_version
                        ,a.post_task_version
                        ,a.process_definition_code
                        ,a.process_definition_version
                 HAVING COUNT(*) > 1
             )bb ON bb.process_definition_code = aa.process_definition_code
                 AND bb.pre_task_code = aa.pre_task_code
                 AND bb.post_task_code = aa.post_task_code
                 AND bb.process_definition_version = aa.process_definition_version
                 AND bb.pre_task_version = aa.pre_task_version
                 AND bb.post_task_version = aa.post_task_version
                 AND bb.min_id != aa.id
     )x
)
;

DELETE FROM t_ds_task_definition_log WHERE id IN
(
   SELECT
       x.id
   FROM
       (
           SELECT
               a.id
           FROM
               t_ds_task_definition_log a
                   JOIN
               (
                   SELECT
                       code
                        ,name
                        ,version
                        ,MAX(id) AS min_id
                   FROM
                       t_ds_task_definition_log
                   GROUP BY code
                          ,name
                          ,version
                   HAVING COUNT(*) > 1
               )b ON b.code = a.code
                   AND b.name = a.name
                   AND b.version = a.version
                   AND b.min_id != a.id
       )x
)
;
