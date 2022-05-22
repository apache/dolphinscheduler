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
import { ListReq, TenantCodeReq, TenantReq, IdReq } from './types'

export function queryTenantListPaging(params: ListReq): any {
  return axios({
    url: '/tenants',
    method: 'get',
    params
  })
}

export function createTenant(data: TenantReq): any {
  return axios({
    url: '/tenants',
    method: 'post',
    data
  })
}

export function queryTenantList(): any {
  return axios({
    url: '/tenants/list',
    method: 'get'
  })
}

export function verifyTenantCode(params: TenantCodeReq): any {
  return axios({
    url: '/tenants/verify-code',
    method: 'get',
    params
  })
}

export function updateTenant(data: TenantCodeReq, idReq: IdReq): any {
  return axios({
    url: `/tenants/${idReq.id}`,
    method: 'put',
    data
  })
}

export function deleteTenantById(id: number): any {
  return axios({
    url: `/tenants/${id}`,
    method: 'delete'
  })
}
