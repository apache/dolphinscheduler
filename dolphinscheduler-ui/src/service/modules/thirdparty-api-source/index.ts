import { axios } from '@/service/service'
import {
  ThirdpartyApiSourceReq,
  ThirdpartyApiSource,
} from './types'

// 分页查询
export function queryThirdpartyApiSourceListPaging(params: Partial<ThirdpartyApiSourceReq>): Promise<any> {
  return axios({
    url: '/external-systems',
    method: 'get',
    params
  })
}

// 新增
export function createThirdpartyApiSource(data: ThirdpartyApiSource): Promise<any> {
  return axios({
    url: '/external-systems/',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    transformRequest: (params) => JSON.stringify(params)
  })
}

// 测试连接
export function testThirdpartyApiSourceConnection(data: ThirdpartyApiSource): Promise<any> {
  return axios({
    url: '/external-systems/test-connection',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    transformRequest: (params) => JSON.stringify(params)
  })
}

// 更新
export function updateThirdpartyApiSource(id: number, data: ThirdpartyApiSource): Promise<any> {
  return axios({
    url: `/external-systems/${id}`,
    method: 'put',
    data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    transformRequest: (params) => JSON.stringify(params)
  })
}

// 删除
export function deleteThirdpartyApiSource(id: number): Promise<any> {
  return axios({
    url: `/external-systems/${id}`,
    method: 'delete'
  })
}

// 查询详情
export function getThirdpartyApiSourceById(id: number): Promise<any> {
  return axios({
    url: `/external-systems/${id}`,
    method: 'get'
  })
}

// 查询已授权第三方系统
export function authedThirdpartySystem(params: { userId: number }): any {
  return axios({
    url: '/external-systems/authed-externalSystem',
    method: 'get',
    params
  })
}

// 查询未授权第三方系统
export function unAuthThirdpartySystem(params: { userId: number }): any {
  return axios({
    url: '/external-systems/unauth-externalSystem',
    method: 'get',
    params
  })
}

// 授权第三方系统
export function grantThirdpartySystem(data: { userId: number, externalSystemIds: string }): any {
  const formData = new URLSearchParams()
  formData.append('userId', String(data.userId))
  formData.append('externalSystemIds', data.externalSystemIds)
  return axios({
    url: '/users/grant-externalSystem',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
  })
} 