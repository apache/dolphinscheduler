export const getTimelineList = () => {
  const list = []
  for (let i = 2008; i < 2018; i++) {
    let index = 0
    for (const key of keys) {
      for (const day of days) {
        list.push({
          timeline: i,
          typeName: key,
          key: day,
          value: Math.floor(Math.random() * 100 + (index + 1) * 100)
        })
        index++
      }
    }
  }
  return list
}

export const getSimpleList = () => {
  const list = []
  for (const key of keys) {
    list.push({
      key: key,
      value: Math.floor(Math.random() * 100)
    })
  }
  return list
}

export const getMultipleList = (scatter = false) => {
  const list = []
  let index = 0
  for (const key of keys) {
    for (const day of days) {
      if (scatter) {
        list.push({
          typeName: key,
          text: day,
          x: Math.floor(Math.random() * 100 + (index + 1) * 100),
          y: Math.floor(Math.random() * 100 + (index + 1) * 100),
          size: Math.floor(Math.random() * 1000)
        })
      } else {
        list.push({
          typeName: key,
          key: day,
          value: Math.floor(Math.random() * 100 + (index + 1) * 100)
        })
      }
    }
    index++
  }
  return list
}

const keys = ['邮件营销', '联盟广告', '视频广告', '直接访问', '搜索引擎']
const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
