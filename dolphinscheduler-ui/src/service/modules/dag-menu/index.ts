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

import { axios } from '@/service/service'

export function getDagMenu(): any {
  return axios({
    url: '/favourite/taskTypes',
    method: 'get'
  })
}

export function Collection(taskType: string): any {
  return axios({
    url: `/favourite/${taskType}`,
    method: 'post'
  })
}

export function CancelCollection(taskType: string): any {
  return axios({
    url: `/favourite/${taskType}`,
    method: 'delete'
  })
}

export function GetDynList(): any {
  return axios({
    url: 'static/dynamic-task-config.json',
    method: 'get'
  })
}
