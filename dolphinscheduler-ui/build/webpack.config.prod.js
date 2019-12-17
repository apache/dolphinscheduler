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
const ExtractTextPlugin = require('extract-text-webpack-plugin')
const ProgressPlugin = require('progress-bar-webpack-plugin')
const UglifyJSPlugin = require('uglifyjs-webpack-plugin')
const OptimizeCssAssetsPlugin = require('optimize-css-assets-webpack-plugin')

const resolve = dir =>
  path.resolve(__dirname, '..', dir)

const config = merge.smart(baseConfig, {
  devtool: 'source-map',
  output: {
    filename: 'js/[name].[chunkhash:7].js'
  },
  module: {
    rules: [
      {
        test: /\.vue$/,
        loader: 'vue-loader',
        options: {
          hotReload: false // Open hot overload
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
  plugins: [
    new ProgressPlugin(),
    new ExtractTextPlugin({ filename: 'css/[name].[contenthash:7].css', allChunks: true }),
    new webpack.optimize.CommonsChunkPlugin({ name: 'common', filename: 'js/[name].[hash:7].js' }),
    new webpack.optimize.OccurrenceOrderPlugin(),
    new OptimizeCssAssetsPlugin({
      assetNameRegExp: /\.css$/g,
      cssProcessor: require('cssnano'),
      cssProcessorOptions: { discardComments: { removeAll: true } },
      canPrint: true
    }),
    new UglifyJSPlugin({
      parallel: true,
      sourceMap: true,
      uglifyOptions: {
        compress: {
          warnings: false,
          drop_debugger: true,
          drop_console: true,
          pure_funcs: ['console.log']// remove console
        },
        comments: function (n, c) {
          /*! IMPORTANT: Please preserve 3rd-party library license info, inspired from @allex/amd-build-worker/config/jsplumb.js */
          var text = c.value, type = c.type
          if (type === 'comment2') {
            return /^!|@preserve|@license|@cc_on|MIT/i.test(text)
          }
        }
      }
    }),
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
  ]
})

module.exports = config
