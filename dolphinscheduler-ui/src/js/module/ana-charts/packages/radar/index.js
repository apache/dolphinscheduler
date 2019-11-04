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
import Base from '../base'
import { checkKeyInModel, init } from '../../common'

const TYPE = 'radar'

/**
 * 雷达图
 */
export default class Radar extends Base {
  /**
   * 单独导出时调用的初始化方法
   * @param {*} el 选择器或者 DOM 对象
   * @param {*} data 数据源
   * @param {*} options 可选项
   */
  static init (el, data, options) {
    return init(Radar, el, data, options)
  }

  /**
   * 将用户配置转换为符合 ECharts API 格式的配置格式
   */
  transform () {
    const {
      // 数据
      data = [],
      // 图表标题
      title = '雷达图',
      // 属性字典
      keyMap = {
        textKey: 'key',
        legendKey: 'typeName',
        dataKey: 'value'
      }
    } = this.settings

    if (data.length === 0) {
      throw new Error('数据源为空！')
    }

    // 文本对应属性名，图例对应的属性名，数据值对应的属性名
    const { textKey, legendKey, dataKey } = keyMap
    checkKeyInModel(data[0], textKey, legendKey, dataKey)

    const legendData = []
    const seriesData = []
    const indicator = []

    // 设置图例并初始化数据系列
    for (let i = 0; i < data.length; i++) {
      const legendItem = data[i][legendKey]
      const textItem = data[i][textKey]
      const dataItem = data[i][dataKey]

      // 图例
      if (!legendData.includes(legendItem)) {
        legendData.push(legendItem)
      }

      // 系列
      let targetSeries = seriesData.find(s => s.name === legendItem)
      if (!targetSeries) {
        targetSeries = {
          name: legendItem,
          value: [],
          _raw: []
        }
        seriesData.push(targetSeries)
      }
      targetSeries.value.push(dataItem)
      targetSeries._raw.push(data[i])

      // 指标
      let targetIndicator = indicator.find(i => i.name === textItem)
      if (!targetIndicator) {
        indicator.push({ name: textItem })
      }
    }

    return { title, seriesData, legendData, indicator }
  }

  /**
   * 绘制图表
   */
  apply () {
    const { title, seriesData, legendData = [], indicator } = this.options
    this.echart.setOption({
      title: {
        text: title
      },
      tooltip: {},
      legend: {
        data: legendData
      },
      radar: {
        name: {
          textStyle: {
            color: '#fff',
            backgroundColor: '#999',
            borderRadius: 3,
            padding: [3, 5]
          }
        },
        indicator
      },
      series: [{
        type: TYPE,
        data: seriesData
      }]
    }, true)
  }
}
