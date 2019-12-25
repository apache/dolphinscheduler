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
const webpack = require('webpack')
const merge = require('webpack-merge')
const { assetsDir, baseConfig } = require('./config')
const MiniCssExtractPlugin = require('mini-css-extract-plugin')
const ProgressPlugin = require('progress-bar-webpack-plugin')
const getEnv = require('env-parse').getEnv

const config = merge.smart(baseConfig, {
  devtool: 'eval-source-map',
  output: {
    filename: 'js/[name].js'
  },
  devServer: {
    hot: true,
    contentBase: assetsDir,
    publicPath: baseConfig.output.publicPath,
    port: getEnv('DEV_PORT', 8888),
    host: getEnv('DEV_HOST', 'localhost'),
    noInfo: false,
    historyApiFallback: true,
    disableHostCheck: true,
    proxy: {
      '/dolphinscheduler': {
        timeout: 1800000,
        target: getEnv('API_BASE', 'http://local.dev:8080/backend'),
        changeOrigin: true
      }
    },
    progress: false,
    quiet: false,
    stats: {
      colors: true
    },
    clientLogLevel: 'none'
  },
  plugins: [
    new ProgressPlugin(),
    new webpack.HotModuleReplacementPlugin(),
    new MiniCssExtractPlugin({ filename: 'css/[name].css' })
  ],
  mode: 'development'
})

module.exports = config
