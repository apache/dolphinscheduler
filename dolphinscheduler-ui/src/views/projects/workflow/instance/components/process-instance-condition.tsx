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
  NInput,
  NButton,
  NDatePicker,
  NSelect,
  NIcon,
  NSpace,
  NEllipsis
} from 'naive-ui'
import { defineComponent, getCurrentInstance, h, ref, unref } from 'vue'
import { useI18n } from 'vue-i18n'
import { format } from 'date-fns'
import { workflowExecutionStateType } from '@/common/common'
import { queryProcessDefinitionList } from '@/service/modules/process-definition'
import { SelectMixedOption } from 'naive-ui/lib/select/src/interface'
import { Router, useRouter } from 'vue-router'
import { SelectOption } from 'naive-ui/es/select/src/interface'

export default defineComponent({
  name: 'ProcessInstanceCondition',
  emits: ['handleSearch'],
  setup(props, ctx) {
    const router: Router = useRouter()

    const searchValRef = ref('')
    const executorNameRef = ref('')
    const hostRef = ref('')
    const stateTypeRef = ref('')
    const startEndTimeRef = ref()
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
      let startDate = ''
      let endDate = ''
      if (startEndTimeRef.value) {
        startDate = format(
          new Date(startEndTimeRef.value[0]),
          'yyyy-MM-dd HH:mm:ss'
        )
        endDate = format(
          new Date(startEndTimeRef.value[1]),
          'yyyy-MM-dd HH:mm:ss'
        )
      }

      ctx.emit('handleSearch', {
        searchVal: searchValRef.value,
        executorName: executorNameRef.value,
        host: hostRef.value,
        stateType: stateTypeRef.value,
        startDate,
        endDate,
        processDefineCode: processDefineCodeRef.value
      })
    }

    const onClearSearchVal = () => {
      searchValRef.value = ''
      handleSearch()
    }

    const onClearSearchHost = () => {
      hostRef.value = ''
      handleSearch()
    }

    const onClearSearchExecutor = () => {
      executorNameRef.value = ''
      handleSearch()
    }

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    const selectFilter = (query: string, option: SelectOption) => {
      return option.filterLabel
        ? option.filterLabel
            .toString()
            .toLowerCase()
            .includes(query.toLowerCase())
        : false
    }

    const updateValue = (value: number) => {
      processDefineCodeRef.value = value
    }

    return {
      searchValRef,
      executorNameRef,
      hostRef,
      stateTypeRef,
      startEndTimeRef,
      handleSearch,
      onClearSearchVal,
      onClearSearchExecutor,
      onClearSearchHost,
      trim,
      processDefinitionOptions,
      processDefineCodeRef,
      selectFilter,
      updateValue
    }
  },
  render() {
    const { t } = useI18n()
    const options = workflowExecutionStateType(t)
    const {
      processDefinitionOptions,
      processDefineCodeRef,
      selectFilter,
      updateValue
    } = this

    return (
      <NSpace justify='end'>
        {h(NSelect, {
          style: {
            width: '210px'
          },
          size: 'small',
          clearable: true,
          filterable: true,
          options: unref(processDefinitionOptions),
          value: processDefineCodeRef,
          filter: selectFilter,
          onUpdateValue: (value: any) => {
            updateValue(value)
          }
        })}
        <NInput
          allowInput={this.trim}
          size='small'
          v-model:value={this.searchValRef}
          placeholder={t('project.workflow.name')}
          clearable
          onClear={this.onClearSearchVal}
        />
        <NInput
          allowInput={this.trim}
          size='small'
          v-model:value={this.executorNameRef}
          placeholder={t('project.workflow.executor')}
          clearable
          onClear={this.onClearSearchExecutor}
        />
        <NInput
          allowInput={this.trim}
          size='small'
          v-model:value={this.hostRef}
          placeholder={t('project.workflow.host')}
          clearable
          onClear={this.onClearSearchHost}
        />
        <NSelect
          options={options}
          size='small'
          style={{ width: '210px' }}
          defaultValue={''}
          v-model:value={this.stateTypeRef}
        />
        <NDatePicker
          type='datetimerange'
          size='small'
          clearable
          v-model:value={this.startEndTimeRef}
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
