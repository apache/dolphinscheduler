import { computed, watch, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useResources, useCustomParams } from '.'
import { viewResource } from '@/service/modules/resources'
import { useUserStore } from '@/store/user/user'
import type { IJsonItem } from '../types'

/** Script source: inline input */
const SCRIPT_SOURCE_SCRIPT = 'SCRIPT'
/** Script source: resource center file */
const SCRIPT_SOURCE_FILE = 'FILE'

export function useFlinkSqlGateway(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()
  const userStore = useUserStore()
  const resourceContentLoading = ref(false)

  if (!model.flinkJdbcUrl) model.flinkJdbcUrl = 'jdbc:flink://host:port'
  if (!model.statementSeparator) model.statementSeparator = ';'
  if (!model.maxPrintRows) model.maxPrintRows = 0
  if (!model.initScript) model.initScript = ''
  if (!model.rawScript) model.rawScript = ''
  if (!model.rawScriptType) model.rawScriptType = SCRIPT_SOURCE_FILE
  if (!model.resourceList) model.resourceList = []
  if (!model.initScriptType) model.initScriptType = SCRIPT_SOURCE_FILE
  if (!model.initScriptResourceList) model.initScriptResourceList = []

  const initResourceContentLoading = ref(false)
  const resourcesRequired = ref(model.rawScriptType === SCRIPT_SOURCE_FILE)
  const initResourcesRequired = ref(model.initScriptType === SCRIPT_SOURCE_FILE)
  const resourcesLimit = computed(() =>
    model.rawScriptType === SCRIPT_SOURCE_SCRIPT ? -1 : 1
  )
  const resourcesSpan = computed(() =>
    model.rawScriptType === SCRIPT_SOURCE_FILE ? 24 : 0
  )
  const initResourcesSpan = computed(() =>
    model.initScriptType === SCRIPT_SOURCE_FILE ? 24 : 0
  )
  const scriptEditorReadonly = computed(() => model.rawScriptType === SCRIPT_SOURCE_FILE)
  const initScriptEditorReadonly = computed(() => model.initScriptType === SCRIPT_SOURCE_FILE)

  const loadResourceContent = (fullName: string) => {
    const tenantCode = (userStore.getUserInfo as any)?.tenantCode
    if (!tenantCode) {
      model.rawScript = ''
      return
    }
    resourceContentLoading.value = true
    viewResource({
      fullName,
      tenantCode,
      skipLineNum: 0,
      limit: -1
    })
      .then((res: { content: string }) => {
        model.rawScript = res?.content ?? ''
      })
      .catch(() => {
        model.rawScript = ''
      })
      .finally(() => {
        resourceContentLoading.value = false
      })
  }

  const loadInitResourceContent = (fullName: string) => {
    const tenantCode = (userStore.getUserInfo as any)?.tenantCode
    if (!tenantCode) {
      model.initScript = ''
      return
    }
    initResourceContentLoading.value = true
    viewResource({
      fullName,
      tenantCode,
      skipLineNum: 0,
      limit: -1
    })
      .then((res: { content: string }) => {
        model.initScript = res?.content ?? ''
      })
      .catch(() => {
        model.initScript = ''
      })
      .finally(() => {
        initResourceContentLoading.value = false
      })
  }

  watch(
    () => model.rawScriptType,
    () => {
      resourcesRequired.value = model.rawScriptType === SCRIPT_SOURCE_FILE
      if (model.rawScriptType === SCRIPT_SOURCE_SCRIPT) {
        model.resourceList = []
      } else if (model.resourceList?.length === 1) {
        loadResourceContent(model.resourceList[0])
      } else {
        model.rawScript = ''
      }
    }
  )

  watch(
    () => model.resourceList,
    (list) => {
      if (model.rawScriptType === SCRIPT_SOURCE_FILE && list?.length === 1) {
        loadResourceContent(list[0])
      } else if (model.rawScriptType === SCRIPT_SOURCE_FILE) {
        model.rawScript = ''
      }
    },
    { deep: true, immediate: true }
  )

  watch(
    () => model.initScriptType,
    () => {
      initResourcesRequired.value = model.initScriptType === SCRIPT_SOURCE_FILE
      if (model.initScriptType === SCRIPT_SOURCE_SCRIPT) {
        model.initScriptResourceList = []
      } else if (model.initScriptResourceList?.length === 1) {
        loadInitResourceContent(model.initScriptResourceList[0])
      } else {
        model.initScript = ''
      }
    }
  )

  watch(
    () => model.initScriptResourceList,
    (list) => {
      if (model.initScriptType === SCRIPT_SOURCE_FILE && list?.length === 1) {
        loadInitResourceContent(list[0])
      } else if (model.initScriptType === SCRIPT_SOURCE_FILE) {
        model.initScript = ''
      }
    },
    { deep: true, immediate: true }
  )

  const SCRIPT_SOURCE_OPTIONS = [
    { label: t('project.node.sql_execution_type_from_script'), value: SCRIPT_SOURCE_SCRIPT },
    { label: t('project.node.sql_execution_type_from_file'), value: SCRIPT_SOURCE_FILE }
  ]

  return [
    {
      type: 'input',
      field: 'flinkJdbcUrl',
      span: 24,
      name: 'JDBC URL',
      props: { placeholder: 'jdbc:flink://host:port' },
      value: model.flinkJdbcUrl,
      validate: { trigger: ['blur', 'input'], required: true }
    },
    {
      type: 'input',
      field: 'statementSeparator',
      span: 24,
      name: 'Statement Separator',
      props: { placeholder: ';' },
      value: model.statementSeparator
    },
    {
      type: 'input-number',
      field: 'maxPrintRows',
      span: 24,
      name: 'Max Print Rows',
      props: { min: 0 },
      value: model.maxPrintRows
    },
    {
      type: 'select',
      field: 'initScriptType',
      span: 12,
      name: t('project.node.init_script_source'),
      options: SCRIPT_SOURCE_OPTIONS,
      validate: { trigger: ['input', 'blur'], required: true }
    },
    useResources(
      initResourcesSpan,
      initResourcesRequired,
      computed(() => (model.initScriptType === SCRIPT_SOURCE_FILE ? 1 : -1)),
      'initScriptResourceList'
    ),
    {
      type: 'editor',
      field: 'initScript',
      span: 24,
      name: 'Init Script',
      props: {
        language: 'sql',
        readOnly: initScriptEditorReadonly,
        loading: initResourceContentLoading
      },
      value: model.initScript
    },
    {
      type: 'select',
      field: 'rawScriptType',
      span: 12,
      name: t('project.node.sql_execution_type'),
      options: SCRIPT_SOURCE_OPTIONS,
      validate: { trigger: ['input', 'blur'], required: true }
    },
    useResources(resourcesSpan, resourcesRequired, resourcesLimit),
    {
      type: 'editor',
      field: 'rawScript',
      span: 24,
      name: 'Script',
      props: {
        language: 'sql',
        readOnly: scriptEditorReadonly,
        loading: resourceContentLoading
      },
      value: model.rawScript,
      validate: {
        trigger: ['blur', 'input'],
        required: model.rawScriptType === SCRIPT_SOURCE_SCRIPT
      }
    },
    ...useCustomParams({
      model,
      field: 'localParams',
      isSimple: model.readonly
    })
  ]
}
