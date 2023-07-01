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

import { defineComponent, withKeys, PropType } from 'vue'
import { NInput } from 'naive-ui'
import { useI18n } from 'vue-i18n'

const props = {
  placeholder: {
    type: String as PropType<string>,
    required: false
  }
}

const Search = defineComponent({
  name: 'Search',
  props: props,
  emits: ['search', 'clear'],
  setup(props, ctx) {
    const { t } = useI18n()

    const onKeyDown = (ev: KeyboardEvent) => {
      ctx.emit('search', (ev.target as HTMLInputElement)?.value || '')
    }
    const onClear = (ev: Event) => {
      ctx.emit('clear', (ev.target as HTMLInputElement)?.value || '')
    }
    return () => (
      <NInput
        size='small'
        clearable
        placeholder={
          props.placeholder ? props.placeholder : t('input_search.placeholder')
        }
        onKeydown={withKeys(onKeyDown, ['enter'])}
        onClear={onClear}
      />
    )
  }
})

export default Search
