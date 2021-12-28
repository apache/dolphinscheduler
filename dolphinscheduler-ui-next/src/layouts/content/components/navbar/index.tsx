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

import { defineComponent, PropType } from 'vue'
import styles from './index.module.scss'
import { NMenu } from 'naive-ui'
import Logo from '../logo'
import Language from '../language'
import User from '../user'
import { useMenuClick } from './use-menuClick'

const navbar = defineComponent({
  name: 'navbar',
  props: {
    headerMenuOptions: {
      type: Array as PropType<any>,
      default: [],
    },
    languageOptions: {
      type: Array as PropType<any>,
      default: [],
    },
    profileOptions: {
      type: Array as PropType<any>,
      default: [],
    },
  },
  setup(props, ctx) {
    const { handleMenuClick } = useMenuClick(ctx)
    return { handleMenuClick }
  },
  render() {
    return (
      <div class={styles.container}>
        <Logo />
        <div class={styles.nav}>
          <NMenu
            default-value='home'
            mode='horizontal'
            options={this.headerMenuOptions}
            onUpdateValue={this.handleMenuClick}
          />
        </div>
        <div class={styles.settings}>
          <Language languageOptions={this.languageOptions} />
          <User profileOptions={this.profileOptions} />
        </div>
      </div>
    )
  },
})

export default navbar
