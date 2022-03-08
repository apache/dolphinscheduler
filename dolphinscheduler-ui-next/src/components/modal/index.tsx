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

import {defineComponent, h, PropType, renderSlot, Ref} from 'vue'
import { NModal, NCard, NButton, NSpace, NIcon } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import styles from './index.module.scss'
import {LinkOption} from "@/components/modal/types";

const props = {
  show: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  title: {
    type: String as PropType<string>,
    required: true
  },
  cancelText: {
    type: String as PropType<string>
  },
  cancelShow: {
    type: Boolean as PropType<boolean>,
    default: true
  },
  confirmText: {
    type: String as PropType<string>
  },
  confirmClassName: {
    type: String as PropType<string>,
    default: ''
  },
  cancelClassName: {
    type: String as PropType<string>,
    default: ''
  },
  confirmDisabled: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  confirmLoading: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  autoFocus: {
    type: Boolean as PropType<boolean>,
    default: true
  },
  headerLinks: {
    type: Object as PropType<Ref<Array<LinkOption>>>,
    default: [] as LinkOption[]
  }
}

const Modal = defineComponent({
  name: 'Modal',
  props,
  emits: ['cancel', 'confirm', 'jumpLink'],
  setup(props, ctx) {
    const { t } = useI18n()

    const onCancel = () => {
      ctx.emit('cancel')
    }

    const onConfirm = () => {
      ctx.emit('confirm')
    }

    return { t, onCancel, onConfirm }
  },
  render() {
    const {
      $slots,
      t,
      onCancel,
      onConfirm,
      confirmDisabled,
      confirmLoading
    } = this

    return (
      <NModal
        v-model={[this.show, 'show']}
        class={styles.container}
        mask-closable={false}
        auto-focus={this.autoFocus}
      >
        <NCard
          title={this.title}
          class={styles['modal-card']}
          contentStyle={{ overflowY: 'auto' }}
        >
          {{
            default: () => renderSlot($slots, 'default'),
            'header-extra': () => (
              <NSpace justify='end'>
                {this.headerLinks.value && (this.headerLinks.value.filter((item: any) => item.show).map((item: any) => {
                  return (
                      <NButton text onClick={item.action}>
                        {{
                          default: () => item.text,
                          icon: () => item.icon()
                        }}
                      </NButton>
                  )
                }))}
              </NSpace>
            ),
            footer: () => (
              <NSpace justify='end'>
                {this.cancelShow && (
                  <NButton
                    class={this.cancelClassName}
                    quaternary
                    size='small'
                    onClick={onCancel}
                  >
                    {this.cancelText || t('modal.cancel')}
                  </NButton>
                )}
                {/* TODO: Add left and right slots later */}
                {renderSlot($slots, 'btn-middle')}
                <NButton
                  class={this.confirmClassName}
                  type='info'
                  size='small'
                  onClick={onConfirm}
                  disabled={confirmDisabled}
                  loading={confirmLoading}
                >
                  {this.confirmText || t('modal.confirm')}
                </NButton>
              </NSpace>
            )
          }}
        </NCard>
      </NModal>
    )
  }
})

export default Modal
