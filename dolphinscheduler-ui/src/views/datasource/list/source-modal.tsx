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

import { defineComponent, PropType, toRefs } from 'vue'
import { NSpace } from 'naive-ui'
import Modal from '@/components/modal'
import { useI18n } from 'vue-i18n'
import { useForm, datasourceTypeList } from './use-form'
import styles from './index.module.scss'

const props = {
  show: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  id: {
    type: Number as PropType<number>
  }
}

const SourceModal = defineComponent({
  name: 'SourceModal',
  props,
  emits: ['change', 'maskClick'],
  setup(props, ctx) {
    const { t } = useI18n()

    const { state } = useForm(props.id)

    const handleTypeSelect = (value: string) => {
      ctx.emit('change', value)
    }

    const handleMaskClick = () => {
      ctx.emit('maskClick')
    }

    return {
      t,
      ...toRefs(state),
      handleTypeSelect,
      handleMaskClick
    }
  },
  render() {
    const { show, t, handleTypeSelect, handleMaskClick } = this

    return (
      <Modal
        class='dialog-source-modal'
        show={show}
        title={t('datasource.choose_datasource_type')}
        cancelShow={false}
        confirmShow={false}
        onMaskClick={handleMaskClick}
      >
        {{
          default: () => (
            <div class={styles.content}>
              <NSpace>
                {datasourceTypeList.map((item) => (
                  <div
                    class={[styles.itemBox, `${item.label}-box`]}
                    onClick={() => handleTypeSelect(item.value)}
                  >
                    {item.label}
                  </div>
                ))}
              </NSpace>
            </div>
          )
        }}
      </Modal>
    )
  }
})

export default SourceModal
