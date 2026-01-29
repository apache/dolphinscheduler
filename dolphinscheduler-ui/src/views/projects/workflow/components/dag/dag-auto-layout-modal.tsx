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

import { defineComponent, PropType, ref } from 'vue'
import type { Ref } from 'vue'
import Modal from '@/components/modal'
import { useI18n } from 'vue-i18n'
import {
  NForm,
  NFormItem,
  NInputNumber,
  NRadioButton,
  NRadioGroup
} from 'naive-ui'
import { LAYOUT_TYPE } from './use-graph-auto-layout'
import './x6-style.scss'

const props = {
  visible: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  formValue: {
    type: Object as PropType<Ref<any>>,
    default: ref()
  },
  formRef: {
    type: Object as PropType<Ref<any>>,
    default: ref()
  },
  submit: {
    type: Function as PropType<() => void>,
    default: () => {}
  },
  cancel: {
    type: Function as PropType<() => void>,
    default: () => {}
  }
}

export default defineComponent({
  name: 'dag-format-modal',
  props,
  setup(props, unusedContext) {
    const { t } = useI18n()
    const { formValue, formRef, submit, cancel } = props

    return () => (
      <Modal
        show={props.visible}
        title={t('project.dag.format')}
        onConfirm={submit}
        onCancel={cancel}
        autoFocus={false}
      >
        <NForm model={formValue.value} rules={{}} size='medium' ref={formRef}>
          <NFormItem label={t('project.dag.layout_type')} path='type'>
            <NRadioGroup
              v-model={[formValue.value.type, 'value']}
              name='radiogroup'
            >
              <NRadioButton value={LAYOUT_TYPE.GRID}>
                {t('project.dag.grid_layout')}
              </NRadioButton>
              <NRadioButton value={LAYOUT_TYPE.DAGRE}>
                {t('project.dag.dagre_layout')}
              </NRadioButton>
            </NRadioGroup>
          </NFormItem>
          {formValue.value.type === LAYOUT_TYPE.GRID ? (
            <NFormItem label={t('project.dag.rows')} path='rows'>
              <NInputNumber v-model={[formValue.value.rows, 'value']} min={0} />
            </NFormItem>
          ) : null}
          {formValue.value.type === LAYOUT_TYPE.GRID ? (
            <NFormItem label={t('project.dag.cols')} path='cols'>
              <NInputNumber v-model={[formValue.value.cols, 'value']} min={0} />
            </NFormItem>
          ) : null}
        </NForm>
      </Modal>
    )
  }
})
