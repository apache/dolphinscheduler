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
  IDataSource,
  IDataBase
} from '@/service/modules/data-source/types'
import type { TableColumns } from 'naive-ui/es/data-table/src/interface'
import type { SelectBaseOption } from 'naive-ui/es/select/src/interface'

// THIRDPARTY_SYSTEM_CONNECTOR
interface AuthMapping {
  key: string
  value: string
}

interface AuthConfig {
  authType: string
  basicUsername?: string
  basicPassword?: string
  jwtToken?: string
  oauth2TokenUrl?: string
  oauth2ClientId?: string
  oauth2ClientSecret?: string
  oauth2GrantType?: string
  oauth2Username?: string
  oauth2Password?: string
  headerPrefix?: string
  authMappings?: AuthMapping[]
}

interface InterfaceParameter {
  paramName: string
  paramValue: string
  location: string
}

interface ResponseParameter {
  key: string
  jsonPath: string
  disabled?: boolean
}

interface InterfaceConfig {
  url: string
  method: string
  parameters: InterfaceParameter[]
  body: string
  responseParameters?: ResponseParameter[]
}

interface PollingSuccessConfig {
  successField: string
  successValue: string
}

interface PollingFailureConfig {
  failureField: string
  failureValue: string
}

interface PollStatusInterfaceConfig extends InterfaceConfig {
  pollingSuccessConfig: PollingSuccessConfig
  pollingFailureConfig: PollingFailureConfig
}

interface IDataSourceDetail extends Omit<IDataSource, 'other'> {
  other?: string
  // THIRDPARTY_SYSTEM_CONNECTOR
  systemName?: string
  serviceAddress?: string
  interfaceTimeout?: number
  authConfig?: AuthConfig
  selectInterface?: InterfaceConfig
  submitInterface?: InterfaceConfig
  pollStatusInterface?: PollStatusInterfaceConfig
  stopInterface?: InterfaceConfig
}

interface IDataBaseOption extends SelectBaseOption {
  label: string
  value: string
  defaultPort: number
  previousPort?: number
}

type IDataBaseOptionKeys = {
  [key in IDataBase]: IDataBaseOption
}

export {
  IDataSource,
  IDataSourceDetail,
  IDataBase,
  IDataBaseOption,
  IDataBaseOptionKeys,
  TableColumns
}