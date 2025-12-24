import { axios } from '@/service/service'
import {
  ThirdpartyApiSourceReq,
  ThirdpartyApiSource,
} from './types'

export function queryThirdpartyApiSourceListPaging(params: Partial<ThirdpartyApiSourceReq>): Promise<any> {
  return axios({
    url: '/external-systems',
    method: 'get',
    params
  })
}




