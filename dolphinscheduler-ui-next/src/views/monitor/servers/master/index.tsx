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
import { useMaster } from './use-master'
import styles from './index.module.scss'
import Card from '@/components/card'
import Gauge from '@/components/chart/modules/Gauge'
import Modal from '@/components/modal'
import type { MasterNode } from '@/service/modules/monitor/types'
import type { Ref } from 'vue'
import type { TableColumns } from 'naive-ui/es/data-table/src/interface'

const master = defineComponent({
  name: 'master',
  setup() {
    const showModalRef = ref(false)
    const { t } = useI18n()
    const { getMaster } = useMaster()
    const masterRef: Ref<Array<MasterNode>> = ref(getMaster())
    const columnsRef: TableColumns<any> = [
      { title: '#', key: 'index' },
      { title: t('monitor.master.directory'), key: 'directory' }
    ]

    return { t, masterRef, showModalRef, columnsRef }
  },
  render() {
    const { t, masterRef, columnsRef } = this

    return (
      <div>
        <NCard class={styles['header-card']}>
          <div class={styles['content']}>
            <p>
              <span class={styles.left}>{`${t('monitor.master.host')}: ${
                masterRef[0] ? masterRef[0].host : ' - '
              }`}</span>
              <span
                class={styles['link-btn']}
                onClick={() => (this.showModalRef = true)}
              >
                {t('monitor.master.directory_detail')}
              </span>
            </p>
            <p>
              <span class={styles.left}>{`${t('monitor.master.create_time')}: ${
                masterRef[0] ? masterRef[0].createTime : ' - '
              }`}</span>
              <span>{`${t('monitor.master.last_heartbeat_time')}: ${
                masterRef[0] ? masterRef[0].lastHeartbeatTime : ' - '
              }`}</span>
            </p>
          </div>
        </NCard>
        <NGrid x-gap='12' cols='3'>
          <NGi>
            <Card title={t('monitor.master.cpu_usage')}>
              <div class={styles.card}>
                {masterRef[0] && (
                  <Gauge
                    data={(
                      JSON.parse(masterRef[0].resInfo).cpuUsage * 100
                    ).toFixed(2)}
                  />
                )}
              </div>
            </Card>
          </NGi>
          <NGi>
            <Card title={t('monitor.master.memory_usage')}>
              <div class={styles.card}>
                {masterRef[0] && (
                  <Gauge
                    data={(
                      JSON.parse(masterRef[0].resInfo).memoryUsage * 100
                    ).toFixed(2)}
                  />
                )}
              </div>
            </Card>
          </NGi>
          <NGi>
            <Card title={t('monitor.master.load_average')}>
              <div class={[styles.card, styles['load-average']]}>
                {masterRef[0] && (
                  <NNumberAnimation
                    precision={2}
                    from={0}
                    to={JSON.parse(masterRef[0].resInfo).loadAverage}
                  />
                )}
              </div>
            </Card>
          </NGi>
        </NGrid>
        <Modal
          title={t('monitor.master.directory_detail')}
          show={this.showModalRef}
          cancelShow={false}
          onConfirm={() => (this.showModalRef = false)}
        >
          {{
            default: () =>
              masterRef[0] && (
                <NDataTable
                  columns={columnsRef}
                  data={[{ index: 1, directory: masterRef[0].zkDirectory }]}
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
