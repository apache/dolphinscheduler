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

import { useTaskType, useProcessName } from '.'
import type { IJsonItem, ITaskData } from '../types'

export const useTaskDefinition = ({
  projectCode,
  from = 0,
  readonly,
  data,
  model
}: {
  projectCode: number
  from?: number
  readonly?: boolean
  data?: ITaskData
  model: { [field: string]: any }
}): IJsonItem[] => {
  if (from === 0) return []
  return [
    useTaskType(model, readonly),
    useProcessName({
      model,
      projectCode,
      isCreate: !data?.id,
      from,
      processName: data?.processName,
      taskCode: data?.code
    })
  ]
}
