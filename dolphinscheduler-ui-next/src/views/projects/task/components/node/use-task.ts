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

import { useFlink } from './tasks/use-flink'
import { useShell } from './tasks/use-shell'
import { useSubProcess } from './tasks/use-sub-process'
import { usePigeon } from './tasks/use-pigeon'
import { usePython } from './tasks/use-python'
import { useSpark } from './tasks/use-spark'
import { useMr } from './tasks/use-mr'
import { useHttp } from './tasks/use-http'
import { useSql } from './tasks/use-sql'
import { useProcedure } from './tasks/use-procedure'
import { useSqoop } from './tasks/use-sqoop'
import { useSeaTunnel } from './tasks/use-sea-tunnel'
import { useSwitch } from './tasks/use-switch'
import { useConditions } from './tasks/use-conditions'
import { useDataX } from './tasks/use-datax'
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
  if (taskType === 'SPARK') {
    node = useSpark({
      projectCode,
      from,
      readonly,
      data
    })
  }
  if (taskType === 'MR') {
    node = useMr({
      projectCode,
      from,
      readonly,
      data
    })
  }
  if (taskType === 'FLINK') {
    node = useFlink({
      projectCode,
      from,
      readonly,
      data
    })
  }
  if (taskType === 'HTTP') {
    node = useHttp({
      projectCode,
      from,
      readonly,
      data
    })
  }
  if (taskType === 'PIGEON') {
    node = usePigeon({
      projectCode,
      from,
      readonly,
      data
    })
  }
  if (taskType === 'SQL') {
    node = useSql({
      projectCode,
      from,
      readonly,
      data
    })
  }
  if (taskType === 'PROCEDURE') {
    node = useProcedure({
      projectCode,
      from,
      readonly,
      data
    })
  }
  if (taskType === 'SQOOP') {
    node = useSqoop({
      projectCode,
      from,
      readonly,
      data
    })
  }
  if (taskType === 'SEATUNNEL') {
    node = useSeaTunnel({
      projectCode,
      from,
      readonly,
      data
    })
  }

  if (taskType === 'SWITCH') {
    node = useSwitch({
      projectCode,
      from,
      readonly,
      data
    })
  }

  if (taskType === 'CONDITIONS') {
    node = useConditions({
      projectCode,
      from,
      readonly,
      data
    })
  }

  if (taskType === 'DATAX') {
    node = useDataX({
      projectCode,
      from,
      readonly,
      data
    })
  }

  return node
}
