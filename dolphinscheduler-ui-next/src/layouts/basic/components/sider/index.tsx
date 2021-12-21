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

import { defineComponent, ref } from 'vue'

import styles from './index.module.scss'
import {NDropdown, NIcon, NLayoutHeader, NMenu} from "naive-ui";
import {Logo} from "@/layouts/basic/components/logo";
import {IosArrowDown} from "@vicons/ionicons4";
import {UserAlt} from "@vicons/fa";

const Header = defineComponent({
  name: 'Header',
  props:{
    inverted: {
      type:Boolean,
      default: true
    },
    menuOptions: {
      type: Array,
      default: []
    },
    languageOptions: {
      type: Array,
      default: []
    },
    profileOptions: {
      type: Array,
      default: []
    },
    currentMenu: {
      type: Object
    },
    defaultMenuKey: {
      type: String
    }
  },
  setup(props) {
    const currentMenu = ref({})

    function handleMenuClick(key, data) {
      currentMenu.value = data
    }

    return { handleMenuClick }
  },
  render() {
    return (
        <NLayoutHeader class={styles['header-model']} inverted={this.inverted} bordered>
          <Logo/>
          <div class={styles.nav}>
            <NMenu mode='horizontal'
                   onUpdate:value={this.handleMenuClick}
                   class={styles.menu}
                   inverted={this.inverted}
                   options={this.menuOptions}/>
            <div class={styles.profile}>
              <NDropdown inverted={this.inverted} options={this.languageOptions}>
                <span>
                 中文<NIcon class={styles.icon}><IosArrowDown/></NIcon>
                </span>
              </NDropdown>
              <NDropdown inverted={this.inverted} options={this.profileOptions}>
                <span>
                  <NIcon class={styles.icon}><UserAlt/></NIcon>
                  admin
                  <NIcon class={styles.icon}><IosArrowDown/></NIcon>
                </span>
              </NDropdown>
            </div>
          </div>
        </NLayoutHeader>
    )
  },
})

export { Header };
