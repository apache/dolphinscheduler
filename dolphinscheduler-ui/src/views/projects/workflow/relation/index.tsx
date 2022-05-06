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

import { defineComponent, onMounted, toRefs, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { NSelect, NButton, NIcon, NSpace, NTooltip } from 'naive-ui'
import { ReloadOutlined, EyeOutlined } from '@vicons/antd'
import { useRelation } from './use-relation'
import Card from '@/components/card'
import Graph from './components/Graph'
import Result from '@/components/result'

const workflowRelation = defineComponent({
  name: 'workflow-relation',
  setup() {
    const { t, locale } = useI18n()
    const route = useRoute()
    const { variables, getWorkflowName, getOneWorkflow, getWorkflowList } =
      useRelation()

    onMounted(() => {
      getWorkflowList(Number(route.params.projectCode))
      getWorkflowName(Number(route.params.projectCode))
    })

    const handleResetDate = () => {
      variables.seriesData = []
      variables.workflow && variables.workflow !== 0
        ? getOneWorkflow(
            Number(variables.workflow),
            Number(route.params.projectCode)
          )
        : getWorkflowList(Number(route.params.projectCode))
    }

    watch(
      () => [variables.workflow, variables.labelShow, locale.value],
      () => {
        handleResetDate()
      }
    )

    return { t, handleResetDate, ...toRefs(variables) }
  },
  render() {
    const { t, handleResetDate } = this

    return (
      (this.seriesData.length === 0 && (
        <Result
          title={t('project.workflow.workflow_relation_no_data_result_title')}
          description={t(
            'project.workflow.workflow_relation_no_data_result_desc'
          )}
          status={'info'}
          size={'medium'}
        />
      )) ||
      (this.seriesData.length > 0 && (
        <Card title={t('project.workflow.workflow_relation')}>
          {{
            default: () =>
              Object.keys(this.seriesData).length > 0 && (
                <Graph
                  seriesData={this.seriesData}
                  labelShow={this.labelShow}
                />
              ),
            'header-extra': () => (
              <NSpace>
                <NSelect
                  clearable
                  style={{ width: '300px' }}
                  placeholder={t('project.workflow.workflow_name')}
                  options={this.workflowOptions}
                  v-model={[this.workflow, 'value']}
                />
                <NTooltip trigger={'hover'}>
                  {{
                    default: () => t('project.workflow.refresh'),
                    trigger: () => (
                      <NButton
                        strong
                        secondary
                        circle
                        type='info'
                        onClick={handleResetDate}
                      >
                        <NIcon>
                          <ReloadOutlined />
                        </NIcon>
                      </NButton>
                    )
                  }}
                </NTooltip>
                <NTooltip trigger={'hover'}>
                  {{
                    default: () => t('project.workflow.show_hide_label'),
                    trigger: () => (
                      <NButton
                        strong
                        secondary
                        circle
                        type='info'
                        onClick={() => (this.labelShow = !this.labelShow)}
                      >
                        <NIcon>
                          <EyeOutlined />
                        </NIcon>
                      </NButton>
                    )
                  }}
                </NTooltip>
              </NSpace>
            )
          }}
        </Card>
      ))
    )
  }
})

export default workflowRelation
