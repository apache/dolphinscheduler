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
  ClusterReq,
  ClusterCodeReq,
  ClusterNameReq,
  ListReq,
  CodeReq
} from './types'

export function createCluster(data: ClusterReq): any {
  return axios({
    url: '/cluster/create',
    method: 'post',
    data
  })
}

export function deleteClusterByCode(data: ClusterCodeReq): any {
  return axios({
    url: '/cluster/delete',
    method: 'post',
    data
  })
}

export function queryClusterListPaging(params: ListReq): any {
  return axios({
    url: '/cluster/list-paging',
    method: 'get',
    params
  })
}

export function queryClusterByCode(params: ClusterCodeReq): any {
  return axios({
    url: '/cluster/query-by-code',
    method: 'get',
    params
  })
}

export function queryAllClusterList(): any {
  return axios({
    url: '/cluster/query-cluster-list',
    method: 'get'
  })
}

export function updateCluster(data: ClusterReq & CodeReq): any {
  return axios({
    url: '/cluster/update',
    method: 'post',
    data
  })
}

export function verifyCluster(data: ClusterNameReq): any {
  return axios({
    url: '/cluster/verify-cluster',
    method: 'post',
    data
  })
}
