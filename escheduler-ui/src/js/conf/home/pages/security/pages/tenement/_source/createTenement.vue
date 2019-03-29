<template>
  <m-popup
          ref="popup"
          :ok-text="item ? $t('确认编辑') : $t('确认提交')"
          :nameText="item ? $t('编辑租户') : $t('创建租户')"
          @ok="_ok">
    <template slot="content">
      <div class="create-tenement-model">
        <m-list-box-f>
          <template slot="name"><b>*</b>{{$t('租户编码')}}</template>
          <template slot="content">
            <x-input
                    type="input"
                    :disabled="item ? true : false"
                    v-model="tenantCode"
                    :placeholder="$t('请输入name')">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><b>*</b>{{$t('租户名称')}}</template>
          <template slot="content">
            <x-input
                    type="input"
                    v-model="tenantName"
                    :placeholder="$t('请输入name')"
                    autocomplete="off">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><b>*</b>{{$t('队列')}}</template>
          <template slot="content">
            <x-select v-model="queueId">
              <x-option
                      v-for="city in queueList"
                      :key="city.id"
                      :value="city"
                      :label="city.code">
              </x-option>
            </x-select>
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
    name: 'create-tenement',
    data () {
      return {
        store,
        queueList: [],
        queueId: {},
        tenantCode: '',
        tenantName: '',
        desc: ''
      }
    },
    props: {
      item: Object
    },
    methods: {
      _ok () {
        if (this._verification()) {
          // The name is not verified
          if (this.item && this.item.groupName === this.groupName) {
            this._submit()
            return
          }
          // Verify username
          this.store.dispatch(`security/verifyName`, {
            type: 'tenant',
            tenantCode: this.tenantCode
          }).then(res => {
            this._submit()
          }).catch(e => {
            this.$message.error(e.msg || '')
          })
        }
      },
      _getQueueList () {
        return new Promise((resolve, reject) => {
          this.store.dispatch('security/getQueueList').then(res => {
            this.queueList = _.map(res, v => {
              return {
                id: v.id,
                code: v.queueName
              }
            })
            this.$nextTick(() => {
              this.queueId = this.queueList[0]
            })
            resolve()
          })
        })
      },
      _verification () {
        let isEn = /^[A-Za-z]+$/
        if (!this.tenantCode) {
          this.$message.warning(`${i18n.$t('请输入租户编码只允许英文')}`)
          return false
        }
        if (!isEn.test(this.tenantCode)) {
          this.$message.warning(`${i18n.$t('请输入英文租户编码')}`)
          return false
        }
        if (!this.tenantName) {
          this.$message.warning(`${i18n.$t('请输入租户名称')}`)
          return false
        }
        return true
      },
      _submit () {
        // 提交
        let param = {
          tenantCode: this.tenantCode,
          tenantName: this.tenantName,
          queueId: this.queueId.id,
          desc: this.desc
        }
        if (this.item) {
          param.id = this.item.id
        }

        this.$refs['popup'].spinnerLoading = true
        this.store.dispatch(`security/${this.item ? 'updateQueue' : 'createQueue'}`, param).then(res => {
          this.$emit('onUpdate')
          this.$message.success(res.msg)
          setTimeout(() => {
            this.$refs['popup'].spinnerLoading = false
          }, 800)
        }).catch(e => {
          this.$message.error(e.msg || '')
          this.$refs['popup'].spinnerLoading = false
        })
      }
    },
    watch: {
    },
    created () {

    },
    mounted () {
      this._getQueueList().then(res => {
        if (this.item) {
          this.$nextTick(() => {
            this.queueId = _.filter(this.queueList, v => v.id === this.item.queueId)[0]
          })
          this.tenantCode = this.item.tenantCode
          this.tenantName = this.item.tenantName
          this.desc = this.item.desc
        }
      })
    },
    components: { mPopup, mListBoxF }
  }
</script>