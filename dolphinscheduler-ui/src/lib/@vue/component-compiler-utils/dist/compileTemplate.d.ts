import { VueTemplateCompiler, VueTemplateCompilerOptions } from './types';
import { AssetURLOptions } from './templateCompilerModules/assetUrl';
export interface TemplateCompileOptions {
    source: string;
    filename: string;
    compiler: VueTemplateCompiler;
    compilerOptions?: VueTemplateCompilerOptions;
    transformAssetUrls?: AssetURLOptions | boolean;
    preprocessLang?: string;
    preprocessOptions?: any;
    transpileOptions?: any;
    isProduction?: boolean;
    isFunctional?: boolean;
    optimizeSSR?: boolean;
}
export interface TemplateCompileResult {
    code: string;
    source: string;
    tips: string[];
    errors: string[];
}
export declare function compileTemplate(options: TemplateCompileOptions): TemplateCompileResult;
