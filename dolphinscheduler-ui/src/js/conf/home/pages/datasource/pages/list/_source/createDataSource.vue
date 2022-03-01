/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
<template>
  <div class="datasource-popup-model">
    <div class="content-p">
      <div class="create-datasource-model">
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Datasource')}}</template>
          <template slot="content" size="small">
              <el-select id="btnDataSourceTypeDropDown" style="width: 100%;" v-model="type" :disabled="this.item.id">
                <el-option
                      class="options-datasource-type"
                      v-for="item in datasourceTypeList"
                      :key="item.value"
                      :value="item.value"
                      :label="item.label">
                </el-option>
              </el-select>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Datasource Name')}}</template>
          <template slot="content">
            <el-input
                    id="inputDataSourceName"
                    type="input"
                    v-model="name"
                    maxlength="60"
                    size="small"
                    :placeholder="$t('Please enter datasource name')">
            </el-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">{{$t('Description')}}</template>
          <template slot="content">
            <el-input
                    id="inputDataSourceDescription"
                    type="textarea"
                    v-model="note"
                    size="small"
                    :placeholder="$t('Please enter description')">
            </el-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('IP')}}</template>
          <template slot="content">
            <el-input
                    id="inputIP"
                    type="input"
                    v-model="host"
                    maxlength="255"
                    size="small"
                    :placeholder="$t('Please enter IP')">
            </el-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Port')}}</template>
          <template slot="content">
            <el-input
                    id="inputPort"
                    type="input"
                    v-model="port"
                    size="small"
                    :placeholder="$t('Please enter port')">
            </el-input>
          </template>
        </m-list-box-f>
        <m-list-box-f :class="{hidden:showPrincipal}">
          <template slot="name"><strong>*</strong>Principal</template>
          <template slot="content">
            <el-input
              type="input"
              v-model="principal"
              size="small"
              :placeholder="$t('Please enter Principal')">
            </el-input>
          </template>
        </m-list-box-f>
        <m-list-box-f :class="{hidden:showPrincipal}">
          <template slot="name">krb5.conf</template>
          <template slot="content">
            <el-input
              type="input"
              v-model="javaSecurityKrb5Conf"
              size="small"
              :placeholder="$t('Please enter the kerberos authentication parameter java.security.krb5.conf')">
            </el-input>
          </template>
        </m-list-box-f>
        <m-list-box-f :class="{hidden:showPrincipal}">
          <template slot="name">keytab.username</template>
          <template slot="content">
            <el-input
              type="input"
              v-model="loginUserKeytabUsername"
              size="small"
              :placeholder="$t('Please enter the kerberos authentication parameter login.user.keytab.username')">
            </el-input>
          </template>
        </m-list-box-f>
        <m-list-box-f :class="{hidden:showPrincipal}">
          <template slot="name">keytab.path</template>
          <template slot="content">
            <el-input
              type="input"
              v-model="loginUserKeytabPath"
              size="small"
              :placeholder="$t('Please enter the kerberos authentication parameter login.user.keytab.path')">
            </el-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('User Name')}}</template>
          <template slot="content">
            <el-input
                    id="inputUserName"
                    type="input"
                    v-model="userName"
                    maxlength="60"
                    size="small"
                    :placeholder="$t('Please enter user name')">
            </el-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">{{$t('Password')}}</template>
          <template slot="content">
            <el-input
                    id="inputPassword"
                    type="password"
                    v-model="password"
                    size="small"
                    :placeholder="$t('Please enter your password')">
            </el-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><strong :class="{hidden:showDatabase}">*</strong>{{$t('Database Name')}}</template>
          <template slot="content">
            <el-input
                    id="inputDataBase"
                    type="input"
                    v-model="database"
                    maxlength="60"
                    size="small"
                    :placeholder="$t('Please enter database name')">
            </el-input>
          </template>
        </m-list-box-f>
        <m-list-box-f v-if="showConnectType">
          <template slot="name"><strong>*</strong>{{$t('Oracle Connect Type')}}</template>
          <template slot="content">
            <el-radio-group v-model="connectType" size="small" style="vertical-align: sub;">
              <el-radio :label="'ORACLE_SERVICE_NAME'">{{$t('Oracle Service Name')}}</el-radio>
              <el-radio :label="'ORACLE_SID'">{{$t('Oracle SID')}}</el-radio>
            </el-radio-group>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">{{$t('jdbc connect parameters')}}</template>
          <template slot="content">
            <el-input
                    id="inputJdbcParams"
                    type="textarea"
                    v-model="other"
                    :autosize="{minRows:2}"
                    size="small"
                    :placeholder="_rtOtherPlaceholder()">
            </el-input>
          </template>
        </m-list-box-f>
      </div>
    </div>
    <div class="bottom-p">
      <el-button id="btnCancel" type="text" ize="mini" @click="_close()"> {{$t('Cancel')}} </el-button>
      <el-button id="btnTestConnection" type="success" size="mini" round @click="_testConnect()" :loading="testLoading">{{testLoading ? $t('Loading...') : $t('Test Connect')}}</el-button>
      <el-button id="btnSubmit" type="primary" size="mini" round :loading="spinnerLoading" @click="_ok()">{{spinnerLoading ? $t('Loading...') :item ? `${$t('Edit')}` : `${$t('Submit')}`}} </el-button>
    </div>
  </div>
</template>
<script>
  import i18n from '@/module/i18n'
  import store from '@/conf/home/store'
  import { isJson } from '@/module/util/util'
  import mListBoxF from '@/module/components/listBoxF/listBoxF'

  export default {
    name: 'create-datasource',
    data () {
      return {
        store,
        // btn loading
        spinnerLoading: false,
        // Data source type
        type: 'MYSQL',
        // name
        name: '',
        // description
        note: '',
        // host
        host: '',
        // port
        port: '',
        // data storage name
        database: '',
        // principal
        principal: '',
        // java.security.krb5.conf
        javaSecurityKrb5Conf: '',
        // login.user.keytab.username
        loginUserKeytabUsername: '',
        // login.user.keytab.path
        loginUserKeytabPath: '',
        // database username
        userName: '',
        // Database password
        password: '',
        // Database connect type
        connectType: '',
        // Jdbc connection parameter
        other: '',
        // btn test loading
        testLoading: false,
        showPrincipal: true,
        showDatabase: false,
        showConnectType: false,
        isShowPrincipal: true,
        prePortMapper: {},
        datasourceTypeList: [
          {
            value: 'MYSQL',
            label: 'MYSQL'
          },
          {
            value: 'POSTGRESQL',
            label: 'POSTGRESQL'
          },
          {
            value: 'HIVE',
            label: 'HIVE/IMPALA'
          },
          {
            value: 'SPARK',
            label: 'SPARK'
          },
          {
            value: 'CLICKHOUSE',
            label: 'CLICKHOUSE'
          },
          {
            value: 'ORACLE',
            label: 'ORACLE'
          },
          {
            value: 'SQLSERVER',
            label: 'SQLSERVER'
          },
          {
            value: 'DB2',
            label: 'DB2'
          },
          {
            value: 'PRESTO',
            label: 'PRESTO'
          },
          {
            value: 'REDSHIFT',
            label: 'REDSHIFT'
          }
        ]
      }
    },
    props: {
      item: Object
    },

    methods: {
      _rtOtherPlaceholder () {
        return `${i18n.$t('Please enter format')} {"key1":"value1","key2":"value2"...} ${i18n.$t('connection parameter')}`
      },
      /**
       * submit
       */
      _ok () {
        if (this._verification()) {
          this._verifName().then(res => {
            this._submit()
          })
        }
      },
      /**
       * close
       */
      _close () {
        this.$emit('close')
      },
      /**
       * return param
       */
      _rtParam () {
        return {
          type: this.type,
          name: this.name,
          note: this.note,
          host: this.host,
          port: this.port,
          database: this.database,
          principal: this.principal,
          javaSecurityKrb5Conf: this.javaSecurityKrb5Conf,
          loginUserKeytabUsername: this.loginUserKeytabUsername,
          loginUserKeytabPath: this.loginUserKeytabPath,
          userName: this.userName,
          password: this.password,
          connectType: this.connectType,
          other: this.other === '' ? null : JSON.parse(this.other)
        }
      },
      /**
       * test connect
       */
      _testConnect () {
        if (this._verification()) {
          this.testLoading = true
          this.store.dispatch('datasource/connectDatasources', this._rtParam()).then(res => {
            setTimeout(() => {
              this.$message.success(res.msg)
              this.testLoading = false
            }, 800)
          }).catch(e => {
            this.$message.error(e.msg || '')
            this.testLoading = false
          })
        }
      },
      /**
       * Verify that the data source name exists
       */
      _verifName () {
        return new Promise((resolve, reject) => {
          if (this.name === this.item.name) {
            resolve()
            return
          }
          this.store.dispatch('datasource/verifyName', { name: this.name }).then(res => {
            resolve()
          }).catch(e => {
            this.$message.error(e.msg || '')
            reject(e)
          })
        })
      },
      /**
       * verification
       */
      _verification () {
        if (!this.name) {
          this.$message.warning(`${i18n.$t('Please enter resource name')}`)
          return false
        }
        if (!this.host) {
          this.$message.warning(`${i18n.$t('Please enter IP/hostname')}`)
          return false
        }
        if (!this.port) {
          this.$message.warning(`${i18n.$t('Please enter port')}`)
          return false
        }
        if (!this.userName) {
          this.$message.warning(`${i18n.$t('Please enter user name')}`)
          return false
        }

        if (!this.database && this.showDatabase === false) {
          this.$message.warning(`${i18n.$t('Please enter database name')}`)
          return false
        }
        if (this.other) {
          if (!isJson(this.other)) {
            this.$message.warning(`${i18n.$t('jdbc connection parameters is not a correct JSON format')}`)
            return false
          }
        }
        return true
      },
      /**
       * submit => add/update
       */
      _submit () {
        this.spinnerLoading = true
        let param = this._rtParam()
        // edit
        if (this.item) {
          param.id = this.item.id
        }
        this.store.dispatch(`datasource/${this.item ? 'updateDatasource' : 'createDatasources'}`, param).then(res => {
          this.$message.success(res.msg)
          this.spinnerLoading = false
          this.$emit('onUpdate')
        }).catch(e => {
          this.$message.error(e.msg || '')
          this.spinnerLoading = false
        })
      },
      /**
       * Get modified data
       */
      _getEditDatasource () {
        this.store.dispatch('datasource/getEditDatasource', { id: this.item.id }).then(res => {
          this.type = res.type
          this.name = res.name
          this.note = res.note
          this.host = res.host

          // When in Editpage, Prevent default value overwrite backfill value
          setTimeout(() => {
            this.port = res.port
          }, 0)

          this.principal = res.principal
          this.javaSecurityKrb5Conf = res.javaSecurityKrb5Conf
          this.loginUserKeytabUsername = res.loginUserKeytabUsername
          this.loginUserKeytabPath = res.loginUserKeytabPath
          this.database = res.database
          this.userName = res.userName
          this.password = res.password
          this.connectType = res.connectType
          this.other = res.other === null ? '' : JSON.stringify(res.other)
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      /**
       * Set default port for each type.
       */
      _setDefaultValues (value) {
        // Default type is MYSQL
        let type = this.type || 'MYSQL'

        let defaultPort = this._getDefaultPort(type)

        // Backfill the previous input from memcache
        let mapperPort = this.prePortMapper[type]

        this.port = mapperPort || defaultPort
      },

      /**
       * Get default port by type
       */
      _getDefaultPort (type) {
        let defaultPort = ''
        switch (type) {
          case 'MYSQL':
            defaultPort = '3306'
            break
          case 'POSTGRESQL':
            defaultPort = '5432'
            break
          case 'HIVE':
            defaultPort = '10000'
            break
          case 'SPARK':
            defaultPort = '10015'
            break
          case 'CLICKHOUSE':
            defaultPort = '8123'
            break
          case 'ORACLE':
            defaultPort = '1521'
            break
          case 'SQLSERVER':
            defaultPort = '1433'
            break
          case 'DB2':
            defaultPort = '50000'
            break
          case 'PRESTO':
            defaultPort = '8080'
            break
          case 'REDSHIFT':
            defaultPort = '5439'
            break
          default:
            break
        }
        return defaultPort
      }
    },
    created () {
      // Backfill
      if (this.item.id) {
        this._getEditDatasource()
      }

      this._setDefaultValues()
    },
    watch: {
      type (value) {
        if (value === 'POSTGRESQL') {
          this.showDatabase = true
        } else {
          this.showDatabase = false
        }

        if (value === 'ORACLE' && !this.item.id) {
          this.showConnectType = true
          this.connectType = 'ORACLE_SERVICE_NAME'
        } else if (value === 'ORACLE' && this.item.id) {
          this.showConnectType = true
        } else {
          this.showConnectType = false
        }
        // Set default port for each type datasource
        this._setDefaultValues(value)

        return new Promise((resolve, reject) => {
          this.store.dispatch('datasource/getKerberosStartupState').then(res => {
            this.isShowPrincipal = res
            if ((value === 'HIVE' || value === 'SPARK') && this.isShowPrincipal === true) {
              this.showPrincipal = false
            } else {
              this.showPrincipal = true
            }
          }).catch(e => {
            this.$message.error(e.msg || '')
            reject(e)
          })
        })
      },
      /**
       * Cache the previous input port for each type datasource
       * @param value
       */
      port (value) {
        this.prePortMapper[this.type] = value
      }
    },

    mounted () {
    },
    components: { mListBoxF }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .datasource-popup-model {
    background: #fff;
    border-radius: 3px;

    .top-p {
      height: 70px;
      line-height: 70px;
      border-radius: 3px 3px 0 0;
      padding: 0 20px;
      >span {
        font-size: 20px;
      }
    }
    .bottom-p {
      text-align: right;
      height: 72px;
      line-height: 72px;
      border-radius:  0 0 3px 3px;
      padding: 0 20px;
    }
    .content-p {
      min-width: 850px;
      min-height: 100px;
      .list-box-f {
        .text {
          width: 166px;
        }
        .cont {
          width: calc(100% - 186px);
        }
      }
    }
    .radio-label-last {
      margin-left: 0px !important;
    }
  }

</style>
