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

import _ from 'lodash'
import { defineComponent, onMounted, PropType, ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { listAlertGroupById } from '@/service/modules/alert-group'
import { queryAllWorkerGroups } from '@/service/modules/worker-groups'
import { runningType, warningTypeList } from '@/common/common'
import { IStartupParam } from './types'
import styles from './startup.module.scss'

const props = {
  startupParam: {
    type: Object as PropType<IStartupParam>,
    require: true
  }
}

export default defineComponent({
  name: 'dag-start-param',
  props,
  setup(props) {
    const { t } = useI18n()

    const alertGroupListRef = ref<any>([])
    const workerGroupListRef = ref<any>([])
    const commandParam = JSON.parse(props.startupParam?.commandParam || '{}')

    const getAlertGroupList = () => {
      listAlertGroupById().then((res: any) => {
        alertGroupListRef.value = res.map((item: any) => ({
          label: item.groupName,
          value: item.id
        }))
      })
    }

    const getWorkerGroupList = () => {
      queryAllWorkerGroups().then((res: any) => {
        workerGroupListRef.value = res
      })
    }

    const runType = computed(
      () =>
        (
          _.filter(
            runningType(t),
            (v) => v.code === props.startupParam?.commandType
          )[0] || {}
        ).desc
    )

    const warningType = computed(() => {
      const id = props.startupParam?.warningType as string
      const o = _.filter(warningTypeList, (v) => v.id === id)
      if (o && o.length) {
        return t(o[0].code)
      }
      return '-'
    })

    const alertGroupName = computed(() => {
      const id = props.startupParam?.warningGroupId
      if (!alertGroupListRef.value || !alertGroupListRef.value.length) {
        return '-'
      }

      const o = _.filter(alertGroupListRef.value, (v) => v.id === id)
      if (o && o.length) {
        return o[0].code
      }
      return '-'
    })

    onMounted(() => {
      getAlertGroupList()
      getWorkerGroupList()
    })

    return {
      t,
      alertGroupListRef,
      workerGroupListRef,
      commandParam,
      runType,
      warningType,
      alertGroupName
    }
  },
  render() {
    const { t } = this

    return (
      <div class={styles.box}>
        <ul class={styles['box-bd']}>
          <li>
            <span class={styles.tab}>
              {t('project.workflow.startup_type')}:
            </span>
            <span class={styles.content}>{this.runType}</span>
          </li>
          <li>
            <span class={styles.tab}>
              {t('project.workflow.complement_range')}:
            </span>
            {this.commandParam && this.commandParam.complementStartDate ? (
              <span class={styles.content}>
                {this.commandParam.complementStartDate}-
                {this.commandParam.complementEndDate}
              </span>
            ) : (
              '-'
            )}
          </li>
          <li>
            <span class={styles.tab}>
              {t('project.workflow.failure_strategy')}:
            </span>
            <span class={styles.content}>
              {this.startupParam?.failureStrategy === 'END'
                ? t('project.workflow.end')
                : t('project.workflow.continue')}
            </span>
          </li>
          <li>
            <span class={styles.tab}>
              {t('project.workflow.workflow_priority')}:
            </span>
            <span class={styles.content}>
              {this.startupParam?.processInstancePriority}
            </span>
          </li>
          <li>
            <span class={styles.tab}>
              {t('project.workflow.worker_group')}:
            </span>
            <span class={styles.content}>
              {this.workerGroupListRef.length
                ? this.startupParam?.workerGroup
                : '-'}
            </span>
          </li>
          <li>
            <span class={styles.tab}>
              {t('project.workflow.notification_strategy')}:
            </span>
            <span class={styles.content}>{this.warningType}</span>
          </li>
          <li>
            <span class={styles.tab}>{t('project.workflow.alarm_group')}:</span>
            <span class={styles.content}>{this.alertGroupName}</span>
          </li>
        </ul>
      </div>
    )
  }
})
