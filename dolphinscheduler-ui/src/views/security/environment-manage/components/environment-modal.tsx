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
import Modal from '@/components/modal'
import { NForm, NFormItem, NInput, NSelect } from 'naive-ui'
import { useModal } from './use-modal'
import { useI18n } from 'vue-i18n'

const envConfigPlaceholder =
  'export HADOOP_HOME=/opt/hadoop-2.6.5\n' +
  'export HADOOP_CONF_DIR=/etc/hadoop/conf\n' +
  'export SPARK_HOME=/opt/soft/spark\n' +
  'export PYTHON_HOME=/opt/soft/python\n' +
  'export JAVA_HOME=/opt/java/jdk1.8.0_181-amd64\n' +
  'export HIVE_HOME=/opt/soft/hive\n' +
  'export FLINK_HOME=/opt/soft/flink\n' +
  'export DATAX_HOME=/opt/soft/datax\n' +
  'export YARN_CONF_DIR=/etc/hadoop/conf\n' +
  'export PATH=$HADOOP_HOME/bin:$SPARK_HOME/bin:$PYTHON_HOME/bin:$JAVA_HOME/bin:$HIVE_HOME/bin:$FLINK_HOME/bin:$DATAX_HOME/bin:$PATH\n' +
  'export HADOOP_CLASSPATH=`hadoop classpath`\n'

const EnvironmentModal = defineComponent({
  name: 'EnvironmentModal',
  props: {
    showModalRef: {
      type: Boolean as PropType<boolean>,
      default: false
    },
    statusRef: {
      type: Number as PropType<number>,
      default: 0
    },
    row: {
      type: Object as PropType<any>,
      default: {}
    }
  },
  emits: ['cancelModal', 'confirmModal'],
  setup(props, ctx) {
    const { variables, handleValidate, getListData } = useModal(props, ctx)
    const { t } = useI18n()

    const cancelModal = () => {
      if (props.statusRef === 0) {
        variables.model.name = ''
        variables.model.config = ''
        variables.model.description = ''
        variables.model.workerGroups = []
      }
      ctx.emit('cancelModal', props.showModalRef)
    }

    const confirmModal = () => {
      handleValidate(props.statusRef)
    }

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    watch(
      () => props.showModalRef,
      () => {
        props.showModalRef && getListData()
      }
    )

    watch(
      () => props.statusRef,
      () => {
        if (props.statusRef === 0) {
          variables.model.name = ''
          variables.model.config = ''
          variables.model.description = ''
          variables.model.workerGroups = []
        } else {
          variables.model.code = props.row.code
          variables.model.name = props.row.name
          variables.model.config = props.row.config
          variables.model.description = props.row.description
          variables.model.workerGroups = props.row.workerGroups
        }
      }
    )

    watch(
      () => props.row,
      () => {
        variables.model.code = props.row.code
        variables.model.name = props.row.name
        variables.model.config = props.row.config
        variables.model.description = props.row.description
        variables.model.workerGroups = props.row.workerGroups
      }
    )

    return { t, ...toRefs(variables), cancelModal, confirmModal, trim }
  },
  render() {
    const { t } = this
    return (
      <div>
        <Modal
          title={
            this.statusRef === 0
              ? t('security.environment.create_environment')
              : t('security.environment.edit_environment')
          }
          show={this.showModalRef}
          onCancel={this.cancelModal}
          onConfirm={this.confirmModal}
          confirmDisabled={
            !this.model.name || !this.model.config || !this.model.description
          }
          confirmClassName='btn-submit'
          cancelClassName='btn-cancel'
          confirmLoading={this.saving}
        >
          {{
            default: () => (
              <NForm
                model={this.model}
                rules={this.rules}
                ref='environmentFormRef'
              >
                <NFormItem
                  label={t('security.environment.environment_name')}
                  path='name'
                >
                  <NInput
                    allowInput={this.trim}
                    class='input-environment-name'
                    placeholder={t(
                      'security.environment.environment_name_tips'
                    )}
                    v-model={[this.model.name, 'value']}
                  />
                </NFormItem>
                <NFormItem
                  label={t('security.environment.environment_config')}
                  path='config'
                >
                  <NInput
                    class='input-environment-config'
                    placeholder={envConfigPlaceholder}
                    type='textarea'
                    autosize={{ minRows: 16 }}
                    v-model={[this.model.config, 'value']}
                  />
                </NFormItem>
                <NFormItem
                  label={t('security.environment.environment_desc')}
                  path='description'
                >
                  <NInput
                    allowInput={this.trim}
                    class='input-environment-desc'
                    placeholder={t(
                      'security.environment.environment_description_tips'
                    )}
                    v-model={[this.model.description, 'value']}
                  />
                </NFormItem>
                <NFormItem
                  label={t('security.environment.worker_groups')}
                  path='workerGroups'
                >
                  <NSelect
                    class='input-environment-worker-group'
                    multiple
                    placeholder={t('security.environment.worker_group_tips')}
                    options={this.model.generalOptions}
                    v-model={[this.model.workerGroups, 'value']}
                  />
                </NFormItem>
              </NForm>
            )
          }}
        </Modal>
      </div>
    )
  }
})

export default EnvironmentModal
