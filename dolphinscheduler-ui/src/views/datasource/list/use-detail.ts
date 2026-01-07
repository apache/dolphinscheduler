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

import {reactive} from 'vue'
import {
    connectDataSource,
    createDataSource,
    queryDataSource,
    updateDataSource,
    updateDataSourcePassword,
    verifyDataSourceName
} from '@/service/modules/data-source'
import {useI18n} from 'vue-i18n'
import type {IDataSource} from './types'

export function useDetail(getFieldsValue?: Function) {
  const { t } = useI18n()
  const status = reactive({
    saving: false,
    testing: false,
    loading: false
  })

  let PREV_NAME: string

  const formatParams = (id?: number, includePassword?: boolean): IDataSource => {
    if (!getFieldsValue) {
      throw new Error('getFieldsValue function is required')
    }
    const values = getFieldsValue()
    
    const params: any = {
      ...values,
      other: values.other ? JSON.parse(values.other) : null
    }
    
    // 如果是编辑模式且不需要包含密码，则移除密码相关字段
    if (id && !includePassword) {
      delete params.password
      delete params.confirmPassword
    }
    
    return params
  }

  const queryById = async (id: number) => {
    if (status.loading) return {}
    status.loading = true
    const dataSourceRes = await queryDataSource(id)
    status.loading = false
    PREV_NAME = dataSourceRes.name
    return dataSourceRes
  }

  const testConnect = async (id?: number) => {
    if (status.testing) return
    status.testing = true
    try {
      // 测试连接时始终传递密码
      const params = formatParams(id, true)
      const res = await connectDataSource(params)
      window.$message.success(
        res && res.msg
          ? res.msg
          : `${t('datasource.test_connect')} ${t('datasource.success')}`
      )
      status.testing = false
    } catch (err) {
      status.testing = false
    }
  }

  const createOrUpdate = async (id?: number) => {
    if (!getFieldsValue) {
      throw new Error('getFieldsValue function is required')
    }
    const values = getFieldsValue()

    if (status.saving || !values.name) return false
    status.saving = true

    try {
      if (PREV_NAME !== values.name) {
        await verifyDataSourceName({ name: values.name })
      }

      id
        ? await updateDataSource(formatParams(id, false), id)
        : await createDataSource(formatParams())

      status.saving = false
      return true
    } catch (err) {
      status.saving = false
      return false
    }
  }

  const updatePassword = async (id: number, password: string, confirmPassword: string) => {
    if (status.saving) return false
    status.saving = true

    try {
      // 使用专门的密码更新接口
      await updateDataSourcePassword(id, password, confirmPassword)
      status.saving = false
      window.$message.success(t('datasource.update_password_success'))
      return true
    } catch (err) {
      status.saving = false
      window.$message.error(t('datasource.update_password_failed'))
      return false
    }
  }

  return { status, queryById, testConnect, createOrUpdate, updatePassword }
}
