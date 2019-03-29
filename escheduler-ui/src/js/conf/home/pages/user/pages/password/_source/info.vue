<template>
  <div class="user-info-model password-model">
    <m-list-box-f>
      <template slot="name">{{$t('用户名称')}}</template>
      <template slot="content">
        <span class="sp1">{{userInfo.userName}}</span>
      </template>
    </m-list-box-f>
    <m-list-box-f>
      <template slot="name">{{$t('密码')}}</template>
      <template slot="content">
        <x-input
                style="width: 320px;"
                type="password"
                v-model="userPassword"
                :placeholder="$t('请输入密码')">
        </x-input>
      </template>
    </m-list-box-f>
    <m-list-box-f>
      <template slot="name">{{$t('确认密码')}}</template>
      <template slot="content">
        <x-input
                style="width: 320px;"
                type="password"
                v-model="oldUserPassword"
                :placeholder="$t('请输入确认密码')">
        </x-input>
      </template>
    </m-list-box-f>
    <m-list-box-f>
      <template slot="name">&nbsp;</template>
      <template slot="content">
        <x-button type="primary" shape="circle" @click="_edit()" :loading="spinnerLoading">{{spinnerLoading ? 'Loading...' : '修改'}}</x-button>
      </template>
    </m-list-box-f>
  </div>
</template>
<script>
  import i18n from '@/module/i18n'
  import { mapState, mapActions } from 'vuex'
  import '@/module/filter/formatDate'
  import mListBoxF from '@/module/components/listBoxF/listBoxF'

  export default {
    name: 'password-info',
    data () {
      return {
        // loading
        spinnerLoading: false,
        // user password
        userPassword: '',
        // Confirm password
        oldUserPassword: ''
      }
    },
    props: {},
    methods: {
      ...mapActions('user', ['signOut']),
      ...mapActions('security', ['updateUser']),
      /**
       * edit
       */
      _edit () {
        // verification
        if (this._verification()) {
          let param = {
            id: this.userInfo.id,
            userName: this.userInfo.userName,
            userPassword: this.userPassword,
            tenantId: this.userInfo.tenantId,
            email: this.userInfo.email,
            phone: this.userInfo.phone
          }
          this.spinnerLoading = true
          this.updateUser(param).then(res => {
            this.$message.success(res.msg)
            setTimeout(() => {
              this.spinnerLoading = false
              this.signOut()
            }, 1500)
          }).catch(e => {
            this.$message.error(e.msg || '')
          })
        }
      },
      /**
       * verification
       */
      _verification () {
        let regCn = new RegExp('[\\u4E00-\\u9FFF]+', 'g')

        // password
        if (regCn.test(this.userPassword)) {
          this.$message.warning(`${i18n.$t('密码不能为中文')}`)
          return false
        }
        if (!this.userPassword) {
          this.$message.warning(`${i18n.$t('请输入密码(6-22)字符密码')}`)
          return false
        }
        if (this.userPassword.length < 6 || this.userPassword.length > 22) {
          this.$message.warning(`${i18n.$t('请输入密码(6-22)字符密码')}`)
          return false
        }

        // confirm password
        if (regCn.test(this.oldUserPassword)) {
          this.$message.warning(`${i18n.$t('确认密码不能为中文')}`)
          return false
        }
        if (!this.oldUserPassword) {
          this.$message.warning(`${i18n.$t('请输入确认密码(6-22)字符密码')}`)
          return false
        }
        if (this.oldUserPassword.length < 6 || this.oldUserPassword.length > 22) {
          this.$message.warning(`${i18n.$t('请输入确认密码(6-22)字符密码')}`)
          return false
        }
        if (this.userPassword !== this.oldUserPassword) {
          this.$message.warning(`${i18n.$t('密码与确认密码不一致,请重新确认')}`)
          return false
        }
        return true
      }
    },
    computed: {
      ...mapState('user', ['userInfo'])
    },
    components: { mListBoxF }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .password-model {
    .list-box-f {
      margin-bottom: 30px;
    }
  }
</style>
