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
import { useProfile } from '../use-profile'
import { useSetting } from '../use-setting'
import styles from '../info.module.scss'
import { ref } from 'vue'
import Timezone from '../../../layouts/content/components/timezone';

const props =  {
  type: String as PropType<string>,
}

const Info = defineComponent({
  name: 'Info',
  props: {type: String},
  setup(props) {
    const type = props.type;
    return {
      type
    }
  },
  render() {
    const { infoOptions } = useProfile()
    const { settingOptions } = useSetting()

    let options;

    if (!props.type || this.type === 'profile') {
      options = infoOptions;
    }

    if (this.type === 'setting') {
      options = settingOptions
    }

    return (
      <dl class={styles.container}>
        {(options || ref([])).value.map((item) => {
          // Todo: custome JSX element based on item key; eg., for Timer option, return TimeZone
          if (item.key === 'Time Zone') {
            return <Timezone timezoneOptions={[{label: 'Africa/Abidjan', value: 'Africa/Abidjan'}]} />
          }
          return (
            <dd class={styles.item}>
              <span class={styles.label}>{item.key}: </span>
              <span>{item.value}</span>
            </dd>
          )
        })}
      </dl>
    )
  }
})

export default Info
