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

import { defineComponent, ref } from 'vue'
import { NGrid, NGi, NCard, NNumberAnimation, NDataTable } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { useWorker } from './use-worker'
import styles from './index.module.scss'
import Card from '@/components/card'
import Gauge from '@/components/chart/modules/Gauge'
import Modal from '@/components/modal'
import type { WorkerRes } from '@/service/modules/monitor/types'
import type { Ref } from 'vue'
import type { TableColumns } from 'naive-ui/es/data-table/src/interface'

const master = defineComponent({
  name: 'master',
  setup() {
    const showModalRef = ref(false)
    const { t } = useI18n()
    const { getWorker } = useWorker()
    const workerRef: Ref<Array<WorkerRes>> = ref(getWorker())
    const columnsRef: TableColumns<any> = [
      { title: '#', key: 'index' },
      { title: t('monitor.worker.directory'), key: 'directory' }
    ]

    return { t, workerRef, showModalRef, columnsRef }
  },
  render() {
    const { t, workerRef, columnsRef } = this

    return (
      <div>
        <NCard class={styles['header-card']}>
          <div class={styles['content']}>
            <p>
              <span class={styles.left}>{`${t('monitor.worker.host')}: ${
                workerRef[0] ? workerRef[0].host : ' - '
              }`}</span>
              <span
                class={styles['link-btn']}
                onClick={() => (this.showModalRef = true)}
              >
                {t('monitor.worker.directory_detail')}
              </span>
            </p>
            <p>
              <span class={styles.left}>{`${t('monitor.worker.create_time')}: ${
                workerRef[0] ? workerRef[0].createTime : ' - '
              }`}</span>
              <span>{`${t('monitor.worker.last_heartbeat_time')}: ${
                workerRef[0] ? workerRef[0].lastHeartbeatTime : ' - '
              }`}</span>
            </p>
          </div>
        </NCard>
        <NGrid x-gap='12' cols='3'>
          <NGi>
            <Card title={t('monitor.worker.cpu_usage')}>
              <div class={styles.card}>
                {workerRef[0] && (
                  <Gauge
                    data={(
                      JSON.parse(workerRef[0].resInfo).cpuUsage * 100
                    ).toFixed(2)}
                  />
                )}
              </div>
            </Card>
          </NGi>
          <NGi>
            <Card title={t('monitor.worker.memory_usage')}>
              <div class={styles.card}>
                {workerRef[0] && (
                  <Gauge
                    data={(
                      JSON.parse(workerRef[0].resInfo).memoryUsage * 100
                    ).toFixed(2)}
                  />
                )}
              </div>
            </Card>
          </NGi>
          <NGi>
            <Card title={t('monitor.worker.load_average')}>
              <div class={[styles.card, styles['load-average']]}>
                {workerRef[0] && (
                  <NNumberAnimation
                    precision={2}
                    from={0}
                    to={JSON.parse(workerRef[0].resInfo).loadAverage}
                  />
                )}
              </div>
            </Card>
          </NGi>
        </NGrid>
        <Modal
          title={t('monitor.worker.directory_detail')}
          show={this.showModalRef}
          cancelShow={false}
          onConfirm={() => (this.showModalRef = false)}
        >
          {{
            default: () =>
              workerRef[0] && (
                <NDataTable
                  columns={columnsRef}
                  data={workerRef[0].zkDirectories.map((item, index) => {
                    return { index: index + 1, directory: item }
                  })}
                  striped
                  size={'small'}
                />
              )
          }}
        </Modal>
      </div>
    )
  }
})

export default master
