import { defineComponent, PropType, reactive, watch, computed, ref } from 'vue'
import type { FormInst } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import styles from './index.module.scss'
import {
  NModal,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NButton,
  NSpace,
  NDivider,
  NDynamicInput,
  NSelect,
  NTooltip,
  NIcon
} from 'naive-ui'
import MonacoEditor from '@/components/monaco-editor'
import { InfoCircleOutlined } from '@vicons/antd';

export default defineComponent({
  name: 'ThirdpartyApiSourceModal',
  props: {
    show: Boolean,
    data: Object as PropType<any>,
    operationType: {
      type: String as PropType<'create' | 'edit'>,
      default: 'create'
    }
  },
  emits: ['close', 'submit', 'test'],
  setup(props, { emit }) {
    const { t } = useI18n()

    const authTypeOptions = computed(() => [
      { label: t('thirdparty_api_source.basic_auth'), value: 'BASIC_AUTH' },
      { label: t('thirdparty_api_source.oauth2'), value: 'OAUTH2' },
      { label: t('thirdparty_api_source.jwt'), value: 'JWT' }
    ])

    const methodOptions = computed(() => [
      { label: t('thirdparty_api_source.get'), value: 'GET' },
      { label: t('thirdparty_api_source.post'), value: 'POST' },
      { label: t('thirdparty_api_source.put'), value: 'PUT' }
    ])

    const form = reactive({
      systemName: '',
      serviceAddress: '',
      interfaceTimeout: 120000, // 默认2分钟
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
        authMappings: [] as any[]
      },
      selectInterface: {
        url: '',
        method: 'GET',
        parameters: [] as any[],
        body: '',
        responseParameters: [
          { key: 'id', jsonPath: '' },
          { key: 'name', jsonPath: '' }
        ]
      },
      submitInterface: {
        url: '',
        method: 'POST',
        parameters: [] as any[],
        body: '',
        responseParameters: [
          { key: 'taskInstanceId', jsonPath: '' }
        ]
      },
      pollStatusInterface: {
        url: '',
        method: 'GET',
        parameters: [] as any[],
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
        parameters: [] as any[],
        body: ''
      }
    })

    // 表单校验规则
    const rules = {
      systemName: [
        { required: true, message: t('thirdparty_api_source.system_name_required'), trigger: 'blur' }
      ],
      serviceAddress: [
        { required: true, message: t('thirdparty_api_source.service_address_required'), trigger: 'blur' }
      ],
      'authConfig.authType': [
        { required: true, message: t('thirdparty_api_source.auth_type_required'), trigger: 'change' }
      ],
      'authConfig.basicUsername': [
        {
          validator: (rule: any, value: any) => {
            if (form.authConfig.authType === 'BASIC_AUTH' && !value) {
              return new Error(t('thirdparty_api_source.username_required'))
            }
            return true
          },
          trigger: 'blur'
        }
      ],
      'authConfig.basicPassword': [
        {
          validator: (rule: any, value: any) => {
            if (form.authConfig.authType === 'BASIC_AUTH' && !value) {
              return new Error(t('thirdparty_api_source.password_required'))
            }
            return true
          },
          trigger: 'blur'
        }
      ],
      'authConfig.oauth2TokenUrl': [
        {
          validator: (rule: any, value: any) => {
            if (form.authConfig.authType === 'OAUTH2' && !value) {
              return new Error(t('thirdparty_api_source.oauth2_token_url_required'))
            }
            return true
          },
          trigger: 'blur'
        }
      ],
      'authConfig.oauth2ClientId': [
        {
          validator: (rule: any, value: any) => {
            if (form.authConfig.authType === 'OAUTH2' && !value) {
              return new Error(t('thirdparty_api_source.oauth2_client_id_required'))
            }
            return true
          },
          trigger: 'blur'
        }
      ],
      'authConfig.oauth2ClientSecret': [
        {
          validator: (rule: any, value: any) => {
            if (form.authConfig.authType === 'OAUTH2' && !value) {
              return new Error(t('thirdparty_api_source.oauth2_client_secret_required'))
            }
            return true
          },
          trigger: 'blur'
        }
      ],
      'authConfig.oauth2GrantType': [
        {
          validator: (rule: any, value: any) => {
            if (form.authConfig.authType === 'OAUTH2' && !value) {
              return new Error(t('thirdparty_api_source.oauth2_grant_type_required'))
            }
            return true
          },
          trigger: 'blur'
        }
      ],
      'authConfig.jwtToken': [
        {
          validator: (rule: any, value: any) => {
            if (form.authConfig.authType === 'JWT' && !value) {
              return new Error(t('thirdparty_api_source.jwt_token_required'))
            }
            return true
          },
          trigger: 'blur'
        }
      ],
      'selectInterface.url': [
        { required: true, message: t('thirdparty_api_source.input_interface_url_required'), trigger: ['blur', 'change'] }
      ],
      'submitInterface.url': [
        { required: true, message: t('thirdparty_api_source.submit_interface_url_required'), trigger: ['blur', 'change'] }
      ],
      'pollStatusInterface.url': [
        { required: true, message: t('thirdparty_api_source.query_interface_url_required'), trigger: ['blur', 'change'] }
      ],
      'stopInterface.url': [
        { required: true, message: t('thirdparty_api_source.stop_interface_url_required'), trigger: ['blur', 'change'] }
      ],
      'pollStatusInterface.pollingSuccessConfig': [
        {
          validator: (rule: any, value: any) => {
            if (!value.successField || !value.successValue) {
              return new Error(t('thirdparty_api_source.success_condition_required'))
            }
            return true
          },
          trigger: ['blur', 'change']
        }
      ],
      'pollStatusInterface.pollingFailureConfig': [
        {
          validator: (rule: any, value: any) => {
            if (!value.failureField || !value.failureValue) {
              return new Error(t('thirdparty_api_source.failure_condition_required'))
            }
            return true
          },
          trigger: ['blur', 'change']
        }
      ],
      'selectInterface.responseParameters': [
        {
          validator: (rule: any, value: any) => {
            const idField = value.find((item: any) => item.key === 'id')
            const nameField = value.find((item: any) => item.key === 'name')

            if (!idField || !idField.jsonPath) {
              return new Error(t('thirdparty_api_source.id_jsonpath_required'))
            }
            if (!nameField || !nameField.jsonPath) {
              return new Error(t('thirdparty_api_source.name_jsonpath_required'))
            }
            return true
          },
          trigger: ['blur', 'change']
        }
      ]
    }

    const formRef = ref<FormInst | null>(null)
    const isEditMode = computed(() => props.operationType === 'edit')

    // 定义表单的初始状态
    const getInitialFormState = () => ({
      systemName: '',
      serviceAddress: 'http://',
      interfaceTimeout: 120000, // 默认2分钟
      authConfig: {
        authType: '',
        headerPrefix: '',
        basicUsername: '',
        basicPassword: '',
        jwtToken: '',
        oauth2TokenUrl: '',
        oauth2ClientId: '',
        oauth2ClientSecret: '',
        oauth2GrantType: '',
        oauth2Username: '',
        oauth2Password: '',
        authMappings: []
      },
      selectInterface: {
        url: '',
        method: 'GET',
        parameters: [] as any[],
        body: '',
        responseParameters: [
          { key: 'id', jsonPath: '' },
          { key: 'name', jsonPath: '' }
        ]
      },
      submitInterface: {
        url: '',
        method: 'POST',
        parameters: [] as any[],
        body: '',
        responseParameters: [
          { key: 'taskInstanceId', jsonPath: '' }
        ]
      },
      pollStatusInterface: {
        url: '',
        method: 'GET',
        parameters: [] as any[],
        body: '',
        pollingSuccessConfig: { successField: '', successValue: '' },
        pollingFailureConfig: { failureField: '', failureValue: '' }
      },
      stopInterface: { url: '', method: 'POST', parameters: [] as any[], body: '' }
    })

    // 重置表单数据的函数
    const resetForm = () => {
      const initialState = getInitialFormState()
      Object.keys(form).forEach(key => {
        delete (form as any)[key]
      })
      Object.assign(form, initialState)
      formRef.value?.restoreValidation?.()
    }

    // 保存原始编辑数据，用于测试连接
    const originalEditData = ref<any>(null)
    // 监听modal显示状态和数据变化
    watch([() => props.show, () => props.data, () => props.operationType], ([show, data, operationType]) => {
      if (show) {
        if (data && operationType === 'edit') {
          originalEditData.value = JSON.parse(JSON.stringify(data))
          resetForm()
          const editData = originalEditData.value
          // 完全使用后端返回的数据
          Object.assign(form, editData)
        } else {
          originalEditData.value = null
          resetForm()
          // 只在创建模式下设置默认值
          form.authConfig.authType = 'BASIC_AUTH'
          form.authConfig.headerPrefix = 'Basic'
        }
      }
    }, { immediate: true })

    watch(() => form.authConfig.authType, (newAuthType) => {
      // 只在创建模式下自动设置headerPrefix
      if (!isEditMode.value) {
        if (newAuthType === 'BASIC_AUTH') {
          form.authConfig.headerPrefix = 'Basic'
        } else if (newAuthType === 'JWT' || newAuthType === 'OAUTH2') {
          form.authConfig.headerPrefix = 'Bearer'
        } else {
          form.authConfig.headerPrefix = ''
        }
      }
    })

    const handleClose = () => {
      resetForm()
      emit('close')
    }

    const handleSubmit = () => {
      (formRef.value as any)?.validate((errors: any) => {
        if (!errors) {
          if (isEditMode.value && originalEditData.value) {
            const submitData = JSON.parse(JSON.stringify(originalEditData.value))
            const initialState = getInitialFormState()
            Object.keys(initialState).forEach(key => {
              if (form.hasOwnProperty(key)) {
                submitData[key] = (form as any)[key]
              }
            })
            emit('submit', submitData)
          } else {
            emit('submit', JSON.parse(JSON.stringify(form)))
          }
        }
      })
    }

    const handleTest = () => {
      (formRef.value as any)?.validate((errors: any) => {
        if (!errors) {
          if (isEditMode.value && originalEditData.value) {
            const testData = JSON.parse(JSON.stringify(originalEditData.value))
            const initialState = getInitialFormState()
            Object.keys(initialState).forEach(key => {
              if (form.hasOwnProperty(key)) {
                testData[key] = (form as any)[key]
              }
            })
            emit('test', testData)
          } else {
            emit('test', JSON.parse(JSON.stringify(form)))
          }
        }
      })
    }

    // location 下拉选项与 method 联动
    const getLocationOptions = (method: string) => {
      return [
        { label: 'Header', value: 'HEADER' },
        { label: 'Param', value: 'PARAM' }
      ]
    }

   return () => (
     <NModal
       show={props.show}
       cancelShow={false}
       confirmShow={false}
       closeOnEsc={false}
       maskClosable={false}
       preset="card"
       class={[styles['thirdparty-modal'], 'dialog-source-modal']}
       title={
         isEditMode.value
           ? t('thirdparty_api_source.edit_thirdparty_api_source')
           : t('thirdparty_api_source.create_thirdparty_api_source')
       }
       onClose={handleClose}
     >
       <div class={styles['modal-content']}>
         <NForm
           labelWidth={120}
           labelAlign="left"
           model={form}
           rules={rules}
           ref={formRef}
         >
           <NFormItem
             label={t('thirdparty_api_source.system_name')}
             path="systemName"
             required
           >
             <NInput
               v-model={[form.systemName, 'value']}
               placeholder={t('thirdparty_api_source.system_name_tips')}
             />
           </NFormItem>

           <NFormItem
             label={t('thirdparty_api_source.service_address')}
             path="serviceAddress"
             required
           >
             <NInput
               v-model={[form.serviceAddress, 'value']}
               placeholder={t('thirdparty_api_source.service_address_tips')}
             />
           </NFormItem>

           {/* 接口超时时间 */}
           <NFormItem label={t('thirdparty_api_source.interface_timeout')} path="interfaceTimeout">
             <NInputNumber
               v-model={[form.interfaceTimeout, 'value']}
               placeholder={t('thirdparty_api_source.interface_timeout_tips')}
               min={1000}
               max={1200000}
               step={1000}
               class={styles['timeout-input']}
             >
               {{
                 suffix: () => t('thirdparty_api_source.millisecond')
               }}
             </NInputNumber>
             <div class={styles['timeout-description']}>
               {t('thirdparty_api_source.interface_timeout_description')}
             </div>
           </NFormItem>

           <NDivider />

           {/* 认证类型 */}
           <NFormItem path="authConfig.authType" required>
             {{
               label: () => (
                 <NSpace align="center" size="small">
                   <span>{t('thirdparty_api_source.auth_type')}</span>
                   <NTooltip
                     placement="top-start"
                     style={{ maxWidth: '500px', zIndex: 5000 }}
                     flip={false}
                   >
                     {{
                       trigger: () => (
                         <NIcon>
                           <InfoCircleOutlined />
                         </NIcon>
                       ),
                       default: () => t('thirdparty_api_source.auth_type_detail_info')
                     }}
                   </NTooltip>
                 </NSpace>
               ),
               default: () => (
                 <NSelect
                   v-model={[form.authConfig.authType, 'value']}
                   options={authTypeOptions.value}
                   class={styles['auth-type-select']}
                   placeholder={t('thirdparty_api_source.auth_type_tips')}
                 />
               )
             }}
           </NFormItem>

           <NFormItem label={t('thirdparty_api_source.header_prefix')}>
             <NInput
               v-model={[form.authConfig.headerPrefix, 'value']}
               placeholder={t('thirdparty_api_source.header_prefix_tips')}
               value={form.authConfig.headerPrefix || ''}
             />
           </NFormItem>

           {/* BASIC_AUTH */}
           <NFormItem
             v-show={form.authConfig.authType === 'BASIC_AUTH'}
             label={t('thirdparty_api_source.username')}
             path="authConfig.basicUsername"
             required
           >
             <NInput
               v-model={[form.authConfig.basicUsername, 'value']}
               placeholder={t('thirdparty_api_source.username_tips')}
             />
           </NFormItem>
           <NFormItem
             v-show={form.authConfig.authType === 'BASIC_AUTH'}
             label={t('thirdparty_api_source.password')}
             path="authConfig.basicPassword"
             required
           >
             <NInput
               v-model={[form.authConfig.basicPassword, 'value']}
               placeholder={t('thirdparty_api_source.password_tips')}
               type="password"
               show-password-on="click"
             />
           </NFormItem>

           {/* OAUTH2 */}
           <NFormItem
             path="authConfig.oauth2TokenUrl"
             required
             v-show={form.authConfig.authType === 'OAUTH2'}
           >
             {{
               label: () => (
                 <NSpace align="center" size="small">
                   <span>{t('thirdparty_api_source.oauth2_token_url')}</span>
                   <NTooltip
                     placement="top-start"
                     style={{ maxWidth: '500px', zIndex: 5000 }}
                     flip={false}
                   >
                     {{
                       trigger: () => (
                         <NIcon style={{ marginLeft: '4px' }}>
                           <InfoCircleOutlined />
                         </NIcon>
                       ),
                       default: () => t('thirdparty_api_source.oauth2_url_info')
                     }}
                   </NTooltip>
                 </NSpace>
               ),
               default: () => (
                 <NInput
                   v-model={[form.authConfig.oauth2TokenUrl, 'value']}
                   placeholder={t('thirdparty_api_source.oauth2_token_url_tips')}
                 />
               )
             }}
           </NFormItem>

           <NFormItem
             v-show={form.authConfig.authType === 'OAUTH2'}
             label={t('thirdparty_api_source.oauth2_client_id')}
             path="authConfig.oauth2ClientId"
             required
           >
             <NInput
               v-model={[form.authConfig.oauth2ClientId, 'value']}
               placeholder={t('thirdparty_api_source.oauth2_client_id_tips')}
             />
           </NFormItem>
           <NFormItem
             v-show={form.authConfig.authType === 'OAUTH2'}
             label={t('thirdparty_api_source.oauth2_client_secret')}
             path="authConfig.oauth2ClientSecret"
             required
           >
             <NInput
               v-model={[form.authConfig.oauth2ClientSecret, 'value']}
               placeholder={t('thirdparty_api_source.oauth2_client_secret_tips')}
             />
           </NFormItem>
           <NFormItem
             v-show={form.authConfig.authType === 'OAUTH2'}
             label={t('thirdparty_api_source.oauth2_grant_type')}
             path="authConfig.oauth2GrantType"
             required
           >
             <NInput
               v-model={[form.authConfig.oauth2GrantType, 'value']}
               placeholder={t('thirdparty_api_source.oauth2_grant_type_tips')}
             />
           </NFormItem>
           <NFormItem
             v-show={form.authConfig.authType === 'OAUTH2'}
             label={t('thirdparty_api_source.oauth2_username')}
           >
             <NInput
               v-model={[form.authConfig.oauth2Username, 'value']}
               placeholder={t('thirdparty_api_source.oauth2_username_tips')}
             />
           </NFormItem>
           <NFormItem
             v-show={form.authConfig.authType === 'OAUTH2'}
             label={t('thirdparty_api_source.oauth2_password')}
           >
             <NInput
               v-model={[form.authConfig.oauth2Password, 'value']}
               placeholder={t('thirdparty_api_source.oauth2_password_tips')}
               type="password"
               show-password-on="click"
             />
           </NFormItem>

           {/* JWT */}
           <NFormItem
             v-show={form.authConfig.authType === 'JWT'}
             label={t('thirdparty_api_source.jwt_token')}
             path="authConfig.jwtToken"
             required
           >
             <NInput
               v-model={[form.authConfig.jwtToken, 'value']}
               placeholder={t('thirdparty_api_source.jwt_token_tips')}
             />
           </NFormItem>

           {/* 额外参数 */}
           <NFormItem label={t('thirdparty_api_source.additional_params')}>
             <NDynamicInput
               v-model={[form.authConfig.authMappings, 'value']}
               onCreate={() => ({ key: '', value: '' })}
               style={{ width: '100%' }}
             >
               {{
                 default: ({ value }: { value: { key: string; value: string } }) => (
                   <NSpace style={{ width: '100%', flexWrap: 'wrap' }}>
                     <NInput
                       v-model={[value.key, 'value']}
                       placeholder={t('thirdparty_api_source.key')}
                       class={styles['key-input']}
                     />
                     <NInput
                       v-model={[value.value, 'value']}
                       placeholder={t('thirdparty_api_source.value')}
                       class={styles['value-input']}
                     />
                   </NSpace>
                 )
               }}
             </NDynamicInput>
           </NFormItem>

           <NDivider />

           {/* 输入接口 */}
           <NFormItem path="selectInterface.url" required>
             {{
               label: () => (
                 <NSpace align="center" size="small">
                   <span>{t('thirdparty_api_source.input_interface')}</span>
                   <NTooltip
                     placement="top-start"
                     style={{ maxWidth: '500px', zIndex: 5000 }}
                     flip={false}
                   >
                     {{
                       trigger: () => (
                         <NIcon style={{ marginLeft: '4px' }}>
                           <InfoCircleOutlined />
                         </NIcon>
                       ),
                       default: () => t('thirdparty_api_source.input_interface_detail_info')
                     }}
                   </NTooltip>
                 </NSpace>
               ),
               default: () => (
                 <>
                   <NInput
                     v-model={[form.selectInterface.url, 'value']}
                     placeholder={t('thirdparty_api_source.input_interface_tips')}
                     onChange={() => formRef.value?.validate?.()}
                   />
                   <NSelect
                     v-model={[form.selectInterface.method, 'value']}
                     options={methodOptions.value}
                     class={styles['method-select']}
                   />
                 </>
               )
             }}
           </NFormItem>

           <NFormItem label={t('thirdparty_api_source.parameters')}>
             <NDynamicInput
               v-model={[form.selectInterface.parameters, 'value']}
               onCreate={() => ({ paramName: '', paramValue: '', location: 'HEADER' })}
               style={{ width: '100%' }}
             >
               {{
                 default: ({
                   value
                 }: {
                   value: { paramName: string; paramValue: string; location: string }
                 }) => (
                   <NSpace style={{ width: '100%', flexWrap: 'nowrap' }}>
                     <NSelect
                       v-model={[value.location, 'value']}
                       options={getLocationOptions(form.selectInterface.method)}
                       placeholder={t('thirdparty_api_source.param_location_tips')}
                       class={styles['param-location']}
                     />
                     <NInput
                       v-model={[value.paramName, 'value']}
                       placeholder={t('thirdparty_api_source.param_name_tips')}
                       class={styles['param-name']}
                     />
                     <NInput
                       v-model={[value.paramValue, 'value']}
                       placeholder={t('thirdparty_api_source.param_value_tips')}
                       class={styles['param-value']}
                     />
                   </NSpace>
                 )
               }}
             </NDynamicInput>
           </NFormItem>

           {(form.selectInterface.method === 'POST' || form.selectInterface.method === 'PUT') && (
             <NFormItem>
               {{
                 label: () => (
                   <NSpace align="center" size="small">
                     <span>{t('thirdparty_api_source.request_body')}</span>
                     <NTooltip
                       placement="top-start"
                       style={{ maxWidth: '500px', zIndex: 5000 }}
                       flip={false}
                     >
                       {{
                         trigger: () => (
                           <NIcon style={{ marginLeft: '4px' }}>
                             <InfoCircleOutlined />
                           </NIcon>
                         ),
                         default: () => t('thirdparty_api_source.input_interface_body_info')
                       }}
                     </NTooltip>
                   </NSpace>
                 ),
                 default: () => (
                   <MonacoEditor
                     v-model={[form.selectInterface.body, 'value']}
                     options={{ language: 'json', readOnly: false }}
                   />
                 )
               }}
             </NFormItem>
           )}

           <NFormItem path="selectInterface.responseParameters" required>
             {{
               label: () => (
                 <NSpace align="center" size="small">
                   <span>{t('thirdparty_api_source.extract_response_data')}</span>
                   <NTooltip
                     placement="top-start"
                     style={{ maxWidth: '500px', zIndex: 5000 }}
                     flip={false}
                   >
                     {{
                       trigger: () => (
                         <NIcon style={{ marginLeft: '4px' }}>
                           <InfoCircleOutlined />
                         </NIcon>
                       ),
                       default: () => t('thirdparty_api_source.input_interface_extract_info')
                     }}
                   </NTooltip>
                 </NSpace>
               ),
               default: () => (
                 <NDynamicInput
                   v-model={[form.selectInterface.responseParameters, 'value']}
                   onCreate={() => ({ key: '', jsonPath: '', disabled: false })}
                   style={{ width: '100%' }}
                 >
                   {{
                     default: ({
                       value
                     }: {
                       value: { key: string; jsonPath: string; disabled: boolean }
                     }) => (
                       <NSpace style={{ width: '100%', flexWrap: 'wrap' }}>
                         <NInput
                           v-model={[value.key, 'value']}
                           placeholder={t('thirdparty_api_source.extract_field')}
                           class={styles['extract-key']}
                           disabled={value.disabled}
                         />
                         <NInput
                           v-model={[value.jsonPath, 'value']}
                           placeholder={t('thirdparty_api_source.json_path_list')}
                           class={styles['extract-path']}
                           disabled={value.disabled}
                         />
                       </NSpace>
                     )
                   }}
                 </NDynamicInput>
               )
             }}
           </NFormItem>

           <NDivider />

           {/* 提交接口 */}
           <NFormItem path="submitInterface.url" required>
             {{
               label: () => (
                 <NSpace align="center" size="small">
                   <span>{t('thirdparty_api_source.submit_interface')}</span>
                   <NTooltip
                     placement="top-start"
                     style={{ maxWidth: '500px', zIndex: 5000 }}
                     flip={false}
                   >
                     {{
                       trigger: () => (
                         <NIcon style={{ marginLeft: '4px' }}>
                           <InfoCircleOutlined />
                         </NIcon>
                       ),
                       default: () => t('thirdparty_api_source.submit_interface_detail_info')
                     }}
                   </NTooltip>
                 </NSpace>
               ),
               default: () => (
                 <>
                   <NInput
                     v-model={[form.submitInterface.url, 'value']}
                     placeholder={t('thirdparty_api_source.submit_interface_tips')}
                     class={styles['submit-url']}
                     onChange={() => formRef.value?.validate?.()}
                   />
                   <NSelect
                     v-model={[form.submitInterface.method, 'value']}
                     options={methodOptions.value}
                     class={styles['submit-method']}
                   />
                 </>
               )
             }}
           </NFormItem>

           <NFormItem label={t('thirdparty_api_source.parameters')}>
             <NDynamicInput
               v-model={[form.submitInterface.parameters, 'value']}
               onCreate={() => ({ paramName: '', paramValue: '', location: 'HEADER' })}
               style={{ width: '100%' }}
             >
               {{
                 default: ({
                   value
                 }: {
                   value: { paramName: string; paramValue: string; location: string }
                 }) => (
                   <NSpace style={{ width: '100%', flexWrap: 'nowrap' }}>
                     <NSelect
                       v-model={[value.location, 'value']}
                       options={getLocationOptions(form.submitInterface.method)}
                       placeholder={t('thirdparty_api_source.param_location_tips')}
                       class={styles['param-location']}
                     />
                     <NInput
                       v-model={[value.paramName, 'value']}
                       placeholder={t('thirdparty_api_source.param_name_tips')}
                       class={styles['param-name']}
                     />
                     <NInput
                       v-model={[value.paramValue, 'value']}
                       placeholder={t('thirdparty_api_source.param_value_tips')}
                       class={styles['param-value']}
                     />
                   </NSpace>
                 )
               }}
             </NDynamicInput>
           </NFormItem>

           {(form.submitInterface.method === 'POST' || form.submitInterface.method === 'PUT') && (
             <NFormItem>
               {{
                 label: () => (
                   <NSpace align="center" size="small">
                     <span>{t('thirdparty_api_source.request_body')}</span>
                     <NTooltip
                       placement="top-start"
                       style={{ maxWidth: '500px', zIndex: 5000 }}
                       flip={false}
                     >
                       {{
                         trigger: () => (
                           <NIcon style={{ marginLeft: '4px' }}>
                             <InfoCircleOutlined />
                           </NIcon>
                         ),
                         default: () => t('thirdparty_api_source.submit_interface_body_info')
                       }}
                     </NTooltip>
                   </NSpace>
                 ),
                 default: () => (
                   <MonacoEditor
                     v-model={[form.submitInterface.body, 'value']}
                     options={{ language: 'json', readOnly: false }}
                   />
                 )
               }}
             </NFormItem>
           )}

           <NFormItem path="submitInterface.responseParameters" required>
             {{
               label: () => (
                 <NSpace align="center" size="small">
                   <span>{t('thirdparty_api_source.extract_response_data')}</span>
                   <NTooltip
                     placement="top-start"
                     style={{ maxWidth: '500px', zIndex: 5000 }}
                     flip={false}
                   >
                     {{
                       trigger: () => (
                         <NIcon style={{ marginLeft: '4px' }}>
                           <InfoCircleOutlined />
                         </NIcon>
                       ),
                       default: () => t('thirdparty_api_source.submit_interface_extract_info')
                     }}
                   </NTooltip>
                 </NSpace>
               ),
               default: () => (
                 <NDynamicInput
                   v-model={[form.submitInterface.responseParameters, 'value']}
                   onCreate={() => ({ key: '', jsonPath: '', disabled: false })}
                   style={{ width: '100%' }}
                 >
                   {{
                     default: ({
                       value
                     }: {
                       value: { key: string; jsonPath: string; disabled: boolean }
                     }) => (
                       <NSpace style={{ width: '100%', flexWrap: 'wrap' }}>
                         <NInput
                           v-model={[value.key, 'value']}
                           placeholder={t('thirdparty_api_source.extract_field')}
                           class={styles['extract-key']}
                           disabled={value.disabled}
                         />
                         <NInput
                           v-model={[value.jsonPath, 'value']}
                           placeholder={t('thirdparty_api_source.json_path')}
                           class={styles['extract-path']}
                           disabled={value.disabled}
                         />
                       </NSpace>
                     )
                   }}
                 </NDynamicInput>
               )
             }}
           </NFormItem>

           <NDivider />

           {/* 查询接口 */}
           <NFormItem path="pollStatusInterface.url" required>
             {{
               label: () => (
                 <NSpace align="center" size="small">
                   <span>{t('thirdparty_api_source.query_interface')}</span>
                   <NTooltip
                     placement="top-start"
                     style={{ maxWidth: '500px', zIndex: 5000 }}
                     flip={false}
                   >
                     {{
                       trigger: () => (
                         <NIcon style={{ marginLeft: '4px' }}>
                           <InfoCircleOutlined />
                         </NIcon>
                       ),
                       default: () => t('thirdparty_api_source.query_interface_detail_info')
                     }}
                   </NTooltip>
                 </NSpace>
               ),
               default: () => (
                 <>
                   <NInput
                     v-model={[form.pollStatusInterface.url, 'value']}
                     placeholder={t('thirdparty_api_source.query_interface_tips')}
                     onChange={() => formRef.value?.validate?.()}
                   />
                   <NSelect
                     v-model={[form.pollStatusInterface.method, 'value']}
                     options={methodOptions.value}
                     class={styles['method-select']}
                   />
                 </>
               )
             }}
           </NFormItem>

           <NFormItem label={t('thirdparty_api_source.parameters')}>
             <NDynamicInput
               v-model={[form.pollStatusInterface.parameters, 'value']}
               onCreate={() => ({ paramName: '', paramValue: '', location: 'HEADER' })}
               style={{ width: '100%' }}
             >
               {{
                 default: ({
                   value
                 }: {
                   value: { paramName: string; paramValue: string; location: string }
                 }) => (
                   <NSpace style={{ width: '100%', flexWrap: 'nowrap' }}>
                     <NSelect
                       v-model={[value.location, 'value']}
                       options={getLocationOptions(form.pollStatusInterface.method)}
                       placeholder={t('thirdparty_api_source.param_location_tips')}
                       class={styles['param-location']}
                     />
                     <NInput
                       v-model={[value.paramName, 'value']}
                       placeholder={t('thirdparty_api_source.param_name_tips')}
                       class={styles['param-name']}
                     />
                     <NInput
                       v-model={[value.paramValue, 'value']}
                       placeholder={t('thirdparty_api_source.param_value_tips')}
                       class={styles['param-value']}
                     />
                   </NSpace>
                 )
               }}
             </NDynamicInput>
           </NFormItem>

           {(form.pollStatusInterface.method === 'POST' ||
             form.pollStatusInterface.method === 'PUT') && (
             <NFormItem>
               {{
                 label: () => (
                   <NSpace align="center" size="small">
                     <span>{t('thirdparty_api_source.request_body')}</span>
                     <NTooltip
                       placement="top-start"
                       style={{ maxWidth: '500px', zIndex: 5000 }}
                       flip={false}
                     >
                       {{
                         trigger: () => (
                           <NIcon style={{ marginLeft: '4px' }}>
                             <InfoCircleOutlined />
                           </NIcon>
                         ),
                         default: () => t('thirdparty_api_source.query_interface_body_info')
                       }}
                     </NTooltip>
                   </NSpace>
                 ),
                 default: () => (
                   <MonacoEditor
                     v-model={[form.pollStatusInterface.body, 'value']}
                     options={{ language: 'json', readOnly: false }}
                   />
                 )
               }}
             </NFormItem>
           )}

           <NFormItem path="pollStatusInterface.pollingSuccessConfig" required>
             {{
               label: () => (
                 <NSpace align="center" size="small">
                   <span>{t('thirdparty_api_source.success_condition')}</span>
                   <NTooltip
                     placement="top-start"
                     style={{ maxWidth: '500px', zIndex: 5000 }}
                     flip={false}
                   >
                     {{
                       trigger: () => (
                         <NIcon style={{ marginLeft: '4px' }}>
                           <InfoCircleOutlined />
                         </NIcon>
                       ),
                       default: () => t('thirdparty_api_source.query_interface_success_info')
                     }}
                   </NTooltip>
                 </NSpace>
               ),
               default: () => (
                 <>
                   <NInput
                     v-model={[form.pollStatusInterface.pollingSuccessConfig.successField, 'value']}
                     placeholder={t('thirdparty_api_source.success_field_tips')}
                     class={styles['condition-field']}
                     onChange={() => formRef.value?.validate?.()}
                   />
                   <NInput
                     v-model={[form.pollStatusInterface.pollingSuccessConfig.successValue, 'value']}
                     placeholder={t('thirdparty_api_source.success_value_tips')}
                     class={styles['condition-value']}
                     onChange={() => formRef.value?.validate?.()}
                   />
                 </>
               )
             }}
           </NFormItem>

           <NFormItem path="pollStatusInterface.pollingFailureConfig" required>
             {{
               label: () => (
                 <NSpace align="center" size="small">
                   <span>{t('thirdparty_api_source.failure_condition')}</span>
                   <NTooltip
                     placement="top-start"
                     style={{ maxWidth: '500px', zIndex: 5000 }}
                     flip={false}
                   >
                     {{
                       trigger: () => (
                         <NIcon style={{ marginLeft: '4px' }}>
                           <InfoCircleOutlined />
                         </NIcon>
                       ),
                       default: () => t('thirdparty_api_source.query_interface_failed_info')
                     }}
                   </NTooltip>
                 </NSpace>
               ),
               default: () => (
                 <>
                   <NInput
                     v-model={[form.pollStatusInterface.pollingFailureConfig.failureField, 'value']}
                     placeholder={t('thirdparty_api_source.failure_field_tips')}
                     class={styles['condition-field']}
                     onChange={() => formRef.value?.validate?.()}
                   />
                   <NInput
                     v-model={[form.pollStatusInterface.pollingFailureConfig.failureValue, 'value']}
                     placeholder={t('thirdparty_api_source.failure_value_tips')}
                     class={styles['condition-value']}
                     onChange={() => formRef.value?.validate?.()}
                   />
                 </>
               )
             }}
           </NFormItem>

           <NDivider />

           {/* 停止接口 */}
           <NFormItem path="stopInterface.url" required>
             {{
               label: () => (
                 <NSpace align="center" size="small">
                   <span>{t('thirdparty_api_source.stop_interface')}</span>
                   <NTooltip
                     placement="top-start"
                     style={{ maxWidth: '500px', zIndex: 5000 }}
                     flip={false}
                   >
                     {{
                       trigger: () => (
                         <NIcon style={{ marginLeft: '4px' }}>
                           <InfoCircleOutlined />
                         </NIcon>
                       ),
                       default: () => t('thirdparty_api_source.stop_interface_detail_info')
                     }}
                   </NTooltip>
                 </NSpace>
               ),
               default: () => (
                 <>
                   <NInput
                     v-model={[form.stopInterface.url, 'value']}
                     placeholder={t('thirdparty_api_source.stop_interface_tips')}
                     onChange={() => formRef.value?.validate?.()}
                   />
                   <NSelect
                     v-model={[form.stopInterface.method, 'value']}
                     options={methodOptions.value}
                     class={styles['method-select']}
                   />
                 </>
               )
             }}
           </NFormItem>

           <NFormItem label={t('thirdparty_api_source.parameters')}>
             <NDynamicInput
               v-model={[form.stopInterface.parameters, 'value']}
               onCreate={() => ({ paramName: '', paramValue: '', location: 'HEADER' })}
               style={{ width: '100%' }}
             >
               {{
                 default: ({
                   value
                 }: {
                   value: { paramName: string; paramValue: string; location: string }
                 }) => (
                   <NSpace style={{ width: '100%', flexWrap: 'nowrap' }}>
                     <NSelect
                       v-model={[value.location, 'value']}
                       options={getLocationOptions(form.stopInterface.method)}
                       placeholder={t('thirdparty_api_source.param_location_tips')}
                       class={styles['param-location']}
                     />
                     <NInput
                       v-model={[value.paramName, 'value']}
                       placeholder={t('thirdparty_api_source.param_name_tips')}
                       class={styles['param-name']}
                     />
                     <NInput
                       v-model={[value.paramValue, 'value']}
                       placeholder={t('thirdparty_api_source.param_value_tips')}
                       class={styles['param-value']}
                     />
                   </NSpace>
                 )
               }}
             </NDynamicInput>
           </NFormItem>

           {(form.stopInterface.method === 'POST' ||
             form.stopInterface.method === 'PUT') && (
             <NFormItem>
               {{
                 label: () => (
                   <NSpace align="center" size="small">
                     <span>{t('thirdparty_api_source.request_body')}</span>
                     <NTooltip
                       placement="top-start"
                       style={{ maxWidth: '500px', zIndex: 5000 }}
                       flip={false}
                     >
                       {{
                         trigger: () => (
                           <NIcon style={{ marginLeft: '4px' }}>
                             <InfoCircleOutlined />
                           </NIcon>
                         ),
                         default: () => t('thirdparty_api_source.stop_interface_body_info')
                       }}
                     </NTooltip>
                   </NSpace>
                 ),
                 default: () => (
                   <MonacoEditor
                     v-model={[form.stopInterface.body, 'value']}
                     options={{ language: 'json', readOnly: false }}
                   />
                 )
               }}
             </NFormItem>
           )}
         </NForm>
       </div>

       <div class={styles['modal-footer']}>
         <NSpace justify="end">
           <NButton onClick={handleClose}>
             {t('thirdparty_api_source.cancel')}
           </NButton>
           <NButton type="primary" onClick={handleTest}>
             {t('thirdparty_api_source.test')}
           </NButton>
           <NButton type="primary" onClick={handleSubmit}>
             {t('thirdparty_api_source.submit')}
           </NButton>
         </NSpace>
       </div>
     </NModal>
   )
  }
})
