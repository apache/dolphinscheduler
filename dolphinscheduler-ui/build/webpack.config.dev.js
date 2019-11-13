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
const ExtractTextPlugin = require('extract-text-webpack-plugin')
const ProgressPlugin = require('progress-bar-webpack-plugin')
const getEnv = require('env-parse').getEnv

const config = merge.smart(baseConfig, {
  devtool: 'eval-source-map',
  output: {
    filename: 'js/[name].js'
  },
  module: {
    rules: [
      {
        test: /\.vue$/,
        loader: 'vue-loader',
        options: {
          hotReload: true // Open hot overload
        }
      },
      {
        test: /\.css$/,
        loader: ExtractTextPlugin.extract({
          use: [
            'css-loader',
            {
              loader: 'postcss-loader',
              options: {
                plugins: (loader) => [
                  require('autoprefixer')({
                    overrideBrowserslist: [
                      "Android 4.1",
                      "iOS 7.1",
                      "Chrome > 31",
                      "ff > 31",
                      "ie >= 8"
                    ]              
                  }),
                  require('cssnano')
                ]
              }
            }
          ],
          fallback: ['vue-style-loader']
        })
      },
      {
        test: /\.scss$/,
        loader: ExtractTextPlugin.extract({
          use: [
            'css-loader',
            'sass-loader',
            {
              loader: 'postcss-loader',
              options: {
                plugins: (loader) => [
                  require('autoprefixer')({
                    overrideBrowserslist: [
                      "Android 4.1",
                      "iOS 7.1",
                      "Chrome > 31",
                      "ff > 31",
                      "ie >= 8"
                    ] 
                  }),
                  require('cssnano')
                ]
              }
            }
          ],
          fallback: ['vue-style-loader']
        })
      }
    ]
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
    new ExtractTextPlugin({ filename: 'css/[name].css', allChunks: true }),
    new webpack.optimize.CommonsChunkPlugin({ name: 'common', filename: 'js/[name].js' }),
    new webpack.optimize.OccurrenceOrderPlugin()
  ]
})

module.exports = config
