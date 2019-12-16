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
<template>
  <m-list-construction :title="searchParams.projectId ? $t('Project Home') : $t('Home')">
    <template slot="content">
      <div class="perject-home-content">
        <div class="time-model">
          <x-datepicker
                  :panel-num="2"
                  placement="bottom-end"
                  @on-change="_datepicker"
                  :value="[searchParams.startDate,searchParams.endDate]"
                  type="daterange"
                  :placeholder="$t('Select date range')"
                  format="YYYY-MM-DD HH:mm:ss">
          </x-datepicker>
        </div>
        <div class="row" >
          <div class="col-md-6">
            <div class="chart-title">
              <span>{{$t('Task status statistics')}}</span>
            </div>
            <div class="row">
              <m-task-ctatus-count :search-params="searchParams">
              </m-task-ctatus-count>
            </div>
          </div>
          <div class="col-md-6">
            <div class="chart-title">
              <span>{{$t('Process Status Statistics')}}</span>
            </div>
            <div class="row">
              <m-process-state-count :search-params="searchParams">
              </m-process-state-count>
            </div>
          </div>
        </div>
        <div class="row">
          <div class="col-md-12">
            <div class="chart-title" style="margin-bottom: -20px;margin-top: 30px">
              <span>{{$t('Process Definition Statistics')}}</span>
            </div>
            <div>
              <m-define-user-count :project-id="searchParams.projectId">
              </m-define-user-count>
            </div>
          </div>
        </div>
      </div>
    </template>
  </m-list-construction>
</template>
<script>
  import dayjs from 'dayjs'
  import mDefineUserCount from './_source/defineUserCount'
  import mCommandStateCount from './_source/commandStateCount'
  import mTaskCtatusCount from './_source/taskCtatusCount'
  import mProcessStateCount from './_source/processStateCount'
  import mQueueCount from './_source/queueCount'
  import localStore from '@/module/util/localStorage'
  import mSecondaryMenu from '@/module/components/secondaryMenu/secondaryMenu'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'projects-index-index',
    data () {
      return {
        searchParams: {
          projectId: null,
          startDate: '',
          endDate: ''
        }
      }
    },
    props: {
      id: Number
    },
    methods: {
      _datepicker (val) {
        this.searchParams.startDate = val[0]
        this.searchParams.endDate = val[1]
      }
    },
    created () {
      this.searchParams.projectId = this.id === 0 ? 0 : localStore.getItem('projectId')
      this.searchParams.startDate = dayjs().format('YYYY-MM-DD 00:00:00')
      this.searchParams.endDate = dayjs().format('YYYY-MM-DD HH:mm:ss')
    },
    components: {
      mSecondaryMenu,
      mListConstruction,
      mDefineUserCount,
      mCommandStateCount,
      mTaskCtatusCount,
      mProcessStateCount,
      mQueueCount
    }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .perject-home-content {
    padding: 10px 20px;
    position: relative;
    .time-model {
      position: absolute;
      right: 8px;
      top: -40px;
      .ans-input {
        >input {
          width: 344px;
        }
      }
    }
    .chart-title {
      text-align: center;
      height: 60px;
      line-height: 60px;
      span {
        font-size: 22px;
        color: #333;
        font-weight: bold;
      }
    }
  }
  .table-small-model {
    .ellipsis {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space:  nowrap;
      display: block;
    }
  }
</style>
