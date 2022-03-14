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

import { h, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAsyncState } from '@vueuse/core'
import ButtonLink from '@/components/button-link'
import { queryProjectListPaging } from '@/service/modules/projects'
import { parseTime } from '@/utils/common'
import { deleteProject } from '@/service/modules/projects'
import { format } from 'date-fns'
import { useRouter } from 'vue-router'
import { useMenuStore } from '@/store/menu/menu'
import {
  NButton,
  NEllipsis,
  NIcon,
  NPopconfirm,
  NSpace,
  NTooltip
} from 'naive-ui'
import type { Router } from 'vue-router'
import type { ProjectRes } from '@/service/modules/projects/types'
import { DeleteOutlined, EditOutlined } from '@vicons/antd'

export function useTable() {
  const { t } = useI18n()
  const router: Router = useRouter()
  const menuStore = useMenuStore()

  const handleEdit = (row: any) => {
    variables.showModalRef = true
    variables.statusRef = 1
    variables.row = row
  }

  const handleDelete = (row: any) => {
    deleteProject(row.code).then(() => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo:
          variables.tableData.length === 1 && variables.page > 1
            ? variables.page - 1
            : variables.page,
        searchVal: variables.searchVal
      })
    })
  }

  const createColumns = (variables: any) => {
    variables.columns = [
      {
        title: '#',
        key: 'index',
        render: (row: any, index: number) => index + 1
      },
      {
        title: t('project.list.project_name'),
        key: 'name',
        render: (row: { code: string; name: any }) =>
          h(
            NEllipsis,
            { style: 'max-width: 200px; color: #2080f0' },
            {
              default: () =>
                h(
                  ButtonLink,
                  {
                    onClick: () => {
                      menuStore.setProjectCode(row.code)
                      router.push({ path: `/projects/${row.code}` })
                    }
                  },
                  { default: () => row.name }
                ),
              tooltip: () => row.name
            }
          )
      },
      { title: t('project.list.owned_users'), key: 'userName' },
      { title: t('project.list.workflow_define_count'), key: 'defCount' },
      {
        title: t('project.list.process_instance_running_count'),
        key: 'instRunningCount'
      },
      { title: t('project.list.description'), key: 'description' },
      { title: t('project.list.create_time'), key: 'createTime' },
      { title: t('project.list.update_time'), key: 'updateTime' },
      {
        title: t('project.list.operation'),
        key: 'actions',
        render(row: any) {
          return h(NSpace, null, {
            default: () => [
              h(
                NTooltip,
                {},
                {
                  trigger: () =>
                    h(
                      NButton,
                      {
                        circle: true,
                        type: 'info',
                        size: 'small',
                        class: 'edit',
                        onClick: () => {
                          handleEdit(row)
                        }
                      },
                      {
                        icon: () =>
                          h(NIcon, null, { default: () => h(EditOutlined) })
                      }
                    ),
                  default: () => t('project.list.edit')
                }
              ),
              h(
                NPopconfirm,
                {
                  onPositiveClick: () => {
                    handleDelete(row)
                  }
                },
                {
                  trigger: () =>
                    h(
                      NTooltip,
                      {},
                      {
                        trigger: () =>
                          h(
                            NButton,
                            {
                              circle: true,
                              type: 'error',
                              size: 'small',
                              class: 'delete'
                            },
                            {
                              icon: () =>
                                h(NIcon, null, {
                                  default: () => h(DeleteOutlined)
                                })
                            }
                          ),
                        default: () => t('project.list.delete')
                      }
                    ),
                  default: () => t('project.list.delete_confirm')
                }
              )
            ]
          })
        }
      }
    ]
  }

  const variables = reactive({
    columns: [],
    tableData: [],
    page: ref(1),
    pageSize: ref(10),
    searchVal: ref(null),
    totalPage: ref(1),
    showModalRef: ref(false),
    statusRef: ref(0),
    row: {}
  })

  const getTableData = (params: any) => {
    const { state } = useAsyncState(
      queryProjectListPaging(params).then((res: ProjectRes) => {
        variables.totalPage = res.totalPage
        variables.tableData = res.totalList.map((item, unused) => {
          item.createTime = format(
            parseTime(item.createTime),
            'yyyy-MM-dd HH:mm:ss'
          )
          item.updateTime = format(
            parseTime(item.updateTime),
            'yyyy-MM-dd HH:mm:ss'
          )
          return {
            ...item
          }
        }) as any
      }),
      {}
    )
    return state
  }

  return {
    variables,
    getTableData,
    createColumns
  }
}
