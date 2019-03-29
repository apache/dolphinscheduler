<template>
  <div class="list-model">
    <div class="table-box">
      <table class="fixed">
        <tr>
          <th>
            <span>{{$t('编号')}}</span>
          </th>
          <th>
            <span>{{$t('UDF函数名称')}}</span>
          </th>
          <th>
            <span>{{$t('类名')}}</span>
          </th>
          <th>
            <span>{{$t('参数')}}</span>
          </th>
          <th width="80">
            <span>{{$t('类型')}}</span>
          </th>
          <th>
            <span>{{$t('描述')}}</span>
          </th>
          <th>
            <span>{{$t('jar包')}}</span>
          </th>
          <th>
            <span>{{$t('库名')}}</span>
          </th>
          <th width="140">
            <span>{{$t('更新时间')}}</span>
          </th>
          <th width="80">
            <span>{{$t('操作')}}</span>
          </th>
        </tr>
        <tr v-for="(item, $index) in list" :key="$index">
          <td>
            <span>{{$index + 1}}</span>
          </td>
          <td>
            <span class="ellipsis">
              <a href="javascript:" class="links">{{item.funcName}}</a>
            </span>
          </td>
          <td><span class="ellipsis">{{item.className || '-'}}</span></td>
          <td>
            <span>{{item.argTypes || '-'}}</span>
          </td>
          <td>
            <span>{{item.type}}</span>
          </td>
          <td>
            <span class="ellipsis">{{item.desc || '-'}}</span>
          </td>
          <td>
            <span>{{item.resourceName}}</span>
          </td>
          <td>
            <span>{{item.database || '-'}}</span>
          </td>
          <td>
            <span>{{item.updateTime | formatDate}}</span>
          </td>
          <td>
            <x-button
                    type="info"
                    shape="circle"
                    size="xsmall"
                    data-toggle="tooltip"
                    :title="$t('编辑')"
                    @click="_edit(item)"
                    icon="iconfont icon-bianjixiugai"
                    v-ps="['GENERAL_USER']">
            </x-button>
            <x-poptip
                    :ref="'poptip-' + $index"
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
                        icon="iconfont icon-shanchu"
                        data-toggle="tooltip"
                        :title="$t('删除')"
                        v-ps="['GENERAL_USER']">
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
  import '@/module/filter/formatDate'
  import mCreateUdf from './createUdf'

  export default {
    name: 'udf-manage-list',
    data () {
      return {
        list: []
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
        this.deleteUdf({
          id: item.id
        }).then(res => {
          this.$refs[`poptip-${i}`][0].doClose()
          this.list.splice(i, 1)
          this.$message.success(res.msg)
        }).catch(e => {
          this.$refs[`poptip-${i}`][0].doClose()
          this.$message.error(e.msg || '')
        })
      },
      _edit (item) {
        let self = this
        let modal = this.$modal.dialog({
          closable: false,
          showMask: true,
          escClose: true,
          className: 'v-modal-custom',
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
