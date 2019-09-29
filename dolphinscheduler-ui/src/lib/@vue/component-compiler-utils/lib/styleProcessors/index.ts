const merge = require('merge-source-map')

export interface StylePreprocessor {
  render(
    source: string,
    map: any | null,
    options: any
  ): StylePreprocessorResults
}

export interface StylePreprocessorResults {
  code: string
  map?: any
  errors: Array<Error>
}

// .scss/.sass processor
const scss: StylePreprocessor = {
  render(
    source: string,
    map: any | null,
    options: any
  ): StylePreprocessorResults {
    const nodeSass = require('node-sass')
    const finalOptions = Object.assign({}, options, {
      data: source,
      file: options.filename,
      outFile: options.filename,
      sourceMap: !!map
    })

    try {
      const result = nodeSass.renderSync(finalOptions)

      if (map) {
        return {
          code: result.css.toString(),
          map: merge(map, JSON.parse(result.map.toString())),
          errors: []
        }
      }

      return { code: result.css.toString(), errors: [] }
    } catch (e) {
      return { code: '', errors: [e] }
    }
  }
}

const sass = {
  render(
    source: string,
    map: any | null,
    options: any
  ): StylePreprocessorResults {
    return scss.render(
      source,
      map,
      Object.assign({}, options, { indentedSyntax: true })
    )
  }
}

// .less
const less = {
  render(
    source: string,
    map: any | null,
    options: any
  ): StylePreprocessorResults {
    const nodeLess = require('less')

    let result: any
    let error: Error | null = null
    nodeLess.render(
      source,
      Object.assign({}, options, { syncImport: true }),
      (err: Error | null, output: any) => {
        error = err
        result = output
      }
    )

    if (error) return { code: '', errors: [error] }

    if (map) {
      return {
        code: result.css.toString(),
        map: merge(map, result.map),
        errors: []
      }
    }

    return { code: result.css.toString(), errors: [] }
  }
}

// .styl
const styl = {
  render(
    source: string,
    map: any | null,
    options: any
  ): StylePreprocessorResults {
    const nodeStylus = require('stylus')
    try {
      const ref = nodeStylus(source)
      Object.keys(options).forEach(key => ref.set(key, options[key]))
      if (map) ref.set('sourcemap', { inline: false, comment: false })

      const result = ref.render()
      if (map) {
        return {
          code: result,
          map: merge(map, ref.sourcemap),
          errors: []
        }
      }

      return { code: result, errors: [] }
    } catch (e) {
      return { code: '', errors: [e] }
    }
  }
}

export const processors: { [key: string]: StylePreprocessor } = {
  less,
  sass,
  scss,
  styl,
  stylus: styl
}
