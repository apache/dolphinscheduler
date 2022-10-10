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

import { defineComponent, PropType, toRefs, h, unref } from 'vue'
import { NSpin, NGrid, NForm, NFormItemGi, FormRules } from 'naive-ui'
import { useForm } from './use-form'
import type { GridProps, IMeta } from './types'

const props = {
  meta: {
    type: Object as PropType<IMeta>,
    default: {},
    required: true
  },
  layout: {
    type: Object as PropType<GridProps>
  },
  loading: {
    type: Boolean as PropType<boolean>,
    default: false
  }
}

const Form = defineComponent({
  name: 'DSForm',
  props,
  setup(props, { expose }) {
    const { state, ...rest } = useForm()
    expose({
      ...rest
    })
    return { ...toRefs(state) }
  },
  render(props: { meta: IMeta; layout?: GridProps; loading?: boolean }) {
    const { loading, layout, meta } = props
    const { elements = [], ...restFormProps } = meta
    return (
      <NSpin show={loading}>
        <NForm {...restFormProps} rules={meta.rules as FormRules} ref='formRef'>
          <NGrid {...layout}>
            {elements.map((element) => {
              const { span = 24, path, widget, ...formItemProps } = element

              return (
                <NFormItemGi
                  {...formItemProps}
                  span={unref(span) === void 0 ? 24 : unref(span)}
                  path={path}
                  key={path || String(Date.now() + Math.random())}
                >
                  {h(widget)}
                </NFormItemGi>
              )
            })}
          </NGrid>
        </NForm>
      </NSpin>
    )
  }
})

export default Form
