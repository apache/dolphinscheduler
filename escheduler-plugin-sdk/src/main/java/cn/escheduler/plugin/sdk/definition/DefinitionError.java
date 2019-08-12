/*
 * Copyright 2017 StreamSets Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.escheduler.plugin.sdk.definition;

import cn.escheduler.plugin.api.ErrorCode;
import cn.escheduler.plugin.api.GenerateResourceBundle;

@GenerateResourceBundle
public enum DefinitionError implements ErrorCode {
    //ConfigValueExtractor
    DEF_000("{}, trigger values cannot have EL expressions '{}'"),
    DEF_001("{}, configuration field is '{}', it must be a boolean type"),
    DEF_002("{}, configuration field is not a numeric type"),
    DEF_003("{}, configuration field is '{}', it must be 'String' or an enum"),
    DEF_004("{}, configuration field is '{}', the value '{}' is an invalid enum"),
    DEF_005("{}, configuration field is '{}', it must be 'java.lang.List'"),
    DEF_006("{}, could not parse default value '{}' as a JSON array: {}"),
    DEF_007("{}, configuration field is '{}', it must be 'java.lang.Map'"),
    DEF_008("{}, could not parse default value '{}' as a JSON map"),
    DEF_009("{}, configuration field is not a character type"),
    DEF_010("{}, the value '{}' is not a character"),
    DEF_011("{}, configuration field is '{}', it must be 'String'"),

    DEF_012("{}, configuration field is a '{}' enum, the list has an invalid enum value: {}"),
    DEF_013("{}, configuration field is '{}', must have a numeric value"),

    DEF_014("{}, configuration field is '{}', it must be 'CredentialValue'"),
    DEF_015("{}, configuration field is '{}', it must be String or Number"),

    //ConfigGroupExtractor
    DEF_100("{} ConfigGroup='{}' is not an enum"),
    DEF_101("{} group '{}' defined more than once"),

    //ConfigDefinitionExtractor
    DEF_150("{} Class='{}' Field='{}', field must public to be a configuration"),
    DEF_151("{} Class='{}' Field='{}', field cannot be static to be a configuration"),
    DEF_152("{} Class='{}' Field='{}', field cannot have both '@ConfigDef' and '@ConfigDefBean' annotations"),
    DEF_153("{}, configuration '{}' depends on an non-existing configuration '{}'"),
    DEF_154("{} Class='{}' Field='{}', field cannot be final to be a configuration"),
    DEF_155("{} Class='{}' Field='{}', field type is not NUMBER, cannot define min or max"),
    DEF_156("{} class '{}' does not have a public default constructor"),
    DEF_157("{} dependsOn name starting with '^' can have only one '^'"),
    DEF_158("{} dependsOn name ending with multiple '^' cannot have other characters in between"),
    DEF_159("{} field has {} '^' but its bean depth is only '{}' ('{}')"),
    DEF_160("{} bean does not have any configuration properties"),
    DEF_161("{} Field='{}' there cannot be nested @ComplexField configs"),
    DEF_162("{} a ConfigDefBean cannot be a primitive type '{}'"),
    DEF_163("{} group ordinal '{}' out of range, valid range '0..{}'"),
    DEF_164("{} invalid group ordinal value: {}"),
    DEF_165("{} group '{}' not in parent groups '{}'"),
    DEF_166("{} field has explicit evaluation but defines the EL function '{}' which is implicitOnly"),

    //ModelDefinitionExtractor
    DEF_200("{}, Model annotation missing'"),
    DEF_201("{}, only one Model annotation is allowed, {}"),
    DEF_202("{}, parser for Model '{}' not found"),
    DEF_210("{}, could not evaluate ChooserValue: {}"),
    DEF_220("{}, could not evaluate ChooserValue: {}"),
    DEF_230("{}, ComplexField configuration must be a list"),

    //StageDefinitionExtractor
    DEF_300("{} does not have a proper annotation"),
    DEF_301("{} version cannot be empty"),
    DEF_302("{} does not implement Source, Processor nor Target"),
    DEF_303("{} a SOURCE cannot be an ErrorStage"),
    DEF_304("{} only a SOURCE can have a RawSourcePreviewer"),
    DEF_305("{} outputStreams '{}' must be an enum"),
    DEF_306("{} a TARGET and EXECUTOR cannot have an OutputStreams"),
    DEF_307("{} the Stage must support at least one execution mode"),
    DEF_308("{} A stage with VariableOutputStreams must have  Stage define a 'outputStreamsDrivenByConfig' config"),
    DEF_309("{} outputStreamsDrivenByConfig='{}' not defined as configuration"),
    DEF_310("{} configuration '{}' has an undefined group '{}'"),
    DEF_311("{} icon file '{}' not found in the classpath"),
    DEF_312("{} only a TARGET can trigger offset commit"),
    DEF_313("{} is hiding non-existing config {}"),
    DEF_314("{} OffsetCommitter can only be a (Pull) Source"),

    DEF_400("Stage library '{}', file '{}' not found"),
    DEF_401("Stage library '{}', could not read file '{}': {}"),

    // General extractor errors
    DEF_500("{} does not implement interface {}"),

    ;

    private final String msg;

    DefinitionError(String msg) {
        this.msg = msg;
    }

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getMessage() {
        return msg;
    }

}
