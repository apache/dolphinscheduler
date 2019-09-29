import {
  RawSourceMap,
  VueTemplateCompiler,
  VueTemplateCompilerParseOptions
} from './types'

const hash = require('hash-sum')
const cache = require('lru-cache')(100)
const { SourceMapGenerator } = require('source-map')

const splitRE = /\r?\n/g
const emptyRE = /^(?:\/\/)?\s*$/

export interface ParseOptions {
  source: string
  filename?: string
  compiler: VueTemplateCompiler
  compilerParseOptions?: VueTemplateCompilerParseOptions
  sourceRoot?: string
  needMap?: boolean
}

export interface SFCCustomBlock {
  type: string
  content: string
  attrs: { [key: string]: string }
  start: number
  end: number
  map: RawSourceMap
}

export interface SFCBlock extends SFCCustomBlock {
  lang?: string
  src?: string
  scoped?: boolean
  module?: string | boolean
}

export interface SFCDescriptor {
  template?: SFCBlock
  script?: SFCBlock
  styles: SFCBlock[]
  customBlocks: SFCCustomBlock[]
}

export function parse(options: ParseOptions): SFCDescriptor {
  const {
    source,
    filename = '',
    compiler,
    compilerParseOptions = { pad: 'line' },
    sourceRoot = process.cwd(),
    needMap = true
  } = options
  const cacheKey = hash(filename + source)
  let output: SFCDescriptor = cache.get(cacheKey)
  if (output) return output
  output = compiler.parseComponent(source, compilerParseOptions)
  if (needMap) {
    if (output.script && !output.script.src) {
      output.script.map = generateSourceMap(
        filename,
        source,
        output.script.content,
        sourceRoot
      )
    }
    if (output.styles) {
      output.styles.forEach(style => {
        if (!style.src) {
          style.map = generateSourceMap(
            filename,
            source,
            style.content,
            sourceRoot
          )
        }
      })
    }
  }
  cache.set(cacheKey, output)
  return output
}

function generateSourceMap(
  filename: string,
  source: string,
  generated: string,
  sourceRoot: string
): RawSourceMap {
  const map = new SourceMapGenerator({
    file: filename,
    sourceRoot
  })
  map.setSourceContent(filename, source)
  generated.split(splitRE).forEach((line, index) => {
    if (!emptyRE.test(line)) {
      map.addMapping({
        source: filename,
        original: {
          line: index + 1,
          column: 0
        },
        generated: {
          line: index + 1,
          column: 0
        }
      })
    }
  })
  return map.toJSON()
}
