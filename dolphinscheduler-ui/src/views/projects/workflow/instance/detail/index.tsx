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

import { defineComponent, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useThemeStore } from '@/store/theme/theme'
import { useI18n } from 'vue-i18n'
import Dag from '../../components/dag'
import {
  queryProcessInstanceById,
  updateProcessInstance
} from '@/service/modules/process-instances'
import {
  WorkflowDefinition,
  WorkflowInstance,
  SaveForm,
  TaskDefinition,
  Connect,
  Location
} from '../../components/dag/types'
import Styles from './index.module.scss'

interface SaveData {
  saveForm: SaveForm
  taskDefinitions: TaskDefinition[]
  connects: Connect[]
  locations: Location[]
}

export default defineComponent({
  name: 'WorkflowInstanceDetails',
  setup() {
    const theme = useThemeStore()
    const route = useRoute()
    const router = useRouter()
    const { t } = useI18n()
    const projectCode = Number(route.params.projectCode)
    const id = Number(route.params.id)

    const definition = ref<WorkflowDefinition>()
    const instance = ref<WorkflowInstance>()

    const refresh = () => {
      queryProcessInstanceById(id, projectCode).then((res: any) => {
        instance.value = res
        if (res.dagData) {
          definition.value = res.dagData
        }
      })
    }

    const save = ({
      taskDefinitions,
      saveForm,
      connects,
      locations
    }: SaveData) => {
      const globalParams = saveForm.globalParams.map((p) => {
        return {
          prop: p.key,
          value: p.value,
          direct: 'IN',
          type: 'VARCHAR'
        }
      })

      updateProcessInstance(
        {
          syncDefine: saveForm.sync,
          globalParams: JSON.stringify(globalParams),
          locations: JSON.stringify(locations),
          taskDefinitionJson: JSON.stringify(taskDefinitions),
          taskRelationJson: JSON.stringify(connects),
          tenantCode: saveForm.tenantCode,
          timeout: saveForm.timeoutFlag ? saveForm.timeout : 0
        },
        id,
        projectCode
      ).then((ignored: any) => {
        window.$message.success(t('project.dag.success'))
        router.push({ path: `/projects/${projectCode}/workflow/instances` })
      })
    }

    onMounted(() => {
      if (!id || !projectCode) return
      refresh()
    })

    return () => (
      <div
        class={[
          Styles.container,
          theme.darkTheme ? Styles['dark'] : Styles['light']
        ]}
      >
        <Dag
          instance={instance.value}
          definition={definition.value}
          onRefresh={refresh}
          projectCode={projectCode}
          onSave={save}
        />
      </div>
    )
  }
})
