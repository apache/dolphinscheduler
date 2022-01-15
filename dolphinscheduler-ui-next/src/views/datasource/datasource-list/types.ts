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

interface IDataSourceDetail extends Omit<IDataSource, 'other'> {
  other?: string
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
