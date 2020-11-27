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
const path = require('path')
const webpack = require('webpack')
const merge = require('webpack-merge')
const CopyWebpackPlugin = require('copy-webpack-plugin')
const { baseConfig } = require('./config')
const TerserPlugin = require('terser-webpack-plugin')
const MiniCssExtractPlugin = require('mini-css-extract-plugin')
const ProgressPlugin = require('progress-bar-webpack-plugin')

const resolve = dir =>
  path.resolve(__dirname, '..', dir)

const config = merge.smart(baseConfig, {
  devtool: 'source-map',
  output: {
    filename: 'js/[name].[chunkhash:7].js'
  },
  plugins: [
    new ProgressPlugin(),
    new MiniCssExtractPlugin({ filename: 'css/[name].[contenthash:7].css' }),
    new webpack.optimize.OccurrenceOrderPlugin(),
    new CopyWebpackPlugin([
      {
        from: resolve('src/lib'),
        to: resolve('dist/lib')
      },
      {
        from: resolve('src/images'),
        to: resolve('dist/images')
      },
    ]),
  ],
  optimization: {
    minimize: true,
    minimizer: [
      new TerserPlugin({
        terserOptions: {
          compress: {}
        },
        cache: true,
        parallel: true,
        sourceMap: false,
        exclude: /node_modules/,
        extractComments: (astNode, comment) => {
          if (/^!|@preserve|@license|@cc_on|MIT/i.test(comment.value)) {
            return true
          }
          return false
        }
      }),
    ],
    sideEffects: true
  },
  mode: 'production'
})

module.exports = config
