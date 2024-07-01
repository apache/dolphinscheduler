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

import { defineComponent } from 'vue'
import Form from '@/components/form'
import { useForm } from './use-form'
import { NButton, NDivider, NSpace, NSwitch } from 'naive-ui'

const PreferenceForm = defineComponent({
  name: 'PreferenceForm',
  setup() {
    const {
      formRef,
      elementsRef,
      rulesRef,
      model,
      stateRef,
      formProps,
      t,
      handleUpdate,
      handleUpdateState
    } = useForm()

    return () => (
      <div>
        <div style={{ marginLeft: '30px' }}>
          <NSwitch
            size={'large'}
            round={false}
            v-model:value={stateRef.value}
            checkedValue={1}
            uncheckedValue={0}
            onUpdateValue={handleUpdateState}
          >
            {{
              checked: () => t('project.preference.enabled'),
              unchecked: () => t('project.preference.disabled')
            }}
          </NSwitch>
        </div>
        <div>
          <div style={{ margin: '30px' }}>
            {t('project.preference.instruction_tips')}
          </div>
          <NDivider />
          <Form
            ref={formRef}
            meta={{
              model,
              disabled: stateRef.value === 1 ? false : true,
              rules: rulesRef.value,
              elements: elementsRef.value,
              ...formProps.value
            }}
            layout={{
              xGap: 10
            }}
            style={{ marginLeft: '150px' }}
          />
          <NDivider />
          <NSpace justify='center'>
            <NButton v-show={stateRef.value} type='info' onClick={handleUpdate}>
              {t('project.preference.submit')}
            </NButton>
          </NSpace>
        </div>
      </div>
    )
  }
})

export default PreferenceForm
