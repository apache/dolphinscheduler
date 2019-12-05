/**
 * webpack config
 *
 * author: liuxin(liuxin@analysys.com.cn)
 */

const path = require('path')
const glob = require('globby')

const isProduction = process.env.NODE_ENV !== 'development'
const resolve = dir => path.join(__dirname, '..', dir)
const assetsDir = resolve('src')
const distDir = resolve('dist')

const baseConfig = {
  entry: {
    'index': glob.sync(['index.js'], { cwd: assetsDir })
  },
  output: {
    path: distDir
  },
  module: {
    rules: [
      {
        test: /\.js$/,
        exclude: file => (
          /node_modules/.test(file) &&
          !/\.vue\.js/.test(file)
        ),
        use: [
          {
            loader: 'babel-loader',
            options: {
              cacheDirectory: true,
              cacheIdentifier: true
            }
          }
        ]
      }
    ]
  },
  resolve: {
    modules: [
      resolve('node_modules'),
      resolve('src')
    ],
    extensions: ['.js', '.json', '.vue', '.scss']
  },
  externals: {
    'vue': 'Vue',
    'echarts': 'echarts'
  }
}

module.exports = {
  isProduction,
  assetsDir,
  distDir,
  baseConfig
}
