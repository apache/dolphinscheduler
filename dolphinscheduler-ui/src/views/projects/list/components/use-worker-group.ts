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

import { useI18n } from 'vue-i18n'
import { reactive, ref, SetupContext } from 'vue'
import { Option } from 'naive-ui/es/transfer/src/interface'
import { queryAllWorkerGroups } from '@/service/modules/worker-groups'
import {
  queryWorkerGroupsByProjectCode,
  assignWorkerGroups
} from '@/service/modules/projects-worker-group'
import { UpdateProjectWorkerGroupsReq } from '@/service/modules/projects-worker-group/types'

export function useWorkerGroup(
  props: any,
  ctx: SetupContext<('cancelModal' | 'confirmModal')[]>
) {
  const { t } = useI18n()

  const variables = reactive({
    model: {
      workerGroupOptions: [] as Option[],
      assignedWorkerGroups: ref([] as any)
    }
  })

  const initOptions = () => {
    variables.model.workerGroupOptions = []
    queryAllWorkerGroups().then((res: any) => {
      for (const workerGroup of res) {
        variables.model.workerGroupOptions.push({
          label: workerGroup,
          value: workerGroup,
          disabled: workerGroup === 'default'
        })
      }
    })
  }

  const initAssignedWorkerGroups = (projectCode: number) => {
    variables.model.assignedWorkerGroups = ref([] as any)
    queryWorkerGroupsByProjectCode(projectCode).then((res: any) => {
      res.data.forEach((item: any) => {
        variables.model.assignedWorkerGroups.push(item.workerGroup)
      })
    })
  }

  initOptions()

  const handleValidate = () => {
    if (variables.model?.assignedWorkerGroups.length > 0) {
      submitModal()
      ctx.emit('confirmModal', props.showModalRef)
    }
  }

  const submitModal = async () => {
    if (props.row.code) {
      const data: UpdateProjectWorkerGroupsReq = {
        workerGroups:
          variables.model.assignedWorkerGroups.length > 0
            ? variables.model.assignedWorkerGroups.join(',')
            : ''
      }
      assignWorkerGroups(data, props.row.code)
    }
  }

  return { variables, t, handleValidate, initAssignedWorkerGroups }
}
