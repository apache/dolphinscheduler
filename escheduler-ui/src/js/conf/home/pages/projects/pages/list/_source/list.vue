<template>
  <div class="list-model">
    <div class="table-box">
      <table>
        <tr>
          <th>
            <span>{{$t('编号')}}</span>
          </th>
          <th>
            <span>{{$t('项目名称')}}</span>
          </th>
          <th>
            <span>{{$t('所属用户')}}</span>
          </th>
          <th>
            <span>{{$t('描述')}}</span>
          </th>
          <th>
            <span>{{$t('创建时间')}}</span>
          </th>
          <th>
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
            <span>
              <a href="javascript:" @click="_switchProjects(item)" class="links">{{item.name}}</a>
            </span>
          </td>
          <td>
            <span>{{item.userName || '-'}}</span>
          </td>
          <td>
            <span>{{item.desc}}</span>
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
                        data-toggle="tooltip"
                        :title="$t('删除')"
                        icon="iconfont icon-shanchu"
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
  import '@/module/filter/formatDate'
  import { mapActions, mapMutations } from 'vuex'
  import localStore from '@/module/util/localStorage'
  import { findComponentDownward } from '@/module/util/'

  export default {
    name: 'projects-list',
    data () {
      return {
        list: []
      }
    },
    props: {
      projectsList: Array,
      pageNo: Number,
      pageSize: Number
    },
    methods: {
      ...mapActions('projects', ['deleteProjects']),
      ...mapMutations('dag', ['setProjectName']),
      _switchProjects (item) {
        this.setProjectName(item.name)
        localStore.setItem('projectName', `${item.name}`)
        localStore.setItem('projectId', `${item.id}`)
        this.$router.push({ path: `/projects/index` })
      },
      _closeDelete (i) {
        this.$refs[`poptip-${i}`][0].doClose()
      },
      /**
       * Delete Project
       * @param item Current record
       * @param i index
       */
      _delete (item, i) {
        this.deleteProjects({
          projectId: item.id
        }).then(res => {
          this.$refs[`poptip-${i}`][0].doClose()
          this.list.splice(i, 1)
          this.$message.success(res.msg)
        }).catch(e => {
          this.$refs[`poptip-${i}`][0].doClose()
          this.$message.error(e.msg || '')
        })
      },
      /**
       * edit project
       * @param item Current record
       */
      _edit (item) {
        findComponentDownward(this.$root, 'projects-list')._create(item)
      }
    },
    watch: {
      projectsList (a) {
        this.list = []
        setTimeout(() => {
          this.list = a
        })
      }
    },
    created () {
    },
    mounted () {
      this.list = this.projectsList
    },
    components: { }
  }
</script>