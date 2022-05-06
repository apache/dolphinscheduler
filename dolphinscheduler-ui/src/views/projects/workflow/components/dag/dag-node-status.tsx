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

import { NTooltip, NSpin, NIcon } from 'naive-ui'
import { defineComponent, PropType, h } from 'vue'
import styles from './status.module.scss'

const props = {
  t: {
    type: Function as PropType<any>,
    require: true
  },
  taskInstance: {
    type: Object as PropType<any>,
    require: true
  },
  stateProps: {
    type: Object as PropType<any>,
    require: true
  }
}

export default defineComponent({
  name: 'dag-node-status',
  props,
  setup(props) {
    const iconElement = h(
      NIcon,
      {
        size: 20
      },
      {
        default: () =>
          h(props.stateProps.icon, {
            color: props.stateProps.color
          })
      }
    )
    return {
      iconElement
    }
  },
  render() {
    return (
      <NTooltip placement='top'>
        {{
          trigger: () => {
            if (this.stateProps.isSpin) {
              return h(
                NSpin,
                {
                  size: 20
                },
                {
                  icon: () => this.iconElement
                }
              )
            } else {
              return this.iconElement
            }
          },
          default: () => (
            <ul class={styles['status-info']}>
              <li>
                {this.$props.t('project.workflow.name')}:{' '}
                {this.taskInstance.name}
              </li>
              <li>
                {this.$props.t('project.workflow.status')}:{' '}
                {this.stateProps.desc}
              </li>
              <li>
                {this.$props.t('project.workflow.type')}:{' '}
                {this.taskInstance.taskType}
              </li>
              <li>
                {this.$props.t('project.workflow.host')}:{' '}
                {this.taskInstance.host || '-'}
              </li>
              <li>
                {this.$props.t('project.workflow.retry_count')}:{' '}
                {this.taskInstance.retryTimes}
              </li>
              <li>
                {this.$props.t('project.workflow.submit_time')}:{' '}
                {this.taskInstance.submitTime}
              </li>
              <li>
                {this.$props.t('project.workflow.start_time')}:{' '}
                {this.taskInstance.startTime}
              </li>
              <li>
                {this.$props.t('project.workflow.end_time')}:{' '}
                {this.taskInstance.endTime ? this.taskInstance.endTime : '-'}
              </li>
            </ul>
          )
        }}
      </NTooltip>
    )
  }
})
