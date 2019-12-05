/**
 * webpack config for production
 *
 * author: liuxin(liuxin@analysys.com.cn)
 */

const webpack = require('webpack')
const merge = require('webpack-merge')
const { baseConfig } = require('./config')
const ExtractTextPlugin = require('extract-text-webpack-plugin')
const UglifyJSPlugin = require('uglifyjs-webpack-plugin')
const OptimizeCssAssetsPlugin = require('optimize-css-assets-webpack-plugin')
const VueLoaderPlugin = require('vue-loader/lib/plugin')

const config = merge.smart(baseConfig, {
  devtool: 'source-map',
  output: {
    filename: '[name].js',
    libraryTarget: 'umd',
    umdNamedDefine: false
  },
  module: {
    rules: [
      {
        test: /\.vue$/,
        loader: 'vue-loader',
        options: {
          hotReload: false
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
                    'browsers': ['ie > 8', 'last 2 version', 'safari >= 9']
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
                    'browsers': ['ie > 8', 'last 2 version', 'safari >= 9']
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
    new VueLoaderPlugin(),
    new ExtractTextPlugin({ filename: '[name].css', allChunks: true }),
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
          drop_console: true,
          drop_debugger: true
        },
        comments: function (n, c) {
          /*! IMPORTANT: Please preserve 3rd-party library license info, inspired from @allex/amd-build-worker/config/util.js */
          var text = c.value, type = c.type
          if (type === 'comment2') {
            return /^!|@preserve|@license|@cc_on|MIT/i.test(text)
          }
        }
      }
    })
  ]
})

module.exports = config
