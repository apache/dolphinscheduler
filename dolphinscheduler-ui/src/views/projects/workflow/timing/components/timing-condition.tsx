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

import { SearchOutlined } from '@vicons/antd'
import {
  NButton,
  NSelect,
  NIcon,
  NSpace,
  NEllipsis
} from 'naive-ui'
import { defineComponent, getCurrentInstance, h, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { queryProcessDefinitionList } from '@/service/modules/process-definition'
import { SelectMixedOption } from 'naive-ui/lib/select/src/interface'
import { Router, useRouter } from 'vue-router'

export default defineComponent({
  name: 'TimingCondition',
  emits: ['handleSearch'],
  setup(props, ctx) {
    const router: Router = useRouter()

    const projectCode = ref(
      Number(router.currentRoute.value.params.projectCode)
    )
    const processDefineCodeRef = router.currentRoute.value.query
      .processDefineCode
      ? ref(Number(router.currentRoute.value.query.processDefineCode))
      : ref()

    const processDefinitionOptions = ref<Array<SelectMixedOption>>([])

    const initProcessList = (code: number) => {
      queryProcessDefinitionList(code).then((result: any) => {
        result.map((item: { code: number; name: string }) => {
          const option: SelectMixedOption = {
            value: item.code,
            label: () => h(NEllipsis, null, item.name),
            filterLabel: item.name
          }
          processDefinitionOptions.value.push(option)
        })
      })
    }

    initProcessList(projectCode.value)

    const handleSearch = () => {
      ctx.emit('handleSearch', {
        processDefinitionCode: processDefineCodeRef.value
      })
    }

    return {
      handleSearch,
      processDefinitionOptions,
      processDefineCodeRef
    }
  },
  render() {
    const { t } = useI18n()

    return (
      <NSpace justify='end'>
        <NSelect
          clearable
          filterable
          options={this.processDefinitionOptions}
          size='small'
          style={{ width: '310px' }}
          v-model:value={this.processDefineCodeRef}
        />
        <NButton type='primary' size='small' onClick={this.handleSearch}>
          <NIcon>
            <SearchOutlined />
          </NIcon>
        </NButton>
      </NSpace>
    )
  }
})
