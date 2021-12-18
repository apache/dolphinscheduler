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

type DataBase =
  | 'MYSQL'
  | 'POSTGRESQL'
  | 'HIVE'
  | 'SPARK'
  | 'CLICKHOUSE'
  | 'ORACLE'
  | 'SQLSERVER'
  | 'DB2'
  | 'PRESTO'
  | 'H2'

interface DataSource {
  database?: string
  host?: string
  id?: number
  name?: string
  note?: string
  other?: object
  password?: string
  port?: number
  type?: DataBase
  userName?: string
}

interface ListReq {
  pageNo: number
  pageSize: number
  searchVal?: string
}

interface DataSourceReq {
  dataSourceParam: DataSource
}

interface UserIdReq {
  userId: number
}

interface TypeReq {
  type: DataBase
}

interface NameReq {
  name: string
}

interface IdReq {
  id: number
}

export { ListReq, DataSourceReq, UserIdReq, TypeReq, NameReq, IdReq }
