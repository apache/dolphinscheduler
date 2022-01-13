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

import { defineComponent, toRefs, onMounted } from 'vue'
import {
  NButton,
  NInput,
  NIcon,
  NDataTable,
  NPagination,
  NCard,
} from 'naive-ui'
import styles from './index.module.scss'
import { useTable } from './use-table'
import { SearchOutlined } from '@vicons/antd'
import TenantModal from './components/tenant-modal'

const tenementManage = defineComponent({
  name: 'tenement-manage',
  setup() {
    const { variables, getTableData } = useTable()

    const requestData = () => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchVal: variables.searchVal,
      })
    }

    const handleModalChange = () => {
      variables.showModalRef = true
      variables.statusRef = 0
    }

    const onCancelModal = () => {
      variables.showModalRef = false
    }

    const onConfirmModal = () => {
      variables.showModalRef = false
      requestData()
    }

    onMounted(() => {
      requestData()
    })

    return {
      ...toRefs(variables),
      requestData,
      handleModalChange,
      onCancelModal,
      onConfirmModal,
    }
  },
  render() {
    return (
      <div class={styles.container}>
        <NCard>
          <div class={styles.header}>
            <div>
              <NButton size='small' onClick={this.handleModalChange}>
                创建租户
              </NButton>
            </div>
            <div class={styles.search}>
              <NInput
                size='small'
                v-model={[this.searchVal, 'value']}
                on-input={this.requestData}
                placeholder='请输入关键词'
                clearable
              />
              <NButton size='small'>
                <NIcon>
                  <SearchOutlined />
                </NIcon>
              </NButton>
            </div>
          </div>
        </NCard>
        <div class={styles.form}>
          <NDataTable columns={this.columns} data={this.tableData} />
        </div>
        <div class={styles.pagination}>
          <NPagination
            v-model:page={this.page}
            v-model:page-size={this.pageSize}
            page-count={this.totalPage}
            show-size-picker
            page-sizes={[10, 30, 50]}
            show-quick-jumper
            onUpdatePage={this.requestData}
            onUpdatePageSize={this.requestData}
          />
        </div>
        <TenantModal showModalRef={this.showModalRef} statusRef={this.statusRef} onCancelModal={this.onCancelModal} onConfirmModal={this.onConfirmModal}></TenantModal>
      </div>
    )
  },
})

export default tenementManage
