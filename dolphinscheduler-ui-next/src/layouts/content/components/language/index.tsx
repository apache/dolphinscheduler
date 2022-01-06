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

import { defineComponent, ref, PropType, onMounted } from 'vue'
import { NDropdown, NIcon, NButton } from 'naive-ui'
import styles from './index.module.scss'
import { DownOutlined } from '@vicons/antd'
import { useDropDown } from './use-dropdown'
import { useLanguageStore } from '@/store/language/language'

const Language = defineComponent({
  name: 'Language',
  props: {
    languageOptions: {
      type: Array as PropType<any>,
      default: [],
    },
  },
  setup(props) {
    const languageStore = useLanguageStore()
    const lang = ref()
    lang.value = languageStore.getLang
    
    const chooseVal = ref(props.languageOptions.filter((item: { key: string }) => item.key === lang.value)[0].label)
    
    const { handleSelect } = useDropDown(chooseVal)

    return { handleSelect, chooseVal }
  },
  render() {
    return (
      <NDropdown
        trigger='hover'
        show-arrow
        options={this.languageOptions}
        on-select={this.handleSelect}
      >
        <NButton text>
          {this.chooseVal}
          <NIcon class={styles.icon}>
            <DownOutlined />
          </NIcon>
        </NButton>
      </NDropdown>
    )
  },
})

export default Language
