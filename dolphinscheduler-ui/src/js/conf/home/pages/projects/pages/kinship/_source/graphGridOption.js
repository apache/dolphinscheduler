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
import _ from 'lodash'
import i18n from '@/module/i18n/index.js'

const getCategory = (categoryDic, { workFlowPublishStatus, schedulePublishStatus, id }, sourceWorkFlowId) => {
  if (id === sourceWorkFlowId) return categoryDic.active
  switch (true) {
    case workFlowPublishStatus === '0':
      return categoryDic['0']
    case workFlowPublishStatus === '1' && schedulePublishStatus === '0':
      return categoryDic['10']
    case workFlowPublishStatus === '1' && schedulePublishStatus === '1':
    default:
      return categoryDic['1']
  }
}

export default function (locations, links, sourceWorkFlowId, isShowLabel) {
  const categoryDic = {
    active: { color: '#2D8DF0', category: i18n.$t('KinshipStateActive') },
    1: { color: '#00C800', category: i18n.$t('KinshipState1') },
    0: { color: '#999999', category: i18n.$t('KinshipState0') },
    10: { color: '#FF8F05', category: i18n.$t('KinshipState10') }
  }
  const newData = _.map(locations, (item) => {
    const { color, category } = getCategory(categoryDic, item, sourceWorkFlowId)
    return {
      ...item,
      emphasis: {
        itemStyle: {
          color
        }
      },
      category
    }
  })

  const categories = [
    { name: categoryDic.active.category },
    { name: categoryDic['1'].category },
    { name: categoryDic['0'].category },
    { name: categoryDic['10'].category }
  ]
  const option = {
    tooltip: {
      trigger: 'item',
      triggerOn: 'mousemove',
      backgroundColor: '#2D303A',
      padding: [8, 12],
      formatter: (params) => {
        if (!params.data.name) return ''
        const { name, scheduleStartTime, scheduleEndTime, crontab, workFlowPublishStatus, schedulePublishStatus } = params.data
        const str = `
      工作流名字：${name}<br/>
      调度开始时间：${scheduleStartTime}<br/>
      调度结束时间：${scheduleEndTime}<br/>
      crontab表达式：${crontab}<br/>
      工作流发布状态：${workFlowPublishStatus}<br/>
      调度发布状态：${schedulePublishStatus}<br/>
      `
        return str
      },
      color: '#2D303A',
      textStyle: {
        rich: {
          a: {
            fontSize: 12,
            color: '#2D303A',
            lineHeight: 12,
            align: 'left',
            padding: [4, 4, 4, 4]
          }
        }
      }
    },
    color: [categoryDic.active.color, categoryDic['1'].color, categoryDic['0'].color, categoryDic['10'].color],
    legend: [{
      orient: 'horizontal',
      top: 6,
      left: 6,
      data: categories
    }],
    series: [{
      type: 'graph',
      layout: 'force',
      nodeScaleRatio: 1.2,
      draggable: true,
      animation: false,
      data: newData,
      roam: true,
      symbol: 'roundRect',
      symbolSize: 70,
      categories,
      label: {
        show: isShowLabel,
        position: 'inside',
        formatter: (params) => {
          if (!params.data.name) return ''
          const str = params.data.name.split('_').map(item => `{a|${item}\n}`).join('')
          return str
        },
        color: '#222222',
        textStyle: {
          rich: {
            a: {
              fontSize: 12,
              color: '#222222',
              lineHeight: 12,
              align: 'left',
              padding: [4, 4, 4, 4]
            }
          }
        }
      },
      edgeSymbol: ['circle', 'arrow'],
      edgeSymbolSize: [4, 12],
      force: {
        repulsion: 1000,
        edgeLength: 300
      },
      links: links,
      lineStyle: {
        color: '#999999'
      }
    }]
  }

  return option
}
