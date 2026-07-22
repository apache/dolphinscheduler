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
import { NButton, NSelect, NIcon, NSpace, NEllipsis } from 'naive-ui'
import { defineComponent, h, ref, unref } from 'vue'
import { queryWorkflowDefinitionList } from '@/service/modules/workflow-definition'
import { SelectMixedOption } from 'naive-ui/lib/select/src/interface'
import { Router, useRouter } from 'vue-router'
import { SelectOption } from 'naive-ui/es/select/src/interface'

export default defineComponent({
  name: 'TimingCondition',
  emits: ['handleSearch'],
  setup(props, ctx) {
    const router: Router = useRouter()

    const projectCode = ref(
      Number(router.currentRoute.value.params.projectCode)
    )
    const workflowDefinitionCodeRef = router.currentRoute.value.query
      .workflowDefinitionCode
      ? ref(Number(router.currentRoute.value.query.workflowDefinitionCode))
      : ref()

    const workflowDefinitionOptions = ref<Array<SelectMixedOption>>([])

    const initWorkflowList = (code: number) => {
      queryWorkflowDefinitionList(code).then((result: any) => {
        result.map((item: { code: number; name: string }) => {
          const option: SelectMixedOption = {
            value: item.code,
            label: () => h(NEllipsis, null, item.name),
            filterLabel: item.name
          }
          workflowDefinitionOptions.value.push(option)
        })
      })
    }

    initWorkflowList(projectCode.value)

    const handleSearch = () => {
      ctx.emit('handleSearch', {
        workflowDefinitionCode: workflowDefinitionCodeRef.value
      })
    }

    const selectFilter = (query: string, option: SelectOption) => {
      return option.filterLabel
        ? option.filterLabel
            .toString()
            .toLowerCase()
            .includes(query.toLowerCase())
        : false
    }

    const updateValue = (value: number) => {
      workflowDefinitionCodeRef.value = value
    }

    return {
      handleSearch,
      workflowDefinitionOptions: workflowDefinitionOptions,
      workflowDefinitionCodeRef: workflowDefinitionCodeRef,
      selectFilter,
      updateValue
    }
  },
  render() {
    const {
      workflowDefinitionCodeRef,
      workflowDefinitionOptions,
      selectFilter,
      updateValue
    } = this
    return (
      <NSpace justify='end'>
        {h(NSelect, {
          style: {
            width: '310px'
          },
          size: 'small',
          clearable: true,
          filterable: true,
          value: workflowDefinitionCodeRef,
          options: unref(workflowDefinitionOptions),
          filter: selectFilter,
          onUpdateValue: (value: any) => {
            updateValue(value)
          }
        })}
        <NButton type='primary' size='small' onClick={this.handleSearch}>
          <NIcon>
            <SearchOutlined />
          </NIcon>
        </NButton>
      </NSpace>
    )
  }
})
