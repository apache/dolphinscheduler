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
import dayjs from 'dayjs'

const getCategory = (categoryDic, { workFlowPublishStatus, schedulePublishStatus, code }, sourceWorkFlowCode) => {
  if (code === sourceWorkFlowCode) return categoryDic.active
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

const formatName = (str) => {
  if (typeof str !== 'string') return ''

  return str.slice(0, 6) + (str.length > 6 ? '\n...' : '')
}

const publishStatusFormat = (status) => {
  return status === 0 || status === '0' ? i18n.$t('offline') : status === 1 || status === '1' ? i18n.$t('online') : '-'
}

export default function (locations, links, sourceWorkFlowCode, isShowLabel) {
  const categoryDic = {
    active: { color: '#2D8DF0', category: i18n.$t('KinshipStateActive') },
    1: { color: '#00C800', category: i18n.$t('KinshipState1') },
    0: { color: '#999999', category: i18n.$t('KinshipState0') },
    10: { color: '#FF8F05', category: i18n.$t('KinshipState10') }
  }
  const newData = _.map(locations, (item) => {
    const { color, category } = getCategory(categoryDic, item, sourceWorkFlowCode)
    return {
      ...item,
      id: item.code,
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
        return `
          ${i18n.$t('workflowName')}：${name}<br/>
          ${i18n.$t('scheduleStartTime')}：${dayjs(scheduleStartTime).format('YYYY-MM-DD HH:mm:ss')}<br/>
          ${i18n.$t('scheduleEndTime')}：${dayjs(scheduleEndTime).format('YYYY-MM-DD HH:mm:ss')}<br/>
          ${i18n.$t('crontabExpression')}：${crontab}<br/>
          ${i18n.$t('workflowPublishStatus')}：${publishStatusFormat(workFlowPublishStatus)}<br/>
          ${i18n.$t('schedulePublishStatus')}：${publishStatusFormat(schedulePublishStatus)}<br/>
        `
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
          return formatName(params.data.name)
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
