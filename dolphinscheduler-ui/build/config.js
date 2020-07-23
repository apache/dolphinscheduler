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
const glob = require('globby')
const webpack = require('webpack')
const VueLoaderPlugin = require('vue-loader/lib/plugin')
const HtmlWebpackPlugin = require('html-webpack-plugin')
const MiniCssExtractPlugin = require('mini-css-extract-plugin')
const isProduction = process.env.NODE_ENV !== 'development'

const resolve = dir => path.join(__dirname, '..', dir)

const assetsDir = resolve('src')
const distDir = resolve('dist')
const viewDir = resolve('src/view')


function moduleName (modules) {
  let filename = path.basename(modules)
  let parts = filename.split('.')
  parts.pop()
  filename = parts.join('.')
  return path.dirname(modules) + '/' + filename
}

const jsEntry = (() => {
  const obj = {}
  const files = glob.sync(['js/conf/*/!(_*).js'], { cwd: assetsDir })
  files.forEach(val => {
    let parts = val.split(/[\\/]/)
    parts.shift()
    parts.shift()
    let modules = parts.join('/')
    let entry = moduleName(modules)
    obj[entry] = val
  })
  return obj
})()

const minifierConfig = isProduction ? {
  removeComments: true,
  removeCommentsFromCDATA: true,
  collapseWhitespace: true,
  collapseBooleanAttributes: true,
  removeRedundantAttributes: true,
  useShortDoctype: true,
  minifyJS: true,
  removeScriptTypeAttributes: true,
  maxLineLength: 1024
} : false

const getPageEntry = view => jsEntry[view] ? view : ''

// Redirect output page
const pageRewriter = {
  'view/home/index.*': 'index.html'
}

const isEmpty = o => {
  for (let k in o) {
    if (o.hasOwnProperty(k)) {
      return
    }
  }
  return true
}

const unixPath = v => v.replace(/\\/g, '/')

const rewriterPath = p => {
  if (isEmpty(pageRewriter)) {
    return
  }

  for (let k in pageRewriter) {
    let regx = new RegExp(k)

    if (regx.test(unixPath(p))) {
      return pageRewriter[k]
    }
  }
}
const version = new Date().getTime();
const pages = glob.sync(['*/!(_*).html'], { cwd: viewDir }).map(p => {
  let pagePath = `${path.join(viewDir, p)}`
  let newPagePath = rewriterPath(pagePath)

  let entry = getPageEntry(p.replace('.html', ''))
  let chunks = ['common']
  if (entry) {
    chunks.push(entry)
  }
  return new HtmlWebpackPlugin({
    filename: newPagePath || path.join('view', p),
    template: `${path.join('src/view', p)}`,
    cache: true,
    favicon:'./favicon.png',
    inject: true,
    hash: version,
    chunks: chunks,
    minify: minifierConfig
  })
})

const baseConfig = {
  entry: jsEntry,
  output: {
    path: distDir,
    publicPath: '/',
    filename: 'js/[name].[chunkhash:7]'+version+'.js'
  },
  module: {
    rules: [
      {
        test: /\.vue$/,
        loader: 'vue-loader',
        options: {
          hotReload: !isProduction
        }
      },
      {
        test: /\.js$/,
        exclude: /(node_modules|bower_components)/,
        use: [
          {
            loader: 'babel-loader',
            options: {
              cacheDirectory: true,
              cacheIdentifier: true
            }
          }
        ]
      },
      {
        test: /\.(sa|sc|c)ss$/,
        use: [
          {
            loader: MiniCssExtractPlugin.loader,
            options: {
              hmr: !isProduction,
            },
          },
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
          },
          'sass-loader'
        ]
      },
      {
        test: /\.(png|jpe?g|gif|svg|cur)(\?.*)?$/,
        loader: 'file-loader',
        options: {
          esModule: false,
          name: 'images/[name].[ext]?[hash]'
        }
      },
      {
        test: /\.(woff2?|eot|ttf|otf)(\?.*)?$/,
        loader: 'url-loader',
        options: {
          esModule: false,
          limit: 10000,
          // publicPath: distDir,
          name: 'font/[name].[hash:7].[ext]'
        }
      }
    ]
  },
  resolve: {
    modules: [
      resolve('node_modules'),
      resolve('src'),
      resolve('src/js')
    ],
    alias: {
      '@': resolve('src/js'),
      '~': resolve('src/lib'),
      'jquery':'jquery/dist/jquery.min.js',
      'jquery-ui': 'jquery-ui'
    },
    extensions: ['.js', 'json', '.vue', '.scss']
  },
  plugins: [
    new VueLoaderPlugin(),
    new webpack.ProvidePlugin({ vue: 'Vue', _: 'lodash',jQuery:"jquery/dist/jquery.min.js",$:"jquery/dist/jquery.min.js" }),
    new webpack.DefinePlugin({
      PUBLIC_PATH: JSON.stringify(process.env.PUBLIC_PATH ? process.env.PUBLIC_PATH : '')
    }),
    ...pages
  ]
}

module.exports = {
  isProduction,
  assetsDir,
  distDir,
  baseConfig
}
