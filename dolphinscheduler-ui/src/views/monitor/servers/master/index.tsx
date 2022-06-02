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
import { NGrid, NGi, NCard, NNumberAnimation, NSpace } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { useMaster } from './use-master'
import styles from './index.module.scss'
import Card from '@/components/card'
import Result from '@/components/result'
import Gauge from '@/components/chart/modules/Gauge'
import MasterModal from './master-modal'
import type { Ref } from 'vue'
import type { RowData } from 'naive-ui/es/data-table/src/interface'
import type { MasterNode } from '@/service/modules/monitor/types'
import jsPDF from 'jspdf'

const master = defineComponent({
  name: 'master',
  setup() {
    const showModalRef = ref(false)
    const { t } = useI18n()
    const { variables, getTableMaster } = useMaster()
    const zkDirectoryRef: Ref<Array<RowData>> = ref([])

    const clickDetails = (zkDirectories: string) => {
      zkDirectoryRef.value = [{ directory: zkDirectories, index: 1 }]
      showModalRef.value = true
    }

    const onConfirmModal = () => {
      showModalRef.value = false
    }

    const exportToFile = (data: any) => {
      function convertToFileData(jsonData: any) {
        const array = []
        let str = ''
        array.push(['Info', 'Value'])
        for (const i in jsonData) {
          if (i === 'resInfo') {
            const resInfoJsonData = JSON.parse(jsonData[i])
            for (const j in resInfoJsonData) {
              array.push([i + '.' + j, resInfoJsonData[j]])
            }
          } else {
            array.push([i, jsonData[i]])
          }
        }
        for (let k = 0; k < array.length; k++) {
          let line = ''
          for (const index in array[k]) {
            if (line != '') line += ','
            line += array[k][index]
          }
          str += line + '\r\n'
        }
        return str
      }

      function padTo2Digits(num: number) {
        return num.toString().padStart(2, '0')
      }

      function formatDate(date: Date) {
        return (
          [
            padTo2Digits(date.getMonth() + 1),
            padTo2Digits(date.getDate()),
            date.getFullYear()
          ].join('-') +
          ' ' +
          [
            padTo2Digits(date.getHours()),
            padTo2Digits(date.getMinutes()),
            padTo2Digits(date.getSeconds())
          ].join('-')
        )
      }

      const fileData = convertToFileData(data)
      const now = formatDate(new Date())

      // Export to CSV file
      const uriCSV = 'data:text/csv;charset=utf-8,' + escape(fileData)
      const csvLink = document.createElement('a')
      csvLink.id = 'exportCSV'
      csvLink.href = uriCSV
      csvLink.download = now.toLocaleString() + '.csv'
      if (document.getElementById('exportCSV') === null) {
        // @ts-ignore
        document
          .getElementById('exportToFile')
          .insertAdjacentElement('beforeend', csvLink)
        // @ts-ignore
        document.getElementById('exportCSV').append('Export to CSV')
        // @ts-ignore
        document.getElementById('exportToFile').append(' ')
      }

      // Export to TXT file
      const uriTXT = 'data:text/txt;charset=utf-8,' + escape(fileData)
      const txtLink = document.createElement('a')
      txtLink.id = 'exportTXT'
      txtLink.href = uriTXT
      txtLink.download = now.toLocaleString() + '.txt'
      if (document.getElementById('exportTXT') === null) {
        // @ts-ignore
        document
          .getElementById('exportToFile')
          .insertAdjacentElement('beforeend', txtLink)
        // @ts-ignore
        document.getElementById('exportTXT').append('Export to TXT')
        // @ts-ignore
        document.getElementById('exportToFile').append(' ')
      }

      // Export to PDF file
      function exportToPDF() {
        const doc = new jsPDF()
        doc.text(fileData, 10, 10)
        doc.save(now.toLocaleString() + '.pdf')
        return false
      }

      const pdfLink = document.createElement('a')
      pdfLink.id = 'exportPDF'
      pdfLink.href = '#'
      if (document.getElementById('exportPDF') === null) {
        // @ts-ignore
        document
          .getElementById('exportToFile')
          .insertAdjacentElement('beforeend', pdfLink)
        // @ts-ignore
        document.getElementById('exportPDF').append('Export to PDF')
        // @ts-ignore
        document.getElementById('exportPDF').onclick = exportToPDF
        // @ts-ignore
        document.getElementById('exportToFile').append(' ')
      }
    }

    onMounted(() => {
      getTableMaster()
    })

    return {
      t,
      ...toRefs(variables),
      clickDetails,
      onConfirmModal,
      exportToFile,
      showModalRef,
      zkDirectoryRef
    }
  },
  render() {
    const {
      t,
      clickDetails,
      onConfirmModal,
      exportToFile,
      showModalRef,
      zkDirectoryRef
    } = this

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
                  <NSpace justify='space-between'>
                    <NSpace>
                      <span>{`${t('monitor.master.host')}: ${
                        item ? item.host : ' - '
                      }`}</span>
                      <span
                        class={styles['link-btn']}
                        onClick={() => clickDetails(item.zkDirectory)}
                      >
                        {t('monitor.master.directory_detail')}
                      </span>
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
                <NGrid x-gap='12' cols='3'>
                  <NGi>
                    <Card title={t('monitor.master.cpu_usage')}>
                      <div class={styles.card}>
                        {item && (
                          <Gauge
                            data={(
                              JSON.parse(item.resInfo).cpuUsage * 100
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
                              JSON.parse(item.resInfo).memoryUsage * 100
                            ).toFixed(2)}
                          />
                        )}
                      </div>
                    </Card>
                  </NGi>
                  <NGi>
                    <Card title={t('monitor.master.load_average')}>
                      <div class={[styles.card, styles['load-average']]}>
                        {item && (
                          <NNumberAnimation
                            precision={2}
                            from={0}
                            to={JSON.parse(item.resInfo).loadAverage}
                          />
                        )}
                      </div>
                    </Card>
                  </NGi>
                </NGrid>
                <NSpace>
                  <span
                    class={styles['link-btn']}
                    onClick={() => exportToFile(item)}
                  >
                    {t('Download Monitor Status')}
                  </span>
                  <div id='exportToFile'></div>
                </NSpace>
              </NSpace>
            )
          })}
        </NSpace>
        <MasterModal
          showModal={showModalRef}
          data={zkDirectoryRef}
          onConfirmModal={onConfirmModal}
        ></MasterModal>
      </>
    )
  }
})

export default master
