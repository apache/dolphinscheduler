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
  <div class="list-model">
    <div class="table-box">
      <table class="fixed">
        <tr>
          <th scope="col">
            <span>{{$t('#')}}</span>
          </th>
          <th scope="col">
            <span>{{$t('Name')}}</span>
          </th>
          <th scope="col">
            <span>{{$t('Whether directory')}}</span>
          </th>
          <th scope="col">
            <span>{{$t('File Name')}}</span>
          </th>
          <th scope="col">
            <span>{{$t('Description')}}</span>
          </th>
          <th scope="col" width="100">
            <span>{{$t('Size')}}</span>
          </th>
          <th scope="col" width="140">
            <span>{{$t('Update Time')}}</span>
          </th>
          <th scope="col" width="160">
            <span>{{$t('Operation')}}</span>
          </th>
        </tr>
        <tr v-for="(item, $index) in list" :key="item.id">
          <td>
            <span>{{parseInt(pageNo === 1 ? ($index + 1) : (($index + 1) + (pageSize * (pageNo - 1))))}}</span>
          </td>
          <td>
            <span class="ellipsis" v-tooltip.large.top.start.light="{text: item.alias, maxWidth: '500px'}">
              <a href="javascript:" class="links" @click="_go(item)">{{item.alias}}</a>
            </span>
          </td>
          <td>
            <span>{{item.directory? $t('Yes') : $t('No')}}</span>
          </td>
          <td><span class="ellipsis" v-tooltip.large.top.start.light="{text: item.fileName, maxWidth: '500px'}">{{item.fileName}}</span></td>
          <td>
            <span v-if="item.description" class="ellipsis" v-tooltip.large.top.start.light="{text: item.description, maxWidth: '500px'}">{{item.description}}</span>
            <span v-else>-</span>
          </td>
          <td>
            <span>{{_rtSize(item.size)}}</span>
          </td>
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
                    :disabled="_rtDisb(item)"
                    @click="_edit(item,$index)"
                    icon="ans-icon-edit">
            </x-button>

            <x-button
                    type="info"
                    shape="circle"
                    size="xsmall"
                    data-toggle="tooltip"
                    :title="$t('ReUpload File')"
                    :disabled="item.directory? true: false"
                    @click="_reUpload(item)"
                    icon="ans-icon-upload">
            </x-button>

            <x-button
                    type="info"
                    shape="circle"
                    size="xsmall"
                    icon="ans-icon-play"
                    data-toggle="tooltip"
                    :title="$t('Rename')"
                    @click="_rename(item,$index)">
            </x-button>

            <x-button
                    type="info"
                    shape="circle"
                    size="xsmall"
                    data-toggle="tooltip"
                    :title="$t('Download')"
                    :disabled="item.directory? true: false"
                    @click="_downloadFile(item)"
                    icon="ans-icon-download">
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
                        icon="ans-icon-trash"
                        type="error"
                        shape="circle"
                        size="xsmall"
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
  import _ from 'lodash'
  import mRename from './rename'
  import { mapActions } from 'vuex'
  import { filtTypeArr } from '../../_source/common'
  import { bytesToSize } from '@/module/util/util'
  import { findComponentDownward } from '@/module/util'
  import { downloadFile } from '@/module/download'
  import localStore from '@/module/util/localStorage'
  export default {
    name: 'file-manage-list',
    data () {
      return {
        list: [],
        spinnerLoading: false
      }
    },
    props: {
      fileResourcesList: Array,
      pageNo: Number,
      pageSize: Number
    },
    methods: {
      ...mapActions('resource', ['deleteResource']),
      _edit (item) {
        localStore.setItem('file', `${item.alias}|${item.size}`)
        this.$router.push({ path: `/resource/file/edit/${item.id}` })
      },
      _go (item) {
        localStore.setItem('file', `${item.alias}|${item.size}`)
        if(item.directory) {
          localStore.setItem('currentDir', `${item.fullName}`)
          this.$router.push({ path: `/resource/file/subdirectory/${item.id}` })
        } else {
          this.$router.push({ path: `/resource/file/list/${item.id}` })
        }
      },
      _reUpload (item) {
        findComponentDownward(this.$root, 'roof-nav')._fileReUpload('FILE',item)
      },
      _downloadFile (item) {
        downloadFile('/dolphinscheduler/resources/download', {
          id: item.id
        })
      },
      _rtSize (val) {
        return bytesToSize(parseInt(val))
      },
      _closeDelete (i) {
        this.$refs[`poptip-${i}`][0].doClose()
      },
      _delete (item, i) {
        this.spinnerLoading = true
        this.deleteResource({
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
      _rename (item, i) {
        let self = this
        let modal = this.$modal.dialog({
          closable: false,
          showMask: true,
          escClose: true,
          className: 'v-modal-custom',
          transitionName: 'opacityp',
          render (h) {
            return h(mRename, {
              on: {
                onUpDate (item) {
                  self.$set(self.list, i, item)
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
      },
      _rtDisb ({ alias, size }) {
        let i = alias.lastIndexOf('.')
        let a = alias.substring(i, alias.length)
        let flag = _.includes(filtTypeArr, _.trimStart(a, '.'))
        if (flag && (size < 1000000)) {
          flag = true
        } else {
          flag = false
        }
        return !flag
      }
    },
    watch: {
      fileResourcesList (a) {
        this.list = []
        setTimeout(() => {
          this.list = a
        })
      }
    },
    created () {
    },
    mounted () {
      this.list = this.fileResourcesList
    },
    components: { }
  }
</script>