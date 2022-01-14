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
  DataSourceReq,
  UserIdReq,
  TypeReq,
  NameReq,
  IdReq
} from './types'

export function queryDataSourceListPaging(params: ListReq): any {
  return axios({
    url: '/datasources',
    method: 'get',
    params
  })
}

export function createDataSource(data: DataSourceReq): any {
  return axios({
    url: '/datasources',
    method: 'post',
    data
  })
}

export function authedDatasource(params: UserIdReq): any {
  return axios({
    url: '/datasources/authed-datasource',
    method: 'get',
    params
  })
}

export function connectDataSource(data: DataSourceReq): any {
  return axios({
    url: '/datasources/connect',
    method: 'post',
    data
  })
}

export function getKerberosStartupState(): any {
  return axios({
    url: '/datasources/kerberos-startup-state',
    method: 'get'
  })
}

export function queryDataSourceList(params: TypeReq): any {
  return axios({
    url: '/datasources/list',
    method: 'get',
    params
  })
}

export function unAuthDatasource(params: UserIdReq): any {
  return axios({
    url: '/datasources/unauth-datasource',
    method: 'get',
    params
  })
}

export function verifyDataSourceName(params: NameReq): any {
  return axios({
    url: '/datasources/verify-name',
    method: 'get',
    params
  })
}

export function queryDataSource(id: IdReq): any {
  return axios({
    url: `/datasources/${id}`,
    method: 'get'
  })
}

export function updateDataSource(data: DataSourceReq, id: IdReq): any {
  return axios({
    url: `/datasources/${id}`,
    method: 'put',
    data
  })
}

export function deleteDataSource(id: IdReq): any {
  return axios({
    url: `/datasources/${id}`,
    method: 'delete'
  })
}

export function connectionTest(id: IdReq): any {
  return axios({
    url: `/datasources/${id}/connect-test`,
    method: 'get'
  })
}
