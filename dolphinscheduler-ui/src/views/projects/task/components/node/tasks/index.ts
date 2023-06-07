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

import { useFlink } from './use-flink'
import { useFlinkStream } from './use-flink-stream'
import { useShell } from './use-shell'
import { useSubProcess } from './use-sub-process'
import { usePigeon } from './use-pigeon'
import { usePython } from './use-python'
import { useSpark } from './use-spark'
import { useMr } from './use-mr'
import { useHttp } from './use-http'
import { useSql } from './use-sql'
import { useProcedure } from './use-procedure'
import { useSqoop } from './use-sqoop'
import { useSeaTunnel } from './use-sea-tunnel'
import { useSwitch } from './use-switch'
import { useConditions } from './use-conditions'
import { useDataX } from './use-datax'
import { useDependent } from './use-dependent'
import { useDataQuality } from './use-data-quality'
import { useEmr } from './use-emr'
import { useZeppelin } from './use-zeppelin'
import { useK8s } from './use-k8s'
import { useJupyter } from './use-jupyter'
import { useMlflow } from './use-mlflow'
import { useOpenmldb } from './use-openmldb'
import { useDvc } from './use-dvc'
import { useJava } from './use-java'
import { useDinky } from './use-dinky'
import { userSagemaker } from './use-sagemaker'
import { useChunjun } from './use-chunjun'
import { usePytorch } from './use-pytorch'
import { useHiveCli } from './use-hive-cli'
import { useDms } from './use-dms'
import { useDatasync } from './use-datasync'
import { useKubeflow } from './use-kubeflow'
import { useLinkis } from './use-linkis'
import { useDataFactory } from './use-data-factory'
import { useRemoteShell } from './use-remote-shell'
import { useDynamic } from './use-dynamic'

export default {
  SHELL: useShell,
  SUB_PROCESS: useSubProcess,
  DYNAMIC: useDynamic,
  PYTHON: usePython,
  SPARK: useSpark,
  MR: useMr,
  FLINK: useFlink,
  HTTP: useHttp,
  PIGEON: usePigeon,
  SQL: useSql,
  PROCEDURE: useProcedure,
  SQOOP: useSqoop,
  SEATUNNEL: useSeaTunnel,
  SWITCH: useSwitch,
  CONDITIONS: useConditions,
  DATAX: useDataX,
  DEPENDENT: useDependent,
  DATA_QUALITY: useDataQuality,
  EMR: useEmr,
  ZEPPELIN: useZeppelin,
  K8S: useK8s,
  JUPYTER: useJupyter,
  MLFLOW: useMlflow,
  OPENMLDB: useOpenmldb,
  DVC: useDvc,
  DINKY: useDinky,
  SAGEMAKER: userSagemaker,
  CHUNJUN: useChunjun,
  FLINK_STREAM: useFlinkStream,
  JAVA: useJava,
  PYTORCH: usePytorch,
  HIVECLI: useHiveCli,
  DMS: useDms,
  DATASYNC: useDatasync,
  KUBEFLOW: useKubeflow,
  LINKIS: useLinkis,
  DATA_FACTORY: useDataFactory,
  REMOTESHELL: useRemoteShell
}
