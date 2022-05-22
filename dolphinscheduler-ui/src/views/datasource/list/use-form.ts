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
    name: '',
    note: '',
    host: '',
    port: datasourceType['MYSQL'].defaultPort,
    principal: '',
    javaSecurityKrb5Conf: '',
    loginUserKeytabUsername: '',
    loginUserKeytabPath: '',
    userName: '',
    password: '',
    database: '',
    connectType: '',
    other: ''
  } as IDataSourceDetail

  const state = reactive({
    detailFormRef: ref(),
    detailForm: { ...initialValues },
    requiredDataBase: true,
    showConnectType: false,
    showPrincipal: false,
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
          if (!state.detailForm.host) {
            return new Error(t('datasource.ip_tips'))
          }
        }
      },
      port: {
        trigger: ['input'],
        validator() {
          if (!state.detailForm.port) {
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
      userName: {
        trigger: ['input'],
        validator() {
          if (!state.detailForm.userName) {
            return new Error(t('datasource.user_name_tips'))
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
      }
    } as FormRules
  })

  const changeType = async (type: IDataBase, options: IDataBaseOption) => {
    state.detailForm.port = options.previousPort || options.defaultPort
    state.detailForm.type = type

    if (type === 'ORACLE' && !id) {
      state.detailForm.connectType = 'ORACLE_SERVICE_NAME'
    }
    state.requiredDataBase = type !== 'POSTGRESQL'
    state.showConnectType = type === 'ORACLE'

    if (type === 'HIVE' || type === 'SPARK') {
      state.showPrincipal = await getKerberosStartupState()
    } else {
      state.showPrincipal = false
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

const datasourceType: IDataBaseOptionKeys = {
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
  PRESTO: {
    value: 'PRESTO',
    label: 'PRESTO',
    defaultPort: 8080
  },
  REDSHIFT: {
    value: 'REDSHIFT',
    label: 'REDSHIFT',
    defaultPort: 5439
  }
}

export const datasourceTypeList: IDataBaseOption[] = Object.values(
  datasourceType
).map((item) => {
  item.class = 'options-datasource-type'
  return item
})
