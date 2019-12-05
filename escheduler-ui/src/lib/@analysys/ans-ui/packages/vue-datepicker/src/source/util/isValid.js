/*
*
* 验证日期是否合法
*
*/

import moment from 'dayjs'

export default (date) => {
  // return Date.parse(date) > 0
  return moment(date).isValid()
}
