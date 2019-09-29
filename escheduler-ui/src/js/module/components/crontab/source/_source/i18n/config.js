/* eslint-disable */
import _ from 'lodash'
import zh_CN from './locale/zh_CN'
import en_US from './locale/en_US'

export function localeList () {
  return [
    {
      code: 'zh_CN',
      name: '中文',
      locale: zh_CN
    },
    {
      code: 'en_US',
      name: 'English',
      locale: en_US
    }
  ]
}

export function findLocale (code) {
  return _.find(localeList(), ['code', code])
}
