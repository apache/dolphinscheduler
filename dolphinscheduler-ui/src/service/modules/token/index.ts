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
import { ListReq, TokenReq, UserReq, UpdateTokenReq } from './types'

export function queryAccessTokenList(params: ListReq): any {
  return axios({
    url: '/access-tokens',
    method: 'get',
    params
  })
}

export function createToken(data: TokenReq): any {
  return axios({
    url: '/access-tokens',
    method: 'post',
    data
  })
}

export function queryAccessTokenByUser(params: UserReq): any {
  return axios({
    url: '/access-tokens',
    method: 'get',
    params
  })
}

export function updateToken(data: UpdateTokenReq): any {
  return axios({
    url: `/access-tokens/${data.id}`,
    method: 'put',
    data
  })
}

export function deleteToken(id: number): any {
  return axios({
    url: `/access-tokens/${id}`,
    method: 'delete',
    params: { id }
  })
}

export function generateToken(data: TokenReq): any {
  return axios({
    url: '/access-tokens/generate',
    method: 'post',
    data
  })
}
