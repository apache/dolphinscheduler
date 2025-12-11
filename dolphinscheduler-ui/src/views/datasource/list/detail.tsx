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
    ),

    //monitor authType change，update headerPrefix
    watch(
      () => state.detailForm.authConfig?.authType,
      (newAuthType) => {
        if (state.detailForm.type === 'THIRDPARTY_SYSTEM_CONNECTOR' && state.detailForm.authConfig) {
          if (newAuthType === 'BASIC_AUTH') {
            state.detailForm.authConfig.headerPrefix = 'Basic'
          } else if (newAuthType === 'JWT' || newAuthType === 'OAUTH2') {
            state.detailForm.authConfig.headerPrefix = 'Bearer'
          } else {
            state.detailForm.authConfig.headerPrefix = ''
          }
        }
      },
      { immediate: true }
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
      showRestEndpoint,
      showAccessKeyId,
      showAccessKeySecret,
      showRegionId,
      showEndpoint,
      showAwsRegion,
      showCompatibleMode,
      showConnectType,
      showPrincipal,
      showMode,
      showDataBaseName,
      showJDBCConnectParameters,
      showPrivateKey,
      showNamespace,
      showKubeConfig,
      modeOptions,
      redShiftModeOptions,
      sagemakerModeOption,
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
                  v-show={showRestEndpoint}
                  label={t('datasource.zeppelin_rest_endpoint')}
                  path='restEndPoint'
                  show-require-mark
                >
                  <NInput
                    allowInput={this.trim}
                    class='input-zeppelin_rest_endpoint'
                    v-model={[detailForm.restEndpoint, 'value']}
                    type='text'
                    maxlength={255}
                    placeholder={t('datasource.zeppelin_rest_endpoint_tips')}
                  />
                </NFormItem>
                <NFormItem
                  v-show={showAccessKeyId}
                  label={t('datasource.access_key_id')}
                  path='accessKeyId'
                  show-require-mark
                >
                  <NInput
                    allowInput={this.trim}
                    class='input-access_key_id'
                    v-model={[detailForm.accessKeyId, 'value']}
                    type='text'
                    maxlength={255}
                    placeholder={t('datasource.access_key_id_tips')}
                  />
                </NFormItem>
                <NFormItem
                  v-show={showAccessKeySecret}
                  label={t('datasource.access_key_secret')}
                  path='accessKeySecret'
                  show-require-mark
                >
                  <NInput
                    allowInput={this.trim}
                    class='input-access_key_secret'
                    v-model={[detailForm.accessKeySecret, 'value']}
                    type='text'
                    maxlength={255}
                    placeholder={t('datasource.access_key_secret_tips')}
                  />
                </NFormItem>
                <NFormItem
                  v-show={showRegionId}
                  label={t('datasource.region_id')}
                  path='regionId'
                  show-require-mark
                >
                  <NInput
                    allowInput={this.trim}
                    class='input-region_id'
                    v-model={[detailForm.regionId, 'value']}
                    type='text'
                    maxlength={255}
                    placeholder={t('datasource.region_id_tips')}
                  />
                </NFormItem>
                <NFormItem
                  v-show={showEndpoint}
                  label={t('datasource.endpoint')}
                  path='endpoint'
                >
                  <NInput
                    allowInput={this.trim}
                    class='input-endpoint'
                    v-model={[detailForm.endpoint, 'value']}
                    type='text'
                    maxlength={255}
                    placeholder={t('datasource.endpoint_tips')}
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
                        ? redShiftModeOptions
                        : detailForm.type === 'SAGEMAKER'
                        ? sagemakerModeOption
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
                  v-show={
                    showMode &&
                    detailForm.mode === 'IAM-accessKey' &&
                    detailForm.type != 'SAGEMAKER'
                  }
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
                  v-show={
                    (!showMode || detailForm.mode === 'password') &&
                    detailForm.type != 'K8S' &&
                    detailForm.type != 'ALIYUN_SERVERLESS_SPARK' &&
                    detailForm.type != 'THIRDPARTY_SYSTEM_CONNECTOR'
                  }
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
                  v-show={
                    (!showMode || detailForm.mode === 'password') &&
                    detailForm.type != 'K8S' &&
                    detailForm.type != 'ALIYUN_SERVERLESS_SPARK' &&
                    detailForm.type != 'THIRDPARTY_SYSTEM_CONNECTOR'
                  }
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
                  v-show={showPrivateKey}
                  label='PrivateKey'
                  path='privateKey'
                >
                  <NInput
                    v-model={[detailForm.privateKey, 'value']}
                    type='textarea'
                    autosize={{
                      minRows: 4
                    }}
                  />
                </NFormItem>
                <NFormItem
                  v-show={showKubeConfig}
                  label={t('datasource.kubeConfig')}
                  path='kubeConfig'
                  show-require-mark
                >
                  <NInput
                    allowInput={this.trim}
                    class='input-kubeConfig'
                    v-model={[detailForm.kubeConfig, 'value']}
                    type='textarea'
                    autosize={{
                      minRows: 14
                    }}
                    placeholder={t('datasource.kubeConfig_tips')}
                  />
                </NFormItem>
                <NFormItem
                  v-show={showNamespace}
                  label={t('datasource.namespace')}
                  path='namespace'
                  show-require-mark
                >
                  <NInput
                    allowInput={this.trim}
                    v-model={[detailForm.namespace, 'value']}
                    placeholder={t('datasource.namespace_tips')}
                  />
                </NFormItem>
                {/* THIRDPARTY_SYSTEM_CONNECTOR 特殊字段 */}
                {detailForm.type === 'THIRDPARTY_SYSTEM_CONNECTOR' && (
                  <>
                    <NFormItem
                      label={t('thirdparty_api_source.system_name')}
                      path='systemName'
                    >
                      <NInput
                        allowInput={this.trim}
                        v-model={[detailForm.systemName, 'value']}
                        placeholder={t('thirdparty_api_source.system_name_tips')}
                      />
                    </NFormItem>
                    <NFormItem
                      label={t('thirdparty_api_source.service_address')}
                      path='serviceAddress'
                      required
                    >
                      <NInput
                        allowInput={this.trim}
                        v-model={[detailForm.serviceAddress, 'value']}
                        placeholder={'http://'}
                      />
                    </NFormItem>
                    <NFormItem
                      label={t('thirdparty_api_source.interface_timeout')}
                      path='interfaceTimeout'
                    >
                      <NInputNumber
                        v-model={[detailForm.interfaceTimeout, 'value']}
                        placeholder={t('thirdparty_api_source.interface_timeout_tips')}
                        min={1000}
                        max={1200000}
                        step={1000}
                      >
                        {{
                          suffix: () => t('thirdparty_api_source.millisecond')
                        }}
                      </NInputNumber>
                    </NFormItem>
                    <NFormItem label={t('thirdparty_api_source.auth_type')} path='authConfig.authType'>
                      <NSelect
                        v-model={[detailForm.authConfig.authType, 'value']}
                        options={[
                          { label: t('thirdparty_api_source.basic_auth'), value: 'BASIC_AUTH' },
                          { label: t('thirdparty_api_source.oauth2'), value: 'OAUTH2' },
                          { label: t('thirdparty_api_source.jwt'), value: 'JWT' }
                        ]}
                      />
                    </NFormItem>
                    <NFormItem label={t('thirdparty_api_source.header_prefix')}>
                      <NInput
                        allowInput={this.trim}
                        v-model={[detailForm.authConfig.headerPrefix, 'value']}
                        placeholder={t('thirdparty_api_source.header_prefix_tips')}
                      />
                    </NFormItem>
                    {detailForm.authConfig.authType === 'BASIC_AUTH' && (
                      <>
                        <NFormItem
                          label={t('thirdparty_api_source.username')}
                          path='authConfig.basicUsername'
                        >
                          <NInput
                            allowInput={this.trim}
                            v-model={[detailForm.authConfig.basicUsername, 'value']}
                            placeholder={t('thirdparty_api_source.username_tips')}
                          />
                        </NFormItem>
                        <NFormItem
                          label={t('thirdparty_api_source.password')}
                          path='authConfig.basicPassword'
                        >
                          <NInput
                            allowInput={this.trim}
                            v-model={[detailForm.authConfig.basicPassword, 'value']}
                            type='password'
                            showPasswordOn='click'
                            placeholder={t('thirdparty_api_source.password_tips')}
                          />
                        </NFormItem>
                      </>
                    )}
                    {detailForm.authConfig.authType === 'OAUTH2' && (
                      <>
                        <NFormItem
                          label={t('thirdparty_api_source.oauth2_token_url')}
                          path='authConfig.oauth2TokenUrl'
                        >
                          <NInput
                            allowInput={this.trim}
                            v-model={[detailForm.authConfig.oauth2TokenUrl, 'value']}
                            placeholder={t('thirdparty_api_source.oauth2_token_url_tips')}
                          />
                        </NFormItem>
                        <NFormItem
                          label={t('thirdparty_api_source.oauth2_client_id')}
                          path='authConfig.oauth2ClientId'
                        >
                          <NInput
                            allowInput={this.trim}
                            v-model={[detailForm.authConfig.oauth2ClientId, 'value']}
                            placeholder={t('thirdparty_api_source.oauth2_client_id_tips')}
                          />
                        </NFormItem>
                        <NFormItem
                          label={t('thirdparty_api_source.oauth2_client_secret')}
                          path='authConfig.oauth2ClientSecret'
                        >
                          <NInput
                            allowInput={this.trim}
                            v-model={[detailForm.authConfig.oauth2ClientSecret, 'value']}
                            placeholder={t('thirdparty_api_source.oauth2_client_secret_tips')}
                          />
                        </NFormItem>
                        <NFormItem
                          label={t('thirdparty_api_source.oauth2_grant_type')}
                          path='authConfig.oauth2GrantType'
                        >
                          <NInput
                            allowInput={this.trim}
                            v-model={[detailForm.authConfig.oauth2GrantType, 'value']}
                            placeholder={t('thirdparty_api_source.oauth2_grant_type_tips')}
                          />
                        </NFormItem>
                        <NFormItem
                          label={t('thirdparty_api_source.oauth2_username')}
                          path='authConfig.oauth2Username'
                        >
                          <NInput
                            allowInput={this.trim}
                            v-model={[detailForm.authConfig.oauth2Username, 'value']}
                            placeholder={t('thirdparty_api_source.oauth2_username_tips')}
                          />
                        </NFormItem>
                        <NFormItem
                          label={t('thirdparty_api_source.oauth2_password')}
                          path='authConfig.oauth2Password'
                        >
                          <NInput
                            allowInput={this.trim}
                            v-model={[detailForm.authConfig.oauth2Password, 'value']}
                            type='password'
                            showPasswordOn='click'
                            placeholder={t('thirdparty_api_source.oauth2_password_tips')}
                          />
                        </NFormItem>
                      </>
                    )}
                    {detailForm.authConfig.authType === 'JWT' && (
                      <NFormItem
                        label={t('thirdparty_api_source.jwt_token')}
                        path='authConfig.jwtToken'
                      >
                        <NInput
                          allowInput={this.trim}
                          v-model={[detailForm.authConfig.jwtToken, 'value']}
                          placeholder={t('thirdparty_api_source.jwt_token_tips')}
                        />
                      </NFormItem>
                    )}
                    {/* 额外参数 */}
                    <NFormItem label={t('thirdparty_api_source.additional_params')}>
                      <div style={{ width: '100%' }}>
                        {/* 添加按钮 */}
                        <NButton
                          onClick={() => {
                            if (!detailForm.authConfig.authMappings) {
                              detailForm.authConfig.authMappings = []
                            }
                            detailForm.authConfig.authMappings.push({ key: '', value: '' })
                          }}
                          style={{ marginBottom: '10px' }}
                        >
                          {t('thirdparty_api_source.add_param')}
                        </NButton>
                        
                        {/* 参数列表 */}
                        {detailForm.authConfig.authMappings && detailForm.authConfig.authMappings.map((param: { key: string; value: string }, index: number) => (
                          <div 
                            key={index} 
                            style={{ display: 'flex', alignItems: 'center', width: '100%', marginBottom: '10px' }}
                          >
                            <NInput
                              v-model={[param.key, 'value']}
                              placeholder={t('thirdparty_api_source.key')}
                              style={{ width: '40%' }}
                            />
                            <NInput
                              v-model={[param.value, 'value']}
                              placeholder={t('thirdparty_api_source.value')}
                              style={{ width: '40%', marginLeft: '10px' }}
                            />
                            <NButton
                              onClick={() => {
                                detailForm.authConfig.authMappings.splice(index, 1)
                              }}
                              style={{ width: '20%', marginLeft: '10px' }}
                              size="small"
                            >
                              {t('thirdparty_api_source.delete')}
                            </NButton>
                          </div>
                        ))}
                      </div>
                    </NFormItem>
                    <NFormItem
                      label={t('thirdparty_api_source.input_interface')}
                      path='selectInterface.url'
                    >
                      <div style={{ display: 'flex', alignItems: 'center', width: '100%' }}>
                        <NInput
                          allowInput={this.trim}
                          v-model={[detailForm.selectInterface.url, 'value']}
                          placeholder={t('thirdparty_api_source.input_interface_tips')}
                          style={{ flex: 1 }}
                        />
                        <NSelect
                          v-model={[detailForm.selectInterface.method, 'value']}
                          options={[
                            { label: t('thirdparty_api_source.get'), value: 'GET' },
                            { label: t('thirdparty_api_source.post'), value: 'POST' },
                            { label: t('thirdparty_api_source.put'), value: 'PUT' }
                          ]}
                          style={{ width: '120px', marginLeft: '10px' }}
                        />
                      </div>
                    </NFormItem>
                    <NFormItem label={t('thirdparty_api_source.parameters')}>
                      <div style={{ width: '100%' }}>
                        {/* 添加按钮 */}
                        <NButton
                          onClick={() => {
                            if (!detailForm.selectInterface.parameters) {
                              detailForm.selectInterface.parameters = []
                            }
                            detailForm.selectInterface.parameters.push({ paramName: '', paramValue: '', location: 'HEADER' })
                          }}
                          style={{ marginBottom: '10px' }}
                        >
                          {t('thirdparty_api_source.add_param')}
                        </NButton>
                        
                        {/* 参数列表 */}
                        {detailForm.selectInterface.parameters && detailForm.selectInterface.parameters.map((param: { paramName: string; paramValue: string; location: string }, index: number) => (
                          <div 
                            key={index} 
                            style={{ display: 'flex', alignItems: 'center', width: '100%', marginBottom: '10px' }}
                          >
                            <NSelect
                              v-model={[param.location, 'value']}
                              options={[
                                { label: 'Header', value: 'HEADER' },
                                { label: 'Param', value: 'PARAM' }
                              ]}
                              placeholder={t('thirdparty_api_source.param_location_tips')}
                              style={{ width: '120px' }}
                            />
                            <NInput
                              v-model={[param.paramName, 'value']}
                              placeholder={t('thirdparty_api_source.param_name_tips')}
                              style={{ flex: 1, marginLeft: '10px' }}
                            />
                            <NInput
                              v-model={[param.paramValue, 'value']}
                              placeholder={t('thirdparty_api_source.param_value_tips')}
                              style={{ flex: 1, marginLeft: '10px' }}
                            />
                            <NButton
                              onClick={() => {
                                detailForm.selectInterface.parameters.splice(index, 1)
                              }}
                              style={{ marginLeft: '10px' }}
                            >
                              {t('thirdparty_api_source.delete')}
                            </NButton>
                          </div>
                        ))}
                      </div>
                    </NFormItem>
                    {(detailForm.selectInterface.method === 'POST' || detailForm.selectInterface.method === 'PUT') && (
                      <NFormItem label={t('thirdparty_api_source.request_body')}>
                        <NInput
                          v-model={[detailForm.selectInterface.body, 'value']}
                          type="textarea"
                          autosize={{
                            minRows: 4,
                            maxRows: 10
                          }}
                          placeholder="请输入JSON格式的请求体"
                        />
                      </NFormItem>
                    )}
                    <NFormItem label={t('thirdparty_api_source.extract_response_data')}>
                      <div style={{ width: '100%' }}>
                        {/* 添加按钮 */}
                        <NButton
                          onClick={() => {
                            if (!detailForm.selectInterface.responseParameters) {
                              detailForm.selectInterface.responseParameters = []
                            }
                            detailForm.selectInterface.responseParameters.push({ key: '', jsonPath: '', disabled: false })
                          }}
                          style={{ marginBottom: '10px' }}
                        >
                          {t('thirdparty_api_source.add_extract_field')}
                        </NButton>
                        
                        {/* 参数列表 */}
                        {detailForm.selectInterface.responseParameters && detailForm.selectInterface.responseParameters.map((param: { key: string; jsonPath: string; disabled: boolean }, index: number) => (
                          <div 
                            key={index} 
                            style={{ display: 'flex', alignItems: 'center', width: '100%', marginBottom: '10px' }}
                          >
                            <NInput
                              v-model={[param.key, 'value']}
                              placeholder={t('thirdparty_api_source.extract_field')}
                              style={{ flex: 1 }}
                              disabled={param.disabled}
                            />
                            <NInput
                              v-model={[param.jsonPath, 'value']}
                              placeholder={t('thirdparty_api_source.json_path_list')}
                              style={{ flex: 1, marginLeft: '10px' }}
                            />
                            <NButton
                              onClick={() => {
                                detailForm.selectInterface.responseParameters.splice(index, 1)
                              }}
                              style={{ marginLeft: '10px' }}
                            >
                              {t('thirdparty_api_source.delete')}
                            </NButton>
                          </div>
                        ))}
                      </div>
                    </NFormItem>
                    <NFormItem
                      label={t('thirdparty_api_source.submit_interface')}
                      path='submitInterface.url'
                    >
                      <div style={{ display: 'flex', alignItems: 'center', width: '100%' }}>
                        <NInput
                          allowInput={this.trim}
                          v-model={[detailForm.submitInterface.url, 'value']}
                          placeholder={t('thirdparty_api_source.submit_interface_tips')}
                          style={{ flex: 1 }}
                        />
                        <NSelect
                          v-model={[detailForm.submitInterface.method, 'value']}
                          options={[
                            { label: t('thirdparty_api_source.get'), value: 'GET' },
                            { label: t('thirdparty_api_source.post'), value: 'POST' },
                            { label: t('thirdparty_api_source.put'), value: 'PUT' }
                          ]}
                          style={{ width: '120px', marginLeft: '10px' }}
                        />
                      </div>
                    </NFormItem>
                    <NFormItem label={t('thirdparty_api_source.parameters')}>
                      <div style={{ width: '100%' }}>
                        {/* 添加按钮 */}
                        <NButton
                          onClick={() => {
                            if (!detailForm.submitInterface.parameters) {
                              detailForm.submitInterface.parameters = []
                            }
                            detailForm.submitInterface.parameters.push({ paramName: '', paramValue: '', location: 'HEADER' })
                          }}
                          style={{ marginBottom: '10px' }}
                        >
                          {t('thirdparty_api_source.add_param')}
                        </NButton>
                        
                        {/* 参数列表 */}
                        {detailForm.submitInterface.parameters && detailForm.submitInterface.parameters.map((param: { paramName: string; paramValue: string; location: string }, index: number) => (
                          <div 
                            key={index} 
                            style={{ display: 'flex', alignItems: 'center', width: '100%', marginBottom: '10px' }}
                          >
                            <NSelect
                              v-model={[param.location, 'value']}
                              options={[
                                { label: 'Header', value: 'HEADER' },
                                { label: 'Param', value: 'PARAM' }
                              ]}
                              placeholder={t('thirdparty_api_source.param_location_tips')}
                              style={{ width: '120px' }}
                            />
                            <NInput
                              v-model={[param.paramName, 'value']}
                              placeholder={t('thirdparty_api_source.param_name_tips')}
                              style={{ flex: 1, marginLeft: '10px' }}
                            />
                            <NInput
                              v-model={[param.paramValue, 'value']}
                              placeholder={t('thirdparty_api_source.param_value_tips')}
                              style={{ flex: 1, marginLeft: '10px' }}
                            />
                            <NButton
                              onClick={() => {
                                detailForm.submitInterface.parameters.splice(index, 1)
                              }}
                              style={{ marginLeft: '10px' }}
                            >
                              {t('thirdparty_api_source.delete')}
                            </NButton>
                          </div>
                        ))}
                      </div>
                    </NFormItem>
                    {(detailForm.submitInterface.method === 'POST' || detailForm.submitInterface.method === 'PUT') && (
                      <NFormItem label={t('thirdparty_api_source.request_body')}>
                        <NInput
                          v-model={[detailForm.submitInterface.body, 'value']}
                          type="textarea"
                          autosize={{
                            minRows: 4,
                            maxRows: 10
                          }}
                          placeholder="请输入JSON格式的请求体"
                        />
                      </NFormItem>
                    )}
                    <NFormItem label={t('thirdparty_api_source.extract_response_data')}>
                      <div style={{ width: '100%' }}>
                        {/* 添加按钮 */}
                        <NButton
                          onClick={() => {
                            if (!detailForm.submitInterface.responseParameters) {
                              detailForm.submitInterface.responseParameters = []
                            }
                            detailForm.submitInterface.responseParameters.push({ key: '', jsonPath: '', disabled: false })
                          }}
                          style={{ marginBottom: '10px' }}
                        >
                          {t('thirdparty_api_source.add_extract_field')}
                        </NButton>
                        
                        {/* 参数列表 */}
                        {detailForm.submitInterface.responseParameters && detailForm.submitInterface.responseParameters.map((param: { key: string; jsonPath: string; disabled: boolean }, index: number) => (
                          <div 
                            key={index} 
                            style={{ width: '100%', marginBottom: '10px' }}
                          >
                            <NInput
                              v-model={[param.key, 'value']}
                              placeholder={t('thirdparty_api_source.extract_field')}
                              style={{ width: '180px' }}
                              disabled={param.disabled}
                            />
                            <NInput
                              v-model={[param.jsonPath, 'value']}
                              placeholder={t('thirdparty_api_source.json_path')}
                              style={{ width: '180px' }}
                              disabled={param.disabled}
                            />
                            <NButton
                              onClick={() => {
                                detailForm.submitInterface.responseParameters.splice(index, 1)
                              }}
                            >
                              {t('thirdparty_api_source.delete')}
                            </NButton>
                          </div>
                        ))}
                      </div>
                    </NFormItem>
                    <NFormItem
                      label={t('thirdparty_api_source.query_interface')}
                      path='pollStatusInterface.url'
                    >
                      <div style={{ display: 'flex', alignItems: 'center', width: '100%' }}>
                        <NInput
                          allowInput={this.trim}
                          v-model={[detailForm.pollStatusInterface.url, 'value']}
                          placeholder={t('thirdparty_api_source.query_interface_tips')}
                          style={{ flex: 1 }}
                        />
                        <NSelect
                          v-model={[detailForm.pollStatusInterface.method, 'value']}
                          options={[
                            { label: t('thirdparty_api_source.get'), value: 'GET' },
                            { label: t('thirdparty_api_source.post'), value: 'POST' },
                            { label: t('thirdparty_api_source.put'), value: 'PUT' }
                          ]}
                          style={{ width: '120px', marginLeft: '10px' }}
                        />
                      </div>
                    </NFormItem>
                    <NFormItem label={t('thirdparty_api_source.parameters')}>
                      <div style={{ width: '100%' }}>
                        {/* 添加按钮 */}
                        <NButton
                          onClick={() => {
                            if (!detailForm.pollStatusInterface.parameters) {
                              detailForm.pollStatusInterface.parameters = []
                            }
                            detailForm.pollStatusInterface.parameters.push({ paramName: '', paramValue: '', location: 'HEADER' })
                          }}
                          style={{ marginBottom: '10px' }}
                        >
                          {t('thirdparty_api_source.add_param')}
                        </NButton>
                        
                        {/* 参数列表 */}
                        {detailForm.pollStatusInterface.parameters && detailForm.pollStatusInterface.parameters.map((param: { paramName: string; paramValue: string; location: string }, index: number) => (
                          <div 
                            key={index} 
                            style={{ display: 'flex', alignItems: 'center', width: '100%', marginBottom: '10px' }}
                          >
                            <NSelect
                              v-model={[param.location, 'value']}
                              options={[
                                { label: 'Header', value: 'HEADER' },
                                { label: 'Param', value: 'PARAM' }
                              ]}
                              placeholder={t('thirdparty_api_source.param_location_tips')}
                              style={{ width: '120px' }}
                            />
                            <NInput
                              v-model={[param.paramName, 'value']}
                              placeholder={t('thirdparty_api_source.param_name_tips')}
                              style={{ flex: 1, marginLeft: '10px' }}
                            />
                            <NInput
                              v-model={[param.paramValue, 'value']}
                              placeholder={t('thirdparty_api_source.param_value_tips')}
                              style={{ flex: 1, marginLeft: '10px' }}
                            />
                            <NButton
                              onClick={() => {
                                detailForm.pollStatusInterface.parameters.splice(index, 1)
                              }}
                              style={{ marginLeft: '10px' }}
                            >
                              {t('thirdparty_api_source.delete')}
                            </NButton>
                          </div>
                        ))}
                      </div>
                    </NFormItem>
                    {(detailForm.pollStatusInterface.method === 'POST' || detailForm.pollStatusInterface.method === 'PUT') && (
                      <NFormItem label={t('thirdparty_api_source.request_body')}>
                        <NInput
                          v-model={[detailForm.pollStatusInterface.body, 'value']}
                          type="textarea"
                          autosize={{
                            minRows: 4,
                            maxRows: 10
                          }}
                          placeholder="请输入JSON格式的请求体"
                        />
                      </NFormItem>
                    )}
                    <NFormItem label={t('thirdparty_api_source.extract_response_data')}>
                      <div style={{ width: '100%' }}>
                        {/* 添加按钮 */}
                        <NButton
                          onClick={() => {
                            if (!detailForm.pollStatusInterface.responseParameters) {
                              detailForm.pollStatusInterface.responseParameters = []
                            }
                            detailForm.pollStatusInterface.responseParameters.push({ key: '', jsonPath: '', disabled: false })
                          }}
                          style={{ marginBottom: '10px' }}
                        >
                          {t('thirdparty_api_source.add_extract_field')}
                        </NButton>
                        
                        {/* 参数列表 */}
                        {detailForm.pollStatusInterface.responseParameters && detailForm.pollStatusInterface.responseParameters.map((param: { key: string; jsonPath: string; disabled: boolean }, index: number) => (
                          <div 
                            key={index} 
                            style={{ display: 'flex', alignItems: 'center', width: '100%', marginBottom: '10px' }}
                          >
                            <NInput
                              v-model={[param.key, 'value']}
                              placeholder={t('thirdparty_api_source.extract_field')}
                              style={{ flex: 1 }}
                              disabled={param.disabled}
                            />
                            <NInput
                              v-model={[param.jsonPath, 'value']}
                              placeholder={t('thirdparty_api_source.json_path')}
                              style={{ flex: 1, marginLeft: '10px' }}
                            />
                            <NButton
                              onClick={() => {
                                detailForm.pollStatusInterface.responseParameters.splice(index, 1)
                              }}
                              style={{ marginLeft: '10px' }}
                            >
                              {t('thirdparty_api_source.delete')}
                            </NButton>
                          </div>
                        ))}
                      </div>
                    </NFormItem>
                    <NFormItem label={t('thirdparty_api_source.success_condition')}>
                      <div style={{ display: 'flex', width: '100%', alignItems: 'center' }}>
                        <NInput
                          v-model={[detailForm.pollStatusInterface.pollingSuccessConfig.successField, 'value']}
                          placeholder={t('thirdparty_api_source.success_field_tips')}
                          style={{ flex: 1 }}
                        />
                        <NInput
                          v-model={[detailForm.pollStatusInterface.pollingSuccessConfig.successValue, 'value']}
                          placeholder={t('thirdparty_api_source.success_value_tips')}
                          style={{ flex: 1, marginLeft: '10px' }}
                        />
                      </div>
                    </NFormItem>
                    <NFormItem label={t('thirdparty_api_source.failure_condition')}>
                      <div style={{ display: 'flex', width: '100%', alignItems: 'center' }}>
                        <NInput
                          v-model={[detailForm.pollStatusInterface.pollingFailureConfig.failureField, 'value']}
                          placeholder={t('thirdparty_api_source.failure_field_tips')}
                          style={{ flex: 1 }}
                        />
                        <NInput
                          v-model={[detailForm.pollStatusInterface.pollingFailureConfig.failureValue, 'value']}
                          placeholder={t('thirdparty_api_source.failure_value_tips')}
                          style={{ flex: 1, marginLeft: '10px' }}
                        />
                      </div>
                    </NFormItem>
                    <NFormItem
                      label={t('thirdparty_api_source.stop_interface')}
                      path='stopInterface.url'
                    >
                      <div style={{ display: 'flex', alignItems: 'center', width: '100%' }}>
                        <NInput
                          allowInput={this.trim}
                          v-model={[detailForm.stopInterface.url, 'value']}
                          placeholder={t('thirdparty_api_source.stop_interface_tips')}
                          style={{ flex: 1 }}
                        />
                        <NSelect
                          v-model={[detailForm.stopInterface.method, 'value']}
                          options={[
                            { label: t('thirdparty_api_source.get'), value: 'GET' },
                            { label: t('thirdparty_api_source.post'), value: 'POST' },
                            { label: t('thirdparty_api_source.put'), value: 'PUT' }
                          ]}
                          style={{ width: '120px', marginLeft: '10px' }}
                        />
                      </div>
                    </NFormItem>
                    <NFormItem label={t('thirdparty_api_source.parameters')}>
                      <div style={{ width: '100%' }}>
                        {/* 添加按钮 */}
                        <NButton
                          onClick={() => {
                            if (!detailForm.stopInterface.parameters) {
                              detailForm.stopInterface.parameters = []
                            }
                            detailForm.stopInterface.parameters.push({ paramName: '', paramValue: '', location: 'HEADER' })
                          }}
                          style={{ marginBottom: '10px' }}
                        >
                          {t('thirdparty_api_source.add_param')}
                        </NButton>
                        
                        {/* 参数列表 */}
                        {detailForm.stopInterface.parameters && detailForm.stopInterface.parameters.map((param: { paramName: string; paramValue: string; location: string }, index: number) => (
                          <div 
                            key={index} 
                            style={{ display: 'flex', alignItems: 'center', width: '100%', marginBottom: '10px' }}
                          >
                            <NSelect
                              v-model={[param.location, 'value']}
                              options={[
                                { label: 'Header', value: 'HEADER' },
                                { label: 'Param', value: 'PARAM' }
                              ]}
                              placeholder={t('thirdparty_api_source.param_location_tips')}
                              style={{ width: '120px' }}
                            />
                            <NInput
                              v-model={[param.paramName, 'value']}
                              placeholder={t('thirdparty_api_source.param_name_tips')}
                              style={{ flex: 1, marginLeft: '10px' }}
                            />
                            <NInput
                              v-model={[param.paramValue, 'value']}
                              placeholder={t('thirdparty_api_source.param_value_tips')}
                              style={{ flex: 1, marginLeft: '10px' }}
                            />
                            <NButton
                              onClick={() => {
                                detailForm.stopInterface.parameters.splice(index, 1)
                              }}
                              style={{ marginLeft: '10px' }}
                            >
                              {t('thirdparty_api_source.delete')}
                            </NButton>
                          </div>
                        ))}
                      </div>
                    </NFormItem>
                    {(detailForm.stopInterface.method === 'POST' || detailForm.stopInterface.method === 'PUT') && (
                      <NFormItem label={t('thirdparty_api_source.request_body')}>
                        <NInput
                          v-model={[detailForm.stopInterface.body, 'value']}
                          type="textarea"
                          autosize={{
                            minRows: 4,
                            maxRows: 10
                          }}
                          placeholder="请输入JSON格式的请求体"
                        />
                      </NFormItem>
                    )}
                    <NFormItem label={t('thirdparty_api_source.extract_response_data')}>
                      <div style={{ width: '100%' }}>
                        {/* 添加按钮 */}
                        <NButton
                          onClick={() => {
                            if (!detailForm.stopInterface.responseParameters) {
                              detailForm.stopInterface.responseParameters = []
                            }
                            detailForm.stopInterface.responseParameters.push({ key: '', jsonPath: '', disabled: false })
                          }}
                          style={{ marginBottom: '10px' }}
                        >
                          {t('thirdparty_api_source.add_extract_field')}
                        </NButton>
                        
                        {/* 参数列表 */}
                        {detailForm.stopInterface.responseParameters && detailForm.stopInterface.responseParameters.map((param: { key: string; jsonPath: string; disabled: boolean }, index: number) => (
                          <div 
                            key={index} 
                            style={{ display: 'flex', alignItems: 'center', width: '100%', marginBottom: '10px' }}
                          >
                            <NInput
                              v-model={[param.key, 'value']}
                              placeholder={t('thirdparty_api_source.extract_field')}
                              style={{ flex: 1 }}
                              disabled={param.disabled}
                            />
                            <NInput
                              v-model={[param.jsonPath, 'value']}
                              placeholder={t('thirdparty_api_source.json_path')}
                              style={{ flex: 1, marginLeft: '10px' }}
                            />
                            <NButton
                              onClick={() => {
                                detailForm.stopInterface.responseParameters.splice(index, 1)
                              }}
                              style={{ marginLeft: '10px' }}
                            >
                              {t('thirdparty_api_source.delete')}
                            </NButton>
                          </div>
                        ))}
                      </div>
                    </NFormItem>
                  </>
                )}
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
