<template>
  <div class="list-model">
    <div class="table-box">
      <table>
        <tr>
          <th>
            <span>{{$t('编号')}}</span>
          </th>
          <th>
            <span>{{$t('租户编码')}}</span>
          </th>
          <th>
            <span>{{$t('租户名称')}}</span>
          </th>
          <th>
            <span>{{$t('描述')}}</span>
          </th>
          <th>
            <span>{{$t('队列')}}</span>
          </th>
          <th>
            <span>{{$t('创建时间')}}</span>
          </th>
          <th>
            <span>{{$t('更新时间')}}</span>
          </th>
          <th width="70">
            <span>{{$t('操作')}}</span>
          </th>
        </tr>
        <tr v-for="(item, $index) in list" :key="$index">
          <td>
            <span>{{parseInt(pageNo === 1 ? ($index + 1) : (($index + 1) + (pageSize * (pageNo - 1))))}}</span>
          </td>
          <td>
            <span>
              <a href="javascript:" class="links">
                {{item.tenantCode}}
              </a>
            </span>
          </td>
          <td>
            <span>
              <a href="javascript:" class="links">
                {{item.tenantName}}
              </a>
            </span>
          </td>
          <td>
            <span>{{item.desc}}</span>
          </td>
          <td>
            <span>{{item.queueName}}</span>
          </td>
          <td><span>{{item.createTime | formatDate}}</span></td>
          <td><span>{{item.updateTime | formatDate}}</span></td>
          <td>
            <x-button
                    type="info"
                    shape="circle"
                    size="xsmall"
                    data-toggle="tooltip"
                    :title="$t('编辑')"
                    @click="_edit(item)"
                    icon="iconfont icon-bianjixiugai">
            </x-button>
            <!--<x-poptip
                    :ref="'poptip-' + $index"
                    placement="bottom-end"
                    width="90">
              <p>{{$t('确定删除吗?')}}</p>
              <div style="text-align: right; margin: 0;padding-top: 4px;">
                <x-button type="text" size="xsmall" shape="circle" @click="_closeDelete($index)">{{$t('取消')}}</x-button>
                <x-button type="primary" size="xsmall" shape="circle" @click="_delete(item,$index)">{{$t('确定')}}</x-button>
              </div>
              <template slot="reference">
                <x-button type="error" shape="circle" size="xsmall" data-toggle="tooltip" :title="$t('删除')">{{$t('删除')}}</x-button>
              </template>
            </x-poptip>-->
          </td>
        </tr>
      </table>
    </div>
  </div>
</template>
<script>
  import { mapActions } from 'vuex'
  import '@/module/filter/formatDate'
  import { findComponentDownward } from '@/module/util/'

  export default {
    name: 'tenement-list',
    data () {
      return {
        list: []
      }
    },
    props: {
      tenementList: Array,
      pageNo: Number,
      pageSize: Number
    },
    methods: {
      ...mapActions('security', ['deleteQueue']),
      _closeDelete (i) {
        this.$refs[`poptip-${i}`][0].doClose()
      },
      _delete (item, i) {
        this.deleteQueue({
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
        findComponentDownward(this.$root, 'tenement-index')._create(item)
      }
    },
    watch: {
      tenementList (a) {
        this.list = []
        setTimeout(() => {
          this.list = a
        })
      }
    },
    created () {
      this.list = this.tenementList
    },
    mounted () {
    },
    components: { }
  }
</script>
