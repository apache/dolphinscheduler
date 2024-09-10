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
import type { ServerNodeType } from './types'

export function queryDatabaseState(): any {
  return axios({
    url: '/monitor/databases',
    method: 'get'
  })
}

export function listMaster(): any {
  return axios({
    url: '/monitor/masters',
    method: 'get'
  })
}

export function listWorker(): any {
  return axios({
    url: '/monitor/workers',
    method: 'get'
  })
}

export function listMonitorServerNode(nodeType: ServerNodeType): any {
  return axios({
    url: `/monitor/${nodeType}`,
    method: 'get'
  })
}
