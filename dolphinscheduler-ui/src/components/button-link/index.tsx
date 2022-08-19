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
import { defineComponent, PropType, renderSlot } from 'vue'
import { NButton } from 'naive-ui'
import styles from './index.module.scss'

const props = {
  disabled: {
    type: Boolean,
    default: false
  },
  iconPlacement: {
    type: String as PropType<'left' | 'right'>,
    default: 'left'
  }
}

const ButtonLink = defineComponent({
  name: 'button-link',
  props,
  emits: ['click'],
  setup(props, { slots, emit }) {
    const onClick = (ev: MouseEvent) => {
      emit('click', ev)
    }
    return () => (
      <NButton {...props} onClick={onClick} text class={styles['button-link']}>
        {{
          default: () => renderSlot(slots, 'default'),
          icon: () => renderSlot(slots, 'icon')
        }}
      </NButton>
    )
  }
})

export default ButtonLink
