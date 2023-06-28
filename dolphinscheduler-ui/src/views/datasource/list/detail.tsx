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
import {
  NButton,
  NSpin,
  NForm,
  NFormItem,
  NSelect,
  NInput,
  NInputNumber,
  NRadioGroup,
  NRadio,
  NSpace
} from 'naive-ui'
import Modal from '@/components/modal'
import { useI18n } from 'vue-i18n'
import { useForm, datasourceType } from './use-form'
import { useDetail } from './use-detail'
import styles from './index.module.scss'

const props = {
  show: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  id: {
    type: Number as PropType<number>
  },
  selectType: {
    type: String as PropType<any>,
    default: 'MYSQL'
  }
}

const DetailModal = defineComponent({
  name: 'DetailModal',
  props,
  emits: ['cancel', 'update', 'open'],
  setup(props, ctx) {
    const { t } = useI18n()

    const {
      state,
      changeType,
      changePort,
      resetFieldsValue,
      setFieldsValue,
      getFieldsValue
    } = useForm(props.id)

    const { status, queryById, testConnect, createOrUpdate } =
      useDetail(getFieldsValue)

    const onCancel = () => {
      resetFieldsValue()
      ctx.emit('cancel')
    }

    const onSubmit = async () => {
      await state.detailFormRef.validate()
      const res = await createOrUpdate(props.id)
      if (res) {
        onCancel()
        ctx.emit('update')
      }
    }

    const onTest = async () => {
      await state.detailFormRef.validate()
      testConnect()
    }

    const onChangeType = changeType
    const onChangePort = changePort

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    const handleSourceModalOpen = () => {
      ctx.emit('open')
    }

    watch(
      () => props.show,
      async () => {
        state.detailForm.type = props.selectType
        state.detailForm.label =
          props.selectType === 'HIVE' ? 'HIVE/IMPALA' : props.selectType
        props.show &&
          state.detailForm.type &&
          (await changeType(
            state.detailForm.type,
            datasourceType[state.detailForm.type]
          ))
        props.show && props.id && setFieldsValue(await queryById(props.id))
      }
    )

    watch(
      () => props.selectType,
      async () => {
        state.detailForm.type = props.selectType
        state.detailForm.label =
          props.selectType === 'HIVE' ? 'HIVE/IMPALA' : props.selectType
        state.detailForm.type &&
          (await changeType(
            state.detailForm.type,
            datasourceType[state.detailForm.type]
          ))
      }
    )

    return {
      t,
      ...toRefs(state),
      ...toRefs(status),
      onChangeType,
      onChangePort,
      onSubmit,
      onTest,
      onCancel,
      trim,
      handleSourceModalOpen
    }
  },
  render() {
    const {
      show,
      id,
      t,
      detailForm,
      rules,
      requiredDataBase,
      showHost,
      showPort,
      showAwsRegion,
      showCompatibleMode,
      showConnectType,
      showPrincipal,
      showMode,
      showDataBaseName,
      showJDBCConnectParameters,
      showPublicKey,
      modeOptions,
      redShitModeOptions,
      loading,
      saving,
      testing,
      onChangePort,
      onCancel,
      onTest,
      onSubmit,
      handleSourceModalOpen
    } = this
    return (
      <Modal
        class='dialog-create-data-source'
        show={show}
        title={`${t(id ? 'datasource.edit' : 'datasource.create')}${t(
          'datasource.datasource'
        )}`}
        onConfirm={onSubmit}
        confirmLoading={saving || loading}
        onCancel={onCancel}
        confirmClassName='btn-submit'
        cancelClassName='btn-cancel'
      >
        {{
          default: () => (
            <NSpin show={loading}>
              <NForm
                rules={rules}
                ref='detailFormRef'
                require-mark-placement='left'
                label-align='left'
              >
                <NFormItem
                  label={t('datasource.datasource')}
                  path='type'
                  show-require-mark
                >
                  <div class={[styles.typeBox, !!id && styles.disabledBox]}>
                    <div v-model={[detailForm.type, 'value']}>
                      {detailForm.label}
                    </div>
                    <div
                      class={[
                        styles['text-color'],
                        'btn-data-source-type-drop-down'
                      ]}
                      onClick={handleSourceModalOpen}
                    >
                      {t('datasource.select')}
                    </div>
                  </div>
                </NFormItem>
                <NFormItem
                  label={t('datasource.datasource_name')}
                  path='name'
                  show-require-mark
                >
                  <NInput
                    allowInput={this.trim}
                    class='input-data-source-name'
                    v-model={[detailForm.name, 'value']}
                    maxlength={60}
                    placeholder={t('datasource.datasource_name_tips')}
                  />
                </NFormItem>
                <NFormItem label={t('datasource.description')} path='note'>
                  <NInput
                    class='input-data-source-description'
                    v-model={[detailForm.note, 'value']}
                    type='textarea'
                    placeholder={t('datasource.description_tips')}
                  />
                </NFormItem>
                <NFormItem
                  v-show={showHost}
                  label={t('datasource.ip')}
                  path='host'
                  show-require-mark
                >
                  <NInput
                    allowInput={this.trim}
                    class='input-ip'
                    v-model={[detailForm.host, 'value']}
                    type='text'
                    maxlength={255}
                    placeholder={t('datasource.ip_tips')}
                  />
                </NFormItem>
                <NFormItem
                  v-show={showPort}
                  label={t('datasource.port')}
                  path='port'
                  show-require-mark={
                    !(showMode && detailForm.mode === 'IAM-accessKey')
                  }
                >
                  <NInputNumber
                    class='input-port'
                    v-model={[detailForm.port, 'value']}
                    show-button={false}
                    placeholder={t('datasource.port_tips')}
                    on-blur={onChangePort}
                    style={{ width: '100%' }}
                  />
                </NFormItem>
                <NFormItem
                  v-show={showPrincipal}
                  label='Principal'
                  path='principal'
                  show-require-mark
                >
                  <NInput
                    allowInput={this.trim}
                    v-model={[detailForm.principal, 'value']}
                    type='text'
                    placeholder={t('datasource.principal_tips')}
                  />
                </NFormItem>
                <NFormItem
                  v-show={showPrincipal}
                  label='krb5.conf'
                  path='javaSecurityKrb5Conf'
                >
                  <NInput
                    allowInput={this.trim}
                    v-model={[detailForm.javaSecurityKrb5Conf, 'value']}
                    type='text'
                    placeholder={t('datasource.krb5_conf_tips')}
                  />
                </NFormItem>
                {/* 验证条件选择 */}
                <NFormItem
                  v-show={showMode}
                  label={t('datasource.validation')}
                  path='mode'
                  show-require-mark
                >
                  <NSelect
                    v-model={[detailForm.mode, 'value']}
                    options={
                      detailForm.type === 'REDSHIFT'
                        ? redShitModeOptions
                        : modeOptions
                    }
                  ></NSelect>
                </NFormItem>
                {/* SqlPassword */}
                <NFormItem
                  v-show={showMode && detailForm.mode === 'SqlPassword'}
                  label={t('datasource.database_username')}
                  path='userName'
                  show-require-mark
                >
                  <NInput
                    allowInput={this.trim}
                    v-model={[detailForm.userName, 'value']}
                    type='text'
                    placeholder={t('datasource.database_username')}
                  />
                </NFormItem>
                <NFormItem
                  v-show={showMode && detailForm.mode === 'SqlPassword'}
                  label={t('datasource.database_password')}
                  path='password'
                  show-require-mark
                >
                  <NInput
                    allowInput={this.trim}
                    v-model={[detailForm.password, 'value']}
                    type='password'
                    placeholder={t('datasource.database_password')}
                  />
                </NFormItem>
                {/* ActiveDirectoryPassword */}
                <NFormItem
                  v-show={
                    showMode && detailForm.mode === 'ActiveDirectoryPassword'
                  }
                  label={t('datasource.Azure_AD_username')}
                  path='userName'
                  show-require-mark
                >
                  <NInput
                    allowInput={this.trim}
                    v-model={[detailForm.userName, 'value']}
                    type='text'
                    placeholder={t('datasource.Azure_AD_username')}
                  />
                </NFormItem>
                <NFormItem
                  v-show={
                    showMode && detailForm.mode === 'ActiveDirectoryPassword'
                  }
                  label={t('datasource.Azure_AD_password')}
                  path='password'
                  show-require-mark
                >
                  <NInput
                    allowInput={this.trim}
                    v-model={[detailForm.password, 'value']}
                    type='password'
                    placeholder={t('datasource.Azure_AD_password')}
                  />
                </NFormItem>
                {/* ActiveDirectoryMSI */}
                <NFormItem
                  v-show={showMode && detailForm.mode === 'ActiveDirectoryMSI'}
                  label={t('datasource.MSIClientId')}
                  path='MSIClientId'
                >
                  <NInput
                    allowInput={this.trim}
                    v-model={[detailForm.MSIClientId, 'value']}
                    type='password'
                    placeholder={t('datasource.MSIClientId')}
                  />
                </NFormItem>
                {/* ActiveDirectoryServicePrincipal */}
                <NFormItem
                  v-show={
                    showMode &&
                    detailForm.mode === 'ActiveDirectoryServicePrincipal'
                  }
                  label={t('datasource.clientId')}
                  path='userName'
                  show-require-mark
                >
                  <NInput
                    allowInput={this.trim}
                    v-model={[detailForm.userName, 'value']}
                    type='text'
                    placeholder={t('datasource.clientId')}
                  />
                </NFormItem>
                <NFormItem
                  v-show={
                    showMode &&
                    detailForm.mode === 'ActiveDirectoryServicePrincipal'
                  }
                  label={t('datasource.clientSecret')}
                  path='password'
                  show-require-mark
                >
                  <NInput
                    allowInput={this.trim}
                    v-model={[detailForm.password, 'value']}
                    type='password'
                    placeholder={t('datasource.clientSecret')}
                  />
                </NFormItem>
                {/* accessToken */}
                <NFormItem
                  v-show={showMode && detailForm.mode === 'accessToken'}
                  label={t('datasource.clientId')}
                  path='userName'
                  show-require-mark
                >
                  <NInput
                    allowInput={this.trim}
                    v-model={[detailForm.userName, 'value']}
                    type='text'
                    placeholder={t('datasource.clientId')}
                  />
                </NFormItem>
                <NFormItem
                  v-show={showMode && detailForm.mode === 'accessToken'}
                  label={t('datasource.clientSecret')}
                  path='password'
                  show-require-mark
                >
                  <NInput
                    allowInput={this.trim}
                    v-model={[detailForm.password, 'value']}
                    type='password'
                    placeholder={t('datasource.clientSecret')}
                  />
                </NFormItem>
                <NFormItem
                  v-show={showMode && detailForm.mode === 'accessToken'}
                  label={t('datasource.OAuth_token_endpoint')}
                  path='endpoint'
                  show-require-mark
                >
                  <NInput
                    allowInput={this.trim}
                    v-model={[detailForm.endpoint, 'value']}
                    type='text'
                    placeholder={t('datasource.OAuth_token_endpoint')}
                  />
                </NFormItem>
                <NFormItem
                  v-show={showMode && detailForm.mode === 'IAM-accessKey'}
                  label={t('datasource.AccessKeyID')}
                  path='userName'
                  show-require-mark
                >
                  <NInput
                    allowInput={this.trim}
                    v-model={[detailForm.userName, 'value']}
                    type='text'
                    maxlength={60}
                    placeholder={t('datasource.AccessKeyID_tips')}
                  />
                </NFormItem>
                <NFormItem
                  v-show={showMode && detailForm.mode === 'IAM-accessKey'}
                  label={t('datasource.SecretAccessKey')}
                  path='password'
                  show-require-mark
                >
                  <NInput
                    allowInput={this.trim}
                    v-model={[detailForm.password, 'value']}
                    type='password'
                    placeholder={t('datasource.SecretAccessKey_tips')}
                  />
                </NFormItem>
                <NFormItem
                  v-show={showMode && detailForm.mode === 'IAM-accessKey'}
                  label={t('datasource.dbUser')}
                  path='dbUser'
                  show-require-mark
                >
                  <NInput
                    allowInput={this.trim}
                    class='input-dbUser'
                    v-model={[detailForm.dbUser, 'value']}
                    type='text'
                    placeholder={t('datasource.dbUser_tips')}
                  />
                </NFormItem>
                <NFormItem
                  v-show={showPrincipal}
                  label='keytab.username'
                  path='loginUserKeytabUsername'
                >
                  <NInput
                    allowInput={this.trim}
                    v-model={[detailForm.loginUserKeytabUsername, 'value']}
                    type='text'
                    placeholder={t('datasource.keytab_username_tips')}
                  />
                </NFormItem>
                <NFormItem
                  v-show={showPrincipal}
                  label='keytab.path'
                  path='loginUserKeytabPath'
                >
                  <NInput
                    allowInput={this.trim}
                    v-model={[detailForm.loginUserKeytabPath, 'value']}
                    type='text'
                    placeholder={t('datasource.keytab_path_tips')}
                  />
                </NFormItem>
                <NFormItem
                  v-show={!showMode || detailForm.mode === 'password'}
                  label={t('datasource.user_name')}
                  path='userName'
                  show-require-mark
                >
                  <NInput
                    allowInput={this.trim}
                    class='input-username'
                    v-model={[detailForm.userName, 'value']}
                    type='text'
                    maxlength={60}
                    placeholder={t('datasource.user_name_tips')}
                  />
                </NFormItem>
                <NFormItem
                  v-show={!showMode || detailForm.mode === 'password'}
                  label={t('datasource.user_password')}
                  path='password'
                >
                  <NInput
                    allowInput={this.trim}
                    class='input-password'
                    v-model={[detailForm.password, 'value']}
                    type='password'
                    placeholder={t('datasource.user_password_tips')}
                  />
                </NFormItem>
                <NFormItem
                  v-show={showAwsRegion}
                  label={t('datasource.aws_region')}
                  path='awsRegion'
                  show-require-mark
                >
                  <NInput
                    allowInput={this.trim}
                    v-model={[detailForm.awsRegion, 'value']}
                    type='text'
                    maxlength={60}
                    placeholder={t('datasource.aws_region_tips')}
                  />
                </NFormItem>
                <NFormItem
                  v-show={showDataBaseName}
                  label={t('datasource.database_name')}
                  path='database'
                  show-require-mark={requiredDataBase}
                >
                  <NInput
                    allowInput={this.trim}
                    class='input-data-base'
                    v-model={[detailForm.database, 'value']}
                    type='text'
                    maxlength={60}
                    placeholder={t('datasource.database_name_tips')}
                  />
                </NFormItem>
                {detailForm.type === 'SNOWFLAKE' && (
                  <NFormItem
                    label={t('datasource.datawarehouse')}
                    path='datawarehouse'
                    show-require-mark
                  >
                    <NInput
                      allowInput={this.trim}
                      class='input-datawarehouse'
                      v-model={[detailForm.datawarehouse, 'value']}
                      maxlength={60}
                      placeholder={t('datasource.datawarehouse_tips')}
                    />
                  </NFormItem>
                )}
                <NFormItem
                  v-show={showConnectType}
                  label={t('datasource.oracle_connect_type')}
                  path='connectType'
                  show-require-mark
                >
                  <NRadioGroup v-model={[detailForm.connectType, 'value']}>
                    <NSpace>
                      <NRadio value='ORACLE_SERVICE_NAME'>
                        {t('datasource.oracle_service_name')}
                      </NRadio>
                      <NRadio value='ORACLE_SID'>
                        {t('datasource.oracle_sid')}
                      </NRadio>
                    </NSpace>
                  </NRadioGroup>
                </NFormItem>
                <NFormItem
                  v-show={showCompatibleMode}
                  label={t('datasource.compatible_mode')}
                  path='compatibleMode'
                  show-require-mark
                >
                  <NInput
                    allowInput={this.trim}
                    class='input-data-base'
                    v-model={[detailForm.compatibleMode, 'value']}
                    type='text'
                    maxlength={60}
                    placeholder={t('datasource.compatible_mode_tips')}
                  />
                </NFormItem>
                <NFormItem
                  v-show={showJDBCConnectParameters}
                  label={t('datasource.jdbc_connect_parameters')}
                  path='other'
                >
                  <NInput
                    allowInput={this.trim}
                    class='input-jdbc-params'
                    v-model={[detailForm.other, 'value']}
                    type='textarea'
                    autosize={{
                      minRows: 2
                    }}
                    placeholder={`${t(
                      'datasource.format_tips'
                    )} {"key1":"value1","key2":"value2"...} ${t(
                      'datasource.connection_parameter'
                    )}`}
                  />
                </NFormItem>
                <NFormItem
                  v-show={showPublicKey}
                  label='PublicKey'
                  path='publicKey'
                >
                  <NInput
                    v-model={[detailForm.publicKey, 'value']}
                    type='textarea'
                    autosize={{
                      minRows: 4
                    }}
                  />
                </NFormItem>
              </NForm>
            </NSpin>
          ),
          'btn-middle': () => (
            <NButton
              class='btn-test-connection'
              type='primary'
              size='small'
              onClick={onTest}
              loading={testing || loading}
            >
              {t('datasource.test_connect')}
            </NButton>
          )
        }}
      </Modal>
    )
  }
})

export default DetailModal
