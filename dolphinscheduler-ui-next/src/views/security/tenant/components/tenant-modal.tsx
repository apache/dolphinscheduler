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

import { defineComponent, PropType } from 'vue'
// import {  } from 'naive-ui'
// import styles from './index.module.scss'
import Modal from '@/components/modal'
import { NForm, NFormItem, NInput } from 'naive-ui'

const TenantModal = defineComponent({
  name: 'tenant-modal',
  emits: ['cancelModal', 'confirmModal'],
  setup(props, ctx) {
    const cancelModal = () => {
      ctx.emit('cancelModal', props.showModalRef)
    }

    const confirmModal = async () => {
      ctx.emit('confirmModal', props.showModalRef)
    }

    return { cancelModal, confirmModal }
  },
  props: {
    showModalRef: {
      type: Boolean as PropType<boolean>,
      default: false,
    }
  },
  render() {
    return (
      <div>
        <Modal
          title="创建租户"
          show={this.showModalRef}
          onCancel={this.cancelModal}
          onConfirm={this.confirmModal}
        >
          {{
            default: () => (
              <div>这里是弹框</div>
            ),
          }}
        </Modal>
      </div>
    )
  },
})

export default TenantModal
