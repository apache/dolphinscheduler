import { SFCDescriptor } from './parse'

export interface StartOfSourceMap {
  file?: string
  sourceRoot?: string
}

export interface RawSourceMap extends StartOfSourceMap {
  version: string
  sources: string[]
  names: string[]
  sourcesContent?: string[]
  mappings: string
}

export interface VueTemplateCompiler {
  parseComponent(source: string, options?: any): SFCDescriptor

  compile(
    template: string,
    options: VueTemplateCompilerOptions
  ): VueTemplateCompilerResults

  ssrCompile(
    template: string,
    options: VueTemplateCompilerOptions
  ): VueTemplateCompilerResults
}

// we'll just shim this much for now - in the future these types
// should come from vue-template-compiler directly, or this package should be
// part of the vue monorepo.
export interface VueTemplateCompilerOptions {
  modules?: Object[]
}

export interface VueTemplateCompilerParseOptions {
  pad?: 'line' | 'space'
}

export interface VueTemplateCompilerResults {
  ast: Object | void
  render: string
  staticRenderFns: string[]
  errors: string[]
  tips: string[]
}
