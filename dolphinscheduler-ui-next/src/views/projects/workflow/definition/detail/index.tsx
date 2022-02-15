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
import { useRoute } from 'vue-router'
import { useThemeStore } from '@/store/theme/theme'
import Dag from '../../components/dag'
import { queryProcessDefinitionByCode } from '@/service/modules/process-definition'
import { WorkflowDefinition } from '../../components/dag/types'
import Styles from './index.module.scss'

export default defineComponent({
  name: 'WorkflowDefinitionDetails',
  setup() {
    const theme = useThemeStore()
    const route = useRoute()
    const projectCode = Number(route.params.projectCode)
    const code = Number(route.params.code)

    const definition = ref<WorkflowDefinition>()

    const refresh = () => {
      queryProcessDefinitionByCode(code, projectCode).then((res: any) => {
        definition.value = res
      })
    }

    const save = () => {}

    onMounted(() => {
      if (!code || !projectCode) return
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
          definition={definition.value}
          onRefresh={refresh}
          projectCode={projectCode}
          onSave={save}
        />
      </div>
    )
  }
})
