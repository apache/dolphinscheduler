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

export type ResourceType = 'FILE' | 'UDF'

export interface ResourceFileTableData {
  name: string
  fullName: string
  user_name: string
  directory: string
  file_name: string
  description: string
  size: number
  type: ResourceType
  update_time: string
}

export interface IEmit {
  (event: any, ...args: any[]): void
}

export interface IReuploadResource {
  (name: string, description: string, fullName: string, user_name: string): void
}

export interface IRenameResource {
  (name: string, description: string, fullName: string, user_name: string): void
}

export interface IRtDisb {
  (name: string, size: number): boolean
}

export interface IResourceListState {
  (
    type: ResourceType,
    searchVal?: string,
    fullName?: string,
    tenantCode?: string,
    pageNo?: number,
    pageSize?: number
  ): any
}

export interface BasicTableProps {
  title?: string
  dataSource: Function
  columns: any[]
  pagination: object
  showPagination: boolean
  actionColumn: any[]
  canResize: boolean
  resizeHeightOffset: number
}

export interface PaginationProps {
  page?: number
  pageCount?: number
  pageSize?: number
  pageSizes?: number[]
  showSizePicker?: boolean
  showQuickJumper?: boolean
}

export interface ISetPagination {
  (itemCount: number): void
}

export interface BreadcrumbItem {
  id: number
  fullName: string
  userName: string
}

export interface ICreateFileDefaultValue {
  (): {
    pid: number
    type: ResourceType
    suffix: string
    fileName: string
    description: string
    content: string
    currentDir: string
  }
}

export interface IFolderDefaultValue {
  (): {
    pid: number
    type: ResourceType
    name: string
    description: string
    currentDir: string
  }
}

export interface IRenameDefaultValue {
  (
    type: ResourceType,
    fullName?: string,
    name?: string,
    description?: string,
    user_name?: string
  ): {
    fullName: string
    name: string
    type: ResourceType
    description: string
    user_name: string
  }
}

export interface IUploadDefaultValue {
  (): {
    isReupload: boolean
    fullName: string
    user_name: string
    name: string
    file: string
    type: ResourceType
    pid: number
    currentDir: string
  }
}
