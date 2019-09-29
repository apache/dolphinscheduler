<a name="2.2.0"></a>
# [2.2.0](https://github.com/vuejs/component-compiler-utils/compare/v2.1.2...v2.2.0) (2018-08-16)


### Features

* **scoped-css:** support leading >>> or /deep/ in selectors ([1a3b5bb](https://github.com/vuejs/component-compiler-utils/commit/1a3b5bb))



<a name="2.1.2"></a>
## [2.1.2](https://github.com/vuejs/component-compiler-utils/compare/v2.1.1...v2.1.2) (2018-08-09)


### Bug Fixes

* pin prettier version ([5f138a6](https://github.com/vuejs/component-compiler-utils/commit/5f138a6))



<a name="2.1.1"></a>
## [2.1.1](https://github.com/vuejs/component-compiler-utils/compare/v2.1.0...v2.1.1) (2018-08-07)


### Bug Fixes

* remove space after selector when inserting scoped attribute ([5b299ed](https://github.com/vuejs/component-compiler-utils/commit/5b299ed)), closes [vue-loader/#1370](https://github.com/vuejs/component-compiler-utils/issues/1370)



<a name="2.1.0"></a>
# [2.1.0](https://github.com/vuejs/component-compiler-utils/compare/v2.0.0...v2.1.0) (2018-07-03)


### Bug Fixes

* Forward preprocessor options to less ([#25](https://github.com/vuejs/component-compiler-utils/issues/25)) ([3b19c1e](https://github.com/vuejs/component-compiler-utils/commit/3b19c1e)), closes [#24](https://github.com/vuejs/component-compiler-utils/issues/24)
* should work with variable named render (close [#23](https://github.com/vuejs/component-compiler-utils/issues/23)) ([273827b](https://github.com/vuejs/component-compiler-utils/commit/273827b))


### Features

* Support `stylus` as `<style>` lang ([#18](https://github.com/vuejs/component-compiler-utils/issues/18)) ([986084e](https://github.com/vuejs/component-compiler-utils/commit/986084e))



<a name="2.0.0"></a>
# [2.0.0](https://github.com/vuejs/component-compiler-utils/compare/v1.3.1...v2.0.0) (2018-06-03)


### Features

* Add async style compilation support ([#13](https://github.com/vuejs/component-compiler-utils/issues/13)) ([54464d6](https://github.com/vuejs/component-compiler-utils/commit/54464d6))
* allow/require compiler to be passed in for `parse` ([caa1538](https://github.com/vuejs/component-compiler-utils/commit/caa1538))


### BREAKING CHANGES

* vue template compiler must now be passed to `parse`
via options.



<a name="1.3.1"></a>
## [1.3.1](https://github.com/vuejs/component-compiler-utils/compare/v1.3.0...v1.3.1) (2018-05-28)


### Bug Fixes

* default parser was removed from prettier ([#15](https://github.com/vuejs/component-compiler-utils/issues/15)) ([598224d](https://github.com/vuejs/component-compiler-utils/commit/598224d))



<a name="1.3.0"></a>
# [1.3.0](https://github.com/vuejs/component-compiler-utils/compare/v1.2.1...v1.3.0) (2018-05-22)


### Features

* include href for <image> in transformAssetUrls (close [#12](https://github.com/vuejs/component-compiler-utils/issues/12)) ([86fddc2](https://github.com/vuejs/component-compiler-utils/commit/86fddc2))
* Provide installation instructions on missing language preprocessors ([#10](https://github.com/vuejs/component-compiler-utils/issues/10)) ([97e772c](https://github.com/vuejs/component-compiler-utils/commit/97e772c))



<a name="1.2.1"></a>
## [1.2.1](https://github.com/vuejs/component-compiler-utils/compare/v1.2.0...v1.2.1) (2018-04-26)


### Bug Fixes

* postcss import ([c845a80](https://github.com/vuejs/component-compiler-utils/commit/c845a80))



<a name="1.2.0"></a>
# [1.2.0](https://github.com/vuejs/component-compiler-utils/compare/v1.1.0...v1.2.0) (2018-04-26)


### Bug Fixes

* compile only lib directory ([#6](https://github.com/vuejs/component-compiler-utils/issues/6)) ([4f787b3](https://github.com/vuejs/component-compiler-utils/commit/4f787b3))


### Features

* accept postcss options and plugins ([#7](https://github.com/vuejs/component-compiler-utils/issues/7)) ([1456e3d](https://github.com/vuejs/component-compiler-utils/commit/1456e3d))



<a name="1.1.0"></a>
# [1.1.0](https://github.com/vuejs/component-compiler-utils/compare/9204f16...v1.1.0) (2018-04-24)


### Bug Fixes

* use more strict regex for matching css animation rules ([4644727](https://github.com/vuejs/component-compiler-utils/commit/4644727))


### Features

* adds stylus & less preprocessor ([#5](https://github.com/vuejs/component-compiler-utils/issues/5)) ([f2fd8b9](https://github.com/vuejs/component-compiler-utils/commit/f2fd8b9))
* preprocess scss/sass styles with node-sass ([#4](https://github.com/vuejs/component-compiler-utils/issues/4)) ([9204f16](https://github.com/vuejs/component-compiler-utils/commit/9204f16))



