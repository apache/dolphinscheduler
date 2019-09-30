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
export default function (value) {
  return {
    series: [
      {
        type: 'gauge',
        center: ['50%', '45%'], // 仪表位置
        radius: '80%', // 仪表大小
        startAngle: 200, // 开始角度
        endAngle: -20, // 结束角度
        axisLine: {
          show: false,
          lineStyle: { // 属性lineStyle控制线条样式
            color: [
              [ 0.5, new echarts.graphic.LinearGradient(0, 0, 1, 0, [{ // eslint-disable-line
                offset: 1,
                color: '#E75F25' // 50% 处的颜色
              }, {
                offset: 0.8,
                color: '#D9452C' // 40% 处的颜色
              }], false) ], // 100% 处的颜色
              [ 0.7, new echarts.graphic.LinearGradient(0, 0, 1, 0, [{ // eslint-disable-line
                offset: 1,
                color: '#FFC539' // 70% 处的颜色
              }, {
                offset: 0.8,
                color: '#FE951E' // 66% 处的颜色
              }, {
                offset: 0,
                color: '#E75F25' // 50% 处的颜色
              }], false) ],
              [ 0.9, new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ // eslint-disable-line
                offset: 1,
                color: '#C7DD6B' // 90% 处的颜色
              }, {
                offset: 0.8,
                color: '#FEEC49' // 86% 处的颜色
              }, {
                offset: 0,
                color: '#FFC539' // 70% 处的颜色
              }], false) ],
              [1, new echarts.graphic.LinearGradient(0, 0, 0, 1, [ { // eslint-disable-line
                offset: 0.2,
                color: '#1CAD52' // 92% 处的颜色
              }, {
                offset: 0,
                color: '#C7DD6B' // 90% 处的颜色
              }], false) ]
            ],
            width: 10
          }
        },
        splitLine: {
          show: false
        },
        axisTick: {
          show: false
        },
        axisLabel: {
          show: false
        },
        pointer: { // 指针样式
          length: '45%'
        },
        detail: {
          show: false
        }
      },
      {
        type: 'gauge',
        center: ['50%', '45%'], // 默认全局居中
        radius: '70%',
        startAngle: 200,
        endAngle: -20,
        axisLine: {
          show: true,
          lineStyle: { // 属性lineStyle控制线条样式
            color: [ // 表盘颜色
              [ 0.5, '#DA462C' ], // 0-50%处的颜色
              [ 0.7, '#FF9618' ], // 51%-70%处的颜色
              [ 0.9, '#FFED44' ], // 70%-90%处的颜色
              [ 1, '#20AE51' ]// 90%-100%处的颜色
            ],
            width: 30// 表盘宽度
          }
        },
        splitLine: { // 分割线样式（及10、20等长线样式）
          length: 30,
          lineStyle: { // 属性lineStyle控制线条样式
            width: 2
          }
        },
        axisTick: { // 刻度线样式（及短线样式）
          length: 20
        },
        axisLabel: { // 文字样式（及“10”、“20”等文字样式）
          color: 'black',
          distance: 5 // 文字离表盘的距离
        },
        detail: {
          formatter: '{score|{value}%}',
          offsetCenter: [0, '50%'],
          backgroundColor: '#2D8BF0',
          height: 30,
          rich: {
            score: {
              color: 'white',
              fontFamily: '微软雅黑',
              fontSize: 32
            }
          }
        },
        data: [{
          value: value || 0,
          label: {
            textStyle: {
              fontSize: 12
            }
          }
        }]
      }
    ]
  }
}
