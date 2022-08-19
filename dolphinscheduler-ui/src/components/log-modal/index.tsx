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

import {
  defineComponent,
  PropType,
  h,
  ref,
  reactive,
  toRefs,
  onMounted,
  onUnmounted
} from 'vue'
import { useI18n } from 'vue-i18n'
import { NIcon, NLog } from 'naive-ui'
import Modal from '../modal'
import {
  DownloadOutlined,
  FullscreenExitOutlined,
  FullscreenOutlined,
  SyncOutlined
} from '@vicons/antd'
import screenfull from 'screenfull'

const props = {
  showModalRef: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  logRef: {
    type: String as PropType<string>,
    default: ''
  },
  logLoadingRef: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  row: {
    type: Object as PropType<any>,
    default: {}
  },
  showDownloadLog: {
    type: Boolean as PropType<boolean>,
    default: false
  }
}

export default defineComponent({
  name: 'log-modal',
  props,
  emits: ['confirmModal', 'refreshLogs', 'downloadLogs'],
  setup(props, ctx) {
    const { t } = useI18n()

    const variables = reactive({
      isFullscreen: false
    })

    const change = () => {
      variables.isFullscreen = screenfull.isFullscreen
    }

    const renderIcon = (icon: any) => {
      return () => h(NIcon, null, { default: () => h(icon) })
    }

    const confirmModal = () => {
      variables.isFullscreen = false
      ctx.emit('confirmModal', props.showModalRef)
    }

    const refreshLogs = () => {
      ctx.emit('refreshLogs', props.row)
    }

    const handleFullScreen = () => {
      screenfull.toggle(document.querySelectorAll('.logModalRef')[0])
    }

    const downloadLogs = () => {
      ctx.emit('downloadLogs', props.row)
    }

    onMounted(() => {
      screenfull.on('change', change)
    })

    onUnmounted(() => {
      screenfull.on('change', change)
    })

    return {
      t,
      renderIcon,
      confirmModal,
      refreshLogs,
      downloadLogs,
      handleFullScreen,
      ...toRefs(variables)
    }
  },
  render() {
    const {
      t,
      renderIcon,
      refreshLogs,
      downloadLogs,
      isFullscreen,
      handleFullScreen,
      showDownloadLog
    } = this
    return (
      <Modal
        class='logModalRef'
        title={t('project.task.view_log')}
        show={this.showModalRef}
        cancelShow={false}
        onConfirm={this.confirmModal}
        style={{ width: '60%' }}
        headerLinks={ref([
          {
            text: t('project.workflow.download_log'),
            show: showDownloadLog,
            action: downloadLogs,
            icon: renderIcon(DownloadOutlined)
          },
          {
            text: t('project.task.refresh'),
            show: true,
            action: refreshLogs,
            icon: renderIcon(SyncOutlined)
          },
          {
            text: isFullscreen
              ? t('project.task.cancel_full_screen')
              : t('project.task.enter_full_screen'),
            show: true,
            action: handleFullScreen,
            icon: isFullscreen
              ? renderIcon(FullscreenExitOutlined)
              : renderIcon(FullscreenOutlined)
          }
        ])}
      >
        <NLog
          rows={30}
          log={this.logRef}
          loading={this.logLoadingRef}
          style={{ height: isFullscreen ? 'calc(100vh - 140px)' : '525px' }}
        />
      </Modal>
    )
  }
})
