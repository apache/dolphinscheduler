<template>
  <div class="list-model">
    <div class="table-box">
      <table class="fixed">
        <tr>
          <th>
            <span>{{$t('编号')}}</span>
          </th>
          <th>
            <span>{{$t('数据源名称')}}</span>
          </th>
          <th width="120">
            <span>{{$t('数据源类型')}}</span>
          </th>
          <th width="100">
            <span>{{$t('数据源参数')}}</span>
          </th>
          <th>
            <span>{{$t('描述')}}</span>
          </th>
          <th width="150">
            <span>{{$t('创建时间')}}</span>
          </th>
          <th width="150">
            <span>{{$t('更新时间')}}</span>
          </th>
          <th width="80">
            <span>{{$t('操作')}}</span>
          </th>
        </tr>
        <tr v-for="(item, $index) in list" :key="$index">
          <td>
            <span>{{parseInt(pageNo === 1 ? ($index + 1) : (($index + 1) + (pageSize * (pageNo - 1))))}}</span>
          </td>
          <td>
            <span class="ellipsis">
              <a href="javascript:" class="links">{{item.name}}</a>
            </span>
          </td>
          <td>
            <span>{{item.type}}</span>
          </td>
          <td>
            <m-tooltips-JSON :JSON="JSON.parse(item.connectionParams)" :id="item.id">
              <span slot="reference">
                <a href="javascript:" class="links" style="font-size: 12px;">{{$t('点击查看')}}</a>
              </span>
            </m-tooltips-JSON>
          </td>
          <td><span class="ellipsis">{{item.note}}</span></td>
          <td><span>{{item.createTime | formatDate}}</span></td>
          <td><span>{{item.updateTime | formatDate}}</span></td>
          <td>
            <x-button
                    type="info"
                    shape="circle"
                    size="xsmall"
                    data-toggle="tooltip"
                    v-ps="['GENERAL_USER']"
                    :title="$t('编辑')"
                    icon="iconfont icon-bianjixiugai"
                    @click="_edit(item)">
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
  import { findComponentDownward } from '@/module/util/'
  import mTooltipsJSON from '@/module/components/tooltipsJSON/tooltipsJSON'

  export default {
    name: 'datasource-list',
    data () {
      return {
        // list
        list: []
      }
    },
    props: {
      // External incoming data
      datasourcesList: Array,
      // current page number
      pageNo: Number,
      // Total number of articles
      pageSize: Number
    },
    methods: {
      ...mapActions('datasource', ['deleteDatasource']),
      /**
       * Close delete popup layer
       */
      _closeDelete (i) {
        this.$refs[`poptip-delete-${i}`][0].doClose()
      },
      /**
       * Delete current line
       */
      _delete (item, i) {
        this.$refs[`poptip-delete-${i}`][0].doClose()
        this.deleteDatasource({
          id: item.id
        }).then(res => {
          this.list.splice(i, 1)
          this.$message.success(res.msg)
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      /**
       * edit
       */
      _edit (item) {
        findComponentDownward(this.$root, 'datasource-indexP')._create(item)
      }
    },
    watch: {
      /**
       * Monitor external data changes
       */
      datasourcesList (a) {
        this.list = []
        setTimeout(() => {
          this.list = a
        })
      }
    },
    created () {
      this.list = this.datasourcesList
    },
    mounted () {
    },
    components: { mTooltipsJSON }
  }
</script>
