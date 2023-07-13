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
import type {
  TableColumns,
  InternalRowData
} from 'naive-ui/es/data-table/src/interface'
import { UserReq } from '@/service/modules/users/types'
export type { UserInfoRes } from '@/service/modules/users/types'

type TUserType = 'GENERAL_USER' | ''
type TAuthType =
  | 'authorize_project'
  | 'authorize_resource'
  | 'authorize_datasource'
  | 'authorize_udf'
  | 'authorize_namespace'

interface IRecord {
  id: number
  userName: string
  userType: TUserType
  tenantCode: string
  tenantId: null | number
  queueName: string
  email: string
  phone: string
  state: 0 | 1
  createTime: string
  updateTime: string
  userPassword?: string
  confirmPassword?: string
}

interface IResourceOption {
  id: number
  fullName: string
  type: string
}

interface IOption {
  value: number
  label: string
}

export {
  IRecord,
  IResourceOption,
  IOption,
  TAuthType,
  UserReq,
  TableColumns,
  InternalRowData
}
