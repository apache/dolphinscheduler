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

type IDataBase =
  | 'MYSQL'
  | 'POSTGRESQL'
  | 'HIVE'
  | 'SPARK'
  | 'CLICKHOUSE'
  | 'ORACLE'
  | 'SQLSERVER'
  | 'DB2'
  | 'VERTICA'
  | 'PRESTO'
  | 'REDSHIFT'
  | 'ATHENA'
  | 'TRINO'
  | 'AZURESQL'
  | 'STARROCKS'
  | 'DAMENG'
  | 'OCEANBASE'
  | 'SSH'
  | 'DATABEND'
  | 'SNOWFLAKE'
  | 'HANA'
  | 'DORIS'
  | 'KYUUBI'
  | 'ZEPPELIN'
  | 'SAGEMAKER'
  | 'K8S'
  | 'ALIYUN_SERVERLESS_SPARK'
  | 'DOLPHINDB'
  | 'THIRDPARTY_SYSTEM_CONNECTOR'

type IDataBaseLabel =
  | 'MYSQL'
  | 'POSTGRESQL'
  | 'HIVE'
  | 'SPARK'
  | 'CLICKHOUSE'
  | 'ORACLE'
  | 'SQLSERVER'
  | 'DB2'
  | 'PRESTO'
  | 'REDSHIFT'
  | 'ATHENA'
  | 'TRINO'
  | 'AZURESQL'
  | 'STARROCKS'
  | 'DAMENG'
  | 'OCEANBASE'
  | 'SSH'
  | 'KYUUBI'
  | 'ZEPPELIN'
  | 'SAGEMAKER'
  | 'K8S'
  | 'ALIYUN_SERVERLESS_SPARK'
  | 'DOLPHINDB'

interface IDataSource {
  id?: number
  type?: IDataBase
  label?: IDataBaseLabel
  name?: string
  note?: string
  host?: string
  port?: number
  principal?: string
  javaSecurityKrb5Conf?: string
  loginUserKeytabUsername?: string
  loginUserKeytabPath?: string
  mode?: string
  userName?: string
  password?: string
  awsRegion?: string
  database?: string
  connectType?: string
  other?: object
  restEndpoint?: string
  kubeConfig?: string
  namespace?: string
  MSIClientId?: string
  dbUser?: string
  compatibleMode?: string
  privateKey?: string
  datawarehouse?: string
  accessKeyId?: string
  accessKeySecret?: string
  regionId?: string
  endpoint?: string
  // THIRDPARTY_SYSTEM_CONNECTOR fields
  serviceAddress?: string
  interfaceTimeout?: number
  authConfig?: {
    authType: string
    basicUsername?: string
    basicPassword?: string
    jwtToken?: string
    oauth2TokenUrl?: string
    oauth2ClientId?: string
    oauth2ClientSecret?: string
    oauth2GrantType?: string
    oauth2Username?: string
    oauth2Password?: string
    headerPrefix?: string
    authMappings?: {
      key: string
      value: string
    }[]
  }
  selectInterface?: {
    url: string
    method: string
    parameters?: {
      paramName: string
      paramValue: string
      location: string
    }[]
    body: string
    responseParameters?: {
      key: string
      jsonPath: string
      disabled?: boolean
    }[]
  }
  submitInterface?: {
    url: string
    method: string
    parameters?: {
      paramName: string
      paramValue: string
      location: string
    }[]
    body: string
    responseParameters?: {
      key: string
      jsonPath: string
      disabled?: boolean
    }[]
  }
  pollStatusInterface?: {
    url: string
    method: string
    parameters?: {
      paramName: string
      paramValue: string
      location: string
    }[]
    body: string
    pollingSuccessConfig: {
      successField: string
      successValue: string
    }
    pollingFailureConfig: {
      failureField: string
      failureValue: string
    }
    responseParameters?: {
      key: string
      jsonPath: string
      disabled?: boolean
    }[]
  }
  stopInterface?: {
    url: string
    method: string
    parameters?: {
      paramName: string
      paramValue: string
      location: string
    }[]
    body: string
    responseParameters?: {
      key: string
      jsonPath: string
      disabled?: boolean
    }[]
  }
}

interface ListReq {
  pageNo: number
  pageSize: number
  searchVal?: string
}

interface UserIdReq {
  userId: number
}

interface TypeReq {
  type: IDataBase
}

interface NameReq {
  name: string
}

type IdReq = number

export { ListReq, IDataBase, IDataSource, UserIdReq, TypeReq, NameReq, IdReq }
