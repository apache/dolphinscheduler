<template>
  <m-popup
          ref="popup"
          :ok-text="item ? $t('确认编辑') : $t('确认提交')"
          :nameText="item ? $t('编辑队列') : $t('创建队列')"
          @ok="_ok">
    <template slot="content">
      <div class="create-tenement-model">
        <m-list-box-f>
          <template slot="name"><b>*</b>名称</template>
          <template slot="content">
            <x-input
                    type="input"
                    v-model="queueName"
                    placeholder="请输入名称"
                    autocomplete="off">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><b>*</b>队列值</template>
          <template slot="content">
            <x-input
                    type="input"
                    v-model="queue"
                    placeholder="请输入队列值"
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
    name: 'create-tenement',
    data () {
      return {
        store,
        queue:'',
        queueName:''
      }
    },
    props: {
      item: Object
    },
    methods: {
      _ok(){
        if (!this._verification()) {
          return
        }

        let param = {
          queue: _.trim(this.queue),
          queueName: _.trim(this.queueName)
        }
        // edit
        if (this.item) {
          param.id = this.item.id
        }

        let $then = (res) => {
          this.$emit('onUpdate')
          this.$message.success(res.msg)
          setTimeout(() => {
            this.$refs['popup'].spinnerLoading = false
          }, 800)
        }

        let $catch = (e) => {
          this.$message.error(e.msg || '')
          this.$refs['popup'].spinnerLoading = false
        }

        if (this.item) {
          this.$refs['popup'].spinnerLoading = true
          this.store.dispatch(`security/updateQueueQ`, param).then(res => {
            $then(res)
          }).catch(e => {
            $catch(e)
          })
        }else{
          this._verifyName(param).then(() => {
            this.$refs['popup'].spinnerLoading = true
            this.store.dispatch(`security/createQueueQ`, param).then(res => {
              $then(res)
            }).catch(e => {
              $catch(e)
            })
          }).catch(e => {
            this.$message.error(e.msg || '')
          })
        }

      },
      _verification(){
        if (!this.queueName) {
          this.$message.warning(`请输入名称`)
          return false
        }
        if (!this.queue) {
          this.$message.warning(`请输入队列值`)
          return false
        }
        return true
      },
      _verifyName(param){
        return new Promise((resolve, reject) => {
          this.store.dispatch(`security/verifyQueueQ`, param).then(res => {
            resolve()
          }).catch(e => {
            reject(e)
          })
        })
      }
    },
    watch: {
    },
    created () {
      if (this.item) {
        this.queueName = this.item.queueName
        this.queue = this.item.queue
      }
    },
    mounted () {

    },
    components: { mPopup, mListBoxF }
  }
</script>