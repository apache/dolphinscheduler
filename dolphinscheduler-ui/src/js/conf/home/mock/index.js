const Mock = require('mockjs');
import isEmpty from 'lodash/isEmpty';

import mockApiDic from './projects';

Mock.setup({
  timeout: '200-600' // 200ms-600ms后返回数据
})

// the resultDTO of response
export const resultDTO = {
  code: 0,
  msg: "success",
  data: {},
}

// Register mock api https://github.com/nuysoft/Mock/wiki/Mock.mock()
if (!isEmpty(mockApiDic) && ISMOCK) {
  Object.keys(mockApiDic).forEach(key => {
    const keyArr = key.split(' ');
    if (keyArr.length === 2) {
      Mock.mock(new RegExp(keyArr[0]), keyArr[1], mockApiDic[key]);
    }
  })
}

