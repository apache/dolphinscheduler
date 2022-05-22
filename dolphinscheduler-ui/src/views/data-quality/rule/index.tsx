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

import { defineComponent, onMounted, ref, toRefs } from 'vue'
import {
  NSpace,
  NInput,
  NButton,
  NIcon,
  NDataTable,
  NPagination,
  NCard
} from 'naive-ui'
import { SearchOutlined } from '@vicons/antd'
import { useTable } from './use-table'
import Card from '@/components/card'
import styles from './index.module.scss'
import RuleModal from './components/rule-modal'

const TaskResult = defineComponent({
  name: 'rule',
  setup() {
    const { t, variables, getTableData } = useTable()

    const showModalRef = ref(false)

    const ruleEntryData = ref('')

    const requestTableData = () => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        startDate: '',
        endDate: '',
        searchVal: variables.searchVal
      })
    }

    const onUpdatePageSize = () => {
      variables.page = 1
      requestTableData()
    }

    const onSearch = () => {
      variables.page = 1
      requestTableData()
    }

    const onCancel = () => {
      showModalRef.value = false
    }

    const onConfirm = () => {
      showModalRef.value = false
    }

    const viewRuleEntry = (ruleJson: string) => {
      showModalRef.value = true
      ruleEntryData.value = ruleJson
    }

    onMounted(() => {
      requestTableData()
    })

    return {
      t,
      ...toRefs(variables),
      requestTableData,
      onUpdatePageSize,
      showModalRef,
      onCancel,
      onConfirm,
      onSearch,
      ruleEntryData,
      viewRuleEntry
    }
  },
  render() {
    const {
      t,
      showModalRef,
      requestTableData,
      onUpdatePageSize,
      onSearch,
      onCancel,
      onConfirm,
      viewRuleEntry,
      ruleEntryData,
      loadingRef
    } = this

    const { columns } = useTable(viewRuleEntry)

    return (
      <div>
        <NCard>
          <NSpace justify='end'>
            <NInput
              v-model={[this.searchVal, 'value']}
              size='small'
              placeholder={t('data_quality.rule.name')}
              clearable
            />
            <NButton size='small' type='primary' onClick={onSearch}>
              {{
                icon: () => (
                  <NIcon>
                    <SearchOutlined />
                  </NIcon>
                )
              }}
            </NButton>
          </NSpace>
        </NCard>
        <Card class={styles['table-card']}>
          <NDataTable
            loading={loadingRef}
            columns={columns}
            data={this.tableData}
          />
          <div class={styles.pagination}>
            <NPagination
              v-model:page={this.page}
              v-model:page-size={this.pageSize}
              page-count={this.totalPage}
              show-size-picker
              page-sizes={[10, 30, 50]}
              show-quick-jumper
              onUpdatePage={requestTableData}
              onUpdatePageSize={onUpdatePageSize}
            />
          </div>
        </Card>
        {showModalRef && (
          <RuleModal
            show={showModalRef}
            onCancel={onCancel}
            onConfirm={onConfirm}
            data={ruleEntryData}
          />
        )}
      </div>
    )
  }
})

export default TaskResult
