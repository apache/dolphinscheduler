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

import { useShell } from './tasks/use-shell'
import { useSubProcess } from './tasks/use-sub-process'
import { usePython } from './tasks/use-python'
import { IJsonItem, INodeData, ITaskData } from './types'

export function useTask({
  data,
  projectCode,
  from,
  readonly
}: {
  data: ITaskData
  projectCode: number
  from?: number
  readonly?: boolean
}): { json: IJsonItem[]; model: INodeData } {
  const { taskType = 'SHELL' } = data
  let node = {} as { json: IJsonItem[]; model: INodeData }
  if (taskType === 'SHELL') {
    node = useShell({
      projectCode,
      from,
      readonly,
      data
    })
  }
  if (taskType === 'SUB_PROCESS') {
    node = useSubProcess({
      projectCode,
      from,
      readonly,
      data
    })
  }
  if (taskType === 'PYTHON') {
    node = usePython({
      projectCode,
      from,
      readonly,
      data
    })
  }
  return node
}
