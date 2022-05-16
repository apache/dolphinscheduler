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

import { ref, onMounted, computed, h } from 'vue'
import { useI18n } from 'vue-i18n'
import { listAlertGroupById } from '@/service/modules/alert-group'
import styles from '../index.module.scss'
import type { IJsonItem } from '../types'

export function useSqlType(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()
  const querySpan = computed(() => (model.sqlType === '0' ? 6 : 0))
  const nonQuerySpan = computed(() => (model.sqlType === '1' ? 6 : 0))
  const emailSpan = computed(() =>
    model.sqlType === '0' && model.sendEmail ? 24 : 0
  )
  const groups = ref([])
  const groupsLoading = ref(false)
  const SQL_TYPES = [
    {
      value: '0',
      label: t('project.node.sql_type_query')
    },
    {
      value: '1',
      label: t('project.node.sql_type_non_query')
    }
  ]

  const getGroups = async () => {
    if (groupsLoading.value) return
    groupsLoading.value = true
    const res = await listAlertGroupById()
    groups.value = res.map((item: { id: number; groupName: string }) => ({
      label: item.groupName,
      value: item.id
    }))
    groupsLoading.value = false
  }

  onMounted(() => {
    getGroups()
  })

  return [
    {
      type: 'select',
      field: 'sqlType',
      span: 6,
      name: t('project.node.sql_type'),
      options: SQL_TYPES,
      validate: {
        trigger: ['input', 'blur'],
        required: true
      }
    },
    {
      type: 'input',
      field: 'segmentSeparator',
      name: t('project.node.segment_separator'),
      props: {
        placeholder: t('project.node.segment_separator_tips')
      },
      span: nonQuerySpan
    },
    {
      type: 'switch',
      field: 'sendEmail',
      span: querySpan,
      name: t('project.node.send_email')
    },
    {
      type: 'select',
      field: 'displayRows',
      span: querySpan,
      name: t('project.node.log_display'),
      options: DISPLAY_ROWS,
      props: {
        filterable: true,
        tag: true
      },
      validate: {
        trigger: ['input', 'blur'],
        validator(unuse, value) {
          if (!/^\+?[1-9][0-9]*$/.test(value)) {
            return new Error(t('project.node.integer_tips'))
          }
        }
      }
    },
    {
      type: 'custom',
      field: 'displayRowsTips',
      span: querySpan,
      widget: h(
        'div',
        { class: styles['display-rows-tips'] },
        t('project.node.rows_of_result')
      )
    },
    {
      type: 'input',
      field: 'title',
      name: t('project.node.title'),
      props: {
        placeholder: t('project.node.title_tips')
      },
      span: emailSpan,
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(unuse, value) {
          if (model.sendEmail && !value)
            return new Error(t('project.node.title_tips'))
        }
      }
    },
    {
      type: 'select',
      field: 'groupId',
      name: t('project.node.alarm_group'),
      options: groups,
      span: emailSpan,
      props: {
        loading: groupsLoading,
        placeholder: t('project.node.alarm_group_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(unuse, value) {
          if (model.sendEmail && !value)
            return new Error(t('project.node.alarm_group_tips'))
        }
      }
    }
  ]
}

const DISPLAY_ROWS = [
  {
    label: '1',
    value: 1
  },
  {
    label: '10',
    value: 10
  },
  {
    label: '25',
    value: 25
  },
  {
    label: '50',
    value: 50
  },
  {
    label: '100',
    value: 100
  }
]
