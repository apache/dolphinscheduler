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
import { NGrid, NGi, NCard, NSpace, NTag } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { useMaster } from './use-master'
import styles from './index.module.scss'
import Card from '@/components/card'
import Result from '@/components/result'
import Gauge from '@/components/chart/modules/Gauge'
import MasterModal from './master-modal'
import RunningWorkflowsModal from './running-workflows-modal'
import RunningTasksModal from '../worker/running-tasks-modal'
import { useUserStore } from '@/store/user/user'
import type { Ref } from 'vue'
import type { RowData } from 'naive-ui/es/data-table/src/interface'
import type { MasterNode } from '@/service/modules/monitor/types'
import type { UserInfoRes } from '@/service/modules/users/types'
import { capitalize } from 'lodash'

const master = defineComponent({
  name: 'master',
  setup() {
    const showModalRef = ref(false)
    const showRunningRef = ref(false)
    const showRunningTasksRef = ref(false)
    const selectedMasterAddressRef = ref('')
    const { t } = useI18n()
    const userStore = useUserStore()
    const IS_ADMIN =
      (userStore.getUserInfo as UserInfoRes).userType === 'ADMIN_USER'
    const { variables, getTableMaster } = useMaster()
    const zkDirectoryRef: Ref<Array<RowData>> = ref([])

    const clickDetails = (zkDirectories: string) => {
      zkDirectoryRef.value = [{ directory: zkDirectories, index: 1 }]
      showModalRef.value = true
    }

    const clickRunningWorkflows = (item: MasterNode) => {
      selectedMasterAddressRef.value = item.host + ':' + item.port
      showRunningRef.value = true
    }

    const clickRunningTasks = (item: MasterNode) => {
      selectedMasterAddressRef.value = item.host + ':' + item.port
      showRunningTasksRef.value = true
    }

    const onConfirmModal = () => {
      showModalRef.value = false
    }

    const onConfirmRunningModal = () => {
      showRunningRef.value = false
    }

    const onConfirmRunningTasksModal = () => {
      showRunningTasksRef.value = false
    }

    onMounted(() => {
      getTableMaster()
    })

    return {
      t,
      ...toRefs(variables),
      clickDetails,
      clickRunningWorkflows,
      clickRunningTasks,
      onConfirmModal,
      onConfirmRunningModal,
      onConfirmRunningTasksModal,
      showModalRef,
      showRunningRef,
      showRunningTasksRef,
      selectedMasterAddressRef,
      zkDirectoryRef,
      IS_ADMIN
    }
  },
  render() {
    const {
      t,
      clickDetails,
      clickRunningWorkflows,
      clickRunningTasks,
      onConfirmModal,
      onConfirmRunningModal,
      onConfirmRunningTasksModal,
      showModalRef,
      showRunningRef,
      showRunningTasksRef,
      selectedMasterAddressRef,
      zkDirectoryRef,
      IS_ADMIN
    } = this

    const renderNodeServerStatusTag = (item: MasterNode) => {
      const serverStatus = JSON.parse(item.heartBeatInfo)?.serverStatus

      if (!serverStatus) return ''

      return (
        <NTag type={serverStatus === 'NORMAL' ? 'info' : 'warning'}>
          {capitalize(serverStatus)}
        </NTag>
      )
    }

    return this.data.length < 1 ? (
      <Result
        title={t('monitor.master.master_no_data_result_title')}
        description={t('monitor.master.master_no_data_result_desc')}
        status={'info'}
        size={'medium'}
      />
    ) : (
      <>
        <NSpace vertical size={25}>
          {this.data.map((item: MasterNode) => {
            return (
              <NSpace vertical>
                <NCard>
                  <NSpace
                    justify='space-between'
                    style={{
                      'line-height': '28px'
                    }}
                  >
                    <NSpace>
                      {renderNodeServerStatusTag(item)}

                      <span>{`${t('monitor.master.host')}: ${
                        item ? item.host : ' - '
                      }`}</span>
                      <span
                        class={styles['link-btn']}
                        onClick={() => clickDetails(item.serverDirectory)}
                      >
                        {t('monitor.master.directory_detail')}
                      </span>
                      {IS_ADMIN && (
                        <span
                          class={styles['link-btn']}
                          onClick={() => clickRunningWorkflows(item)}
                        >
                          {t('monitor.master.running_workflows')}
                        </span>
                      )}
                      {IS_ADMIN && (
                        <span
                          class={styles['link-btn']}
                          onClick={() => clickRunningTasks(item)}
                        >
                          {t('monitor.master.running_tasks')}
                        </span>
                      )}
                    </NSpace>
                    <NSpace>
                      <span>{`${t('monitor.master.create_time')}: ${
                        item ? item.createTime : ' - '
                      }`}</span>
                      <span>{`${t('monitor.master.last_heartbeat_time')}: ${
                        item ? item.lastHeartbeatTime : ' - '
                      }`}</span>
                    </NSpace>
                  </NSpace>
                </NCard>
                <NGrid x-gap='12' cols='4'>
                  <NGi>
                    <Card title={t('monitor.master.cpu_usage')}>
                      <div class={styles.card}>
                        {item && (
                          <Gauge
                            data={(
                              JSON.parse(item.heartBeatInfo).cpuUsage * 100
                            ).toFixed(2)}
                          />
                        )}
                      </div>
                    </Card>
                  </NGi>
                  <NGi>
                    <Card title={t('monitor.master.memory_usage')}>
                      <div class={styles.card}>
                        {item && (
                          <Gauge
                            data={(
                              JSON.parse(item.heartBeatInfo).memoryUsage * 100
                            ).toFixed(2)}
                          />
                        )}
                      </div>
                    </Card>
                  </NGi>
                  <NGi>
                    <Card title={t('monitor.master.disk_usage')}>
                      <div class={[styles.card]}>
                        {item && (
                          <Gauge
                            data={(
                              JSON.parse(item.heartBeatInfo).diskUsage * 100
                            ).toFixed(2)}
                          />
                        )}
                      </div>
                    </Card>
                  </NGi>
                </NGrid>
              </NSpace>
            )
          })}
        </NSpace>
        <MasterModal
          showModal={showModalRef}
          data={zkDirectoryRef}
          onConfirmModal={onConfirmModal}
        ></MasterModal>
        <RunningWorkflowsModal
          showModal={showRunningRef}
          masterAddress={selectedMasterAddressRef}
          onConfirmModal={onConfirmRunningModal}
        ></RunningWorkflowsModal>
        <RunningTasksModal
          showModal={showRunningTasksRef}
          serverAddress={selectedMasterAddressRef}
          onConfirmModal={onConfirmRunningTasksModal}
        ></RunningTasksModal>
      </>
    )
  }
})

export default master
