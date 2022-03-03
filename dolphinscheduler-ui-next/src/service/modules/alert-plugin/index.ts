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
import {
  ListReq,
  PluginInstanceReq,
  InstanceNameReq,
  IdReq,
  UpdatePluginInstanceReq
} from './types'

export function queryAlertPluginInstanceListPaging(params: ListReq): any {
  return axios({
    url: '/alert-plugin-instances',
    method: 'get',
    params
  })
}

export function createAlertPluginInstance(data: PluginInstanceReq): any {
  return axios({
    url: '/alert-plugin-instances',
    method: 'post',
    data
  })
}

export function verifyAlertInstanceName(params: InstanceNameReq): any {
  return axios({
    url: '/alert-plugin-instances/verify-name',
    method: 'get',
    params
  })
}

export function queryAlertPluginInstanceList(): any {
  return axios({
    url: '/alert-plugin-instances/list',
    method: 'get'
  })
}

export function getAlertPluginInstance(id: IdReq): any {
  return axios({
    url: `/alert-plugin-instances/${id}`,
    method: 'get'
  })
}

export function updateAlertPluginInstance(
  data: UpdatePluginInstanceReq,
  id: IdReq
): any {
  return axios({
    url: `/alert-plugin-instances/${id}`,
    method: 'put',
    data
  })
}

export function deleteAlertPluginInstance(id: IdReq): any {
  return axios({
    url: `/alert-plugin-instances/${id}`,
    method: 'delete'
  })
}
