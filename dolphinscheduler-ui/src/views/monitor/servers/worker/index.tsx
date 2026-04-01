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

import { defineComponent, h, onMounted, ref, toRefs } from 'vue'
import { NButton, NGrid, NGi, NCard, NIcon, NModal, NSpace, NTag } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { useWorker } from './use-worker'
import styles from './index.module.scss'
import Card from '@/components/card'
import Result from '@/components/result'
import Gauge from '@/components/chart/modules/Gauge'
import WorkerModal from './worker-modal'
import type { Ref } from 'vue'
import type { RowData } from 'naive-ui/es/data-table/src/interface'
import type { WorkerNode } from '@/service/modules/monitor/types'
import { capitalize } from 'lodash'
import {
  DashboardOutlined,
  HddOutlined,
  DatabaseOutlined,
  BarsOutlined
} from '@vicons/antd'

const worker = defineComponent({
  name: 'worker',
  setup() {
    const showModalRef = ref(false)
    const showDiskDetailModalRef = ref(false)
    const diskDetailListRef: Ref<Array<any>> = ref([])
    const { t } = useI18n()
    const { variables, getTableWorker } = useWorker()
    const zkDirectoryRef: Ref<Array<RowData>> = ref([])

    const clickDetails = (zkDirectories: string) => {
      zkDirectoryRef.value = [{ directory: zkDirectories, index: 1 }]
      showModalRef.value = true
    }

    const onConfirmModal = () => {
      showModalRef.value = false
    }

    onMounted(() => {
      getTableWorker()
    })

    const openDiskDetails = (details: any[]) => {
      diskDetailListRef.value = details || []
      showDiskDetailModalRef.value = true
    }

    const setDiskDetailModalShow = (v: boolean) => {
      showDiskDetailModalRef.value = v
      if (!v) {
        diskDetailListRef.value = []
      }
    }

    return {
      t,
      ...toRefs(variables),
      clickDetails,
      onConfirmModal,
      showModalRef,
      zkDirectoryRef,
      showDiskDetailModalRef,
      diskDetailListRef,
      openDiskDetails,
      setDiskDetailModalShow
    }
  },
  render() {
    const {
      t,
      clickDetails,
      onConfirmModal,
      showModalRef,
      zkDirectoryRef,
      showDiskDetailModalRef,
      diskDetailListRef,
      openDiskDetails
    } = this

    const renderNodeServerStatusTag = (item: WorkerNode) => {
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
        title={t('monitor.worker.worker_no_data_result_title')}
        description={t('monitor.worker.worker_no_data_result_desc')}
        status={'info'}
        size={'medium'}
      />
    ) : (
      <>
        <NSpace vertical size={25}>
          {this.data.map((item: WorkerNode) => {
            const heartBeatInfo = item?.heartBeatInfo
              ? JSON.parse(item.heartBeatInfo)
              : {}
            const diskUsageDetails = heartBeatInfo?.diskUsageDetails || []

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

                      <span>{`${t('monitor.worker.host')}: ${
                        item ? item.host : ' - '
                      }`}</span>
                      <span
                        class={styles['link-btn']}
                        onClick={() => clickDetails(item.serverDirectory)}
                      >
                        {t('monitor.worker.directory_detail')}
                      </span>
                    </NSpace>
                    <NSpace>
                      <span>{`${t('monitor.worker.create_time')}: ${
                        item ? item.createTime : ' - '
                      }`}</span>
                      <span>{`${t('monitor.worker.last_heartbeat_time')}: ${
                        item ? item.lastHeartbeatTime : ' - '
                      }`}</span>
                    </NSpace>
                  </NSpace>
                </NCard>
                <NGrid x-gap='12' cols='4'>
                  <NGi>
                    <Card
                      title={
                        <span class={styles.metricTitle}>
                          <NIcon size={16}>
                            <DashboardOutlined />
                          </NIcon>
                          {t('monitor.worker.cpu_usage')}
                        </span>
                      }
                    >
                      <div class={styles.metricCard}>
                        {item && (
                          <Gauge
                            data={(
                              heartBeatInfo.cpuUsage * 100
                            ).toFixed(2)}
                          />
                        )}
                      </div>
                    </Card>
                  </NGi>
                  <NGi>
                    <Card
                      title={
                        <span class={styles.metricTitle}>
                          <NIcon size={16}>
                            <DatabaseOutlined />
                          </NIcon>
                          {t('monitor.worker.memory_usage')}
                        </span>
                      }
                    >
                      <div class={styles.metricCard}>
                        {item && (
                          <Gauge
                            data={(
                              heartBeatInfo.memoryUsage * 100
                            ).toFixed(2)}
                          />
                        )}
                      </div>
                    </Card>
                  </NGi>
                  <NGi>
                    <Card
                      title={
                        <span class={styles.metricTitle}>
                          <NIcon size={16}>
                            <HddOutlined />
                          </NIcon>
                          {t('monitor.worker.disk_usage')}
                        </span>
                      }
                    >
                      <div class={styles.metricCard}>
                        {item && (
                          <div class={styles.metricCardInner}>
                            <Gauge
                              data={(
                                heartBeatInfo.diskUsage * 100
                              ).toFixed(2)}
                            />
                            {diskUsageDetails.length > 0 && (
                              <div class={styles.metricAction}>
                                <NButton
                                  size='small'
                                  tertiary
                                  onClick={() => {
                                    openDiskDetails(diskUsageDetails)
                                  }}
                                >
                                  {t('monitor.worker.disk_usage_detail')}
                                </NButton>
                              </div>
                            )}
                          </div>
                        )}
                      </div>
                    </Card>
                  </NGi>
                  <NGi>
                    <Card
                      title={
                        <span class={styles.metricTitle}>
                          <NIcon size={16}>
                            <BarsOutlined />
                          </NIcon>
                          {t('monitor.worker.thread_pool_usage')}
                        </span>
                      }
                    >
                      <div class={styles.metricCard}>
                        {item && (
                          <Gauge
                            data={(
                              heartBeatInfo.threadPoolUsage * 100
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
        <WorkerModal
          showModal={showModalRef}
          data={zkDirectoryRef}
          onConfirmModal={onConfirmModal}
        />
        <NModal
          show={showDiskDetailModalRef}
          maskClosable
          preset='card'
          style={{ width: '520px' }}
          title={t('monitor.worker.data_disk_usage')}
          onUpdateShow={this.setDiskDetailModalShow}
        >
          <NSpace vertical size={8}>
            {diskDetailListRef.length === 0 &&
              t('monitor.worker.data_disk_usage_empty')}
            {diskDetailListRef.map((d: any) =>
              h(
                NSpace,
                { justify: 'space-between' },
                {
                  default: () => [
                    h('span', d.diskPath || '-'),
                    h(
                      NTag,
                      { type: 'info', size: 'small' },
                      {
                        default: () =>
                          `${((d.usedPercentage || 0) * 100).toFixed(2)}%`
                      }
                    )
                  ]
                }
              )
            )}
          </NSpace>
        </NModal>
      </>
    )
  }
})

export default worker
