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
    <div>
      <form-create v-model="fApi" :rule="rule" :option="option"></form-create>
    </div>
    <div class="table-box">
      <table>
        <tr>
          <th>
            <span>{{$t('#')}}</span>
          </th>
          <th>
            <span>{{$t('Group Name')}}</span>
          </th>
          <th>
            <span>{{$t('Group Type')}}</span>
          </th>
          <th>
            <span>{{$t('Remarks')}}</span>
          </th>
          <th>
            <span>{{$t('Create Time')}}</span>
          </th>
          <th>
            <span>{{$t('Update Time')}}</span>
          </th>
          <th width="120">
            <span>{{$t('Operation')}}</span>
          </th>
        </tr>
        <tr v-for="(item, $index) in list" :key="$index">
          <td>
            <span>{{parseInt(pageNo === 1 ? ($index + 1) : (($index + 1) + (pageSize * (pageNo - 1))))}}</span>
          </td>
          <td>
            <span>
              {{item.groupName}}
            </span>
          </td>
          <td><span>{{item.groupType === 'EMAIL' ? `${$t('Email')}` : `${$t('SMS')}`}}</span></td>
          <td>
            <span v-if="item.description" class="ellipsis" v-tooltip.large.top.start.light="{text: item.description, maxWidth: '500px'}">{{item.description}}</span>
            <span v-else>-</span>
          </td>
          <td>
            <span v-if="item.createTime">{{item.createTime | formatDate}}</span>
            <span v-else>-</span>
          </td>
          <td>
            <span v-if="item.updateTime">{{item.updateTime | formatDate}}</span>
            <span v-else>-</span>
          </td>
          <td>
            <x-button type="info" shape="circle" size="xsmall" data-toggle="tooltip" icon="ans-icon-user-empty" :title="$t('Managing Users')" @click="_mangeUser(item)">
            </x-button>
            <x-button type="info" shape="circle" size="xsmall" data-toggle="tooltip" icon="ans-icon-edit" :title="$t('Edit')" @click="_edit(item)">
            </x-button>
            <x-poptip
                    :ref="'poptip-delete-' + $index"
                    placement="bottom-end"
                    width="90">
              <p>{{$t('Delete?')}}</p>
              <div style="text-align: right; margin: 0;padding-top: 4px;">
                <x-button type="text" size="xsmall" shape="circle" @click="_closeDelete($index)">{{$t('Cancel')}}</x-button>
                <x-button type="primary" size="xsmall" shape="circle" @click="_delete(item,$index)">{{$t('Confirm')}}</x-button>
              </div>
              <template slot="reference">
                <x-button type="error" shape="circle" size="xsmall" data-toggle="tooltip" icon="ans-icon-trash" :title="$t('delete')" :disabled="item.id==1?true: false"></x-button>
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
  import i18n from '@/module/i18n'
  import { mapActions } from 'vuex'
  import mTransfer from '@/module/components/transfer/transfer'

  export default {
    name: 'user-list',
    data () {
      return {
        list: [],
        fApi: {},
        rule: [
          {
            type: "input", // 生成组件的名称(就是表单的名称：如input，radio，checkbox，select，slider等)
            field: "userName", // 表单组件的字段名称(就是表单的name属性，注：该必须唯一),自定义组件可以不配置
            className: "user-name-dom", // 设置组件的class属性
            title: "用户名称：", // 组件的名称, 选填
            value: "", // 表单组件的字段值(就是表单的value值),自定义组件可以不用设置
            props: {
              placeholder: "请输入用户名称！",
              disabled: false,
              readonly: false,
              clearable: true // 是否显示清空按钮
            },
            validate: [
              {
                trigger: "blur",
                required: true,
                message: "用户名称不能为空！"
              }
            ],
            col: {
              md: { span: 12 }
            }
          }
        ],
        option: {
          // 显示重置表单按扭
          resetBtn: true,
  
          // 表单提交按扭事件
          onSubmit: formData => {
            alert(JSON.stringify(formData));
  
            console.log("获取表单中的数据：", formData);
  
            //按钮进入提交状态
            //   this.fApi.btn.loading();
  
            //重置按钮禁用
            //   this.fApi.resetBtn.disabled();
  
            //按钮进入可点击状态
            //   this.fApi.btn.finish();
          }
        }
      }
    },
    props: {
      alertgroupList: Array,
      pageNo: Number,
      pageSize: Number
    },
    methods: {
      ...mapActions('security', ['deleteAlertgrou', 'getAuthList', 'grantAuthorization']),
      _closeDelete (i) {
        this.$refs[`poptip-delete-${i}`][0].doClose()
      },
      _delete (item, i) {
        this.deleteAlertgrou({
          id: item.id
        }).then(res => {
          this.$refs[`poptip-delete-${i}`][0].doClose()
          this.$emit('on-update')
          this.$message.success(res.msg)
        }).catch(e => {
          this.$refs[`poptip-delete-${i}`][0].doClose()
          this.$message.error(e.msg || '')
        })
      },
      _edit (item) {
        this.$emit('on-edit', item)
      },
      _mangeUser (item, i) {
        this.getAuthList({
          id: item.id,
          type: 'user',
          category: 'users'
        }).then(data => {
          let sourceListPrs = _.map(data[0], v => {
            return {
              id: v.id,
              name: v.userName
            }
          })
          let targetListPrs = _.map(data[1], v => {
            return {
              id: v.id,
              name: v.userName
            }
          })
          let self = this
          let modal = this.$modal.dialog({
            closable: false,
            showMask: true,
            escClose: true,
            className: 'v-modal-custom',
            transitionName: 'opacityp',
            render (h) {
              return h(mTransfer, {
                on: {
                  onUpdate (userIds) {
                    self._grantAuthorization('alert-group/grant-user', {
                      userIds: userIds,
                      alertgroupId: item.id
                    })
                    modal.remove()
                  },
                  close () {
                    modal.remove()
                  }
                },
                props: {
                  sourceListPrs: sourceListPrs,
                  targetListPrs: targetListPrs,
                  type: {
                    name: `${i18n.$t('Managing Users')}`
                  }
                }
              })
            }
          })
        })
      },
      _grantAuthorization (api, param) {
        this.grantAuthorization({
          api: api,
          param: param
        }).then(res => {
          this.$message.success(res.msg)
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      }
    },
    watch: {
      alertgroupList (a) {
        this.list = []
        setTimeout(() => {
          this.list = a
        })
      }
    },
    created () {
      this.list = this.alertgroupList
    },
    mounted () {
    },
    components: { }
  }
</script>