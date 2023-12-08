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

import {h, onMounted, Ref, ref, watch} from 'vue'
import {useI18n} from 'vue-i18n'
import {useDatasource} from './use-sqoop-datasource'
import styles from '../index.module.scss'
import type {IJsonItem, IOption, SourceType} from '../types'

export function useTargetType(
    model: { [field: string]: any },
    unCustomSpan: Ref<number>
): IJsonItem[] {
    const {t} = useI18n()
    const hiveSpan = ref(24)
    const hdfsSpan = ref(0)
    const rdbmsSpan = ref(0)
    const dataSourceSpan = ref(0)
    const updateSpan = ref(0)
    const isChange: any = ref(false)
    const rdbmsSourceTypes = ref([
        {
            label: 'MYSQL',
            value: 'MYSQL'
        },
        {
            label: 'ORACLE',
            value: 'ORACLE'
        },
        {
            label: 'SQLSERVER',
            value: 'SQLSERVER'
        },
        {
            label: 'HANA',
            value: 'HANA'
        }
    ] as IOption[])
    const hadoopSourceTypes = ref([
        {
            label: 'HIVE',
            value: 'HIVE'
        }, {
            label: 'HDFS',
            value: 'HDFS'
        }
    ] as IOption[])
    const targetTypes = ref()

    const resetSpan = () => {
        hiveSpan.value = unCustomSpan.value && model.targetType === 'HIVE' ? 24 : 0
        hdfsSpan.value = unCustomSpan.value && model.targetType === 'HDFS' ? 24 : 0
        rdbmsSpan.value =
            unCustomSpan.value && rdbmsSourceTypes.value.some(target => target.value === model.targetType) ? 24 : 0
        dataSourceSpan.value =
            unCustomSpan.value && rdbmsSourceTypes.value.some(target => target.value === model.targetType) ? 24 : 0
        updateSpan.value = rdbmsSpan.value && model.targetMysqlIsUpdate ? 24 : 0
    }

    const getTargetTypesBySourceType = (
        sourceType: SourceType,
        srcQueryType: string
    ): IOption[] => {
        switch (sourceType) {
            case 'MYSQL':
                if (srcQueryType === '1') {
                    return hadoopSourceTypes.value
                }
                return hadoopSourceTypes.value
            case 'HDFS':
            case 'HIVE':
                return rdbmsSourceTypes.value
            default:
                return hadoopSourceTypes.value
        }
    }

    const resetValue = () => {
        if (!isChange.value) {
            isChange.value = true
            return
        }
        switch (model.modelType) {
            case 'import':
                model.targetHiveDatabase = '';
                model.targetHiveTable = '';
                model.targetHdfsTargetPath = '';
                break
            case 'export':
                model.targetMysqlDatasource = '';
                model.targetMysqlTable = '';
                model.targetMysqlColumns = '';
                model.targetMysqlFieldsTerminated = '';
                model.targetMysqlLinesTerminated = '';
                model.targetMysqlTable = '';
                break
            default:
                model.sourceMysqlDatasource = '';
        }

    }

    onMounted(() => {
        targetTypes.value = [...hadoopSourceTypes.value];
    })

    watch(
        () => [model.sourceType, model.srcQueryType],
        ([sourceType, srcQueryType]) => {
            targetTypes.value = getTargetTypesBySourceType(sourceType, srcQueryType)
            model.targetType = targetTypes.value[0].value
        }
    )

    watch(
        () => [unCustomSpan.value, model.targetType, model.targetMysqlIsUpdate],
        () => {
            resetValue();
            resetSpan()
        }
    )

    return [
        {
            type: 'custom',
            field: 'custom-title-target',
            span: unCustomSpan,
            widget: h(
                'div',
                {class: styles['field-title']},
                t('project.node.data_target')
            )
        },
        {
            type: 'select',
            field: 'targetType',
            name: t('project.node.type'),
            span: unCustomSpan,
            options: targetTypes
        },
        {
            type: 'input',
            field: 'targetHiveDatabase',
            name: t('project.node.database'),
            span: hiveSpan,
            props: {
                placeholder: t('project.node.database_tips')
            },
            validate: {
                trigger: ['blur', 'input'],
                required: true,
                validator(validate, value) {
                    if (hiveSpan.value && !value) {
                        return new Error(t('project.node.database_tips'))
                    }
                }
            }
        },
        {
            type: 'input',
            field: 'targetHiveTable',
            name: t('project.node.table'),
            span: hiveSpan,
            props: {
                placeholder: t('project.node.table')
            },
            validate: {
                trigger: ['blur', 'input'],
                required: true,
                validator(rule, value) {
                    if (hiveSpan.value && !value) {
                        return new Error(t('project.node.table_tips'))
                    }
                }
            }
        },
        {
            type: 'switch',
            field: 'targetHiveCreateTable',
            span: hiveSpan,
            name: t('project.node.create_hive_table')
        },
        {
            type: 'switch',
            field: 'targetHiveDropDelimiter',
            span: hiveSpan,
            name: t('project.node.drop_delimiter')
        },
        {
            type: 'switch',
            field: 'targetHiveOverWrite',
            span: hiveSpan,
            name: t('project.node.over_write_src')
        },
        {
            type: 'input',
            field: 'targetHiveTargetDir',
            name: t('project.node.hive_target_dir'),
            span: hiveSpan,
            props: {
                placeholder: t('project.node.hive_target_dir_tips')
            }
        },
        {
            type: 'input',
            field: 'targetHiveReplaceDelimiter',
            name: t('project.node.replace_delimiter'),
            span: hiveSpan,
            props: {
                placeholder: t('project.node.replace_delimiter_tips')
            }
        },
        {
            type: 'input',
            field: 'targetHivePartitionKey',
            name: t('project.node.hive_partition_keys'),
            span: hiveSpan,
            props: {
                placeholder: t('project.node.hive_partition_keys_tips')
            }
        },
        {
            type: 'input',
            field: 'targetHivePartitionValue',
            name: t('project.node.hive_partition_values'),
            span: hiveSpan,
            props: {
                placeholder: t('project.node.hive_partition_values_tips')
            }
        },
        {
            type: 'input',
            field: 'targetHdfsTargetPath',
            name: t('project.node.target_dir'),
            span: hdfsSpan,
            props: {
                placeholder: t('project.node.target_dir_tips')
            },
            validate: {
                trigger: ['blur', 'input'],
                required: true,
                validator(rule, value) {
                    if (hdfsSpan.value && !value) {
                        return new Error(t('project.node.target_dir_tips'))
                    }
                }
            }
        },
        {
            type: 'switch',
            field: 'targetHdfsDeleteTargetDir',
            name: t('project.node.delete_target_dir'),
            span: hdfsSpan
        },
        {
            type: 'radio',
            field: 'targetHdfsCompressionCodec',
            name: t('project.node.compression_codec'),
            span: hdfsSpan,
            options: COMPRESSIONCODECS
        },
        {
            type: 'radio',
            field: 'targetHdfsFileType',
            name: t('project.node.file_type'),
            span: hdfsSpan,
            options: FILETYPES
        },
        {
            type: 'input',
            field: 'targetHdfsFieldsTerminated',
            name: t('project.node.fields_terminated'),
            span: hdfsSpan,
            props: {
                placeholder: t('project.node.fields_terminated_tips')
            }
        },
        {
            type: 'input',
            field: 'targetHdfsLinesTerminated',
            name: t('project.node.lines_terminated'),
            span: hdfsSpan,
            props: {
                placeholder: t('project.node.lines_terminated_tips')
            }
        },
        ...useDatasource(
            model,
            dataSourceSpan,
            'targetMysqlType',
            'targetMysqlDatasource'
        ),
        {
            type: 'input',
            field: 'targetMysqlTable',
            name: t('project.node.table'),
            span: rdbmsSpan,
            props: {
                placeholder: t('project.node.table_tips')
            },
            validate: {
                trigger: ['blur', 'input'],
                required: true,
                validator(validate, value) {
                    if (rdbmsSpan.value && !value) {
                        return new Error(t('project.node.table_tips'))
                    }
                }
            }
        },
        {
            type: 'input',
            field: 'targetMysqlColumns',
            name: t('project.node.column'),
            span: rdbmsSpan,
            props: {
                placeholder: t('project.node.column_tips')
            }
        },
        {
            type: 'input',
            field: 'targetMysqlFieldsTerminated',
            name: t('project.node.fields_terminated'),
            span: rdbmsSpan,
            props: {
                placeholder: t('project.node.fields_terminated_tips')
            }
        },
        {
            type: 'input',
            field: 'targetMysqlLinesTerminated',
            name: t('project.node.lines_terminated'),
            span: rdbmsSpan,
            props: {
                placeholder: t('project.node.lines_terminated_tips')
            }
        },
        {
            type: 'switch',
            field: 'targetMysqlIsUpdate',
            span: rdbmsSpan,
            name: t('project.node.is_update')
        },
        {
            type: 'input',
            field: 'targetMysqlTargetUpdateKey',
            name: t('project.node.update_key'),
            span: updateSpan,
            props: {
                placeholder: t('project.node.update_key_tips')
            }
        },
        {
            type: 'radio',
            field: 'targetMysqlUpdateMode',
            name: t('project.node.update_mode'),
            span: updateSpan,
            options: [
                {
                    label: t('project.node.only_update'),
                    value: 'updateonly'
                },
                {
                    label: t('project.node.allow_insert'),
                    value: 'allowinsert'
                }
            ]
        }
    ]
}

const COMPRESSIONCODECS = [
    {
        label: 'snappy',
        value: 'snappy'
    },
    {
        label: 'lzo',
        value: 'lzo'
    },
    {
        label: 'gzip',
        value: 'gzip'
    },
    {
        label: 'no',
        value: ''
    }
]
const FILETYPES = [
    {
        label: 'avro',
        value: '--as-avrodatafile'
    },
    {
        label: 'sequence',
        value: '--as-sequencefile'
    },
    {
        label: 'text',
        value: '--as-textfile'
    },
    {
        label: 'parquet',
        value: '--as-parquetfile'
    }
]
