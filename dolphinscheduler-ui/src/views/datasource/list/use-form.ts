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

import { reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { getKerberosStartupState } from '@/service/modules/data-source'
import type { FormRules } from 'naive-ui'
import type {
  IDataSourceDetail,
  IDataBase,
  IDataBaseOption,
  IDataBaseOptionKeys,
  IDataSource
} from './types'
import utils from '@/utils'

export function useForm(id?: number) {
  const { t } = useI18n()

  const initialValues = {
    type: 'MYSQL',
    label: 'MYSQL',
    name: '',
    note: '',
    host: '',
    port: datasourceType['MYSQL'].defaultPort,
    principal: '',
    javaSecurityKrb5Conf: '',
    loginUserKeytabUsername: '',
    loginUserKeytabPath: '',
    mode: '',
    userName: '',
    password: '',
    database: '',
    connectType: '',
    other: '',
    endpoint: '',
    MSIClientId: '',
    dbUser: '',
    datawarehouse: '',
    // THIRDPARTY_SYSTEM_CONNECTOR
    serviceAddress: 'http://',
    interfaceTimeout: 120000,
    authConfig: {
      authType: 'BASIC_AUTH',
      basicUsername: '',
      basicPassword: '',
      jwtToken: '',
      oauth2TokenUrl: '',
      oauth2ClientId: '',
      oauth2ClientSecret: '',
      oauth2GrantType: '',
      oauth2Username: '',
      oauth2Password: '',
      headerPrefix: 'Basic',
      authMappings: [] as { key: string; value: string }[]
    },
    selectInterface: {
      url: '',
      method: 'GET',
      parameters: [],
      body: '',
      responseParameters: [
        { key: 'id', jsonPath: '' },
        { key: 'name', jsonPath: '' }
      ]
    },
    submitInterface: {
      url: '',
      method: 'POST',
      parameters: [],
      body: '',
      responseParameters: [
        { key: 'taskInstanceId', jsonPath: '' }
      ]
    },
    pollStatusInterface: {
      url: '',
      method: 'GET',
      parameters: [],
      body: '',
      pollingSuccessConfig: {
        successField: '',
        successValue: ''
      },
      pollingFailureConfig: {
        failureField: '',
        failureValue: ''
      }
    },
    stopInterface: {
      url: '',
      method: 'POST',
      parameters: [],
      body: ''
    }
  } as IDataSourceDetail

  const state = reactive({
    detailFormRef: ref(),
    detailForm: { ...initialValues },
    requiredDataBase: true,
    showHost: true,
    showPort: true,
    showAwsRegion: false,
    showRestEndpoint: false,
    showCompatibleMode: false,
    showConnectType: false,
    showPrincipal: false,
    showMode: false,
    showDataBaseName: true,
    showJDBCConnectParameters: true,
    showPublicKey: false,
    showNamespace: false,
    showKubeConfig: false,
    showAccessKeyId: false,
    showAccessKeySecret: false,
    showRegionId: false,
    showEndpoint: false,
    rules: {
      name: {
        trigger: ['input'],
        validator() {
          if (!state.detailForm.name) {
            return new Error(t('datasource.datasource_name_tips'))
          }
        }
      },
      host: {
        trigger: ['input'],
        validator() {
          if (!state.detailForm.host && state.showHost) {
            return new Error(t('datasource.ip_tips'))
          }
        }
      },
      port: {
        trigger: ['input'],
        validator() {
          if (state.showMode && state.detailForm.mode === 'IAM-accessKey') {
            return
          }
          if (!state.detailForm.port && state.showPort) {
            return new Error(t('datasource.port_tips'))
          }
        }
      },
      principal: {
        trigger: ['input'],
        validator() {
          if (!state.detailForm.principal && state.showPrincipal) {
            return new Error(t('datasource.principal_tips'))
          }
        }
      },
      mode: {
        trigger: ['blur'],
        validator() {
          if (!state.detailForm.mode && state.showMode) {
            return new Error(t('datasource.mode_tips'))
          }
        }
      },
      userName: {
        trigger: ['input'],
        validator() {
          if (
            !state.detailForm.userName &&
            state.detailForm.type !== 'AZURESQL' &&
            state.detailForm.type !== 'K8S' &&
            state.detailForm.type !== 'ALIYUN_SERVERLESS_SPARK' &&
            state.detailForm.type !== 'THIRDPARTY_SYSTEM_CONNECTOR'
          ) {
            return new Error(t('datasource.user_name_tips'))
          }
        }
      },
      password: {
        trigger: ['input'],
        validator() {
          if (
            !state.detailForm.password &&
            state.detailForm.type !== 'K8S' &&
            state.detailForm.type !== 'ALIYUN_SERVERLESS_SPARK' &&
            state.detailForm.type !== 'THIRDPARTY_SYSTEM_CONNECTOR'
          ) {
            return new Error(t('datasource.user_password_tips'))
          }
        }
      },
      awsRegion: {
        trigger: ['input'],
        validator() {
          if (!state.detailForm.awsRegion && state.showAwsRegion) {
            return new Error(t('datasource.aws_region_tips'))
          }
        }
      },
      database: {
        trigger: ['input'],
        validator() {
          if (!state.detailForm.database && state.requiredDataBase) {
            return new Error(t('datasource.database_name_tips'))
          }
        }
      },
      datawarehouse: {
        trigger: ['input'],
        validator() {
          if (!state.detailForm.datawarehouse) {
            return new Error(t('datasource.datawarehouse_tips'))
          }
        }
      },
      connectType: {
        trigger: ['update'],
        validator() {
          if (!state.detailForm.connectType && state.showConnectType) {
            return new Error(t('datasource.oracle_connect_type_tips'))
          }
        }
      },
      other: {
        trigger: ['input', 'blur'],
        validator() {
          if (state.detailForm.other && !utils.isJson(state.detailForm.other)) {
            return new Error(t('datasource.jdbc_format_tips'))
          }
        }
      },
      endpoint: {
        trigger: ['input'],
        validator() {
          if (
            !state.detailForm.endpoint &&
            state.detailForm.type === 'AZURESQL' &&
            state.detailForm.mode === 'accessToken'
          ) {
            return new Error(t('datasource.endpoint_tips'))
          }
        }
      },
      dbUser: {
        trigger: ['input'],
        validator() {
          if (
            !state.detailForm.dbUser &&
            state.showMode &&
            state.detailForm.mode === 'IAM-accessKey' &&
            state.detailForm.type != 'SAGEMAKER'
          ) {
            return new Error(t('datasource.IAM-accessKey'))
          }
        }
      },
      // THIRDPARTY_SYSTEM_CONNECTOR check rule
      serviceAddress: {
        trigger: ['input'],
        validator() {
          if (state.detailForm.type === 'THIRDPARTY_SYSTEM_CONNECTOR' && !state.detailForm.serviceAddress) {
            return new Error(t('thirdparty_api_source.service_address_required'))
          }
        }
      },
      'authConfig.authType': {
        trigger: ['change'],
        validator() {
          if (state.detailForm.type === 'THIRDPARTY_SYSTEM_CONNECTOR' && !state.detailForm.authConfig?.authType) {
            return new Error(t('thirdparty_api_source.auth_type_required'))
          }
        }
      },
      'authConfig.basicUsername': {
        trigger: ['blur'],
        validator() {
          if (
            state.detailForm.type === 'THIRDPARTY_SYSTEM_CONNECTOR' &&
            state.detailForm.authConfig?.authType === 'BASIC_AUTH' &&
            !state.detailForm.authConfig?.basicUsername
          ) {
            return new Error(t('thirdparty_api_source.username_required'))
          }
          return true
        }
      },
      'authConfig.basicPassword': {
        trigger: ['blur'],
        validator() {
          if (
            state.detailForm.type === 'THIRDPARTY_SYSTEM_CONNECTOR' &&
            state.detailForm.authConfig?.authType === 'BASIC_AUTH' &&
            !state.detailForm.authConfig?.basicPassword
          ) {
            return new Error(t('thirdparty_api_source.password_required'))
          }
          return true
        }
      },
      'authConfig.oauth2TokenUrl': {
        trigger: ['blur'],
        validator() {
          if (
            state.detailForm.type === 'THIRDPARTY_SYSTEM_CONNECTOR' &&
            state.detailForm.authConfig?.authType === 'OAUTH2' &&
            !state.detailForm.authConfig?.oauth2TokenUrl
          ) {
            return new Error(t('thirdparty_api_source.oauth2_token_url_required'))
          }
          return true
        }
      },
      'authConfig.oauth2ClientId': {
        trigger: ['blur'],
        validator() {
          if (
            state.detailForm.type === 'THIRDPARTY_SYSTEM_CONNECTOR' &&
            state.detailForm.authConfig?.authType === 'OAUTH2' &&
            !state.detailForm.authConfig?.oauth2ClientId
          ) {
            return new Error(t('thirdparty_api_source.oauth2_client_id_required'))
          }
          return true
        }
      },
      'authConfig.oauth2ClientSecret': {
        trigger: ['blur'],
        validator() {
          if (
            state.detailForm.type === 'THIRDPARTY_SYSTEM_CONNECTOR' &&
            state.detailForm.authConfig?.authType === 'OAUTH2' &&
            !state.detailForm.authConfig?.oauth2ClientSecret
          ) {
            return new Error(t('thirdparty_api_source.oauth2_client_secret_required'))
          }
          return true
        }
      },
      'authConfig.oauth2GrantType': {
        trigger: ['blur'],
        validator() {
          if (
            state.detailForm.type === 'THIRDPARTY_SYSTEM_CONNECTOR' &&
            state.detailForm.authConfig?.authType === 'OAUTH2' &&
            !state.detailForm.authConfig?.oauth2GrantType
          ) {
            return new Error(t('thirdparty_api_source.oauth2_grant_type_required'))
          }
          return true
        }
      },
      'authConfig.jwtToken': {
        trigger: ['blur'],
        validator() {
          if (
            state.detailForm.type === 'THIRDPARTY_SYSTEM_CONNECTOR' &&
            state.detailForm.authConfig?.authType === 'JWT' &&
            !state.detailForm.authConfig?.jwtToken
          ) {
            return new Error(t('thirdparty_api_source.jwt_token_required'))
          }
          return true
        }
      },
      'selectInterface.url': {
        trigger: ['blur', 'change'],
        validator() {
          if (
            state.detailForm.type === 'THIRDPARTY_SYSTEM_CONNECTOR' &&
            !state.detailForm.selectInterface?.url
          ) {
            return new Error(t('thirdparty_api_source.input_interface_url_required'))
          }
        }
      },
      'submitInterface.url': {
        trigger: ['blur', 'change'],
        validator() {
          if (
            state.detailForm.type === 'THIRDPARTY_SYSTEM_CONNECTOR' &&
            !state.detailForm.submitInterface?.url
          ) {
            return new Error(t('thirdparty_api_source.submit_interface_url_required'))
          }
        }
      },
      'pollStatusInterface.url': {
        trigger: ['blur', 'change'],
        validator() {
          if (
            state.detailForm.type === 'THIRDPARTY_SYSTEM_CONNECTOR' &&
            !state.detailForm.pollStatusInterface?.url
          ) {
            return new Error(t('thirdparty_api_source.query_interface_url_required'))
          }
        }
      },
      'stopInterface.url': {
        trigger: ['blur', 'change'],
        validator() {
          if (
            state.detailForm.type === 'THIRDPARTY_SYSTEM_CONNECTOR' &&
            !state.detailForm.stopInterface?.url
          ) {
            return new Error(t('thirdparty_api_source.stop_interface_url_required'))
          }
        }
      }
    } as FormRules,
    modeOptions: [
      {
        label: 'SqlPassword',
        value: 'SqlPassword'
      },
      {
        label: 'ActiveDirectoryPassword',
        value: 'ActiveDirectoryPassword'
      },
      {
        label: 'ActiveDirectoryMSI',
        value: 'ActiveDirectoryMSI'
      },
      {
        label: 'ActiveDirectoryServicePrincipal',
        value: 'ActiveDirectoryServicePrincipal'
      },
      {
        label: 'accessToken',
        value: 'accessToken'
      }
    ],
    redShiftModeOptions: [
      {
        label: 'password',
        value: 'password'
      },
      {
        label: 'IAM-accessKey',
        value: 'IAM-accessKey'
      }
    ],
    sagemakerModeOption: [
      {
        label: 'IAM-accessKey',
        value: 'IAM-accessKey'
      }
    ]
  })

  const changeType = async (type: IDataBase, options: IDataBaseOption) => {
    state.detailForm.port = options.previousPort || options.defaultPort
    state.detailForm.type = type

    state.requiredDataBase =
      type !== 'POSTGRESQL' && type !== 'ATHENA' && type !== 'DOLPHINDB'

    state.showHost = type !== 'ATHENA'
    state.showPort = type !== 'ATHENA'
    state.showAwsRegion = type === 'ATHENA' || type === 'SAGEMAKER'
    state.showMode = ['AZURESQL', 'REDSHIFT', 'SAGEMAKER'].includes(type)

    if (type === 'ORACLE' && !id) {
      state.detailForm.connectType = 'ORACLE_SERVICE_NAME'
    }
    state.showConnectType = type === 'ORACLE'

    state.showCompatibleMode = type == 'OCEANBASE'

    if (type === 'HIVE' || type === 'SPARK') {
      state.showPrincipal = await getKerberosStartupState()
    } else {
      state.showPrincipal = false
    }

    if (
      type === 'SSH' ||
      type === 'ZEPPELIN' ||
      type === 'SAGEMAKER' ||
      type === 'K8S' ||
      type === 'ALIYUN_SERVERLESS_SPARK' ||
      type === 'DOLPHINDB' ||
      type === 'THIRDPARTY_SYSTEM_CONNECTOR'
    ) {
      state.showDataBaseName = false
      state.requiredDataBase = false
      state.showJDBCConnectParameters = false
      state.showPublicKey = false
      if (type === 'DOLPHINDB') {
        state.showJDBCConnectParameters = true
        state.showPublicKey = false
      }
      if (type === 'SSH') {
        state.showPublicKey = true
      }
      if (type === 'ZEPPELIN') {
        state.showHost = false
        state.showPort = false
        state.showRestEndpoint = true
      } else {
        state.showRestEndpoint = false
      }
      if (
        type === 'SAGEMAKER' ||
        type === 'K8S' ||
        type == 'ALIYUN_SERVERLESS_SPARK' ||
        type === 'THIRDPARTY_SYSTEM_CONNECTOR'
      ) {
        state.showHost = false
        state.showPort = false
      }
      if (type === 'K8S') {
        state.showNamespace = true
        state.showKubeConfig = true
      } else {
        state.showNamespace = false
        state.showKubeConfig = false
      }
      if (type === 'ALIYUN_SERVERLESS_SPARK') {
        state.showAccessKeyId = true
        state.showAccessKeySecret = true
        state.showRegionId = true
        state.showEndpoint = true
      } else {
        state.showAccessKeyId = false
        state.showAccessKeySecret = false
        state.showRegionId = false
        state.showEndpoint = false
      }
      // 处理THIRDPARTY_SYSTEM_CONNECTOR特殊字段显示
      if (type === 'THIRDPARTY_SYSTEM_CONNECTOR') {
        state.showHost = false
        state.showPort = false
        state.showJDBCConnectParameters = false
        state.showDataBaseName = false
        state.requiredDataBase = false

        // make sure authConfig \ authMappings inited
        if (!state.detailForm.authConfig) {
          state.detailForm.authConfig = {
            authType: 'BASIC_AUTH',
            basicUsername: '',
            basicPassword: '',
            jwtToken: '',
            oauth2TokenUrl: '',
            oauth2ClientId: '',
            oauth2ClientSecret: '',
            oauth2GrantType: '',
            oauth2Username: '',
            oauth2Password: '',
            headerPrefix: 'Basic',
            authMappings: []
          }
        } else if (!state.detailForm.authConfig.authMappings) {
          state.detailForm.authConfig.authMappings = []
        }
      }

      // init THIRDPARTY_SYSTEM_CONNECTOR  authConfig
      if (type === 'THIRDPARTY_SYSTEM_CONNECTOR' && !state.detailForm.authConfig) {
        state.detailForm.authConfig = {
          authType: 'BASIC_AUTH',
          basicUsername: '',
          basicPassword: '',
          jwtToken: '',
          oauth2TokenUrl: '',
          oauth2ClientId: '',
          oauth2ClientSecret: '',
          oauth2GrantType: '',
          oauth2Username: '',
          oauth2Password: '',
          headerPrefix: 'Basic',
          authMappings: []
        }
      }
    } else {
      state.showDataBaseName = true
      state.requiredDataBase = true
      state.showJDBCConnectParameters = true
      state.showPublicKey = false
      state.showRestEndpoint = false
      state.showNamespace = false
      state.showKubeConfig = false
      state.showAccessKeyId = false
      state.showAccessKeySecret = false
      state.showRegionId = false
      state.showEndpoint = false
    }
  }

  const changePort = async () => {
    if (!state.detailForm.type) return
    const currentDataBaseOption = datasourceType[state.detailForm.type]
    currentDataBaseOption.previousPort = state.detailForm.port
  }

  const resetFieldsValue = () => {
    state.detailForm = { ...initialValues }
  }

  const setFieldsValue = (values: IDataSource) => {
    state.detailForm = {
      ...state.detailForm,
      ...values,
      other: values.other ? JSON.stringify(values.other) : values.other
    }

    // check THIRDPARTY_SYSTEM_CONNECTOR has right authConfig
    if (values.type === 'THIRDPARTY_SYSTEM_CONNECTOR') {
      if (!state.detailForm.authConfig) {
        state.detailForm.authConfig = {
          authType: 'BASIC_AUTH',
          basicUsername: '',
          basicPassword: '',
          jwtToken: '',
          oauth2TokenUrl: '',
          oauth2ClientId: '',
          oauth2ClientSecret: '',
          oauth2GrantType: '',
          oauth2Username: '',
          oauth2Password: '',
          headerPrefix: 'Basic',
          authMappings: []
        }
      } else {
        state.detailForm.authConfig = {
          authType: values.authConfig?.authType || 'BASIC_AUTH',
          basicUsername: values.authConfig?.basicUsername || '',
          basicPassword: values.authConfig?.basicPassword || '',
          jwtToken: values.authConfig?.jwtToken || '',
          oauth2TokenUrl: values.authConfig?.oauth2TokenUrl || '',
          oauth2ClientId: values.authConfig?.oauth2ClientId || '',
          oauth2ClientSecret: values.authConfig?.oauth2ClientSecret || '',
          oauth2GrantType: values.authConfig?.oauth2GrantType || '',
          oauth2Username: values.authConfig?.oauth2Username || '',
          oauth2Password: values.authConfig?.oauth2Password || '',
          headerPrefix: values.authConfig?.headerPrefix || 'Basic',
          authMappings: values.authConfig?.authMappings ? [...values.authConfig.authMappings] : []
        }
      }

      if (!state.detailForm.selectInterface) {
        state.detailForm.selectInterface = {
          url: '',
          method: 'GET',
          parameters: [],
          body: '',
          responseParameters: [
            { key: 'id', jsonPath: '' },
            { key: 'name', jsonPath: '' }
          ]
        }
      } else {
        state.detailForm.selectInterface = {
          url: values.selectInterface?.url || '',
          method: values.selectInterface?.method || 'GET',
          parameters: values.selectInterface?.parameters ? [...values.selectInterface.parameters] : [],
          body: values.selectInterface?.body || '',
          responseParameters: values.selectInterface?.responseParameters ? [...values.selectInterface.responseParameters] : [
            { key: 'id', jsonPath: '' },
            { key: 'name', jsonPath: '' }
          ]
        }
      }

      if (!state.detailForm.submitInterface) {
        state.detailForm.submitInterface = {
          url: '',
          method: 'POST',
          parameters: [],
          body: '',
          responseParameters: [
            { key: 'taskInstanceId', jsonPath: '' }
          ]
        }
      } else {
        state.detailForm.submitInterface = {
          url: values.submitInterface?.url || '',
          method: values.submitInterface?.method || 'POST',
          parameters: values.submitInterface?.parameters ? [...values.submitInterface.parameters] : [],
          body: values.submitInterface?.body || '',
          responseParameters: values.submitInterface?.responseParameters ? [...values.submitInterface.responseParameters] : [
            { key: 'taskInstanceId', jsonPath: '' }
          ]
        }
      }

      if (!state.detailForm.pollStatusInterface) {
        state.detailForm.pollStatusInterface = {
          url: '',
          method: 'GET',
          parameters: [],
          body: '',
          pollingSuccessConfig: {
            successField: '',
            successValue: ''
          },
          pollingFailureConfig: {
            failureField: '',
            failureValue: ''
          },
          responseParameters: []
        }
      } else {
        state.detailForm.pollStatusInterface = {
          url: values.pollStatusInterface?.url || '',
          method: values.pollStatusInterface?.method || 'GET',
          parameters: values.pollStatusInterface?.parameters ? [...values.pollStatusInterface.parameters] : [],
          body: values.pollStatusInterface?.body || '',
          pollingSuccessConfig: {
            successField: values.pollStatusInterface?.pollingSuccessConfig?.successField || '',
            successValue: values.pollStatusInterface?.pollingSuccessConfig?.successValue || ''
          },
          pollingFailureConfig: {
            failureField: values.pollStatusInterface?.pollingFailureConfig?.failureField || '',
            failureValue: values.pollStatusInterface?.pollingFailureConfig?.failureValue || ''
          },
          responseParameters: values.pollStatusInterface?.responseParameters ? [...values.pollStatusInterface.responseParameters] : []
        }
      }

      if (!state.detailForm.stopInterface) {
        state.detailForm.stopInterface = {
          url: '',
          method: 'POST',
          parameters: [],
          body: '',
          responseParameters: []
        }
      } else {
        state.detailForm.stopInterface = {
          url: values.stopInterface?.url || '',
          method: values.stopInterface?.method || 'POST',
          parameters: values.stopInterface?.parameters ? [...values.stopInterface.parameters] : [],
          body: values.stopInterface?.body || '',
          responseParameters: values.stopInterface?.responseParameters ? [...values.stopInterface.responseParameters] : []
        }
      }

      delete state.detailForm.userName;
      delete state.detailForm.password;
    }
  }

  const getFieldsValue = () => state.detailForm

  return {
    state,
    changeType,
    changePort,
    resetFieldsValue,
    setFieldsValue,
    getFieldsValue
  }
}

export const datasourceType: IDataBaseOptionKeys = {
  MYSQL: {
    value: 'MYSQL',
    label: 'MYSQL',
    defaultPort: 3306
  },
  POSTGRESQL: {
    value: 'POSTGRESQL',
    label: 'POSTGRESQL',
    defaultPort: 5432
  },
  HIVE: {
    value: 'HIVE',
    label: 'HIVE/IMPALA',
    defaultPort: 10000
  },
  KYUUBI: {
    value: 'KYUUBI',
    label: 'KYUUBI',
    defaultPort: 10000
  },
  SPARK: {
    value: 'SPARK',
    label: 'SPARK',
    defaultPort: 10015
  },
  CLICKHOUSE: {
    value: 'CLICKHOUSE',
    label: 'CLICKHOUSE',
    defaultPort: 8123
  },
  ORACLE: {
    value: 'ORACLE',
    label: 'ORACLE',
    defaultPort: 1521
  },
  SQLSERVER: {
    value: 'SQLSERVER',
    label: 'SQLSERVER',
    defaultPort: 1433
  },
  DB2: {
    value: 'DB2',
    label: 'DB2',
    defaultPort: 50000
  },
  VERTICA: {
    value: 'VERTICA',
    label: 'VERTICA',
    defaultPort: 5433
  },
  PRESTO: {
    value: 'PRESTO',
    label: 'PRESTO',
    defaultPort: 8080
  },
  REDSHIFT: {
    value: 'REDSHIFT',
    label: 'REDSHIFT',
    defaultPort: 5439
  },
  ATHENA: {
    value: 'ATHENA',
    label: 'ATHENA',
    defaultPort: 0
  },
  TRINO: {
    value: 'TRINO',
    label: 'TRINO',
    defaultPort: 8080
  },
  AZURESQL: {
    value: 'AZURESQL',
    label: 'AZURESQL',
    defaultPort: 1433
  },
  STARROCKS: {
    value: 'STARROCKS',
    label: 'STARROCKS',
    defaultPort: 9030
  },
  DAMENG: {
    value: 'DAMENG',
    label: 'DAMENG',
    defaultPort: 5236
  },
  OCEANBASE: {
    value: 'OCEANBASE',
    label: 'OCEANBASE',
    defaultPort: 2881
  },
  SNOWFLAKE: {
    value: 'SNOWFLAKE',
    label: 'SNOWFLAKE',
    defaultPort: 3306
  },
  SSH: {
    value: 'SSH',
    label: 'SSH',
    defaultPort: 22
  },
  DATABEND: {
    value: 'DATABEND',
    label: 'DATABEND',
    defaultPort: 8000
  },
  HANA: {
    value: 'HANA',
    label: 'HANA',
    defaultPort: 30015
  },
  ZEPPELIN: {
    value: 'ZEPPELIN',
    label: 'ZEPPELIN',
    defaultPort: 8080
  },
  DORIS: {
    value: 'DORIS',
    label: 'DORIS',
    defaultPort: 9030
  },
  SAGEMAKER: {
    value: 'SAGEMAKER',
    label: 'SAGEMAKER',
    defaultPort: 0
  },
  K8S: {
    value: 'K8S',
    label: 'K8S',
    defaultPort: 6443
  },
  ALIYUN_SERVERLESS_SPARK: {
    value: 'ALIYUN_SERVERLESS_SPARK',
    label: 'ALIYUN_SERVERLESS_SPARK',
    defaultPort: 0
  },
  DOLPHINDB: {
    value: 'DOLPHINDB',
    label: 'DOLPHINDB',
    defaultPort: 8848
  },
  THIRDPARTY_SYSTEM_CONNECTOR: {
    value: 'THIRDPARTY_SYSTEM_CONNECTOR',
    label: 'THIRDPARTY_SYSTEM_CONNECTOR',
    defaultPort: 80
  }
}

export const datasourceTypeList: IDataBaseOption[] = Object.values(
  datasourceType
).map((item) => {
  item.class = 'options-datasource-type'
  return item
})