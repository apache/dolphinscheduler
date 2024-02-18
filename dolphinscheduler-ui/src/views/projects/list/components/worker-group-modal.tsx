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

import {
  defineComponent,
  getCurrentInstance,
  PropType,
  toRefs,
  watch
} from 'vue'
import { NTransfer} from 'naive-ui'
import { reactive, ref, SetupContext } from 'vue'
import Modal from '@/components/modal'
import { useUserStore } from '@/store/user/user'
import type { UserInfoRes } from '@/service/modules/users/types'
import styles from "@/views/security/user-manage/index.module.scss";
import {useWorkerGroup} from "@/views/projects/list/components/use-worker-group";
import {queryAllWorkerGroups} from "@/service/modules/worker-groups";
import {Option} from "naive-ui/es/transfer/src/interface";

const props = {
  showModalRef: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  row: {
    type: Object as PropType<any>,
    default: {}
  }
}

const WorkerGroupModal = defineComponent({
  name: 'WorkerGroupModal',
  props,
  emits: ['cancelModal', 'confirmModal'],
  setup(props, ctx) {
    const { variables, t, handleValidate } = useWorkerGroup(props, ctx)

    const userStore = useUserStore()

    const cancelModal = () => {
      ctx.emit('cancelModal', props.showModalRef)
    }

    const confirmModal = () => {

    }
    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    const workerGroupOptions:Option[]  = []

    const createOptions = () => {
      queryAllWorkerGroups().then((res: any) => {
        for (const workerGroup of res) {
          workerGroupOptions.push({label: workerGroup, value: workerGroup})
        }
      })
    }

    const assignedWorkerGroups = ref([])


    createOptions()

    // variables.model.assignedWorkerGroups
    // variables.model.workerGroupOptions = createWorkerGroupOptions()

    watch(
        () => props.row,
        () => {
          variables.model.projectName = props.row.name
          variables.model.userName = props.row.userName
          variables.model.description = props.row.description
        }
    )

    return { ...toRefs(variables), t, cancelModal, confirmModal, workerGroupOptions, assignedWorkerGroups, trim }
  },
  render() {
    const { t } = this
    return (
        <Modal
            title={t('project.list.assign_worker_group')}
            show={this.showModalRef}
            onConfirm={this.confirmModal}
            onCancel={this.cancelModal}
            confirmClassName='btn-submit'
            cancelClassName='btn-cancel'
        >
          <NTransfer
              virtualScroll
              class={styles.transfer}
              options={this.workerGroupOptions}
              v-model:value={this.assignedWorkerGroups}
          />
        </Modal>
    )
  }
})

export default WorkerGroupModal
