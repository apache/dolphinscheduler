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

import { reactive } from 'vue'
import {
  queryDataSourceListPaging,
  deleteDataSource
} from '@/service/modules/data-source'

export function useTable() {
  const data = reactive({
    page: 1,
    pageSize: 10,
    itemCount: 0,
    searchVal: '',
    list: [],
    loading: false
  })

  const getList = async () => {
    if (data.loading) return
    data.loading = true

    const listRes = await queryDataSourceListPaging({
      pageNo: data.page,
      pageSize: data.pageSize,
      searchVal: data.searchVal
    })
    data.loading = false
    data.list = listRes.totalList
    data.itemCount = listRes.total
  }

  const updateList = () => {
    if (data.list.length === 1 && data.page > 1) {
      --data.page
    }
    getList()
  }

  const deleteRecord = async (id: number) => {
    const ignored = await deleteDataSource(id)
    updateList()
  }

  const changePage = (page: number) => {
    data.page = page
    getList()
  }

  const changePageSize = (pageSize: number) => {
    data.page = 1
    data.pageSize = pageSize
    getList()
  }

  return { data, changePage, changePageSize, deleteRecord, updateList }
}
