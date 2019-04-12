<template>
  <m-popup ref="popup" :ok-text="item ? $t('确认编辑') : $t('确认提交')" :nameText="item ? $t('编辑项目') : $t('创建项目')" @ok="_ok">
    <template slot="content">
      <div class="projects-create-model">
        <m-list-box-f>
          <template slot="name"><b>*</b>{{$t('项目名称')}}</template>
          <template slot="content">
            <x-input
                    type="input"
                    v-model="projectName"
                    :placeholder="$t('请输入name')"
                    autocomplete="off">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">{{$t('描述')}}</template>
          <template slot="content">
            <x-input
                    type="textarea"
                    v-model="desc"
                    :placeholder="$t('请输入desc')"
                    autocomplete="off">
            </x-input>
          </template>
        </m-list-box-f>
      </div>
    </template>
  </m-popup>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import store from '@/conf/home/store'
  import mPopup from '@/module/components/popup/popup'
  import mListBoxF from '@/module/components/listBoxF/listBoxF'

  export default {
    name: 'projects-create',
    data () {
      return {
        store,
        desc: '',
        projectName: ''
      }
    },
    props: {
      item: Object
    },
    methods: {
      _ok () {
        if (!this._verification()) {
          return
        }

        let param = {
          projectName: _.trim(this.projectName),
          desc: _.trim(this.desc)
        }

        // edit
        if (this.item) {
          param.projectId = this.item.id
        }

        this.$refs['popup'].spinnerLoading = true

        this.store.dispatch(`projects/${this.item ? 'updateProjects' : 'createProjects'}`, param).then(res => {
          this.$emit('onUpdate')
          this.$message.success(res.msg)
          setTimeout(() => {
            this.$refs['popup'].spinnerLoading = false
          }, 800)
        }).catch(e => {
          this.$message.error(e.msg || '')
          this.$refs['popup'].spinnerLoading = false
        })
      },
      _verification () {
        if (!this.projectName) {
          this.$message.warning(`${i18n.$t('请输入名称')}`)
          return false
        }
        return true
      }
    },
    watch: {},
    created () {
      if (this.item) {
        this.projectName = this.item.name
        this.desc = this.item.desc
      }
    },
    mounted () {
    },
    components: { mPopup, mListBoxF }
  }
</script>
