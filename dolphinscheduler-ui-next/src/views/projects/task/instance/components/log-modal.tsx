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

import { defineComponent, onMounted, PropType, toRefs, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { NLog } from 'naive-ui'
import { useModal } from './use-modal'
import Modal from '@/components/modal'

const props = {
  showModalRef: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  row: {
    type: Object as PropType<any>,
    default: {}
  }
}

const LogModal = defineComponent({
  name: 'LogModal',
  props,
  emits: ['confirmModal'],
  setup(props, ctx) {
    const { t } = useI18n()
    const { variables, getLogs } = useModal()

    const confirmModal = () => {
      ctx.emit('confirmModal', props.showModalRef)
    }

    watch(
      () => props.showModalRef,
      () => {
        if (props.showModalRef) {
          variables.id = props.row.id
          props.showModalRef && variables.id && getLogs()
        } else {
          variables.id = ''
          variables.logRef = ''
          variables.loadingRef = true
          variables.skipLineNum = 0
          variables.limit = 1000
        }
      }
    )

    return { t, ...toRefs(variables), confirmModal }
  },
  render() {
    const { t } = this

    return (
      <Modal
        title={t('project.task.view_log')}
        show={this.showModalRef}
        cancelShow={false}
        onConfirm={this.confirmModal}
        style={{ width: '60%' }}
      >
        <NLog rows={30} log={this.logRef} loading={this.loadingRef} />
      </Modal>
    )
  }
})

export default LogModal
