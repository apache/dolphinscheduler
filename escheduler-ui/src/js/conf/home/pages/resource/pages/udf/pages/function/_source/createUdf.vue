<template>
  <m-popup :ok-text="item ? $t('编辑') : $t('确定提交')" :nameText="item ? $t('编辑UDF函数') : $t('创建UDF函数')" @ok="_ok" ref="popup">
    <template slot="content">
      <div class="udf-create-model">
        <m-list-box-f>
          <template slot="name"><b>*</b>{{$t('类型')}}</template>
          <template slot="content">
            <x-radio-group v-model="type">
              <x-radio :label="'HIVE'">HIVE UDF</x-radio>
              <!--<v-radio :label="'SPARK'">SPARK UDF</v-radio>-->
            </x-radio-group>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><b>*</b>{{$t('UDF函数名称')}}</template>
          <template slot="content">
            <x-input
                    type="input"
                    maxlength="40"
                    v-model="funcName"
                    :placeholder="$t('请输入函数名')">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><b>*</b>{{$t('包名类名')}}</template>
          <template slot="content">
            <x-input
                    type="input"
                    maxlength="40"
                    v-model="className"
                    :placeholder="$t('请输入包名类名')">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">{{$t('参数')}}</template>
          <template slot="content">
            <x-input
                    type="input"
                    v-model="argTypes"
                    :placeholder="$t('请输入参数')">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">{{$t('数据库名')}}</template>
          <template slot="content">
            <x-input
                    type="input"
                    v-model="database"
                    :placeholder="$t('请输入数据库名')">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><b>*</b>{{$t('UDF资源')}}</template>
          <template slot="content">
            <x-select
                    filterable
                    v-model="resourceId"
                    :disabled="isUpdate"
                    style="width: 200px">
              <x-option
                      v-for="city in udfResourceList"
                      :key="city.id"
                      :value="city"
                      :label="city.alias">
              </x-option>
            </x-select>
            <x-button type="primary" @click="_toggleUpdate" :disabled="upDisabled">{{$t('上传资源')}}</x-button>
          </template>
        </m-list-box-f>
        <m-list-box-f v-if="isUpdate">
          <template slot="name">&nbsp;</template>
          <template slot="content">
            <m-udf-update
                    @on-update-present="_onUpdatePresent"
                    @on-update="_onUpdate">
            </m-udf-update>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">{{$t('使用说明')}}</template>
          <template slot="content">
            <x-input
                    type="textarea"
                    v-model="desc"
                    :placeholder="$t('请输入使用说明')">
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
  import mUdfUpdate from '@/module/components/fileUpdate/udfUpdate'

  export default {
    name: 'udf-create',
    data () {
      return {
        store,
        type: 'HIVE',
        funcName: '',
        className: '',
        argTypes: '',
        database: '',
        desc: '',
        resourceId: {},
        udfResourceList: [],
        isUpdate: false,
        upDisabled: false
      }
    },
    props: {
      item: Object
    },
    methods: {
      _ok () {
        this.$refs['popup'].spinnerLoading = true
        if (this._validation()) {
          this._verifyUdfFuncName().then(res => {
            this._createUdfFunc().then()
          }).then(res => {
            setTimeout(() => {
              this.$refs['popup'].spinnerLoading = false
            }, 800)
          }).catch(e => {
            this.$refs['popup'].spinnerLoading = false
          })
        } else {
          this.$refs['popup'].spinnerLoading = false
        }
      },
      _createUdfFunc () {
        return new Promise((resolve, reject) => {
          // parameter
          let param = {
            type: this.type, // HIVE,SPARK
            funcName: this.funcName,
            className: this.className,
            argTypes: this.argTypes, // Can not pass this parameter
            database: this.database, // Can not pass this parameter
            desc: this.desc,
            resourceId: this.resourceId.id
          }

          let id = this.item && this.item.id || null

          // edit
          if (id) {
            param.id = id
          }
          // api
          this.store.dispatch(`resource/${id ? `updateUdfFunc` : `createUdfFunc`}`, param).then(res => {
            this.$emit('onUpdate', param)
            this.$message.success(res.msg)
            resolve()
          }).catch(e => {
            this.$message.error(e.msg || '')
            reject(e)
          })
        })
      },
      _onUpdatePresent () {
        // disabled submit
        this.$refs.popup.apDisabled = true
        // disabled update
        this.upDisabled = true
      },
      /**
       * get udf resources
       */
      _getUdfList () {
        return new Promise((resolve, reject) => {
          this.store.dispatch('resource/getResourcesList', { type: 'UDF' }).then(res => {
            this.udfResourceList = res.data
            resolve()
          })
        })
      },
      /**
       * Upload udf resources
       */
      _onUpdate (o) {
        this.upDisabled = false
        this.udfResourceList.push(o)
        this.isUpdate = false
        this.$nextTick(() => {
          this.resourceId = _.filter(this.udfResourceList, v => v.id === o.id)[0]
        })
        this.$refs.popup.apDisabled = false
      },
      _toggleUpdate () {
        this.isUpdate = !this.isUpdate
        if (this.isUpdate) {
          this.resourceId = null
        }
      },
      /**
       * verification
       */
      _validation () {
        if (!this.funcName) {
          this.$message.warning(`${i18n.$t('请输入UDF函数名称')}`)
          return false
        }
        if (!this.className) {
          this.$message.warning(`${i18n.$t('请输入包名类名')}`)
          return false
        }
        if (!this.resourceId) {
          this.$message.warning(`${i18n.$t('请选择UDF资源')}`)
          return false
        }
        return true
      },
      /**
       * Verify that the function name exists
       */
      _verifyUdfFuncName () {
        return new Promise((resolve, reject) => {
          if (this.item && this.item.funcName === this.funcName) {
            resolve()
          } else {
            this.store.dispatch('resource/verifyUdfFuncName', { name: this.funcName }).then(res => {
              resolve()
            }).catch(e => {
              this.$message.error(e.msg || '')
              reject(e)
            })
          }
        })
      }
    },
    watch: {
    },
    created () {
      this._getUdfList().then(res => {
        // edit
        if (this.item) {
          this.type = this.item.type
          this.funcName = this.item.funcName || ''
          this.className = this.item.className || ''
          this.argTypes = this.item.argTypes || ''
          this.database = this.item.database || ''
          this.desc = this.item.desc || ''
          this.resourceId = _.filter(this.udfResourceList, v => v.id === this.item.resourceId)[0]
        } else {
          this.resourceId = this.udfResourceList.length && this.udfResourceList[0] || []
        }
      })
    },
    mounted () {

    },
    components: { mPopup, mListBoxF, mUdfUpdate }
  }
</script>
