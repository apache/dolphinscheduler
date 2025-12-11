import { defineComponent, ref, onMounted, computed } from 'vue'
import {
  NDataTable,
  NButton,
  NSpace,
  NPopconfirm,
  NIcon,
  NPagination,
  NTooltip
} from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { deleteThirdpartyApiSource, queryThirdpartyApiSourceListPaging, createThirdpartyApiSource, updateThirdpartyApiSource, getThirdpartyApiSourceById, testThirdpartyApiSourceConnection } from '@/service/modules/thirdparty-api-source'
import type { ThirdpartyApiSource } from '@/service/modules/thirdparty-api-source/types'
import Card from '@/components/card'
import Search from '@/components/input-search'
import { EditOutlined, DeleteOutlined } from '@vicons/antd'
import ThirdpartyApiSourceModal from './modal'
import { de } from 'date-fns/locale'

export default defineComponent({
  name: 'ThirdpartyApiSourceList',
  setup() {
    const { t } = useI18n()
    const router = useRouter()

    const tableData = ref<ThirdpartyApiSource[]>([])
    const loading = ref(false)
    const searchVal = ref('')
    const page = ref(1)
    const pageSize = ref(10)
    const itemCount = ref(0)
    const showModal = ref(false)
    const editData = ref<any>(null)
    const operationType = ref<'create' | 'edit'>('create')

    // 获取真实接口数据
    const getTableData = async () => {
      loading.value = true
      try {
        const res = await queryThirdpartyApiSourceListPaging({
          pageNo: page.value,
          pageSize: pageSize.value,
          searchVal: searchVal.value || undefined
        }) as any
        if(res) {
          tableData.value = (res.totalList || []) as ThirdpartyApiSource[]
          itemCount.value = res.total || 0
        }
      } finally {
        loading.value = false
      }
    }

    const handleDelete = async (row: ThirdpartyApiSource) => {
      await deleteThirdpartyApiSource(row.id!)
      await getTableData()
    }

    const changePage = (p: number) => {
      page.value = p
      getTableData()
    }
    const changePageSize = (ps: number) => {
      page.value = 1
      pageSize.value = ps
      getTableData()
    }

    const handleCreate = () => {
      editData.value = null
      operationType.value = 'create'
      showModal.value = true
    }
    const handleEdit = async (row: any) => {
      // 获取详情
      const detail = await getThirdpartyApiSourceById(row.id)
      editData.value = detail
      operationType.value = 'edit'
      showModal.value = true
    }
    const handleModalClose = () => {
      showModal.value = false
      editData.value = null
      operationType.value = 'create'
    }
    const handleModalSubmit = async (data: any) => {
      const res = data.id ? await updateThirdpartyApiSource(data.id, data) : await createThirdpartyApiSource(data)
      if(res) {
        window.$message.success(data.id ? t('message.edit.success') : t('message.create.success'))
      } else {
        window.$message.error(data.id ? t('message.edit.failed') : t('message.create.failed'))
      }
      showModal.value = false
      editData.value = null
      getTableData()
    }
    const handleModalTest = async (data: any) => {
      try {
        const res = await testThirdpartyApiSourceConnection(data)
        window.$message.success(
          res && res.msg
            ? res.msg
            : `${t('datasource.test_connect')} ${t('datasource.success')}`
        )
      } catch (e: any) {
        console.log(e)
      }
    }

    const columns = computed(() => [
      {
        title: t('thirdparty_api_source.id'),
        key: 'id'
      },
      {
        title: t('thirdparty_api_source.system_name'),
        key: 'name'
      },
      {
        title: t('thirdparty_api_source.create_time'),
        key: 'createTime',
        render: (row: any) => row.createTime ? row.createTime : '-'
      },
      {
        title: t('thirdparty_api_source.update_time'),
        key: 'updateTime',
        render: (row: any) => row.updateTime ? row.updateTime : '-'
      },
      {
        title: t('datasource.operation'),
        key: 'actions',
        render: (row: ThirdpartyApiSource) => {
          return (
            <NSpace>
              <NTooltip>
                {{
                  trigger: () => (
                    <NButton
                      circle
                      type='info'
                      size='small'
                      onClick={() => handleEdit(row)}
                    >
                      <NIcon><EditOutlined /></NIcon>
                    </NButton>
                  ),
                  default: () => t('thirdparty_api_source.edit')
                }}
              </NTooltip>
              <NTooltip>
                {{
                  trigger: () => (
                    <NPopconfirm onPositiveClick={() => handleDelete(row)}>
                      {{
                        trigger: () => (
                          <NButton circle type='error' size='small' class='btn-delete'>
                            <NIcon><DeleteOutlined /></NIcon>
                          </NButton>
                        ),
                        default: () => t('datasource.delete_confirm')
                      }}
                    </NPopconfirm>
                  ),
                  default: () => t('thirdparty_api_source.delete')
                }}
              </NTooltip>
            </NSpace>
          )
        }
      }
    ])

    onMounted(() => {
      getTableData()
    })

    return () => (
      <NSpace vertical>
        <Card>
          <NSpace justify='space-between'>
            <NButton
              type='primary'
              size='small'
              onClick={handleCreate}
            >
              {t('thirdparty_api_source.create_thirdparty_api_source')}
            </NButton>
            <NSpace>
              <Search
                placeholder={t('resource.file.enter_keyword_tips')}
                v-model:value={searchVal.value}
                onSearch={getTableData}
              />
              <NButton size='small' type='primary' onClick={getTableData}>
                {t('thirdparty_api_source.search')}
              </NButton>
            </NSpace>
          </NSpace>
        </Card>
        <Card title={t('thirdparty_api_source.thirdparty_api_source')}>
          <NSpace vertical>
            <NDataTable
              loading={loading.value}
              columns={columns.value}
              data={tableData.value}
              striped
              size={'small'}
            />
            <NSpace justify='center'>
              <NPagination
                page={page.value}
                page-size={pageSize.value}
                item-count={itemCount.value}
                show-quick-jumper
                show-size-picker
                page-sizes={[10, 30, 50]}
                on-update:page={changePage}
                on-update:page-size={changePageSize}
              />
            </NSpace>
          </NSpace>
        </Card>
        <ThirdpartyApiSourceModal
          show={showModal.value}
          data={editData.value}
          operationType={operationType.value}
          onClose={handleModalClose}
          onSubmit={handleModalSubmit}
          onTest={handleModalTest}
        />
      </NSpace>
    )
  }
}) 