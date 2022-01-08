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

import {h, defineComponent, toRefs} from 'vue'
import {NButton, NIcon, NInput, NTag, NDataTable} from 'naive-ui'
import Card from '@/components/card'
import {useForm} from "@/views/resource/taskGroupOption/use-form";
import {useUpdate} from "@/views/resource/taskGroupOption/use-update";
import type { TableColumns } from 'naive-ui/es/data-table/src/interface'
import { SearchOutlined } from '@vicons/antd'


// <el-table-column type="index" :label="$t('#')" width="50"></el-table-column>
// <el-table-column prop="name" :label="$t('Task group name')" width="150"></el-table-column>
// <el-table-column prop="projectName" :label="$t('Project Name')"></el-table-column>
// <el-table-column prop="groupSize" :label="$t('Task group resource pool size')" min-width="50"></el-table-column>
// <el-table-column prop="useSize" :label="$t('Task group resource used pool size')" min-width="50"></el-table-column>
// <el-table-column prop="description" :label="$t('Task group desc')" min-width="50"></el-table-column>
// <el-table-column :label="$t('Create Time')" min-width="50">
// <template slot-scope="scope">
//   <span>{{scope.row.createTime | formatDate}}</span>
// </template>
// </el-table-column>
// <el-table-column :label="$t('Update Time')" min-width="50">
// <template slot-scope="scope">
//   <span>{{scope.row.updateTime | formatDate}}</span>
// </template>
// </el-table-column>
// <el-table-column prop="status" :label="$t('Task group status')" min-width="50">
// <template slot-scope="scope">
//   <el-tooltip :content="scope.row.status? $t('Task group enable status'):$t('Task group disable status')" placement="top">
//   <el-switch
//       v-model="scope.row.status"
//       active-color="#13ce66"
//       inactive-color="#ff4949"
//   @change="_switchTaskGroupStatus(scope.row)"/>
// </el-tooltip>
// </template>
// </el-table-column>
// <el-table-column :label="$t('Operation')" width="100">
// <template slot-scope="scope">
//   <el-tooltip :content="$t('Edit')" placement="top">
//   <el-button type="primary" size="mini" icon="el-icon-edit-outline" @click="_edit(scope.row)" circle></el-button>
// </el-tooltip>
// <el-tooltip :content="$t('View task group queue')" placement="top">
// <el-button type="success" size="mini" icon="el-icon-tickets" @click="_switchTaskGroupQueue(scope.row)" circle></el-button>
// </el-tooltip>
// </template>
// </el-table-column>
//
// const createColumns = () => {
//   return [
//     {
//       title: {t('resource.task_group_option')},
//       key: 'index',
//     },
//     {
//       title: 'Name',
//       key: 'name',
//     },
//     {
//       title: 'Age',
//       key: 'age'
//     },
//     {
//       title: 'Address',
//       key: 'address'
//     },
//     {
//       title: 'Tags',
//       key: 'tags',
//       render (row: { tags: any[]; }) {
//         const tags = row.tags.map((tagKey) => {
//           return h(
//               NTag,
//               {
//                 style: {
//                   marginRight: '6px'
//                 },
//                 type: 'info'
//               },
//               {
//                 default: () => tagKey
//               }
//           )
//         })
//         return tags
//       }
//     },
//     {
//       title: 'Action',
//       key: 'actions'
//     }
//   ]
// }

const createData = () => [
  {
    id: 1,
    name: 'g1',
    projectName: 'test-tech',
    groupSize: 10,
    useSize: 0,
    description: 'aaaa',
    createTime: '2021-01-01',
    updateTime: '2021-01-01'
  },
  {
    id: 2,
    name: 'g2',
    projectName: 'test-tech',
    groupSize: 10,
    useSize: 0,
    description: 'aaaa',
    createTime: '2021-01-01',
    updateTime: '2021-01-01'
  },
  {
    id: 1,
    name: 'g3',
    projectName: 'test-tech',
    groupSize: 10,
    useSize: 0,
    description: 'aaaa',
    createTime: '2021-01-01',
    updateTime: '2021-01-01'
  }
]


const taskGroupOption = defineComponent({
  name: 'taskGroupOption',
  setup() {
    const { state, t } = useForm()
    const { handleUpdate } = useUpdate(state)

    const columnsRef: TableColumns<any> = [
      { title: '#', key: 'id' },
      { title: t('resource.task_group_name'), key: 'name' },
      { title: t('project.project_name'), key: 'projectName' },
      { title: t('resource.task_group_resource_pool_size'), key: 'groupSize' },
      { title: t('resource.task_group_resource_used_pool_size'), key: 'useSize' },
      { title: t('resource.task_group_desc'), key: 'description' },
      { title: t('resource.create_time'), key: 'createTime' },
      { title: t('resource.update_time'), key: 'updateTime' },
      { title: t('resource.actions'), key: 'actions' },
    ]

    const pageSizes = [
      {
        label: '10 每页',
        value: 10
      },
      {
        label: '30 每页',
        value: 30
      },
      {
        label: '50 每页',
        value: 50
      }
    ]

    return {
      ...toRefs(state),
      t,
      handleUpdate,
      data: createData(),
      columnsRef,
      pagination: {
        pageSize: 10,
        pageCount: 100,
        showSizePicker: true,
        pageSizes: pageSizes
      }
    }
  },
  render() {
    const { t } = this

    return (
        <Card title={t('resource.task_group_option')}>
          {{
            default: () => (
                <div>
                  <div>
                    <NButton type={"primary"}>{t('resource.create_task_group')}</NButton>
                    <NInput></NInput>
                    <NButton type={"tertiary"}>
                      <NIcon>
                        <SearchOutlined />
                      </NIcon>
                    </NButton>
                  </div>
                  <NDataTable columns={this.columnsRef} data={this.data} pagination={this.pagination}></NDataTable>
                </div>
            ),
          }}
        </Card>
    )
  },
})

export default taskGroupOption
