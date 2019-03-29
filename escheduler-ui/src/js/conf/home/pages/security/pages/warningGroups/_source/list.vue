<template>
  <div class="list-model">
    <div class="table-box">
      <table>
        <tr>
          <th>
            <span>{{$t('编号')}}</span>
          </th>
          <th>
            <span>{{$t('组名称')}}</span>
          </th>
          <th>
            <span>{{$t('组类型')}}</span>
          </th>
          <th>
            <span>{{$t('备注')}}</span>
          </th>
          <th>
            <span>{{$t('创建时间')}}</span>
          </th>
          <th>
            <span>{{$t('更新时间')}}</span>
          </th>
          <th width="120">
            <span>{{$t('操作')}}</span>
          </th>
        </tr>
        <tr v-for="(item, $index) in list" :key="$index">
          <td>
            <span>{{parseInt(pageNo === 1 ? ($index + 1) : (($index + 1) + (pageSize * (pageNo - 1))))}}</span>
          </td>
          <td>
            <span>
              <a href="javascript:" class="links">{{item.groupName}}</a>
            </span>
          </td>
          <td><span>{{item.groupType === 'EMAIL' ? '邮件' : '短信'}}</span></td>
          <td>
            <span>{{item.desc}}</span>
          </td>
          <td>
            <span>{{item.createTime | formatDate}}</span>
          </td>
          <td><span>{{item.updateTime | formatDate}}</span></td>
          <td>
            <x-button type="info" shape="circle" size="xsmall" data-toggle="tooltip" icon="iconfont icon-yonghu1" :title="$t('管理用户')" @click="_mangeUser(item)">
            </x-button>
            <x-button type="info" shape="circle" size="xsmall" data-toggle="tooltip" icon="iconfont icon-bianjixiugai" :title="$t('编辑')" @click="_edit(item)">
            </x-button>
            <x-poptip
                    :ref="'poptip-delete-' + $index"
                    placement="bottom-end"
                    width="90">
              <p>{{$t('确定删除吗?')}}</p>
              <div style="text-align: right; margin: 0;padding-top: 4px;">
                <x-button type="text" size="xsmall" shape="circle" @click="_closeDelete($index)">{{$t('取消')}}</x-button>
                <x-button type="primary" size="xsmall" shape="circle" @click="_delete(item,$index)">{{$t('确定')}}</x-button>
              </div>
              <template slot="reference">
                <x-button type="error" shape="circle" size="xsmall" data-toggle="tooltip" icon="iconfont icon-shanchu" :title="$t('删除')">
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
  import i18n from '@/module/i18n'
  import { mapActions } from 'vuex'
  import '@/module/filter/formatDate'
  import { findComponentDownward } from '@/module/util/'
  import mTransfer from '@/module/components/transfer/transfer'

  export default {
    name: 'user-list',
    data () {
      return {
        list: []
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
          this.list.splice(i, 1)
          this.$message.success(res.msg)
        }).catch(e => {
          this.$refs[`poptip-delete-${i}`][0].doClose()
          this.$message.error(e.msg || '')
        })
      },
      _edit (item) {
        findComponentDownward(this.$root, 'warning-groups-index')._create(item)
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
                    name: `${i18n.$t('管理用户')}`
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