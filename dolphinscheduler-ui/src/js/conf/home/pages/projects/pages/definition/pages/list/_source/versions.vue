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
  <div class="container">

    <div class="title-box">
      <span class="name">{{$t('Version Info')}}</span>
    </div>

    <div class="table-box" v-if="processDefinitionVersions.length > 0">
      <table class="fixed">
        <caption><!-- placeHolder --></caption>
        <tr>
          <th scope="col" style="min-width: 40px;text-align: left">
            <span>{{$t('Version')}}</span>
          </th>
          <th scope="col" style="min-width: 30px">
            <span>{{$t('Description')}}</span>
          </th>
          <th scope="col" style="min-width: 50px">
            <span>{{$t('Create Time')}}</span>
          </th>
          <th scope="col" style="min-width: 300px">
            <span>{{$t('Operation')}}</span>
          </th>
        </tr>
        <tr v-for="(item, $index) in processDefinitionVersions" :key="item.id">
          <td>
            <span v-if="item.version">
              <span v-if="item.version === processDefinition.version" style="color: green"><strong>{{item.version}} {{$t('Current Version')}}</strong></span>
              <span v-else>{{item.version}}</span>
            </span>
            <span v-else>-</span>
          </td>
          <td style="word-break:break-all;">
            <span v-if="item.description">{{item.description}}</span>
            <span v-else>-</span>
          </td>
          <td>
            <span v-if="item.createTime">{{item.createTime}}</span>
            <span v-else>-</span>
          </td>
          <td>
            <x-poptip
              :ref="'poptip-switch-version-' + $index"
              placement="top-end"
              width="260">
              <p>{{$t('Confirm Switch To This Version?')}}</p>
              <div style="text-align: right; margin: 0;padding-top: 4px;">
                <x-button type="text" size="xsmall" shape="circle" @click="_closeSwitchVersion($index)">
                  {{$t('Cancel')}}
                </x-button>
                <x-button type="primary" size="xsmall" shape="circle"
                          @click="_mVersionSwitchProcessDefinitionVersion(item)">{{$t('Confirm')}}
                </x-button>
              </div>
              <template slot="reference">
                <x-button
                  icon="ans-icon-dependence"
                  type="primary"
                  shape="circle"
                  size="xsmall"
                  :disabled="item.version === processDefinition.version || 'ONLINE' === processDefinition.state"
                  data-toggle="tooltip"
                  :title="$t('Switch To This Version')">
                </x-button>
              </template>
            </x-poptip>
            <x-poptip
              :ref="'poptip-delete-' + $index"
              placement="top-end"
              width="90">
              <p>{{$t('Delete?')}}</p>
              <div style="text-align: right; margin: 0;padding-top: 4px;">
                <x-button type="text" size="xsmall" shape="circle" @click="_closeDelete($index)">{{$t('Cancel')}}
                </x-button>
                <x-button type="primary" size="xsmall" shape="circle"
                          @click="_mVersionDeleteProcessDefinitionVersion(item,$index)">{{$t('Confirm')}}
                </x-button>
              </div>
              <template slot="reference">
                <x-button
                  icon="ans-icon-trash"
                  type="error"
                  shape="circle"
                  size="xsmall"
                  :disabled="item.version === processDefinition.version || 'ONLINE' === processDefinition.state"
                  data-toggle="tooltip"
                  :title="$t('delete')">
                </x-button>
              </template>
            </x-poptip>
          </td>
        </tr>
      </table>
    </div>

    <div v-if="processDefinitionVersions.length === 0">
      <m-no-data><!----></m-no-data>
    </div>

    <div v-if="processDefinitionVersions.length > 0">
      <div class="bottom-box">
        <x-button type="text" @click="_close()"> {{$t('Cancel')}}</x-button>
        <x-page :current="pageNo" :total="total" @on-change="_mVersionGetProcessDefinitionVersionsPage" small>
          <!----></x-page>
      </div>
    </div>

  </div>
</template>

<script>
  import mNoData from '@/module/components/noData/noData'

  export default {
    name: 'versions',
    data () {
      return {
        tableHeaders: [
          {
            label: 'version',
            prop: 'version'
          },
          {
            label: 'createTime',
            prop: 'createTime'
          }
        ]
      }
    },
    props: {
      processDefinition: Object,
      processDefinitionVersions: Array,
      total: Number,
      pageNo: Number,
      pageSize: Number
    },
    methods: {
      /**
       * switch version in process definition version list
       */
      _mVersionSwitchProcessDefinitionVersion (item) {
        this.$emit('mVersionSwitchProcessDefinitionVersion', {
          version: item.version,
          processDefinitionId: this.processDefinition.id,
          fromThis: this
        })
      },

      /**
       * delete one version of process definition
       */
      _mVersionDeleteProcessDefinitionVersion (item) {
        this.$emit('mVersionDeleteProcessDefinitionVersion', {
          version: item.version,
          processDefinitionId: this.processDefinition.id,
          fromThis: this
        })
      },

      /**
       * Paging event of process definition versions
       */
      _mVersionGetProcessDefinitionVersionsPage (val) {
        this.$emit('mVersionGetProcessDefinitionVersionsPage', {
          pageNo: val,
          pageSize: this.pageSize,
          processDefinitionId: this.processDefinition.id,
          fromThis: this
        })
      },

      /**
       * Close the switch version layer
       */
      _closeSwitchVersion (i) {
        this.$refs[`poptip-switch-version-${i}`][0].doClose()
      },

      /**
       * Close the delete layer
       */
      _closeDelete (i) {
        this.$refs[`poptip-delete-${i}`][0].doClose()
      },

      /**
       * Close and destroy component and component internal events
       */
      _close () {
        // flag Whether to delete a node this.$destroy()
        this.$emit('close', {
          fromThis: this
        })
      }
    },
    created () {
    },
    mounted () {
    },
    components: { mNoData }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .container {
    width: 500px;
    position: relative;

    .title-box {
      height: 61px;
      border-bottom: 1px solid #DCDEDC;
      position: relative;

      .name {
        position: absolute;
        left: 24px;
        top: 18px;
        font-size: 16px;
      }
    }

    .bottom-box {
      position: absolute;
      bottom: 0;
      left: 0;
      width: 100%;
      text-align: right;
      height: 60px;
      line-height: 60px;
      border-top: 1px solid #DCDEDC;
      background: #fff;

      .ans-page {
        display: inline-block;
      }
    }

    .table-box {
      overflow-y: scroll;
      height: calc(100vh - 61px);
      padding-bottom: 60px;
    }
  }
</style>
