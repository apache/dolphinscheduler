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
v-ps<template>
  <div class="list-model">
    <div class="table-box">
      <table class="fixed">
        <tr>
          <th scope="col">
            <span>{{$t('#')}}</span>
          </th>
          <th scope="col">
            <span>{{$t('UDF Function Name')}}</span>
          </th>
          <th scope="col">
            <span>{{$t('Class Name')}}</span>
          </th>
          <!-- <th scope="col">
            <span>{{$t('Parameter')}}</span>
          </th> -->
          <th scope="col" width="80">
            <span>{{$t('type')}}</span>
          </th>
          <th scope="col">
            <span>{{$t('Description')}}</span>
          </th>
          <th scope="col">
            <span>{{$t('Jar Package')}}</span>
          </th>
          <!-- <th scope="col">
            <span>{{$t('Library Name')}}</span>
          </th> -->
          <th scope="col" width="150">
            <span>{{$t('Update Time')}}</span>
          </th>
          <th scope="col" width="80">
            <span>{{$t('Operation')}}</span>
          </th>
        </tr>
        <tr v-for="(item, $index) in list" :key="$index">
          <td>
            <span>{{$index + 1}}</span>
          </td>
          <td>
            <span class="ellipsis" v-tooltip.large.top.start.light="{text: item.funcName, maxWidth: '500px'}">
              <a href="javascript:" class="links">{{item.funcName}}</a>
            </span>
          </td>
          <td><span class="ellipsis">{{item.className || '-'}}</span></td>
          <!-- <td>
            <span>{{item.argTypes || '-'}}</span>
          </td> -->
          <td>
            <span>{{item.type}}</span>
          </td>
          <td>
            <span v-if="item.description" class="ellipsis" v-tooltip.large.top.start.light="{text: item.description, maxWidth: '500px'}">{{item.description}}</span>
            <span v-else>-</span>
          </td>
          <td>
            <span v-if="item.resourceName" class="ellipsis" v-tooltip.large.top.start.light="{text: item.resourceName, maxWidth: '500px'}">{{item.resourceName}}</span>
            <span v-else>-</span>
          </td>
          <!-- <td>
            <span>{{item.database || '-'}}</span>
          </td> -->
          <td>
            <span v-if="item.updateTime">{{item.updateTime | formatDate}}</span>
            <span v-else>-</span>
          </td>
          <td>
            <x-button
                    type="info"
                    shape="circle"
                    size="xsmall"
                    data-toggle="tooltip"
                    :title="$t('Edit')"
                    @click="_edit(item)"
                    icon="ans-icon-edit">
            </x-button>
            <x-poptip
                    :ref="'poptip-' + $index"
                    placement="bottom-end"
                    width="190">
              <p>{{$t('Delete?')}}</p>
              <div style="text-align: right; margin: 0;padding-top: 4px;">
                <x-button type="text" size="xsmall" shape="circle" @click="_closeDelete($index)">{{$t('Cancel')}}</x-button>
                <x-button type="primary" size="xsmall" shape="circle" :loading="spinnerLoading" @click="_delete(item,$index)">{{spinnerLoading ? 'Loading' : $t('Confirm')}}</x-button>
              </div>
              <template slot="reference">
                <x-button
                        type="error"
                        shape="circle"
                        size="xsmall"
                        icon="ans-icon-trash"
                        data-toggle="tooltip"
                        :title="$t('delete')">
                </x-button>
              </template>
            </x-poptip>
          </td>
        </tr>
      </table>
    </div>
  </div>
</template>
<script>
  import { mapActions } from 'vuex'
  import mCreateUdf from './createUdf'

  export default {
    name: 'udf-manage-list',
    data () {
      return {
        list: [],
        spinnerLoading: false
      }
    },
    props: {
      udfFuncList: Array,
      pageNo: Number,
      pageSize: Number
    },
    methods: {
      ...mapActions('resource', ['deleteUdf']),
      _closeDelete (i) {
        this.$refs[`poptip-${i}`][0].doClose()
      },
      _delete (item, i) {
        this.spinnerLoading = true
        this.deleteUdf({
          id: item.id
        }).then(res => {
          this.$refs[`poptip-${i}`][0].doClose()
          this.$emit('on-update')
          this.$message.success(res.msg)
          this.spinnerLoading = false
        }).catch(e => {
          this.$refs[`poptip-${i}`][0].doClose()
          this.$message.error(e.msg || '')
          this.spinnerLoading = false
        })
      },
      _edit (item) {
        let self = this
        let modal = this.$modal.dialog({
          closable: false,
          showMask: true,
          escClose: true,
          className: 'v-modal-custom',
          width: '800px',
          transitionName: 'opacityp',
          render (h) {
            return h(mCreateUdf, {
              on: {
                onUpdate () {
                  self.$emit('on-update')
                  modal.remove()
                },
                close () {
                  modal.remove()
                }
              },
              props: {
                item: item
              }
            })
          }
        })
      }
    },
    watch: {
      udfFuncList (a) {
        this.list = []
        setTimeout(() => {
          this.list = a
        })
      }
    },
    created () {
    },
    mounted () {
      this.list = this.udfFuncList
    },
    components: { }
  }
</script>
