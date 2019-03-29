<template>
  <m-popup
          ref="popup"
          :ok-text="item ? $t('确认编辑') : $t('确认提交')"
          :nameText="item ? $t('编辑用户') : $t('创建用户')"
          @ok="_ok">
    <template slot="content">
      <div class="create-user-model">
        <m-list-box-f>
          <template slot="name"><b>*</b>{{$t('用户名称')}}</template>
          <template slot="content">
            <x-input
                    type="input"
                    v-model="userName"
                    :placeholder="$t('请输入用户名称')">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f v-if="router.history.current.name !== 'account'">
          <template slot="name"><b>*</b>{{$t('密码')}}</template>
          <template slot="content">
            <x-input
                    type="input"
                    v-model="userPassword"
                    :placeholder="$t('请输入密码')">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f v-if="isADMIN">
          <template slot="name"><b>*</b>{{$t('租户')}}</template>
          <template slot="content">
            <x-select v-model="tenantId">
              <x-option
                      v-for="city in tenantList"
                      :key="city.id"
                      :value="city"
                      :label="city.code">
              </x-option>
            </x-select>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><b>*</b>{{$t('邮件')}}</template>
          <template slot="content">
            <x-input
                    type="input"
                    v-model="email"
                    :placeholder="$t('请输入邮件')">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">{{$t('手机')}}</template>
          <template slot="content">
            <x-input
                    type="input"
                    v-model="phone"
                    :placeholder="$t('请输入手机')">
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
  import router from '@/conf/home/router'
  import mPopup from '@/module/components/popup/popup'
  import mListBoxF from '@/module/components/listBoxF/listBoxF'

  export default {
    name: 'create-user',
    data () {
      return {
        store,
        router,
        userName: '',
        userPassword: '',
        tenantId: {},
        email: '',
        phone: '',
        tenantList: [],
        // Source admin user information
        isADMIN: store.state.user.userInfo.userType === 'ADMIN_USER' && router.history.current.name !== 'account'
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
            type: 'user',
            userName: this.userName
          }).then(res => {
            this._submit()
          }).catch(e => {
            this.$message.error(e.msg || '')
          })
        }
      },
      _verification () {
        let regEmail = /^([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+\.[a-zA-Z]{2,3}$/ // eslint-disable-line
        // Mobile phone number regular
        let regPhone = /(^1[3|4|5|7|8]\d{9}$)|(^09\d{8}$)/; // eslint-disable-line

        // user name
        if (!this.userName) {
          this.$message.warning(`${i18n.$t('请输入用户名')}`)
          return false
        }
        // password
        if (!this.userPassword && !this.item) {
          this.$message.warning(`${i18n.$t('请输入密码')}`)
          return false
        }
        // email
        if (!this.email) {
          this.$message.warning(`${i18n.$t('请输入邮箱')}`)
          return false
        }
        // Verify email
        if (!regEmail.test(this.email)) {
          this.$message.warning(`${i18n.$t('请输入正确的邮箱格式')}`)
          return false
        }
        // Verify phone
        if (this.phone) {
          if (!regPhone.test(this.phone)) {
            this.$message.warning(`${i18n.$t('请输入正确的手机格式')}`)
            return false
          }
        }

        return true
      },
      _getTenantList () {
        return new Promise((resolve, reject) => {
          this.store.dispatch('security/getTenantList').then(res => {
            this.tenantList = _.map(res, v => {
              return {
                id: v.id,
                code: v.tenantName
              }
            })
            this.$nextTick(() => {
              this.tenantId = this.tenantList[0]
            })
            resolve()
          })
        })
      },
      _submit () {
        this.$refs['popup'].spinnerLoading = true
        let param = {
          userName: this.userName,
          userPassword: this.userPassword,
          tenantId: this.tenantId.id,
          email: this.email,
          phone: this.phone
        }
        if (this.item) {
          param.id = this.item.id
        }
        this.store.dispatch(`security/${this.item ? 'updateUser' : 'createUser'}`, param).then(res => {
          setTimeout(() => {
            this.$refs['popup'].spinnerLoading = false
          }, 800)
          this.$emit('onUpdate', param)
          this.$message.success(res.msg)
        }).catch(e => {
          this.$message.error(e.msg || '')
          this.$refs['popup'].spinnerLoading = false
        })
      }
    },
    watch: {},
    created () {
      // Administrator gets tenant list
      if (this.isADMIN) {
        this._getTenantList().then(res => {
          if (this.item) {
            this.userName = this.item.userName
            this.userPassword = ''
            this.email = this.item.email
            this.phone = this.item.phone
            this.tenantId = _.filter(this.tenantList, v => v.id === this.item.tenantId)[0]
          }
        })
      } else {
        if (this.item) {
          this.userName = this.item.userName
          this.userPassword = ''
          this.email = this.item.email
          this.phone = this.item.phone
          this.tenantId.id = this.item.tenantId
        }
      }
    },
    mounted () {
    },
    components: { mPopup, mListBoxF }
  }
</script>
