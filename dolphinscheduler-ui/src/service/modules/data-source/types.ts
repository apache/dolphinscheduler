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
  | 'DOLPHINDB'

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
  endpoint?: string
  restEndpoint?: string
  kubeConfig?: string
  namespace?: string
  MSIClientId?: string
  dbUser?: string
  compatibleMode?: string
  publicKey?: string
  datawarehouse?: string
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
