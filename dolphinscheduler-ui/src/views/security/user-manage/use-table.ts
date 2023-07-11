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

import { reactive, onMounted } from 'vue'
import { queryUserList, delUserById } from '@/service/modules/users'
import { format } from 'date-fns'
import { parseTime } from '@/common/common'
import type { IRecord, TAuthType } from './types'

export function useTable() {

  const state = reactive({
    page: 1,
    pageSize: 10,
    itemCount: 0,
    searchVal: '',
    list: [],
    loading: false,
    currentRecord: {} as IRecord | null,
    authorizeType: 'authorize_project' as TAuthType,
    detailModalShow: false,
    authorizeModalShow: false,
    passwordModalShow: false
  })

  const getList = async () => {
    if (state.loading) return
    state.loading = true

    const { totalList, total } = await queryUserList({
      pageNo: state.page,
      pageSize: state.pageSize,
      searchVal: state.searchVal
    })
    state.loading = false
    if (!totalList) throw Error()
    state.list = totalList.map((record: IRecord) => {
      record.createTime = record.createTime
        ? format(parseTime(record.createTime), 'yyyy-MM-dd HH:mm:ss')
        : ''
      record.updateTime = record.updateTime
        ? format(parseTime(record.updateTime), 'yyyy-MM-dd HH:mm:ss')
        : ''
      record.tenantId = record.tenantId === 0 ? null : record.tenantId
      return record
    })

    state.itemCount = total
  }

  const updateList = () => {
    if (state.list.length === 1 && state.page > 1) {
      --state.page
    }
    getList()
  }

  const deleteUser = async (userId: number) => {
    await delUserById({ id: userId })
    updateList()
  }

  const onOperationClick = (
    data: { rowData: IRecord; key?: TAuthType },
    type: 'authorize' | 'edit' | 'delete' | 'resetPassword'
  ) => {
    state.currentRecord = data.rowData
    if (type === 'edit') {
      state.detailModalShow = true
    }
    if (type === 'authorize' && data.key) {
      state.authorizeModalShow = true
      state.authorizeType = data.key
    }
    if (type === 'delete') {
      deleteUser(data.rowData.id)
    }
    if (type === 'resetPassword') {
      state.passwordModalShow = true
    }
  }

  const changePage = (page: number) => {
    state.page = page
    getList()
  }

  const changePageSize = (pageSize: number) => {
    state.page = 1
    state.pageSize = pageSize
    getList()
  }

  onMounted(() => {
    getList()
  })

  return { state, changePage, changePageSize, updateList, onOperationClick }
}
