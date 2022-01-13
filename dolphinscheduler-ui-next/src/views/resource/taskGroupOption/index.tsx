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

import {h, ref, defineComponent, toRefs, reactive} from 'vue'
import {NButton, NIcon, NInput, NCard, NDataTable, NSwitch, NPagination} from 'naive-ui'
import Card from '@/components/card'
import {useForm} from "@/views/resource/taskGroupOption/use-form"
import {useUpdate} from "@/views/resource/taskGroupOption/use-update"
import type { TableColumns } from 'naive-ui/es/data-table/src/interface'
import { SearchOutlined, EditTwotone, UnorderedListOutlined } from '@vicons/antd'
import {useI18n} from "vue-i18n"
import styles from './index.module.scss'

import { queryTaskGroupListPaging, startTaskGroup, closeTaskGroup } from '@/service/modules/task-group'
import { ListReq } from "@/service/modules/resources/types";
//
// const createData = () => [
//   {
//     id: 1,
//     name: 'g1',
//     projectName: 'test-tech',
//     groupSize: 10,
//     useSize: 0,
//     description: 'aaaa',
//     createTime: '2021-01-01',
//     updateTime: '2021-01-01'
//   },
//   {
//     id: 2,
//     name: 'g2',
//     projectName: 'test-tech',
//     groupSize: 10,
//     useSize: 0,
//     description: 'aaaa',
//     createTime: '2021-01-01',
//     updateTime: '2021-01-01'
//   },
//   {
//     id: 3,
//     name: 'g3',
//     projectName: 'test-tech',
//     groupSize: 10,
//     useSize: 0,
//     description: 'aaaa',
//     createTime: '2021-01-01',
//     updateTime: '2021-01-01'
//   }
// ]

const taskGroupOption = defineComponent({
  name: 'taskGroupOption',
  setup() {
    const { t } = useI18n()
    const { state } = useForm()
    const { handleUpdate } = useUpdate(state)

    const searchParamRef = ref()
    const searchVal = ref()
    const data = ref([])

    const variables = reactive({
      tableData: [],
      page: ref(1),
      pageSize: ref(10),
      searchVal: ref(null),
      totalPage: ref(1),
    })

    const renderIcon = (icon: any) => {
      return () => h(NIcon, null, { default: () => h(icon) })
    }
    const renderTableAction = (row: any) => {
      return () => h(
          NButton,
          {
            size: 'small',
            type: 'primary',
            circle: true,
            onClick: () => onEdit(row),
          },
          {
            icon: renderIcon(EditTwotone)
          }
      )
    }
    const onSearch = () => {
      const params = {
        pageNo:1,
        pageSize:10,
        name: searchVal.value
      } as ListReq

      queryTaskGroupListPaging(params).then(res => {
        console.log(res.totalList)
        data.value.push(...res.totalList)
        // data = res.dataList
        // console.log(data)
        // console.log(data)
      })
      console.log('search....')
    }

    const onCreateTaskGroupOption = () => {
      console.log('create task...')

    }

    const onSwitchStatus = (value: any, item: any) => {
      console.log(value)
      console.log(item)
      const params = {
        id: item.id
      }
      if (value === 1) {
        startTaskGroup(params).catch(e => {
          console.log(e)
        })
      } else if (value ===0) {
        closeTaskGroup(params).catch(e => {
          console.log(e)
        })
      }
      item.id
    }

    const resetTableData = () => {
      // getTableData({
      //   pageSize: variables.pageSize,
      //   pageNo: variables.page,
      //   searchVal: variables.searchVal,
      // })
    }

    const onUpdatePageSize = () => {
      resetTableData()
    }

    const onEdit = (item: any) => {
      console.log(item)
    }

    const onViewTaskGroupQueue = (item: any) => {
      console.log(item)
    }

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
    const columnsRef: TableColumns<any> = [
      { title: t('resource.task_group_option.id'), key: 'id' },
      { title: t('resource.task_group_option.name'), key: 'name' },
      { title: t('resource.task_group_option.project_name'), key: 'projectName' },
      { title: t('resource.task_group_option.resource_pool_size'), key: 'groupSize' },
      { title: t('resource.task_group_option.resource_used_pool_size'), key: 'useSize' },
      { title: t('resource.task_group_option.desc'), key: 'description' },
      { title: t('resource.task_group_option.create_time'), key: 'createTime' },
      { title: t('resource.task_group_option.update_time'), key: 'updateTime' },
      {
        title: t('resource.task_group_option.status'),
        key: 'status',
        render(row) {
          return (
              <NSwitch v-model={[row.status, 'value']} checkedValue={1} uncheckedValue={0} onUpdate:value={(value) => onSwitchStatus(value,row)}/>
          )
        },
      },
      {
        title: t('resource.task_group_option.actions'), key: 'actions', width: 100,
        render(row) {
          return (
              <div class={styles['table-action']}>
                <NButton size={"small"} type={"info"} circle={true} onClick={() => onEdit(row)}>
                  <NIcon>
                    <EditTwotone/>
                  </NIcon>
                </NButton>
                <NButton size={"small"} type={"primary"} circle={true} onClick={() => onViewTaskGroupQueue(row)}>
                  <NIcon>
                    <UnorderedListOutlined/>
                  </NIcon>
                </NButton>
              </div>
          )
        },
      }
    ]

    return {
      ...toRefs(state),
      ...toRefs(variables),
      t,
      handleUpdate,
      data,
      columnsRef,
      onEdit,
      onCreateTaskGroupOption,
      onSearch,
      searchParamRef,
      resetTableData,
      onUpdatePageSize,
    }
  },
  render() {
    const {
      t,
      resetTableData,
      onUpdatePageSize,
    } = this

    return (
        <div>
          <NCard>
            <div class={styles.toolbar}>
              <div class={styles.left}>
                <NButton size='small' type={"primary"} onClick={() => this.onCreateTaskGroupOption()}>
                  {t('resource.task_group_option.create')}
                </NButton>
              </div>
              <div class={styles.right}>
                <NInput size='small' v-model={[this.searchVal, 'value']} placeholder={t('resource.task_group_option.please_enter_keywords')}></NInput>
                <NButton size='small' type='primary' onClick={() => this.onSearch()}>
                  <NIcon>
                    <SearchOutlined />
                  </NIcon>
                </NButton>
              </div>
            </div>
          </NCard>
          <Card class={styles['table-card']} title={t('resource.task_group_option.option')}>
            {{
              default: () => (
                  <div>
                    <NDataTable size={'small'} columns={this.columnsRef} data={this.data} striped/>
                    <div class={styles.pagination}>
                      <NPagination
                          v-model:page={this.page}
                          v-model:page-size={this.pageSize}
                          page-count={this.totalPage}
                          show-size-picker
                          page-sizes={[10, 30, 50]}
                          show-quick-jumper
                          onUpdatePage={resetTableData}
                          onUpdatePageSize={onUpdatePageSize}
                      />
                    </div>
                  </div>
              )
            }}
          </Card>
        </div>
    )
  },
})

export default taskGroupOption
