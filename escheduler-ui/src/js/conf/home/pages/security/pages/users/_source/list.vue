<template>
  <div class="list-model user-list-model">
    <div class="table-box">
      <table>
        <tr>
          <th>
            <span>{{$t('编号')}}</span>
          </th>
          <th>
            <span>{{$t('用户名称')}}</span>
          </th>
          <th>
            <span>{{$t('租户')}}</span>
          </th>
          <th>
            <span>{{$t('邮箱')}}</span>
          </th>
          <th>
            <span>{{$t('手机')}}</span>
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
        <tr v-for="(item, $index) in list" :key="item.id">
          <td>
            <span>{{parseInt(pageNo === 1 ? ($index + 1) : (($index + 1) + (pageSize * (pageNo - 1))))}}</span>
          </td>
          <td>
            <span>
              <a href="javascript:" class="links">{{item.userName || '-'}}</a>
            </span>
          </td>
          <td><span>{{item.tenantName || '-'}}</span></td>
          <td>
            <span>{{item.email || '-'}}</span>
          </td>
          <td>
            <span>{{item.phone || '-'}}</span>
          </td>
          <td><span>{{item.createTime | formatDate}}</span></td>
          <td><span>{{item.updateTime | formatDate}}</span></td>
          <td>
            <x-poptip
                    :ref="'poptip-auth-' + $index"
                    popper-class="user-list-poptip"
                    placement="bottom-end">
              <div class="auth-select-box">
                <a href="javascript:" @click="_authProject(item,$index)">{{$t('项目')}}</a>
                <a href="javascript:" @click="_authFile(item,$index)">{{$t('资源')}}</a>
                <a href="javascript:" @click="_authDataSource(item,$index)">{{$t('数据源')}}</a>
                <a href="javascript:" @click="_authUdfFunc(item,$index)">{{$t('UDF函数')}}</a>
              </div>
              <template slot="reference">
                <x-button type="warning" shape="circle" size="xsmall" data-toggle="tooltip" :title="$t('授权')" icon="iconfont icon-yonghu1"></x-button>
              </template>
            </x-poptip>

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
                <x-button
                        type="error"
                        shape="circle"
                        size="xsmall"
                        data-toggle="tooltip"
                        :title="$t('删除')"
                        icon="iconfont icon-shanchu">
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
      userList: Array,
      pageNo: Number,
      pageSize: Number
    },
    methods: {
      ...mapActions('security', ['deleteUser', 'getAuthList', 'grantAuthorization']),
      _closeDelete (i) {
        this.$refs[`poptip-delete-${i}`][0].doClose()
      },
      _delete (item, i) {
        this.deleteUser({
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
        findComponentDownward(this.$root, 'users-index')._create(item)
      },
      _authProject (item, i) {
        this.$refs[`poptip-auth-${i}`][0].doClose()
        this.getAuthList({
          id: item.id,
          type: 'project',
          category: 'projects'
        }).then(data => {
          let sourceListPrs = _.map(data[0], v => {
            return {
              id: v.id,
              name: v.name
            }
          })
          let targetListPrs = _.map(data[1], v => {
            return {
              id: v.id,
              name: v.name
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
                  onUpdate (projectIds) {
                    self._grantAuthorization('users/grant-project', {
                      userId: item.id,
                      projectIds: projectIds
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
                    name: `${i18n.$t('项目')}`
                  }
                }
              })
            }
          })
        })
      },
      _authFile (item, i) {
        this.$refs[`poptip-auth-${i}`][0].doClose()
        this.getAuthList({
          id: item.id,
          type: 'file',
          category: 'resources'
        }).then(data => {
          let sourceListPrs = _.map(data[0], v => {
            return {
              id: v.id,
              name: v.name
            }
          })
          let targetListPrs = _.map(data[1], v => {
            return {
              id: v.id,
              name: v.name
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
                  onUpdate (resourceIds) {
                    self._grantAuthorization('users/grant-file', {
                      userId: item.id,
                      resourceIds: resourceIds
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
                    name: `${i18n.$t('资源')}`
                  }
                }
              })
            }
          })
        })
      },
      _authDataSource (item, i) {
        this.$refs[`poptip-auth-${i}`][0].doClose()
        this.getAuthList({
          id: item.id,
          type: 'datasource',
          category: 'datasources'
        }).then(data => {
          let sourceListPrs = _.map(data[0], v => {
            return {
              id: v.id,
              name: v.name
            }
          })
          let targetListPrs = _.map(data[1], v => {
            return {
              id: v.id,
              name: v.name
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
                  onUpdate (datasourceIds) {
                    self._grantAuthorization('users/grant-datasource', {
                      userId: item.id,
                      datasourceIds: datasourceIds
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
                    name: `${i18n.$t('数据源')}`
                  }
                }
              })
            }
          })
        })
      },
      _authUdfFunc (item, i) {
        this.$refs[`poptip-auth-${i}`][0].doClose()
        this.getAuthList({
          id: item.id,
          type: 'udf-func',
          category: 'resources'
        }).then(data => {
          let sourceListPrs = _.map(data[0], v => {
            return {
              id: v.id,
              name: v.funcName
            }
          })
          let targetListPrs = _.map(data[1], v => {
            return {
              id: v.id,
              name: v.funcName
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
                  onUpdate (udfIds) {
                    self._grantAuthorization('users/grant-udf-func', {
                      userId: item.id,
                      udfIds: udfIds
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
                    name: 'UDF函数'
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
      userList (a) {
        this.list = []
        setTimeout(() => {
          this.list = a
        })
      }
    },
    created () {
      this.list = this.userList
    },
    mounted () {
    },
    components: { }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .user-list-model {
    .user-list-poptip {
      min-width: 90px !important;
      .auth-select-box {
        a {
          font-size: 14px;
          height: 28px;
          line-height: 28px;
          display: block;
        }
      }
    }
  }
</style>
