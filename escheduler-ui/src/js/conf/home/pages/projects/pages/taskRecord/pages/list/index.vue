<template>
  <div class="main-layout-box">
    <m-secondary-menu :type="'projects'"></m-secondary-menu>
    <m-list-construction :title="$t('Task record')">
      <template slot="conditions">
        <m-conditions>
          <template slot="search-group">
            <div class="list">
              <x-button type="ghost" size="small" @click="_ckQuery" icon="fa fa-search"></x-button>
            </div>
            <div class="list">
              <x-datepicker
                      ref="datepicker"
                      @on-change="_onChangeStartStop"
                      type="daterange"
                      format="YYYY-MM-DD HH:mm:ss"
                      placement="bottom-end"
                      :panelNum="2">
                <x-input slot="input" readonly slot-scope="{value}" :value="value" style="width: 310px;" size="small" :placeholder="$t('Select date range')">
                  <i slot="suffix"
                     @click.stop="_dateEmpty()"
                     class="ans-icon-fail-solid"
                     v-show="value"
                     style="font-size: 13px;cursor: pointer;margin-top: 1px;">
                  </i>
                </x-input>
              </x-datepicker>
            </div>
            <div class="list">
              <x-input v-model="destTable" style="width: 120px;" size="small" :placeholder="$t('Target Table')"></x-input>
            </div>
            <div class="list">
              <x-input v-model="sourceTable" style="width: 120px;" size="small" :placeholder="$t('Source Table')"></x-input>
            </div>
            <div class="list">
              <x-select style="width: 90px;" @on-change="_onChangeState">
                <x-input slot="trigger" readonly :value="selectedModel ? selectedModel.label : ''" slot-scope="{ selectedModel }" style="width: 90px;" size="small" :placeholder="$t('State')" suffix-icon="ans-icon-arrow-down"></x-input>
                <x-option
                        v-for="city in stateList"
                        :key="city.label"
                        :value="city.code"
                        :label="city.label">
                </x-option>
              </x-select>
            </div>
            <div class="list">
              <x-datepicker
                      @on-change="_onChangeDate"
                      format="YYYY-MM-DD"
                      :panelNum="1">
                <x-input slot="input" readonly slot-scope="{value}" style="width: 130px;" :value="value" size="small" :placeholder="$t('Date')"></x-input>
              </x-datepicker>
            </div>
            <div class="list">
              <x-input v-model="taskName" style="width: 130px;" size="small" :placeholder="$t('Task Name')"></x-input>
            </div>
          </template>
        </m-conditions>
      </template>
      <template slot="content">
        <template v-if="taskRecordList.length">
          <m-list :task-record-list="taskRecordList" @on-update="_onUpdate" :page-no="pageNo" :page-size="pageSize">
          </m-list>
          <div class="page-box">
            <x-page :current="pageNo" :total="total" show-elevator @on-change="_page"></x-page>
          </div>
        </template>
        <template v-if="!taskRecordList.length">
          <m-no-data></m-no-data>
        </template>
        <m-spin :is-spin="isLoading"></m-spin>
      </template>
    </m-list-construction>
  </div>
</template>
<script>
  import i18n from '@/module/i18n'
  import { mapActions } from 'vuex'
  import mList from './_source/list'
  import mSpin from '@/module/components/spin/spin'
  import mNoData from '@/module/components/noData/noData'
  import mConditions from '@/module/components/conditions/conditions'
  import mSecondaryMenu from '@/module/components/secondaryMenu/secondaryMenu'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'task-record-list-index',
    data () {
      return {
        pageSize: 10,
        pageNo: 1,
        total: null,
        taskRecordList: [],
        isLoading: true,
        taskName: '',
        state: '',
        sourceTable: '',
        destTable: '',
        taskDate: '',
        startDate: '',
        endDate: '',
        stateList: [
          {
            label: `${i18n.$t('none')}`,
            code: ``
          },
          {
            label: `${i18n.$t('success')}`,
            code: `${i18n.$t('success')}`
          },
          {
            label: `${i18n.$t('waiting')}`,
            code: `${i18n.$t('waiting')}`
          },
          {
            label: `${i18n.$t('In Execution')}`,
            code: `${i18n.$t('In Execution')}`
          },
          {
            label: `${i18n.$t('Finish')}`,
            code: `${i18n.$t('Finish')}`
          }, {
            label: `${i18n.$t('failed')}`,
            code: `${i18n.$t('failed')}`
          }
        ]
      }
    },
    props: {},
    methods: {
      ...mapActions('dag', ['getTaskRecordList']),
      /**
       * empty date
       */
      _dateEmpty () {
        this.startDate = ''
        this.endDate = ''
        this.$refs.datepicker.empty()
      },
      _ckQuery () {
        this._getTaskRecordList()
      },
      _onChangeState (val) {
        this.state = val.value
      },
      _onChangeStartStop (val) {
        this.startDate = val[0]
        this.endDate = val[1]
      },
      _onChangeDate (val) {
        this.taskDate = val.replace(/-/g, '')
      },
      _page (val) {
        this.pageNo = val
        this._getTaskRecordList()
      },
      /**
       * get list data
       */
      _getTaskRecordList (flag) {
        this.isLoading = !flag
        let param = {
          pageSize: this.pageSize,
          pageNo: this.pageNo,
          taskName: this.taskName,
          state: this.state,
          sourceTable: this.sourceTable,
          destTable: this.destTable,
          taskDate: this.taskDate,
          startDate: this.startDate,
          endDate: this.endDate
        }
        this.taskRecordList = []
        this.getTaskRecordList(param).then(res => {
          this.taskRecordList = res.totalList
          this.total = res.total
          this.isLoading = false
        }).catch(e => {
          this.isLoading = false
        })
      },
      _onUpdate () {
        this._getTaskRecordList('false')
      }
    },
    watch: {
    },
    created () {
    },
    mounted () {
      this._getTaskRecordList()
    },
    components: { mList, mConditions, mSpin, mListConstruction, mSecondaryMenu, mNoData }
  }
</script>
