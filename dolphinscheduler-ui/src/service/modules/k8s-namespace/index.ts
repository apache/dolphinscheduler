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
import { ListReq, K8SReq } from './types'
import { UserIdReq } from '@/service/modules/resources/types'

export function queryNamespaceListPaging(params: ListReq): any {
  return axios({
    url: '/k8s-namespace',
    method: 'get',
    params
  })
}

export function verifyNamespaceK8s(params: K8SReq): any {
  return axios({
    url: '/k8s-namespace/verify',
    method: 'post',
    params
  })
}

export function createK8sNamespace(params: K8SReq): any {
  return axios({
    url: '/k8s-namespace',
    method: 'post',
    params
  })
}

export function updateK8sNamespace(params: K8SReq, id: number): any {
  return axios({
    url: `/k8s-namespace/${id}`,
    method: 'put',
    params: {
      ...params,
      id
    }
  })
}

export function delNamespaceById(id: number): any {
  return axios({
    url: '/k8s-namespace/delete',
    method: 'post',
    params: { id }
  })
}

export function authNamespaceFunc(params: UserIdReq): any {
  return axios({
    url: '/k8s-namespace/authed-namespace',
    method: 'get',
    params
  })
}

export function unAuthNamespaceFunc(params: UserIdReq): any {
  return axios({
    url: '/k8s-namespace/unauth-namespace',
    method: 'get',
    params
  })
}
