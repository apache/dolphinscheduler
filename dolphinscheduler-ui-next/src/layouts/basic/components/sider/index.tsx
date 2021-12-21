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
import { NLayoutSider, NMenu } from "naive-ui";

const Sider = defineComponent({
  name: 'Sider',
  props:{
    visible: {
      type:Boolean,
      default: true
    },
    inverted: {
      type:Boolean,
      default: true
    },
    menuOptions: {
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
      console.log(data)
    }

    return { handleMenuClick }
  },
  render() {
    if(this.visible) {
      return (
          <NLayoutSider
              width={240}
              collapseMode={'width'}
              collapsedWidth={64}
              inverted={this.inverted}
              nativeScrollbar={false}
              show-trigger
              bordered>
            <NMenu
                onUpdate:value={this.handleMenuClick}
                inverted={this.inverted}
                collapsedWidth={64}
                collapsedIconSize={22}
                options={this.menuOptions}
            />
          </NLayoutSider>
      )
    } else {
      return ('')
    }
  },
})

export { Sider };
